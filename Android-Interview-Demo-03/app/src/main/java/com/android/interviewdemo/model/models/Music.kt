package com.android.interviewdemo.model.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Music constructor(
    val id: Long,
    val name: String,
    val duration: String,
    val artist: String,
    val coverUri: Uri? = null
) : Parcelable

data class MusicAction(
    val index: Int = 0,
    val music: Music? = null
)