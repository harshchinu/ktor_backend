package routes


import database.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utils.getFactoryId

fun Route.roleRoutes() {
    // Get Orders
    authenticate {
        post("/role")  {
            val factoryId=call.getFactoryId()
            val roleRequestData=call.receive<models.RoleRequest>()
            val roles= transaction {
                Roles.insert {
                    it[role] = roleRequestData.role.trim().uppercase()
                    it[this.factoryId] = factoryId
                }
            }[Roles.role]
            call.respond(HttpStatusCode.Created, mapOf("role" to roles))
        }
        get("/roles")  {
            val factoryId=call.getFactoryId()
            val roles= transaction {
                Roles.selectAll().where(Roles.factoryId eq factoryId).map { it[Roles.role] }
            }
            call.respond(HttpStatusCode.OK, roles)
        }

    }
}
