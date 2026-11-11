package com.anubhav.devtools.model.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tool constructor(
    val name: String,
    val description: String,
    val imageRes: Int = 0
) : Parcelable {

    fun title(): String {
        return name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}

enum class ToolNames(val description: String, val imageRes: Int) {
    KOTLIN_PLAYGROUND("Edit, Run, Share Kotlin Code Online", 0),
    VIEW_CONTACTS("View device contacts", 0),
    EXTRA("", 0),
}