package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("url") ?: "jdbc:postgresql://localhost:5432/embly_backend"
            driverClassName = "org.postgresql.Driver"
            username = System.getenv("DB_USER") ?: "naruto"
            password = System.getenv("DB_PASSWORD") ?: "naruto@305"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}