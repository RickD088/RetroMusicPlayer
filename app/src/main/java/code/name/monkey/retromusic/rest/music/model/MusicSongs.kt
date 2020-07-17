package code.name.monkey.retromusic.rest.music.model

import code.name.monkey.retromusic.interfaces.CommonDataConverter
import code.name.monkey.retromusic.model.CommonData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MusicSongs(
    val type: Int,
    val songs: ArrayList<SongBean>
) : CommonDataConverter {
    override fun convertToCommonData(): CommonData {
        return CommonData(type, cloudData = this)
    }
}