package com.example.denikv1

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

    // Metoda pro přidání nebo aktualizaci cesty.
    suspend fun addOrUpdateCesta(cesta: CestaEntity)

    // Metoda pro získání všech cest na konkrétní datum.
    suspend fun getAllCestaForDate(selectedDate: Long): List<CestaEntity>

    // Metoda pro získání všech cest v určeném časovém rozmezí.
    suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity>

    // Metoda pro získání cesty podle ID.
    suspend fun getCestaById(cestaId: Long): CestaEntity

    // Metoda pro získání všech cest podle částečného názvu.
    suspend fun getAllCestaByName(roadName: String): List<CestaEntity>

    // Metoda pro export dat do souboru CSV.
    suspend fun exportDataToFile(context: Context, fileName: String)
}

// Implementace rozhraní CestaModel.
class CestaModelImpl(private val context: Context) : CestaModel {
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
    override suspend fun addOrUpdateCesta(cesta: CestaEntity) {
        if (cesta.id != 0L) {
            // Aktualizace existující cesty
            val existingCesta = getCestaById(cesta.id)
            existingCesta.apply {
                // Aktualizace hodnot
                roadName = cesta.roadName
                fallCount = cesta.fallCount
                climbStyle = cesta.climbStyle
                gradeNum = cesta.gradeNum
                gradeSign = cesta.gradeSign
                roadChar = cesta.roadChar
                timeMinute = cesta.timeMinute
                timeSecond = cesta.timeSecond
                description = cesta.description
                opinion = cesta.opinion
                date = cesta.date
            }
            removeCesta(existingCesta)
            insertCesta(existingCesta)
        } else {
            // Přidání nové cesty
            insertCesta(cesta)
        }
    }

    // Metoda pro export dat do souboru CSV.
    override suspend fun exportDataToFile(context: Context, fileName: String) {
        withContext(Dispatchers.IO) {
            val data = cestaDao.getAllCesta()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            try {
                val csvWriter = FileWriter(file, false)

                // Header
                csvWriter.append("Jméno cesty,Počet pádů,Styl přelezu,Obtížnost,Charakter cesty,Minuty,Sekundy,Popis cesty,Názor, Datum\n")

                // Data
                data.forEach { cesta ->
                    csvWriter.append("${cesta.roadName},${cesta.fallCount},${cesta.climbStyle},${cesta.gradeNum},${cesta.gradeSign},${cesta.roadChar},${cesta.timeMinute},${cesta.timeSecond},${cesta.description},${cesta.opinion},${formatDate(cesta.date)}\n")
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

    // Metoda pro získání všech cest podle částečného názvu.
    override suspend fun getAllCestaByName(partialName: String): List<CestaEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext cestaDao.getAllCesta().filter { cesta ->
                cesta.roadName.contains(partialName, ignoreCase = true)
            }
        }
    }
}
