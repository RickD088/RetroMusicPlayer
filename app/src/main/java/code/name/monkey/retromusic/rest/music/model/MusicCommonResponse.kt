package code.name.monkey.retromusic.rest.music.model

import androidx.annotation.Keep

@Keep
class MusicCommonResponse<T>(
    val status: Int,
    val errorMsg: String,
    val data: T
)