package routes

import database.Manufacturers

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.ManufacturerRequest
import models.ManufacturerResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.manufacturerRoutes() {

    // Create Manufacturer
    post("/manufacturers") {
        val manufacturerData = call.receive<ManufacturerRequest>()
        val manufacturerId = transaction {
            Manufacturers.insert {
                it[factoryId] = manufacturerData.factoryId
                it[name] = manufacturerData.name
                it[contactInfo] = manufacturerData.contactInfo
                it[address] = manufacturerData.address
            } get Manufacturers.id
        }
        call.respond(HttpStatusCode.Created, mapOf("manufacturer_id" to manufacturerId))
    }

    // Get Manufacturer by ID
    get("/manufacturers/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid manufacturer ID")
            return@get
        }

        val manufacturer = transaction {
            Manufacturers.selectAll().where { Manufacturers.id eq id }
                .map { ManufacturerResponse(it[Manufacturers.id], it[Manufacturers.factoryId], it[Manufacturers.name], it[Manufacturers.contactInfo], it[Manufacturers.address]) }
                .singleOrNull()
        }

        if (manufacturer == null) {
            call.respond(HttpStatusCode.NotFound, "Manufacturer not found")
        } else {
            call.respond(manufacturer)
        }
    }

    // Get All Manufacturers
    get("/manufacturers") {
        val manufacturers = transaction {
            Manufacturers.selectAll()
                .map { ManufacturerResponse(it[Manufacturers.id], it[Manufacturers.factoryId], it[Manufacturers.name], it[Manufacturers.contactInfo], it[Manufacturers.address]) }
        }
        call.respond(manufacturers)
    }

    // Update Manufacturer
    put("/manufacturers/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid manufacturer ID")
            return@put
        }

        val manufacturerData = call.receive<ManufacturerRequest>()
        val rowsUpdated = transaction {
            Manufacturers.update({ Manufacturers.id eq id }) {
                it[factoryId] = manufacturerData.factoryId
                it[name] = manufacturerData.name
                it[contactInfo] = manufacturerData.contactInfo
                it[address] = manufacturerData.address
            }
        }

        if (rowsUpdated == 0) {
            call.respond(HttpStatusCode.NotFound, "Manufacturer not found")
        } else {
            call.respond(HttpStatusCode.OK, "Manufacturer updated successfully")
        }
    }

    // Delete Manufacturer
    delete("/manufacturers/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid manufacturer ID")
            return@delete
        }

        val rowsDeleted = transaction {
            Manufacturers.deleteWhere { Manufacturers.id eq id }
        }

        if (rowsDeleted == 0) {
            call.respond(HttpStatusCode.NotFound, "Manufacturer not found")
        } else {
            call.respond(HttpStatusCode.OK, "Manufacturer deleted successfully")
        }
    }
}
