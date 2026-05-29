package com.anatomia.app.db

object DatabaseProvider {
    private var _database: DidactaiDatabase? = null

    val database: DidactaiDatabase
        get() = _database ?: error("DatabaseProvider no inicializado. Llama init() primero.")

    fun init(factory: DatabaseDriverFactory) {
        if (_database == null) {
            _database = DidactaiDatabase(factory.createDriver())
        }
    }
}
