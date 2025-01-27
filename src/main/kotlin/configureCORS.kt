import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {


    install(CORS) {
        // Allow specific origins (replace with your frontend origin)
        allowHost("localhost:3000", schemes = listOf("http", "https"))
        allowHost("example.com", schemes = listOf("https"))

        // Allow specific HTTP methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        // Allow headers (Authorization is needed for JWT)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeaders { true }

        // Allow credentials if required
        allowCredentials = true

        // Specify allowed headers for preflight requests
        allowNonSimpleContentTypes = true
    }
}