package models

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeResponse(
    val employeeId: Int,
    val name: String,
    val role: String,
    val contactInfo: String,
    val factoryId: Int
)

@Serializable
data class EmployeeRequest(
    val name: String,
    val role: String,
    val contactInfo: String,
    val factoryId: Int
)