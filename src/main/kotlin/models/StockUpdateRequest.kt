package models

import kotlinx.serialization.Serializable

@Serializable
data class StockUpdateRequest(
    val newStock: Double
)

@Serializable
data class ProductStockResponse(
    val variantId: Int,
    val variantName: String,
    val skuCode: String,
    val quantityInStock: Double
)

@Serializable
data class RawMaterialStockResponse(
    val materialName: String,
    val currentStock: Double
)
