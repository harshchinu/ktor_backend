package routes

import database.Factories
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.FactoryRequest
import models.FactoryResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.factoryRoutes() {
    // Create a factory
    post("/factories") {
        val factoryData = call.receive<FactoryRequest>()
        val factoryId = transaction {
            Factories.insert {
                it[name] = factoryData.name
                it[address] = factoryData.address
                it[contactInfo] = factoryData.contactInfo
            }[Factories.id]
        }
        call.respond(HttpStatusCode.Created, factoryId)
    }

    // Get a factory by ID
    get("/factories/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid factory ID")
            return@get
        }

        val factory = transaction {
            Factories.selectAll().where { Factories.id eq id }
                .map { row ->
                    FactoryResponse(
                        id = row[Factories.id],
                        name = row[Factories.name],
                        address = row[Factories.address],
                        contactInfo = row[Factories.contactInfo]
                    )
                }
                .singleOrNull()
        }

        if (factory == null) {
            call.respond(HttpStatusCode.NotFound, "Factory not found")
        } else {
            call.respond(factory)
        }
    }

    // Update a factory
    put("/factories/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid factory ID")
            return@put
        }

        val factoryData = call.receive<FactoryRequest>()
        val rowsUpdated = transaction {
            Factories.update({ Factories.id eq id }) {
                it[name] = factoryData.name
                it[address] = factoryData.address
                it[contactInfo] = factoryData.contactInfo
            }
        }

        if (rowsUpdated == 0) {
            call.respond(HttpStatusCode.NotFound, "Factory not found")
        } else {
            call.respond(HttpStatusCode.OK, "Factory updated successfully")
        }
    }

    // Delete a factory
    delete("/factories/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid factory ID")
            return@delete
        }

        val rowsDeleted = transaction {
            Factories.deleteWhere { Factories.id eq id }
        }

        if (rowsDeleted == 0) {
            call.respond(HttpStatusCode.NotFound, "Factory not found")
        } else {
            call.respond(HttpStatusCode.OK, "Factory deleted successfully")
        }
    }
}
