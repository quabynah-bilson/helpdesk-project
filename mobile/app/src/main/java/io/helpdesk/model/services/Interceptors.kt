package io.helpdesk.model.services

import io.helpdesk.core.storage.BaseUserPersistentStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * interceptor for user elevated network requests
 */
class TokenizedInterceptor(private val prefs: BaseUserPersistentStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        with(request) {
            addHeader("Content-Type", "application/json")
            addHeader("User-Agent", "HelpDesk Mobile")
            addHeader("x-auth-token", prefs.userId ?: "")
        }
        return chain.proceed(request.build())
    }
}