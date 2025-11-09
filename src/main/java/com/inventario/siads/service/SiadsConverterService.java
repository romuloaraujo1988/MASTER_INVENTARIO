package com.inventario.siads.service;

import com.inventario.model.Patrimonio;
import com.inventario.siads.model.SiadsRegistro;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável por converter dados do sistema interno
 * para o formato SIADS
 */
public class SiadsConverterService {
    
    /**
     * Converte um patrimônio do sistema para registro SIADS
     * Usa campos transientes carregados pelo SiadsPatrimonioDAO
     */
    public SiadsRegistro converterPatrimonio(Patrimonio patrimonio) {
        SiadsRegistro registro = new SiadsRegistro();
        
        // Identificação
        registro.setNumeroPatrimonio(patrimonio.getNumero());
        registro.setDescricao(patrimonio.getDescricao());
        registro.setEspecificacao(patrimonio.getRotulos()); // Usando rótulos como especificação
        
        // Código do material (usar número do patrimônio como base)
        registro.setCodigoMaterial("P" + patrimonio.getNumero());
        
        // Valores
        registro.setValorAquisicao(patrimonio.getValorAquisicao());
        registro.setValorDepreciado(patrimonio.getValorDepreciado());
        
        // Datas
        if (patrimonio.getDataEntrada() != null) {
            registro.setDataAquisicao(patrimonio.getDataEntrada().toLocalDate());
        }
        
        // Localização (usa campos transientes carregados pelo DAO)
        registro.setSala(patrimonio.getNomeSala());
        registro.setSetor(patrimonio.getNomeSetor());
        
        // Código UOrg: usa código configurado ou ID da sala como fallback
        String codigoUOrg = patrimonio.getCodigoUOrg();
        if (codigoUOrg == null || codigoUOrg.trim().isEmpty()) {
            codigoUOrg = String.format("%07d", patrimonio.getIdSala()); // Formata com 7 dígitos
        }
        registro.setUnidadeGestora(codigoUOrg);
        
        // Responsável (usa campos transientes carregados pelo DAO)
        registro.setCpfResponsavel(patrimonio.getCpfResponsavel());
        registro.setNomeResponsavel(patrimonio.getNomeResponsavel());
        registro.setMatriculaResponsavel(patrimonio.getMatriculaResponsavel());
        
        // Situação
        registro.setSituacaoBem(patrimonio.getStatus());
        registro.setEstadoConservacao(patrimonio.getEstadoConservacao());
        
        // Outros campos
        registro.setGrupoMaterial(patrimonio.getMarca());
        registro.setModelo(patrimonio.getModelo());
        registro.setFabricante(patrimonio.getFornecedor()); // Usando fornecedor como fabricante
        registro.setFormaAquisicao("COMPRA"); // Padrão
        registro.setNumeroNotaFiscal(patrimonio.getNumeroNotaFiscal());
        registro.setFornecedor(patrimonio.getFornecedor());
        
        // Classificação contábil (pode ser configurado conforme necessidade)
        registro.setClasseContabil(patrimonio.getCategoria());
        
        return registro;
    }
    
    /**
     * Converte uma lista de patrimônios
     */
    public List<SiadsRegistro> converterPatrimonios(List<Patrimonio> patrimonios) {
        return patrimonios.stream()
                .map(this::converterPatrimonio)
                .collect(Collectors.toList());
    }
}
