package app.avocado.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowArtistPost(
    @SerialName("artist_id") val artistId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
data class FollowStatus(
    val isFollowing: Boolean
)

@Serializable
data class FollowingCount(
    val count: Long
)