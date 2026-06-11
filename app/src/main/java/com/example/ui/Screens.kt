package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HunterStats
import com.example.data.ShadowEntity
import com.example.data.WeaponEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

// --- MASTER MAIN GRAPHICAL HUD LAYOUT ---
@Composable
fun MainGameHudScreen(viewModel: GameViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val stats by viewModel.hunterStats.collectAsState()
    val isFighting by viewModel.isFighting.collectAsState()
    val isExtractionPhase by viewModel.isExtractionPhase.collectAsState()

    Scaffold(
        topBar = {
            if (!isFighting && !isExtractionPhase) {
                stats?.let { HunterCompactHeader(it) }
            }
        },
        bottomBar = {
            if (!isFighting && !isExtractionPhase) {
                BottomNavigationBar(currentTab) { viewModel.selectTab(it) }
            }
        },
        containerColor = SystemDeepBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Sophisticated Indigo background glow (ref. Design HTML custom blur)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1E4F46E5), Color.Transparent), // Indigo-600 soft glow
                            center = Offset(size.width * 0.2f, size.height * 0.15f),
                            radius = size.width * 0.85f
                        ),
                        radius = size.width * 0.85f,
                        center = Offset(size.width * 0.2f, size.height * 0.15f)
                    )
                    // Sophisticated Purple background glow (ref. Design HTML custom blur)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x168B5CF6), Color.Transparent), // Purple-600 soft glow
                            center = Offset(size.width * 0.85f, size.height * 0.8f),
                            radius = size.width * 0.75f
                        ),
                        radius = size.width * 0.75f,
                        center = Offset(size.width * 0.85f, size.height * 0.8f)
                    )
                }
        ) {
            if (isExtractionPhase) {
                ShadowExtractionScreen(viewModel = viewModel)
            } else if (isFighting) {
                ActiveCombatArena(viewModel = viewModel)
            } else {
                when (currentTab) {
                    ActiveUiTab.Status -> StatusTabScreen(viewModel)
                    ActiveUiTab.Workout -> DailyWorkoutScreen(viewModel)
                    ActiveUiTab.Gates -> GateDungeonsScreen(viewModel)
                    ActiveUiTab.Shadows -> ShadowArmyScreen(viewModel)
                    ActiveUiTab.Armory -> ArmoryDaggersScreen(viewModel)
                    ActiveUiTab.MonarchCore -> MonarchCoreScreen(viewModel)
                }
            }
        }
    }
}

// --- COMPONENT: PORTRAIT HEADER ---
@Composable
fun HunterCompactHeader(stats: HunterStats) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color(0x660A0A0C), // Transparent black overlay for glassmorphic slate look
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left avatar: Indigo-to-Purple Rounded-2xl container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF4F46E5), Color(0xFF6B21A8)) // indigo-600 to purple-800
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.5.dp, Color(0xFF818CF8).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Monarch Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name & Subtitles
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stats.name.uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp,
                            modifier = Modifier.testTag("hunter_name_text")
                        )
                    }
                    Text(
                        text = stats.title.uppercase(),
                        color = Color(0xFF818CF8), // text-indigo-400 equivalent
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    // Dynamic indicators for Gold and Crystals in a row instead of right-aligned column
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Gold",
                                tint = SystemMonarchGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${stats.gold}",
                                color = SystemMonarchGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Crystals",
                                tint = Color(0xFF818CF8), // beautiful indigo
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${stats.crystals}",
                                color = Color(0xFF818CF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Right side: Active Rank with sophisticated typography gradient shadow
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stats.rank,
                        color = Color(0xFFC084FC), // light purple text
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(bottom = 0.dp),
                        style = LocalTextStyle.current.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFFA855F7).copy(alpha = 0.5f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = "SYSTEM RANK",
                        color = Color(0xFF64748B), // slate-500
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val nextLvlExp = 100 * Math.pow(1.15, (stats.level - 1).toDouble())
            val progress = (stats.experience.toFloat() / nextLvlExp.toFloat()).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "EXP LEVEL ${stats.level}",
                    color = SystemTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f)) // Slate 800 background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFFA855F7)) // indigo-600 to purple-500
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stats.experience}/${nextLvlExp.toInt()}",
                    color = SystemTextLight,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// --- TAB 1: STATUS AND ATTRIBUTES DISTRIBUTOR ---
@Composable
fun StatusTabScreen(viewModel: GameViewModel) {
    val stats by viewModel.hunterStats.collectAsState()
    val summonedList by viewModel.summonedShadows.collectAsState()
    val weaponsList by viewModel.weapons.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    
    val equippedWeapon = weaponsList.find { it.isEquipped }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats?.let { liveStats ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, SystemNeonCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SYSTEM REGISTRATION INQUIRY",
                            color = SystemNeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (editingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { if (it.length <= 12) tempName = it },
                                    label = { Text("Hunter Name", color = SystemNeonCyan) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = SystemTextLight,
                                        unfocusedTextColor = SystemTextLight,
                                        focusedBorderColor = SystemNeonCyan,
                                        unfocusedBorderColor = SystemTextMuted,
                                        focusedContainerColor = Color.Black,
                                        unfocusedContainerColor = Color.Black
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        viewModel.updateHunterName(tempName)
                                        editingName = false
                                        keyboardController?.hide()
                                    }),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("name_edit_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        viewModel.updateHunterName(tempName)
                                        editingName = false
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier.testTag("save_name_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = SystemNeonCyan)
                                }
                            } else {
                                Text(
                                    text = liveStats.name,
                                    color = SystemTextLight,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        tempName = liveStats.name
                                        editingName = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SystemNeonCyan.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, SystemNeonCyan),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("edit_name_btn")
                                ) {
                                    Text("CUSTOMIZE", color = SystemNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Stat Distribution Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, SystemVividViolet.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "HUNTER PHYSICAL CONDITIONING",
                                    color = SystemVividViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Distribute awakened essence core status.",
                                    color = SystemTextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (liveStats.statPoints > 0) SystemShadowPurple else Color(0x22FFFFFF))
                                    .border(1.dp, if (liveStats.statPoints > 0) SystemNeonCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "POINTS: ${liveStats.statPoints}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("stat_points_indicator")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val attributes = listOf(
                            Triple("STRENGTH", liveStats.strength, "Raises raw physical attacks and dagger slashing damage"),
                            Triple("AGILITY", liveStats.agility, "Improves combat hit combos and passive evasion chances"),
                            Triple("VITALITY", liveStats.vitality, "Expands full HP capacity and reduces enemy blows"),
                            Triple("INTELLIGENCE", liveStats.intelligence, "Expands base Mana pool, skill potency, and shadow arise extraction"),
                            Triple("SENSE", liveStats.sense, "Augments critical hits factor and dungeon gold drop parameters")
                        )

                        attributes.forEach { (name, value, desc) ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            color = SystemTextLight,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = desc,
                                            color = SystemTextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.width(110.dp)
                                    ) {
                                        Text(
                                            text = "$value",
                                            color = SystemNeonCyan,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(end = 12.dp)
                                        )
                                        if (liveStats.statPoints > 0) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(SystemShadowPurple, CircleShape)
                                                    .clickable { viewModel.allocateStat(name) }
                                                    .testTag("allocate_${name.lowercase()}_button")
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "Allocate", tint = Color.White, modifier = Modifier.size(18.dp))
                                            }
                                        } else {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0x1FADADAD), CircleShape)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "Allocate", tint = Color(0x35FFFFFF), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Divider(color = Color(0x15FFFFFF))
                            }
                        }
                    }
                }
            }

            // Equipped Squad Overview Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, Color(0x15FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMMAND SQUAD REGISTRY",
                            color = SystemMutedGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Weapon Slot
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ACTIVE WEAPON", color = SystemTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .border(2.dp, SystemNeonCyan, RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F0E1A))
                                        .padding(4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = equippedWeapon?.name ?: "Bare fists",
                                            color = SystemNeonCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (equippedWeapon != null) "+${equippedWeapon.attackBonus} ATK" else "+0 ATK",
                                            color = SystemTextMuted,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                            
                            // Shadow Slots
                            Column(modifier = Modifier.weight(2.0f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SUMMONED SHADOWS", color = SystemTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    for (i in 0 until 3) {
                                        val shadowItem = summonedList.getOrNull(i)
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(64.dp)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (shadowItem != null) SystemShadowPurple else Color(0x1F8B8A9E),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .background(Color(0xFF0F0E1A))
                                                .padding(2.dp)
                                        ) {
                                            if (shadowItem != null) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = shadowItem.name.split(" ").first(),
                                                        color = SystemVividViolet,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Text(
                                                        text = "Lv.${shadowItem.level}",
                                                        color = SystemTextMuted,
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            } else {
                                                Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked Slit", tint = Color(0x15FFFFFF), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: DAILY SYSTEM WORKOUT REPS ---
@Composable
fun DailyWorkoutScreen(viewModel: GameViewModel) {
    val stats by viewModel.hunterStats.collectAsState()
    var leftFootSelected by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats?.let { liveStats ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, SystemNeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CRITICAL SYSTEM INJUNCTION: DAILY QUEST",
                            color = SystemNeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "PREPARATION OF THE WEAKEST",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Failure to execute this conditioning matrix daily triggers sandstorm dunes penalty hazards.",
                            color = SystemTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            val workoutSpecs = listOf(
                Triple("Pushup", liveStats.dailyPushUps, liveStats.pushUpsCompleted),
                Triple("Situp", liveStats.dailySitUps, liveStats.sitUpsCompleted),
                Triple("Squat", liveStats.dailySquats, liveStats.squatsCompleted)
            )

            items(workoutSpecs) { (name, progress, completed) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x55100D22)),
                    border = BorderStroke(1.dp, if (completed) SystemNeonCyan else Color(0x1F8B8A9E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (completed) "✅ $name Training" else "🏋️ $name Training",
                                color = if (completed) SystemNeonCyan else SystemTextLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF07050F))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress / 100f)
                                        .background(if (completed) SystemNeonCyan else SystemShadowPurple)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Completions: $progress / 100 reps",
                                color = SystemTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (!completed) {
                            Button(
                                onClick = { viewModel.incrementWorkout(name) },
                                colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                                border = BorderStroke(1.dp, SystemNeonCyan),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("perform_${name.lowercase()}_rep_btn")
                            ) {
                                Text("REP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(width = 68.dp, height = 36.dp)
                                    .background(Color(0x1200E5FF), RoundedCornerShape(4.dp))
                                    .border(1.dp, SystemNeonCyan, RoundedCornerShape(4.dp))
                            ) {
                                Text("DONE", color = SystemNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Cardio Trainer Run Games
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x55100D22)),
                    border = BorderStroke(1.dp, if (liveStats.runCompleted) SystemNeonCyan else Color(0x1F8B8A9E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (liveStats.runCompleted) "✅ 10 KM RUN TRAINING" else "🏃 10 KM RUN TRAINING",
                            color = if (liveStats.runCompleted) SystemNeonCyan else SystemTextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF07050F))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(liveStats.dailyRunMeters / 10000f)
                                    .background(if (liveStats.runCompleted) SystemNeonCyan else SystemVividViolet)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Distance: ${liveStats.dailyRunMeters} m / 10,000 m",
                                color = SystemTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (liveStats.runCompleted) {
                                Text("COMPLETED", color = SystemNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!liveStats.runCompleted) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Alternate left/right foot as rapidly as possible to finish run:",
                                color = SystemTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        if (leftFootSelected) {
                                            viewModel.incrementRunning(135)
                                            leftFootSelected = false
                                        }
                                    },
                                    enabled = leftFootSelected,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SystemShadowPurple,
                                        disabledContainerColor = Color(0x22100D22)
                                    ),
                                    border = BorderStroke(1.dp, if (leftFootSelected) SystemNeonCyan else Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("run_left_foot_btn")
                                ) {
                                    Text("LEFT FOOT", fontSize = 11.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        if (!leftFootSelected) {
                                            viewModel.incrementRunning(135)
                                            leftFootSelected = true
                                        }
                                    },
                                    enabled = !leftFootSelected,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SystemShadowPurple,
                                        disabledContainerColor = Color(0x22100D22)
                                    ),
                                    border = BorderStroke(1.dp, if (!leftFootSelected) SystemNeonCyan else Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("run_right_foot_btn")
                                ) {
                                    Text("RIGHT FOOT", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Penalty Area Trigger
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF19090D)),
                    border = BorderStroke(1.dp, SystemRedDread),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚠️ EMERGENCY HAZARD TRIGGER",
                            color = SystemRedDread,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Dunes Terror Penalty Gate",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Enter survive dunes quest manually to test combat combinations and active dodge capabilities.",
                            color = SystemTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.triggerWorkoutPenalty() },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemRedDread),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("force_penalty_btn")
                        ) {
                            Text("ENTER PHYSICAL PENALTY GATE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: GATE DUNGEONS PORTALS LIST ---
@Composable
fun GateDungeonsScreen(viewModel: GameViewModel) {
    val stats by viewModel.hunterStats.collectAsState()
    val gates = viewModel.dungeonGates

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DIMENSIONAL PORTALS INDEX",
                    color = SystemNeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Awakened Gate Portals",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Claim dimensional crystals, gold coin piles, and perform awakening arise captures under the Monarch Core logic.",
                    color = SystemTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        stats?.let { liveStats ->
            items(gates) { gate ->
                val levelLocked = liveStats.level < gate.recommendedLevel
                val borderGlowColor = when {
                    levelLocked -> Color(0x15FFFFFF)
                    gate.rank.startsWith("S") -> SystemMonarchGold
                    gate.rank.startsWith("A") -> SystemRedDread
                    gate.rank.startsWith("B") -> SystemShadowPurple
                    else -> SystemNeonCyan
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.5.dp, borderGlowColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (levelLocked) 0.dp else 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            clip = true,
                            ambientColor = borderGlowColor,
                            spotColor = borderGlowColor
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(borderGlowColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = gate.rank,
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            if (levelLocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = SystemRedDread, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "LOCKED (Req Lv. ${gate.recommendedLevel})",
                                        color = SystemRedDread,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Text(
                                    "RECOMMENDED LEVEL: ${gate.recommendedLevel}+",
                                    color = SystemTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = gate.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GUARDIAN: ${gate.bossName}",
                            color = SystemTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "⭐ +${gate.goldReward}G",
                                color = SystemMonarchGold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "❤️ +${gate.crystalReward}C",
                                color = SystemNeonCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "✨ +${gate.expReward} EXP",
                                color = SystemVividViolet,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        gate.shadowExtractName?.let { name ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F0E1A), RoundedCornerShape(4.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "👤 Shadow Extraction Target: $name",
                                    color = SystemVividViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startDungeon(gate) },
                            enabled = !levelLocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = borderGlowColor,
                                disabledContainerColor = Color(0x1F8B8A9E)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("enter_portal_button_${gate.id}")
                        ) {
                            Text(
                                text = if (levelLocked) "PORTAL SEALED" else "CHALLENGE PORTAL GATE",
                                color = if (levelLocked) SystemTextMuted else Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 4: SHADOW ARMY CRYPT COMMAND ---
@Composable
fun ShadowArmyScreen(viewModel: GameViewModel) {
    val stats by viewModel.hunterStats.collectAsState()
    val shadowList by viewModel.shadows.collectAsState()
    val summonedList by viewModel.summonedShadows.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "THE MONARCH'S CRYPT",
                    color = SystemVividViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Command Shadow Forces",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Extract shadows from elite portal dungeoneers. Toggle active squad commands up to 3 to support active slashes.",
                    color = SystemTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                
                if (shadowList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Summon Active Count: ${summonedList.size} / 3",
                        color = SystemNeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (shadowList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, Color(0x15FFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💀",
                            fontSize = 54.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = "NO COMMAND SHADOWS FOUND",
                            color = SystemVividViolet,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your sovereign shadow legion is empty. Defeat ranked portal Bosses (such as Commander Igris or Ant King Beru) and declare \"ARISE!\" during critical extraction phases.",
                            color = SystemTextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.selectTab(ActiveUiTab.Gates) },
                            colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                            modifier = Modifier.testTag("go_hunt_btn")
                        ) {
                            Text("CHALLENGE BOSS PORTALS", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(shadowList) { shadow ->
                val goldCost = shadow.level * 800
                val crystalCost = shadow.level + 1

                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, if (shadow.isSummoned) SystemVividViolet else Color(0x33FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = shadow.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(SystemShadowPurple)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            shadow.type,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text(
                                    text = "LEVEL: ${shadow.level}  •  DPS ASSIST: ${shadow.baseDamage + (shadow.level * 8)}",
                                    color = SystemNeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleShadowSummon(shadow) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (shadow.isSummoned) SystemVividViolet else Color(0xFF1B1A2E)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, if (shadow.isSummoned) SystemNeonCyan else SystemTextMuted),
                                modifier = Modifier.testTag("summon_toggle_btn_${shadow.id}")
                            ) {
                                Text(
                                    text = if (shadow.isSummoned) "ACTIVE SQUAD" else "SUMMON",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0x15FFFFFF))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("UPGRADE MANA REGISTRY", color = SystemTextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Gold", tint = SystemMonarchGold, modifier = Modifier.size(12.dp))
                                    Text(" ${goldCost}G  ", color = SystemMonarchGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Crystals", tint = SystemNeonCyan, modifier = Modifier.size(11.dp))
                                    Text(" ${crystalCost}C", color = SystemNeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { viewModel.releaseShadowSoldier(shadow.id) },
                                    modifier = Modifier.testTag("release_shadow_btn_${shadow.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Dismiss", tint = SystemRedDread, modifier = Modifier.size(18.dp))
                                }

                                Button(
                                    onClick = { viewModel.upgradeShadow(shadow) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    border = BorderStroke(1.dp, SystemMonarchGold),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("upgrade_shadow_btn_${shadow.id}")
                                ) {
                                    Text("LEVEL UP", color = SystemMonarchGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 5: WEAPONS ARMORY SHOP ---
@Composable
fun ArmoryDaggersScreen(viewModel: GameViewModel) {
    val stats by viewModel.hunterStats.collectAsState()
    val weaponsList by viewModel.weapons.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "THE SYSTEM FORGE AND ARMORY",
                    color = SystemMonarchGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Legendary Awakened Blades",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Acquire high-grade daggers forged using mana core materials. Equipped blades directly supplement core damage attributes.",
                    color = SystemTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        stats?.let { liveStats ->
            items(weaponsList) { weapon ->
                val borderGlow = when (weapon.rank) {
                    "S-Rank" -> SystemMonarchGold
                    "A-Rank" -> SystemRedDread
                    "B-Rank" -> SystemShadowPurple
                    else -> SystemNeonCyan
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                    border = BorderStroke(1.dp, if (weapon.isEquipped) SystemNeonCyan else borderGlow.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = weapon.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(borderGlow)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            weapon.rank,
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text(
                                    text = "COMBAT ATK: +${weapon.attackBonus}",
                                    color = SystemNeonCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (weapon.isOwned) {
                                if (weapon.isEquipped) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .border(1.dp, SystemNeonCyan, RoundedCornerShape(4.dp))
                                            .background(Color(0x1F00E5FF))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("EQUIPPED", color = SystemNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.selectWeapon(weapon.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1A2E)),
                                        border = BorderStroke(1.dp, SystemTextMuted),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("equip_weapon_btn_${weapon.id}")
                                    ) {
                                        Text("EQUIP", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyWeapon(weapon) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                                    border = BorderStroke(1.dp, borderGlow),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("buy_weapon_btn_${weapon.id}")
                                ) {
                                    Text("ACQUIRE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = weapon.effectDescription,
                            color = SystemTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )

                        if (!weapon.isOwned) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0x15FFFFFF))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sovereign Value: ", color = SystemTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                if (weapon.goldCost > 0) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Gold", tint = SystemMonarchGold, modifier = Modifier.size(12.dp))
                                    Text(" ${weapon.goldCost}G  ", color = SystemMonarchGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                if (weapon.crystalCost > 0) {
                                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Crystals", tint = SystemNeonCyan, modifier = Modifier.size(11.dp))
                                    Text(" ${weapon.crystalCost}C", color = SystemNeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 6: SOVEREIGN SYSTEM CORE ASSESSMENT ---
@Composable
fun MonarchCoreScreen(viewModel: GameViewModel) {
    val results by viewModel.geminiResult.collectAsState()
    val loading by viewModel.isGeminiLoading.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "AWAKENED CONVERSION COGNITION",
                color = SystemNeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Shadow System Monarch Core",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerPoint = Offset(size.width / 2, size.height / 2)
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3500E5FF), Color.Transparent),
                        center = centerPoint,
                        radius = size.width / 2
                    ),
                    radius = size.width / 2
                )
                
                drawCircle(
                    color = SystemShadowPurple,
                    radius = (size.width / 2.6f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f),
                            rotationAngle * 0.7f
                        )
                    )
                )

                drawCircle(
                    color = SystemNeonCyan,
                    radius = (size.width / 3.4f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(30f, 15f),
                            -rotationAngle * 1.2f
                        )
                    )
                )
                
                drawCircle(
                    color = SystemMonarchGold,
                    radius = 12.dp.toPx()
                )
            }
        }

        Text(
            text = "Initiate systemic analysis. The Core will process your current physical conditioning, weapons parameters, and active shadow configurations to output dynamic S-Rank assessments and exclusive Hunter titles.",
            color = SystemTextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Button(
            onClick = { viewModel.invokeSovereignCore() },
            colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
            border = BorderStroke(1.5.dp, SystemNeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("activate_assessment_btn")
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SCANNING STATUS...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            } else {
                Text("INITIATE DIAGNOSTIC MATRIX", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C0A19))
                .border(1.5.dp, if (results != null) SystemNeonCyan else Color(0x1F8B8A9E), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            if (results != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = results!!,
                            color = SystemTextLight,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("assessment_result_text")
                        )
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "CORE TELEMETRY STANDBY\n\nConnect system node to begin live Awakening alignment assessment.",
                        color = SystemTextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// --- ACTIVE COMBAT ARENA SCREEN ---
@Composable
fun ActiveCombatArena(viewModel: GameViewModel) {
    val activeGate by viewModel.activeDungeon.collectAsState()
    val isPenalty by viewModel.isPenaltyMode.collectAsState()
    val logs by viewModel.combatLogs.collectAsState()
    val hunterHp by viewModel.hunterCurrentHp.collectAsState()
    val hunterMax by viewModel.hunterMaxHp.collectAsState()
    val hunterMp by viewModel.hunterCurrentMp.collectAsState()
    val hunterMaxM by viewModel.hunterMaxMp.collectAsState()
    val weaponList by viewModel.weapons.collectAsState()
    
    val monsterHp by viewModel.monsterCurrentHp.collectAsState()
    val monsterMax by viewModel.monsterMaxHp.collectAsState()
    
    val combo by viewModel.comboCount.collectAsState()
    val damages by viewModel.damageTexts.collectAsState()

    val currentWeapon = weaponList.find { it.isEquipped }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.runFromCombat() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                    border = BorderStroke(1.dp, SystemRedDread),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("retreat_combat_btn")
                ) {
                    Text("RETREAT", color = SystemRedDread, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (combo > 0) {
                    Box(
                        modifier = Modifier
                            .background(SystemShadowPurple, RoundedCornerShape(4.dp))
                            .border(1.dp, SystemNeonCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "COMBO: x$combo (+${(combo / 15 * 10).coerceAtMost(50)}% ATK)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                border = BorderStroke(1.5.dp, SystemRedDread),
                modifier = Modifier.fillModifierWithPulse(monsterHp / monsterMax)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isPenalty) "ANCIENT DUNES TERROR CENTIPEDE" else activeGate?.bossName ?: "Portal Monster",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .background(SystemRedDread)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (isPenalty) "PENALTY" else activeGate?.rank ?: "Rank E",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val percentage = (monsterHp / monsterMax).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF0F0E1A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(percentage)
                                .background(SystemRedDread)
                        )
                        Text(
                            "${monsterHp.toInt()} / ${monsterMax.toInt()} HP",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x1F07050F), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
                ) {
                    val centerPt = Offset(size.width / 2, size.height / 2)
                    val steps = 8
                    for (i in 0..steps) {
                        val spacing = size.width / steps
                        drawLine(
                            color = Color(0x0600E5FF),
                            start = Offset(i * spacing, 0f),
                            end = Offset(i * spacing, size.height),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0x0600E5FF),
                            start = Offset(0f, i * spacing),
                            end = Offset(size.width, i * spacing),
                            strokeWidth = 1f
                        )
                    }
                    
                    val pulse = 1.0f + 0.05f * sin(System.currentTimeMillis() / 200.0).toFloat()
                    drawCircle(
                        color = Color(0x189E00FF),
                        radius = 80.dp.toPx() * pulse,
                        center = centerPt
                    )
                    drawCircle(
                        color = Color(0x0F00E5FF),
                        radius = 45.dp.toPx() * pulse,
                        center = centerPt
                    )
                }

                damages.forEach { damage ->
                    Box(
                        modifier = Modifier.offset(damage.offsetX.dp, damage.offsetY.dp)
                    ) {
                        Text(
                            text = damage.text,
                            color = Color(damage.colorHex),
                            fontSize = if ("CRIT" in damage.text) 24.sp else 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.shadow(8.dp, CircleShape)
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SystemObsidian),
                border = BorderStroke(1.dp, SystemNeonCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "HUNTER HEALTH AND MANA CONDUIT",
                        color = SystemNeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    val hpFrac = (hunterHp / hunterMax).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF0F0E1A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(hpFrac)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            "HP ${hunterHp.toInt()}/${hunterMax.toInt()}",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    val mpFrac = (hunterMp / hunterMaxM).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF0F0E1A))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(mpFrac)
                                .background(Color(0xFF2196F3))
                        )
                        Text(
                            "MP ${hunterMp.toInt()}/${hunterMaxM.toInt()}",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.performHunterSlash() },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemNeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("basic_slash_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Slash", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentWeapon != null) "${currentWeapon.name.uppercase()} SLICE" else "STRIKE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { viewModel.performHunterSkill("Mutilate") },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                    shape = RoundedCornerShape(8.dp),
                    enabled = hunterMp >= 30,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("skill_mutilate_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MUTILATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("30 MP", color = SystemNeonCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                
                Button(
                    onClick = { viewModel.performHunterSkill("Dagger Rush") },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                    shape = RoundedCornerShape(8.dp),
                    enabled = hunterMp >= 15,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("skill_dagger_rush_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RUSH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        Text("15 MP", color = SystemNeonCyan, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF07050F))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = when {
                                "CRITICAL" in log || "Penalties" in log || "💥" in log || "FAILED" in log -> SystemRedDread
                                "VICTORY" in log || "🎉" in log -> SystemMonarchGold
                                "SKILL" in log -> SystemVividViolet
                                else -> SystemTextMuted
                            },
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SHADOW EXTRACTION "ARISE" CINEMATIC SUB-SCREEN ---
@Composable
fun ShadowExtractionScreen(viewModel: GameViewModel) {
    val name by viewModel.extractionTargetName.collectAsState()
    val attempts by viewModel.extractionAttemptsLeft.collectAsState()
    val status by viewModel.extractionStatusMessage.collectAsState()
    val success by viewModel.extractionSuccess.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA07050F))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "AWAKENING EXTRACTION CHAMBER",
                color = SystemNeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, if (success) SystemNeonCyan else SystemVividViolet, CircleShape)
                    .background(Color(0xFF0E0B1F), CircleShape)
            ) {
                val infiniteProgress = rememberInfiniteTransition()
                val pulseScale by infiniteProgress.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = EaseInOutQuad),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size((110 * pulseScale).dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (success) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color(0xFF9E00FF).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                Text(
                    text = if (success) "SOLDIER" else "BOSS\nSOUL",
                    color = if (success) SystemNeonCyan else SystemVividViolet,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SOUL: $name",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Shadow extractions remaining: $attempts / 3",
                color = SystemTextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (success) SystemNeonCyan else SystemVividViolet, RoundedCornerShape(8.dp))
                    .background(Color(0x35141221))
                    .padding(16.dp)
            ) {
                Text(
                    text = status,
                    color = SystemTextLight,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            if (!success && attempts > 0) {
                Button(
                    onClick = { viewModel.performAriseExtraction() },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemShadowPurple),
                    border = BorderStroke(1.5.dp, SystemNeonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("arise_speak_button")
                ) {
                    Text(
                        text = "DECLARE COMMAND: 'ARISE'",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.closeExtractionScreen() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (success) SystemNeonCyan else SystemTextMuted),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("close_extraction_btn")
                ) {
                    Text(
                        text = if (success) "WELCOME TO SOLDIERS ARMY" else "CLOSE GATE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- SYSTEM NAVIGATION BAR ---
@Composable
fun BottomNavigationBar(currentTab: ActiveUiTab, onTabSelected: (ActiveUiTab) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF0A0915),
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val items = listOf(
            Triple(ActiveUiTab.Status, "Status", Icons.Default.Person),
            Triple(ActiveUiTab.Workout, "Workout", Icons.Default.Check),
            Triple(ActiveUiTab.Gates, "Gates", Icons.Default.PlayArrow),
            Triple(ActiveUiTab.Shadows, "Shadows", Icons.Default.Refresh),
            Triple(ActiveUiTab.Armory, "Armory", Icons.Default.ShoppingCart),
            Triple(ActiveUiTab.MonarchCore, "Sovereign AI", Icons.Default.Settings)
        )

        items.forEach { (tab, label, icon) ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = icon, contentDescription = label, tint = if (currentTab == tab) SystemNeonCyan else SystemTextMuted) },
                label = {
                    Text(
                        text = label,
                        color = if (currentTab == tab) SystemNeonCyan else SystemTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0x3500E5FF)
                ),
                modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
            )
        }
    }
}

// Extension modifier to flash when health decays
fun Modifier.fillModifierWithPulse(hpFraction: Float): Modifier = this
    .fillMaxWidth()
    .drawBehind {
        if (hpFraction < 0.25f) {
            val brightness = (sin(System.currentTimeMillis() / 150.0) + 1.0) / 2.0
            drawRect(
                color = Color(0xFFFF1744).copy(alpha = (brightness * 0.15f).toFloat()),
                size = size
            )
        }
    }
