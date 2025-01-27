package routes


import database.*
import database.Payroll.autoIncrement
import database.Payroll.references
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.OrderResponse
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.orderRoutes() {
    // Get Orders
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
}
