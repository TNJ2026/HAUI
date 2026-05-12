package ai.tnj.haui.core.network.di

import ai.tnj.haui.core.model.HauiJson
import ai.tnj.haui.core.network.HermesEndpoint
import ai.tnj.haui.core.network.HermesEndpointInterceptor
import ai.tnj.haui.core.network.HermesService
import ai.tnj.haui.core.utils.LogUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        endpointInterceptor: HermesEndpointInterceptor,
    ): OkHttpClient {
        // Avoid logging request/response bodies (which include the bearer
        // token and chat payloads) in release builds.
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (LogUtil.isDebug) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(endpointInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = HauiJson

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(HermesEndpoint.PLACEHOLDER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideHermesService(retrofit: Retrofit): HermesService =
        retrofit.create(HermesService::class.java)
}
