package app.avocado.utils

import app.avocado.SupabaseConfig.supabase
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable


class BadRequestException(message: String) : RuntimeException(message)

const val baseUrl = "api/v1"

@Serializable
data class PostSuccessResponse(val message: String = "", val status: Int = 201)

suspend fun ApplicationCall.setUserSession() {
    val bearerToken = this.request.headers["Authorization"]
        ?: throw BadRequestException("Bad Request: Authorization header missing")

    if (!bearerToken.startsWith("Bearer "))
        throw BadRequestException("Bad Request: Invalid Authorization header format")

    val accessToken = bearerToken.substringAfter("Bearer ")
    val refreshToken = this.request.headers["grant_type"]
        ?: throw BadRequestException("Bad Request: grant_type header missing")

    val expiresIn =
        this.request.headers["expires_in"] ?: throw BadRequestException("Bad Request: expires_in header missing")
    val expiresAt =
        this.request.headers["expires_at"] ?: throw BadRequestException("Bad Request: expires_at header missing")

    if (Clock.System.now().toEpochMilliseconds() / 1000 > expiresAt.toLong()) {
        throw BadRequestException("Session expired")
    } else {
        supabase.auth.importSession(
            UserSession(
                accessToken,
                refreshToken,
                expiresIn = expiresIn.toLong(),
                tokenType = "Bearer",
                user = null
            )
        )
        // don't refresh session on backend. We handle refreshes on frontend.
        supabase.auth.stopAutoRefreshForCurrentSession()
    }
}