package models

import kotlinx.serialization.Serializable

@Serializable
data class MasterTaskRequest(
    val productId: Int,
    val workflowId: Int,
    val workflowStageId: Int,
    val assignedMasterId: Int,
    val quantityAssigned: Double
)

@Serializable
data class MasterTaskResponse(
    val id: Int,
    val factoryId: Int,
    val productId: Int,
    val assignedMasterId: Int,
    val quantityAssigned: Double,
    val cutDate: String,
    val status: String,
    val workflowStageId: Int
)
@Serializable
data class MasterTaskPutResponse (
    val cutId: Int,
    val orderId: Int,
)
@Serializable
data class MasterTaskPuRequest (
    val productId: Int,
    val workflowId: Int,
    val cutVariants: List<CutVariants>

)

@Serializable
data class CutVariants (
    val productVariantId: Int,
    val quantity: Int
)