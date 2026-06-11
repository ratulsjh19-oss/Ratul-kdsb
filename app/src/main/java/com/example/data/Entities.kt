package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hunter_stats")
data class HunterStats(
    @PrimaryKey val id: Int = 1,
    val name: String = "Jin-Woo",
    val title: String = "The Weakest Hunter",
    val rank: String = "E-Rank",
    val level: Int = 1,
    val experience: Int = 0,
    val gold: Int = 500,
    val crystals: Int = 10,
    val statPoints: Int = 10,
    
    // Core Attributes
    val strength: Int = 10,
    val agility: Int = 10,
    val vitality: Int = 10,
    val intelligence: Int = 10,
    val sense: Int = 10,
    
    // Selected / Equipped Weapon
    val equippedWeaponId: String = "default_bare_hands",
    
    // Daily System Quest Progress (Solo Leveling Workout)
    val dailyPushUps: Int = 0,
    val dailySitUps: Int = 0,
    val dailySquats: Int = 0,
    val dailyRunMeters: Int = 0, // In meters (or reps equivalent)
    
    val pushUpsCompleted: Boolean = false,
    val sitUpsCompleted: Boolean = false,
    val squatsCompleted: Boolean = false,
    val runCompleted: Boolean = false,
    
    val lastQuestCompletedTime: Long = 0L,
    val dailyPenaltiesTriggered: Int = 0
)

@Entity(tableName = "shadows")
data class ShadowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // Knight, General, Commander, Monarch
    val level: Int = 1,
    val experience: Int = 0,
    val baseDamage: Int = 30,
    val isSummoned: Boolean = false,
    val dateExtracted: Long = System.currentTimeMillis()
)

@Entity(tableName = "weapons")
data class WeaponEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rank: String, // E to S Rank
    val attackBonus: Int,
    val effectDescription: String,
    val goldCost: Int,
    val crystalCost: Int,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false
)
