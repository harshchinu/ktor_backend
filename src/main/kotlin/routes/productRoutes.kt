package routes

import database.Factories
import database.ProductVariants
import database.Products
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.ProductRequest
import models.ProductResponse
import models.VariantRequest
import models.VariantResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun Route.productRoutes() {

    // Product Routes


    authenticate {
        // Create Product
        post("/products") {

            val principal = call.principal<JWTPrincipal>()
            val factory = principal?.payload?.getClaim("factoryId")?.asInt() ?: 0

            val productData = call.receive<ProductRequest>()
            val productId = transaction {
                Products.insert {
                    it[factoryId] = factory
                    it[productName] = productData.productName
                    it[description] = productData.description
                    it[category] = ""
                } get Products.id
            }
            call.respond(HttpStatusCode.Created, mapOf("product_id" to productId))
        }

        // Get Product by ID
        get("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@get
            }

            val product = transaction {
                Products.selectAll().where { Products.id eq id }
                    .map {
                        ProductResponse(
                            it[Products.id],
                            it[Products.factoryId],
                            it[Products.productName],
                            it[Products.description],
                            it[Products.category]
                        )
                    }
                    .singleOrNull()
            }

            if (product == null) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                call.respond(product)
            }
        }
        // Get All Products
        get("/products") {

            val principal = call.principal<JWTPrincipal>()
            val factoryId = principal?.payload?.getClaim("factoryId")?.asInt() ?: 0

            val products = transaction {
                Products.selectAll().where(Products.factoryId eq factoryId)
                    .map {
                        ProductResponse(
                            it[Products.id],
                            it[Products.factoryId],
                            it[Products.productName],
                            it[Products.description],
                            it[Products.category]
                        )
                    }
            }
            call.respond(products)
        }


        // Update Product
        put("/products/{id}") {

            val principal = call.principal<JWTPrincipal>()
            val factory = principal?.payload?.getClaim("factoryId")?.asInt() ?: 0


            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@put
            }

            val productData = call.receive<ProductRequest>()
            val rowsUpdated = transaction {
                Products.update({ Products.id eq id }) {
                    it[factoryId] = factory
                    it[productName] = productData.productName
                    it[description] = productData.description
                    it[category] = ""
                }
            }

            if (rowsUpdated == 0) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                call.respond(HttpStatusCode.OK, "Product updated successfully")
            }
        }


        // Delete Product
        delete("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@delete
            }

            val rowsDeleted = transaction {
                Products.deleteWhere { Products.id eq id }
            }

            if (rowsDeleted == 0) {
                call.respond(HttpStatusCode.NotFound, "Product not found")
            } else {
                call.respond(HttpStatusCode.OK, "Product deleted successfully")
            }
        }

        // Variant Routes

        // Create Variant
        post("/products/{productId}/variants") {
            val productId = call.parameters["productId"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@post
            }

            val variantData = call.receive<VariantRequest>()
            val variantId = transaction {
                ProductVariants.insert {
                    it[ProductVariants.productId] = productId
                    it[variantName] = variantData.variantName
                    it[skuCode] = variantData.skuCode
                    it[price] = BigDecimal.valueOf(variantData.price)
                } get ProductVariants.id
            }
            call.respond(HttpStatusCode.Created, mapOf("variant_id" to variantId))
        }

        // Get All Variants for a Product
        get("/products/{productId}/variants") {
            val productId = call.parameters["productId"]?.toIntOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid product ID")
                return@get
            }

            val variants = transaction {
                ProductVariants.selectAll().where { ProductVariants.productId eq productId }
                    .map {
                        VariantResponse(
                            it[ProductVariants.id],
                            it[ProductVariants.productId],
                            it[ProductVariants.variantName],
                            it[ProductVariants.skuCode],
                            it[ProductVariants.price].toDouble()
                        )
                    }
            }
            call.respond(variants)
        }
    }
}
