package server

data class LogEntry(
    val hora:   String,
    val metodo: String,
    val ruta:   String,
    val status: Int
)

object RequestLog {
    private val entries = ArrayDeque<LogEntry>(50)

    @Synchronized
    fun agregar(entry: LogEntry) {
        if (entries.size >= 50) entries.removeFirst()
        entries.addLast(entry)
    }

    @Synchronized
    fun obtenerTodos(): List<LogEntry> = entries.toList().reversed()

    @Synchronized
    fun limpiar() = entries.clear()
}
