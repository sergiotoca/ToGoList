package com.groupf.togolist.Model

data class PlaceListItem(
    val id: String = "",
    val name: String = "",
    val placeCount: Int = 0,
    var places: Map<String, Boolean> = emptyMap()
)
