# HOTFIX-02 — Seed de contenido biológico al arrancar el servidor

**Tipo:** Bug crítico — QuestionsScreen bloqueada por tablas vacías
**Archivo a modificar:** `src/main/kotlin/db/DatabaseConfig.kt`
**NO tocar:** ningún archivo de UI ni de rutas

---

## Contexto

Las tablas `contenido` y `contenido_biologico` están vacías.
`QuestionsScreen` llama a `ContenidoRepository.getAll()` y recibe
`emptyList()` → muestra "No hay contenido biológico disponible".

La solución es insertar 3 filas de seed al arrancar el servidor,
una por cada modelo 3D disponible en la app Android.

## Docentes existentes en la BD

| usuario_id | nombre | apellido |
|---|---|---|
| 12395472 | Carlos | Mendoza |
| 45986456 | Patricia | Rojas |

El seed usará `12395472` como `creadoPor`.

## Modelos 3D y su mapeo

| organId Android | Título contenido | Categoría | Modelo 3D |
|---|---|---|---|
| heart | Corazón humano | Circulatorio | organ_heart.glb ✅ |
| lungs | Pulmones | Respiratorio | organ_lungs.glb (pendiente) |
| kidneys | Riñones | Urinario | organ_kidneys.glb (pendiente) |

Los títulos deben contener exactamente "Corazón", "Pulmon" y "Riñon"
para que el keyword search del HOTFIX-01 funcione correctamente.

## Qué hacer

### Paso 1 — Crear función `seedContenidoBiologico` en DatabaseConfig.kt

Agregar esta función privada al objeto `DatabaseConfig`:

```kotlin
private fun seedContenidoBiologico() {
    transaction {
        // Solo insertar si las tablas están vacías
        val count = ContenidoBiologicoTable.selectAll().count()
        if (count > 0L) {
            println("[SEED] Contenido biológico ya existe ($count registros) — omitiendo seed")
            return@transaction
        }

        val docenteId = "12395472"  // Carlos Mendoza — docente existente
        val now = java.time.LocalDateTime.now()

        val organs = listOf(
            Triple(
                "Corazón humano",
                "Anatomía y fisiología del corazón: cámaras, válvulas y ciclo cardíaco.",
                "Circulatorio"
            ),
            Triple(
                "Pulmones",
                "Anatomía del sistema respiratorio: bronquios, alvéolos e intercambio gaseoso.",
                "Respiratorio"
            ),
            Triple(
                "Riñones",
                "Anatomía del sistema urinario: nefronas, filtración y producción de orina.",
                "Urinario"
            ),
        )

        organs.forEach { (titulo, descripcion, categoria) ->
            val newId = java.util.UUID.randomUUID()

            // 1. Insertar en tabla padre contenido
            ContenidoTable.insert {
                it[contenidoId] = newId
                it[tipo]        = "biologico"
                it[createdAt]   = now
            }

            // 2. Insertar en tabla hija contenido_biologico
            ContenidoBiologicoTable.insert {
                it[contenidoId]         = newId
                it[ContenidoBiologicoTable.titulo]       = titulo
                it[ContenidoBiologicoTable.descripcion]  = descripcion
                it[ContenidoBiologicoTable.categoria]    = categoria
                it[nivelDificultad]     = 1
                it[activo]              = true
                it[creadoPor]           = docenteId
                it[ContenidoBiologicoTable.createdAt]    = now
            }

            println("[SEED] Contenido insertado: $titulo")
        }

        println("[SEED] 3 contenidos biológicos insertados correctamente")
    }
}
```

### Paso 2 — Llamar seedContenidoBiologico desde init()

Actualizar la función `init()`:

```kotlin
fun init() {
    Database.connect(url = URL, driver = "org.postgresql.Driver",
        user = USER, password = PASSWORD)
    println("[DB] Conectado a PostgreSQL — esquema Didactai v2")
    seedContenidoBiologico()   // ← agregar esta línea
}
```

### Paso 3 — Verificar imports necesarios

```kotlin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
```

Si ya están importados (probable — el archivo ya usa Exposed),
no agregarlos de nuevo.

---

## Criterio de éxito

- El servidor arranca y muestra en consola:
  ```
  [SEED] Contenido insertado: Corazón humano
  [SEED] Contenido insertado: Pulmones
  [SEED] Contenido insertado: Riñones
  [SEED] 3 contenidos biológicos insertados correctamente
  ```
- En reinicios posteriores muestra:
  ```
  [SEED] Contenido biológico ya existe (3 registros) — omitiendo seed
  ```
- Al abrir QuestionsScreen, el dropdown "Contenido biológico" muestra
  las 3 opciones: Corazón humano, Pulmones, Riñones.
- El endpoint `/questions/by-organ/heart` devuelve las preguntas
  asociadas al contenido "Corazón humano" una vez que se creen.

---

## Reporte requerido al finalizar

1. ¿Compiló sin errores?
2. Output exacto de la consola al arrancar el servidor
3. ¿QuestionsScreen muestra los 3 contenidos en el dropdown?
4. ¿El guard `if (count > 0L) return@transaction` funciona en el segundo arranque?
5. Bloqueantes
