
import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureSwaggerUI() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi.json") // Swagger UI available at `/swagger`
    }
}
