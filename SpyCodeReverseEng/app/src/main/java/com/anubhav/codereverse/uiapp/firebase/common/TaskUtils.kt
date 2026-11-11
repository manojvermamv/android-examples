package com.anubhav.codereverse.uiapp.firebase.common

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import com.anubhav.codereverse.uiapp.common.Result

fun <T> task(block: (TaskCompletionSource<T>) -> Unit): Task<T> {
    val taskSource = TaskCompletionSource<T>()
    block(taskSource)
    return taskSource.task
}

fun Task<*>.toUnit(): Task<Unit> =
    onSuccessTask { Tasks.forResult(Unit) }

fun <T> Task<*>.addOnFailure(source: TaskCompletionSource<Result<T>>? = null) =
    addOnFailureListener {
        Log.e("addOnFailure", it.toString())
        source?.setResult(Result.Error(exception = it, msg = it.message))
    }

fun <T> Task<*>.addOnFailure(b: ((Result<T>) -> Unit)) =
    addOnFailureListener {
        Log.e("addOnFailure", it.toString())
        b.invoke(Result.Error(exception = it, msg = it.message))
    }

fun Task<*>.addOnFailure() =
    addOnFailureListener { Log.e("addOnFailure", it.toString()) }

fun <T> Gson.jsonToObjectOrNull(json: String?, clazz: Class<T>): T? =
    try {
        fromJson(json, clazz)
    } catch (ignored: Exception) {
        null
    }