package app.avocado.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val id: String,
    @SerialName("artist_name") val artistName: String,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("is_verified") val isVerified: Boolean
)
