package io.helpdesk.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.helpdesk.model.db.LocalDatabase
import io.helpdesk.model.db.QuestionDao
import io.helpdesk.model.db.TicketDao
import io.helpdesk.model.db.UserDao
import javax.inject.Singleton

/**
 * local database module
 *
 * provides singletons for local database & its DAOs
 */
@InstallIn(SingletonComponent::class)
@Module
object LocalDatabaseModule {

    @Singleton
    @Provides
    fun provideLocalDatabase(@ApplicationContext context: Context): LocalDatabase =
        LocalDatabase.get(context)

    @Singleton
    @Provides
    fun provideUserDao(db: LocalDatabase): UserDao = db.userDao()

    @Singleton
    @Provides
    fun provideTicketDao(db: LocalDatabase): TicketDao = db.ticketDao()

    @Singleton
    @Provides
    fun provideFAQsDao(db: LocalDatabase): QuestionDao = db.faqDao()
}