package com.inventario.mobile.presentation.validation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment de exemplo mostrando como usar o ValidationViewModel
 * 
 * Este é um exemplo de implementação que pode ser adaptado
 * para as telas de coleta existentes (ColetaActivity, ManualCollectionActivity, etc)
 */
@AndroidEntryPoint
class ValidationExampleFragment : Fragment() {
    
    private val viewModel: ValidationViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar layout aqui
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.validationState.collect { state ->
                when (state) {
                    is ValidationState.Idle -> {
                        hideLoading()
                    }
                    
                    is ValidationState.Loading -> {
                        showLoading()
                    }
                    
                    is ValidationState.Valid -> {
                        hideLoading()
                        handleValidPatrimonio(state.data, state.jaColetado)
                    }
                    
                    is ValidationState.Invalid -> {
                        hideLoading()
                        handleInvalidPatrimonio(state.motivo, state.mensagem)
                    }
                    
                    is ValidationState.PodeRegistrar -> {
                        hideLoading()
                        handlePodeRegistrar()
                    }
                    
                    is ValidationState.Duplicado -> {
                        hideLoading()
                        handleDuplicado(state.mensagem, state.coletaExistente)
                    }
                    
                    is ValidationState.NaoPodeRegistrar -> {
                        hideLoading()
                        handleNaoPodeRegistrar(state.motivo, state.mensagem)
                    }
                    
                    is ValidationState.JaColetado -> {
                        hideLoading()
                        handleJaColetado(state.info)
                    }
                    
                    is ValidationState.AindaNaoColetado -> {
                        hideLoading()
                        handleAindaNaoColetado(state.info)
                    }
                    
                    is ValidationState.Error -> {
                        hideLoading()
                        showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun setupListeners() {
        // Exemplo: Validar ao escanear QR Code
        // btnScan.setOnClickListener {
        //     val numeroPatrimonio = getScannedCode()
        //     viewModel.validarPatrimonio(numeroPatrimonio)
        // }
        
        // Exemplo: Verificar duplicata antes de registrar
        // btnRegistrar.setOnClickListener {
        //     val numeroPatrimonio = edtNumero.text.toString()
        //     viewModel.verificarDuplicata(numeroPatrimonio)
        // }
        
        // Exemplo: Verificar se já foi coletado
        // btnVerificar.setOnClickListener {
        //     val numeroPatrimonio = edtNumero.text.toString()
        //     viewModel.verificarSeJaFoiColetado(numeroPatrimonio)
        // }
    }
    
    /**
     * Patrimônio válido - mostrar dados e permitir coleta
     */
    private fun handleValidPatrimonio(data: Map<String, Any>, jaColetado: Boolean) {
        // Extrair dados do patrimônio
        val patrimonio = data["patrimonio"] as? Map<String, Any>
        val codigo = patrimonio?.get("codigo") as? String
        val descricao = patrimonio?.get("descricao") as? String
        val sala = patrimonio?.get("salaNome") as? String
        
        // Mostrar dados na UI
        // tvCodigo.text = codigo
        // tvDescricao.text = descricao
        // tvSala.text = sala
        
        if (jaColetado) {
            // Mostrar aviso de que já foi coletado
            val coletadoPor = data["coletadoPor"] as? String
            val dataColeta = data["dataColeta"] as? String
            
            showWarning("⚠️ Este patrimônio já foi coletado por $coletadoPor em $dataColeta")
            
            // Ainda permitir registro (pode ser recoleta)
            // btnRegistrar.isEnabled = true
            // btnRegistrar.text = "Registrar Novamente"
        } else {
            // Patrimônio válido e não coletado
            showSuccess("✅ Patrimônio válido e pode ser coletado")
            // btnRegistrar.isEnabled = true
            // btnRegistrar.text = "Registrar Coleta"
        }
    }
    
    /**
     * Patrimônio inválido - mostrar erro e não permitir coleta
     */
    private fun handleInvalidPatrimonio(motivo: String, mensagem: String) {
        when (motivo) {
            "PATRIMONIO_NAO_ENCONTRADO" -> {
                showError("❌ Patrimônio não encontrado no sistema")
            }
            "PATRIMONIO_INATIVO" -> {
                showError("❌ Patrimônio está inativo ou baixado")
            }
            else -> {
                showError("❌ $mensagem")
            }
        }
        
        // Desabilitar botão de registro
        // btnRegistrar.isEnabled = false
    }
    
    /**
     * Pode registrar - não é duplicata
     */
    private fun handlePodeRegistrar() {
        showSuccess("✅ Patrimônio pode ser coletado")
        // Prosseguir com registro
        // registrarColeta()
    }
    
    /**
     * Coleta duplicada - mostrar alerta
     */
    private fun handleDuplicado(mensagem: String, coletaExistente: Map<String, Any>?) {
        val coletadoPor = coletaExistente?.get("coletadoPor") as? String
        val dataColeta = coletaExistente?.get("dataColeta") as? String
        val localizacao = coletaExistente?.get("localizacao") as? String
        
        val detalhes = """
            ⚠️ COLETA DUPLICADA
            
            Este patrimônio já foi coletado:
            • Por: $coletadoPor
            • Em: $dataColeta
            • Local: $localizacao
            
            Deseja registrar novamente?
        """.trimIndent()
        
        // Mostrar dialog de confirmação
        showConfirmationDialog(
            title = "Coleta Duplicada",
            message = detalhes,
            onConfirm = {
                // Permitir registro mesmo sendo duplicata
                // registrarColeta(forceDuplicate = true)
            },
            onCancel = {
                // Cancelar registro
                viewModel.clearState()
            }
        )
    }
    
    /**
     * Não pode registrar por outro motivo
     */
    private fun handleNaoPodeRegistrar(motivo: String, mensagem: String) {
        showError("❌ $mensagem")
        // btnRegistrar.isEnabled = false
    }
    
    /**
     * Patrimônio já foi coletado - mostrar informações
     */
    private fun handleJaColetado(info: com.inventario.mobile.domain.usecase.ColetaInfo.Coletado) {
        val detalhes = """
            ✅ PATRIMÔNIO JÁ COLETADO
            
            Número: ${info.numeroPatrimonio}
            Coletado por: ${info.coletadoPor}
            Data: ${info.dataColeta}
            Local encontrado: ${info.localizacaoEncontrada}
            Estado: ${info.estadoEncontrado}
            ${info.observacoes?.let { "Observações: $it" } ?: ""}
        """.trimIndent()
        
        showInfo(detalhes)
    }
    
    /**
     * Patrimônio ainda não foi coletado
     */
    private fun handleAindaNaoColetado(info: com.inventario.mobile.domain.usecase.ColetaInfo.NaoColetado) {
        showInfo("ℹ️ Patrimônio ${info.numeroPatrimonio} ainda não foi coletado no inventário ${info.inventarioNome}")
    }
    
    // Métodos auxiliares de UI
    
    private fun showLoading() {
        // progressBar.visibility = View.VISIBLE
        // btnRegistrar.isEnabled = false
    }
    
    private fun hideLoading() {
        // progressBar.visibility = View.GONE
    }
    
    private fun showSuccess(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(android.R.color.holo_green_dark, null))
                .show()
        }
    }
    
    private fun showWarning(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(android.R.color.holo_orange_dark, null))
                .show()
        }
    }
    
    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
                .show()
        }
    }
    
    private fun showInfo(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .show()
        }
    }
    
    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Não") { _, _ -> onCancel() }
            .setCancelable(false)
            .show()
    }
}
