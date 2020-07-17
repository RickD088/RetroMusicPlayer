package code.name.monkey.retromusic.rest.music.model

import androidx.annotation.Keep
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.rest.music.MusicClient
import kotlinx.android.parcel.Parcelize

@Keep
data class MusicHome(
        val status: Int = 200,
        val errorMsg: String = "ok",
        val data: DiscoverBean = DiscoverBean()
)

@Keep
data class DiscoverBean(
        val charts: Charts = Charts(),
        val generalPlaylists: ArrayList<PlaylistBean> = arrayListOf(
            PlaylistBean("", 1, "https://i.ytimg.com/vi/QvE9xOhSWSo/hqdefault.jpg", "PL8mq1LRdzJm0Jd_fRPMfjLiqgD3Gn7KXx", "著作権フリー音楽素材 - Royalty Free Music", 0, "Playlist"),
            PlaylistBean("", 2, "https://i.ytimg.com/vi/uZVDj48GbuI/hqdefault.jpg", "PLw9eIDdirOVuNwjpfjiJGkS3txNouQWSu", "License-free Music list", 0, "Playlist"),
            PlaylistBean("", 3, "https://i.ytimg.com/vi/JzC6QZzmHUQ/hqdefault.jpg", "PLY6TTFtqY0ArCJychSvgft9w5WqA9pG5Y", "ボーカルつきフリーBGM集", 0, "Playlist"),
            PlaylistBean("", 4, "https://i.ytimg.com/vi/cle5RVo82ok/hqdefault.jpg", "PL7zrP2jMzaIaMJuLHT2R4SkBtfxAMnphs", "商用フリーBGM著作権フリー音楽 Free Music", 0, "Playlist"),
            PlaylistBean("", 5, "https://i.ytimg.com/vi/Bxdd2wKc0VQ/hqdefault.jpg", "PLw-EF7Go2fRtjDCxwUkcvIuhR1Lip-Hl2", "配信用著作権フリーBGM", 0, "Playlist"),
            PlaylistBean("", 6, "https://i.ytimg.com/vi/TMhxERHQOKI/hqdefault.jpg", "PLVBidPHJMXWrGgpCd6stMGx3rADYVv9qk", "著作権フリー 音楽 ポップ", 0, "Playlist")
        ),
        val genre: List<Genre> = listOf(),
        val hotSingerPlaylists: ArrayList<PlaylistBean> = arrayListOf(),
        val latest: Latest = Latest(),
        val recommendations: List<SongBean> = listOf(),
        val top: List<SongBean> = listOf()
)

@Keep
data class Charts(
        val Billboard: List<SongBean> = listOf(),
        val Collection: List<SongBean> = listOf(),
        val Listen: List<SongBean> = listOf(),
        val Mnet: List<SongBean> = listOf(),
        val Oricon: List<SongBean> = listOf(),
        val UK: List<SongBean> = listOf(),
        val iTunes: List<SongBean> = listOf()
)

@Parcelize
@Keep
data class PlaylistBean(
        val description: String?,
        val id: Int,
        var img: String?,
        val originalId: String,
        val title: String,
        val tracksCount: Int,
        val type: String
) : CommonDataConverter {
    override fun convertToCommonData(): CommonData {
        if (type == Constants.PLAYLIST_SINGER) {
            return CommonData(CommonData.TYPE_CLOUD_ARTIST_PLAYLIST, cloudData = this)
        } else {
            return CommonData(CommonData.TYPE_CLOUD_PLAYLIST, cloudData = this)
        }
    }
}

@Keep
data class Genre(
        val id: Int,
        val image: String,
        val title: String
)

@Keep
data class Latest(
        val Europe: List<SongBean> = listOf(),
        val Japan: List<SongBean> = listOf(),
        val Korea: List<SongBean> = listOf()
)

@Parcelize
@Keep
data class SongBean(
        var artworkBigUrl: String?,
        var artworkUrl: String?,
        var cache: String?,
        val channelTitle: String,
        val durationInSeconds: Int,
        val heatIndex: Float,
        val id: Int,
        val originalId: String?,
        val singerName: String,
        val songId: String,
        val songName: String,
        val source: String,
        var title: String?
) : CommonDataConverter {
    override fun convertToCommonData(): CommonData {
        if (title.isNullOrBlank()) {
            title = songName
        }
        return CommonData(CommonData.TYPE_CLOUD_SONG, cloudData = this)
    }

    fun getDuration(): Long {
        return durationInSeconds * 1000L
    }
}

@Keep
data class CustomPlaylist(
        val title: String? = null,
        val oldTitle: String? = null,
        val img: String? = null,
        val data: List<SongBean>? = null
)

@Keep
data class UserData(
        val history: List<SongBean>? = null,
        val favorite: List<SongBean>? = null,
        val download: List<SongBean>? = null,
        val playlist: List<PlaylistBean>? = null,
        val customlist: List<CustomPlaylist>? = null
)

@Keep
data class UserInfo(
        val name: String? = null,
        val contact: String? = null,
        val avatar: String? = null
)
