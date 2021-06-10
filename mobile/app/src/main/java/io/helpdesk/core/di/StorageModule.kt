package io.helpdesk.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.storage.UserPersistentStorage
import javax.inject.Singleton

/**
 * storage module
 *
 * handles scaffolding of persisted storage related dependencies
 */
@InstallIn(SingletonComponent::class)
@Module
object StorageModule {

    @Singleton
    @Provides
    fun providePersistedStorage(@ApplicationContext context: Context): BaseUserPersistentStorage =
        UserPersistentStorage(context)
}