package io.helpdesk.model.repos

import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.model.data.AuthRequestParams
import io.helpdesk.model.data.User
import io.helpdesk.model.services.AuthWebService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.plus
import retrofit2.await
import javax.inject.Inject

/**
 *
 */
sealed class AuthState {
    object Loading : AuthState()
    data class Success(val data: User) : AuthState()
    data class Error(val reason: String?) : AuthState()
}

val AuthState.isSuccessful: Boolean
    get() = this is AuthState.Success

interface BaseAuthenticationRepository {
    fun login(params: AuthRequestParams): Flow<AuthState>

    fun register(params: AuthRequestParams): Flow<AuthState>

    fun googleAuth(): Flow<AuthState>

    fun dispose()
}

class AuthenticationRepository @Inject constructor(
    private val service: AuthWebService,
    private val storage: BaseUserPersistentStorage
) :
    BaseAuthenticationRepository {
    private val job = Job()
    var authScope: CoroutineScope? = null

    @ExperimentalCoroutinesApi
    override fun login(params: AuthRequestParams): Flow<AuthState> = channelFlow {
        authScope = this + job
        offer(AuthState.Loading)
        try {
            val token = service.login(params).await()
            storage.userId = token.token
            offer(AuthState.Success(token.data))
        } catch (e: Exception) {
            offer(AuthState.Error(e.localizedMessage))
        }

    }

    @ExperimentalCoroutinesApi
    override fun register(params: AuthRequestParams): Flow<AuthState> = channelFlow {
        authScope = this + job
        offer(AuthState.Loading)
        try {
            val token = service.register(params).await()
            storage.userId = token.token
            offer(AuthState.Success(token.data))
        } catch (e: Exception) {
            offer(AuthState.Error(e.localizedMessage))
        }
    }

    @ExperimentalCoroutinesApi
    override fun googleAuth(): Flow<AuthState> = channelFlow {
        authScope = this + job
        offer(AuthState.Loading)
        try {
            val token = service.googleAuth().await()
            storage.userId = token.token
            offer(AuthState.Success(token.data))
        } catch (e: Exception) {
            offer(AuthState.Error(e.localizedMessage))
        }
    }

    override fun dispose() {
        job.cancel()
    }

}