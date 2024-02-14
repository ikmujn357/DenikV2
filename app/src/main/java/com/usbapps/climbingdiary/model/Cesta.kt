package com.usbapps.climbingdiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CestaEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var routeName: String,
    var fallCount: Int,
    var climbStyle: String,
    var gradeNum: String,
    var routeChar: String,
    var timeMinute: Int,
    var timeSecond: Int,
    var description: String,
    var rating: Float,
    var date: Long,
    var latitude: Double,
    var longitude: Double
) {
    constructor(
        routeName: String,
        fallCount: Int,
        climbStyle: String,
        gradeNum: String,
        routeChar: String,
        timeMinute: Int,
        timeSecond: Int,
        description: String,
        rating: Float,
        date: Long,
        latitude: Double,
        longitude: Double
    ) : this(
        0, routeName, fallCount, climbStyle, gradeNum, routeChar, timeMinute, timeSecond, description, rating, date, latitude, longitude
    )
}
