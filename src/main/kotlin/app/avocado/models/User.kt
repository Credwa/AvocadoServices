package app.avocado.models

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class NotificationPreferences(@SerialName("notifications_enabled") val notificationsEnabled: Boolean)

@Serializable
data class User(
    val id: String,
    @SerialName("avatar_url") val avatarUrl: String?,
    val role: UserRole,
    @SerialName("notification_preferences") val notificationPreferences: NotificationPreferences?,
    @SerialName("is_onboarded") val isOnboarded: Boolean?,
    @SerialName("stripe_onboarding_complete") val stripeOnboarded: Boolean?,
    @SerialName("expo_push_token") val expoPushToken: String?,
    val paypal: String?
)

@Serializable
data class NotificationTokenPost(
    val token: String
)

@Serializable
data class PaypalInfoPost(
    val email: String
)

@Serializable
data class UserId(val uid: String)


@Serializable
data class AvatarUpload(val id: String, val imageBase64: String, val currentAvatar: String?)

@Serializable
data class UserExistsResponse(val exists: Boolean)


@Serializable(with = UserRoleSerializer::class)
enum class UserRole {
    ARTIST, USER, ADMIN;

    companion object {
        fun fromString(roleStr: String): UserRole {
            return entries.firstOrNull { it.name.equals(roleStr, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid role: $roleStr")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = UserRole::class)
object UserRoleSerializer : KSerializer<UserRole> {
    override fun serialize(encoder: Encoder, value: UserRole) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): UserRole {
        val roleStr = decoder.decodeString()
        return UserRole.fromString(roleStr)
    }
}