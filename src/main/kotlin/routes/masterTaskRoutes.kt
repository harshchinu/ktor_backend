package routes

import constants.WorkStatus
import database.*
import database.CutVariants
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.createPayroll
import utils.createTask
import utils.getFactoryId
import java.time.LocalDateTime

fun Route.masterTaskRoutes() {


    authenticate {
        get("/masterTasks"){
            val factoryId = call.getFactoryId()
            val masterTasks = transaction {
                MasterTasks.selectAll().where(MasterTasks.factoryId eq factoryId).map {
                    MasterTaskResponse(
                        id = it[MasterTasks.id],
                        factoryId = it[MasterTasks.factoryId],
                        productId = it[MasterTasks.productId],
                        status = it[MasterTasks.status],
                        quantityAssigned = it[MasterTasks.quantityAssigned],
                        assignedMasterId = it[MasterTasks.assignedMasterId],
                        cutDate = it[MasterTasks.cutDate].toString(),
                        workflowStageId = it[MasterTasks.workflowStageId]
                    )
                }
            }
            call.respond(masterTasks)
        }


        post("/masterTasks") {
            val factoryId = call.getFactoryId()
            val payrollData = call.receive<MasterTaskRequest>()
            val payrollId = transaction {
                MasterTasks.insert {
                    it[productId] = payrollData.productId
                    it[assignedMasterId] = payrollData.assignedMasterId
                    it[quantityAssigned] = payrollData.quantityAssigned
                    it[cutDate] = LocalDateTime.now()
                    it[workflowStageId] = payrollData.workflowStageId
                    it[status] = WorkStatus.IN_PROGRESS.toString()
                    it[this.factoryId] = factoryId
                } get MasterTasks.id
            }
            call.respond(HttpStatusCode.Created, mapOf("master_taskId" to payrollId))
        }

        get("/masterTasks"){
            val factoryId = call.getFactoryId()
            val masterTasks = transaction {
                MasterTasks.selectAll().where(MasterTasks.factoryId eq factoryId).map {
                    MasterTaskResponse(
                        id = it[MasterTasks.id],
                        factoryId = it[MasterTasks.factoryId],
                        it[MasterTasks.productId],
                        status = it[MasterTasks.status],
                        quantityAssigned = it[MasterTasks.quantityAssigned],
                        assignedMasterId = it[MasterTasks.assignedMasterId],
                        cutDate = it[MasterTasks.cutDate].toString(),
                        workflowStageId = it[MasterTasks.workflowStageId]
                    )
                }
            }
            call.respond(masterTasks)
        }
        put("/masterTasks/{masterTaskId}/complete") {
            val masterTaskId = call.parameters["masterTaskId"]?.toIntOrNull()
            if (masterTaskId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("errorMessage" to "Invalid Master task ID"))
                return@put
            }
            val currentTask = transaction {
                MasterTasks
                    .selectAll().where { MasterTasks.id eq masterTaskId } // Use select with a condition
                    .map {
                        MasterTaskResponse(
                            id = it[MasterTasks.id],
                            factoryId = it[MasterTasks.factoryId],
                            productId = it[MasterTasks.productId],
                            status = it[MasterTasks.status],
                            quantityAssigned = it[MasterTasks.quantityAssigned],
                            assignedMasterId = it[MasterTasks.assignedMasterId],
                            cutDate = it[MasterTasks.cutDate].toString(),
                            workflowStageId = it[MasterTasks.workflowStageId]
                        )
                    }
                    .singleOrNull() // Expecting a single result, or null if no match
            }

            if (currentTask != null) {
                if(currentTask.status == WorkStatus.COMPLETE.toString()) {
                    call.respond(HttpStatusCode.fromValue(208), mapOf("errorMessage" to "Already marked as completed"))
                    return@put
                }
            } else {
                call.respond(HttpStatusCode.fromValue(400), mapOf("errorMessage" to "Master task not found"))
                return@put
            }
            val body=call.receive<MasterTaskPuRequest>()
            val factory = call.getFactoryId()
            val resp : MutableList<MasterTaskPutResponse> = mutableListOf()
            transaction {
                for (variant in body.cutVariants){
                    val cutVariantId=CutVariants.insert {
                        it[factoryId] = factory
                        it[this.masterTaskId] = masterTaskId
                        it[variantId] = variant.productVariantId
                        it[quantityCut] = variant.quantity
                    } get CutVariants.id
                    call.application.environment.log.info("Cut variant created with id: {}",cutVariantId)
                    val orderId=Orders.insert {
                        it[workflowId]=body.workflowId
                        it[productId]=body.productId
                        it[this.cutVariantId] = cutVariantId
                        it[quantity]=variant.quantity
                        it[status]=WorkStatus.IN_PROGRESS.toString()
                    } get Orders.id
                    MasterTasks.update({MasterTasks.id eq masterTaskId}) {
                        it[status]= WorkStatus.COMPLETE.toString()
                    }
                    val taskId=createTask(currentTask.assignedMasterId, orderId = orderId, status = WorkStatus.COMPLETE.toString(), workflowStageId = currentTask.workflowStageId )
                    println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>$taskId")
                    createPayroll(
                        factoryId = factory,
                        employeeId = currentTask.assignedMasterId,
                        quantityCompleted = variant.quantity,
                        taskId = taskId,
                        workflowStageId = currentTask.workflowStageId
                    )


                    call.application.environment.log.info("order Created with id: {}",orderId)
                    resp.add(MasterTaskPutResponse(cutVariantId,orderId))
                }
            }
            call.respond(resp)
    }

    }
}