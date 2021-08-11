package io.helpdesk.core.util

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
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

suspend inline fun <reified T> CollectionReference.observeCollection(
    scope: CoroutineScope,
    crossinline successBlock: suspend (MutableList<T>) -> Unit,
    crossinline errorBlock: suspend (Exception?) -> Unit
) {
    addSnapshotListener { snapshot, error ->
        if (error != null) {
            scope.launch { errorBlock(error) }
            return@addSnapshotListener
        }

        if (snapshot != null) {
            scope.launch {
                successBlock(snapshot.toObjects(T::class.java) ?: mutableListOf())
            }
        } else {
            Timber.tag("task-completion").i("failed")
            scope.launch { errorBlock(Exception("no snapshots found")) }
        }
    }
}

fun Task<Void>.await(scope: CoroutineScope) = scope.launch {
    try {
        addOnCompleteListener {
            if (it.isSuccessful)
                Timber.tag("task-completion").i("successful")
            else Timber.tag("task-completion").e("failed -> ${it.exception?.localizedMessage}")

        }

        addOnFailureListener {
            Timber.tag("task-completion").e(it)
        }
    } catch (e: Exception) {
        Timber.tag("task-completion").e(e)
    }
}

@ExperimentalCoroutinesApi
suspend fun Task<AuthResult>.awaitAuthResult(scope: CoroutineScope): Flow<FirebaseUser?> =
    channelFlow {
        val logger = Timber.tag("auth-result-task-error")
        try {
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logger.i("user created")
                    offer(task.result?.user)
                } else {
                    logger.e("unable to create user: ${task.exception?.localizedMessage}")
                    offer(null)
                }
            }

            addOnFailureListener { exception ->
                logger.e(exception)
                offer(null)
            }
        } catch (e: Exception) {
            logger.d("failed to authenticate user: ${e.localizedMessage}")
            offer(null)
        }
        awaitClose()
    }.stateIn(scope)

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