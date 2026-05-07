package com.anatomia.app.agent

object ContentBank {

    private val byOrgan: Map<String, List<Question>> = mapOf(

        "heart" to listOf(
            Question(
                id = "heart_q1",
                organId = "heart",
                stem = "¿Cuál es la frecuencia cardíaca normal en reposo para un adulto sano?",
                options = listOf("20 – 40 lpm", "60 – 100 lpm", "100 – 140 lpm", "140 – 180 lpm"),
                correctIndex = 1,
                explanation = "60-100 lpm es el rango normal. Por encima de 100 se llama taquicardia; por debajo de 60, bradicardia.",
            ),
            Question(
                id = "heart_q2",
                organId = "heart",
                stem = "¿Qué nombre recibe la condición cuando el corazón late a más de 100 lpm en reposo?",
                options = listOf("Bradicardia", "Arritmia", "Taquicardia", "Fibrilación"),
                correctIndex = 2,
                explanation = "Taquicardia es la frecuencia cardíaca superior a 100 lpm. Puede ser fisiológica (ejercicio) o patológica.",
            ),
            Question(
                id = "heart_q3",
                organId = "heart",
                stem = "¿Cuántos litros de sangre bombea el corazón aproximadamente en un día?",
                options = listOf("~700 L", "~7 000 L", "~70 L", "~70 000 L"),
                correctIndex = 1,
                explanation = "Con ~5 L/min × 1 440 min/día, el corazón bombea alrededor de 7 200 litros diarios.",
            ),
        ),

        "lungs" to listOf(
            Question(
                id = "lungs_q1",
                organId = "lungs",
                stem = "¿Cuál es la frecuencia respiratoria normal de un adulto en reposo?",
                options = listOf("6 – 10 /min", "12 – 20 /min", "25 – 35 /min", "40 – 50 /min"),
                correctIndex = 1,
                explanation = "12-20 respiraciones/min es el rango normal. Por encima de 25 se habla de taquipnea.",
            ),
            Question(
                id = "lungs_q2",
                organId = "lungs",
                stem = "¿Qué valor de saturación de oxígeno en sangre (SpO₂) se considera normal?",
                options = listOf("70 – 80 %", "80 – 90 %", "95 – 100 %", "60 – 70 %"),
                correctIndex = 2,
                explanation = "SpO₂ normal es 95-100 %. Por debajo de 90 % se considera hipoxemia y requiere atención urgente.",
            ),
            Question(
                id = "lungs_q3",
                organId = "lungs",
                stem = "¿Cuál es la capacidad pulmonar total aproximada de un adulto?",
                options = listOf("~1 L", "~3 L", "~6 L", "~12 L"),
                correctIndex = 2,
                explanation = "La capacidad pulmonar total ronda los 6 litros, aunque en reposo solo se usan ~0.5 L por ciclo (volumen tidal).",
            ),
        ),

        "kidneys" to listOf(
            Question(
                id = "kidneys_q1",
                organId = "kidneys",
                stem = "¿Cuántos litros de sangre filtran los riñones aproximadamente al día?",
                options = listOf("~18 L", "~50 L", "~180 L", "~500 L"),
                correctIndex = 2,
                explanation = "Los riñones filtran ~125 mL/min, equivalente a 180 litros de ultrafiltrado al día. La mayoría se reabsorbe.",
            ),
            Question(
                id = "kidneys_q2",
                organId = "kidneys",
                stem = "¿Cómo se llama la condición en que se produce menos de 400 mL de orina al día?",
                options = listOf("Poliuria", "Oliguria", "Anuria", "Hematuria"),
                correctIndex = 1,
                explanation = "Oliguria (< 400 mL/día) indica filtración insuficiente. Anuria (< 100 mL/día) es una emergencia renal.",
            ),
            Question(
                id = "kidneys_q3",
                organId = "kidneys",
                stem = "¿Qué valor de presión arterial se considera normal para proteger la función renal?",
                options = listOf("< 80/50 mmHg", "< 120/80 mmHg", "< 160/100 mmHg", "< 200/120 mmHg"),
                correctIndex = 1,
                explanation = "Mantener la presión por debajo de 120/80 mmHg protege los glomérulos. La hipertensión crónica es causa frecuente de insuficiencia renal.",
            ),
        ),
    )

    fun questionsFor(organId: String): List<Question> = byOrgan[organId] ?: emptyList()

    fun organName(organId: String): String = when (organId) {
        "heart"   -> "el Corazón"
        "lungs"   -> "los Pulmones"
        "kidneys" -> "los Riñones"
        else      -> organId
    }
}
