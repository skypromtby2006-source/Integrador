package com.anatomia.app.ui.model
/**
 * Representa un órgano tal como la UI necesita verlo.
 *
 * Separamos este modelo del dominio (que vivirá en el agente/SQLDelight)
 * para que la UI nunca dependa directamente de la capa de datos.
 * Si mañana cambia el esquema de BD, solo actualizamos el mapper — la UI no se toca.
 */
data class OrganUiModel(
    val id: String,
    val name: String,
    val systemName: String,
    val description: String,
    val facts: List<OrganFact>,
)

/**
 * Un dato clínico de importancia para el estudiante.
 *
 * [iconName] usa los nombres de Material Symbols que Compose soporta nativamente.
 * [alertNote] es la nota que explica cuándo ese valor es relevante clínicamente.
 */
data class OrganFact(
    val iconName: String,
    val label: String,
    val value: String,
    val alertNote: String,
)

/**
 * Estado completo de la pantalla del modelo 3D.
 *
 * Usamos un sealed interface para representar los tres estados posibles
 * del bottom sheet. Esto evita booleans sueltos como "isSheetOpen" + "isLoading"
 * que pueden combinarse en estados imposibles (ej: isOpen=true e isLoading=true
 * al mismo tiempo sin órgano seleccionado).
 */
sealed interface BodyModelUiState {
    /** No hay órgano seleccionado, sheet oculto */
    data object Idle : BodyModelUiState

    /** Unity disparó OrganSelected, sheet visible con datos */
    data class OrganFocused(
        val organ: OrganUiModel,
        val activeSystem: AnatomySystem,
    ) : BodyModelUiState
}

enum class AnatomySystem(val displayName: String) {
    CARDIOVASCULAR("Cardiovascular"),
    RESPIRATORY("Respiratorio"),
    DIGESTIVE("Digestivo"),
    NERVOUS("Nervioso"),
}

