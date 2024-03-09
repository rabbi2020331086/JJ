package com.journeyjunctionxml

data class TripsModel(
    val picture: String = "",
    val title: String = "",
    val duration: String = "",
    val date: String = "",
    val places: String = "",
    val budget: String = "",
    val check_in: String = "",
    val vacancy: String = "",
    val owner: String = "",
    val gender: String = "",
    val uid: String = ""
) {
    // No-argument constructor
    constructor() : this("", "", "", "", "", "", "", "", "", "", "")
}
