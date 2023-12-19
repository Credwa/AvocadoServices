package app.avocado

import app.avocado.plugins.configureHTTP
import app.avocado.plugins.configureRouting
import app.avocado.plugins.configureSerialization
import com.stripe.Stripe
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    Stripe.apiKey =
        dotenv().get("STRIPE_API_KEY")
    embeddedServer(Netty, port = 8080, host = "192.168.1.23", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    configureSerialization()
    configureHTTP()
    configureRouting()
}
