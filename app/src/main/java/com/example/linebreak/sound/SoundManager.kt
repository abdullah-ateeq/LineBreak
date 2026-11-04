package com.example.linebreak.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100
    @Volatile var soundEnabled: Boolean = true

    fun playHit(streak: Int = 1) {
        if (!soundEnabled) return
        scope.launch {
            // Frequency rises slightly with streak for satisfying combo progression
            val baseFreq = 580f
            val freqStep = 45f
            val freq = (baseFreq + (streak.coerceAtMost(15) * freqStep)).coerceIn(400f, 1500f)
            val durationMs = 80
            val pcm = generateDecaySine(freq, durationMs, 0.4f)
            playPcm(pcm)
        }
    }

    fun playPerfect() {
        if (!soundEnabled) return
        scope.launch {
            // Dual chime chord (A5 + E6)
            val durationMs = 120
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            val freq1 = 880f
            val freq2 = 1320f
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val env = exp(-t * 22f)
                val s1 = sin(2.0 * PI * freq1 * t).toFloat()
                val s2 = sin(2.0 * PI * freq2 * t).toFloat()
                val sample = ((s1 * 0.5f + s2 * 0.5f) * env * 0.5f * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playWrongTap() {
        if (!soundEnabled) return
        scope.launch {
            // Low dissonant saw/buzz
            val durationMs = 140
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            val freq = 130f
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val env = exp(-t * 18f)
                val s = (sin(2.0 * PI * freq * t) + 0.4 * sin(4.0 * PI * freq * t)).toFloat()
                val sample = (s * env * 0.5f * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playDangerLineBreach() {
        if (!soundEnabled) return
        scope.launch {
            // Low sub-frequency downward dive
            val durationMs = 250
            val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / sampleRate
                val progress = i.toFloat() / numSamples
                val currentFreq = 220f - progress * 140f
                val env = exp(-t * 8f)
                val s = sin(2.0 * PI * currentFreq * t).toFloat()
                val sample = (s * env * 0.6f * Short.MAX_VALUE).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playButtonClick() {
        if (!soundEnabled) return
        scope.launch {
            val pcm = generateDecaySine(900f, 25, 0.25f)
            playPcm(pcm)
        }
    }

    private fun generateDecaySine(freq: Float, durationMs: Int, volume: Float): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val env = exp(-t * 30f)
            val sample = (sin(2.0 * PI * freq * t) * env * volume * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val trackSize = buffer.size.coerceAtLeast(minBufferSize) * 2

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(trackSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) { }
                }
                override fun onPeriodicNotification(t: AudioTrack?) { }
            })
        } catch (_: Exception) {
            // Ignore audio initialization errors gracefully
        }
    }
}
