package models

import kotlinx.serialization.Serializable

@Serializable
data class RoleRequest(
    val role: String
)