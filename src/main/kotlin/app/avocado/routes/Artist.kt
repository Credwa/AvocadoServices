package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.models.Artist
import app.avocado.utils.baseUrl
import app.avocado.utils.setUserSession
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.artistRouting() {
    route("$baseUrl/artists/featured") {
        get {
            call.setUserSession()
            val featuredCampaigns = supabase.postgrest.rpc("get_featured_artists")
                .decodeList<Artist>()
            call.respond(featuredCampaigns)
        }
    }
}