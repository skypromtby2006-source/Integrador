# PRUEBA-GENERAL — Verificación completa de ambas apps

## Objetivo
Verificar que todos los cambios aplicados (FIX-A al FIX-D, FIX-T1T2, FEATURE-T3)
compilan, arrancan y funcionan correctamente antes de hacer commit final.

Ejecuta cada sección EN ORDEN. Si una sección falla, reporta el error exacto
y detente — no continúes a la siguiente sección hasta que esté resuelto.

---

## SECCIÓN 1 — Compilación Android (Student App)

### 1a — Build debug

Desde la raíz del proyecto `android/`:

```bash
./gradlew :composeApp:assembleDebug 2>&1 | tail -30
```

**Resultado esperado:** `BUILD SUCCESSFUL`

Si falla, reporta las líneas que contienen `error:` o `ERROR`.

### 1b — Verificar imports no rotos

```bash
./gradlew :composeApp:compileKotlinAndroid 2>&1 | grep -i "error\|unresolved\|not found" | head -20
```

**Resultado esperado:** sin líneas de error.

### 1c — Verificar archivos clave existen

```bash
find composeApp/src -name "EditProfileViewModel.kt" -o \
                    -name "HistoryViewModel.kt" -o \
                    -name "AgentDashboardViewModel.kt" | sort
```

**Resultado esperado:** los 3 archivos encontrados.

```bash
# Verificar que AgentScreen legacy fue eliminado
find composeApp/src -path "*/screen/agent/AgentScreen.kt" && echo "LEGACY AUN EXISTE" || echo "OK - legacy eliminado"
```

**Resultado esperado:** `OK - legacy eliminado`

---

## SECCIÓN 2 — Compilación Teacher App

### 2a — Build

Desde `teacher-app/`:

```bash
./gradlew run --dry-run 2>&1 | tail -20
```

o si usan `package`:

```bash
./gradlew packageDistributionForCurrentOS 2>&1 | tail -20
```

**Resultado esperado:** `BUILD SUCCESSFUL`

### 2b — Verificar archivo nuevo existe

```bash
find src -name "SessionProgressScreen.kt" && echo "OK" || echo "FALTA SessionProgressScreen.kt"
```

**Resultado esperado:** `OK`

### 2c — Verificar que Screen.Progress está en App.kt

```bash
grep -n "Screen.Progress\|Progress.*BarChart\|SessionProgressScreen" src/main/kotlin/ui/App.kt
```

**Resultado esperado:** mínimo 2 líneas (una en el enum, una en el when).

---

## SECCIÓN 3 — Verificar base de datos

### 3a — Tabla sesion_quiz existe

Ejecuta contra la base de datos del teacher:

```bash
grep -A 15 "sesion_quiz" setup-db.sh
```

**Resultado esperado:** el bloque CREATE TABLE completo con sus índices.

### 3b — Ejecutar el script SQL en modo dry-run

```bash
# Solo verificar sintaxis sin ejecutar en producción
grep -c "CREATE TABLE\|CREATE INDEX" setup-db.sh
```

**Resultado esperado:** número mayor o igual a los que había antes + 3
(1 tabla nueva + 2 índices nuevos).

---

## SECCIÓN 4 — Verificar endpoints HTTP

### 4a — Arrancar el servidor Teacher App

```bash
./gradlew run &
sleep 5
```

### 4b — Ping

```bash
curl -s http://localhost:8080/ping | python3 -m json.tool
```

**Resultado esperado:**
```json
{
    "status": "ok",
    "version": "2.0"
}
```

### 4c — Endpoint de sesión existe

```bash
curl -s -X POST http://localhost:8080/progreso/sesion \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": "test123",
    "organId": "heart",
    "answers": [
      {"questionId": "q1", "wasCorrect": true},
      {"questionId": "q2", "wasCorrect": false}
    ],
    "timestamp": '"$(date +%s%3N)"'
  }' | python3 -m json.tool
```

**Resultado esperado:**
```json
{
    "ok": true
}
```

Si retorna `404` o `500`, reporta la respuesta completa.

Si retorna error de BD (estudiante no existe por FK), es esperado —
significa que el endpoint existe y funciona, solo que el `test123` no
está en la tabla `usuario`. Reporta ese caso como ✅ PARCIAL.

### 4d — Endpoint GET sesiones

```bash
curl -s http://localhost:8080/progreso/sesiones/test123 | python3 -m json.tool
```

**Resultado esperado:** `{"ok": true, "data": [...]}` — array puede estar vacío.

### 4e — Detener el servidor

```bash
pkill -f "teacher-app" || true
```

---

## SECCIÓN 5 — Verificar flujo Android: datos reales en Agente

### 5a — AgentDashboardViewModel no tiene hardcodes

```bash
grep -n '"Ana"\|weekNumber = 6' \
  android/composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/AgentDashboardViewModel.kt
```

**Resultado esperado:** 0 líneas (sin resultados). Si aparece alguna, es
que el hardcode no fue eliminado.

### 5b — EditProfileViewModel existe y tiene save()

```bash
grep -n "fun save\|SessionRepository.save\|EditProfileResult" \
  android/composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/EditProfileViewModel.kt | head -10
```

**Resultado esperado:** mínimo 3 líneas.

### 5c — HistoryScreen no tiene datos hardcodeados

```bash
grep -n 'HistoryEntry("h1"\|listOf.*HistoryGroup\|private val historyGroups' \
  android/composeApp/src/commonMain/kotlin/com/anatomia/app/ui/screens/HistoryScreen.kt
```

**Resultado esperado:** 0 líneas.

### 5d — ProgressService usa el endpoint correcto

```bash
grep -n "progreso/sesion\|submitProgress\|/progress\|/progreso" \
  android/composeApp/src/commonMain/kotlin/com/anatomia/app/network/ProgressService.kt
```

**Resultado esperado:** al menos una línea con `/progreso/sesion`.

---

## SECCIÓN 6 — Verificar manejo de errores Teacher App

```bash
grep -n "loadError\|try {\|catch (e" \
  teacher-app/src/main/kotlin/ui/ClassesScreen.kt | head -10
echo "---"
grep -n "loadError\|try {\|catch (e" \
  teacher-app/src/main/kotlin/ui/StudentsScreen.kt | head -10
echo "---"
grep -n "loadError\|fun reload\|suspend fun reload" \
  teacher-app/src/main/kotlin/ui/EvaluacionesScreen.kt | head -10
```

**Resultado esperado:**
- ClassesScreen: mínimo 3 líneas (loadError, try, catch)
- StudentsScreen: mínimo 3 líneas
- EvaluacionesScreen: `fun reload()` sin `suspend` + líneas de loadError

---

## SECCIÓN 7 — Reporte final

Al terminar todas las secciones, genera un reporte con este formato:

```
PRUEBA GENERAL — RESULTADO

SECCIÓN 1 Android Build:     [ PASS / FAIL ]
SECCIÓN 2 Teacher Build:     [ PASS / FAIL ]
SECCIÓN 3 Base de datos:     [ PASS / FAIL ]
SECCIÓN 4 Endpoints HTTP:    [ PASS / FAIL / PARCIAL ]
SECCIÓN 5 Flujo Android:     [ PASS / FAIL ]
SECCIÓN 6 Error handling:    [ PASS / FAIL ]

Errores encontrados:
- [lista de errores si los hay]

Archivos que requieren corrección:
- [lista de archivos si aplica]
```

Si todas las secciones pasan → el proyecto está listo para commit.
Si alguna falla → reporta el error exacto antes de hacer cualquier corrección.
