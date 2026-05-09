package com.inventario.sihcp.service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Serviço inteligente para gerar resumos de descrições longas de patrimônios.
 * Facilita a identificação de itens durante trabalho de campo.
 */
public class DescricaoResumoService {
    
    private static final int TAMANHO_MAXIMO_RESUMO = 50;
    private static final int PALAVRAS_MAXIMAS = 4;
    
    // Cache para evitar recálculos - LIMITE REDUZIDO para baixo consumo de memória
    // CRÍTICO: Cache grande causava vazamento de memória no servidor mobile
    private static final int MAX_CACHE_SIZE = 100;  // Reduzido de 500
    private static final Map<String, String> cacheResumos = new java.util.LinkedHashMap<String, String>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };
    
    // Dicionário de categorias principais
    private static final Map<String, List<String>> CATEGORIAS = new HashMap<>();
    
    // Palavras irrelevantes que devem ser removidas
    private static final Set<String> PALAVRAS_IRRELEVANTES = new HashSet<>();
    
    // Palavras que indicam características importantes
    private static final Set<String> CARACTERISTICAS_IMPORTANTES = new HashSet<>();
    
    // Marcas conhecidas que devem ser priorizadas
    private static final Set<String> MARCAS_CONHECIDAS = new HashSet<>();
    
    // Padrões de modelo (números e códigos)
    private static final Pattern PADRAO_MODELO = Pattern.compile("[A-Z0-9]{2,}|I[357]|CORE|RYZEN|GTX|RTX");
    
    static {
        inicializarDicionarios();
    }
    
    /**
     * Inicializa os dicionários de categorias e palavras
     */
    private static void inicializarDicionarios() {
        // Categorias de mobiliário
        CATEGORIAS.put("MOBILIARIO", Arrays.asList(
            "CADEIRA", "MESA", "ARMARIO", "ESTANTE", "BANCO", "POLTRONA", 
            "ESCRIVANINHA", "GAVETEIRO", "PRATELEIRA", "BALCAO"
        ));
        
        // Categorias de equipamentos
        CATEGORIAS.put("EQUIPAMENTO", Arrays.asList(
            "COMPUTADOR", "NOTEBOOK", "IMPRESSORA", "SCANNER", "PROJETOR",
            "MONITOR", "TECLADO", "MOUSE", "TELEFONE", "FAX"
        ));
        
        // Categorias de eletrodomésticos
        CATEGORIAS.put("ELETRODOMESTICO", Arrays.asList(
            "GELADEIRA", "MICROONDAS", "CAFETEIRA", "BEBEDOURO", "VENTILADOR",
            "AR CONDICIONADO", "PURIFICADOR", "FOGAO"
        ));
        
        // Categorias de veículos
        CATEGORIAS.put("VEICULO", Arrays.asList(
            "CARRO", "CAMINHAO", "MOTOCICLETA", "BICICLETA", "ONIBUS"
        ));
        
        // Marcas conhecidas (eletrônicos, móveis, etc.)
        MARCAS_CONHECIDAS.addAll(Arrays.asList(
            // Informática
            "HP", "DELL", "LENOVO", "ASUS", "ACER", "SAMSUNG", "LG", "SONY", "APPLE",
            "INTEL", "AMD", "NVIDIA", "MICROSOFT", "LOGITECH", "CANON", "EPSON",
            "BROTHER", "XEROX", "CISCO", "HUAWEI", "TPLINK", "DLINK",
            // Eletrodomésticos
            "BRASTEMP", "CONSUL", "ELECTROLUX", "PHILIPS", "PANASONIC", "MIDEA",
            "BRITANIA", "MONDIAL", "CADENCE", "ARNO", "BLACK", "DECKER",
            // Móveis
            "KAPPESBERG", "MADESA", "POLITORNO", "DALLA", "COSTA", "FLEXFORM",
            "CAVALETTI", "PRESIDENTE", "GIROFLEX", "PLAXMETAL", "BENETTON",
            // Veículos
            "VOLKSWAGEN", "FORD", "CHEVROLET", "FIAT", "TOYOTA", "HONDA", "YAMAHA"
        ));
        
        // Palavras irrelevantes (removemos MODELO e MARCA da lista)
        PALAVRAS_IRRELEVANTES.addAll(Arrays.asList(
            "COM", "DE", "DA", "DO", "EM", "PARA", "POR", "E", "OU", "QUE",
            "POSSUI", "DOTADO", "EQUIPADO", "FABRICADO", "TIPO", "COR",
            "MATERIAL", "REVESTIMENTO", "ACABAMENTO", "DIMENSOES", "MEDIDAS",
            "ESPECIFICACAO", "CARACTERISTICAS", "FUNCIONALIDADES", "RECURSOS"
        ));
        
        // Características importantes a preservar
        CARACTERISTICAS_IMPORTANTES.addAll(Arrays.asList(
            "PRETO", "BRANCO", "AZUL", "VERMELHO", "VERDE", "AMARELO", "CINZA",
            "PEQUENO", "MEDIO", "GRANDE", "ALTO", "BAIXO", "LARGO", "ESTREITO",
            "EXECUTIVA", "SIMPLES", "DUPLO", "TRIPLO", "INDIVIDUAL",
            "LASER", "JATO", "LED", "LCD", "PLASMA", "DIGITAL", "ANALOGICO",
            "GIRATORIA", "FIXA", "DOBRAVEL", "RETRATIL", "AJUSTAVEL"
        ));
    }
    
    /**
     * Gera um resumo inteligente da descrição completa
     * @param descricaoCompleta Descrição original do patrimônio
     * @return Descrição resumida
     */
    public static String gerarResumo(String descricaoCompleta) {
        if (descricaoCompleta == null || descricaoCompleta.trim().isEmpty()) {
            return "";
        }
        
        // Verificar cache primeiro
        String chaveCache = descricaoCompleta.trim().toUpperCase();
        if (cacheResumos.containsKey(chaveCache)) {
            return cacheResumos.get(chaveCache);
        }
        
        String resumo = processarDescricao(descricaoCompleta);
        
        // Armazenar no cache
        cacheResumos.put(chaveCache, resumo);
        
        return resumo;
    }
    
    /**
     * Processa a descrição e gera o resumo
     */
    private static String processarDescricao(String descricao) {
        // Pré-processamento
        String textoLimpo = limparTexto(descricao);
        
        // Extrair palavras
        List<String> palavras = extrairPalavras(textoLimpo);
        
        // Identificar categoria principal
        String categoriaPrincipal = identificarCategoria(palavras);
        
        // Extrair marca e modelo
        String marca = extrairMarca(palavras);
        String modelo = extrairModelo(textoLimpo);
        
        // Extrair palavras-chave
        List<String> palavrasChave = extrairPalavrasChave(palavras, categoriaPrincipal, marca, modelo);
        
        // Gerar resumo final
        return construirResumo(palavrasChave);
    }
    
    /**
     * Limpa o texto removendo caracteres especiais e normalizando
     */
    private static String limparTexto(String texto) {
        return texto.toUpperCase()
                   .replaceAll("[^A-ZÁÀÂÃÉÊÍÓÔÕÚÇ\\s]", " ")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
    
    /**
     * Extrai palavras individuais do texto
     */
    private static List<String> extrairPalavras(String texto) {
        return Arrays.stream(texto.split("\\s+"))
                    .filter(palavra -> palavra.length() > 2)
                    .collect(Collectors.toList());
    }
    
    /**
     * Identifica a categoria principal do item
     */
    private static String identificarCategoria(List<String> palavras) {
        for (Map.Entry<String, List<String>> categoria : CATEGORIAS.entrySet()) {
            for (String palavra : palavras) {
                if (categoria.getValue().contains(palavra)) {
                    return palavra; // Retorna a palavra específica encontrada
                }
            }
        }
        
        // Se não encontrou categoria específica, usa a primeira palavra significativa
        return palavras.isEmpty() ? "ITEM" : palavras.get(0);
    }
    
    /**
     * Extrai a marca do produto da lista de palavras
     */
    private static String extrairMarca(List<String> palavras) {
        for (String palavra : palavras) {
            if (MARCAS_CONHECIDAS.contains(palavra)) {
                return palavra;
            }
        }
        return null;
    }
    
    /**
     * Extrai o modelo do produto usando padrões regex
     */
    private static String extrairModelo(String textoLimpo) {
        java.util.regex.Matcher matcher = PADRAO_MODELO.matcher(textoLimpo);
        
        // Procura por padrões de modelo (códigos alfanuméricos, processadores, etc.)
        while (matcher.find()) {
            String candidato = matcher.group();
            
            // Filtrar modelos válidos (não são palavras comuns)
            if (!PALAVRAS_IRRELEVANTES.contains(candidato) && 
                !CARACTERISTICAS_IMPORTANTES.contains(candidato) &&
                candidato.length() >= 2 && candidato.length() <= 10) {
                return candidato;
            }
        }
        
        return null;
    }
    
    /**
     * Extrai as palavras-chave mais importantes
     */
    private static List<String> extrairPalavrasChave(List<String> palavras, String categoriaPrincipal, String marca, String modelo) {
        List<String> palavrasChave = new ArrayList<>();
        
        // Sempre incluir a categoria principal primeiro
        palavrasChave.add(categoriaPrincipal);
        
        // Priorizar marca se encontrada
        if (marca != null && !marca.equals(categoriaPrincipal)) {
            palavrasChave.add(marca);
        }
        
        // Priorizar modelo se encontrado
        if (modelo != null && !modelo.equals(categoriaPrincipal) && 
            (marca == null || !modelo.equals(marca))) {
            palavrasChave.add(modelo);
        }
        
        // Adicionar características importantes
        for (String palavra : palavras) {
            if (palavrasChave.size() >= PALAVRAS_MAXIMAS) break;
            
            if (!palavra.equals(categoriaPrincipal) && 
                (marca == null || !palavra.equals(marca)) &&
                (modelo == null || !palavra.equals(modelo)) &&
                !PALAVRAS_IRRELEVANTES.contains(palavra) &&
                (CARACTERISTICAS_IMPORTANTES.contains(palavra) || 
                 palavra.length() <= 8)) { // Palavras curtas tendem a ser mais importantes
                
                palavrasChave.add(palavra);
            }
        }
        
        return palavrasChave;
    }
    
    /**
     * Constrói o resumo final a partir das palavras-chave
     */
    private static String construirResumo(List<String> palavrasChave) {
        String resumo = String.join(" ", palavrasChave);
        
        // Garantir que não exceda o tamanho máximo
        if (resumo.length() > TAMANHO_MAXIMO_RESUMO) {
            // Tentar remover palavras menos importantes
            while (palavrasChave.size() > 1 && resumo.length() > TAMANHO_MAXIMO_RESUMO) {
                palavrasChave.remove(palavrasChave.size() - 1);
                resumo = String.join(" ", palavrasChave);
            }
            
            // Se ainda estiver muito longo, truncar
            if (resumo.length() > TAMANHO_MAXIMO_RESUMO) {
                resumo = resumo.substring(0, TAMANHO_MAXIMO_RESUMO - 3) + "...";
            }
        }
        
        return resumo;
    }
    
    /**
     * Atualiza o cache com um resumo personalizado
     */
    public static void atualizarCache(String descricaoCompleta, String resumoPersonalizado) {
        if (descricaoCompleta != null && resumoPersonalizado != null) {
            cacheResumos.put(descricaoCompleta.trim().toUpperCase(), resumoPersonalizado);
        }
    }
    
    /**
     * Limpa o cache de resumos
     */
    public static void limparCache() {
        cacheResumos.clear();
    }
    
    /**
     * Retorna estatísticas do cache
     */
    public static Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalResumos", cacheResumos.size());
        stats.put("categorias", CATEGORIAS.size());
        stats.put("palavrasIrrelevantes", PALAVRAS_IRRELEVANTES.size());
        stats.put("caracteristicasImportantes", CARACTERISTICAS_IMPORTANTES.size());
        return stats;
    }
    
    /**
     * Método para testar o serviço com exemplos
     */
    public static void main(String[] args) {
        // Exemplos de teste com foco em marca e modelo
        String[] exemplos = {
            "CADEIRA GIRATÓRIA EXECUTIVA COM BRAÇOS, ENCOSTO ALTO, REVESTIMENTO EM COURO SINTÉTICO COR PRETA",
            "MESA DE ESCRITÓRIO EM MADEIRA MDF COM 3 GAVETAS E ACABAMENTO EM FÓRMICA BRANCA",
            "COMPUTADOR DESKTOP DELL OPTIPLEX 7090 INTEL CORE I5 8GB RAM 500GB HD",
            "NOTEBOOK LENOVO THINKPAD E14 AMD RYZEN 5 8GB RAM 256GB SSD",
            "IMPRESSORA LASER HP LASERJET PRO M404N MONOCROMÁTICA COM REDE",
            "MONITOR LED SAMSUNG 24 POLEGADAS FULL HD MODELO F24T450FQL",
            "SMARTPHONE SAMSUNG GALAXY A54 128GB DUAL CHIP ANDROID",
            "PROJETOR EPSON POWERLITE X41 3600 LUMENS XGA",
            "ARMÁRIO DE AÇO PRESIDENTE 4 PORTAS COM 2 PRATELEIRAS COR CINZA",
            "GELADEIRA BRASTEMP FROST FREE DUPLEX 375 LITROS BRANCA"
        };
        
        System.out.println("=== TESTE DO SERVIÇO DE RESUMO COM MARCA/MODELO ===\n");
        
        for (String exemplo : exemplos) {
            String resumo = gerarResumo(exemplo);
            System.out.println("Original: " + exemplo);
            System.out.println("Resumo:   " + resumo);
            System.out.println("Tamanho:  " + resumo.length() + " caracteres\n");
        }
        
        System.out.println("Estatísticas: " + obterEstatisticas());
    }
}