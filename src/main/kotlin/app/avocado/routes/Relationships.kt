package app.avocado.routes

import app.avocado.SupabaseConfig.supabase
import app.avocado.models.FollowArtistPost
import app.avocado.models.FollowStatus
import app.avocado.utils.BadRequestException
import app.avocado.utils.PostSuccessResponse
import app.avocado.utils.baseUrl
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.relationshipsRouting() {
    route("$baseUrl/relationships") {
        get("following/total") {
            try {
                val count = supabase.from("artist_followers").select(head = true) {
                    count(Count.PLANNED)
                }.countOrNull()!!
                call.respond(HttpStatusCode.OK, count)
            } catch (e: BadRequestException) {
                call.respond(HttpStatusCode.BadRequest, "Something went wrong. Try again later")
            }
        }
        get("follow-status/{userId?}/{artistId?}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val artistId = call.parameters["artistId"] ?: return@get call.respondText(
                "Missing artist id",
                status = HttpStatusCode.BadRequest
            )
            println(artistId)

            val data = supabase.from("artist_followers").select {
                filter {
                    eq("user_id", userId)
                    eq("artist_id", artistId)
                }
            }.decodeSingleOrNull<FollowArtistPost>()

            if (data !== null) {
                call.respond(FollowStatus(true))
            } else {
                call.respond(FollowStatus(false))
            }
        }
        post("follow-artist") {
            try {
                val postData = call.receive<FollowArtistPost>()
                supabase.from("artist_followers").insert(postData)
                call.respond(PostSuccessResponse("Follow Successful"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Something went wrong. Try again later")
            }
        }
        post("unfollow-artist") {
            try {
                val postData = call.receive<FollowArtistPost>()
                supabase.from("artist_followers").delete() {
                    filter {
                        eq("user_id", postData.userId)
                        eq("artist_id", postData.artistId)
                    }
                }
                call.respond(PostSuccessResponse("Unfollow Successful"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Something went wrong. Try again later")
            }
        }
    }
}