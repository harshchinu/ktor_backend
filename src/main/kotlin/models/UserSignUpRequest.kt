package models

import kotlinx.serialization.Serializable

@Serializable
data class UserSignUpRequest(
    val factoryId: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

@Serializable
data class UserSignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: Int,
    val factoryId: Int,
    val name: String,
    val email: String,
    val role: String
)