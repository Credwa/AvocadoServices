package app.avocado

import app.avocado.plugins.configureHTTP
import app.avocado.plugins.configureRouting
import app.avocado.plugins.configureSerialization
import com.stripe.Stripe
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    Stripe.apiKey =
        System.getenv("STRIPE_API_KEY")

    embeddedServer(
        Netty,
        port = (System.getenv("PORT") ?: "8080").toInt(),
        host = System.getenv("HOST") ?: "0.0.0.0",
        module = Application::module
    )
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
}
