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

@Serializable
data class ActivityTrackInfoArtists(
    @SerialName("songstats_artist_id") val songstatsArtistId: String?,
    val name: String?
)

@Serializable
data class ActivityTrackInfo(
    @SerialName("songstats_track_id") val songstatsTrackId: String?,
    val title: String?,
    val avatar: String?,
    @SerialName("release_date") val releaseDate: String?,
    @SerialName("site_url") val siteUrl: String?,
    val artists: List<ActivityTrackInfoArtists>
)

@Serializable
data class Activity(
    val source: String?,
    @SerialName("activity_text") val activityText: String?,
    @SerialName("activity_url") val activityUrl: String?,
    @SerialName("activity_date") val activityDate: String?,
    @SerialName("activity_avatar") val activityAvatar: String?,
    @SerialName("track_info") val trackInfo: ActivityTrackInfo?
)

@Serializable
data class ArtistActivities(
    val activities: List<Activity>
)

@Serializable
data class ArtistDetails(
    val id: String,
    @SerialName("artist_name") val artistName: String,
    @SerialName("avatar_url") val avatarUrl: String,
    val bio: String?,
    @SerialName("video_url") val videoUrl: String?,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("artist_links") val artistLinks: ArtistLinks,
    @SerialName("artist_stats") val artistStats: ArtistStats,
    @SerialName("artist_activities") val artistActivities: ArtistActivities?,
    val songs: List<ArtistSong>
)

@Serializable
data class ArtistSong(
    @SerialName("id") val songId: String,
    @SerialName("song_title") val songTitle: String,
    @SerialName("artwork_url") val artworkUrl: String,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("explicit_lyrics") val explicitLyrics: Boolean,
    @SerialName("add_version_info") val versionInfo: String,
    @SerialName("add_version_info_other") val addVersionInfoOther: String?,
    @SerialName("is_radio_edit") val isRadioEdit: Boolean,
    val duration: Double?,
    val status: String
)


@Serializable
data class ArtistLinks(
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("instagram_url") val instagramUrl: String?,
    @SerialName("facebook_url") val facebookUrl: String?,
    @SerialName("twitter_url") val twitterUrl: String?,
    @SerialName("apple_music_url") val appleMusicUrl: String?,
    @SerialName("beatport_url") val beatportUrl: String?,
    @SerialName("deezer_url") val deezerUrl: String?,
    @SerialName("tidal_url") val tidalUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("soundcloud_url") val soundcloudUrl: String?,
    @SerialName("tracklist_url") val tracklistUrl: String?,
    @SerialName("shazam_url") val shazamUrl: String?,
    @SerialName("songkick_url") val songkickUrl: String?,
    @SerialName("bandsintown_url") val bandsintownUrl: String?,
    @SerialName("itunes_url") val itunesUrl: String?,
    @SerialName("traxsource_url") val traxsourceUrl: String?,
    @SerialName("amazon_url") val amazonUrl: String?,
    @SerialName("tiktok_url") val tiktokUrl: String?,
    @SerialName("musicbrainz_url") val musicbrainzUrl: String?,
)

@Serializable
data class ArtistStats(
    @SerialName("spotify_streams_total") val spotifyStreamsTotal: Long?,
    @SerialName("spotify_monthly_listeners_current") val spotifyMonthlyListenersTotal: Long?,
    @SerialName("spotify_followers_total") val spotifyFollowersTotal: Long?,
    @SerialName("amazon_followers_total") val amazonFollowersTotal: Long?,
    @SerialName("deezer_followers_total") val deezerFollowersTotal: Long?,
    @SerialName("youtube_video_views_total") val youtubeVideoViewsTotal: Long?,
    @SerialName("instagram_followers_total") val instagramFollowersTotal: Long?,
    @SerialName("tiktok_views_total") val tiktokViewsTotal: Long?,
    @SerialName("youtube_subscribers_total") val youtubeSubscribersTotal: Long?,
    @SerialName("soundcloud_streams_total") val soundcloudStreamsTotal: Long?,
    @SerialName("soundcloud_followers_total") val soundcloudFollowersTotal: Long?,
    @SerialName("twitter_followers_total") val twitterFollowersTotal: Long?,
    @SerialName("facebook_followers_total") val facebookFollowersTotal: Long?,
    @SerialName("instagram_views_total") val instagramViewsTotal: Long?,
    @SerialName("shazam_shazams_total") val shazamsTotal: Long?,
    @SerialName("tracklist_tracklist_views_total") val tracklistTotal: Long?,
    @SerialName("shown_stats") val shownStats: List<String>
)



