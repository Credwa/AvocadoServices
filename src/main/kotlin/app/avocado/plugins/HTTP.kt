package app.avocado.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("grant_type")
        allowHeader("expires_in")
        allowHeader("expires_at")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}
