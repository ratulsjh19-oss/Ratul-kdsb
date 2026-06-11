package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val context: Context) {
    
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "shadow_monarch_rpg.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val hunterDao = database.hunterDao()
    private val shadowDao = database.shadowDao()
    private val weaponDao = database.weaponDao()

    val hunterStats: Flow<HunterStats?> = hunterDao.getHunterStatsFlow()
    val allShadows: Flow<List<ShadowEntity>> = shadowDao.getAllShadowsFlow()
    val summonedShadows: Flow<List<ShadowEntity>> = shadowDao.getSummonedShadowsFlow()
    val allWeapons: Flow<List<WeaponEntity>> = weaponDao.getAllWeaponsFlow()

    suspend fun getHunterStatsDirect(): HunterStats? = hunterDao.getHunterStats()

    suspend fun updateHunter(stats: HunterStats) {
        hunterDao.updateHunterStats(stats)
    }

    suspend fun addShadow(shadow: ShadowEntity) {
        shadowDao.insertShadow(shadow)
    }

    suspend fun updateShadow(shadow: ShadowEntity) {
        shadowDao.updateShadow(shadow)
    }

    suspend fun releaseShadow(id: String) {
        shadowDao.deleteShadow(id)
    }

    suspend fun updateWeapon(weapon: WeaponEntity) {
        weaponDao.updateWeapon(weapon)
    }

    suspend fun equipWeapon(weaponId: String) {
        weaponDao.unequipAllWeapons()
        weaponDao.equipWeapon(weaponId)
    }

    // Initialize values if they are empty
    suspend fun initializeDatabase() {
        val stats = hunterDao.getHunterStats()
        if (stats == null) {
            hunterDao.insertHunterStats(HunterStats())
        }

        val weapons = weaponDao.getAllWeaponsFlow().firstOrNull() ?: emptyList()
        if (weapons.isEmpty()) {
            val defaultWeapons = listOf(
                WeaponEntity(
                    id = "default_bare_hands",
                    name = "Bare Fists",
                    rank = "E-Rank",
                    attackBonus = 0,
                    effectDescription = "Your starting fists. Standard E-Rank strength, dependent entirely on body conditioning.",
                    goldCost = 0,
                    crystalCost = 0,
                    isOwned = true,
                    isEquipped = true
                ),
                WeaponEntity(
                    id = "kasaka_dagger",
                    name = "Kasaka's Venom Fang",
                    rank = "C-Rank",
                    attackBonus = 25,
                    effectDescription = "Carved from the venom fang of the blue serpent. 20% Poison Bleed chance on slash attacks.",
                    goldCost = 1200,
                    crystalCost = 0,
                    isOwned = false,
                    isEquipped = false
                ),
                WeaponEntity(
                    id = "knight_killer",
                    name = "Knight Killer",
                    rank = "B-Rank",
                    attackBonus = 48,
                    effectDescription = "A grooved weapon forged by an expert blacksmith. 1.5x damage when striking armored bosses.",
                    goldCost = 4500,
                    crystalCost = 10,
                    isOwned = false,
                    isEquipped = false
                ),
                WeaponEntity(
                    id = "demon_king_dagger",
                    name = "Demon King's Daggers",
                    rank = "S-Rank",
                    attackBonus = 135,
                    effectDescription = "Twin blades holding the lightning elements of Baran. Strikes discharge static spark chain damage.",
                    goldCost = 18000,
                    crystalCost = 45,
                    isOwned = false,
                    isEquipped = false
                ),
                WeaponEntity(
                    id = "kamish_wrath",
                    name = "Kamish's Wrath",
                    rank = "S-Rank",
                    attackBonus = 380,
                    effectDescription = "Undisputed dragon relic dagger. Consuming pure dragon aura to double critical hit chances.",
                    goldCost = 65000,
                    crystalCost = 150,
                    isOwned = false,
                    isEquipped = false
                )
            )
            weaponDao.prePopulateWeapons(defaultWeapons)
        }
        
        // Populate standard initial shadows if they don't exist
        val initialShadows = shadowDao.getAllShadowsFlow().firstOrNull() ?: emptyList()
        if (initialShadows.isEmpty()) {
            // Note: We don't start with owned shadows, they are unlocked through active boss gates!
            // But let's create 1 standard shadow so the user gets a preview, or let them extract everything!
            // Let's let them extract everything on defeating bosses. That is way more rewarding!
        }
    }
}
