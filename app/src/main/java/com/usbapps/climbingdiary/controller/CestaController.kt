package com.usbapps.climbingdiary.controller

import android.content.Context
import com.usbapps.climbingdiary.model.CestaEntity
import com.usbapps.climbingdiary.model.CestaModel

/**
 * Rozhraní pro kontrolér cest.
 * Definuje metody pro práci s cestami, jako je získání, export dat a další.
 */
interface CestaController {

    // Metoda pro získání všech cest
    suspend fun getAllCesta(): List<CestaEntity>

    // Metoda pro export dat do souboru
    suspend fun exportDataToFile(context: Context, fileName: String)

    // Metoda pro získání všech cest v daném datovém rozsahu
    suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity>

    // Metoda pro získání všech cest se zadaným názvem
    suspend fun getAllCestaByName(roadName: String): List<CestaEntity>

    // Metoda pro získání cesty podle ID
    suspend fun getCestaById(cestaId: Long): CestaEntity

    // Metoda pro získání cesty podle ID trasy
    suspend fun getCestaByRouteId(routeId: Long): CestaEntity

    // Metoda pro přidání cesty
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
        date: Long,
        latitude: Double,
        longitude: Double
    )

    // Metoda pro smazání všech cest
    suspend fun deleteAllCesta()
}

/**
 * Implementace rozhraní CestaController.
 * Tato třída provádí implementaci metod pro práci s cestami.
 *
 * @param cestaModel Model obsahující logiku pro manipulaci s daty cest.
 */
class CestaControllerImpl(private val cestaModel: CestaModel) : CestaController {

    /**
     * Získává seznam všech cest.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @return Seznam všech cest v databázi.
     */
    override suspend fun getAllCesta(): List<CestaEntity> {
        return cestaModel.getAllCesta()
    }

    /**
     * Exportuje data o cestách do souboru.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param context Kontext aplikace.
     * @param fileName Název souboru, do kterého se mají data exportovat.
     */
    override suspend fun exportDataToFile(context: Context, fileName: String) {
        cestaModel.exportDataToFile(context, fileName)
    }

    /**
     * Získává seznam všech cest v daném datovém rozsahu.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param startDate Počáteční datum datového rozsahu.
     * @param endDate Koncové datum datového rozsahu.
     * @return Seznam všech cest v daném datovém rozsahu.
     */
    override suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity> {
        return cestaModel.getAllCestaForDateRange(startDate, endDate)
    }

    /**
     * Získává seznam všech cest se zadaným názvem.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param roadName Název cesty.
     * @return Seznam všech cest se zadaným názvem.
     */
    override suspend fun getAllCestaByName(roadName: String): List<CestaEntity> {
        return cestaModel.getAllCestaByName(roadName)
    }

    /**
     * Získává cestu se zadaným identifikátorem.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param cestaId Identifikátor cesty.
     * @return Cesta se zadaným identifikátorem.
     */
    override suspend fun getCestaById(cestaId: Long): CestaEntity {
        return cestaModel.getCestaById(cestaId)
    }

    /**
     * Získává cestu podle identifikátoru trasy.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param routeId Identifikátor trasy.
     * @return Cesta přiřazená k danému identifikátoru trasy.
     */
    override suspend fun getCestaByRouteId(routeId: Long): CestaEntity {
        return cestaModel.getCestaByRouteId(routeId)
    }

    /**
     * Přidává novou cestu do databáze.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     *
     * @param routeName Název trasy.
     * @param fallCount Počet pádů.
     * @param climbStyle Styl lezení.
     * @param gradeNum Úroveň obtížnosti.
     * @param gradeSign Značka úrovně obtížnosti.
     * @param routeChar Charakter trasy.
     * @param timeMinute Čas (minuty).
     * @param timeSecond Čas (sekundy).
     * @param description Popis trasy.
     * @param rating Hodnocení trasy.
     * @param date Datum.
     * @param latitude Zeměpisná šířka.
     * @param longitude Zeměpisná délka.
     */
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
        date: Long,
        latitude: Double,
        longitude: Double
    ) {
        cestaModel.addCesta(
            routeName, fallCount, climbStyle, gradeNum, gradeSign,
            routeChar, timeMinute, timeSecond, description, rating, date, latitude, longitude
        )
    }

    /**
     * Smaže všechny cesty z databáze.
     * Tato metoda je označena jako suspend, což znamená, že je vhodná pro použití v suspendovaných funkcích,
     * jako jsou korutiny.
     */
    override suspend fun deleteAllCesta() {
        cestaModel.deleteAllCesta()
    }
}
