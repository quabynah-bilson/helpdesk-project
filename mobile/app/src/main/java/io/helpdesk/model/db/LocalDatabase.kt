package io.helpdesk.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.helpdesk.core.worker.LocalDatabaseWorker
import io.helpdesk.model.data.Question
import io.helpdesk.model.data.Ticket
import io.helpdesk.model.data.User

/**
 * local database implementation using [RoomDatabase]
 */
@TypeConverters(
    TicketStateConverter::class,
    TicketTypeConverter::class,
    TicketPriorityConverter::class,
    ListOfStringConverter::class,
)
@Database(
    entities = [User::class, Ticket::class, Question::class],
    version = 1,
    exportSchema = true
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun ticketDao(): TicketDao
    abstract fun faqDao(): QuestionDao

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        @JvmStatic
        fun get(context: Context): LocalDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context, LocalDatabase::class.java, "helpdesk.db")
                .fallbackToDestructiveMigrationOnDowngrade()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        with(WorkManager.getInstance(context)) {
                            enqueue(OneTimeWorkRequestBuilder<LocalDatabaseWorker>().build())
                        }
                    }
                })
                .build()
                .also { instance = it }
        }
    }
}