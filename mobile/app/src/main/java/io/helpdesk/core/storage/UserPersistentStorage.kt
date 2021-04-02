package io.helpdesk.core.storage

import android.content.Context
import androidx.core.content.edit
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
    private val _loginState = MutableStateFlow(false)

    override var userType: Int = UserType.Customer.ordinal
        get() = prefs.getInt(USER_TYPE_KEY, UserType.Customer.ordinal)
        set(value) {
            if (field == value) return
            prefs.edit {
                putInt(USER_TYPE_KEY, value)
                apply()
            }
        }

    /**
     * authenticated user id storage
     */
    override var userId: String? = ""
        get() = prefs.getString(USER_ID_KEY, null)
        set(value) {
            if (field == value) return
            prefs.edit {
                putString(USER_ID_KEY, value)
                apply()
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
        prefs.edit {
            clear()
            apply()
            _loginState.emit(false)
        }
    }

    companion object {
        private const val USER_ID_KEY = "helpdesk.user_id"
        private const val USER_TYPE_KEY = "helpdesk.user_type"
        private const val PREFS_KEY = "helpdesk.prefs"
    }
}