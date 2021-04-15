package io.helpdesk.core.storage

import android.content.Context
import androidx.core.content.edit
import io.helpdesk.core.util.logger
import io.helpdesk.model.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * persisted storage wrapper
 */
interface BaseUserPersistentStorage {
    var userId: String?
    var userType: Int
    val loginState: StateFlow<Boolean>

    suspend fun clear()
}

class UserPersistentStorage @Inject constructor(context: Context) : BaseUserPersistentStorage {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private val _loginState = MutableStateFlow(!prefs.getString(USER_ID_KEY, null).isNullOrEmpty())

    override var userType: Int = UserType.Customer.ordinal
        get() = prefs.getInt(USER_TYPE_KEY, UserType.Customer.ordinal)
        set(value) {
            if (field == value) return
            prefs.edit(false) {
                putInt(USER_TYPE_KEY, value)
            }
        }

    /**
     * authenticated user id storage
     */
    override var userId: String? = null
        get() = prefs.getString(USER_ID_KEY, null)
        set(value) {
            logger.d("storing $value")
            if (field == value) return
            prefs.edit(false) {
                putString(USER_ID_KEY, value)
            }
            // notify all observers
            _loginState.value = !value.isNullOrEmpty()
        }

    /**
     * observer for login state
     */
    override val loginState: StateFlow<Boolean>
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