package io.helpdesk.model.services

import io.helpdesk.model.data.BaseResponse
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.User
import retrofit2.Call
import retrofit2.http.*

/**
 * tokenized web service -> requires that all calls are backed by authenticated user's access token
 */
interface TokenizedWebService {

    // region users
    @GET("/api/users")
    suspend fun allUsers(): Call<BaseResponse<List<User>>>

    @GET("/api/users/me")
    suspend fun currentUser(): Call<BaseResponse<User>>

    @GET("/api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Call<BaseResponse<User>>

    @PUT("/api/users/{id}")
    suspend fun updateProfile(@Path("id") id: String, @Body user: User): Call<BaseResponse<User>>

    // endregion

    // region tickets
    @GET("/api/tickets")
    suspend fun tickets(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): Call<BaseResponse<List<Ticket>>>
    // endregion
}