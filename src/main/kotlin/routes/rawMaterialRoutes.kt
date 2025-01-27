package routes

import database.RawMaterials
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.RawMaterialRequest
import models.RawMaterialResponse
import models.RawMaterialStockResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.getFactoryId

fun Route.rawMaterialRoutes() {

    authenticate {
        // Create Raw Material
        post("/raw-materials") {
            val factoryId = call.getFactoryId()
            val rawMaterialData = call.receive<RawMaterialRequest>()

            if (rawMaterialData.id != null) {
                val material = transaction {
                    RawMaterials.selectAll().where(RawMaterials.id eq rawMaterialData.id).single()[RawMaterials.currentStock]
                }

                val materialId = transaction {
                    RawMaterials.update({ RawMaterials.id eq rawMaterialData.id }) {
                        it[currentStock] = material + rawMaterialData.stock
                    }
                }
                call.respond(HttpStatusCode.Created, mapOf("material_id" to materialId))
            } else {
                val materialId = transaction {
                    RawMaterials.insert {
                        it[this.factoryId] = factoryId
                        it[materialName] = rawMaterialData.materialName?:""
                        it[description] = "rawMaterialData.description"
                        it[unitOfMeasurement] = "rawMaterialData.unitOfMeasurement"
                        it[currentStock] = rawMaterialData.stock
                    } get RawMaterials.id
                }
                call.respond(HttpStatusCode.Created, mapOf("material_id" to materialId))
            }
        }


        // Get Raw Material by ID
        get("/raw-materials/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid raw material ID")
                return@get
            }

            val rawMaterial = transaction {
                RawMaterials.selectAll().where { RawMaterials.id eq id }
                    .map {
                        RawMaterialResponse(
                            it[RawMaterials.id],
                            it[RawMaterials.materialName],
                            it[RawMaterials.currentStock]
                        )
                    }
                    .singleOrNull()
            }

            if (rawMaterial == null) {
                call.respond(HttpStatusCode.NotFound, "Raw material not found")
            } else {
                call.respond(rawMaterial)
            }
        }

        // Get All Raw Materials
        get("/raw-materials") {
            val rawMaterials = transaction {
                RawMaterials.selectAll()
                    .map {
                        RawMaterialResponse(
                            it[RawMaterials.id],
                            it[RawMaterials.materialName],
                            it[RawMaterials.currentStock]
                        )
                    }
            }
            call.respond(rawMaterials)
        }

        // Update Raw Material
        put("/raw-materials/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid raw material ID")
                return@put
            }
            val factoryId = call.getFactoryId()
            val rawMaterialData = call.receive<RawMaterialRequest>()
            val rowsUpdated = transaction {
                RawMaterials.update({ RawMaterials.id eq id }) {
                    it[this.factoryId] = factoryId
                    it[materialName] = rawMaterialData.materialName?:""
                    it[description] = "rawMaterialData.description"
                    it[unitOfMeasurement] = "rawMaterialData.unitOfMeasurement"
                }
            }

            if (rowsUpdated == 0) {
                call.respond(HttpStatusCode.NotFound, "Raw material not found")
            } else {
                call.respond(HttpStatusCode.OK, "Raw material updated successfully")
            }
        }

        // Delete Raw Material
        delete("/raw-materials/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid raw material ID")
                return@delete
            }

            val rowsDeleted = transaction {
                RawMaterials.deleteWhere { RawMaterials.id eq id }
            }

            if (rowsDeleted == 0) {
                call.respond(HttpStatusCode.NotFound, "Raw material not found")
            } else {
                call.respond(HttpStatusCode.OK, "Raw material deleted successfully")
            }
        }

        // Fetch Current Stock Levels
        get("/raw-materials/stock") {
            val stockLevels = transaction {
                RawMaterials.selectAll()
                    .map { RawMaterialStockResponse(it[RawMaterials.materialName], it[RawMaterials.currentStock]) }
            }
            call.respond(stockLevels)
        }
    }
}
