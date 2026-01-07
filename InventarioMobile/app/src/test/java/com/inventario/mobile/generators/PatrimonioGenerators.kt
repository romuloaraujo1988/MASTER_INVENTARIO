package com.inventario.mobile.generators

import com.inventario.mobile.domain.model.Patrimonio
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

/**
 * Generators (Arb) para modelos de Patrimônio
 * Usados em Property-Based Testing com Kotest
 */
object PatrimonioGenerators {
    
    /**
     * Gera um Patrimônio aleatório com todos os campos preenchidos
     */
    fun patrimonio(): Arb<Patrimonio> = arbitrary {
        Patrimonio(
            id = Arb.int(1..10000).bind(),
            numeroPatrimonio = "PAT${Arb.int(10000..99999).bind()}",
            descricao = Arb.string(10..100).orNull().bind(),
            marca = Arb.string(3..20).orNull().bind(),
            modelo = Arb.string(3..20).orNull().bind(),
            numeroSerie = Arb.string(5..20).orNull().bind(),
            estado = listOf("BOM", "OCIOSO", "RECUPERAVEL", "ANTIECONOMICO", "IRRECUPERAVEL").random(),
            valor = Arb.double(100.0..50000.0).orNull().bind(),
            dataAquisicao = Arb.long(1000000000000L..1700000000000L).orNull().bind(),
            observacoes = Arb.string(0..200).orNull().bind(),
            idSala = Arb.int(1..100).orNull().bind(),
            nomeSala = listOf("Sala 101", "Sala 102", "Laboratório", "Biblioteca", "Auditório").random(),
            idResponsavel = Arb.int(1..50).orNull().bind(),
            nomeResponsavel = listOf("João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa").random(),
            idSetor = Arb.int(1..20).orNull().bind(),
            nomeSetor = listOf("Administrativo", "Acadêmico", "TI", "Biblioteca").random(),
            qrCode = "QR${Arb.int(100000..999999).bind()}",
            sincronizado = Arb.boolean().bind(),
            dataCriacao = System.currentTimeMillis(),
            dataAtualizacao = System.currentTimeMillis(),
            servidorId = Arb.long(1L..100000L).orNull().bind(),
            coletado = Arb.boolean().bind(),
            dataColeta = if (Arb.boolean().bind()) "01/12/2025 10:30" else null,
            coletorId = Arb.long(1L..100L).orNull().bind(),
            coletadoPor = listOf("João Silva", "Maria Santos", "Pedro Oliveira", null).random(),
            dataColetaFormatada = null,
            localizacaoEncontrada = Arb.string(5..30).orNull().bind(),
            estadoEncontrado = listOf("BOM", "OCIOSO", "RECUPERAVEL", "ANTIECONOMICO", "IRRECUPERAVEL", null).random()
        )
    }
    
    /**
     * Gera um Patrimônio coletado
     */
    fun patrimonioColetado(): Arb<Patrimonio> = arbitrary {
        patrimonio().bind().copy(
            coletado = true,
            dataColeta = "01/12/2025 10:30",
            coletadoPor = "João Silva"
        )
    }
    
    /**
     * Gera um Patrimônio não coletado
     */
    fun patrimonioNaoColetado(): Arb<Patrimonio> = arbitrary {
        patrimonio().bind().copy(
            coletado = false,
            dataColeta = null,
            coletadoPor = null
        )
    }
    
    /**
     * Gera um Patrimônio com sala específica
     */
    fun patrimonioComSala(salaId: Int, nomeSala: String): Arb<Patrimonio> = arbitrary {
        patrimonio().bind().copy(idSala = salaId, nomeSala = nomeSala)
    }
    
    /**
     * Gera um Patrimônio com caracteres especiais na descrição
     * Útil para testar encoding no PDF
     */
    fun patrimonioComCaracteresEspeciais(): Arb<Patrimonio> = arbitrary {
        val descricoes = listOf(
            "Cadeira Giratória - Ergonômica",
            "Mesa de Reunião 2,5m × 1,2m",
            "Computador Dell™ Optiplex",
            "Ar Condicionado 12.000 BTU's",
            "Projetor Epson® PowerLite",
            "Impressora HP LaserJet Pro M404n",
            "Monitor LG 24\" Full HD",
            "Notebook Lenovo ThinkPad™ E14",
            "Câmera de Segurança IP 360°",
            "Roteador Wi-Fi 6 (802.11ax)"
        )
        patrimonio().bind().copy(descricao = descricoes.random())
    }
    
    /**
     * Gera uma lista de patrimônios com tamanho variável
     */
    fun listaPatrimonios(minSize: Int = 1, maxSize: Int = 100): Arb<List<Patrimonio>> =
        Arb.list(patrimonio(), minSize..maxSize)
    
    /**
     * Gera uma lista de patrimônios mista (alguns coletados, alguns não)
     */
    fun listaPatrimoniosMista(): Arb<List<Patrimonio>> = arbitrary {
        val patrimonios = mutableListOf<Patrimonio>()
        val quantidade = Arb.int(10..50).bind()
        
        repeat(quantidade) {
            val p = patrimonio().bind()
            // 50% de chance de ser coletado
            val pFinal = if (Arb.boolean().bind()) {
                p.copy(coletado = true, dataColeta = "01/12/2025 10:30", coletadoPor = "João Silva")
            } else {
                p.copy(coletado = false, dataColeta = null, coletadoPor = null)
            }
            patrimonios.add(pFinal)
        }
        
        patrimonios
    }
    
    /**
     * Gera uma lista grande de patrimônios para testar múltiplas páginas no PDF
     */
    fun listaPatrimoniosGrande(): Arb<List<Patrimonio>> =
        Arb.list(patrimonio(), 100..200)
}
