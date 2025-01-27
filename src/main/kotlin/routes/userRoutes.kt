package routes

import database.Employees.role
import database.Users
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.UserResponse
import models.UserSignInRequest
import models.UserSignUpRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest

fun Route.userRoutes() {

    // Sign-Up
    post("/auth/signup") {
        val signUpRequest = call.receive<UserSignUpRequest>()
        val userId = transaction {
            Users.insert {
                it[factoryId] = signUpRequest.factoryId
                it[name] = signUpRequest.name
                it[email] = signUpRequest.email
                it[passwordHash] = hashPassword(signUpRequest.password)
                it[role] = signUpRequest.role
            } get Users.id
        }
        call.respond(HttpStatusCode.Created, mapOf("user_id" to userId))
    }

    // Sign-In
    post("/auth/signin") {


            val signInRequest = call.receive<UserSignInRequest>()

            // Fetch user details, including userId and factoryId
            val user = transaction {
                Users.selectAll().where { Users.email eq signInRequest.email }
                    .map {
                        Triple(it[Users.passwordHash], it[Users.id], it[Users.factoryId]) // Fetch passwordHash, userId, and factoryId
                    }
                    .singleOrNull()
            }

            // Validate user credentials
            if (user == null || user.first != hashPassword(signInRequest.password)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            } else {
                // Generate JWT with userId and factoryId
                val token = JwtConfig.generateToken(
                    userId = user.second,       // User ID
                    factoryId = user.third      // Factory ID
                )
                call.respond(HttpStatusCode.OK, mapOf("token" to token))
            }

    }

    // Protected Routes for Admin
    authenticate {
        // Get User by ID
        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val user = transaction {
                Users.selectAll().where { Users.id eq id }
                    .map { UserResponse(it[Users.id], it[Users.factoryId], it[Users.name], it[Users.email], it[Users.role]) }
                    .singleOrNull()
            }

            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        // Get All Users
        get("/users") {
            val users = transaction {
                Users.selectAll()
                    .map { UserResponse(it[Users.id], it[Users.factoryId], it[Users.name], it[Users.email], it[Users.role]) }
            }
            call.respond(users)
        }


    }


}

fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}
