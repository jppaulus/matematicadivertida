package com.joaop.matematicadivertida

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * Player de sons de feedback usando SoundPool (alta compatibilidade e baixa latência).
 * Usa arquivos WAV em res/raw para sons alegres de jogo infantil.
 */
class SoundFeedbackPlayer(
    context: Context,
    private val volume: Float = 1.0f,
) : AutoCloseable {
    private val tag = "JogoInfantilSound"
    
    private val soundPool: SoundPool? = try {
        SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    } catch (e: Exception) {
        Log.w(tag, "Falha ao criar SoundPool: ${e.message}")
        null
    }
    
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var isLoaded = false
    
    init {
        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                isLoaded = true
                Log.d(tag, "Sons carregados com sucesso")
            }
        }
        
        try {
            correctSoundId = soundPool?.load(context, R.raw.sound_correct, 1) ?: 0
            wrongSoundId = soundPool?.load(context, R.raw.sound_wrong, 1) ?: 0
        } catch (e: Exception) {
            Log.w(tag, "Falha ao carregar sons: ${e.message}")
        }
    }

    fun playCorrect() {
        if (correctSoundId > 0) {
            try {
                soundPool?.play(correctSoundId, volume, volume, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.w(tag, "Erro ao tocar som de acerto: ${e.message}")
            }
        }
    }

    fun playWrong() {
        if (wrongSoundId > 0) {
            try {
                soundPool?.play(wrongSoundId, volume, volume, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.w(tag, "Erro ao tocar som de erro: ${e.message}")
            }
        }
    }

    override fun close() {
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
    }
}
