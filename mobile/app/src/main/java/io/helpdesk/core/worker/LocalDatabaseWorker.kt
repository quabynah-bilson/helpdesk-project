package io.helpdesk.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.helpdesk.core.util.deserializeJson
import io.helpdesk.model.data.Question
import io.helpdesk.model.data.User
import io.helpdesk.model.db.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads sample data into the database
 */
@HiltWorker
class LocalDatabaseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    // local database
    private val db = LocalDatabase.get(applicationContext)

    // data access objects
    private val faqsDao = db.faqDao()
    private val userDao = db.userDao()

    // remote database
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            // faqs
            val faqs =
                applicationContext.deserializeJson("faqs.json") {
                    Question.parser(it)
                }
            faqsDao.insertAll(faqs)

            // technicians
            val technicians =
                applicationContext.deserializeJson("technicians.json") {
                    User.parser(it)
                }
            userDao.insertAll(technicians)

            // upload faqs to firestore
            faqs.forEach { faq ->
                firestore.collection(Question.TABLE_NAME).document(faq.id)
                    .set(faq, SetOptions.merge())
            }

            // upload user to firestore
            technicians.forEach { user ->
                firestore.collection(User.TABLE_NAME).document(user.id)
                    .set(user, SetOptions.merge())
            }
        }

        return Result.success()
    }
}