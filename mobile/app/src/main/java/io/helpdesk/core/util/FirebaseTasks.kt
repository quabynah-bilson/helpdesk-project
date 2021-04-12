package io.helpdesk.core.util

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

fun globalScope(block: suspend () -> Unit) = GlobalScope.launch { block() }

/**
 * Query snapshots
 */
suspend inline fun <reified T> Task<QuerySnapshot>.fold(
    crossinline successBlock: suspend (MutableList<T>) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            Timber.tag("task-completion").i("successful -> ${snapshot.result?.documents}")
            globalScope {
                successBlock(snapshot.result?.toObjects(T::class.java) ?: mutableListOf())
            }
        } else {
            Timber.tag("task-completion").i("failed")
            globalScope { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener { globalScope { errorBlock(it) } }
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
) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            globalScope { successBlock(snapshot.result?.toObject(T::class.java)) }
        } else {
            globalScope { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener { globalScope { errorBlock(it) } }
}

suspend inline fun <reified T> DocumentReference.observe(
    crossinline block: suspend (T?, Exception?) -> Unit
) {
    addSnapshotListener { value, error ->
        if (error != null) {
            globalScope { block(null, error) }
            return@addSnapshotListener
        }

        if (value != null) {
            globalScope {
                if (value.exists()) {
                    val data = value.toObject(T::class.java)
                    block(data, null)
                } else {
                    block(null, Exception("no data exists"))
                }
            }
        }
    }
}