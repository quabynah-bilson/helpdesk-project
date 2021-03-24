package io.helpdesk.model.services

import io.helpdesk.model.data.AccessToken
import io.helpdesk.model.data.AuthRequestParams
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * authentication web service
 */
interface AuthWebService {

    @POST("/api/auth/login")
    suspend fun login(params: AuthRequestParams): Call<AccessToken>

    @POST("/api/auth/new")
    suspend fun register(params: AuthRequestParams): Call<AccessToken>

    @GET("/api/auth/google")
    suspend fun googleAuth(): Call<AccessToken>
}