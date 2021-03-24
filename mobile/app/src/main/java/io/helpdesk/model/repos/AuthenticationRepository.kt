package io.helpdesk.model.repos

import io.helpdesk.core.storage.BaseUserPersistentStorage
import io.helpdesk.core.util.Result
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
 * base authentication repository
 */
interface BaseAuthenticationRepository {

    fun login(params: AuthRequestParams): Flow<Result<User>>

    fun register(params: AuthRequestParams): Flow<Result<User>>

    fun googleAuth(): Flow<Result<User>>

    fun dispose()
}

class AuthenticationRepository @Inject constructor(
    private val service: AuthWebService,
    private val storage: BaseUserPersistentStorage,
) :
    BaseAuthenticationRepository {
    private val job = Job()
    private var authScope: CoroutineScope? = null

    @ExperimentalCoroutinesApi
    override fun login(params: AuthRequestParams): Flow<Result<User>> = channelFlow {
        authScope = this + job
        offer(Result.Loading)
        try {
            val token = service.login(params).await()
            storage.userId = token.token
            offer(Result.Success(token.data))
        } catch (e: Exception) {
            offer(Result.Error(e))
        }

    }

    @ExperimentalCoroutinesApi
    override fun register(params: AuthRequestParams): Flow<Result<User>> = channelFlow {
        authScope = this + job
        offer(Result.Loading)
        try {
            val token = service.register(params).await()
            storage.userId = token.token
            offer(Result.Success(token.data))
        } catch (e: Exception) {
            offer(Result.Error(e))
        }
    }

    @ExperimentalCoroutinesApi
    override fun googleAuth(): Flow<Result<User>> = channelFlow {
        authScope = this + job
        offer(Result.Loading)
        try {
            val token = service.googleAuth().await()
            storage.userId = token.token
            offer(Result.Success(token.data))
        } catch (e: Exception) {
            offer(Result.Error(e))
        }
    }

    override fun dispose() {
        job.cancel()
    }

}