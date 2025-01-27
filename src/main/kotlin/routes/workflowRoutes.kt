package routes

import database.WorkflowTasks
import database.Workflows
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun Route.workflowRoutes() {

    // Workflow Routes

    // Create Workflow
    post("/workflows") {
        val workflowData = call.receive<WorkflowRequest>()
        val workflowId = transaction {
            Workflows.insert {
                it[factoryId] = workflowData.factoryId
                it[workflowName] = workflowData.workflowName
                it[description] = workflowData.description
            } get Workflows.id
        }
        call.respond(HttpStatusCode.Created, mapOf("workflow_id" to workflowId))
    }

    // Get Workflow by ID
    get("/workflows/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
            return@get
        }

        val workflow = transaction {
            Workflows.selectAll().where { Workflows.id eq id }
                .map { WorkflowResponse(it[Workflows.id], it[Workflows.factoryId], it[Workflows.workflowName], it[Workflows.description]) }
                .singleOrNull()
        }

        if (workflow == null) {
            call.respond(HttpStatusCode.NotFound, "Workflow not found")
        } else {
            call.respond(workflow)
        }
    }

    authenticate {
        // Get All Workflows
        get("/workflows") {
            val workflows = transaction {
                Workflows.selectAll()
                    .map {
                        WorkflowResponse(
                            it[Workflows.id],
                            it[Workflows.factoryId],
                            it[Workflows.workflowName],
                            it[Workflows.description]
                        )
                    }
            }
            call.respond(workflows)
        }

    }
    // Update Workflow
    put("/workflows/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
            return@put
        }

        val workflowData = call.receive<WorkflowRequest>()
        val rowsUpdated = transaction {
            Workflows.update({ Workflows.id eq id }) {
                it[factoryId] = workflowData.factoryId
                it[workflowName] = workflowData.workflowName
                it[description] = workflowData.description
            }
        }

        if (rowsUpdated == 0) {
            call.respond(HttpStatusCode.NotFound, "Workflow not found")
        } else {
            call.respond(HttpStatusCode.OK, "Workflow updated successfully")
        }
    }

    // Delete Workflow
    delete("/workflows/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
            return@delete
        }

        val rowsDeleted = transaction {
            Workflows.deleteWhere { Workflows.id eq  id }
        }

        if (rowsDeleted == 0) {
            call.respond(HttpStatusCode.NotFound, "Workflow not found")
        } else {
            call.respond(HttpStatusCode.OK, "Workflow deleted successfully")
        }
    }

    // Task Routes

    // Create Task
    post("/workflows/{workflowId}/tasks") {
        val workflowId = call.parameters["workflowId"]?.toIntOrNull()
        if (workflowId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
            return@post
        }

        val taskData = call.receive<WorkflowTaskRequest>()
        val taskId = transaction {
            WorkflowTasks.insert {
                it[WorkflowTasks.workflowId] = workflowId
                it[taskName] = taskData.taskName
                it[payRatePerUnit] = BigDecimal.valueOf(taskData.payRatePerUnit)
            } get(WorkflowTasks.id)
        }
        call.respond(HttpStatusCode.Created, mapOf("task_id" to taskId))
    }

    // Get All Tasks for a Workflow
    get("/workflows/{workflowId}/tasks") {
        val workflowId = call.parameters["workflowId"]?.toIntOrNull()
        if (workflowId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid workflow ID")
            return@get
        }

        val tasks = transaction {
            WorkflowTasks.selectAll().where { WorkflowTasks.workflowId eq workflowId }
                .map { WorkflowTaskResponse(it[WorkflowTasks.id], it[WorkflowTasks.workflowId], it[WorkflowTasks.taskName], it[WorkflowTasks.payRatePerUnit].toDouble()) }
        }
        call.respond(tasks)
    }
}
