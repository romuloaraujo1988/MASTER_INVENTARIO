package com.inventario.sihcp.siads.util;

import com.inventario.sihcp.siads.model.SiadsRegistro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

/**
 * Utilitário para formatação de registros no layout SIADS oficial
 * 
 * Formato conforme Guia de Orientações Gerais para Geração dos Arquivos de Implantação v6.2.11:
 * - Delimitador de campo: ¥ (símbolo do iene)
 * - Delimitador de linha: £ (símbolo da libra esterlina)
 * - Encoding: UTF-8
 * - Estrutura: Header → Details → Trailer
 */
public class SiadsLayoutFormatter {
    
    // Delimitadores oficiais SIADS
    private static final String DELIMITADOR_CAMPO = "¥";  // Iene japonês
    private static final String DELIMITADOR_LINHA = "£";  // Libra esterlina
    
    // Formatadores de data conforme padrão SIADS
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    
    // Códigos de órgão e UG (devem ser configurados por instituição)
    private String codigoOrgao = "25000";  // Código do órgão (ex: IFMT)
    private String codigoUG = "00001";     // Código da Unidade Gestora
    private String cpfResponsavelSistema = "00000000000"; // CPF do responsável pelo sistema
    
    /**
     * Formata o cabeçalho (Header) do arquivo SIADS
     * Formato: H¥PE¥versao¥codigoOrgao¥codigoUG¥cpfResponsavel¥codigoUGDestino¥£
     * 
     * @return Linha de cabeçalho formatada
     */
    public String formatarCabecalho() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("H");                          // Tipo: Header
        sb.append(DELIMITADOR_CAMPO);
        sb.append("PE");                         // Tipo de arquivo: Material Permanente
        sb.append(DELIMITADOR_CAMPO);
        sb.append("1");                          // Versão do arquivo
        sb.append(DELIMITADOR_CAMPO);
        sb.append(codigoOrgao);                  // Código do órgão
        sb.append(DELIMITADOR_CAMPO);
        sb.append(codigoUG);                     // Código da UG
        sb.append(DELIMITADOR_CAMPO);
        sb.append(cpfResponsavelSistema);        // CPF do responsável
        sb.append(DELIMITADOR_CAMPO);
        sb.append(codigoUG);                     // Código UG destino
        sb.append(DELIMITADOR_CAMPO);
        sb.append(DELIMITADOR_LINHA);            // Fim da linha
        
        return sb.toString();
    }
    
    /**
     * Configura os códigos institucionais
     */
    public void configurarInstituicao(String codigoOrgao, String codigoUG, String cpfResponsavel) {
        this.codigoOrgao = codigoOrgao;
        this.codigoUG = codigoUG;
        this.cpfResponsavelSistema = cpfResponsavel;
    }
    
    /**
     * Formata um registro de patrimônio (Detail)
     * Formato conforme especificação SIADS v6.2.11 para Material Permanente
     * 
     * Campos obrigatórios marcados com *
     * D¥codigoMaterial*¥descricao*¥codigoCatmat¥endereco¥codigoUorg*¥
     * tipoAquisicao*¥estadoConservacao*¥situacao*¥dataAquisicao*¥valorAquisicao*¥
     * formaAquisicao¥especificacao¥dataDepreciacao¥valorDepreciado¥numeroPatrimonio*¥
     * marca¥modelo¥fabricante¥garantidor¥contrato¥dataInicioGarantia¥dataFimGarantia¥
     * cpfResponsavel¥nomeResponsavel¥baixado¥dataBaixa¥motivoBaixa¥numeroProcessoBaixa¥£
     * 
     * Adequado às normas federais:
     * - IN SGD/ME nº 1/2019
     * - Decreto nº 9.373/2018
     * - NBC TSP 07
     */
    public String formatarRegistro(SiadsRegistro registro) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("D");                                                    // Tipo: Detail
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getCodigoMaterial()));           // Código do material*
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getDescricao()));                // Descrição*
        sb.append(DELIMITADOR_CAMPO);
        // Usa código CATMAT se disponível, senão usa classe contábil
        String codigoCatmat = registro.getCodigoCatmat();
        if (codigoCatmat == null || codigoCatmat.trim().isEmpty()) {
            codigoCatmat = registro.getClasseContabil();
        }
        sb.append(formatarCampo(codigoCatmat));                           // Código CATMAT
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getSala()));                     // Endereço/Localização
        sb.append(DELIMITADOR_CAMPO);
        // Usa codigoUorg se disponível, senão usa unidadeGestora (compatibilidade)
        String codigoUorg = registro.getCodigoUorg();
        if (codigoUorg == null || codigoUorg.trim().isEmpty()) {
            codigoUorg = registro.getUnidadeGestora();
        }
        sb.append(formatarCampo(codigoUorg));                             // Código UOrg*
        sb.append(DELIMITADOR_CAMPO);
        // Tipo aquisição: usa valor do registro ou padrão "1" (Compra)
        String tipoAquisicao = registro.getTipoAquisicao();
        sb.append(tipoAquisicao != null ? tipoAquisicao : "1");           // Tipo aquisição* (1=Compra)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarEstadoConservacao(registro.getEstadoConservacao())); // Estado conservação* (1-5)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarSituacao(registro.getSituacaoBem()));           // Situação* (1-3)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarData(registro.getDataAquisicao()));             // Data aquisição*
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarValorInteiro(registro.getValorAquisicao()));    // Valor aquisição* (centavos)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getFormaAquisicao()));           // Forma aquisição
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getEspecificacao()));            // Especificação
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarData(registro.getDataIncorporacao()));          // Data depreciação
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarValorInteiro(registro.getValorDepreciado()));   // Valor depreciado (centavos)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getNumeroPatrimonio()));         // Número patrimônio*
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getGrupoMaterial()));            // Marca
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getModelo()));                   // Modelo
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getFabricante()));               // Fabricante
        sb.append(DELIMITADOR_CAMPO);
        
        // ========================================
        // CAMPOS DE GARANTIA (Novos - Normas Federais)
        // ========================================
        sb.append(formatarCampo(registro.getGarantidor()));               // Garantidor
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getContratoGarantia()));         // Contrato garantia
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarData(registro.getDataInicioGarantia()));        // Data início garantia
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarData(registro.getDataFimGarantia()));           // Data fim garantia
        sb.append(DELIMITADOR_CAMPO);
        
        // ========================================
        // RESPONSÁVEL
        // ========================================
        sb.append(formatarCPF(registro.getCpfResponsavel()));             // CPF responsável
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getNomeResponsavel()));          // Nome responsável
        sb.append(DELIMITADOR_CAMPO);
        
        // ========================================
        // CAMPOS DE BAIXA (Decreto nº 9.373/2018)
        // ========================================
        sb.append(registro.isBaixado() ? "TRUE" : "FALSE");               // Baixado
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarData(registro.getDataBaixa()));                 // Data baixa
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getMotivoBaixa()));              // Motivo baixa (código 1-10)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(formatarCampo(registro.getNumeroProcessoBaixa()));      // Número processo baixa
        sb.append(DELIMITADOR_CAMPO);
        sb.append(DELIMITADOR_LINHA);                                     // Fim da linha
        
        return sb.toString();
    }
    
    /**
     * Formata o rodapé (Trailer) do arquivo SIADS
     * Formato: T¥dataHoraGeracao¥quantidadeRegistros¥valorTotal¥FIM¥£
     * 
     * @param totalRegistros Quantidade de registros no arquivo
     * @param valorTotal Valor total dos bens (em centavos)
     * @return Linha de rodapé formatada
     */
    public String formatarRodape(int totalRegistros, long valorTotal) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("T");                                          // Tipo: Trailer
        sb.append(DELIMITADOR_CAMPO);
        sb.append(LocalDateTime.now().format(DATETIME_FORMATTER)); // Data/hora geração
        sb.append(DELIMITADOR_CAMPO);
        sb.append(totalRegistros);                               // Quantidade de registros
        sb.append(DELIMITADOR_CAMPO);
        sb.append(valorTotal);                                   // Valor total (centavos)
        sb.append(DELIMITADOR_CAMPO);
        sb.append("FIM");                                        // Palavra FIM (obrigatório)
        sb.append(DELIMITADOR_CAMPO);
        sb.append(DELIMITADOR_LINHA);                           // Fim da linha
        
        return sb.toString();
    }
    
    /**
     * Sobrecarga para compatibilidade (sem valor total)
     */
    public String formatarRodape(int totalRegistros) {
        return formatarRodape(totalRegistros, 0);
    }
    
    /**
     * Formata um campo de texto
     * Remove delimitadores e caracteres especiais
     */
    private String formatarCampo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "";
        }
        // Remove delimitadores SIADS e caracteres de controle
        return valor.trim()
                .replace(DELIMITADOR_CAMPO, " ")
                .replace(DELIMITADOR_LINHA, " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");
    }
    
    /**
     * Formata um valor monetário para centavos (formato SIADS)
     * Exemplo: R$ 1.234,56 → 123456
     */
    private String formatarValorInteiro(BigDecimal valor) {
        if (valor == null) {
            return "0";
        }
        // Multiplica por 100 para converter para centavos
        return valor.multiply(new BigDecimal("100")).longValue() + "";
    }
    
    /**
     * Formata uma data no padrão SIADS (ddMMyyyy)
     * Exemplo: 15/05/2024 → 15052024
     */
    private String formatarData(LocalDate data) {
        if (data == null) {
            return "";
        }
        return data.format(DATE_FORMATTER);
    }
    
    /**
     * Formata CPF (apenas números, 11 dígitos)
     */
    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "";
        }
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        // Garante 11 dígitos
        if (cpfLimpo.length() < 11) {
            cpfLimpo = String.format("%011d", Long.parseLong(cpfLimpo));
        }
        return cpfLimpo;
    }
    
    /**
     * Converte estado de conservação para código SIADS
     * 1 = Ótimo, 2 = Bom, 3 = Regular, 4 = Ruim, 5 = Péssimo
     */
    private String formatarEstadoConservacao(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return "2"; // Padrão: Bom
        }
        
        String estadoUpper = estado.toUpperCase().trim();
        switch (estadoUpper) {
            case "ÓTIMO":
            case "OTIMO":
                return "1";
            case "BOM":
                return "2";
            case "REGULAR":
                return "3";
            case "RUIM":
                return "4";
            case "PÉSSIMO":
            case "PESSIMO":
                return "5";
            default:
                return "2"; // Padrão: Bom
        }
    }
    
    /**
     * Converte situação do bem para código SIADS
     * 1 = Em uso, 2 = Ocioso, 3 = Baixado
     */
    private String formatarSituacao(String situacao) {
        if (situacao == null || situacao.trim().isEmpty()) {
            return "1"; // Padrão: Em uso
        }
        
        String situacaoUpper = situacao.toUpperCase().trim();
        switch (situacaoUpper) {
            case "ATIVO":
            case "EM USO":
            case "EM_USO":
                return "1";
            case "OCIOSO":
            case "INATIVO":
                return "2";
            case "BAIXADO":
                return "3";
            default:
                return "1"; // Padrão: Em uso
        }
    }
}
