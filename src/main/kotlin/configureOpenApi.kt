import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*

fun Application.configureOpenApi() {
    routing {
        openAPI(path = "openapi") // This generates OpenAPI documentation at `/openapi`
    }
}