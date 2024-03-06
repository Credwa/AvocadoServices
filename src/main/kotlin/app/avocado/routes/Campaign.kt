package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.*
import app.avocado.utils.*
import com.stripe.model.Customer
import com.stripe.param.CustomerCreateParams
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


fun Route.campaignRouting() {
    route("${baseUrl}/search") {
        get {
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
            val latestCampaigns = supabase.postgrest.rpc("get_latest_campaigns")
                .decodeList<CampaignInfo>()
            call.respond(latestCampaigns)
        }
    }
    route("$baseUrl/campaigns/featured") {
        get {
            val featuredCampaigns = supabase.postgrest.rpc("get_featured_campaigns")
                .decodeList<CampaignInfo>()
            call.respond(featuredCampaigns)
        }
    }
    route("$baseUrl/campaigns/upcoming") {
        get {
            val featuredCampaigns = supabase.postgrest.rpc("get_upcoming_campaigns")
                .decodeList<CampaignInfo>()
            call.respond(featuredCampaigns)
        }
    }
    route("$baseUrl/campaigns/purchase") {
        post("payment-sheet") {
            try {
                val postData = call.receive<PaymentIntentPost>()
                // get stripe customer info
                val customerInfo = supabaseAdmin.from("customers").select() {
                    filter {
                        SupabaseCustomer::id eq postData.uid
                    }
                }.decodeSingleOrNull<SupabaseCustomer>()
                // get campaign details to purchase
                val campaignDetails = supabaseAdmin.from("campaign_details").select() {
                    filter {
                        eq("song_id", postData.songId)
                    }
                }.decodeSingleOrNull<CampaignDetails>()
                // validate campaign details before purchase
                if (campaignDetails === null) {
                    call.respond(HttpStatusCode.BadRequest, "Campaign not found")
                    return@post
                } else {
                    val campaignTimestampInstant = Instant.parse(campaignDetails.campaignStartDate)
                    val currentInstant = Instant.now()

                    if (campaignDetails.availableShares < postData.quantity) {
                        println("Separate purchased occurred at similar time and available shares was decreased causing it to be less than intent quantity")
                        call.respond(HttpStatusCode.BadRequest, "Something went wrong try again later 44821")
                        return@post
                    }

                    if (campaignTimestampInstant.isBefore(currentInstant)) {
                        val startDatePlusTimeRestraint = addDaysToTimestampWithZone(
                            campaignDetails.campaignStartDate,
                            campaignDetails.timeRestraint.toLong(),
                            "UTC"
                        )
                        if (startDatePlusTimeRestraint.isBefore(currentInstant)) {
                            call.respond(HttpStatusCode.BadRequest, "Campaign has finished")
                            return@post
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Campaign not yet started")
                        return@post
                    }
                }
                // total price calculation
                val pricePerShareAsLong: Long = (campaignDetails.pricePerShare * 100).toLong()
                val totalPrice: Long = pricePerShareAsLong * postData.quantity
                if (customerInfo === null) {
                    // create customer for purchase if not exists
                    val customerParams = CustomerCreateParams.builder().setEmail(postData.email).build()
                    val customer = Customer.create(customerParams)
                    supabaseAdmin.from("customers").insert(SupabaseCustomer(postData.uid, customer.id))
                    call.respond(createPaymentIntent(customer.id, totalPrice, postData))
                } else {
                    call.respond(createPaymentIntent(customerInfo.customerId, totalPrice, postData))
                }
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong. Could not complete purchase")
            }
        }

        post("payment-sheet/released") {
            try {
                val postData = call.receive<PaymentIntentPostReleased>()
                // get stripe customer info
                val customerInfo = supabaseAdmin.from("customers").select() {
                    filter {
                        SupabaseCustomer::id eq postData.uid
                    }
                }.decodeSingleOrNull<SupabaseCustomer>()
                // get campaign details to purchase
                val campaignDetails = supabaseAdmin.from("campaign_details").select() {
                    filter {
                        eq("song_id", postData.songId)
                    }
                }.decodeSingleOrNull<CampaignDetails>()
                // validate campaign details before purchase
                if (campaignDetails === null) {
                    call.respond(HttpStatusCode.BadRequest, "Campaign not found")
                    return@post
                } else {
                    val campaignTimestampInstant = Instant.parse(campaignDetails.campaignStartDate)
                    val currentInstant = Instant.now()

                    if (campaignTimestampInstant.isBefore(currentInstant)) {
                        val startDatePlusTimeRestraint = addDaysToTimestampWithZone(
                            campaignDetails.campaignStartDate,
                            campaignDetails.timeRestraint.toLong(),
                            "UTC"
                        )
                        if (!startDatePlusTimeRestraint.isBefore(currentInstant)) {
                            println("Tried to purchase-released to a campaign that's still ongoing")
                            call.respond(HttpStatusCode.BadRequest, "Error purchasing")
                            return@post
                        }
                    } else {
                        call.respond(HttpStatusCode.BadRequest, "Campaign not yet started")
                        return@post
                    }
                }

                if (customerInfo === null) {
                    // create customer for purchase if not exists
                    val customerParams = CustomerCreateParams.builder().setEmail(postData.email).build()
                    val customer = Customer.create(customerParams)
                    supabaseAdmin.from("customers").insert(SupabaseCustomer(postData.uid, customer.id))
                    call.respond(createPaymentIntentForDonation(customer.id, postData.amount, postData))
                } else {
                    call.respond(createPaymentIntentForDonation(customerInfo.customerId, postData.amount, postData))
                }
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong. Could not complete purchase")
            }
        }
        post("{id?}") {
            val songId = call.parameters["id"] ?: return@post call.respondText(
                "Missing song id",
                status = HttpStatusCode.BadRequest
            )
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
                shares,
                donation_amount
            """.trimIndent().lines().joinToString("")
            )

            val purchaseInfo = supabase.from("song_purchases").select(columns = columns) {
                filter {
                    PurchaseInfo::songId eq songId
                    and {
                        PurchaseInfo::userId eq userId
                    }
                }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<PurchaseInfo>()

            println(purchaseInfo)
            call.respond(purchaseInfo)
        }
    }
    route("$baseUrl/campaigns") {
        get("discover") {
            val limit = call.parameters["limit"]?.toLongOrNull() ?: 8 // Default limit
            val offset = call.parameters["offset"]?.toIntOrNull() ?: 0 // Default offset

            val rangeFrom = (offset * limit)
            val rangeTo = (offset * limit) + limit - 1

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
              is_verified,
              role
            )
            """.trimIndent().lines().joinToString("")
            )
            val campaigns = supabaseAdmin.from("songs").select(columns = columns) {
                filter {
                    Campaign::status neq "draft"
                }
                order(column = "last_updated_at", order = Order.DESCENDING)
                range(rangeFrom, rangeTo)
            }.decodeList<Campaign>()

            val filteredCampaigns = campaigns.filter { campaign ->
                campaign.campaignDetails?.campaignStartDate != null
            }

            call.respond(filteredCampaigns)
        }
        get("{id?}") {
            val songId = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            println("my id $songId")
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
        post("play/{id?}") {
            val songId = call.parameters["id"] ?: return@post call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )

            @Serializable
            data class IncrementPlays(
                @SerialName("song_id") val songId: String
            )

            supabaseAdmin.postgrest.rpc("increment_song_plays", IncrementPlays(songId))
            call.respond(PostSuccessResponse("$songId play incremented"))
        }
    }
}