package com.inventario.mobile.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Gerenciador da passphrase do SQLCipher utilizada para criptografar o banco
 * de dados local (`inventario_offline_secure.db`) do aplicativo Android.
 *
 * ## Motivação
 *
 * Antes desta classe, a passphrase do SQLCipher era uma constante literal
 * (`"inventario_secure_key"`) embutida no código-fonte Kotlin. Qualquer
 * engenheiro reverso que extraísse o APK poderia ler a chave em texto claro
 * e descriptografar o banco local, expondo dados de coleta e tokens
 * armazenados offline.
 *
 * Esta classe elimina essa vulnerabilidade armazenando a passphrase de forma
 * criptografada em [EncryptedSharedPreferences], que por sua vez é protegida
 * por uma [MasterKey] gerenciada pelo **Android Keystore** — um enclave
 * seguro (hardware-backed em dispositivos compatíveis) que nunca expõe
 * material criptográfico para o espaço de usuário.
 *
 * ## Comportamento
 *
 * - **Primeira execução em um dispositivo:** gera uma passphrase aleatória
 *   de 32 bytes (256 bits de entropia) usando [SecureRandom], serializa em
 *   Base64 e persiste em [EncryptedSharedPreferences].
 * - **Execuções subsequentes:** recupera a passphrase armazenada, decodifica
 *   de Base64 e retorna para uso pelo SQLCipher.
 *
 * A passphrase é **única por instalação** — desinstalar e reinstalar o app
 * gera uma nova passphrase, tornando o banco antigo ilegível (comportamento
 * desejado: reinstalação equivale a wipe dos dados locais).
 *
 * ## Singleton thread-safe
 *
 * A classe é um singleton com double-checked locking (`@Volatile` +
 * `synchronized`) para evitar múltiplas instâncias concorrentes ao acessar o
 * Android Keystore — operações no Keystore são relativamente custosas e
 * devem ser serializadas.
 *
 * ## Uso típico
 *
 * ```kotlin
 * val passphraseBytes = SqlCipherKeyManager.getInstance(context).getOrCreatePassphrase()
 * val factory = net.sqlcipher.database.SupportFactory(passphraseBytes)
 * Room.databaseBuilder(context, AppDatabase::class.java, "inventario_offline_secure.db")
 *     .openHelperFactory(factory)
 *     .build()
 * ```
 *
 * ## Tratamento de erros
 *
 * Qualquer falha ao inicializar [EncryptedSharedPreferences] ou ler/gravar a
 * passphrase é propagada como [SqlCipherKeyException]. O chamador (tipicamente
 * `AppDatabase`) deve capturar esta exceção e exibir mensagem bloqueante ao
 * usuário, uma vez que sem a passphrase o banco local não pode ser aberto.
 *
 * ## Requisitos atendidos (spec `correcoes-seguranca`)
 *
 * - **R3.1** — Passphrase nunca é literal no código-fonte Kotlin.
 * - **R3.2** — Gerada com entropia mínima de 128 bits (usamos 256).
 * - **R3.3** — Recuperada do Keystore em execuções subsequentes, sem exposição
 *   em logs ou variáveis de longa duração.
 * - **R3.4** — Falhas do Keystore são sinalizadas via [SqlCipherKeyException]
 *   para que a UI bloqueie o acesso ao banco.
 *
 * @see MasterKey
 * @see EncryptedSharedPreferences
 * @see net.sqlcipher.database.SupportFactory
 */
class SqlCipherKeyManager private constructor(context: Context) {

    companion object {
        private const val TAG = "SqlCipherKeyManager"

        /** Nome do arquivo de preferências criptografadas que guarda a passphrase. */
        private const val PREFS_NAME = "sqlcipher_key_prefs"

        /** Chave sob a qual a passphrase Base64 é persistida nas prefs. */
        private const val KEY_PASSPHRASE = "sqlcipher_passphrase_b64"

        /** Tamanho da passphrase em bytes (256 bits). */
        private const val PASSPHRASE_BYTES = 32

        @Volatile
        private var instance: SqlCipherKeyManager? = null

        /**
         * Retorna a instância singleton do gerenciador, criando-a se necessário.
         *
         * Usa double-checked locking para evitar sincronização no caminho quente
         * (quando a instância já existe). O contexto é sempre reduzido a
         * `applicationContext` para evitar vazamento de Activity.
         *
         * @param context qualquer [Context] do aplicativo; internamente é convertido
         *                para `applicationContext`.
         * @return instância singleton de [SqlCipherKeyManager].
         * @throws SqlCipherKeyException se a inicialização das preferências
         *                               criptografadas falhar.
         */
        fun getInstance(context: Context): SqlCipherKeyManager =
            instance ?: synchronized(this) {
                instance ?: SqlCipherKeyManager(context.applicationContext).also { instance = it }
            }
    }

    /**
     * Handle para as preferências criptografadas que armazenam a passphrase.
     *
     * A inicialização é feita no construtor (eager) para falhar rapidamente
     * caso o Android Keystore esteja indisponível (dispositivo sem lock screen,
     * Keystore corrompido, etc.), em vez de adiar a falha para o primeiro uso.
     */
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        throw SqlCipherKeyException("Falha ao inicializar EncryptedSharedPreferences", e)
    }

    /**
     * Retorna a passphrase do SQLCipher para este dispositivo.
     *
     * Se já existir uma passphrase persistida, é decodificada de Base64 e
     * retornada. Caso contrário, gera uma nova passphrase aleatória com
     * [PASSPHRASE_BYTES] bytes (256 bits) via [SecureRandom], persiste
     * criptografada e retorna.
     *
     * A operação é idempotente: chamadas subsequentes no mesmo dispositivo
     * retornam sempre o mesmo `ByteArray` (em termos de conteúdo — a referência
     * pode variar).
     *
     * **Atenção:** o `ByteArray` retornado contém material criptográfico
     * sensível. O chamador deve:
     * - Não logá-lo nem convertê-lo para String fora de contextos criptográficos.
     * - Mantê-lo vivo apenas pelo tempo necessário para configurar o SQLCipher.
     *
     * @return passphrase de 32 bytes (256 bits) para uso com SQLCipher.
     * @throws SqlCipherKeyException se houver falha na leitura/gravação das
     *                               preferências criptografadas.
     */
    fun getOrCreatePassphrase(): ByteArray {
        try {
            val existing = prefs.getString(KEY_PASSPHRASE, null)
            if (existing != null) {
                return Base64.decode(existing, Base64.NO_WRAP)
            }
            val random = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
            prefs.edit()
                .putString(KEY_PASSPHRASE, Base64.encodeToString(random, Base64.NO_WRAP))
                .apply()
            return random
        } catch (e: Exception) {
            throw SqlCipherKeyException("Falha ao ler/gerar passphrase SQLCipher", e)
        }
    }

    /**
     * Exceção lançada quando o gerenciador não consegue inicializar o
     * Android Keystore ou ler/gravar a passphrase.
     *
     * Deve ser tratada pela camada de UI para bloquear o acesso ao banco local
     * e orientar o usuário (ex.: "Falha ao acessar o armazenamento seguro.
     * Verifique se o dispositivo tem tela de bloqueio configurada.").
     */
    class SqlCipherKeyException(message: String, cause: Throwable? = null)
        : RuntimeException(message, cause)
}
