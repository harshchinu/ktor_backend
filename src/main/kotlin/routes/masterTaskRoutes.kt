package routes

import database.Employees
import database.MasterTasks
import database.Payroll
import database.Tasks
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utils.getFactoryId
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

fun Route.masterTaskRoutes() {


    authenticate {
        get("/masterTasks"){
            val factoryId = call.getFactoryId()
            val masterTasks = transaction {
                MasterTasks.selectAll().where(MasterTasks.factoryId eq factoryId).map {
                    MasterTaskResponse(
                        id = it[MasterTasks.id],
                        factoryId = it[MasterTasks.factoryId],
                        productId = it[MasterTasks.productId],
                        status = it[MasterTasks.status],
                        quantityAssigned = it[MasterTasks.quantityAssigned],
                        assignedMasterId = it[MasterTasks.assignedMasterId],
                        cutDate = it[MasterTasks.cutDate].toString()
                    )
                }
            }
            call.respond(masterTasks)
        }


        post("/masterTasks") {
            val factoryId = call.getFactoryId()
            val payrollData = call.receive<MasterTaskRequest>()
            val payrollId = transaction {
                MasterTasks.insert {
                    it[productId] = payrollData.productId
                    it[assignedMasterId] = payrollData.assignedMasterId
                    it[quantityAssigned] = payrollData.quantityAssigned
                    it[cutDate] = LocalDateTime.now()
                    it[status] = "in_progress"
                    it[this.factoryId] = factoryId
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("master_taskId" to payrollId))
        }

        get("/masterTasks"){
            val factoryId = call.getFactoryId()
            val masterTasks = transaction {
                MasterTasks.selectAll().where(MasterTasks.factoryId eq factoryId).map {
                    MasterTaskResponse(
                        id = it[MasterTasks.id],
                        factoryId = it[MasterTasks.factoryId],
                        it[MasterTasks.productId],
                        status = it[MasterTasks.status],
                        quantityAssigned = it[MasterTasks.quantityAssigned],
                        assignedMasterId = it[MasterTasks.assignedMasterId],
                        cutDate = it[MasterTasks.cutDate].toString()
                    )
                }
            }
            call.respond(masterTasks)
        }
    }


   /* // Update Task Status
    put("/masterTasks/{mastertaskId}/complete") {
        val taskId = call.parameters["mastertaskId"]?.toIntOrNull()
        if (taskId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@put
        }

        transaction {
            Tasks.update({ Tasks.id eq taskId }) {
                it[status] = newStatus
            }
        }

        // If status is Completed, create orders
        if (newStatus == "Completed") {
            val task = transaction {
                Tasks.selectAll().where { Tasks.id eq taskId }.single()
            }
            val orderId = transaction {
                Orders.insert {
                    it[workflowId] = task[Tasks.workflowId]
                    it[productId] = task[Tasks.productId]
                    it[cutVariantId] = task[Tasks.cutVariantId]
                    it[quantity] = task[Tasks.quantity]
                    it[status] = "Task Completed"
                    it[createdAt] = System.currentTimeMillis()
                }[Orders.id]
            }
            call.respond(HttpStatusCode.OK, mapOf("message" to "Task marked as completed", "order_id" to orderId))
        } else {
            call.respond(HttpStatusCode.OK, "Task status updated")
        }
    }*/
}