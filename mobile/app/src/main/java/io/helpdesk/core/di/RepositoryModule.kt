package io.helpdesk.core.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.db.LocalDatabase
import io.helpdesk.model.repos.AuthenticationRepository
import io.helpdesk.model.repos.BaseAuthenticationRepository
import io.helpdesk.model.services.AuthWebService
import io.helpdesk.repository.BaseTicketRepository
import io.helpdesk.repository.TicketRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        service: AuthWebService,
        storage: BaseUserPersistentStorage,
    ): BaseAuthenticationRepository =
        AuthenticationRepository(service, storage)

    @Singleton
    @Provides
    fun provideTicketRepository(
        storage: BaseUserPersistentStorage,
        db: LocalDatabase,
        firestore: FirebaseFirestore,
    ): BaseTicketRepository = TicketRepository(storage, db, firestore)
}