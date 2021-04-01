package io.helpdesk.model.db

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.helpdesk.model.data.TicketCompletionState
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.TicketType
import io.helpdesk.model.data.UserType

/**
 * [TypeConverter] for [TicketCompletionState]
 */
class TicketStateConverter {
    @TypeConverter
    fun toTicketState(value: Int): TicketCompletionState =
        enumValues<TicketCompletionState>()[value]

    @TypeConverter
    fun fromTicketState(value: TicketCompletionState): Int = value.ordinal
}

/**
 * [TypeConverter] for [TicketType]
 */
class TicketTypeConverter {
    @TypeConverter
    fun toTicketType(value: Int): TicketType = enumValues<TicketType>()[value]

    @TypeConverter
    fun fromTicketType(value: TicketType): Int = value.ordinal
}

/**
 * [TypeConverter] for [TicketPriority]
 */
class TicketPriorityConverter {
    @TypeConverter
    fun toTicketPriority(value: Int): TicketPriority = enumValues<TicketPriority>()[value]

    @TypeConverter
    fun fromTicketPriority(value: TicketPriority): Int = value.ordinal
}

/**
 * [TypeConverter] for [UserType]
 */
class UserTypeConverter {
    @TypeConverter
    fun toUserType(value: Int): UserType = enumValues<UserType>()[value]

    @TypeConverter
    fun fromUserType(value: UserType): Int = value.ordinal
}

class ListOfStringConverter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @TypeConverter
    fun toListOfStrings(value: String): List<String> = gson.fromJson<List<String>>(
        value,
        object : TypeToken<List<String>>() {
        }.type
    ) ?: emptyList()

    @TypeConverter
    fun fromListOfString(value: List<String>): String = value.toString()

}