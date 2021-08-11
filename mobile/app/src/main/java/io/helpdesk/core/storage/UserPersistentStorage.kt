package io.helpdesk.core.storage

import android.content.Context
import androidx.core.content.edit
import io.helpdesk.core.util.logger
import io.helpdesk.model.data.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    /**
     * {
     *  "user-id" : "qpwoieoqie939824934opiqe",
     *  "user-type" : 0, -> ADMIN
     * }
     */
    private val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private val _loginState = MutableStateFlow<Boolean>(false)

    init {
        _loginState.value = !prefs.getString(USER_ID_KEY, null).isNullOrEmpty()
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                USER_TYPE_KEY -> {
                    logger.d("changed user type")
                }

                USER_ID_KEY -> {
                    logger.d("changed user id")
                    _loginState.value = !prefs.getString(key, null).isNullOrEmpty()
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
            _loginState.value = !value.isNullOrEmpty()
        }

    /**
     * observer for login state
     */
    override val loginState: Flow<Boolean>
        get() = _loginState.asStateFlow()

    override suspend fun clear() {
        userId = null
        prefs.edit {
            clear()
            apply()
        }
    }


//    private static final String USER_ID_KEY = "";

    companion object {
        private const val USER_ID_KEY = "helpdesk.user_id"
        private const val USER_TYPE_KEY = "helpdesk.user_type"
        private const val PREFS_KEY = "helpdesk.prefs"
    }
}