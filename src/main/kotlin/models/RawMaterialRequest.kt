package models

import kotlinx.serialization.Serializable
import java.nio.DoubleBuffer

@Serializable
data class RawMaterialRequest(
    val id: Int?= null,
    val materialName: String?=null,
    val stock: Double
)

@Serializable
data class RawMaterialResponse(
    val id: Int,
    val materialName: String,
    val currentStock: Double
)

