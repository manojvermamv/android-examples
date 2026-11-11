package com.anubhav.codereverse.uiapp.data.model

import com.anubhav.codereverse.R
import com.google.firebase.database.Exclude

data class PhotoAlbumModel constructor(
    val imageUrl: String = "",
    val isAddImage: Boolean = false
) {

    @Exclude
    @set:Exclude
    @get:Exclude
    var imageRes: Int = R.drawable.img_load_placeholder

}