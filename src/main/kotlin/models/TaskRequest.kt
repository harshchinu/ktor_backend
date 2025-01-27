package models

import kotlinx.serialization.Serializable

@Serializable
data class TaskRequest(
    val employeeId: Int,
    val status: String,
    val orderId: Int
)

@Serializable
data class TaskResponse(
    val taskId: Int,
    val employeeId: Int,
    val status: String,
    val orderId: Int
)
