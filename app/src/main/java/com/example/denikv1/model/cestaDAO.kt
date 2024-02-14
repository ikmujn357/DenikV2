package com.example.denikv1.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

// Rozhraní pro manipulaci s entitou CestaEntity v databázi.
// Obsahuje metody pro vkládání, získávání a mazání záznamů v
@Dao
interface CestaDao {
    // Metoda pro vložení nové cesty do databáze.
    @Insert
    suspend fun insertCesta(cesta: CestaEntity)

    @Update
    suspend fun updateCesta(cesta: CestaEntity)

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

    @Query("SELECT * FROM CestaEntity WHERE id = :routeId")
    suspend fun getCestaByRouteId(routeId: Long): CestaEntity

    // Metoda pro získání cest podle názvu cesty.
    @Query("SELECT * FROM CestaEntity WHERE routeName = :routeName")
    suspend fun getAllCestaByName(routeName: String): List<CestaEntity>

    @Query("DELETE FROM cestaentity")
    suspend fun deleteAllCesta()
    @Query("SELECT COUNT(*) FROM CestaEntity")
    suspend fun getCountOfItems(): Int

    @Query("SELECT * FROM CestaEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastCesta(): CestaEntity?

    @Query("SELECT id FROM CestaEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLastCestaId(): Long?


}
