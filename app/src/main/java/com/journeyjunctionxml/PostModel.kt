package com.journeyjunctionxml
import android.net.Uri
import java.util.Date

data class PostModel(
    val profileName: String,
    val contentCaption: String,
    val contentImage: String,
    val reactCount: String,
    val uid: String,
    val pid: String,
    val time: Date
)
