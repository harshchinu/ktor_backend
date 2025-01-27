package models

import kotlinx.serialization.Serializable

@Serializable
data class WorkflowRequest(
    val workflowName: String,
    val description: String? = null
)

@Serializable
data class WorkflowResponse(
    val id: Int,
    val workflowName: String,
    val description: String
)

@Serializable
data class WorkflowTaskRequest(
    val taskName: String,
    val payRatePerUnit: Double
)

@Serializable
data class WorkflowTaskResponse(
    val id: Int,
    val workflowId: Int,
    val taskName: String,
    val payRatePerUnit: Double
)
