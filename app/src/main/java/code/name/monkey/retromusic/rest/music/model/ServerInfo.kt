package code.name.monkey.retromusic.rest.music.model

import androidx.annotation.Keep

@Keep
data class ServerInfo(
    val status: Int,
    val errorMsg: String,
    val data: ConfigInfo
)

@Keep
data class ConfigInfo(
    val devAuthStatus: String,
    val minSupVer: String,
    val ads: AdsItems,
    val ads1: AdsItems,
    val utc: Long,
    val svt: Float,
    val ip: String
)

@Keep
data class AdsItems(
    var welcomeInterstitial: AdsItem = AdsItem(),
    var mainTinyBannerAdmob: AdsItem = AdsItem()
)

@Keep
data class AdsItem(
    val type: String = "",
    val placement: String = "",
    val percentage: Int = 50
)

