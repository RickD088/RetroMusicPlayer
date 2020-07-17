package code.name.monkey.retromusic.rest.music

import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.rest.music.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import kotlin.collections.ArrayList

interface MusicService {

    companion object {
        const val SUFFIX_URL =
                "?platform=android&app_id=${Constants.APPLICATION_ID}&app_version=${Constants.VERSION_NAME}"
    }

    @GET("info/start$SUFFIX_URL")
    suspend fun startupInfo(
            @Query("uid") uid: String,
            @Query("did") did: String,
            @Query("loc") loc: String,
            @Query("lang") lang: String,
            @Query("hl") hl: String
    ): ServerInfo

    @GET("info/reg$SUFFIX_URL")
    suspend fun startupReg(
            @Query("uid") uid: String,
            @Query("did") did: String,
            @Query("name") name: String,
            @Query("avatar") avatar: String,
            @Query("hl") hl: String
    ): MusicCommonResponse<UserInfo>

    @GET("home/all$SUFFIX_URL")
    suspend fun discoverAll(
            @Query("hl") language: String
    ): MusicHome

    @GET("playlist/songs$SUFFIX_URL")
    suspend fun getArtistSong(
            @Query("singerId") singerId: Int,
            @Query("row") row: Int,
            @Query("page") page: Int
    ): MusicCommonResponse<MusicArtist>

    @GET("playlist/all$SUFFIX_URL")
    suspend fun getPlaylistSong(
            @Query("playlistId") playlistId: Int,
            @Query("rows") rows: Int,
            @Query("start") start: Int,
            @Query("hl") language: String
    ): MusicCommonResponse<ArrayList<SongBean>>

    @GET("find/songs$SUFFIX_URL")
    suspend fun findSong(
            @Query("q") query: String,
            @Query("hl") language: String,
            @Query("page") page: Int,
            @Query("size") size: Int
    ): MusicCommonResponse<MusicSearch>

    @GET("song/keywords$SUFFIX_URL")
    suspend fun getKeywords(
            @Query("hl") language: String
    ): MusicCommonResponse<ArrayList<SearchKeywords>>

    @GET("user/getUserData$SUFFIX_URL")
    suspend fun getUserData(
            @Query("uid") uid: String
    ): MusicCommonResponse<UserData>

    @POST("user/syncUserData$SUFFIX_URL")
    suspend fun syncUserData(
            @Query("uid") uid: String,
            @Body data: UserData
    ): MusicCommonResponse<String>

    @Streaming
    @GET
    fun downloadFile(@Url url: String): Call<ResponseBody>
}