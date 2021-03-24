package io.helpdesk.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.repos.AuthenticationRepository
import io.helpdesk.model.repos.BaseAuthenticationRepository
import io.helpdesk.model.services.AuthWebService

@InstallIn(ActivityComponent::class)
@Module
object RepositoryModule {

    @Provides
    fun provideAuthRepository(
        service: AuthWebService,
        storage: BaseUserPersistentStorage
    ): BaseAuthenticationRepository =
        AuthenticationRepository(service, storage)
}