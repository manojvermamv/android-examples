package com.anubhav.codereverse.uiapp.common


sealed class Result<out R> {
    data class Success<out T>(val data: T? = null, val msg: String? = null) : Result<T>()
    class Error(val msg: String? = null, val exception: Exception? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}