package com.journeyjunctionxml
data class PostModel(
    val profilePic: Int,
    val profileName: String,
    val contentCaption: String,
    val contentImage: Int,
    val reactCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0

)
