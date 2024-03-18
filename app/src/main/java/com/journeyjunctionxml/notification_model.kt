package com.journeyjunctionxml

import java.util.Date

data class notification_model(
    val text: String,
    val uid: String,
    val name: String,
    val timestampField: Date
)
