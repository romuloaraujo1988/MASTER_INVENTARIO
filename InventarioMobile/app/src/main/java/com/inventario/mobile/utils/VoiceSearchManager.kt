package com.inventario.mobile.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Gerenciador de busca por voz
 * Utiliza a API nativa do Android para reconhecimento de fala
 */
class VoiceSearchManager(private val context: Context) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var listener: VoiceSearchListener? = null
    
    companion object {
        private const val TAG = "VoiceSearchManager"
    }
    
    interface VoiceSearchListener {
        fun onResults(text: String)
        fun onError(error: String)
        fun onReadyForSpeech()
        fun onBeginningOfSpeech()
        fun onEndOfSpeech()
        fun onPartialResults(text: String)
    }
    
    /**
     * Verifica se o dispositivo suporta reconhecimento de voz
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }
    
    /**
     * Verifica se tem permissão de áudio
     */
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Inicia o reconhecimento de voz
     */
    fun startListening(listener: VoiceSearchListener) {
        this.listener = listener
        
        Log.d(TAG, "Iniciando reconhecimento de voz")
        
        // Verificar disponibilidade
        if (!isAvailable()) {
            Log.e(TAG, "Reconhecimento de voz não disponível")
            listener.onError("Reconhecimento de voz não disponível neste dispositivo")
            return
        }
        
        // Verificar permissão
        if (!hasAudioPermission()) {
            Log.e(TAG, "Permissão de áudio não concedida")
            listener.onError("Permissão de áudio necessária")
            return
        }
        
        try {
            // Destruir recognizer anterior se existir
            speechRecognizer?.destroy()
            
            // Criar novo recognizer
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
            
            // Configurar intent
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }
            
            // Iniciar reconhecimento
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Reconhecimento iniciado com sucesso")
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar reconhecimento", e)
            listener.onError("Erro ao iniciar reconhecimento: ${e.message}")
        }
    }
    
    /**
     * Para o reconhecimento de voz
     */
    fun stopListening() {
        Log.d(TAG, "Parando reconhecimento de voz")
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar reconhecimento", e)
        }
    }
    
    /**
     * Cancela o reconhecimento de voz
     */
    fun cancel() {
        Log.d(TAG, "Cancelando reconhecimento de voz")
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar reconhecimento", e)
        }
    }
    
    /**
     * Libera recursos
     */
    fun destroy() {
        Log.d(TAG, "Destruindo VoiceSearchManager")
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            listener = null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao destruir recognizer", e)
        }
    }
    
    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
            listener?.onReadyForSpeech()
        }
        
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
            listener?.onBeginningOfSpeech()
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Volume do áudio - pode ser usado para animação
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Buffer de áudio recebido
        }
        
        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            listener?.onEndOfSpeech()
        }
        
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                SpeechRecognizer.ERROR_CLIENT -> "Erro do cliente"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de áudio necessária"
                SpeechRecognizer.ERROR_NETWORK -> "Erro de rede"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de rede"
                SpeechRecognizer.ERROR_NO_MATCH -> "Não consegui entender. Tente novamente."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecedor ocupado"
                SpeechRecognizer.ERROR_SERVER -> "Erro do servidor"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nenhuma fala detectada"
                else -> "Erro desconhecido ($error)"
            }
            
            Log.e(TAG, "onError: $errorMessage")
            listener?.onError(errorMessage)
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Log.d(TAG, "onResults: $text")
                listener?.onResults(text)
            } else {
                Log.w(TAG, "onResults: Nenhum resultado")
                listener?.onError("Nenhum resultado encontrado")
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )
            
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Log.d(TAG, "onPartialResults: $text")
                listener?.onPartialResults(text)
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "onEvent: $eventType")
        }
    }
}
