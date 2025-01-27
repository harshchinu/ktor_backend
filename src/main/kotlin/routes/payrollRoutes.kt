package routes

import database.Payroll
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.PayrollRequest
import models.PayrollResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun Route.payrollRoutes() {


    // Create Payroll Record
    post("/payroll") {
        val payrollData = call.receive<PayrollRequest>()
        val payrollId = transaction {
            Payroll.insert {
                it[workerId] = payrollData.workerId
                it[taskId] = payrollData.taskId
                it[this.quantityCompleted] = payrollData.quantityCompleted
                it[totalPay] = BigDecimal.valueOf(payrollData.totalPay)
                it[factoryId] = factoryId
            } get Payroll.id
        }
        call.respond(HttpStatusCode.Created, mapOf("payroll_id" to payrollId))
    }

    // Get Payroll Record by ID
    get("/payroll/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid payroll ID")
            return@get
        }

        val payroll = transaction {
            Payroll.selectAll().where { Payroll.id eq id }
                .map {
                    PayrollResponse(
                        it[Payroll.id],
                        it[Payroll.workerId],
                        it[Payroll.taskId],
                        it[Payroll.quantityCompleted],
                        it[Payroll.totalPay].toDouble(),
                        it[Payroll.factoryId]
                    )
                }.singleOrNull()
        }

        if (payroll == null) {
            call.respond(HttpStatusCode.NotFound, "Payroll record not found")
        } else {
            call.respond(payroll)
        }
    }

    // Get All Payroll Records
    get("/payroll") {
        val payrollRecords = transaction {
            Payroll.selectAll()
                .map {
                    PayrollResponse(
                        it[Payroll.id],
                        it[Payroll.workerId],
                        it[Payroll.taskId],
                        it[Payroll.quantityCompleted],
                        it[Payroll.totalPay].toDouble(),
                        it[Payroll.factoryId]
                    )
                }
        }
        call.respond(payrollRecords)
    }

    // Get Payroll Records by Worker
    get("/payroll/worker/{workerId}") {
        val workerId = call.parameters["workerId"]?.toIntOrNull()
        if (workerId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid worker ID")
            return@get
        }

        val payrollRecords = transaction {
            Payroll.selectAll().where { Payroll.workerId eq workerId }
                .map {
                    PayrollResponse(
                        it[Payroll.id],
                        it[Payroll.workerId],
                        it[Payroll.taskId],
                        it[Payroll.quantityCompleted],
                        it[Payroll.totalPay].toDouble(),
                        it[Payroll.factoryId]
                    )
                }
        }
        call.respond(payrollRecords)
    }

    // Get Payroll Records by Factory
    get("/payroll/factory/{factoryId}") {
        val factoryId = call.parameters["factoryId"]?.toIntOrNull()
        if (factoryId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid factory ID")
            return@get
        }

        val payrollRecords = transaction {
            Payroll.selectAll().where { Payroll.factoryId eq factoryId }
                .map {
                    PayrollResponse(
                        it[Payroll.id],
                        it[Payroll.workerId],
                        it[Payroll.taskId],
                        it[Payroll.quantityCompleted],
                        it[Payroll.totalPay].toDouble(),
                        it[Payroll.factoryId]
                    )
                }
        }
        call.respond(payrollRecords)
    }
}
