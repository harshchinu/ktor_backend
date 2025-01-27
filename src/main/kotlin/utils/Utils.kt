package utils

import database.Factories
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

fun RoutingCall.getFactoryId():Int {
    val principal = principal<JWTPrincipal>()
    val factory = principal?.payload?.getClaim("factoryId")?.asInt() ?: 0
    return factory
}