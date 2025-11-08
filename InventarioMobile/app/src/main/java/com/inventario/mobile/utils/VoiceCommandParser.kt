package com.inventario.mobile.utils

import android.util.Log

/**
 * Comando de voz reconhecido
 */
data class VoiceCommand(
    val action: CommandAction,
    val parameter: String? = null,
    val originalText: String = ""
)

/**
 * Ações disponíveis para comandos de voz
 */
enum class CommandAction {
    BUSCAR_PATRIMONIO,      // "buscar patrimônio 12345"
    MOSTRAR_SALA,           // "mostrar sala 203"
    LISTAR_DIVERGENCIAS,    // "listar divergências"
    LISTAR_PENDENTES,       // "listar pendentes"
    LISTAR_COLETADOS,       // "listar coletados"
    SINCRONIZAR,            // "sincronizar"
    ABRIR_SCANNER,          // "abrir scanner"
    VOLTAR,                 // "voltar"
    UNKNOWN                 // Comando não reconhecido
}

/**
 * Parser de comandos de voz
 * Converte texto em comandos estruturados
 */
class VoiceCommandParser {
    
    companion object {
        private const val TAG = "VoiceCommandParser"
    }
    
    /**
     * Analisa o texto e retorna um comando estruturado
     */
    fun parse(text: String): VoiceCommand {
        val normalizedText = normalizeText(text)
        Log.d(TAG, "Parsing: '$text' -> '$normalizedText'")
        
        return when {
            // Buscar patrimônio
            matchesBuscarPatrimonio(normalizedText) -> {
                val numero = extractNumber(normalizedText)
                Log.d(TAG, "Comando: BUSCAR_PATRIMONIO($numero)")
                VoiceCommand(CommandAction.BUSCAR_PATRIMONIO, numero, text)
            }
            
            // Mostrar sala
            matchesMostrarSala(normalizedText) -> {
                val numero = extractNumber(normalizedText)
                Log.d(TAG, "Comando: MOSTRAR_SALA($numero)")
                VoiceCommand(CommandAction.MOSTRAR_SALA, numero, text)
            }
            
            // Listar divergências
            matchesDivergencias(normalizedText) -> {
                Log.d(TAG, "Comando: LISTAR_DIVERGENCIAS")
                VoiceCommand(CommandAction.LISTAR_DIVERGENCIAS, null, text)
            }
            
            // Listar pendentes
            matchesPendentes(normalizedText) -> {
                Log.d(TAG, "Comando: LISTAR_PENDENTES")
                VoiceCommand(CommandAction.LISTAR_PENDENTES, null, text)
            }
            
            // Listar coletados
            matchesColetados(normalizedText) -> {
                Log.d(TAG, "Comando: LISTAR_COLETADOS")
                VoiceCommand(CommandAction.LISTAR_COLETADOS, null, text)
            }
            
            // Sincronizar
            matchesSincronizar(normalizedText) -> {
                Log.d(TAG, "Comando: SINCRONIZAR")
                VoiceCommand(CommandAction.SINCRONIZAR, null, text)
            }
            
            // Abrir scanner
            matchesAbrirScanner(normalizedText) -> {
                Log.d(TAG, "Comando: ABRIR_SCANNER")
                VoiceCommand(CommandAction.ABRIR_SCANNER, null, text)
            }
            
            // Voltar
            matchesVoltar(normalizedText) -> {
                Log.d(TAG, "Comando: VOLTAR")
                VoiceCommand(CommandAction.VOLTAR, null, text)
            }
            
            else -> {
                Log.w(TAG, "Comando não reconhecido: '$text'")
                VoiceCommand(CommandAction.UNKNOWN, null, text)
            }
        }
    }
    
    /**
     * Normaliza o texto para facilitar o matching
     */
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .trim()
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .replace("â", "a")
            .replace("ê", "e")
            .replace("ô", "o")
            .replace("ã", "a")
            .replace("õ", "o")
            .replace("ç", "c")
    }
    
    /**
     * Extrai número do texto
     */
    private fun extractNumber(text: String): String? {
        val regex = "\\d+".toRegex()
        val match = regex.find(text)
        return match?.value
    }
    
    // Matchers para cada tipo de comando
    
    private fun matchesBuscarPatrimonio(text: String): Boolean {
        return text.contains("buscar patrimonio") ||
                text.contains("procurar patrimonio") ||
                text.contains("buscar item") ||
                text.contains("procurar item") ||
                text.contains("buscar numero") ||
                text.contains("procurar numero") ||
                (text.contains("buscar") && extractNumber(text) != null) ||
                (text.contains("procurar") && extractNumber(text) != null)
    }
    
    private fun matchesMostrarSala(text: String): Boolean {
        return text.contains("mostrar sala") ||
                text.contains("abrir sala") ||
                text.contains("ir para sala") ||
                text.contains("ir pra sala") ||
                text.contains("sala numero") ||
                (text.contains("sala") && extractNumber(text) != null)
    }
    
    private fun matchesDivergencias(text: String): Boolean {
        return text.contains("divergencia") ||
                text.contains("problema") ||
                text.contains("erro") ||
                text.contains("inconsistencia") ||
                text.contains("listar divergencia") ||
                text.contains("mostrar divergencia") ||
                text.contains("ver divergencia")
    }
    
    private fun matchesPendentes(text: String): Boolean {
        return text.contains("pendente") ||
                text.contains("falta") ||
                text.contains("nao coletado") ||
                text.contains("listar pendente") ||
                text.contains("mostrar pendente") ||
                text.contains("ver pendente") ||
                text.contains("o que falta")
    }
    
    private fun matchesColetados(text: String): Boolean {
        return text.contains("coletado") ||
                text.contains("concluido") ||
                text.contains("finalizado") ||
                text.contains("listar coletado") ||
                text.contains("mostrar coletado") ||
                text.contains("ver coletado")
    }
    
    private fun matchesSincronizar(text: String): Boolean {
        return text.contains("sincronizar") ||
                text.contains("sincroniza") ||
                text.contains("atualizar") ||
                text.contains("enviar dados") ||
                text.contains("enviar coleta") ||
                text.contains("fazer sync") ||
                text.contains("fazer sinc")
    }
    
    private fun matchesAbrirScanner(text: String): Boolean {
        return text.contains("abrir scanner") ||
                text.contains("abrir camera") ||
                text.contains("escanear") ||
                text.contains("ler qr") ||
                text.contains("ler codigo") ||
                text.contains("scanner")
    }
    
    private fun matchesVoltar(text: String): Boolean {
        return text.contains("voltar") ||
                text.contains("retornar") ||
                text.contains("sair") ||
                text.contains("fechar")
    }
    
    /**
     * Retorna sugestões de comandos disponíveis
     */
    fun getSuggestions(): List<String> {
        return listOf(
            "Buscar patrimônio 12345",
            "Mostrar sala 203",
            "Listar divergências",
            "Listar pendentes",
            "Listar coletados",
            "Sincronizar",
            "Abrir scanner",
            "Voltar"
        )
    }
}
