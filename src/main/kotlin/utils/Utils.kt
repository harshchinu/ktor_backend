package utils

import database.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

import org.jetbrains.exposed.sql.insert

import java.math.BigDecimal
import java.time.LocalDateTime

fun RoutingCall.getFactoryId():Int {
    val principal = principal<JWTPrincipal>()
    val factory = principal?.payload?.getClaim("factoryId")?.asInt() ?: 0
    return factory
}

fun createTask(employeeId: Int, orderId: Int, status: String,workflowStageId:Int): Int  {
    return Tasks.insert {
        it[this.employeeId]=employeeId
        it[this.orderId]=orderId
        it[this.status]=status
        it[this.workflowStageId]=workflowStageId
    } get Tasks.id
}

fun createPayroll(factoryId: Int, taskId: Int, employeeId: Int, quantityCompleted: Int, workflowStageId: Int): Int  {
    val totalPay= getProductPrice(workflowStageId).times(quantityCompleted)
    println("TotalPayc$totalPay")
    return Payroll.insert {
        it[this.factoryId]=factoryId
        it[this.taskId]=taskId
        it[this.workerId]=employeeId
        it[this.quantityCompleted]=quantityCompleted
        it[this.totalPay]=BigDecimal(totalPay)
        it[this.dateReceived]=LocalDateTime.now()
    } get Payroll.id
}

fun getProductPrice(workflowStageId: Int): Double {
    return WorkflowStages
            .select(WorkflowStages.payRatePerUnit)
            .where { WorkflowStages.id eq workflowStageId }
            .map { it[WorkflowStages.payRatePerUnit].toDouble() }
            .single()

}