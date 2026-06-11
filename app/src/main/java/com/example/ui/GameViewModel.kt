package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.HunterStats
import com.example.data.ShadowEntity
import com.example.data.WeaponEntity
import com.example.util.GeminiClient
import com.example.util.SoundSynth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ActiveUiTab {
    object Status : ActiveUiTab
    object Workout : ActiveUiTab
    object Gates : ActiveUiTab
    object Shadows : ActiveUiTab
    object Armory : ActiveUiTab
    object MonarchCore : ActiveUiTab
}

data class DungeonGate(
    val id: String,
    val name: String,
    val bossName: String,
    val rank: String, // E, D, C, B, A, S
    val recommendedLevel: Int,
    val bossHp: Int,
    val bossAtk: Int,
    val goldReward: Int,
    val crystalReward: Int,
    val expReward: Int,
    val shadowExtractId: String?,
    val shadowExtractName: String?
)

data class DamageText(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val colorHex: Long, // e.g. 0xFF00E5FF for cyan, 0xFFFF1744 for red, 0xFFFFD600 for crit gold
    val offsetX: Float,
    val offsetY: Float
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)

    // Room Database Observables
    val hunterStats = repository.hunterStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val shadows = repository.allShadows.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val weapons = repository.allWeapons.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val summonedShadows = repository.summonedShadows.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current screen layout state
    private val _currentTab = MutableStateFlow<ActiveUiTab>(ActiveUiTab.Status)
    val currentTab: StateFlow<ActiveUiTab> = _currentTab.asStateFlow()

    // --- Combat / Dungeon state ---
    private val _activeDungeon = MutableStateFlow<DungeonGate?>(null)
    val activeDungeon: StateFlow<DungeonGate?> = _activeDungeon.asStateFlow()

    private val _isFighting = MutableStateFlow(false)
    val isFighting: StateFlow<Boolean> = _isFighting.asStateFlow()

    private val _isPenaltyMode = MutableStateFlow(false)
    val isPenaltyMode: StateFlow<Boolean> = _isPenaltyMode.asStateFlow()

    // Fighter Live values
    val hunterMaxHp = MutableStateFlow(100f)
    val hunterCurrentHp = MutableStateFlow(100f)
    val hunterMaxMp = MutableStateFlow(50f)
    val hunterCurrentMp = MutableStateFlow(50f)

    val monsterMaxHp = MutableStateFlow(100f)
    val monsterCurrentHp = MutableStateFlow(100f)

    private val _combatLogs = MutableStateFlow<List<String>>(emptyList())
    val combatLogs: StateFlow<List<String>> = _combatLogs.asStateFlow()

    private val _comboCount = MutableStateFlow(0)
    val comboCount: StateFlow<Int> = _comboCount.asStateFlow()

    private val _damageTexts = MutableStateFlow<List<DamageText>>(emptyList())
    val damageTexts: StateFlow<List<DamageText>> = _damageTexts.asStateFlow()

    // Extraction Phase State
    private val _isExtractionPhase = MutableStateFlow(false)
    val isExtractionPhase: StateFlow<Boolean> = _isExtractionPhase.asStateFlow()

    private val _extractionTargetName = MutableStateFlow("")
    val extractionTargetName: StateFlow<String> = _extractionTargetName.asStateFlow()

    private val _extractionTargetId = MutableStateFlow("")
    val extractionTargetId: StateFlow<String> = _extractionTargetId.asStateFlow()

    private val _extractionAttemptsLeft = MutableStateFlow(3)
    val extractionAttemptsLeft: StateFlow<Int> = _extractionAttemptsLeft.asStateFlow()

    private val _extractionStatusMessage = MutableStateFlow("Unleash your mana. Command the shadow to ARISE!")
    val extractionStatusMessage: StateFlow<String> = _extractionStatusMessage.asStateFlow()

    private val _extractionSuccess = MutableStateFlow(false)
    val extractionSuccess: StateFlow<Boolean> = _extractionSuccess.asStateFlow()

    // --- Gemini state ---
    private val _geminiResult = MutableStateFlow<String?>(null)
    val geminiResult: StateFlow<String?> = _geminiResult.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    // Master list of Solo Leveling-inspired Gate Dungeons
    val dungeonGates = listOf(
        DungeonGate(
            id = "gate_e_goblins",
            name = "Goblin Den Raid Dungeon",
            bossName = "Grogorr the Goblin Elder",
            rank = "E-Rank",
            recommendedLevel = 1,
            bossHp = 80,
            bossAtk = 8,
            goldReward = 200,
            crystalReward = 2,
            expReward = 50,
            shadowExtractId = "goblin_soldier",
            shadowExtractName = "Grogorr (Grave Guard)"
        ),
        DungeonGate(
            id = "gate_d_lich",
            name = "Desolate Graveyard Red Gate",
            bossName = "Kael'Thas the Bone Necrolich",
            rank = "D-Rank",
            recommendedLevel = 8,
            bossHp = 220,
            bossAtk = 18,
            goldReward = 600,
            crystalReward = 6,
            expReward = 150,
            shadowExtractId = "shadow_lich",
            shadowExtractName = "Bone Mage Shadow"
        ),
        DungeonGate(
            id = "gate_c_ice_elves",
            name = "Glacial Spires Frost Gate",
            bossName = "Frost Sovereign Snow Elf",
            rank = "C-Rank",
            recommendedLevel = 18,
            bossHp = 600,
            bossAtk = 42,
            goldReward = 1600,
            crystalReward = 15,
            expReward = 500,
            shadowExtractId = "shadow_ice_ranger",
            shadowExtractName = "Glacial Ranger Shadow"
        ),
        DungeonGate(
            id = "gate_b_igris",
            name = "Throne Room Knight Trial",
            bossName = "Blood-Red Commander Igris",
            rank = "B-Rank",
            recommendedLevel = 32,
            bossHp = 1800,
            bossAtk = 95,
            goldReward = 4500,
            crystalReward = 35,
            expReward = 1800,
            shadowExtractId = "igris",
            shadowExtractName = "Igris (Noble Crimson Commander)"
        ),
        DungeonGate(
            id = "gate_a_beru",
            name = "Jeju Island S-Rank Hive",
            bossName = "Unstoppable Insect King Beru",
            rank = "A-Rank",
            recommendedLevel = 50,
            bossHp = 5000,
            bossAtk = 260,
            goldReward = 14000,
            crystalReward = 90,
            expReward = 7500,
            shadowExtractId = "beru",
            shadowExtractName = "Beru (Vicious Ant King)"
        ),
        DungeonGate(
            id = "gate_s_baran",
            name = "Demon Castle Sovereign Pinnacle",
            bossName = "Demon King Baran & Wyvern",
            rank = "S-Rank",
            recommendedLevel = 75,
            bossHp = 15000,
            bossAtk = 720,
            goldReward = 45000,
            crystalReward = 300,
            expReward = 28000,
            shadowExtractId = "baran_wyvern",
            shadowExtractName = "Kaisel (Sovereign Wyvern)"
        )
    )

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    fun selectTab(tab: ActiveUiTab) {
        viewModelScope.launch {
            _currentTab.value = tab
            playBeep()
        }
    }

    // --- Audio synthesis trigger shortcut helpers ---
    fun playSlash() {
        viewModelScope.launch { SoundSynth.playSlash() }
    }

    fun playBeep() {
        viewModelScope.launch { SoundSynth.playButtonClick() }
    }

    fun playSummon() {
        viewModelScope.launch { SoundSynth.playSummonShadow() }
    }

    fun playLevelUpSound() {
        viewModelScope.launch { SoundSynth.playLevelUp() }
    }

    fun playQuestCompleteSound() {
        viewModelScope.launch { SoundSynth.playQuestComplete() }
    }

    // --- Stats allocation ---
    fun allocateStat(attribute: String) {
        val stats = hunterStats.value ?: return
        if (stats.statPoints <= 0) return

        viewModelScope.launch {
            val updated = when (attribute.lowercase()) {
                "strength" -> stats.copy(strength = stats.strength + 1, statPoints = stats.statPoints - 1)
                "agility" -> stats.copy(agility = stats.agility + 1, statPoints = stats.statPoints - 1)
                "vitality" -> stats.copy(vitality = stats.vitality + 1, statPoints = stats.statPoints - 1)
                "intelligence" -> stats.copy(intelligence = stats.intelligence + 1, statPoints = stats.statPoints - 1)
                "sense" -> stats.copy(sense = stats.sense + 1, statPoints = stats.statPoints - 1)
                else -> stats
            }
            repository.updateHunter(updated)
            playBeep()
        }
    }

    // --- Gamified Daily Exercises Clicker ---
    fun incrementWorkout(type: String) {
        val stats = hunterStats.value ?: return
        viewModelScope.launch {
            var updated = stats
            when (type.lowercase()) {
                "pushup" -> {
                    if (stats.dailyPushUps < 100) {
                        val newCount = stats.dailyPushUps + 1
                        updated = stats.copy(
                            dailyPushUps = newCount,
                            pushUpsCompleted = newCount >= 100
                        )
                        spawnImpactText("+1 Push-up", 0xFF00E5FF)
                    }
                }
                "situp" -> {
                    if (stats.dailySitUps < 100) {
                        val newCount = stats.dailySitUps + 1
                        updated = stats.copy(
                            dailySitUps = newCount,
                            sitUpsCompleted = newCount >= 100
                        )
                        spawnImpactText("+1 Sit-up", 0xFF00E5FF)
                    }
                }
                "squat" -> {
                    if (stats.dailySquats < 100) {
                        val newCount = stats.dailySquats + 1
                        updated = stats.copy(
                            dailySquats = newCount,
                            squatsCompleted = newCount >= 100
                        )
                        spawnImpactText("+1 Squat", 0xFF00E5FF)
                    }
                }
            }
            repository.updateHunter(updated)
            playSlash() // provides cool rhythmic feedback
            
            // Check if user just reached 100
            val oldCompleted = when(type.lowercase()) {
                "pushup" -> stats.pushUpsCompleted
                "situp" -> stats.sitUpsCompleted
                "squat" -> stats.squatsCompleted
                else -> true
            }
            val newCompleted = when(type.lowercase()) {
                "pushup" -> updated.pushUpsCompleted
                "situp" -> updated.sitUpsCompleted
                "squat" -> updated.squatsCompleted
                else -> true
            }
            if (!oldCompleted && newCompleted) {
                playQuestCompleteSound()
                awardWorkoutStats(type)
            }
        }
    }

    fun incrementRunning(distance: Int) {
        val stats = hunterStats.value ?: return
        if (stats.dailyRunMeters >= 10000) return

        viewModelScope.launch {
            val nextMeters = (stats.dailyRunMeters + distance).coerceAtMost(10000)
            val completed = nextMeters >= 10000
            val updated = stats.copy(
                dailyRunMeters = nextMeters,
                runCompleted = completed
            )
            repository.updateHunter(updated)
            spawnImpactText("+$distance m Run", 0xFFB026FF)
            playSlash()

            if (!stats.runCompleted && completed) {
                playQuestCompleteSound()
                awardWorkoutStats("run")
            }
        }
    }

    private suspend fun awardWorkoutStats(type: String) {
        val stats = hunterStats.value ?: return
        val pointsToAward = 3
        val goldToAward = 150
        val crystalsToAward = 2

        val baseUpdated = stats.copy(
            statPoints = stats.statPoints + pointsToAward,
            gold = stats.gold + goldToAward,
            crystals = stats.crystals + crystalsToAward
        )
        repository.updateHunter(baseUpdated)
        
        val typeLabel = type.substring(0,1).uppercase() + type.substring(1)
        _combatLogs.value = listOf("DAILY ALERT: Completed $typeLabel training! Awarded +$pointsToAward Stat Points, $goldToAward Gold, $crystalsToAward Crystals!") + _combatLogs.value
    }

    // Force Trigger daily penalties if workout failed
    fun triggerWorkoutPenalty() {
        playBeep()
        _isPenaltyMode.value = true
        _isFighting.value = true
        
        hunterMaxHp.value = 150f
        hunterCurrentHp.value = 150f
        monsterMaxHp.value = 250f
        monsterCurrentHp.value = 250f
        
        _combatLogs.value = listOf(
            "CRITICAL WARNING: LEVEL OF FITNESS FAILURE DETECTED!",
            "You failed to finish your training tasks. You have been transported to the Sandstorm Dunes Penalty Gate.",
            "Survive the Ancient Dunes Terror Centipede or perish. Click furiously to slash!"
        )
    }

    fun escapePenaltyGate() {
        viewModelScope.launch {
            val stats = hunterStats.value ?: return@launch
            val updated = stats.copy(
                dailyPushUps = 0,
                dailySitUps = 0,
                dailySquats = 0,
                dailyRunMeters = 0,
                pushUpsCompleted = false,
                sitUpsCompleted = false,
                squatsCompleted = false,
                runCompleted = false,
                dailyPenaltiesTriggered = stats.dailyPenaltiesTriggered + 1
            )
            repository.updateHunter(updated)
            _isPenaltyMode.value = false
            _isFighting.value = false
            _combatLogs.value = emptyList()
            playQuestCompleteSound()
        }
    }

    // --- Armory / Weapon Actions ---
    fun buyWeapon(weapon: WeaponEntity) {
        val stats = hunterStats.value ?: return
        if (stats.gold < weapon.goldCost || stats.crystals < weapon.crystalCost) {
            viewModelScope.launch { spawnImpactText("Insufficient Currency!", 0xFFFF1744) }
            return
        }

        viewModelScope.launch {
            val updatedStats = stats.copy(
                gold = stats.gold - weapon.goldCost,
                crystals = stats.crystals - weapon.crystalCost
            )
            val updatedWeapon = weapon.copy(isOwned = true)
            repository.updateHunter(updatedStats)
            repository.updateWeapon(updatedWeapon)
            playBeep()
            playQuestCompleteSound()
        }
    }

    fun selectWeapon(weaponId: String) {
        viewModelScope.launch {
            repository.equipWeapon(weaponId)
            val stats = hunterStats.value ?: return@launch
            val updated = stats.copy(equippedWeaponId = weaponId)
            repository.updateHunter(updated)
            playSummon()
        }
    }

    // --- Shadows Army Upgrades ---
    fun toggleShadowSummon(shadow: ShadowEntity) {
        viewModelScope.launch {
            val isCurrentlySummoned = shadow.isSummoned
            val currentSummonCount = summonedShadows.value.size
            
            if (!isCurrentlySummoned && currentSummonCount >= 3) {
                spawnImpactText("Max 3 Shadows Summoned!", 0xFFFF1744)
                return@launch
            }

            val updated = shadow.copy(isSummoned = !isCurrentlySummoned)
            repository.updateShadow(updated)
            
            if (!isCurrentlySummoned) {
                playSummon()
            } else {
                playBeep()
            }
        }
    }

    fun upgradeShadow(shadow: ShadowEntity) {
        val stats = hunterStats.value ?: return
        val goldCost = shadow.level * 800
        val crystalCost = shadow.level + 1

        if (stats.gold < goldCost || stats.crystals < crystalCost) {
            viewModelScope.launch { spawnImpactText("Insufficient resources!", 0xFFFF1744) }
            return
        }

        viewModelScope.launch {
            val updatedStats = stats.copy(
                gold = stats.gold - goldCost,
                crystals = stats.crystals - crystalCost
            )
            val updatedShadow = shadow.copy(
                level = shadow.level + 1,
                baseDamage = shadow.baseDamage + 25
            )
            repository.updateHunter(updatedStats)
            repository.updateShadow(updatedShadow)
            playLevelUpSound()
        }
    }

    fun releaseShadowSoldier(id: String) {
        viewModelScope.launch {
            repository.releaseShadow(id)
            playBeep()
        }
    }

    // --- GATE DUNGEONS & ACTIVE COMBAT LOOP ---
    fun startDungeon(gate: DungeonGate) {
        _activeDungeon.value = gate
        _isFighting.value = true
        _isPenaltyMode.value = false
        _isExtractionPhase.value = false
        _extractionSuccess.value = false
        _comboCount.value = 0

        val stats = hunterStats.value ?: return
        
        // Setup Fighter Live attributes
        val equippedWeaponBonus = weapons.value.find { it.isEquipped }?.attackBonus ?: 0
        val hpCalc = 100f + (stats.vitality * 12)
        val mpCalc = 50f + (stats.intelligence * 4)

        hunterMaxHp.value = hpCalc
        hunterCurrentHp.value = hpCalc
        hunterMaxMp.value = mpCalc
        hunterCurrentMp.value = mpCalc

        monsterMaxHp.value = gate.bossHp.toFloat()
        monsterCurrentHp.value = gate.bossHp.toFloat()

        _combatLogs.value = listOf(
            "[GATE DETECTED: ${gate.rank}] Entered dynamic portal. Pre-combat resonance detected.",
            "Sensing Boss: ${gate.bossName} (${gate.bossHp} HP). Draw your blade!",
            "Tips: Normal strikes restore MP. Skills consume MP for burst massive combos!"
        )
        playSummon()
    }

    fun runFromCombat() {
        _isFighting.value = false
        _activeDungeon.value = null
        _combatLogs.value = emptyList()
        _comboCount.value = 0
        playBeep()
    }

    // Standard Weapon Strike Composable Click
    fun performHunterSlash() {
        if (!_isFighting.value) return

        val stats = hunterStats.value ?: return
        playSlash()

        // Calculate player attack calculations
        val currentWeapon = weapons.value.find { it.isEquipped }
        val rawBaseAtk = stats.strength * 1.5 + (currentWeapon?.attackBonus ?: 0)
        
        // Critical multipliers check
        val critStat = stats.sense * 0.5f + stats.agility * 0.25f
        val isCrit = (Math.random() * 100) < critStat.coerceAtMost(85f)
        val critMult = if (isCrit) 2.0f else 1.0f

        // Combo multipliers
        val multi = 1.0f + (_comboCount.value / 15f).coerceAtMost(0.5f)
        val damageDealt = (rawBaseAtk * critMult * multi).toInt()

        viewModelScope.launch {
            // Apply damage to monster
            val nextMonsterHp = (monsterCurrentHp.value - damageDealt).coerceAtLeast(0f)
            monsterCurrentHp.value = nextMonsterHp

            // Raise combo
            val nextCombo = _comboCount.value + 1
            _comboCount.value = nextCombo

            // Restore partial MP
            val restoredMp = (5 + stats.intelligence * 0.2f).toInt()
            hunterCurrentMp.value = (hunterCurrentMp.value + restoredMp).coerceAtMost(hunterMaxMp.value)

            // Spawn floating cyan-neon damage text
            spawnImpactText(
                text = if (isCrit) "CRIT $damageDealt!" else "$damageDealt",
                colorHex = if (isCrit) 0xFFFFD600 else 0xFF00E5FF
            )

            // Log entry
            val weaponName = currentWeapon?.name ?: "Fists"
            val logMessage = if (isCrit) {
                "⚡ CRITICAL STRIKE! Your $weaponName struck ${monsterName()} dealing $damageDealt damage!"
            } else {
                "🗡️ You slashed ${monsterName()} with $weaponName for $damageDealt damage."
            }
            addCombatLog(logMessage)

            // Assist damage from Shadow Squad (if any summoned)
            executeShadowSquadAttack()

            if (nextMonsterHp <= 0f) {
                handleVictory()
            } else {
                // Boss counters
                delay(120)
                executeMonsterCounterAttack()
            }
        }
    }

    fun performHunterSkill(skillName: String) {
        if (!_isFighting.value) return
        val stats = hunterStats.value ?: return

        val manaCost = if (skillName == "Mutilate") 30 else 15
        if (hunterCurrentMp.value < manaCost) {
            viewModelScope.launch { spawnImpactText("No MP!", 0xFFFF1744) }
            return
        }

        playSlash()
        viewModelScope.launch {
            hunterCurrentMp.value -= manaCost

            val currentWeapon = weapons.value.find { it.isEquipped }
            val rawBaseAtk = stats.strength * 1.5 + (currentWeapon?.attackBonus ?: 0)
            val skillMultiplier = if (skillName == "Mutilate") 3.5f else 1.9f
            
            val damageDealt = (rawBaseAtk * skillMultiplier).toInt()

            val nextMonsterHp = (monsterCurrentHp.value - damageDealt).coerceAtLeast(0f)
            monsterCurrentHp.value = nextMonsterHp

            spawnImpactText("🌀 $skillName $damageDealt!", 0xFFB026FF)
            addCombatLog("🔮 SKILL ACTIVATED: You executed [$skillName] consuming $manaCost Mana for $damageDealt damage!")

            _comboCount.value += 2

            // Assist from shadows
            executeShadowSquadAttack()

            if (nextMonsterHp <= 0f) {
                handleVictory()
            } else {
                delay(140)
                executeMonsterCounterAttack()
            }
        }
    }

    private suspend fun executeShadowSquadAttack() {
        val squad = summonedShadows.value
        if (squad.isEmpty()) return

        // 35% chance shadows deal active support strikes
        if (Math.random() < 0.35) {
            val shadowDamage = squad.sumOf { it.baseDamage + (it.level * 8) }
            val nextHP = (monsterCurrentHp.value - shadowDamage).coerceAtLeast(0f)
            monsterCurrentHp.value = nextHP
            
            spawnImpactText("👤 Shadows +$shadowDamage!", 0xFF9E00FF)
            addCombatLog("👤 SHADOW ARMY COHORT: ${squad.joinToString { it.name }} arose from the darkness doing +$shadowDamage supporting damage!")
            
            if (nextHP <= 0f) {
                handleVictory()
            }
        }
    }

    private suspend fun executeMonsterCounterAttack() {
        if (!_isFighting.value || monsterCurrentHp.value <= 0) return

        val stats = hunterStats.value ?: return
        val activeGate = _activeDungeon.value
        
        val bossBaseAtk = activeGate?.bossAtk ?: 15
        val baseDodgeChance = stats.agility * 0.4f
        val isDodged = (Math.random() * 100) < baseDodgeChance.coerceAtMost(60f)

        if (isDodged) {
            spawnImpactText("DODGED!", 0xFF00E5FF)
            addCombatLog("⚡ DODGE SUCCESS! Your agility allowed you to roll away from ${monsterName()}'s sweep!")
            _comboCount.value = (_comboCount.value + 1).coerceAtMost(50) // agility combat mastery reward
        } else {
            // Apply damage
            val reductionPercentage = (stats.vitality * 0.5f).coerceAtMost(50f)
            val damageTaken = (bossBaseAtk * (1.0f - reductionPercentage / 100f)).toInt().coerceAtLeast(2)
            
            val nextHunterHp = (hunterCurrentHp.value - damageTaken).coerceAtLeast(0f)
            hunterCurrentHp.value = nextHunterHp
            _comboCount.value = 0 // combo broken

            spawnImpactText("-$damageTaken HP", 0xFFFF1744)
            addCombatLog("💥 ${monsterName()} struck back heavily! You sustained $damageTaken damage.")

            if (nextHunterHp <= 0f) {
                handleDefeat()
            }
        }
    }

    private fun handleVictory() {
        viewModelScope.launch {
            if (_isPenaltyMode.value) {
                _combatLogs.value = listOf("PENALTY DEFEATED! Restoring dimension matrix...") + _combatLogs.value
                delay(1200)
                escapePenaltyGate()
                return@launch
            }

            val gate = _activeDungeon.value ?: return@launch
            val stats = hunterStats.value ?: return@launch

            playQuestCompleteSound()
            
            // Calculate gold + crystals multiplying based on Sense attribute
            val senseBonusMultiplier = 1.0f + (stats.sense * 0.015f)
            val finalGold = (gate.goldReward * senseBonusMultiplier).toInt()
            val finalCrystals = gate.crystalReward

            var expNext = stats.experience + gate.expReward
            var levelNext = stats.level
            var addedStatPoints = 0

            // Formula: next level requirements
            while (expNext >= nextLevelExp(levelNext)) {
                expNext -= nextLevelExp(levelNext)
                levelNext += 1
                addedStatPoints += 5
            }

            val rankNext = when {
                levelNext >= 75 -> "S-Rank"
                levelNext >= 50 -> "A-Rank"
                levelNext >= 32 -> "B-Rank"
                levelNext >= 18 -> "C-Rank"
                levelNext >= 8 -> "D-Rank"
                else -> "E-Rank"
            }

            val titleNext = when {
                levelNext >= 80 -> "Sovereign of Absolute Death"
                levelNext >= 50 -> "General Monarch Commander"
                levelNext >= 32 -> "Crimson Shadow vanguard"
                levelNext >= 15 -> "Awakened S-Rank Core"
                else -> stats.title
            }

            val updatedStats = stats.copy(
                level = levelNext,
                experience = expNext,
                gold = stats.gold + finalGold,
                crystals = stats.crystals + finalCrystals,
                statPoints = stats.statPoints + addedStatPoints,
                rank = rankNext,
                title = titleNext
            )

            repository.updateHunter(updatedStats)

            _combatLogs.value = listOf(
                "🎉 VICTORY! Gate dimensional portal sealed successfully.",
                "Rewards Claimed: +$finalGold Gold, +$finalCrystals Crystals, +${gate.expReward} EXP.",
                if (levelNext > stats.level) "💥 SYSTEM ALERT: LEVEL UP! You reached Level $levelNext. Awarded +$addedStatPoints Stat Points!" else "Your mana density continues to rise."
            )

            if (levelNext > stats.level) {
                playLevelUpSound()
            }

            // Move to extraction phase if gate has extractable entity
            if (gate.shadowExtractId != null && gate.shadowExtractName != null) {
                // Ensure they don't already own this shadow
                val ownsAlready = shadows.value.any { it.id == gate.shadowExtractId }
                if (!ownsAlready) {
                    delay(1200)
                    triggerExtractionPhase(gate.shadowExtractId, gate.shadowExtractName)
                } else {
                    _isFighting.value = false
                    _activeDungeon.value = null
                }
            } else {
                _isFighting.value = false
                _activeDungeon.value = null
            }
        }
    }

    private fun handleDefeat() {
        viewModelScope.launch {
            _combatLogs.value = listOf(
                "🚑 DEFEAT: Your HP depleted to zero.",
                "The system auto-activated emergencies, reviving you back in hospital limits.",
                "Train your physical stats or upgrade weapons before challenging portal Gate again!"
            )
            playSummon() // play alert beep/growl
            _isFighting.value = false
            _activeDungeon.value = null
            _isPenaltyMode.value = false
        }
    }

    private fun monsterName(): String {
        return if (_isPenaltyMode.value) "Terror Dunes Centipede" else _activeDungeon.value?.bossName ?: "Portal Monster"
    }

    // --- Arise Shadow Extraction System "Arise" ---
    private fun triggerExtractionPhase(shadowId: String, name: String) {
        _isExtractionPhase.value = true
        _extractionTargetId.value = shadowId
        _extractionTargetName.value = name
        _extractionAttemptsLeft.value = 3
        _extractionSuccess.value = false
        _extractionStatusMessage.value = "The mana mist of the defeated boss lingers. Speak your declaration: 'ARISE!'"
    }

    fun performAriseExtraction() {
        if (!_isExtractionPhase.value || _extractionAttemptsLeft.value <= 0) return

        val stats = hunterStats.value ?: return
        val currentAttempts = _extractionAttemptsLeft.value - 1
        _extractionAttemptsLeft.value = currentAttempts

        viewModelScope.launch {
            // Intellect and sense attributes give +2% extraction probability per level!
            val baseChance = 35f // 35% default
            val statsBonus = (stats.intelligence + stats.sense) * 0.4f
            val actualSuccessChance = (baseChance + statsBonus).coerceAtMost(85f)

            val isSuccess = (Math.random() * 100) < actualSuccessChance

            if (isSuccess) {
                playSummon()
                _extractionSuccess.value = true
                _extractionStatusMessage.value = "SUCCESS! Crimson smoke wraps around the beast. Shadows mold into your command."
                
                // Add to database
                val shadowType = when(_extractionTargetId.value) {
                    "igris" -> "Commander"
                    "beru" -> "General"
                    "baran_wyvern" -> "Monarch"
                    else -> "Knight"
                }
                
                val baseDamage = when(_extractionTargetId.value) {
                    "igris" -> 160
                    "beru" -> 380
                    "baran_wyvern" -> 850
                    else -> 60
                }

                val newShadow = ShadowEntity(
                    id = _extractionTargetId.value,
                    name = _extractionTargetName.value,
                    type = shadowType,
                    level = 1,
                    baseDamage = baseDamage,
                    isSummoned = false
                )
                repository.addShadow(newShadow)
            } else {
                playSlash() // friction noise
                if (currentAttempts > 0) {
                    _extractionStatusMessage.value = "FAILED! The beast's pride resists your command! Try declaring command with greater power ($currentAttempts attempts remaining)."
                } else {
                    _extractionStatusMessage.value = "FAILED! The beast's soul has dissipated into the void. Improve your Intelligence/Sense and try other Gates."
                }
            }
        }
    }

    fun closeExtractionScreen() {
        _isExtractionPhase.value = false
        _isFighting.value = false
        _activeDungeon.value = null
        playBeep()
    }

    // --- Gemini Sovereign Core Inquiry ---
    fun invokeSovereignCore() {
        val stats = hunterStats.value ?: return
        val activeWeapons = weapons.value.filter { it.isEquipped }.map { it.name }
        val activeShadows = shadows.value.filter { it.isSummoned }.map { it.name }
        
        val statsDescriptor = "Str:${stats.strength}, Agi:${stats.agility}, Vit:${stats.vitality}, Int:${stats.intelligence}, Sen:${stats.sense}"

        viewModelScope.launch {
            _isGeminiLoading.value = true
            _geminiResult.value = null
            playSummon()

            val resultText = GeminiClient.getAwakeningReview(
                hunterName = stats.name,
                rank = stats.rank,
                level = stats.level,
                weapons = activeWeapons,
                shadows = activeShadows,
                statsSummary = statsDescriptor
            )
            
            _geminiResult.value = resultText
            _isGeminiLoading.value = false
            playQuestCompleteSound()
        }
    }

    fun updateHunterName(newName: String) {
        val stats = hunterStats.value ?: return
        if (newName.isBlank() || newName.length > 12) return
        viewModelScope.launch {
            repository.updateHunter(stats.copy(name = newName))
            playBeep()
        }
    }

    // --- Floating Damage FX generator ---
    private fun spawnImpactText(text: String, colorHex: Long) {
        val randomX = (Math.random() * 240 - 120).toFloat()
        val randomY = (Math.random() * 120 - 240).toFloat()
        val newText = DamageText(text = text, colorHex = colorHex, offsetX = randomX, offsetY = randomY)
        
        val currentList = _damageTexts.value.toMutableList()
        currentList.add(newText)
        _damageTexts.value = currentList

        // Clear after showing (animates nicely)
        viewModelScope.launch {
            delay(1000)
            val updated = _damageTexts.value.toMutableList()
            updated.removeAll { it.id == newText.id }
            _damageTexts.value = updated
        }
    }

    private fun addCombatLog(log: String) {
        val current = _combatLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 25) {
            current.removeAt(current.size - 1)
        }
        _combatLogs.value = current
    }

    fun nextLevelExp(lvl: Int): Int {
        return (100 * Math.pow(1.15, (lvl - 1).toDouble())).toInt()
    }
}
