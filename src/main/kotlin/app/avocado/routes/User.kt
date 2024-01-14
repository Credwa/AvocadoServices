package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.models.*
import app.avocado.utils.BadRequestException
import app.avocado.utils.baseUrl
import app.avocado.utils.setUserSession
import io.github.jan.supabase.postgrest.from
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val defaultAvatarUrl =
    "https://avgjpetxuposojfgvele.supabase.co/storage/v1/object/public/ProfilePhotos/defaultProfilePhoto.png"

fun Route.userRouting() {
    route("$baseUrl/user/me") {
        get() {
            try {
                call.setUserSession()
                val user = supabase.from("users").select().decodeSingle<User>()
                call.respond(user)
            } catch (e: BadRequestException) {
                call.respondText(e.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
            }
        }
    }
    route("$baseUrl/user/verify") {
        post() {
            try {
                call.setUserSession()
                val userRequestBody = call.receive<UserId>()
                val user = supabase.from("users").select().decodeList<User>()
                println(user.isEmpty())
                if (user.isEmpty()) {
                    // check if user is an artist
                    val artist = supabase.from("artists").select().decodeList<Artist>()
                    if (artist.isEmpty()) {
                        supabase.from("users").insert(
                            User(
                                userRequestBody.uid,
                                defaultAvatarUrl,
                                UserRole.USER
                            )
                        )
                    } else {
                        supabase.from("users").insert(
                            User(
                                userRequestBody.uid,
                                artist[0].avatarUrl,
                                UserRole.ARTIST
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