package code.name.monkey.retromusic.rest.music.model

import code.name.monkey.retromusic.BuildConfig
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.rest.music.RewardService
import com.google.gson.Gson
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RewardClient {

    private var rewardService: RewardService

    fun getApiService(): RewardService {
        return rewardService
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .callFactory(createDefaultOkHttpClientBuilder().build())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()

        rewardService = retrofit.create(RewardService::class.java)
    }

    private fun createDefaultOkHttpClientBuilder(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .retryOnConnectionFailure(true)
            .connectTimeout(Constants.TEN_SECONDS_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.HALF_MINUTE_MS, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.HALF_MINUTE_MS, TimeUnit.MILLISECONDS)
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
}