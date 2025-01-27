package models

import kotlinx.serialization.Serializable

@Serializable
data class ProductRequest(
    val productName: String,
    val description: String,
    val category: String
)

@Serializable
data class ProductResponse(
    val id: Int,
    val factoryId: Int,
    val productName: String,
    val description: String,
    val category: String
)

@Serializable
data class VariantRequest(
    val variantName: String,
    val skuCode: String,
    val price: Double
)

@Serializable
data class VariantResponse(
    val id: Int,
    val productId: Int,
    val variantName: String,
    val skuCode: String,
    val price: Double
)
