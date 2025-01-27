package models

import constants.WorkStatus
import kotlinx.serialization.Serializable
import java.util.Optional

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

@Serializable
data class OrderCompleteRequest(
    val taskDetails: List<OrderTaskDetails>
)

@Serializable
data class OrderTaskDetails (
    val taskId: Int? = null,
    val employeeId: Int,
    val workflowStageId: Int,
    val quantityCompleted: Int
)
