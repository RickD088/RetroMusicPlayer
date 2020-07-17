package code.name.monkey.retromusic.rest.music

import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.BuildConfig
import code.name.monkey.retromusic.abram.Constants
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object MusicClient {

    private var musicService: MusicService

    fun getApiService(): MusicService {
        return musicService
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .callFactory(createDefaultOkHttpClientBuilder().build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        musicService = retrofit.create(MusicService::class.java)
    }

    private fun createDefaultOkHttpClientBuilder(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .retryOnConnectionFailure(true)
            .connectTimeout(Constants.TEN_SECONDS_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.HALF_MINUTE_MS, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.HALF_MINUTE_MS, TimeUnit.MILLISECONDS)
            .cache(createDefaultCache())
            .addInterceptor(createCacheControlInterceptor())
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(createLogInterceptor())
        }
        return builder
    }

    private fun createLogInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    private fun createCacheControlInterceptor(): Interceptor {
        return Interceptor { chain ->
            val modifiedRequest = chain.request().newBuilder()
                .addHeader("Cache-Control", "max-age=31536000, max-stale=31536000")
                .build()
            chain.proceed(modifiedRequest)
        }
    }

    private fun createDefaultCache(): Cache? {
        val cacheDir = File(App.getContext().cacheDir.absolutePath, "/okhttp-music/")
        if (cacheDir.mkdirs() || cacheDir.isDirectory) {
            return Cache(cacheDir, 1024 * 1024 * 10)
        }
        return null
    }
}