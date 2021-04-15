package io.helpdesk.core.util

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.*
import timber.log.Timber


/**
 * Query snapshots
 */
suspend inline fun <reified T> Task<QuerySnapshot>.fold(
    scope: CoroutineScope,
    crossinline successBlock: suspend (MutableList<T>) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            Timber.tag("task-completion").i("successful -> ${snapshot.result?.documents}")
            scope.launch {
                successBlock(snapshot.result?.toObjects(T::class.java) ?: mutableListOf())
            }
        } else {
            Timber.tag("task-completion").i("failed")
            scope.launch { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener { scope.launch { errorBlock(it) } }
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

suspend inline fun Task<AuthResult>.awaitAuthResult(scope: CoroutineScope): FirebaseUser? {
    val logger = Timber.tag("auth-result-task")
    return try {
        Tasks.await(this@awaitAuthResult).user
    } catch (e: Exception) {
        logger.d("failed to authenticate user: ${e.localizedMessage}")
        null
    }
}

/**
 * Document snapshots
 */
inline fun <reified T> Task<DocumentSnapshot>.foldDoc(
    scope: CoroutineScope,
    crossinline successBlock: (T?) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) {

    addOnCompleteListener { snapshot ->
        if (snapshot.isSuccessful) {
            Timber.tag("fold-doc").i("result -> ${snapshot.result?.data}")
//            scope.launch {  }
            successBlock(snapshot.result?.toObject(T::class.java))
        } else {
            Timber.tag("fold-doc").e("error -> ${snapshot.exception?.localizedMessage}")
            scope.launch { errorBlock(snapshot.exception) }
        }
    }

    // error branch
    addOnFailureListener {
        Timber.tag("fold-doc").i("failure error -> ${it.localizedMessage}")
        scope.launch { errorBlock(it) }
    }
}

suspend inline fun <reified T> DocumentReference.observe(
    scope: CoroutineScope,
    crossinline block: suspend (T?, Exception?) -> Unit
) {
    addSnapshotListener { value, error ->
        if (error != null) {
            scope.launch { block(null, error) }
            return@addSnapshotListener
        }

        if (value != null) {
            scope.launch {
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