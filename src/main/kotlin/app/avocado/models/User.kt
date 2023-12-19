package app.avocado.models

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class User(val id: String, @SerialName("avatar_url") val avatarUrl: String?, val role: UserRole)

@Serializable
data class UserId(val id: String)

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