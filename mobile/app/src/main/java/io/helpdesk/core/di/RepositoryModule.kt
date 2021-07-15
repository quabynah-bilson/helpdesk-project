package io.helpdesk.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.db.LocalDatabase
import io.helpdesk.model.db.UserDao
import io.helpdesk.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Singleton
    @Provides
    fun provideAuthRepository(
        scope: CoroutineScope,
        userDao: UserDao,
        storage: BaseUserPersistentStorage,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        messaging: FirebaseMessaging,
    ): BaseAuthenticationRepository =
        AuthenticationRepository(scope, userDao, storage, firestore, auth, messaging)

    @Singleton
    @Provides
    fun provideUserRepository(
        scope: CoroutineScope,
        userDao: UserDao,
        storage: BaseUserPersistentStorage,
        firestore: FirebaseFirestore,
    ): BaseUserRepository =
        UserRepository(scope, userDao, storage, firestore)

    @Singleton
    @Provides
    fun provideTicketRepository(
        scope: CoroutineScope,
        storage: BaseUserPersistentStorage,
        db: LocalDatabase,
        firestore: FirebaseFirestore,
    ): BaseTicketRepository = TicketRepository(scope, storage, db, firestore)
}