package models

import kotlinx.serialization.Serializable


@Serializable
data class FactoryRequest(
    val name: String,
    val address: String,
    val contactInfo: String
)

@Serializable
data class FactoryResponse(
    val id: Int,
    val name: String,
    val address: String,
    val contactInfo: String
)
