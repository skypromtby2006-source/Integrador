package ui

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import models.Estudiante
import models.Evaluacion
import models.Pregunta
import java.awt.Desktop
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PdfExporter {

    private val formatter   = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")
    private val displayFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun exportEstudiantes(estudiantes: List<Estudiante>, filtros: String = ""): Boolean {
        val file = outputFile("reporte_estudiantes")
        return try {
            Document(PdfDocument(PdfWriter(file))).use { doc ->
                header(doc, "Reporte de Estudiantes", filtros)
                val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 20f, 20f, 20f, 15f, 10f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                tableHeader(table, listOf("C.I.", "Nombre", "Apellido", "Email", "Curso", "Estado"))
                estudiantes.forEach { e ->
                    tableRow(table, listOf(
                        e.usuarioId, e.nombre, e.apellido,
                        e.email, e.claseNombre, e.estado
                    ), highlight = e.estado == "suspendido")
                }
                doc.add(table)
                footer(doc, estudiantes.size, "estudiante")
            }
            openFile(file)
            true
        } catch (e: Exception) {
            println("[PDF] Error: ${e.message}")
            false
        }
    }

    fun exportEliminados(eliminados: List<Estudiante>): Boolean {
        val file = outputFile("lista_eliminados")
        return try {
            Document(PdfDocument(PdfWriter(file))).use { doc ->
                header(doc, "Lista de Estudiantes Eliminados (Baja Lógica)", "Estado: inactivo / suspendido")
                val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 20f, 20f, 25f, 20f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                tableHeader(table, listOf("C.I.", "Nombre", "Apellido", "Email", "Estado"))
                eliminados.forEach { e ->
                    tableRow(table, listOf(
                        e.usuarioId, e.nombre, e.apellido, e.email, e.estado
                    ), highlight = true)
                }
                doc.add(table)
                footer(doc, eliminados.size, "estudiante eliminado")
            }
            openFile(file)
            true
        } catch (e: Exception) {
            println("[PDF] Error: ${e.message}")
            false
        }
    }

    fun exportPreguntas(preguntas: List<Pregunta>, filtros: String = ""): Boolean {
        val file = outputFile("reporte_preguntas")
        return try {
            Document(PdfDocument(PdfWriter(file))).use { doc ->
                header(doc, "Reporte de Preguntas", filtros)
                val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 25f, 20f, 10f, 15f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                tableHeader(table, listOf("Enunciado", "Contenido", "Subtema", "Nivel", "Respuesta correcta"))
                preguntas.forEach { p ->
                    val correcta = p.opciones.find { it.esCorrecta }?.texto ?: ""
                    tableRow(table, listOf(
                        p.enunciado.take(60), p.tituloContenido, p.subtema,
                        p.nivelDificultad.toString(), correcta.take(30)
                    ))
                }
                doc.add(table)
                footer(doc, preguntas.size, "pregunta")
            }
            openFile(file)
            true
        } catch (e: Exception) {
            println("[PDF] Error: ${e.message}")
            false
        }
    }

    fun exportEvaluaciones(evaluaciones: List<Evaluacion>, filtros: String = ""): Boolean {
        val file = outputFile("reporte_evaluaciones")
        return try {
            Document(PdfDocument(PdfWriter(file))).use { doc ->
                header(doc, "Reporte de Evaluaciones", filtros)
                val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 30f, 15f, 15f, 10f)))
                    .setWidth(UnitValue.createPercentValue(100f))
                tableHeader(table, listOf("Título", "Contenido", "Nivel", "Intentos", "Creada"))
                evaluaciones.forEach { e ->
                    tableRow(table, listOf(
                        e.titulo, e.tituloContenido,
                        e.nivelDificultad.toString(),
                        e.totalIntentos.toString(),
                        e.creadoEn.take(10)
                    ))
                }
                doc.add(table)
                footer(doc, evaluaciones.size, "evaluación")
            }
            openFile(file)
            true
        } catch (e: Exception) {
            println("[PDF] Error: ${e.message}")
            false
        }
    }

    private fun outputFile(name: String): File {
        val dir = File(System.getProperty("user.home"), "Didactai-Reportes")
        dir.mkdirs()
        return File(dir, "${name}_${LocalDateTime.now().format(formatter)}.pdf")
    }

    private fun header(doc: Document, titulo: String, filtros: String) {
        doc.add(Paragraph("Didactai · Panel del Maestro")
            .setFontSize(10f).setFontColor(ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.RIGHT))
        doc.add(Paragraph(titulo)
            .setFontSize(18f).setBold()
            .setTextAlignment(TextAlignment.CENTER))
        if (filtros.isNotBlank()) {
            doc.add(Paragraph("Filtros: $filtros")
                .setFontSize(9f).setFontColor(ColorConstants.GRAY))
        }
        doc.add(Paragraph("Generado: ${LocalDateTime.now().format(displayFmt)}")
            .setFontSize(9f).setFontColor(ColorConstants.GRAY)
            .setMarginBottom(12f))
    }

    private fun tableHeader(table: Table, headers: List<String>) {
        headers.forEach { h ->
            table.addHeaderCell(Cell().add(Paragraph(h).setBold().setFontSize(10f))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(6f))
        }
    }

    private fun tableRow(table: Table, values: List<String>, highlight: Boolean = false) {
        values.forEach { v ->
            val cell = Cell().add(Paragraph(v).setFontSize(9f)).setPadding(5f)
            if (highlight) cell.setBackgroundColor(ColorConstants.PINK)
            table.addCell(cell)
        }
    }

    private fun footer(doc: Document, count: Int, unit: String) {
        doc.add(Paragraph("\nTotal: $count $unit${if (count != 1) "s" else ""}")
            .setFontSize(10f).setBold().setMarginTop(8f))
    }

    private fun openFile(file: File) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file)
            println("[PDF] Guardado en: ${file.absolutePath}")
        } catch (_: Exception) {
            println("[PDF] Guardado en: ${file.absolutePath}")
        }
    }
}
