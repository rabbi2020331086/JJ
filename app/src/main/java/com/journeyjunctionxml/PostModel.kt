package com.journeyjunctionxml
import android.net.Uri

data class PostModel(
    val profilePic: String,
    val profileName: String,
    val contentCaption: String,
    val contentImage: Uri,
    val reactCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0
)
