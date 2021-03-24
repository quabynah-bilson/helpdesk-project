package io.helpdesk.model.db

import androidx.room.TypeConverter
import io.helpdesk.model.data.TicketCompletionState
import io.helpdesk.model.data.TicketPriority
import io.helpdesk.model.data.TicketType

/**
 * [TypeConverter] for [TicketCompletionState]
 */
class TicketStateConverter {
    @TypeConverter
    fun toTicketState(value: String) = enumValueOf<TicketCompletionState>(value)

    @TypeConverter
    fun fromTicketState(value: TicketCompletionState) = value.name
}

/**
 * [TypeConverter] for [TicketType]
 */
class TicketTypeConverter {
    @TypeConverter
    fun toTicketType(value: String) = enumValueOf<TicketType>(value)

    @TypeConverter
    fun fromTicketType(value: TicketType) = value.name
}

/**
 * [TypeConverter] for [TicketPriority]
 */
class TicketPriorityConverter {
    @TypeConverter
    fun toTicketPriority(value: String) = enumValueOf<TicketPriority>(value)

    @TypeConverter
    fun fromTicketPriority(value: TicketPriority) = value.name
}