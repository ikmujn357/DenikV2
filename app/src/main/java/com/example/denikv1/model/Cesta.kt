package com.example.denikv1

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entita reprezentující informace o lezené cestě
@Entity
data class CestaEntity(
    // Primární klíč s automatickým generováním
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    // Název cesty
    var roadName: String,

    // Počet pádů
    var fallCount: Int,

    // Styl lezení
    var climbStyle: String,

    // Obtížnost cesty - číslo a znaménko oddělené
    var gradeNum: String,

    // Znaménko obtížnosti cesty
    var gradeSign: String,

    // Charakter cesty
    var roadChar: String,

    // Doba lezení v minutách
    var timeMinute: Int,

    // Doba lezení v sekundách
    var timeSecond: Int,

    // Popis cesty
    var description: String,

    // Názor na cestu
    var opinion: String,

    // Datum vylezení
    var date: Long
) {
    // konstruktor pro vytváření instance bez ID.
    constructor(
        roadName: String,
        fallCount: Int,
        climbStyle: String,
        gradeNum: String,
        gradeSign: String,
        roadChar: String,
        timeMinute: Int,
        timeSecond: Int,
        description: String,
        opinion: String,
        date: Long
    ) : this(
        0, roadName, fallCount, climbStyle, gradeNum, gradeSign, roadChar, timeMinute, timeSecond, description, opinion, date
    )
}
