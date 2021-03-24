package io.helpdesk.core.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.services.AuthWebService
import io.helpdesk.model.services.TokenizedInterceptor
import io.helpdesk.model.services.TokenizedWebService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Singleton

/**
 * network module
 *
 * handles scaffolding of network related dependencies
 */
@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    // todo -> replace this when deployed to a remote server
    private const val BASE_URL = "http://10.0.2.2:2021/"

    @Singleton
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Timber.tag("HelpDesk Mobile Service").d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Singleton
    @Provides
    fun provideTokenizedInterceptor(storage: BaseUserPersistentStorage): Interceptor =
        TokenizedInterceptor(storage)

    @Singleton
    @Provides
    fun provideHttpClient(
        interceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addNetworkInterceptor(interceptor)
            .addInterceptor(loggingInterceptor).build()

    @Singleton
    @Provides
    fun provideConverterFactory(): Converter.Factory = GsonConverterFactory.create(Gson())

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(converterFactory)
            .client(client)
            .build()

    @Singleton
    @Provides
    fun provideAuthWebService(retrofit: Retrofit): AuthWebService =
        retrofit.create(AuthWebService::class.java)

    @Singleton
    @Provides
    fun provideTokenizedWebService(retrofit: Retrofit): TokenizedWebService =
        retrofit.create(TokenizedWebService::class.java)
}