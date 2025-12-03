package com.inventario.mobile.utils

/**
 * Constantes da aplicação
 */
object Constants {
    
    // Configurações de rede
    const val NETWORK_TIMEOUT = 30L // segundos
    const val CONNECT_TIMEOUT = 15L // segundos
    const val READ_TIMEOUT = 30L // segundos
    const val WRITE_TIMEOUT = 30L // segundos
    
    // Configurações de sincronização
    const val SYNC_INTERVAL_MINUTES = 15
    const val SYNC_RETRY_ATTEMPTS = 3
    const val SYNC_RETRY_DELAY_MS = 5000L
    const val SYNC_BATCH_SIZE = 50
    
    // Configurações de autenticação
    const val TOKEN_REFRESH_THRESHOLD_MINUTES = 5
    const val SESSION_TIMEOUT_MINUTES = 30
    
    // Configurações de QR Code
    const val QR_CODE_SCAN_TIMEOUT_MS = 30000L
    const val QR_CODE_VIBRATION_DURATION_MS = 200L
    
    // Configurações de banco de dados
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "inventario_database"
    
    // Configurações de arquivo
    const val MAX_FILE_SIZE_MB = 10
    const val IMAGE_QUALITY = 80
    const val IMAGE_MAX_WIDTH = 1024
    const val IMAGE_MAX_HEIGHT = 1024
    
    // Tipos de arquivo suportados
    val SUPPORTED_IMAGE_TYPES = listOf("jpg", "jpeg", "png", "bmp")
    
    // Status de sincronização
    object SyncStatus {
        const val PENDING = "PENDING"
        const val SYNCING = "SYNCING"
        const val SYNCED = "SYNCED"
        const val ERROR = "ERROR"
    }
    
    // Status de coleta
    object ColetaStatus {
        const val PENDENTE = "PENDENTE"
        const val COLETADO = "COLETADO"
        const val CONFERIDO = "CONFERIDO"
        const val DIVERGENTE = "DIVERGENTE"
    }
    
    // Perfis de usuário
    object UserProfile {
        const val ADMINISTRADOR = "ADMINISTRADOR"
        const val OPERADOR = "OPERADOR"
        const val CONSULTOR = "CONSULTOR"
    }
    
    // Tipos de entidade para sincronização
    object EntityType {
        const val PATRIMONIO = "PATRIMONIO"
        const val COLETA = "COLETA"
        const val USUARIO = "USUARIO"
        const val SETOR = "SETOR"
        const val SALA = "SALA"
    }
    
    // Operações de sincronização
    object SyncOperation {
        const val INSERT = "INSERT"
        const val UPDATE = "UPDATE"
        const val DELETE = "DELETE"
    }
    
    // Códigos de erro
    object ErrorCode {
        const val NETWORK_ERROR = 1001
        const val AUTH_ERROR = 1002
        const val VALIDATION_ERROR = 1003
        const val DATABASE_ERROR = 1004
        const val FILE_ERROR = 1005
        const val QR_CODE_ERROR = 1006
        const val SYNC_ERROR = 1007
        const val UNKNOWN_ERROR = 9999
    }
    
    // Mensagens de erro
    object ErrorMessage {
        const val NETWORK_UNAVAILABLE = "Sem conexão com a internet"
        const val SERVER_UNREACHABLE = "Servidor indisponível"
        const val INVALID_CREDENTIALS = "Credenciais inválidas"
        const val SESSION_EXPIRED = "Sessão expirada"
        const val VALIDATION_FAILED = "Dados inválidos"
        const val DATABASE_ERROR = "Erro no banco de dados"
        const val FILE_NOT_FOUND = "Arquivo não encontrado"
        const val QR_CODE_INVALID = "QR Code inválido"
        const val SYNC_FAILED = "Falha na sincronização"
        const val UNKNOWN_ERROR = "Erro desconhecido"
    }
    
    // Chaves de Intent/Bundle
    object IntentKey {
        const val PATRIMONIO_ID = "patrimonio_id"
        const val COLETA_ID = "coleta_id"
        const val QR_CODE_RESULT = "qr_code_result"
        const val SETOR_ID = "setor_id"
        const val SALA_ID = "sala_id"
        const val USER_ID = "user_id"
        const val SYNC_RESULT = "sync_result"
    }
    
    // Códigos de request
    object RequestCode {
        const val QR_CODE_SCAN = 1001
        const val CAMERA_PERMISSION = 1002
        const val STORAGE_PERMISSION = 1003
        const val LOCATION_PERMISSION = 1004
    }
    
    // Configurações de notificação
    object Notification {
        const val CHANNEL_ID_SYNC = "sync_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
        const val NOTIFICATION_ID_SYNC = 1001
        const val NOTIFICATION_ID_ERROR = 1002
    }
    
    // Configurações de WorkManager
    object WorkManager {
        const val SYNC_WORK_NAME = "sync_work"
        const val PERIODIC_SYNC_WORK_NAME = "periodic_sync_work"
        const val CLEANUP_WORK_NAME = "cleanup_work"
    }
    
    // Configurações de log
    object Log {
        const val TAG_MAIN = "InventarioMobile"
        const val TAG_SYNC = "Sync"
        const val TAG_AUTH = "Auth"
        const val TAG_DATABASE = "Database"
        const val TAG_NETWORK = "Network"
        const val TAG_QR_CODE = "QRCode"
    }
    
    // Configurações de cache
    object Cache {
        const val MAX_CACHE_SIZE = 10 * 1024 * 1024L // 10MB
        const val CACHE_EXPIRY_HOURS = 24
    }
    
    // Configurações de paginação
    object Pagination {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
    
    // Regex patterns
    object Pattern {
        const val EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val PHONE = "^\\([0-9]{2}\\)\\s[0-9]{4,5}-[0-9]{4}$"
        const val QR_CODE = "^[A-Za-z0-9]{1,50}$"
        const val PATRIMONIO_NUMBER = "^[0-9]{1,20}$"
    }
}
