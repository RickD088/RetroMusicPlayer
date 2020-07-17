package code.name.monkey.retromusic.rest.music.model

import androidx.annotation.Keep
import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.model.CommonData
import code.name.monkey.retromusic.rest.music.MusicClient
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class MusicArtist(
    val singer: PlaylistBean,
    val songs: ArrayList<SongBean>
) : CommonDataConverter {
    override fun convertToCommonData(): CommonData {
        return CommonData(CommonData.TYPE_CLOUD_ARTIST, cloudData = this)
    }
}

@Keep
data class SingerBean(
    val id: Int,
    val img: String,
    val originalId: String,
    val title: String,
    val tracksCount: Int,
    val type: String
)
