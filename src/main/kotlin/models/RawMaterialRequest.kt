package models

import kotlinx.serialization.Serializable

@Serializable
data class RawMaterialRequest(
    val factoryId: Int,
    val materialName: String,
    val description: String,
    val unitOfMeasurement: String
)

@Serializable
data class RawMaterialResponse(
    val id: Int,
    val factoryId: Int,
    val materialName: String,
    val description: String,
    val unitOfMeasurement: String,
    val currentStock: Double
)

