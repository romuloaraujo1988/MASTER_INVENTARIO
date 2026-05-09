package com.inventario.mobile.presentation.coleta

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.inventario.mobile.R
import com.inventario.mobile.presentation.descricao.DescricaoSelectionActivity
import com.inventario.mobile.presentation.descricao.DescricaoSelectionViewModelClean
import com.inventario.mobile.presentation.descricao.ColetaState
import com.inventario.mobile.presentation.dialog.EstadoPatrimonioDialog
import com.inventario.mobile.utils.SoundUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Tela intermediária para escolha do método de identificação de patrimônio.
 * 
 * Oferece opção de busca manual - Abre lista de descrições para busca tradicional.
 * 
 * Após a seleção da descrição, o fluxo continua
 * para seleção de sala e confirmação da coleta.
 */
@AndroidEntryPoint
class EscolhaMetodoColetaActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EscolhaMetodoColeta"
        const val REQUEST_CODE_MANUAL = 1002
        const val EXTRA_DESCRICAO_SELECIONADA = "descricao_selecionada"
        const val EXTRA_SALA_ID = "SALA_ID"
        const val EXTRA_SALA_NOME = "SALA_NOME"
    }

    private var salaId: Long? = null
    private var salaNome: String? = null
    
    // ViewModel para registrar coleta
    private val viewModel: DescricaoSelectionViewModelClean by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escolha_metodo_coleta)

        // Recuperar extras da sala
        salaId = intent.getLongExtra(EXTRA_SALA_ID, -1L).takeIf { it != -1L }
        salaNome = intent.getStringExtra(EXTRA_SALA_NOME)
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Sala recebida: ID=$salaId, Nome=$salaNome")
        Log.d(TAG, "═══════════════════════════════════════")

        setupToolbar()
        setupButtons()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = "Coletar por Descrição"
            subtitle = salaNome ?: ""
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupButtons() {
        // Card/Botão para busca manual
        findViewById<MaterialCardView>(R.id.cardBuscaManual)?.setOnClickListener {
            abrirBuscaManual()
        }
        findViewById<MaterialButton>(R.id.btnBuscaManual)?.setOnClickListener {
            abrirBuscaManual()
        }
    }
    
    /**
     * Observa o estado da coleta para feedback ao usuário
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.coletaState.collect { state ->
                when (state) {
                    is ColetaState.Idle -> { /* nada */ }
                    is ColetaState.Loading -> {
                        // Mostrar loading se necessário
                    }
                    is ColetaState.Success -> {
                        SoundUtils.playSuccessSound()
                        Toast.makeText(
                            this@EscolhaMetodoColetaActivity,
                            "✓ Coleta registrada com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.limparColetaState()
                        // Voltar para tela anterior após sucesso
                        finish()
                    }
                    is ColetaState.Error -> {
                        Toast.makeText(
                            this@EscolhaMetodoColetaActivity,
                            "Erro: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.limparColetaState()
                    }
                }
            }
        }
    }

    private fun abrirBuscaManual() {
        Log.d(TAG, "Abrindo Busca Manual com sala: ID=$salaId, Nome=$salaNome")
        val intent = Intent(this, DescricaoSelectionActivity::class.java).apply {
            // Passar como Long pois DescricaoSelectionActivity espera Long
            salaId?.let { 
                putExtra(DescricaoSelectionActivity.EXTRA_SALA_ID, it)
                Log.d(TAG, "Passando SALA_ID=${DescricaoSelectionActivity.EXTRA_SALA_ID} com valor=$it")
            }
            salaNome?.let { 
                putExtra(DescricaoSelectionActivity.EXTRA_SALA_NOME, it)
                Log.d(TAG, "Passando SALA_NOME=${DescricaoSelectionActivity.EXTRA_SALA_NOME} com valor=$it")
            }
        }
        startActivityForResult(intent, REQUEST_CODE_MANUAL)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            val descricaoSelecionada = data?.getStringExtra(EXTRA_DESCRICAO_SELECIONADA)
                ?: data?.getStringExtra("descricao_selecionada")
                ?: data?.getStringExtra("DESCRICAO_SELECIONADA")

            if (!descricaoSelecionada.isNullOrBlank()) {
                Log.d(TAG, "Descrição selecionada: $descricaoSelecionada (requestCode=$requestCode)")
                
                when (requestCode) {
                    REQUEST_CODE_MANUAL -> {
                        // Veio da busca manual - a DescricaoSelectionActivity já tratou
                        // Apenas finalizar esta activity
                        finish()
                    }
                    else -> {
                        // Fallback - repassar para activity chamadora
                        continuarParaSelecaoSala(descricaoSelecionada)
                    }
                }
            }
        }
    }

    private fun continuarParaSelecaoSala(descricao: String) {
        // Retorna a descrição para a activity chamadora
        // que irá continuar o fluxo de coleta
        val resultIntent = Intent().apply {
            putExtra(EXTRA_DESCRICAO_SELECIONADA, descricao)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
