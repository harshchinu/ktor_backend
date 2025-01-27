package models

import kotlinx.serialization.Serializable

@Serializable
data class ManufacturerRequest(
    val factoryId: Int,
    val name: String,
    val contactInfo: String,
    val address: String
)

@Serializable
data class ManufacturerResponse(
    val id: Int,
    val factoryId: Int,
    val name: String,
    val contactInfo: String,
    val address: String
)
