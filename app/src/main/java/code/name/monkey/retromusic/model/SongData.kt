package code.name.monkey.retromusic.model

import androidx.annotation.Keep
import code.name.monkey.retromusic.rest.music.model.SongBean

@Keep
data class SongData(
    val dataType: Int,
    val localData: Song,
    val cloudData: SongBean
) {
}