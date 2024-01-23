package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.Artist
import app.avocado.models.ArtistDetails
import app.avocado.utils.baseUrl
import app.avocado.utils.setUserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.ktor.http.*
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

    route("$baseUrl/artists") {
        get("{id?}") {
            val artistId = call.parameters["id"] ?: return@get call.respondText(
                "Missing artist id",
                status = HttpStatusCode.BadRequest
            )
            val columns = Columns.raw(
                """
                id,
                artist_name,
                avatar_url,
                is_verified,
                bio,
                video_url,
                artist_links (*),
                artist_stats (*),
                artist_activities (activities),
                songs (
                    id,
                    song_title,
                    artwork_url,
                    audio_url,
                    explicit_lyrics,
                    add_version_info,
                    add_version_info_other,
                    is_radio_edit,
                    duration
                )
            """.trimIndent().lines().joinToString("")
            )

            val artistDetails = supabaseAdmin.from("artists").select(columns = columns) {
                filter {
                    ArtistDetails::id eq artistId
                }
            }.decodeSingle<ArtistDetails>()

            call.respond(artistDetails)
        }
    }
}