package com.example.denikv1.controller

import android.content.Context
import com.example.denikv1.model.CestaEntity
import com.example.denikv1.model.CestaModel

// Rozhraní
interface CestaController {


    // Metoda pro získání všech cest
    suspend fun getAllCesta(): List<CestaEntity>

    // Metoda pro export dat do souboru
    suspend fun exportDataToFile(context: Context, fileName: String)

    // Metoda pro získání všech cest v daném datovém rozsahu
    suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity>

    // Metoda pro získání všech cest se zadaným názvem
    suspend fun getAllCestaByName(roadName: String): List<CestaEntity>

    // Metoda pro zobrazení Toast zprávy
    fun showToast(message: String, duration: Int)
    suspend fun getCestaById(cestaId: Long): CestaEntity

    suspend fun getCestaByRouteId(routeId: Long): CestaEntity
    suspend fun addCesta(
        routeName: String,
        fallCount: Int,
        climbStyle: String,
        gradeNum: String,
        gradeSign: String,
        routeChar: String,
        timeMinute: Int,
        timeSecond: Int,
        description: String,
        rating: Float,
        date: Long
    )
}

// Implementace rozhraní CestaController
class CestaControllerImpl(private val cestaModel: CestaModel) : CestaController {

    // Metoda pro získání všech cest
    override suspend fun getAllCesta(): List<CestaEntity> {
        return cestaModel.getAllCesta()
    }

    // Metoda pro export dat do souboru
    override suspend fun exportDataToFile(context: Context, fileName: String) {
        cestaModel.exportDataToFile(context, fileName)
    }

    // Metoda pro získání všech cest v daném datovém rozsahu
    override suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity> {
        return cestaModel.getAllCestaForDateRange(startDate, endDate)
    }

    // Metoda pro získání všech cest se zadaným názvem
    override suspend fun getAllCestaByName(roadName: String): List<CestaEntity> {
        return cestaModel.getAllCestaByName(roadName)
    }

    override suspend fun getCestaById(cestaId: Long): CestaEntity {
        return cestaModel.getCestaById(cestaId)
    }

    override suspend fun getCestaByRouteId(routeId: Long): CestaEntity {
        return cestaModel.getCestaByRouteId(routeId)
    }

    override suspend fun addCesta(
        routeName: String,
        fallCount: Int,
        climbStyle: String,
        gradeNum: String,
        gradeSign: String,
        routeChar: String,
        timeMinute: Int,
        timeSecond: Int,
        description: String,
        rating: Float,
        date: Long
    ) {

        cestaModel.addCesta(
            routeName, fallCount, climbStyle, gradeNum, gradeSign,
            routeChar, timeMinute, timeSecond, description, rating, date)
    }

    // Metoda pro zobrazení Toast zprávy
    override fun showToast(message: String, duration: Int) {

    }


}
