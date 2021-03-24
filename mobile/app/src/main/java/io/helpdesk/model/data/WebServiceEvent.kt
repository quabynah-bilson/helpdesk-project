package io.helpdesk.model.data

import com.google.gson.annotations.SerializedName

/**
 * wrapper for responses from server APIs
 */
data class BaseResponse<T>(var data: T?, var message: String?)

/**
 * parameters for authentication requests
 */
data class AuthRequestParams(@SerializedName("username") val email: String, val password: String)

/**
 * authentication response
 */
data class AccessToken(val data: User, @SerializedName("access_token") val token: String)