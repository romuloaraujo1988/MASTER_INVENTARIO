package com.inventario.mobile.generators

import com.inventario.mobile.data.model.Coleta
import com.inventario.mobile.domain.model.StatusFiltro
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

/**
 * Generators (Arb) para modelos de teste
 * Usados em Property-Based Testing com Kotest
 * 
 * @see Requirements: Testing Strategy
 */
object ColetaGenerators {
    
    /**
     * Gera uma Coleta aleatória com todos os campos preenchidos
     */
    fun coleta(): Arb<Coleta> = arbitrary {
        Coleta(
            id = Arb.int(1..10000).bind(),
            patrimonioId = Arb.int(1..1000).bind(),
            usuarioId = Arb.int(1..100).bind(),
            dataColeta = Arb.long(1000000000000L..2000000000000L).bind().toString(),
            localizacaoAtual = Arb.string(5..30).orNull().bind(),
            estadoEncontrado = Arb.string(3..10).orNull().bind(),
            observacoes = Arb.string(0..100).orNull().bind(),
            fotoPath = null,
            status = "COLETADO",
            latitude = Arb.double(-90.0..90.0).orNull().bind(),
            longitude = Arb.double(-180.0..180.0).orNull().bind(),
            dataCriacao = null,
            dataAtualizacao = null,
            divergencia = Arb.boolean().bind(),
            motivoDivergencia = null,
            numeroPatrimonio = "PAT${Arb.int(10000..99999).bind()}",
            descricaoPatrimonio = Arb.string(10..50).bind(),
            nomeInventario = "Inventário ${Arb.int(2020..2025).bind()}",
            nomeSala = listOf("Sala 101", "Sala 102", "Laboratório", "Biblioteca", "Auditório", null).random(),
            nomeColetor = listOf("João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa").random(),
            sincronizado = Arb.boolean().bind(),
            tentativasSincronizacao = Arb.int(0..5).bind()
        )
    }
    
    /**
     * Gera uma Coleta com usuário específico
     */
    fun coletaComUsuario(nomeColetor: String): Arb<Coleta> = arbitrary {
        coleta().bind().copy(nomeColetor = nomeColetor)
    }
    
    /**
     * Gera uma Coleta com sala específica
     */
    fun coletaComSala(nomeSala: String?): Arb<Coleta> = arbitrary {
        coleta().bind().copy(nomeSala = nomeSala, localizacaoAtual = nomeSala)
    }
    
    /**
     * Gera uma Coleta sincronizada
     */
    fun coletaSincronizada(): Arb<Coleta> = arbitrary {
        coleta().bind().copy(sincronizado = true)
    }
    
    /**
     * Gera uma Coleta pendente (não sincronizada)
     */
    fun coletaPendente(): Arb<Coleta> = arbitrary {
        coleta().bind().copy(sincronizado = false)
    }
    
    /**
     * Gera um StatusFiltro aleatório
     */
    fun statusFiltro(): Arb<StatusFiltro> = Arb.enum<StatusFiltro>()
    
    /**
     * Gera uma lista de coletas com tamanho variável
     */
    fun listaColetas(minSize: Int = 1, maxSize: Int = 100): Arb<List<Coleta>> = 
        Arb.list(coleta(), minSize..maxSize)
    
    /**
     * Gera uma lista de coletas mistas (algumas do usuário, algumas de outros)
     */
    fun listaColetasMistas(usuarioAtual: String): Arb<List<Coleta>> = arbitrary {
        val coletas = mutableListOf<Coleta>()
        val quantidade = Arb.int(5..20).bind()
        
        repeat(quantidade) {
            val coleta = coleta().bind()
            // 50% de chance de ser do usuário atual
            val coletaFinal = if (Arb.boolean().bind()) {
                coleta.copy(nomeColetor = usuarioAtual)
            } else {
                coleta
            }
            coletas.add(coletaFinal)
        }
        
        coletas
    }
    
    /**
     * Gera uma lista de coletas com salas variadas
     */
    fun listaColetasComSalas(): Arb<List<Coleta>> = arbitrary {
        val salas = listOf("Sala 101", "Sala 102", "Laboratório", "Biblioteca", "Auditório", null)
        val coletas = mutableListOf<Coleta>()
        val quantidade = Arb.int(10..30).bind()
        
        repeat(quantidade) {
            val sala = salas.random()
            val coleta = coleta().bind().copy(nomeSala = sala, localizacaoAtual = sala)
            coletas.add(coleta)
        }
        
        coletas
    }
}
