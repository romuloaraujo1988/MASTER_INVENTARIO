package com.inventario.mobile.domain.usecase

import com.inventario.mobile.domain.model.Coleta
import com.inventario.mobile.domain.repository.ColetaRepository
import com.inventario.mobile.domain.repository.PatrimonioRepository
import com.inventario.mobile.data.local.LocalDataManager
import javax.inject.Inject

/**
 * Use Case: Registrar uma coleta
 * Contém lógica de negócio para validação e registro de coletas
 * 
 * v2.7: Suporte a itens sem etiqueta
 */
class RegistrarColetaUseCase @Inject constructor(
    private val coletaRepository: ColetaRepository,
    private val patrimonioRepository: PatrimonioRepository,
    private val localDataManager: LocalDataManager
) {
    /**
     * Registra coleta de patrimônio COM etiqueta
     */
    suspend operator fun invoke(
        numeroPatrimonio: String,
        salaId: Int? = null,
        localizacaoAtual: String?,
        estadoEncontrado: String? = null,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        idUsuario: Long? = null,
        tempoColetaSegundos: Int? = null,
        tempoScanSegundos: Int? = null,
        tempoPreenchimentoSegundos: Int? = null,
        metodoColeta: String? = null,
        tipoScan: String? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (numeroPatrimonio.isBlank()) {
                return Result.failure(Exception("Número do patrimônio é obrigatório"))
            }
            
            // 2. Obter usuário atual (CRÍTICO para modo offline)
            val usuarioAtual = localDataManager.getCurrentUser()
            if (usuarioAtual == null) {
                return Result.failure(Exception("[USUARIO_NAO_IDENTIFICADO] Usuário não está logado. Faça login novamente."))
            }
            
            val usuarioIdFinal = idUsuario ?: usuarioAtual.id.toLong()
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Usuário identificado: ${usuarioAtual.nome} (ID: $usuarioIdFinal)")
            
            // 3. Buscar patrimônio
            val patrimonio = patrimonioRepository.buscarPorNumero(numeroPatrimonio)
                ?: return Result.failure(Exception("Patrimônio não encontrado"))
            
            // 4. Detectar divergência automaticamente (v2.13)
            val salaPatrimonioCadastrada = patrimonio.idSala
            val divergenciaLocalizacao = salaId != null
                && salaPatrimonioCadastrada != null
                && salaId != salaPatrimonioCadastrada

            val estadoCadastrado = patrimonio.estado?.uppercase()?.trim()
            val estadoColetado = estadoEncontrado?.uppercase()?.trim()
            val divergenciaEstado = estadoCadastrado != null
                && estadoColetado != null
                && estadoCadastrado != estadoColetado

            val temDivergencia = divergenciaLocalizacao || divergenciaEstado
            val motivoDivergencia = when {
                divergenciaLocalizacao && divergenciaEstado -> "LOCALIZACAO_E_ESTADO"
                divergenciaLocalizacao -> "LOCALIZACAO_DIFERENTE"
                divergenciaEstado -> "ESTADO_DIFERENTE"
                else -> null
            }

            if (temDivergencia) {
                android.util.Log.w("RegistrarColetaUseCase",
                    "⚠️ DIVERGÊNCIA detectada para ${patrimonio.numeroPatrimonio}: motivo=$motivoDivergencia" +
                    " | Sala cadastrada=${patrimonio.nomeSala}(${salaPatrimonioCadastrada}) vs coletada=$salaId" +
                    " | Estado cadastrado=$estadoCadastrado vs coletado=$estadoColetado")
            }

            // 5. Criar coleta com campos de divergência
            val coleta = Coleta(
                id = 0,
                patrimonioId = patrimonio.id.toLong(),
                numeroPatrimonio = numeroPatrimonio,
                usuarioId = usuarioIdFinal,
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = observacoes,
                status = "COLETADO",
                estadoEncontrado = estadoEncontrado ?: "BOM",
                latitude = latitude,
                longitude = longitude,
                sincronizado = false,
                tempoColetaSegundos = tempoColetaSegundos,
                tempoScanSegundos = tempoScanSegundos,
                tempoPreenchimentoSegundos = tempoPreenchimentoSegundos,
                metodoColeta = metodoColeta,
                tipoScan = tipoScan,
                divergencia = temDivergencia,
                motivoDivergencia = motivoDivergencia
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta criada: Patrimônio ${coleta.numeroPatrimonio}, Usuário ${coleta.usuarioId}, Sala ${coleta.salaId}")
            
            // 5. Registrar coleta
            coletaRepository.registrarColeta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar coleta", e)
            Result.failure(e)
        }
    }
    
    /**
     * v2.7: Registra coleta de item SEM etiqueta
     */
    suspend fun registrarItemSemEtiqueta(
        descricao: String,
        categoria: String,
        salaId: Int? = null,
        localizacaoAtual: String?,
        estadoEncontrado: String,
        observacoes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        fotoBase64: String? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição do item é obrigatória"))
            }
            
            // 2. Obter usuário atual
            val usuarioAtual = localDataManager.getCurrentUser()
            if (usuarioAtual == null) {
                return Result.failure(Exception("[USUARIO_NAO_IDENTIFICADO] Usuário não está logado. Faça login novamente."))
            }
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Registrando item SEM ETIQUETA: $descricao")
            
            // 3. Criar coleta para item sem etiqueta
            val coleta = Coleta(
                id = 0,
                patrimonioId = 0,  // Sem patrimônio associado
                numeroPatrimonio = null,  // Sem número
                descricaoPatrimonio = descricao,  // Descrição do item
                usuarioId = usuarioAtual.id.toLong(),
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = localizacaoAtual,
                observacoes = "Categoria: $categoria | $observacoes",
                status = "COLETADO",  // Status da coleta
                estadoEncontrado = estadoEncontrado,  // Estado de conservação
                latitude = latitude,
                longitude = longitude,
                sincronizado = false,
                // v2.7: Campos específicos para item sem etiqueta
                semEtiqueta = true,
                descricaoItemSemEtiqueta = descricao,
                categoriaItemSemEtiqueta = categoria,
                fotoPath = fotoBase64
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta SEM ETIQUETA criada: $descricao, Categoria: $categoria")
            
            // 4. Registrar coleta
            coletaRepository.registrarColetaSemEtiqueta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar item sem etiqueta", e)
            Result.failure(e)
        }
    }
    
    /**
     * ✅ NOVO: Registra coleta por descrição (item similar sem etiqueta)
     * Usado quando o coletor encontra um item igual ao anterior mas sem etiqueta
     */
    suspend fun registrarColetaPorDescricao(
        descricao: String,
        salaId: Int,
        salaNome: String,
        estadoConservacao: String,
        inventarioId: Int,
        usuarioId: Long,
        usuarioNome: String,
        observacoes: String? = null
    ): Result<Coleta> {
        return try {
            // 1. Validar entrada
            if (descricao.isBlank()) {
                return Result.failure(Exception("Descrição do item é obrigatória"))
            }
            
            android.util.Log.d("RegistrarColetaUseCase", "═══════════════════════════════════════")
            android.util.Log.d("RegistrarColetaUseCase", "✓ Registrando coleta por DESCRIÇÃO (similar)")
            android.util.Log.d("RegistrarColetaUseCase", "  Descrição: $descricao")
            android.util.Log.d("RegistrarColetaUseCase", "  Sala: $salaNome (ID: $salaId)")
            android.util.Log.d("RegistrarColetaUseCase", "  Estado: $estadoConservacao")
            android.util.Log.d("RegistrarColetaUseCase", "  Usuário: $usuarioNome (ID: $usuarioId)")
            android.util.Log.d("RegistrarColetaUseCase", "═══════════════════════════════════════")
            
            // 2. Criar coleta para item similar (sem número de patrimônio)
            val coleta = Coleta(
                id = 0,
                patrimonioId = 0,  // Sem patrimônio específico
                numeroPatrimonio = null,  // Sem número
                descricaoPatrimonio = descricao,
                usuarioId = usuarioId,
                salaId = salaId,
                dataColeta = System.currentTimeMillis(),
                localizacaoAtual = salaNome,
                estadoEncontrado = estadoConservacao,
                observacoes = observacoes ?: "Coleta similar - item sem etiqueta",
                status = "COLETADO",
                sincronizado = false,
                // Campos para item sem etiqueta
                semEtiqueta = true,
                descricaoItemSemEtiqueta = descricao
            )
            
            android.util.Log.d("RegistrarColetaUseCase", "✓ Coleta por descrição criada, salvando...")
            
            // 3. Registrar coleta
            coletaRepository.registrarColetaSemEtiqueta(coleta)
            
        } catch (e: Exception) {
            android.util.Log.e("RegistrarColetaUseCase", "❌ Erro ao registrar coleta por descrição", e)
            Result.failure(e)
        }
    }
}
