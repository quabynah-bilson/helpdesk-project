package io.helpdesk.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.User

/**
 * local database implementation using [RoomDatabase]
 */
@TypeConverters(
    TicketStateConverter::class,
    TicketTypeConverter::class,
    TicketPriorityConverter::class,
)
@Database(entities = [User::class, Ticket::class], version = 3, exportSchema = true)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        @JvmStatic
        fun get(context: Context): LocalDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, LocalDatabase::class.java, "helpdesk.db")
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                .also { instance = it }
        }
    }
}