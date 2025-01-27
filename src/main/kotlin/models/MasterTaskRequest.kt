package models

import kotlinx.serialization.Serializable

@Serializable
data class MasterTaskRequest(
    val productId: Int,
    val workflowId: Int,
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
    val status: String
)