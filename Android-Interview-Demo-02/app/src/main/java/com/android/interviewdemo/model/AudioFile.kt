package com.android.interviewdemo.model

import android.content.Context
import java.io.File

data class AudioFile(
    var context: Context,
    var name: String
) {

    fun getPath() = context.externalCacheDir?.absolutePath + "/" + name + ".3gp"

    fun exists() = File(getPath()).exists()

}