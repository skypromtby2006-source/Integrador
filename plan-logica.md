# Plan — Fase Lógica
## Orden de implementación y dependencias

> Ejecutar los fixes en el orden numerado.
> Cada fix depende del anterior. No saltar pasos.

---

## Mapa del problema

```
ContentBank (JSON) ──┐
DecisionEngine ───────┤──► [DESCONECTADO] ──► QuizScreen (hardcoded)
AgentRepository (RAM)─┘                  └──► AgentScreen (hardcoded)

Lo que hay que construir:

ContentBank ──► QuizViewModel ──► QuizScreen
                      └──────────► QuizResultsScreen

AgentRepository ──► AgentDashboardViewModel ──► AgentScreen (M3)
DecisionEngine  ──┘
```

---

## Secuencia de fixes

| # | Archivo(s) | Qué hace | Depende de |
|---|-----------|----------|------------|
| 03 | `Screen.kt` + `App.kt` | Añade `organId` a la ruta Quiz | nada |
| 04 | `QuizViewModel.kt` (nuevo) | Puente ContentBank → UI | fix 03 |
| 05 | `QuizScreen.kt` | Conectar al QuizViewModel real | fix 04 |
| 06 | `QuizResultsScreen.kt` | Resultados reales desde ViewModel | fix 04 |
| 07 | `AgentDashboardViewModel.kt` (nuevo) | Estado del dashboard BDI | fix 03 |
| 08 | `AgentScreen.kt` (M3) | Conectar al AgentDashboardViewModel | fix 07 |
| 09 | `HomeScreen.kt` | Fecha real + nombre desde estado | fix 03 |
| 10 | SQLDelight | Persistencia entre sesiones | fix 04, 07 |

---

## Decisiones de arquitectura

**¿Por qué no reusar AgentViewModel viejo directamente?**
El AgentViewModel viejo expone un estado de chat (mensajes burbujas + pregunta actual).
La AgentScreen M3 muestra un dashboard (creencias, meta, pasos).
Son dos paradigmas de UX distintos. Se crea `AgentDashboardViewModel` que lee
los mismos datos (AgentRepository + DecisionEngine) pero los expone en formato dashboard.
El AgentViewModel viejo se puede conservar para la pantalla de chat BDI en el futuro.

**¿Por qué QuizViewModel en commonMain?**
ContentBank ya está en commonMain. QuizViewModel lo consume directamente.
Moverlo a androidMain obligaría a duplicar lógica para Desktop.

**¿Por qué SharedViewModel entre Quiz y QuizResults?**
Los resultados del quiz (respuestas, score) los genera QuizScreen y los necesita
QuizResultsScreen. La forma más limpia en KMP sin DI es compartir el mismo
ViewModel instance via el NavBackStackEntry del NavHost. Se crea el VM a nivel
de la ruta padre y ambas pantallas lo consumen.

**¿Por qué organId en la ruta?**
Hoy hay 3 órganos (heart, lungs, kidneys). La Home ya tiene un hero con
"Sistema Circulatorio" → el organId viaja por navegación para que Quiz cargue
las preguntas correctas del JSON. MVP default: "heart".
