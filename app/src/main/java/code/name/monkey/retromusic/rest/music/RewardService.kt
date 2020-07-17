package code.name.monkey.retromusic.rest.music

import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.rest.music.model.*
import okhttp3.ResponseBody
import org.eclipse.egit.github.core.User
import retrofit2.Call
import retrofit2.http.*
import java.util.*
import kotlin.collections.ArrayList

interface RewardService {

    companion object {
        const val SUFFIX_URL =
                "?platform=android&app_id=${Constants.APPLICATION_ID}&app_version=${Constants.VERSION_NAME}"
    }

    @GET("reward/start$SUFFIX_URL")
    suspend fun startReward(
            @Query("did") did: String,
            @Query("hl") hl: String,
            @Query("push_token") pushToken: String
    ): MusicCommonResponse<RewardData>

    @POST("reward/win$SUFFIX_URL")
    suspend fun winReward(
            @Query("did") did: String,
            @Query("hl") hl: String,
            @Query("multiple") multiple: Int,
            @Body prize: Prize,
            @Query("card") card: String
    ): MusicCommonResponse<RewardData>

    @GET("reward/spin$SUFFIX_URL")
    suspend fun startSpin(
            @Query("did") did: String,
            @Query("hl") hl: String,
            @Query("card") card: String
    ): MusicCommonResponse<RewardData>

    @GET("reward/buy$SUFFIX_URL")
    suspend fun buyReward(
            @Query("did") did: String,
            @Query("address") address: String,
            @Query("hl") hl: String,
            @Query("action") action: Int,
            @Query("card") card: String
    ): MusicCommonResponse<RewardData>

    @Streaming
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}