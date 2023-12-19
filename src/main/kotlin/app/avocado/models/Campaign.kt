package app.avocado.models

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class SearchParams(val query: String, val threshold: Int, val limit: Int, val offset: Int)


@Serializable
data class SearchResults(
    val id: String,
    @SerialName("title_or_name") val name: String,
    @SerialName("image_url") val imageURL: String,
    val type: SearchType
)

@Serializable(with = SearchTypeSerializer::class)
enum class SearchType {
    ARTIST, SONG;

    companion object {
        fun fromString(roleStr: String): SearchType {
            return entries.firstOrNull { it.name.equals(roleStr, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid role: $roleStr")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = UserRole::class)
object SearchTypeSerializer : KSerializer<SearchType> {
    override fun serialize(encoder: Encoder, value: SearchType) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): SearchType {
        val roleStr = decoder.decodeString()
        return SearchType.fromString(roleStr)
    }
}