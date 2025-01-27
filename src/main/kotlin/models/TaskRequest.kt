package models

import constants.WorkStatus
import kotlinx.serialization.Serializable

@Serializable
data class TaskRequest(
    val employeeId: Int,
    val status: WorkStatus,
    val orderId: Int
)

@Serializable
data class TaskResponse(
    val taskId: Int,
    val employeeId: Int,
    val status: WorkStatus,
    val orderId: Int
)
@Serializable
data class TaskPatchRequest(
    val status: WorkStatus
)