package app.avocado.utils

import app.avocado.SupabaseConfig.supabase
import app.avocado.models.PaymentIntentPost
import app.avocado.models.PaymentIntentPostReleased
import app.avocado.models.PaymentIntentResponse
import com.stripe.model.EphemeralKey
import com.stripe.model.PaymentIntent
import com.stripe.param.EphemeralKeyCreateParams
import com.stripe.param.PaymentIntentCreateParams
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


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

suspend fun ApplicationCall.closeUserSession() {
    supabase.auth.signOut()
}

fun addDaysToTimestampWithZone(timestamp: String?, daysToAdd: Long, zoneId: String): Instant {
    // Parse the timestamp string to a ZonedDateTime
    val initialDateTime = ZonedDateTime.ofInstant(Instant.parse(timestamp), ZoneId.of(zoneId))
    // Add and return the specified number of days
    return initialDateTime.plusDays(daysToAdd).toInstant()
}

fun createPaymentIntent(customerId: String, price: Long, paymentIntentPost: PaymentIntentPost): PaymentIntentResponse {
    try {
        val ephemeralKeyParams =
            EphemeralKeyCreateParams.builder()
                .setStripeVersion("2023-10-16")
                .setCustomer(customerId)
                .build()

        val ephemeralKey = EphemeralKey.create(ephemeralKeyParams)

        val metaDataMap: Map<String, String> = mapOf(
            "userId" to paymentIntentPost.uid,
            "songId" to paymentIntentPost.songId,
            "uid" to paymentIntentPost.uid,
            "quantity" to paymentIntentPost.quantity.toString()
        )

        val paymentIntentParams =
            PaymentIntentCreateParams.builder()
                .setAmount(price)
                .setCurrency("usd")
                .setCustomer(customerId)
                .setDescription("Purchased ${paymentIntentPost.quantity} shares of song ${paymentIntentPost.songName} by ${paymentIntentPost.artistName}")
                .setReceiptEmail(paymentIntentPost.email)
                .putAllMetadata(metaDataMap)
                .build()
        val paymentIntent = PaymentIntent.create(paymentIntentParams)

        val paymentIntentResponse = PaymentIntentResponse(
            paymentIntent.clientSecret,
            ephemeralKey.secret,
            customerId,
            System.getenv("STRIPE_PUBLISHABLE_KEY")
        )

        println("here 4 $paymentIntentResponse")
        return paymentIntentResponse
    } catch (e: Exception) {
        println(e)
        throw e
    }
}

fun createPaymentIntentForDonation(
    customerId: String,
    price: Long,
    paymentIntentPost: PaymentIntentPostReleased
): PaymentIntentResponse {
    try {
        val ephemeralKeyParams =
            EphemeralKeyCreateParams.builder()
                .setStripeVersion("2023-10-16")
                .setCustomer(customerId)
                .build()

        val ephemeralKey = EphemeralKey.create(ephemeralKeyParams)

        val metaDataMap: Map<String, String> = mapOf(
            "userId" to paymentIntentPost.uid,
            "songId" to paymentIntentPost.songId,
            "uid" to paymentIntentPost.uid,
            "quantity" to "0"
        )

        val paymentIntentParams =
            PaymentIntentCreateParams.builder()
                .setAmount(price)
                .setCurrency("usd")
                .setCustomer(customerId)
                .setDescription("Purchased song ${paymentIntentPost.songName} by ${paymentIntentPost.artistName} for $price")
                .setReceiptEmail(paymentIntentPost.email)
                .putAllMetadata(metaDataMap)
                .build()
        val paymentIntent = PaymentIntent.create(paymentIntentParams)

        val paymentIntentResponse = PaymentIntentResponse(
            paymentIntent.clientSecret,
            ephemeralKey.secret,
            customerId,
            System.getenv("STRIPE_PUBLISHABLE_KEY")
        )

        println("here 4 $paymentIntentResponse")
        return paymentIntentResponse
    } catch (e: Exception) {
        println(e)
        throw e
    }
}