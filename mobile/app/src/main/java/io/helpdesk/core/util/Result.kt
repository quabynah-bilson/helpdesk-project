package io.helpdesk.core.util

/**
 * wrapper for repository result
 */
sealed class Result<out R> {
    object Initial : Result<Nothing>()  // initial
    object Loading : Result<Nothing>()  // loading
    data class Success<out T>(val data: T) : Result<T>()    // successful
    data class Error(val exception: Exception?) : Result<Nothing>()  // error

//    val isSuccessful get() = this is Success<*> && data != null
}

