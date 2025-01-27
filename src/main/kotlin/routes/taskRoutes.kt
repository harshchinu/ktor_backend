package routes

import database.Tasks
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.TaskPatchRequest
import models.TaskRequest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Route.taskRoutes() {
    // Create Task
    post("/tasks") {
        val taskRequest = call.receive<TaskRequest>()
        val taskId = transaction {
            Tasks.insert {
                it[employeeId] = taskRequest.employeeId
                it[status] = taskRequest.status.toString()
                it[orderId] = taskRequest.orderId
            }[Tasks.id]
        }
        call.respond(HttpStatusCode.Created, mapOf("task_id" to taskId))
    }
    patch("/tasks/{taskId}") {
        val taskId = call.parameters["taskId"]?.toIntOrNull()
        if (taskId == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("errorMessage" to "taskId can not be null"))
            return@patch
        }
        val taskRequest = call.receive<TaskPatchRequest>()
        transaction {
            Tasks.update ({ Tasks.id eq  taskId}){
                it[status]=taskRequest.status.toString()
            }
        }
        call.respond(HttpStatusCode.Created, mapOf("task_id" to taskId))
    }

    /*// Update Task Status
    put("/tasks/{taskId}/status") {
        val taskId = call.parameters["taskId"]?.toIntOrNull()
        if (taskId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            return@put
        }
        val statusUpdate = call.receive<Map<String, String>>()
        val newStatus = statusUpdate["status"] ?: run {
            call.respond(HttpStatusCode.BadRequest, "Missing status")
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
