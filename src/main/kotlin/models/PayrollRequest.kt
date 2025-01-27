package models

import kotlinx.serialization.Serializable

@Serializable
data class PayrollRequest(
    val workerId: Int,
    val taskId: Int,
    val quantityCompleted: Int,
    val totalPay: Double,
    val factoryId: Int
)

@Serializable
data class PayrollResponse(
    val id: Int,
    val workerId: Int,
    val taskId: Int,
    val quantityCompleted: Int,
    val totalPay: Double,
    val factoryId: Int
)
