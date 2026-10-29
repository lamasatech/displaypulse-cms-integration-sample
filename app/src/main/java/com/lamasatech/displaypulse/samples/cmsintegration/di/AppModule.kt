package com.lamasatech.displaypulse.samples.cmsintegration.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lamasatech.displaypulse.samples.cmsintegration.BuildConfig
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsApi
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsApiFactory
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor.Level

/** Dependency-injection providers for objects that need construction code. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** @return the shared JSON converter. */
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    /**
     * @param settingsStore source of the device address and token.
     * @param gson converter for request and response bodies.
     * @return the shared API client.
     */
    @Provides
    @Singleton
    fun provideCmsApi(settingsStore: ConnectionSettingsStore, gson: Gson): CmsApi =
        CmsApiFactory.create(settingsStore, gson, if (BuildConfig.DEBUG) Level.BODY else Level.NONE)
}
