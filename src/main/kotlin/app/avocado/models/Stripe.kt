package app.avocado.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StripeAccount(val id: String, @SerialName("stripe_account_id") val stripeAccountId: String)

@Serializable
data class StripeRequestBody(
    val uid: String,
    val returnUrl: String,
    val refreshUrl: String
)

@Serializable
data class AppAccountLink(val accountLink: String)

@Serializable
data class AccountInfo(
    @SerialName("charges_enabled") val chargesEnabled: Boolean,
    @SerialName("payouts_enabled") val payoutsEnabled: Boolean
)

@Serializable
data class AccountBalance(
    val available: List<Balance>,
    val pending: List<Balance>
)

@Serializable
data class Balance(
    val amount: Int,
    val currency: String
)

@Serializable
data class PaymentIntentPost(
    val songId: String,
    val uid: String,
    val quantity: Int,
    val email: String,
    val songName: String,
    val artistName: String
)

@Serializable
data class PaymentIntentPostReleased(
    val songId: String,
    val uid: String,
    val email: String,
    val songName: String,
    val artistName: String,
    val amount: Long
)

@Serializable
data class PaymentIntentResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String
)

@Serializable
data class SupabaseCustomer(
    val id: String,
    @SerialName("stripe_customer_id") val customerId: String
)

fun deserializeAccountBalance(jsonString: String): AccountBalance {
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(jsonString)
}

