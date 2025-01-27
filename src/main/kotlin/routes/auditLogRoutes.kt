package routes

import database.AuditLogs
import database.Factories
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.AuditLogResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.auditLogRoutes() {

    // Get All Audit Logs
    get("/audit-logs") {
        val logs = transaction {
            AuditLogs.selectAll()
                .map {
                    AuditLogResponse(
                        it[AuditLogs.id],
                        it[AuditLogs.factoryId],
                        it[AuditLogs.orderId],
                        it[AuditLogs.taskId],
                        it[AuditLogs.workerId],
                        it[AuditLogs.action],
                        it[AuditLogs.timestamp].toString()
                    )
                }
        }
        call.respond(logs)
    }

    // Get Audit Logs by Order ID
    get("/audit-logs/order/{orderId}") {
        val orderId = call.parameters["orderId"]?.toIntOrNull()
        if (orderId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid order ID")
            return@get
        }

        val logs = transaction {
            AuditLogs.selectAll().where { AuditLogs.orderId eq orderId }
                .map {
                    AuditLogResponse(
                        it[AuditLogs.id],
                        it[AuditLogs.factoryId],
                        it[AuditLogs.orderId],
                        it[AuditLogs.taskId],
                        it[AuditLogs.workerId],
                        it[AuditLogs.action],
                        it[AuditLogs.timestamp].toString()
                    )
                }
        }
        call.respond(logs)
    }

    // Get Audit Logs by Task ID
    get("/audit-logs/task/{taskId}") {
        val taskId = call.parameters["taskId"]?.toIntOrNull()
        if (taskId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@get
        }

        val logs = transaction {
            AuditLogs.selectAll().where { AuditLogs.taskId eq taskId }
                .map {
                    AuditLogResponse(
                        it[AuditLogs.id],
                        it[AuditLogs.factoryId],
                        it[AuditLogs.orderId],
                        it[AuditLogs.taskId],
                        it[AuditLogs.workerId],
                        it[AuditLogs.action],
                        it[AuditLogs.timestamp].toString()
                    )
                }
        }
        call.respond(logs)
    }

    // Get Audit Logs by Worker ID
    get("/audit-logs/worker/{workerId}") {
        val workerId = call.parameters["workerId"]?.toIntOrNull()
        if (workerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid worker ID")
            return@get
        }

        val logs = transaction {
            AuditLogs.selectAll().where { AuditLogs.workerId eq workerId }
                .map {
                    AuditLogResponse(
                        it[AuditLogs.id],
                        it[AuditLogs.factoryId],
                        it[AuditLogs.orderId],
                        it[AuditLogs.taskId],
                        it[AuditLogs.workerId],
                        it[AuditLogs.action],
                        it[AuditLogs.timestamp].toString()
                    )
                }
        }
        call.respond(logs)
    }

    // Get Audit Logs by Factory ID
    get("/audit-logs/factory/{factoryId}") {
        val factoryId = call.parameters["factoryId"]?.toIntOrNull()
        if (factoryId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid factory ID")
            return@get
        }

        val logs = transaction {
            AuditLogs.selectAll().where { AuditLogs.factoryId eq factoryId }
                .map {
                    AuditLogResponse(
                        it[AuditLogs.id],
                        it[AuditLogs.factoryId],
                        it[AuditLogs.orderId],
                        it[AuditLogs.taskId],
                        it[AuditLogs.workerId],
                        it[AuditLogs.action],
                        it[AuditLogs.timestamp].toString()
                    )
                }
        }
        call.respond(logs)
    }
}
