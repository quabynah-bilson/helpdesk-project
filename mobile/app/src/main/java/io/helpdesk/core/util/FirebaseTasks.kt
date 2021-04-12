package io.helpdesk.core.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Query snapshots
 */
suspend inline fun <reified T> Task<QuerySnapshot>.fold(
    crossinline successBlock: suspend (MutableList<T>?) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) = withContext(Dispatchers.IO) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            launch { successBlock(snapshot.result?.toObjects(T::class.java)) }
        } else {
            launch { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener { launch { errorBlock(it) } }
}

suspend inline fun Task<Void>.await() = withContext(Dispatchers.IO) {
    try {
        addOnCompleteListener {
            if (it.isSuccessful)
                Timber.tag("task-completion").i("successful")
            else Timber.tag("task-completion").e("failed -> ${it.exception}")

        }

        addOnFailureListener {
            Timber.tag("task-completion").e(it)
        }
    } catch (e: Exception) {
        Timber.tag("task-completion").e(e)
    }
}

/**
 * Document snapshots
 */
suspend inline fun <reified T> Task<DocumentSnapshot>.foldDoc(
    crossinline successBlock: suspend (T?) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) = withContext(Dispatchers.IO) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            launch { successBlock(snapshot.result?.toObject(T::class.java)) }
        } else {
            launch { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener { launch { errorBlock(it) } }
}