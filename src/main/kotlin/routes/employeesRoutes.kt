package routes

import database.Employees
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.EmployeeRequest
import models.EmployeeResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import utils.getFactoryId

fun Route.employeesRoutes() {

    authenticate {
        post("/employees") {
            val employeeRequest = call.receive<EmployeeRequest>()
            val employeeId = transaction {
                Employees.insert {
                    it[name] = employeeRequest.name
                    it[role] = employeeRequest.role
                    it[contactInfo] = employeeRequest.contactInfo
                    it[factoryId] = employeeRequest.factoryId
                }[Employees.id]
            }
            call.respond(HttpStatusCode.Created, mapOf("employee_id" to employeeId))
        }

        get("/employees") {
            val employees = transaction {
                Employees.selectAll().map {
                    EmployeeResponse(
                        it[Employees.id],
                        it[Employees.name],
                        it[Employees.role],
                        it[Employees.contactInfo],
                        it[Employees.factoryId]
                    )
                }
            }
            call.respond(employees)
        }

        get("/employees/{role}") {
            val role = call.parameters["role"]
            if (role == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Role ID")
                return@get
            }
            val factory = call.getFactoryId()
            val employees = transaction {
                Employees.selectAll().where((Employees.role.upperCase() eq role.uppercase()) and (Employees.factoryId eq factory)).map {
                    EmployeeResponse(
                        it[Employees.id],
                        it[Employees.name],
                        it[Employees.role],
                        it[Employees.contactInfo],
                        it[Employees.factoryId]
                    )
                }
            }
            call.respond(employees)
        }

        get("/employees/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid employee ID")
                return@get
            }

            val employee = transaction {
                Employees.selectAll().where { Employees.id eq id }
                    .map {
                        EmployeeResponse(
                            it[Employees.id],
                            it[Employees.name],
                            it[Employees.role],
                            it[Employees.contactInfo],
                            it[Employees.factoryId]
                        )
                    }.singleOrNull()
            }

            if (employee == null) {
                call.respond(HttpStatusCode.NotFound, "Employee not found")
            } else {
                call.respond(employee)
            }
        }

        put("/employees/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid employee ID")
                return@put
            }

            val employeeRequest = call.receive<EmployeeRequest>()
            val rowsUpdated = transaction {
                Employees.update({ Employees.id eq id }) {
                    it[name] = employeeRequest.name
                    it[role] = employeeRequest.role
                    it[contactInfo] = employeeRequest.contactInfo
                    it[factoryId] = employeeRequest.factoryId
                }
            }

            if (rowsUpdated == 0) {
                call.respond(HttpStatusCode.NotFound, "Employee not found")
            } else {
                call.respond(HttpStatusCode.OK, "Employee updated successfully")
            }
        }

        delete("/employees/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid employee ID")
                return@delete
            }

            val rowsDeleted = transaction {
                Employees.deleteWhere { Employees.id eq id }
            }

            if (rowsDeleted == 0) {
                call.respond(HttpStatusCode.NotFound, "Employee not found")
            } else {
                call.respond(HttpStatusCode.OK, "Employee deleted successfully")
            }
        }

    }


}
