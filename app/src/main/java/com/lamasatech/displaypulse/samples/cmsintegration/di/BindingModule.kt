package com.lamasatech.displaypulse.samples.cmsintegration.di

import com.lamasatech.displaypulse.samples.cmsintegration.data.CmsRepositoryImpl
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.KeystoreTokenCipher
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PlaybackStore
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PrefsConnectionSettingsStore
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PrefsPlaybackStore
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.TokenCipher
import com.lamasatech.displaypulse.samples.cmsintegration.domain.Clock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.CmsRepository
import com.lamasatech.displaypulse.samples.cmsintegration.domain.IdGenerator
import com.lamasatech.displaypulse.samples.cmsintegration.domain.SystemClock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.UuidIdGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Dependency-injection bindings from interfaces to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    /** Uses [CmsRepositoryImpl] wherever a [CmsRepository] is requested. */
    @Binds
    @Singleton
    abstract fun bindRepository(impl: CmsRepositoryImpl): CmsRepository

    /** Stores connection settings in the app's private preferences. */
    @Binds
    abstract fun bindConnectionSettingsStore(impl: PrefsConnectionSettingsStore): ConnectionSettingsStore

    /** Encrypts the stored access token with a key held in the Android Keystore. */
    @Binds
    abstract fun bindTokenCipher(impl: KeystoreTokenCipher): TokenCipher

    /** Stores the campaign, the open play and the pending event in the app's private preferences. */
    @Binds
    abstract fun bindPlaybackStore(impl: PrefsPlaybackStore): PlaybackStore

    /** Uses the system wall clock for timestamps. */
    @Binds
    abstract fun bindClock(impl: SystemClock): Clock

    /** Generates random UUIDs for every id the app creates. */
    @Binds
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator
}
