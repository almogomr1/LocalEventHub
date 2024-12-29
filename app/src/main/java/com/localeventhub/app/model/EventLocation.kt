package com.localeventhub.app.model

import androidx.room.ColumnInfo
import java.io.Serializable

data class EventLocation(
    val latitude: Double,                   // Latitude of the event
    val longitude: Double,                  // Longitude of the event
    @ColumnInfo(name = "address") val address: String? // Optional address
): Serializable{
    constructor():this(0.0,0.0,"")
}
