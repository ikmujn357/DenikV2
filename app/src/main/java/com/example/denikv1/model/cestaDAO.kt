package com.example.denikv1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// Rozhraní pro manipulaci s entitou CestaEntity v databázi.
// Obsahuje metody pro vkládání, získávání a mazání záznamů v
@Dao
interface CestaDao {
    // Metoda pro vložení nové cesty do databáze.
    @Insert
    suspend fun insertCesta(cesta: CestaEntity)

    // Metoda pro získání všech cest v databázi.
    @Query("SELECT * FROM CestaEntity")
    suspend fun getAllCesta(): List<CestaEntity>

    // Metoda pro smazání cesty z databáze, NEPOUŽÍVÁ SE ZATÍM
    @Delete
    suspend fun deleteCesta(cesta: CestaEntity)

    // Metoda pro získání všech cest v určitý den.
    @Query("SELECT * FROM CestaEntity WHERE date = :selectedDate")
    suspend fun getAllCestaForDate(selectedDate: Long): List<CestaEntity>

    // Metoda pro získání všech cest v určeném časovém rozmezí.
    @Query("SELECT * FROM CestaEntity WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAllCestaForDateRange(startDate: Long, endDate: Long): List<CestaEntity>

    // Metoda pro získání cesty podle ID.
    @Query("SELECT * FROM CestaEntity WHERE id = :cestaId")
    suspend fun getCestaById(cestaId: Long): CestaEntity

    // Metoda pro získání cest podle názvu cesty.
    @Query("SELECT * FROM CestaEntity WHERE roadName = :roadName")
    suspend fun getAllCestaByName(roadName: String): List<CestaEntity>
}
