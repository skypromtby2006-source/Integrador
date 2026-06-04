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

    private val heartPois = listOf(
        OrganPoi(
            name        = "poi_auricula_derecha",
            label       = "Aurícula derecha",
            description = "Cámara superior derecha que recibe sangre desoxigenada del cuerpo vía vena cava.",
            topic       = "Cámaras del corazón",
            facts       = listOf(
                "Recibe sangre de la vena cava superior e inferior",
                "Bombea hacia el ventrículo derecho",
                "Tiene paredes más delgadas que los ventrículos",
            )
        ),
        OrganPoi(
            name        = "poi_auricula_izquierda",
            label       = "Aurícula izquierda",
            description = "Cámara superior izquierda que recibe sangre oxigenada de los pulmones.",
            topic       = "Cámaras del corazón",
            facts       = listOf(
                "Recibe sangre de las 4 venas pulmonares",
                "Bombea hacia el ventrículo izquierdo",
                "Es la cámara con mayor riesgo de fibrilación auricular",
            )
        ),
        OrganPoi(
            name        = "poi_ventriculo_derecho",
            label       = "Ventrículo derecho",
            description = "Cámara inferior derecha que bombea sangre hacia los pulmones.",
            topic       = "Cámaras del corazón",
            facts       = listOf(
                "Bombea sangre a través de la arteria pulmonar",
                "Genera menor presión que el ventrículo izquierdo",
                "Tiene forma de media luna en corte transversal",
            )
        ),
        OrganPoi(
            name        = "poi_ventriculo_izquierdo",
            label       = "Ventrículo izquierdo",
            description = "Cámara inferior izquierda — la más potente, bombea sangre a todo el cuerpo.",
            topic       = "Cámaras del corazón",
            facts       = listOf(
                "Genera la mayor presión del corazón (sistólica)",
                "Sus paredes son 3 veces más gruesas que el derecho",
                "Bombea sangre a través de la aorta",
            )
        ),
        OrganPoi(
            name        = "poi_valvula_mitral",
            label       = "Válvula mitral",
            description = "Válvula bicúspide entre aurícula y ventrículo izquierdo. Evita el reflujo.",
            topic       = "Válvulas cardíacas",
            facts       = listOf(
                "Tiene dos valvas (bicúspide)",
                "Se abre cuando la aurícula se contrae",
                "El prolapso mitral es la valvulopatía más común",
            )
        ),
        OrganPoi(
            name        = "poi_aorta",
            label       = "Aorta",
            description = "La arteria más grande del cuerpo. Sale del ventrículo izquierdo y distribuye sangre oxigenada.",
            topic       = "Flujo sanguíneo",
            facts       = listOf(
                "Tiene 2.5 cm de diámetro aproximadamente",
                "Se divide en aorta ascendente, arco aórtico y aorta descendente",
                "Contiene barorreceptores que regulan la presión arterial",
            )
        ),
        OrganPoi(
            name        = "poi_arteria_pulmonar",
            label       = "Arteria pulmonar",
            description = "Única arteria que transporta sangre desoxigenada. Sale del ventrículo derecho.",
            topic       = "Circulación pulmonar",
            facts       = listOf(
                "Transporta sangre con CO₂ hacia los pulmones",
                "Es la única arteria con sangre venosa",
                "Se divide en arteria pulmonar derecha e izquierda",
            )
        ),
    )

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
            pois = heartPois,
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


