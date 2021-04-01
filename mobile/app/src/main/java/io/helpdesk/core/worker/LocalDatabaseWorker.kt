package io.helpdesk.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.helpdesk.core.util.deserializeJson
import io.helpdesk.model.data.Question
import io.helpdesk.model.db.LocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class LocalDatabaseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val faqsDao = LocalDatabase.get(applicationContext).faqDao()
        withContext(Dispatchers.IO) {
            val faqs =
                applicationContext.deserializeJson<Question>("faqs.json") {
                    Question.parser(it)
                }
            faqsDao.insertAll(faqs)
        }

        return Result.success()
    }
}