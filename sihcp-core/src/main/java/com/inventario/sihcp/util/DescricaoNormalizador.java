package com.inventario.sihcp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizador de descrições de patrimônios para agrupamento de fotos de referência.
 * 
 * Remove informações específicas de cada item (números de patrimônio, séries, etc.)
 * para permitir que uma única foto de referência sirva para múltiplos patrimônios
 * do mesmo tipo.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class DescricaoNormalizador {
    
    // Padrão ANAC: "PATRIMÔNIO ANAC XXXXXXX" ou "- PATRIMÔNIO ANAC XXXXXXX"
    private static final Pattern PATTERN_ANAC = Pattern.compile(
        "\\s*-?\\s*PATRIM[OÔ]NIO\\s+ANAC\\s*[:\\-]?\\s*\\d+",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    
    // Padrão número de série: "N/S: XXX", "N/S XXX", "SÉRIE: XXX"
    private static final Pattern PATTERN_SERIE = Pattern.compile(
        "\\s*[,;\\-]?\\s*(N/?S|S[EÉ]RIE|SERIAL)\\s*[:\\-]?\\s*[A-Z0-9\\-]+",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    
    // Padrão número de patrimônio genérico no final: "- 12345" ou múltiplos "- 12345 - 67890"
    // Usa replaceAll para remover todos os padrões de uma vez
    private static final Pattern PATTERN_NUMERO_FINAL = Pattern.compile(
        "(\\s*[\\-]\\s*\\d{4,})+\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Padrão de código interno entre parênteses
    private static final Pattern PATTERN_CODIGO = Pattern.compile(
        "\\s*\\([^)]*(?:COD|REF|ID|NUM)[^)]*\\)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Múltiplos espaços
    private static final Pattern PATTERN_ESPACOS = Pattern.compile("\\s{2,}");
    
    // Pontuação no final
    private static final Pattern PATTERN_PONTUACAO_FINAL = Pattern.compile("[,;\\-\\s]+$");
    
    /**
     * Normaliza uma descrição de patrimônio removendo informações específicas.
     * 
     * @param descricao Descrição original
     * @return Descrição normalizada em maiúsculas
     */
    public static String normalizar(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "";
        }
        
        String resultado = descricao.trim();
        
        // Remover padrões específicos
        resultado = PATTERN_ANAC.matcher(resultado).replaceAll("");
        resultado = PATTERN_SERIE.matcher(resultado).replaceAll("");
        resultado = PATTERN_NUMERO_FINAL.matcher(resultado).replaceAll("");
        resultado = PATTERN_CODIGO.matcher(resultado).replaceAll("");
        
        // Normalizar espaços e pontuação
        resultado = PATTERN_ESPACOS.matcher(resultado).replaceAll(" ");
        resultado = PATTERN_PONTUACAO_FINAL.matcher(resultado).replaceAll("");
        
        return resultado.trim().toUpperCase();
    }
    
    /**
     * Normaliza mantendo o case original.
     * 
     * @param descricao Descrição original
     * @return Descrição normalizada mantendo case
     */
    public static String normalizarMantendoCase(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            return "";
        }
        
        String resultado = descricao.trim();
        resultado = PATTERN_ANAC.matcher(resultado).replaceAll("");
        resultado = PATTERN_SERIE.matcher(resultado).replaceAll("");
        resultado = PATTERN_NUMERO_FINAL.matcher(resultado).replaceAll("");
        resultado = PATTERN_CODIGO.matcher(resultado).replaceAll("");
        resultado = PATTERN_ESPACOS.matcher(resultado).replaceAll(" ");
        resultado = PATTERN_PONTUACAO_FINAL.matcher(resultado).replaceAll("");
        
        return resultado.trim();
    }
    
    /**
     * Verifica se duas descrições são equivalentes após normalização.
     * 
     * @param descricao1 Primeira descrição
     * @param descricao2 Segunda descrição
     * @return true se são equivalentes
     */
    public static boolean saoEquivalentes(String descricao1, String descricao2) {
        return normalizar(descricao1).equals(normalizar(descricao2));
    }
    
    /**
     * Extrai o número de patrimônio ANAC de uma descrição.
     * 
     * @param descricao Descrição original
     * @return Número ANAC ou null se não encontrado
     */
    public static String extrairNumeroAnac(String descricao) {
        if (descricao == null) {
            return null;
        }
        
        Pattern pattern = Pattern.compile(
            "PATRIM[OÔ]NIO\\s+ANAC\\s*[:\\-]?\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        );
        
        Matcher matcher = pattern.matcher(descricao);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Gera uma versão resumida da descrição para exibição.
     * 
     * @param descricao Descrição original
     * @param maxLength Tamanho máximo
     * @return Descrição resumida
     */
    public static String resumir(String descricao, int maxLength) {
        if (descricao == null || descricao.isEmpty()) {
            return "";
        }
        
        String normalizada = normalizarMantendoCase(descricao);
        
        if (normalizada.length() <= maxLength) {
            return normalizada;
        }
        
        int corte = normalizada.lastIndexOf(' ', maxLength - 3);
        if (corte > maxLength / 2) {
            return normalizada.substring(0, corte) + "...";
        }
        
        return normalizada.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Verifica se a descrição contém padrão ANAC.
     * 
     * @param descricao Descrição a verificar
     * @return true se contém padrão ANAC
     */
    public static boolean contemPadraoAnac(String descricao) {
        return descricao != null && PATTERN_ANAC.matcher(descricao).find();
    }
}
