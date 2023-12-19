package app.avocado.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeAccount(val id: String, @SerialName("stripe_account_id") val stripeAccountId: String)
