package com.inventario.siads.service;

import com.inventario.model.Patrimonio;
import com.inventario.siads.config.SiadsConfig;
import com.inventario.siads.model.SiadsRegistro;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Serviço responsável por converter dados do sistema interno
 * para o formato SIADS conforme normas federais
 * 
 * Adequado às normas:
 * - IN SGD/ME nº 1/2019 (Gestão de Patrimônio)
 * - Decreto nº 9.373/2018 (Alienação de Bens)
 * - Manual SIADS v6.2.11
 * - NBC TSP 07 (Depreciação)
 * 
 * @version 2.0.0 - Adequação às normas federais
 */
public class SiadsConverterService {
    
    private SiadsConfig config;
    
    // Mapeamento de categorias para códigos CATMAT (exemplos)
    private static final Map<String, String> CATMAT_MAP = new HashMap<>();
    
    // Mapeamento de categorias para Elementos de Despesa (ED)
    private static final Map<String, String> ED_MAP = new HashMap<>();
    
    // Vida útil padrão por categoria (em anos) conforme NBC TSP 07
    private static final Map<String, Integer> VIDA_UTIL_MAP = new HashMap<>();
    
    static {
        // Códigos CATMAT exemplos (devem ser configurados conforme catálogo oficial)
        CATMAT_MAP.put("INFORMATICA", "254602152");
        CATMAT_MAP.put("MOBILIARIO", "234567890");
        CATMAT_MAP.put("ELETRODOMESTICO", "345678901");
        CATMAT_MAP.put("VEICULO", "456789012");
        CATMAT_MAP.put("EQUIPAMENTO_LABORATORIO", "567890123");
        CATMAT_MAP.put("OUTROS", "999999999");
        
        // Elementos de Despesa conforme SIAFI
        ED_MAP.put("INFORMATICA", "449052");
        ED_MAP.put("MOBILIARIO", "449052");
        ED_MAP.put("ELETRODOMESTICO", "449052");
        ED_MAP.put("VEICULO", "449052");
        ED_MAP.put("EQUIPAMENTO_LABORATORIO", "449052");
        ED_MAP.put("OUTROS", "449052");
        
        // Vida útil padrão conforme NBC TSP 07 e IN STN
        VIDA_UTIL_MAP.put("INFORMATICA", 5);
        VIDA_UTIL_MAP.put("MOBILIARIO", 10);
        VIDA_UTIL_MAP.put("ELETRODOMESTICO", 10);
        VIDA_UTIL_MAP.put("VEICULO", 5);
        VIDA_UTIL_MAP.put("EQUIPAMENTO_LABORATORIO", 10);
        VIDA_UTIL_MAP.put("OUTROS", 10);
    }
    
    public SiadsConverterService() {
        this.config = new SiadsConfig();
    }
    
    public SiadsConverterService(SiadsConfig config) {
        this.config = config;
    }
    
    /**
     * Converte um patrimônio do sistema para registro SIADS
     * Usa campos transientes carregados pelo SiadsPatrimonioDAO
     * 
     * Mapeamento conforme Manual SIADS v6.2.11
     */
    public SiadsRegistro converterPatrimonio(Patrimonio patrimonio) {
        SiadsRegistro registro = new SiadsRegistro();
        
        // ========================================
        // IDENTIFICAÇÃO (Campos Obrigatórios)
        // ========================================
        registro.setNumeroPatrimonio(patrimonio.getNumero());
        registro.setDescricao(patrimonio.getDescricao());
        registro.setEspecificacao(patrimonio.getRotulos());
        
        // Código do material (P + número do patrimônio)
        registro.setCodigoMaterial("P" + patrimonio.getNumero());
        
        // ========================================
        // CLASSIFICAÇÃO (CATMAT/ED)
        // ========================================
        String categoria = patrimonio.getCategoria();
        if (categoria == null) categoria = "OUTROS";
        
        // Código CATMAT baseado na categoria
        registro.setCodigoCatmat(CATMAT_MAP.getOrDefault(categoria, "999999999"));
        registro.setClasseContabil(categoria);
        
        // Elemento de Despesa (ED) conforme SIAFI
        registro.setElementoDespesa(ED_MAP.getOrDefault(categoria, "449052"));
        
        // ========================================
        // VALORES (Conforme normas contábeis)
        // ========================================
        registro.setValorAquisicao(patrimonio.getValorAquisicao());
        registro.setValorDepreciado(patrimonio.getValorDepreciado());
        
        // Calcular valor líquido contábil
        if (patrimonio.getValorAquisicao() != null) {
            BigDecimal valorDepreciado = patrimonio.getValorDepreciado();
            if (valorDepreciado == null) valorDepreciado = BigDecimal.ZERO;
            registro.setValorLiquido(patrimonio.getValorAquisicao().subtract(valorDepreciado));
        }
        
        // ========================================
        // DATAS
        // ========================================
        if (patrimonio.getDataEntrada() != null) {
            registro.setDataAquisicao(patrimonio.getDataEntrada().toLocalDate());
            // Data de incorporação = data de entrada (se não houver específica)
            registro.setDataIncorporacao(patrimonio.getDataEntrada().toLocalDate());
        }
        
        // ========================================
        // LOCALIZAÇÃO (Estrutura Organizacional)
        // ========================================
        registro.setSala(patrimonio.getNomeSala());
        registro.setSetor(patrimonio.getNomeSetor());
        
        // Código UOrg: usa código configurado ou ID da sala como fallback
        String codigoUOrg = patrimonio.getCodigoUOrg();
        if (codigoUOrg == null || codigoUOrg.trim().isEmpty()) {
            codigoUOrg = String.format("%07d", patrimonio.getIdSala());
        }
        registro.setCodigoUorg(codigoUOrg);
        registro.setUnidadeGestora(config.getCodigoUG());
        registro.setOrgao(config.getCodigoOrgao());
        
        // ========================================
        // RESPONSÁVEL (Termo de Responsabilidade)
        // ========================================
        registro.setCpfResponsavel(patrimonio.getCpfResponsavel());
        registro.setNomeResponsavel(patrimonio.getNomeResponsavel());
        registro.setMatriculaResponsavel(patrimonio.getMatriculaResponsavel());
        
        // ========================================
        // SITUAÇÃO DO BEM
        // ========================================
        registro.setSituacaoBem(converterSituacao(patrimonio.getStatus()));
        registro.setEstadoConservacao(converterEstadoConservacao(patrimonio.getEstadoConservacao()));
        
        // ========================================
        // ORIGEM/AQUISIÇÃO
        // ========================================
        registro.setTipoAquisicao(SiadsRegistro.TipoAquisicao.COMPRA); // Padrão
        registro.setFormaAquisicao("COMPRA");
        registro.setNumeroNotaFiscal(patrimonio.getNumeroNotaFiscal());
        registro.setFornecedor(patrimonio.getFornecedor());
        
        // ========================================
        // DETALHES TÉCNICOS
        // ========================================
        registro.setGrupoMaterial(patrimonio.getMarca());
        registro.setModelo(patrimonio.getModelo());
        registro.setFabricante(patrimonio.getFornecedor());
        registro.setNumeroSerie(patrimonio.getNumeroSerie());
        
        // ========================================
        // DEPRECIAÇÃO (NBC TSP 07)
        // ========================================
        int vidaUtil = VIDA_UTIL_MAP.getOrDefault(categoria, 10);
        registro.setVidaUtilAnos(vidaUtil);
        registro.setTaxaDepreciacao(new BigDecimal(100.0 / vidaUtil));
        registro.setMetodoDepreciacao("LINEAR");
        
        // ========================================
        // BAIXA (se aplicável)
        // ========================================
        boolean isBaixado = "BAIXADO".equalsIgnoreCase(patrimonio.getStatus());
        registro.setBaixado(isBaixado);
        
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
    
    /**
     * Converte situação do sistema para código SIADS
     */
    private String converterSituacao(String situacao) {
        if (situacao == null) return SiadsRegistro.SituacaoBem.EM_USO;
        
        switch (situacao.toUpperCase()) {
            case "ATIVO":
            case "EM USO":
            case "EM_USO":
                return SiadsRegistro.SituacaoBem.EM_USO;
            case "OCIOSO":
            case "INATIVO":
                return SiadsRegistro.SituacaoBem.OCIOSO;
            case "BAIXADO":
                return SiadsRegistro.SituacaoBem.BAIXADO;
            default:
                return SiadsRegistro.SituacaoBem.EM_USO;
        }
    }
    
    /**
     * Converte estado de conservação para código SIADS
     */
    private String converterEstadoConservacao(String estado) {
        if (estado == null) return SiadsRegistro.EstadoConservacao.BOM;
        
        switch (estado.toUpperCase()) {
            case "ÓTIMO":
            case "OTIMO":
                return SiadsRegistro.EstadoConservacao.OTIMO;
            case "BOM":
                return SiadsRegistro.EstadoConservacao.BOM;
            case "REGULAR":
                return SiadsRegistro.EstadoConservacao.REGULAR;
            case "RUIM":
                return SiadsRegistro.EstadoConservacao.RUIM;
            case "PÉSSIMO":
            case "PESSIMO":
                return SiadsRegistro.EstadoConservacao.PESSIMO;
            default:
                return SiadsRegistro.EstadoConservacao.BOM;
        }
    }
    
    /**
     * Configura mapeamento CATMAT personalizado
     */
    public void configurarCatmat(String categoria, String codigoCatmat) {
        CATMAT_MAP.put(categoria, codigoCatmat);
    }
    
    /**
     * Configura mapeamento de Elemento de Despesa personalizado
     */
    public void configurarElementoDespesa(String categoria, String ed) {
        ED_MAP.put(categoria, ed);
    }
    
    /**
     * Configura vida útil personalizada por categoria
     */
    public void configurarVidaUtil(String categoria, int anos) {
        VIDA_UTIL_MAP.put(categoria, anos);
    }
}
