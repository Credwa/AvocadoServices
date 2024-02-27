package app.avocado.models

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class NewCampaignPurchase(
    var songid: String?,
    val userid: String,
    val shares: Int,
)

@Serializable
data class PurchaseInfo(
    @SerialName("user_id") val userId: String,
    @SerialName("song_id") val songId: String,
    @SerialName("created_at") val createdAt: String,
    val shares: Int,
)

@Serializable
data class UserCampaignPurchases(
    @SerialName("song_id") val songId: String,
    @SerialName("song_title") val songTitle: String,
    @SerialName("artwork_url") val artworkUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    val duration: Double?,
    @SerialName("explicit_lyrics") val explicitLyrics: Boolean,
    @SerialName("artist_name") val artistName: String,
    @SerialName("add_version_info") val versionInfo: String,
    @SerialName("add_version_info_other") val addVersionInfoOther: String,
    @SerialName("is_radio_edit") val isRadioEdit: Boolean,
    @SerialName("latest_purchase") val latestPurchase: String,
    @SerialName("total_shares") val totalShares: Int
)

@Serializable(with = CampaignStatusSerializer::class)
enum class CampaignStatus {
    DRAFT, RELEASING, ONGOING, RELEASED;

    companion object {
        fun fromString(roleStr: String): CampaignStatus {
            return entries.firstOrNull { it.name.equals(roleStr, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid role: $roleStr")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = UserRole::class)
object CampaignStatusSerializer : KSerializer<CampaignStatus> {
    override fun serialize(encoder: Encoder, value: CampaignStatus) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): CampaignStatus {
        val roleStr = decoder.decodeString()
        return CampaignStatus.fromString(roleStr)
    }
}

/**
 * Lightweight campaign information. For display on search page or minimized view
 */
@Serializable
data class CampaignInfo(
    @SerialName("song_id") val songId: String,
    @SerialName("song_title") val songTitle: String,
    @SerialName("artwork_url") val artworkUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("explicit_lyrics") val explicitLyrics: Boolean,
    @SerialName("artist_name") val artistName: String,
    @SerialName("add_version_info") val versionInfo: String,
    @SerialName("add_version_info_other") val addVersionInfoOther: String,
    @SerialName("is_radio_edit") val isRadioEdit: Boolean,
    val duration: Double?
)

@Serializable
data class CampaignDetails(
    @SerialName("available_shares") val availableShares: Int,
    @SerialName("price_per_share") val pricePerShare: Double,
    @SerialName("time_restraint") val timeRestraint: Int,
    @SerialName("campaign_start_date") val campaignStartDate: String?
)

/**
 * Full campaign information for display on song page
 */
@Serializable
data class Campaign(
    val id: String,
    @SerialName("song_title") val songTitle: String,
    @SerialName("song_description") val songDescription: String,
    @SerialName("song_lyrics") val songLyrics: String,
    @SerialName("artwork_url") val artworkUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("primary_genre") val primaryGenre: String,
    @SerialName("secondary_genre") val secondaryGenre: String?,
    @SerialName("explicit_lyrics") val explicitLyrics: Boolean,
    @SerialName("add_version_info") val versionInfo: String,
    @SerialName("add_version_info_other") val addVersionInfoOther: String,
    @SerialName("is_radio_edit") val isRadioEdit: Boolean,
    val status: CampaignStatus,
    val duration: Double?,
    @SerialName("campaign_details") val campaignDetails: CampaignDetails?,
    val artists: Artist
)


@Serializable
data class SearchParams(
    @SerialName("search_query") val query: String,
    val threshold: Int,
    @SerialName("limit_val") val limit: Int,
)

@Serializable
data class SearchResults(
    val id: String,
    @SerialName("artist_name") val artistName: String,
    @SerialName("song_title") val songTitle: String,
    @SerialName("image_url") val imageURL: String,
    @SerialName("is_verified") val isVerified: Boolean,
    val type: SearchType
)

@Serializable(with = SearchTypeSerializer::class)
enum class SearchType {
    ARTIST, SONG;

    companion object {
        fun fromString(typeStr: String): SearchType {
            return entries.firstOrNull { it.name.equals(typeStr, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid search type: $typeStr")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SearchType::class)
object SearchTypeSerializer : KSerializer<SearchType> {
    override fun serialize(encoder: Encoder, value: SearchType) {
        encoder.encodeString(value.name.lowercase())
    }

    override fun deserialize(decoder: Decoder): SearchType {
        val typeStr = decoder.decodeString()
        return SearchType.fromString(typeStr)
    }
}