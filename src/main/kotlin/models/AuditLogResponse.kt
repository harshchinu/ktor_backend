package models

import kotlinx.serialization.Serializable

@Serializable
data class AuditLogResponse(
    val id: Int,
    val factoryId: Int,
    val orderId: Int?,
    val taskId: Int?,
    val workerId: Int?,
    val action: String,
    val timestamp: String
)
