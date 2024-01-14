package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.*
import app.avocado.utils.PostSuccessResponse
import app.avocado.utils.baseUrl
import app.avocado.utils.setUserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.campaignRouting() {
    route("${baseUrl}/search") {
        get {
            call.setUserSession()
            // Extracting 'limit' and 'offset' from the query string
            // Providing default values if they are not specified in the request
            val limit = call.parameters["limit"]?.toIntOrNull() ?: 10 // Default limit
            val offset = call.parameters["offset"]?.toIntOrNull() ?: 0 // Default offset
            // Extracting and validating 'query'
            val query = call.parameters["query"]
            if (query.isNullOrEmpty() || query.length <= 1) {
                call.respondText(
                    "Search query must be longer than 1 character",
                    status = HttpStatusCode.BadRequest
                )
                return@get  // Important to return here to stop further execution
            }
            val results = supabase.postgrest.rpc("app_search", SearchParams(query, 2, limit))
                .decodeList<SearchResults>()
            call.respond(results)
        }
    }
    route("$baseUrl/campaigns/recent") {
        get {
            call.setUserSession()
            val latestCampaigns = supabase.postgrest.rpc("get_latest_campaigns")
                .decodeList<CampaignInfo>()
            call.respond(latestCampaigns)
        }
    }
    route("$baseUrl/campaigns/featured") {
        get {
            call.setUserSession()
            val featuredCampaigns = supabase.postgrest.rpc("get_featured_campaigns")
                .decodeList<CampaignInfo>()
            call.respond(featuredCampaigns)
        }
    }
    route("$baseUrl/campaigns/purchase") {
        post("{id?}") {
            val songId = call.parameters["id"] ?: return@post call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            call.setUserSession()
            val purchaseDetails = call.receive<NewCampaignPurchase>()
            purchaseDetails.songid = songId
            supabase.postgrest.rpc(
                "purchase_song_shares",
                NewCampaignPurchase(songId, purchaseDetails.userid, purchaseDetails.shares)
            )
            call.respond(PostSuccessResponse("Purchase successful"))
        }
        get {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            call.setUserSession()
            val userPurchasedSongs = supabase.postgrest.rpc(
                "get_user_purchased_songs",
                UserId(userId)
            ).decodeList<UserCampaignPurchases>()
            call.respond(userPurchasedSongs)
        }
        get("{id?}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val songId = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            val columns = Columns.raw(
                """
                song_id,
                user_id,
                created_at,
                shares
            """.trimIndent().lines().joinToString("")
            )

            val purchaseInfo = supabaseAdmin.from("song_purchases").select(columns = columns) {
                filter {
                    PurchaseInfo::songId eq songId
                    and {
                        PurchaseInfo::userId eq userId
                    }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<PurchaseInfo>()

            call.respond(purchaseInfo)
        }
    }
    route("$baseUrl/campaigns") {
        get("{id?}") {
            val songId = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            call.setUserSession()
            val columns = Columns.raw(
                """
            id,
            song_title,
            song_description,
            song_lyrics,
            artwork_url,
            audio_url,
            primary_genre,
            secondary_genre,
            add_version_info,
            is_radio_edit,
            add_version_info_other,
            explicit_lyrics,
            status,
            duration,
            campaign_details (
              available_shares,
              price_per_share,
              campaign_start_date,
              time_restraint
            ),
            artists (
              id,
              artist_name,
              avatar_url,
              is_verified
            )
            """.trimIndent().lines().joinToString("")
            )
            val campaign = supabase.from("songs").select(columns = columns) {
                filter {
                    Campaign::id eq songId
                }
            }.decodeSingle<Campaign>()

            call.respond(campaign)
        }

    }
}