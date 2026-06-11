package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object SoundSynth {
    private const val SAMPLE_RATE = 22050

    suspend fun playSlash() = withContext(Dispatchers.Default) {
        val duration = 0.15f
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 1300f - progress * 1000f // Sweep down
            val angle = 2.0 * Math.PI * freq * t
            val sineVal = sin(angle)
            val noise = (Math.random() * 2.0 - 1.0) * (1.0 - progress) * 0.35
            val composite = (sineVal * (1.0 - progress) * 0.5) + noise
            buffer[i] = (composite * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playAudio(buffer)
    }

    suspend fun playLevelUp() = withContext(Dispatchers.Default) {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f) // C5, E5, G5, C6
        val noteDuration = 0.12f
        val numSamplesPerNote = (noteDuration * SAMPLE_RATE).toInt()
        val totalSamples = numSamplesPerNote * notes.size
        val buffer = ShortArray(totalSamples)

        for (n in notes.indices) {
            val freq = notes[n]
            val offset = n * numSamplesPerNote
            for (i in 0 until numSamplesPerNote) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = i.toFloat() / numSamplesPerNote
                val envelope = if (progress < 0.15f) progress / 0.15f else (1.0f - progress)
                val angle = 2.0 * Math.PI * freq * t
                val sample = sin(angle) * envelope * 0.65
                buffer[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
        }
        playAudio(buffer)
    }

    suspend fun playSummonShadow() = withContext(Dispatchers.Default) {
        val duration = 0.45f
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 200f - progress * 140f
            val angle = 2.0 * Math.PI * freq * t
            val wave = if (sin(angle) > 0) 1.0 else -1.0
            val noise = (Math.random() * 2.0 - 1.0) * 0.2
            val composite = (wave * (1.0 - progress) * 0.4) + (noise * (1.0 - progress) * 0.3)
            buffer[i] = (composite * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playAudio(buffer)
    }

    suspend fun playQuestComplete() = withContext(Dispatchers.Default) {
        val notes = floatArrayOf(523.25f, 783.99f, 659.25f, 1046.50f)
        val noteDuration = 0.14f
        val numSamplesPerNote = (noteDuration * SAMPLE_RATE).toInt()
        val totalSamples = numSamplesPerNote * notes.size
        val buffer = ShortArray(totalSamples)

        for (n in notes.indices) {
            val freq = notes[n]
            val offset = n * numSamplesPerNote
            for (i in 0 until numSamplesPerNote) {
                val t = i.toFloat() / SAMPLE_RATE
                val progress = i.toFloat() / numSamplesPerNote
                val envelope = (1.0f - progress) * (1.0f - progress)
                val angle = 2.0 * Math.PI * freq * t
                val sample = sin(angle) * envelope * 0.6
                buffer[offset + i] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
        }
        playAudio(buffer)
    }

    suspend fun playButtonClick() = withContext(Dispatchers.Default) {
        val duration = 0.04f
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 2300f - progress * 1000f
            val angle = 2.0 * Math.PI * freq * t
            val sample = sin(angle) * (1.0f - progress) * 0.45
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        playAudio(buffer)
    }

    private fun playAudio(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            
            val playDurationMs = (buffer.size.toFloat() / SAMPLE_RATE * 1000).toLong()
            Thread.sleep(playDurationMs + 40)
            
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
