package app.avocado.plugins

import app.avocado.routes.artistRouting
import app.avocado.routes.campaignRouting
import app.avocado.routes.relationshipsRouting
import app.avocado.routes.userRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    routing {
        userRouting()
        campaignRouting()
        artistRouting()
        relationshipsRouting()
    }
}
