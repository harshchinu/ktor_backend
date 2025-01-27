
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle generic exceptions
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // Log the exception for debugging
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Something went wrong. Please try again later.")
            )
        }

        // Handle specific exceptions (e.g., Not Found)
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to  "Resource not found.")
            )
        }

        // Handle bad requests
        exception<IllegalArgumentException> { call, cause ->
            var message="Invalid request"
            if(cause.message != null) message= cause.message.toString()
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to message)
            )
        }

        // Catch all other HTTP errors
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "The requested endpoint was not found.")
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Unauthorized access. Please provide valid credentials.")
            )
        }

        status(HttpStatusCode.Forbidden) { call, _ ->
            call.respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "You do not have permission to access this resource.")
            )
        }
    }
}
