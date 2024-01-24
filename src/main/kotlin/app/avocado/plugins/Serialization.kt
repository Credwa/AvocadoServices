package app.avocado.plugins

import app.avocado.utils.baseUrl
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Configure Json to ignore unknown keys
            prettyPrint = true
            isLenient = true
        })
    }
    routing {
        get("$baseUrl/health") {
            call.respondText("Service running")
        }
    }
}
