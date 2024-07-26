package com.groupf.togolist.Model

data class LocationItem(
    var id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val note: String = "",
    val list: String = "",
    var visited: Boolean = false
)
