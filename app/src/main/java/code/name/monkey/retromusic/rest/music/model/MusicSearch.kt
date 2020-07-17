package code.name.monkey.retromusic.rest.music.model

import androidx.annotation.Keep

@Keep
data class MusicSearch(
    val nextPageTokenVideo: String,
    val videos: List<Any>,
    val songs: ArrayList<SongBean>
)

@Keep
data class SearchKeywords(
    val id: Long,
    val keyword: String?,
    val icon: String?
)