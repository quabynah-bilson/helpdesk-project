package io.helpdesk.core.storage

import android.content.Context
import androidx.core.content.edit
import io.helpdesk.core.util.logger
import io.helpdesk.model.data.UserType
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * persisted storage wrapper
 */
interface BaseUserPersistentStorage {
    var userId: String?
    var userType: Int
    val loginState: Flow<Boolean>

    suspend fun clear()
}

class UserPersistentStorage @Inject constructor(context: Context) : BaseUserPersistentStorage {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private val _loginState = MutableSharedFlow<Boolean>()

    init {
        println("starting prefs")
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                USER_TYPE_KEY -> {
                    logger.d("changed user type")
                    _loginState.tryEmit(!prefs.getString(key, null).isNullOrEmpty())
                }

                USER_ID_KEY -> {
                    logger.d("changed user id")
                }
            }
        }
    }

    override var userType: Int
        get() = prefs.getInt(USER_TYPE_KEY, UserType.Customer.ordinal)
        set(value) {
            prefs.edit(false) {
                putInt(USER_TYPE_KEY, value)
            }
        }

    /**
     * authenticated user id storage
     */
    override var userId: String?
        get() = prefs.getString(USER_ID_KEY, null)
        set(value) {
            logger.d("storing $value")
            prefs.edit(false) {
                putString(USER_ID_KEY, value)
            }
            // notify all observers
            _loginState.tryEmit(!value.isNullOrEmpty())
        }

    /**
     * observer for login state
     */
    override val loginState: Flow<Boolean>
        get() = _loginState

    override suspend fun clear() {
        userId = null
    }

    companion object {
        private const val USER_ID_KEY = "helpdesk.user_id"
        private const val USER_TYPE_KEY = "helpdesk.user_type"
        private const val PREFS_KEY = "helpdesk.prefs"
    }
}