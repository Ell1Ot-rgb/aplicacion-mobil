package com.ell1ot.l13monitor.di

import android.content.Context
import androidx.room.Room
import com.ell1ot.l13monitor.BuildConfig
import com.ell1ot.l13monitor.data.local.db.AppDatabase
import com.ell1ot.l13monitor.data.remote.HerokuApiService
import com.ell1ot.l13monitor.data.remote.L13ApiService
import com.ell1ot.l13monitor.data.repository.CredentialsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.CertificatePinner
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Provides
    @Singleton
    @Named("L13_BASE_URL_DEFAULT")
    fun provideDefaultBaseUrl(): String = BuildConfig.L13_BASE_URL

    @Provides
    @Singleton
    @Named("L13")
    fun provideL13OkHttp(credentials: CredentialsRepository): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                // v3 fix (audit #2023): resolve base URL per request from
                // CredentialsRepository and only attach Bearer to that host.
                val original = chain.request()
                val dynamicBase = credentials.baseUrl
                var newUrl = original.url
                val parsed = dynamicBase.toHttpUrlOrNull()
                if (parsed != null) {
                    newUrl = original.url.newBuilder()
                        .scheme(parsed.scheme)
                        .host(parsed.host)
                        .port(parsed.port)
                        .build()
                }
                val token = credentials.bearerToken
                val targetHost = parsed?.host
                val builder = original.newBuilder().url(newUrl)
                if (!token.isNullOrBlank() && targetHost != null && newUrl.host == targetHost) {
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)

        if (!BuildConfig.DEBUG) {
            // Pins are supplied via gradle property L13_PINS (comma-separated SPKI).
            // Empty here means "no pinning configured" rather than a bogus pin.
            val pins = BuildConfig.L13_PINS.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            if (pins.isNotEmpty()) {
                val pb = CertificatePinner.Builder()
                pins.forEach { pb.add("*.herokuapp.com", it) }
                builder.certificatePinner(pb.build())
            }
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @Named("Heroku")
    fun provideHerokuOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    @Named("L13")
    fun provideL13Retrofit(
        @Named("L13") client: OkHttpClient,
        credentials: CredentialsRepository,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        // v3 fix: static fallback only; real base URL is rewritten
        // per request by the interceptor above.
        .baseUrl(BuildConfig.L13_BASE_URL + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideL13Api(@Named("L13") retrofit: Retrofit): L13ApiService = retrofit.create(L13ApiService::class.java)

    @Provides
    @Singleton
    @Named("Heroku")
    fun provideHerokuRetrofit(
        @Named("Heroku") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HEROKU_API_BASE + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideHerokuApi(@Named("Heroku") retrofit: Retrofit): HerokuApiService = retrofit.create(HerokuApiService::class.java)

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "l13.db",
    ).build() // v3 fix: no destructive fallback migration

    @Provides fun provideCycleDao(db: AppDatabase) = db.cycleDao()
    @Provides fun provideCommandLogDao(db: AppDatabase) = db.commandLogDao()
    @Provides fun provideHealthDao(db: AppDatabase) = db.healthDao()

    @Provides
    fun provideTransport(
        rest: com.ell1ot.l13monitor.core.transport.RestTransport,
    ): com.ell1ot.l13monitor.core.transport.Transport = rest
}

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
