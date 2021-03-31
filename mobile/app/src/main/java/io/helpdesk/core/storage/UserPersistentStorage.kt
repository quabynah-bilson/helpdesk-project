package io.helpdesk.core.storage

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * persisted storage wrapper
 */
interface BaseUserPersistentStorage {
    var userId: String?
    val loginState: StateFlow<Boolean>
}

class UserPersistentStorage @Inject constructor(context: Context) : BaseUserPersistentStorage {
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private val _loginState = MutableStateFlow(false)

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

    companion object {
        private const val USER_ID_KEY = "helpdesk.user_id"
        private const val PREFS_KEY = "helpdesk.prefs"
    }
}