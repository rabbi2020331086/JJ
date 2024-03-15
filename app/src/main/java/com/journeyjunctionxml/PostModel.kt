package com.journeyjunctionxml
import android.net.Uri

data class PostModel(
    val profileName: String,
    val contentCaption: String,
    val contentImage: String,
    val reactCount: String,
    val uid: String,
    val pid: String
)
