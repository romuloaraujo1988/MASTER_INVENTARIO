package com.inventario.mobile.security

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.inventario.mobile.R
import java.io.File
import kotlin.system.exitProcess

/**
 * Tela de recuperação exibida quando o Android Keystore fica inacessível.
 *
 * CR-02 (correcoes-seguranca — maio/2026): esta Activity é parte da
 * estratégia de resiliência do app quando o Keystore é invalidado (usuário
 * troca lock screen, ativa biometria em alguns OEMs, realiza factory reset
 * parcial ou restaura backup em novo dispositivo). Nessas situações a
 * `MasterKey` do SQLCipher deixa de abrir o banco local e
 * [SqlCipherKeyManager.SqlCipherKeyException] é lançada.
 *
 * Sem esta Activity, o app crashava no splash com mensagem genérica "O app
 * parou", sem caminho de recuperação exceto desinstalar — o que apagaria
 * coletas pendentes.
 *
 * ## Design
 *
 * - Standalone: **não depende de Hilt**, **não injeta AppDatabase**,
 *   **não usa coroutines**. Qualquer uma dessas dependências poderia
 *   recursivamente disparar a mesma exceção que nos trouxe aqui.
 * - Single-task: quando iniciada via `UncaughtExceptionHandler`, remove a
 *   tarefa original corrompida e inicia uma nova.
 * - Reset total: ao confirmar, apaga tudo que possa estar vinculado à chave
 *   invalidada — banco SQLCipher, prefs criptografadas e a própria chave —,
 *   em seguida força restart do processo. Na próxima execução, o fluxo
 *   normal de boot cria banco/chaves novos.
 *
 * Coletas locais não sincronizadas são perdidas ao fazer reset. Isso é
 * inevitável porque o banco está ilegível sem a chave perdida.
 */
class KeystoreRecoveryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "KeystoreRecovery"

        /**
         * Cria uma Intent configurada para este ponto de recuperação, com as
         * flags necessárias para limpar qualquer tarefa corrompida pelo crash.
         */
        fun createIntent(context: android.content.Context): Intent =
            Intent(context, KeystoreRecoveryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keystore_recovery)

        val messageView = findViewById<TextView>(R.id.tvRecoveryMessage)
        val btnReset = findViewById<Button>(R.id.btnResetData)

        messageView.text = getString(R.string.keystore_recovery_message)

        btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.keystore_recovery_confirm_title)
                .setMessage(R.string.keystore_recovery_confirm_message)
                .setPositiveButton(R.string.keystore_recovery_confirm_yes) { _, _ ->
                    performReset()
                }
                .setNegativeButton(R.string.keystore_recovery_confirm_no, null)
                .show()
        }
    }

    override fun onBackPressed() {
        // Impedir voltar: a Activity só pode ser dispensada via reset.
        Toast.makeText(
            this,
            R.string.keystore_recovery_back_blocked,
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Apaga o banco SQLCipher, as prefs criptografadas e a passphrase no
     * Keystore. Em seguida reinicia o processo para recriar tudo do zero.
     */
    private fun performReset() {
        Log.w(TAG, "═══════════════════════════════════════")
        Log.w(TAG, "RESET COMPLETO — recuperação de Keystore")
        Log.w(TAG, "═══════════════════════════════════════")

        try {
            // 1) Passphrase do Keystore — este passo é tolerante a falhas.
            try {
                SqlCipherKeyManager.getInstance(applicationContext)
                    .resetPassphrase(applicationContext)
                Log.w(TAG, "✓ Passphrase SQLCipher resetada")
            } catch (e: Exception) {
                Log.w(TAG, "Reset da passphrase falhou — apagando arquivo manualmente", e)
                deletePrefsFile("sqlcipher_key_prefs")
            }

            // 2) Banco SQLCipher — sem a chave, o arquivo está ilegível.
            val dbNames = listOf(
                "inventario_offline_secure.db",
                "inventario_offline.db"
            )
            for (name in dbNames) {
                val dbFile = getDatabasePath(name)
                listOf(
                    dbFile,
                    File(dbFile.parentFile, "$name-journal"),
                    File(dbFile.parentFile, "$name-shm"),
                    File(dbFile.parentFile, "$name-wal")
                ).forEach { if (it.exists()) it.delete() }
            }
            Log.w(TAG, "✓ Bancos de dados removidos")

            // 3) Prefs criptografadas (tokens JWT, dados sensíveis).
            deletePrefsFile("inventario_secure_prefs")
            deletePrefsFile("secure_prefs")
            Log.w(TAG, "✓ SharedPreferences criptografadas removidas")

            // 4) Caches HTTP / fotos locais que ficariam órfãos.
            try {
                cacheDir.deleteRecursively()
            } catch (_: Exception) { /* best-effort */ }

            Toast.makeText(
                this,
                R.string.keystore_recovery_success,
                Toast.LENGTH_LONG
            ).show()

            // 5) Reiniciar o processo. `finishAffinity` fecha todas as
            // Activities da task; `exitProcess(0)` encerra a JVM para que o
            // próximo launch recrie tudo do zero.
            finishAffinity()
            exitProcess(0)

        } catch (e: Exception) {
            Log.e(TAG, "Falha catastrófica no reset — usuário precisará reinstalar", e)
            AlertDialog.Builder(this)
                .setTitle(R.string.keystore_recovery_fail_title)
                .setMessage(R.string.keystore_recovery_fail_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    finishAffinity()
                    exitProcess(1)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun deletePrefsFile(name: String) {
        try {
            val file = File(applicationInfo.dataDir, "shared_prefs/$name.xml")
            if (file.exists()) file.delete()
        } catch (_: Exception) { /* best-effort */ }
    }
}
