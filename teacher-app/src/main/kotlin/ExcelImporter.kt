import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

data class EstudianteImportRow(
    val nombre          : String,
    val segundoNombre   : String = "",
    val apPaterno       : String,
    val apMaterno       : String = "",
    val ci              : String,
    val complementoCi   : String = "",
    val fechaNacimiento : String,
    val correoPersonal  : String = "",
    val claseNombre     : String,
    val rowNumber       : Int,
    val errors          : List<String> = emptyList(),
) {
    val isValid: Boolean get() = errors.isEmpty()
    val nombreCompleto: String get() = listOf(nombre, segundoNombre, apPaterno, apMaterno)
        .filter { it.isNotBlank() }.joinToString(" ")
    val apellidoCompleto: String get() = listOf(apPaterno, apMaterno)
        .filter { it.isNotBlank() }.joinToString(" ")
}

object ExcelImporter {

    fun parse(file: File): List<EstudianteImportRow> {
        val rows = mutableListOf<EstudianteImportRow>()

        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)

            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue

                val nombre        = row.cellString(0)
                val segundoNombre = row.cellString(1)
                val apPaterno     = row.cellString(2)
                val apMaterno     = row.cellString(3)
                val ci            = row.cellString(4)
                val complementoCi = row.cellString(5)
                val fechaNac      = row.cellString(6)
                val correo        = row.cellString(7)
                val claseNombre   = row.cellString(8)

                if (listOf(nombre, apPaterno, ci, claseNombre).all { it.isBlank() }) continue

                val errors = mutableListOf<String>()
                if (nombre.isBlank())    errors += "Nombre requerido"
                if (apPaterno.isBlank()) errors += "Apellido paterno requerido"
                if (ci.isBlank() || !ci.all { it.isDigit() } || ci.length !in 6..8)
                    errors += "C.I. inválida (debe tener 6-8 dígitos)"
                if (fechaNac.isBlank()) errors += "Fecha de nacimiento requerida"
                else try {
                    val date = java.time.LocalDate.parse(fechaNac)
                    if (date.isAfter(java.time.LocalDate.now()))
                        errors += "Fecha de nacimiento no puede ser futura"
                } catch (_: Exception) {
                    errors += "Formato de fecha inválido (usar YYYY-MM-DD)"
                }
                if (correo.isNotBlank() &&
                    !Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$").matches(correo))
                    errors += "Correo inválido"
                if (claseNombre.isBlank()) errors += "Clase requerida"

                rows.add(EstudianteImportRow(
                    nombre          = nombre,
                    segundoNombre   = segundoNombre,
                    apPaterno       = apPaterno,
                    apMaterno       = apMaterno,
                    ci              = ci,
                    complementoCi   = complementoCi,
                    fechaNacimiento = fechaNac,
                    correoPersonal  = correo,
                    claseNombre     = claseNombre,
                    rowNumber       = rowIndex + 1,
                    errors          = errors,
                ))
            }
        }
        return rows
    }

    private fun org.apache.poi.ss.usermodel.Row.cellString(index: Int): String {
        val cell = getCell(index) ?: return ""
        return when (cell.cellType) {
            CellType.STRING  -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val n = cell.numericCellValue
                if (n == n.toLong().toDouble()) n.toLong().toString() else n.toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }
}
