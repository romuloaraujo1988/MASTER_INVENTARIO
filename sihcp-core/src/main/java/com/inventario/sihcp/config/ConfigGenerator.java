package com.inventario.sihcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Gerador do arquivo {@code configuracao_banco.json} a partir de um
 * {@link SetupConfig} coletado pelo setup interativo.
 *
 * <p>Utiliza o Jackson {@link ObjectMapper} para serializar o JSON no formato
 * esperado pelo {@link DatabaseConfigManager} e pelos scripts de setup.</p>
 *
 * <p>Formato gerado:</p>
 * <pre>{@code
 * {
 *   "campus": {
 *     "nome": "IFMT - Campus Cuiabá",
 *     "sigla": "CBA",
 *     "cidade": "Cuiabá",
 *     "estado": "MT",
 *     "responsavel_tecnico": "João Silva",
 *     "contato": "joao.silva@ifmt.edu.br"
 *   },
 *   "postgresql": {
 *     "host": "localhost",
 *     "port": 5432,
 *     "database": "sispatrimonio",
 *     "user": "inventario",
 *     "password": "senha_aqui"
 *   },
 *   "api": {
 *     "porta": 8080,
 *     "versao": "2.7.0"
 *   }
 * }
 * }</pre>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * SetupConfig cfg = new SetupConfig(...);
 * String json = ConfigGenerator.generateConfigJson(cfg);
 * Files.writeString(Path.of("config/configuracao_banco.json"), json);
 * }</pre>
 *
 * @author Sistema de Inventário IFMT
 * @version 2.7.0
 * @see SetupConfig
 * @see DatabaseConfigManager
 */
public class ConfigGenerator {

    /** Versão atual do sistema, inserida no campo {@code api.versao}. */
    private static final String VERSAO_SISTEMA = "2.7.0";

    /**
     * Construtor privado — classe utilitária, não deve ser instanciada.
     */
    private ConfigGenerator() {
    }

    /**
     * Serializa um {@link SetupConfig} no formato JSON do
     * {@code configuracao_banco.json}.
     *
     * @param input dados coletados pelo setup interativo (não pode ser {@code null})
     * @return string JSON formatada (pretty-print) pronta para ser gravada em arquivo
     * @throws Exception se ocorrer erro na serialização Jackson ou se {@code input}
     *                   for {@code null}
     */
    public static String generateConfigJson(SetupConfig input) throws Exception {
        if (input == null) {
            throw new IllegalArgumentException("SetupConfig não pode ser nulo.");
        }

        ObjectMapper mapper = new ObjectMapper();

        // Seção "campus"
        ObjectNode campusNode = mapper.createObjectNode();
        campusNode.put("nome",               nullSafe(input.getNomeCampus()));
        campusNode.put("sigla",              nullSafe(input.getSiglaCampus()));
        campusNode.put("cidade",             nullSafe(input.getCidadeCampus()));
        campusNode.put("estado",             nullSafe(input.getEstadoCampus()));
        campusNode.put("responsavel_tecnico", nullSafe(input.getResponsavelTecnico()));
        campusNode.put("contato",            nullSafe(input.getContato()));

        // Seção "postgresql"
        ObjectNode pgNode = mapper.createObjectNode();
        pgNode.put("host",     nullSafe(input.getHostPg()));
        pgNode.put("port",     input.getPortaPg());
        pgNode.put("database", nullSafe(input.getBanco()));
        pgNode.put("user",     nullSafe(input.getUsuarioPg()));
        pgNode.put("password", nullSafe(input.getSenhaPg()));

        // Seção "api"
        ObjectNode apiNode = mapper.createObjectNode();
        apiNode.put("porta",  input.getPortaApi());
        apiNode.put("versao", VERSAO_SISTEMA);

        // Raiz
        ObjectNode root = mapper.createObjectNode();
        root.set("campus",     campusNode);
        root.set("postgresql", pgNode);
        root.set("api",        apiNode);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    /**
     * Retorna a string fornecida, ou uma string vazia se for {@code null}.
     *
     * @param value valor a verificar
     * @return {@code value} ou {@code ""} se nulo
     */
    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}
