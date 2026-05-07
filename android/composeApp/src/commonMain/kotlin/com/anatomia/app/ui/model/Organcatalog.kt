package com.anatomia.app.ui.model
/**
 * Catálogo estático de órganos con sus datos clínicos de importancia.
 *
 * Por ahora es un object con datos hardcodeados — esto es intencional para el MVP.
 * La arquitectura ya está preparada para que este objeto sea reemplazado por
 * un repositorio que lea de SQLDelight sin cambiar nada en la UI.
 *
 * Los datos clínicos NO son solo estadísticas frías — están redactados para
 * que un estudiante entienda cuándo un valor es normal vs cuándo es alerta.
 */
object OrganCatalog {

    fun findById(id: String): OrganUiModel? = all.find { it.id == id }

    val all: List<OrganUiModel> = listOf(

        OrganUiModel(
            id = "heart",
            name = "Corazón",
            systemName = "Sistema cardiovascular",
            description = "Músculo que bombea sangre sin descanso. " +
                    "Cada latido empuja oxígeno y nutrientes a cada célula del cuerpo.",
            facts = listOf(
                OrganFact(
                    iconName = "monitor_heart",
                    label = "Pulso normal",
                    value = "60 – 100 lpm",
                    alertNote = "En reposo, adulto sano",
                ),
                OrganFact(
                    iconName = "warning",
                    label = "Pulso de alerta",
                    value = "> 100 lpm",
                    alertNote = "Puede indicar taquicardia",
                ),
                OrganFact(
                    iconName = "emoji_events",
                    label = "Pulso atleta",
                    value = "40 – 60 lpm",
                    alertNote = "Corazón más eficiente",
                ),
                OrganFact(
                    iconName = "water_drop",
                    label = "Bombeo diario",
                    value = "~7,000 L",
                    alertNote = "Litros de sangre al día",
                ),
            ),
        ),

        OrganUiModel(
            id = "lungs",
            name = "Pulmones",
            systemName = "Sistema respiratorio",
            description = "Intercambian oxígeno del aire con el dióxido de carbono " +
                    "de la sangre. Sin este intercambio las células mueren en minutos.",
            facts = listOf(
                OrganFact(
                    iconName = "air",
                    label = "Respiraciones",
                    value = "12 – 20 /min",
                    alertNote = "Frecuencia normal adulto",
                ),
                OrganFact(
                    iconName = "warning",
                    label = "Alerta (taquipnea)",
                    value = "> 25 /min",
                    alertNote = "Puede indicar problema",
                ),
                OrganFact(
                    iconName = "open_in_full",
                    label = "Capacidad total",
                    value = "~6 L",
                    alertNote = "Capacidad pulmonar total",
                ),
                OrganFact(
                    iconName = "percent",
                    label = "O₂ en sangre",
                    value = "95 – 100%",
                    alertNote = "Saturación normal (SpO₂)",
                ),
            ),
        ),

        OrganUiModel(
            id = "kidneys",
            name = "Riñones",
            systemName = "Sistema urinario",
            description = "Filtran la sangre eliminando desechos. " +
                    "También regulan la presión arterial y el balance de sales en el cuerpo.",
            facts = listOf(
                OrganFact(
                    iconName = "filter_alt",
                    label = "Filtrado diario",
                    value = "~180 L",
                    alertNote = "De sangre filtrada al día",
                ),
                OrganFact(
                    iconName = "opacity",
                    label = "Orina producida",
                    value = "1 – 2 L/día",
                    alertNote = "Rango normal adulto",
                ),
                OrganFact(
                    iconName = "warning",
                    label = "Alerta (oliguria)",
                    value = "< 400 mL/día",
                    alertNote = "Producción insuficiente",
                ),
                OrganFact(
                    iconName = "favorite",
                    label = "Presión normal",
                    value = "< 120/80",
                    alertNote = "mmHg para proteger riñones",
                ),
            ),
        ),
    )
}


