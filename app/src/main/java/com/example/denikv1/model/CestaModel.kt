package com.example.denikv1.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Rozhraní pro datový model.
interface CestaModel {
    // Metoda pro získání všech cest.
    suspend fun getAllCesta(): List<CestaEntity>

    // Metoda pro odstranění cesty. NEPOUŽÍVÁ SE ZATÍM
    suspend fun removeCesta(cesta: CestaEntity)

    // Metoda pro vložení cesty.
    suspend fun insertCesta(cesta: CestaEntity)

    // Metoda pro získání všech cest na konkrétní datum.
    suspend fun getAllCestaForDate(selectedDate: Long): List<CestaEntity>

    // Metoda pro získání všech cest v určeném časovém rozmezí.
    suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity>

    // Metoda pro získání cesty podle ID.
    suspend fun getCestaById(cestaId: Long): CestaEntity

    suspend fun getLastCesta(): CestaEntity?

    suspend fun getLastCestaId(): Long?

    suspend fun getCestaByRouteId(routeId: Long): CestaEntity

    suspend fun getCountOfItems(): Int

    // Metoda pro získání všech cest podle částečného názvu.
    suspend fun getAllCestaByName(routeName: String): List<CestaEntity>

    // Metoda pro export dat do souboru CSV.
    suspend fun exportDataToFile(context: Context, fileName: String)
    suspend fun updateCesta(cesta: CestaEntity)
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

    suspend fun deleteAllCesta()
}

// Implementace rozhraní CestaModel.
class CestaModelImpl(context: Context) : CestaModel {
    // Získání přístupu k databázi.
    private val cestaDao = AppDatabase.getDatabase(context).cestaDao()

    // Metoda pro získání všech cest.
    override suspend fun getAllCesta(): List<CestaEntity> = withContext(Dispatchers.IO) {
        return@withContext cestaDao.getAllCesta()
    }

    // Metoda pro odstranění cesty.
    override suspend fun removeCesta(cesta: CestaEntity) {
        cestaDao.deleteCesta(cesta)
    }

    // Metoda pro vložení cesty.
    override suspend fun insertCesta(cesta: CestaEntity) = withContext(Dispatchers.IO) {
        cestaDao.insertCesta(cesta)
    }

    // Metoda pro přidání nebo aktualizaci cesty.
    @SuppressLint("SuspiciousIndentation")
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
        val newCesta = CestaEntity(
            routeName = routeName,
            fallCount= fallCount,
            climbStyle = climbStyle,
            gradeNum = gradeNum,
            gradeSign = gradeSign,
            routeChar = routeChar,
            timeMinute = timeMinute,
            timeSecond = timeSecond,
            description = description,
            rating = rating,
            date = date
        )
            insertCesta(newCesta)
    }

    override suspend fun updateCesta(cesta: CestaEntity) {
        return cestaDao.updateCesta(cesta)
    }

    override suspend fun getCountOfItems(): Int{
        return cestaDao.getCountOfItems()
    }

    override suspend fun getLastCesta(): CestaEntity?{
        return cestaDao.getLastCesta()
    }

    override suspend fun getLastCestaId(): Long?{
    return cestaDao.getLastCestaId()
    }

    // Metoda pro export dat do souboru CSV.
    override suspend fun exportDataToFile(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            val data = cestaDao.getAllCesta()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            try {
                val csvWriter = FileWriter(file, false)

                // Header
                csvWriter.append("Jméno cesty,Počet pádů,Styl přelezu,Obtížnost,Obtížnost znak,Charakter cesty,Minuty,Sekundy,Popis cesty,Hodnocení, Datum\n")

                // Data
                data.forEach { cesta ->
                    csvWriter.append("${cesta.routeName},${cesta.fallCount},${cesta.climbStyle},${cesta.gradeNum},${cesta.gradeSign},${cesta.routeChar},${cesta.timeMinute},${cesta.timeSecond},${cesta.description},${cesta.rating},${formatDate(cesta.date)}\n")
                }

                csvWriter.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Pomocná metoda pro formátování data do textové podoby (datum zapsaný pro čechy)
    private fun formatDate(date: Long): String {
        val pattern = "dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(Date(date))
    }

    // Metoda pro získání všech cest na konkrétní datum.
    override suspend fun getAllCestaForDate(selectedDate: Long): List<CestaEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext cestaDao.getAllCestaForDate(selectedDate)
        }
    }

    // Metoda pro získání všech cest v určeném časovém rozmezí.
    override suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext cestaDao.getAllCestaForDateRange(startDate, endDate)
        }
    }

    // Metoda pro získání cesty podle ID.
    override suspend fun getCestaById(cestaId: Long): CestaEntity {
        return cestaDao.getCestaById(cestaId)
    }

    override suspend fun getCestaByRouteId(routeId: Long): CestaEntity {
        return cestaDao.getCestaByRouteId(routeId)
    }

    // Metoda pro získání všech cest podle částečného názvu.
    override suspend fun getAllCestaByName(routeName: String): List<CestaEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext cestaDao.getAllCesta().filter { cesta ->
                cesta.routeName.contains(routeName, ignoreCase = true)
            }
        }
    }

    override suspend fun deleteAllCesta () {
        return cestaDao.deleteAllCesta()
    }
}
