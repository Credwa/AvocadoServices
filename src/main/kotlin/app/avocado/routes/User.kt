package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.*
import app.avocado.services.Stripe
import app.avocado.utils.BadRequestException
import app.avocado.utils.PostSuccessResponse
import app.avocado.utils.baseUrl
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

const val defaultAvatarUrl =
    "https://avgjpetxuposojfgvele.supabase.co/storage/v1/object/public/ProfilePhotos/defaultProfilePhoto.png"

fun Route.userRouting() {
    route("$baseUrl/user/me") {
        get() {
            try {
                val user = supabase.from("users").select().decodeSingle<User>()
                println(user)
                call.respond(user)
            } catch (e: BadRequestException) {
                call.respondText(e.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
            }
        }
        post("avatar") {
            try {
                val data = call.receive<AvatarUpload>()
                println("avatar upload received")
                val myUuid = UUID.randomUUID()
                val myUuidAsString = myUuid.toString()
                val imageData = Base64.getDecoder().decode(data.imageBase64)
                val bucket = supabaseAdmin.storage.from("profilephotos")

                // clear bucket first
                if (data.currentAvatar !== null) {
                    bucket.delete("${data.id}/${data.currentAvatar}")
                }

                println("uploading... id - ${data.id}")
                bucket.upload("${data.id}/${myUuidAsString}.jpeg", imageData, upsert = true)
                supabaseAdmin.from("users").update(
                    {
                        User::avatarUrl setTo "${System.getenv("SUPABASE_URL")}/storage/v1/object/public/profilephotos/${data.id}/${myUuidAsString}.jpeg"
                    }
                ) {
                    filter {
                        eq("id", data.id)
                    }
                }
                call.respond(PostSuccessResponse("Upload successful"))

            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
        post("onboarding-complete/{id?}") {
            try {
                val userId = call.parameters["id"] ?: return@post call.respondText(
                    "Missing user id",
                    status = HttpStatusCode.BadRequest
                )
                println(userId)
                supabase.from("users").update(
                    {
                        User::isOnboarded setTo true
                    }
                ) {
                    filter {
                        User::id eq userId
                    }
                }
                call.respond(PostSuccessResponse("Onboarding complete"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
        post("notifications/{id?}") {
            try {
                val userId = call.parameters["id"] ?: return@post call.respondText(
                    "Missing user id",
                    status = HttpStatusCode.BadRequest
                )
                println(userId)
                val data = call.receive<NotificationPreferences>()
                supabase.from("users").update(
                    {
                        User::notificationPreferences setTo data
                    }
                ) {
                    filter {
                        User::id eq userId
                    }
                }
                call.respond(PostSuccessResponse("Notifications updated successfully"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
        post("notifications/{id?}/token") {
            try {
                val userId = call.parameters["id"] ?: return@post call.respondText(
                    "Missing user id",
                    status = HttpStatusCode.BadRequest
                )
                println(userId)
                val data = call.receive<NotificationTokenPost>()
                supabase.from("users").update(
                    {
                        User::expoPushToken setTo data.token
                    }
                ) {
                    filter {
                        User::id eq userId
                    }
                }
                call.respond(PostSuccessResponse("Expo push token updated successfully"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
    }
    route("$baseUrl/user/stripe") {
        get("account-details/{id?}") {
            try {
                val userId = call.parameters["id"] ?: return@get call.respondText(
                    "Missing user id",
                    status = HttpStatusCode.BadRequest
                )
                val stripeAccount = supabaseAdmin.from("stripe_accounts").select() {
                    filter {
                        StripeAccount::id eq userId
                    }
                }.decodeSingleOrNull<StripeAccount>()
                val stripe = stripeAccount?.let { it1 -> Stripe(it1.stripeAccountId) }
                if (stripe != null) {
                    stripe.setAccountId(stripeAccount.stripeAccountId)
                    val stripeAccInfo = stripe.getAccountInfo()
                    call.respond(AccountInfo(stripeAccInfo.chargesEnabled, stripeAccInfo.payoutsEnabled))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Stripe account not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
        get("balance/{id?}") {
            val userId = call.parameters["id"] ?: return@get call.respondText(
                "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val stripeAccount = supabaseAdmin.from("stripe_accounts").select() {
                filter {
                    StripeAccount::id eq userId
                }
            }.decodeSingleOrNull<StripeAccount>()
            val stripe = stripeAccount?.let { it1 -> Stripe(it1.stripeAccountId) }
            if (stripe != null) {
                stripe.setAccountId(stripeAccount.stripeAccountId)
                val stripeAccBalanceJson = stripe.getAccountBalance().toJson()
                val stripeAccBalance = deserializeAccountBalance(stripeAccBalanceJson)
                call.respond(stripeAccBalance)
            } else {
                call.respond(HttpStatusCode.NotFound, "Stripe account not found")
            }
        }
        post("/account-link") {
            try {
                val stripeRequestBody = call.receive<StripeRequestBody>()
                val stripeAccount = supabaseAdmin.from("stripe_accounts").select() {
                    filter {
                        StripeAccount::id eq stripeRequestBody.uid
                    }
                }.decodeList<StripeAccount>()
                val stripe = Stripe(stripeRequestBody.uid)
                if (stripeAccount.isEmpty()) {
                    stripe.createStripeAccount()
                    call.respond(
                        AppAccountLink(
                            stripe.createAccountLink(
                                stripeRequestBody.returnUrl,
                                stripeRequestBody.refreshUrl
                            )
                        )
                    )
                } else {
                    stripe.setAccountId(stripeAccount[0].stripeAccountId)
                    val accountLink = stripe.createAccountLink(
                        stripeRequestBody.returnUrl,
                        stripeRequestBody.refreshUrl
                    )
                    val appAccountLink = AppAccountLink(
                        accountLink
                    )
                    call.respond(
                        appAccountLink
                    )
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.RequestTimeout, "Something went wrong")
            }
        }
    }
    route("$baseUrl/user/verify") {
        post() {
            try {
                val userRequestBody = call.receive<UserId>()
                println(userRequestBody)
                val user = supabaseAdmin.from("users").select() {
                    filter {
                        User::id eq userRequestBody.uid
                    }
                }.decodeList<User>()
                println(user)
                if (user.isEmpty()) {
                    // check if user is an artist
                    val artist = supabase.from("artists").select() {
                        filter {
                            Artist::id eq userRequestBody.uid
                        }
                    }.decodeList<Artist>()
                    if (artist.isEmpty()) {
                        supabase.from("users").insert(
                            User(
                                userRequestBody.uid,
                                defaultAvatarUrl,
                                UserRole.USER,
                                null,
                                isOnboarded = false,
                                stripeOnboarded = false,
                                expoPushToken = null
                            )
                        )
                    } else {
                        supabase.from("users").insert(
                            User(
                                userRequestBody.uid,
                                artist[0].avatarUrl,
                                UserRole.ARTIST,
                                null,
                                isOnboarded = false,
                                stripeOnboarded = false,
                                expoPushToken = null
                            )
                        )
                    }
                    call.respond(UserExistsResponse(false))
                } else {
                    call.respond(UserExistsResponse(true))
                }
            } catch (e: BadRequestException) {
                call.respondText(e.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
            }
        }
    }
}