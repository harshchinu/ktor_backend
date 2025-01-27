package routes


import constants.WorkStatus
import database.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.OrderCompleteRequest
import models.OrderResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utils.createPayroll
import utils.createTask
import utils.getFactoryId

fun Route.orderRoutes() {
    // Get Orders
    authenticate {
        get("/orders") {
            val orders = transaction {
                Orders.selectAll().map {
                    OrderResponse(
                        it[Orders.id],
                        it[Orders.workflowId],
                        it[Orders.productId],
                        it[Orders.cutVariantId],
                        it[Orders.quantity],
                        it[Orders.status],
                        it[Orders.createdAt].toString()
                    )
                }
            }
            call.respond(orders)
        }

        // Get Orders by Workflow
        get("/orders/workflow/{workflowId}") {
            val workflowId = call.parameters["workflowId"]?.toIntOrNull()
            if (workflowId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
                return@get
            }

            val orders = transaction {
                Orders.selectAll().where { Orders.workflowId eq workflowId }.map {
                    OrderResponse(
                        it[Orders.id],
                        it[Orders.workflowId],
                        it[Orders.productId],
                        it[Orders.cutVariantId],
                        it[Orders.quantity],
                        it[Orders.status],
                        it[Orders.createdAt].toString()
                    )
                }
            }
            call.respond(orders)
        }

        put("/orders/{orderId}/complete") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("errorMessage" to "OrderId can not be null"))
                return@put
            }
            val request = call.receive<OrderCompleteRequest>()
            val factoryId = call.getFactoryId()
            transaction {
                val odStatus= Orders
                    .selectAll()
                    .where(Orders.id eq orderId)
                    .single()[Orders.status]
                if(odStatus == WorkStatus.COMPLETE.toString()) {
                    throw IllegalArgumentException("Ordered already marked as Completed")
                }
                for (task in request.taskDetails) {
                    var taskId = task.taskId
                    if (task.taskId != null) {
                        val taskDetail = Tasks
                            .select(Tasks.id eq task.taskId)
                            .singleOrNull()
                        if (taskDetail == null) {
                            throw IllegalArgumentException("Task not found")
                        }
                        Tasks.update({ Tasks.id eq task.taskId }) {
                            it[status] = WorkStatus.COMPLETE.toString()
                        }
                    } else {
                        //create Task
                        taskId =
                            createTask(task.employeeId, orderId, WorkStatus.COMPLETE.toString(), task.workflowStageId)
                    }
                    if (taskId == null) {
                        throw IllegalArgumentException("Invalid taskId")
                    }
                    createPayroll(factoryId, taskId, task.employeeId, task.quantityCompleted, task.workflowStageId)

                    Orders.update ({Orders.id eq orderId}){
                        it[status]=WorkStatus.COMPLETE.toString()
                    }
                }


            }.runCatching {
                // Transaction completed successfully
                call.respond(HttpStatusCode.OK, mapOf("message" to "Order processed successfully"))
            }.onFailure { e ->
                when (e) {
                    is IllegalArgumentException -> call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("errorMessage" to e.message)
                    )

                    else -> call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("errorMessage" to "An unexpected error occurred")
                    )
                }
            }


        }
    }
}
