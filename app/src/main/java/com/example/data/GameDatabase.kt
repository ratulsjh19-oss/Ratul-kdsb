package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HunterDao {
    @Query("SELECT * FROM hunter_stats WHERE id = 1 LIMIT 1")
    fun getHunterStatsFlow(): Flow<HunterStats?>

    @Query("SELECT * FROM hunter_stats WHERE id = 1 LIMIT 1")
    suspend fun getHunterStats(): HunterStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHunterStats(stats: HunterStats)

    @Update
    suspend fun updateHunterStats(stats: HunterStats)
}

@Dao
interface ShadowDao {
    @Query("SELECT * FROM shadows ORDER BY dateExtracted DESC")
    fun getAllShadowsFlow(): Flow<List<ShadowEntity>>

    @Query("SELECT * FROM shadows WHERE isSummoned = 1")
    fun getSummonedShadowsFlow(): Flow<List<ShadowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShadow(shadow: ShadowEntity)

    @Update
    suspend fun updateShadow(shadow: ShadowEntity)

    @Query("DELETE FROM shadows WHERE id = :id")
    suspend fun deleteShadow(id: String)
}

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons ORDER BY goldCost ASC")
    fun getAllWeaponsFlow(): Flow<List<WeaponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeapon(weapon: WeaponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun prePopulateWeapons(weapons: List<WeaponEntity>)

    @Update
    suspend fun updateWeapon(weapon: WeaponEntity)

    @Query("UPDATE weapons SET isEquipped = 0")
    suspend fun unequipAllWeapons()

    @Query("UPDATE weapons SET isEquipped = 1 WHERE id = :weaponId")
    suspend fun equipWeapon(weaponId: String)
}

@Database(entities = [HunterStats::class, ShadowEntity::class, WeaponEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hunterDao(): HunterDao
    abstract fun shadowDao(): ShadowDao
    abstract fun weaponDao(): WeaponDao
}
