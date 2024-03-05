package app.avocado

import app.avocado.plugins.configureHTTP
import app.avocado.plugins.configureRouting
import app.avocado.plugins.configureSerialization
import app.avocado.utils.setUserSession
import com.stripe.Stripe
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*

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
    intercept(ApplicationCallPipeline.Setup) {
        if (call.request.httpMethod == io.ktor.http.HttpMethod.Post || call.request.httpMethod == io.ktor.http.HttpMethod.Get) {
            // Code to run before processing request
            call.setUserSession()
        }
    }

    // After any request
    intercept(ApplicationCallPipeline.Fallback) {
        if (call.request.httpMethod == io.ktor.http.HttpMethod.Post || call.request.httpMethod == io.ktor.http.HttpMethod.Get) {
            // Code to run after processing request
//            call.closeUserSession()
        }
    }
    configureSerialization()
    configureHTTP()
    configureRouting()
}
