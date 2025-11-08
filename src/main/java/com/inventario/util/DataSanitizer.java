package com.inventario.util;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilitário para sanitizar dados antes da exportação Excel
 */
public class DataSanitizer {
    
    // Padrões para detectar dados problemáticos
    private static final Pattern INVALID_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
    private static final Pattern XML_INVALID = Pattern.compile("[\\uFFFE\\uFFFF]");
    private static final int MAX_CELL_LENGTH = 32767; // Limite do Excel
    
    /**
     * Sanitiza uma lista de dados para exportação Excel
     */
    public static List<Map<String, Object>> sanitizarDados(List<Map<String, Object>> dados) {
        if (dados == null || dados.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> dadosLimpos = new ArrayList<>();
        int linhaAtual = 0;
        
        System.out.println("=== SANITIZAÇÃO DE DADOS PARA EXCEL ===");
        System.out.println("Total de registros a processar: " + dados.size());
        
        for (Map<String, Object> linha : dados) {
            linhaAtual++;
            
            try {
                Map<String, Object> linhaSanitizada = sanitizarLinha(linha, linhaAtual);
                if (linhaSanitizada != null) {
                    dadosLimpos.add(linhaSanitizada);
                }
            } catch (Exception e) {
                System.err.println("Erro ao sanitizar linha " + linhaAtual + ": " + e.getMessage());
                // Adicionar linha com dados seguros
                dadosLimpos.add(criarLinhaSegura(linhaAtual));
            }
            
            if (linhaAtual % 100 == 0) {
                System.out.println("Processadas " + linhaAtual + " linhas...");
            }
        }
        
        System.out.println("Sanitização concluída: " + dadosLimpos.size() + " registros válidos");
        return dadosLimpos;
    }
    
    /**
     * Sanitiza uma linha individual
     */
    private static Map<String, Object> sanitizarLinha(Map<String, Object> linha, int numeroLinha) {
        if (linha == null) {
            return criarLinhaSegura(numeroLinha);
        }
        
        Map<String, Object> linhaSanitizada = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : linha.entrySet()) {
            String chave = sanitizarChave(entry.getKey());
            Object valor = sanitizarValor(entry.getValue(), chave, numeroLinha);
            
            if (chave != null && !chave.trim().isEmpty()) {
                linhaSanitizada.put(chave, valor);
            }
        }
        
        return linhaSanitizada;
    }
    
    /**
     * Sanitiza uma chave (nome da coluna)
     */
    private static String sanitizarChave(String chave) {
        if (chave == null) {
            return "coluna_sem_nome";
        }
        
        // Remover caracteres inválidos
        String chaveLimpa = INVALID_CHARS.matcher(chave).replaceAll("");
        chaveLimpa = XML_INVALID.matcher(chaveLimpa).replaceAll("");
        
        // Limitar tamanho
        if (chaveLimpa.length() > 255) {
            chaveLimpa = chaveLimpa.substring(0, 255);
        }
        
        return chaveLimpa.trim().isEmpty() ? "coluna_vazia" : chaveLimpa;
    }
    
    /**
     * Sanitiza um valor individual
     */
    private static Object sanitizarValor(Object valor, String chave, int numeroLinha) {
        if (valor == null) {
            return "";
        }
        
        try {
            // Converter para string para análise
            String valorStr = valor.toString();
            
            // Verificar se é muito longo
            if (valorStr.length() > MAX_CELL_LENGTH) {
                System.out.println("Valor muito longo na linha " + numeroLinha + ", coluna " + chave + 
                                 " (truncando de " + valorStr.length() + " para " + MAX_CELL_LENGTH + " caracteres)");
                valorStr = valorStr.substring(0, MAX_CELL_LENGTH - 3) + "...";
            }
            
            // Remover caracteres de controle inválidos
            valorStr = INVALID_CHARS.matcher(valorStr).replaceAll("");
            valorStr = XML_INVALID.matcher(valorStr).replaceAll("");
            
            // Verificar tipos específicos
            if (valor instanceof Number) {
                return sanitizarNumero((Number) valor);
            } else if (valor instanceof Date) {
                return valor; // Datas geralmente são seguras
            } else if (valor instanceof Boolean) {
                return valor; // Booleanos são seguros
            } else {
                // String ou outros tipos
                return sanitizarString(valorStr);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao sanitizar valor na linha " + numeroLinha + ", coluna " + chave + ": " + e.getMessage());
            return "ERRO_DADOS";
        }
    }
    
    /**
     * Sanitiza números
     */
    private static Object sanitizarNumero(Number numero) {
        if (numero == null) {
            return 0;
        }
        
        // Verificar se é um número válido
        double valorDouble = numero.doubleValue();
        
        if (Double.isNaN(valorDouble) || Double.isInfinite(valorDouble)) {
            return 0;
        }
        
        return numero;
    }
    
    /**
     * Sanitiza strings
     */
    private static String sanitizarString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return "";
        }
        
        // Remover caracteres problemáticos para XML/Excel
        str = str.replace("\u0000", ""); // Null character
        str = str.replace("\uFFFE", ""); // Invalid Unicode
        str = str.replace("\uFFFF", ""); // Invalid Unicode
        
        // Substituir quebras de linha por espaços (Excel pode ter problemas)
        str = str.replace("\r\n", " ");
        str = str.replace("\n", " ");
        str = str.replace("\r", " ");
        
        // Remover múltiplos espaços
        str = str.replaceAll("\\s+", " ");
        
        return str.trim();
    }
    
    /**
     * Cria uma linha segura em caso de erro
     */
    private static Map<String, Object> criarLinhaSegura(int numeroLinha) {
        Map<String, Object> linhaSegura = new HashMap<>();
        linhaSegura.put("numero", "ERRO_LINHA_" + numeroLinha);
        linhaSegura.put("descricao", "Dados corrompidos ou inválidos");
        linhaSegura.put("marca", "N/A");
        linhaSegura.put("modelo", "N/A");
        linhaSegura.put("setor", "N/A");
        linhaSegura.put("responsavel", "N/A");
        linhaSegura.put("situacao", "ERRO");
        linhaSegura.put("valor", 0.0);
        return linhaSegura;
    }
    
    /**
     * Valida se os dados estão seguros para Excel
     */
    public static boolean validarDadosParaExcel(List<Map<String, Object>> dados) {
        if (dados == null || dados.isEmpty()) {
            return true;
        }
        
        System.out.println("=== VALIDAÇÃO DE DADOS PARA EXCEL ===");
        
        boolean dadosValidos = true;
        int problemasEncontrados = 0;
        
        for (int i = 0; i < dados.size(); i++) {
            Map<String, Object> linha = dados.get(i);
            
            if (linha == null) {
                System.out.println("Linha " + (i + 1) + ": null");
                dadosValidos = false;
                problemasEncontrados++;
                continue;
            }
            
            for (Map.Entry<String, Object> entry : linha.entrySet()) {
                String problema = validarCampo(entry.getKey(), entry.getValue(), i + 1);
                if (problema != null) {
                    System.out.println("Linha " + (i + 1) + ", Campo " + entry.getKey() + ": " + problema);
                    dadosValidos = false;
                    problemasEncontrados++;
                }
            }
        }
        
        System.out.println("Validação concluída: " + 
                         (dadosValidos ? "✅ Dados válidos" : "❌ " + problemasEncontrados + " problemas encontrados"));
        
        return dadosValidos;
    }
    
    /**
     * Valida um campo específico
     */
    private static String validarCampo(String chave, Object valor, int numeroLinha) {
        if (chave == null) {
            return "Chave null";
        }
        
        if (chave.length() > 255) {
            return "Chave muito longa (" + chave.length() + " caracteres)";
        }
        
        if (valor != null) {
            String valorStr = valor.toString();
            
            if (valorStr.length() > MAX_CELL_LENGTH) {
                return "Valor muito longo (" + valorStr.length() + " caracteres)";
            }
            
            if (INVALID_CHARS.matcher(valorStr).find()) {
                return "Contém caracteres de controle inválidos";
            }
            
            if (XML_INVALID.matcher(valorStr).find()) {
                return "Contém caracteres Unicode inválidos";
            }
        }
        
        return null; // Campo válido
    }
    
    /**
     * Cria dados de teste seguros
     */
    public static List<Map<String, Object>> criarDadosTesteSeguros() {
        List<Map<String, Object>> dadosSegurosTeste = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("numero", "TEST_" + String.format("%03d", i));
            item.put("descricao", "Item de Teste " + i);
            item.put("marca", "Marca Teste");
            item.put("modelo", "Modelo " + i);
            item.put("setor", "Setor Teste");
            item.put("responsavel", "Responsável Teste");
            item.put("situacao", "ATIVO");
            item.put("valor", 100.0 * i);
            dadosSegurosTeste.add(item);
        }
        
        return dadosSegurosTeste;
    }
}