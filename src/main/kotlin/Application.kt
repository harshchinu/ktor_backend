import database.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import routes.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    install(ContentNegotiation) {
        json()
    }

    configureCORS()
    configureSecurity()
    configureStatusPages()

    // Configure OpenAPI and Swagger UI
    /*configureOpenApi()
    configureSwaggerUI()
*/

    transaction {
        SchemaUtils.create(
            Factories,
            Users,
            Manufacturers,
            RawMaterials,
            Products,
            ProductVariants,
            InwardRecords,
            MasterTasks,
            CutVariants, Workflows,
            WorkflowTasks,
            ProductStock,
            Payroll,
            AuditLogs
        )
    }

    routing {
        factoryRoutes()
        userRoutes()
        auditLogRoutes()
        manufacturerRoutes()
        payrollRoutes()
        productRoutes()
        rawMaterialRoutes()
        stockRoutes()
        workflowRoutes()
    }
}
