package models

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
    val workflowId: Int,
    val productId: Int,
    val cutVariantId: Int,
    val quantity: Int
)


@Serializable
data class OrderResponse(
    val orderId: Int,
    val workflowId: Int,
    val productId: Int,
    val cutVariantId: Int,
    val quantity: Int,
    val status: String,
    val createdAt: String
)