package routes

import database.Factories
import database.ProductStock
import database.RawMaterials
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.ProductStockResponse
import models.RawMaterialResponse
import models.StockUpdateRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.stockRoutes() {

    // Product Stock Routes

    // Get Product Stock Levels
    get("/stock/products") {
        val stockLevels = transaction {
            ProductStock.selectAll()
                .map {
                    ProductStockResponse(
                        it[ProductStock.variantId],
                        it[ProductStock.variantId].toString(), // Replace with proper variant name lookup
                        "SKU-${it[ProductStock.variantId]}", // Replace with proper SKU lookup
                        it[ProductStock.quantityInStock]
                    )
                }
        }
        call.respond(stockLevels)
    }

    // Update Product Stock Levels

    put("/stock/products/{variantId}") {
        val variantId = call.parameters["variantId"]?.toIntOrNull()
        if (variantId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid variant ID")
            return@put
        }

        val stockUpdate = call.receive<StockUpdateRequest>()
        val rowsUpdated = transaction {
            ProductStock.update({ ProductStock.variantId eq variantId }) {
                it[quantityInStock] = stockUpdate.newStock
            }
        }

        if (rowsUpdated == 0) {
            call.respond(HttpStatusCode.NotFound, "Product variant not found")
        } else {
            call.respond(HttpStatusCode.OK, "Product stock updated successfully")
        }
    }

    // Raw Material Stock Routes

    // Get Raw Material Stock Levels
    get("/stock/raw-materials") {
        val stockLevels = transaction {
            RawMaterials.selectAll()
                .map {
                    RawMaterialResponse(
                        it[RawMaterials.id],
                        it[RawMaterials.materialName],
                        it[RawMaterials.currentStock]
                    )
                }
        }
        call.respond(stockLevels)
    }

    // Update Raw Material Stock Levels
    put("/stock/raw-materials/{materialId}") {
        val materialId = call.parameters["materialId"]?.toIntOrNull()
        if (materialId == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid material ID")
            return@put
        }

        val stockUpdate = call.receive<StockUpdateRequest>()
        val rowsUpdated = transaction {
            RawMaterials.update({ RawMaterials.id eq materialId }) {
                it[currentStock] = stockUpdate.newStock
            }
        }

        if (rowsUpdated == 0) {
            call.respond(HttpStatusCode.NotFound, "Raw material not found")
        } else {
            call.respond(HttpStatusCode.OK, "Raw material stock updated successfully")
        }
    }
}
