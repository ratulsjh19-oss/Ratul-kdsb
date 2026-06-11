package com.example.util

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(val text: String)
data class GeminiContent(val parts: List<GeminiPart>)
data class GeminiRequest(val contents: List<GeminiContent>)

data class GeminiResponsePart(val text: String?)
data class GeminiResponseContent(val parts: List<GeminiResponsePart>?)
data class GeminiCandidate(val content: GeminiResponseContent?)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getAwakeningReview(
        hunterName: String,
        rank: String,
        level: Int,
        weapons: List<String>,
        shadows: List<String>,
        statsSummary: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getOfflineReview(hunterName, rank, level, weapons, shadows, statsSummary)
        }

        val prompt = """
            You are the Solo Leveling holographic 'System'. Oversee Hunter $hunterName's development.
            Current Stats & Progress:
            - Hunter Rank: $rank
            - Level: $level
            - Attributes: $statsSummary
            - Weapons Equipped: ${weapons.joinToString(", ")}
            - Active Shadows: ${shadows.joinToString(", ")}

            Please write a dynamic, epic evaluation of the player's progression, in your distinct, cold, mechanical, but hype System tone. 
            Format with:
            1. AWAKENING STATE: A highly dramatic status line describing their current aura (e.g. 'Your mana is beginning to condense into the Shadow Monarch's gravity!').
            2. ANALYSIS: 2 concise bullet points highlighting their build choices or accomplishments.
            3. DYNAMIC TITLE AWARDED: Assign a cool new customized title matching their playstyle in brackets like [The Goblins' Nightmare] or [Sovereign of Chilled Crypts].
            4. SYSTEM DIRECTIVE: A short, motivational mission warning. Like 'If you stop growing, death awaits. Keep leveling.'
            Keep it strictly under 180 words, using clean, crisp uppercase headings and direct spacings.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: getOfflineReview(hunterName, rank, level, weapons, shadows, statsSummary)
        } catch (e: Exception) {
            getOfflineReview(hunterName, rank, level, weapons, shadows, statsSummary) + "\n\n(System Note: Network Connection Offline.)"
        }
    }

    private fun getOfflineReview(
        hunterName: String,
        rank: String,
        level: Int,
        weapons: List<String>,
        shadows: List<String>,
        statsSummary: String
    ): String {
        val calculatedTitle = when {
            level >= 50 -> "[Sovereign Monarch of Death, Master of Shadows]"
            level >= 30 -> "[Shadow Lord Vanguard]"
            level >= 15 -> "[Elite S-Rank Awakening Candidate]"
            level >= 5 -> "[The E-Rank Prodigious Challenger]"
            else -> "[The Struggling Rank-E Awakening]"
        }
        
        return """
            [SYSTEM WARNING: CORE PROGRESS REPORT]

            AWAKENING STATE:
            Your progress has been indexed by the Monarch Core. At Level $level, the System senses your dark mana aura beginning to condense into the Shadow Monarch's gravity field! Your lethality indexes and physical density indices are growing proportionally.

            ANALYSIS:
            • Conditioned with daggers ${weapons.firstOrNull() ?: "Fists"}. Speed and critical velocity indexes are rising at an exponential rate.
            • Commanding ${shadows.size} shadow soldiers. Your dark forces stand fully materialized, ready to march across the gate domains.

            DYNAMIC TITLE AWARDED:
            $calculatedTitle

            SYSTEM DIRECTIVE:
            "A Sovereign does not hide from challenges. Expand your base stats, clear S-Rank Gates, and accumulate resources. Do not stagnate."
        """.trimIndent()
    }
}
