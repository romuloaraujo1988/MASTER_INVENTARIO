package com.inventario.sihcp.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuração do campus para o SIHCP.
 *
 * <p>Representa a seção {@code campus} do arquivo {@code configuracao_banco.json},
 * permitindo que cada instalação identifique o campus ao qual pertence.</p>
 *
 * <p>Exemplo de JSON correspondente:</p>
 * <pre>{@code
 * {
 *   "campus": {
 *     "nome": "IFMT - Campus Cuiabá",
 *     "sigla": "CBA",
 *     "cidade": "Cuiabá",
 *     "estado": "MT",
 *     "responsavel_tecnico": "João Silva",
 *     "contato": "joao.silva@ifmt.edu.br"
 *   }
 * }
 * }</pre>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampusConfig {

    /** Comprimento máximo permitido para o nome do campus. */
    public static final int NOME_MAX_LENGTH = 100;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("sigla")
    private String sigla;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("responsavel_tecnico")
    private String responsavelTecnico;

    @JsonProperty("contato")
    private String contato;

    /**
     * Construtor padrão (necessário para desserialização Jackson).
     */
    public CampusConfig() {
    }

    /**
     * Construtor completo.
     *
     * @param nome              nome do campus (obrigatório, máx. 100 caracteres)
     * @param sigla             sigla do campus
     * @param cidade            cidade do campus
     * @param estado            UF do estado
     * @param responsavelTecnico nome do responsável técnico
     * @param contato           e-mail ou telefone de contato
     * @throws IllegalArgumentException se {@code nome} for nulo, vazio ou exceder 100 caracteres
     */
    public CampusConfig(String nome, String sigla, String cidade, String estado,
                        String responsavelTecnico, String contato) {
        setNome(nome);
        this.sigla = sigla;
        this.cidade = cidade;
        this.estado = estado;
        this.responsavelTecnico = responsavelTecnico;
        this.contato = contato;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    /**
     * Retorna o nome do campus.
     *
     * @return nome do campus
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do campus.
     *
     * @param nome nome do campus (não pode ser nulo/vazio; máx. 100 caracteres)
     * @throws IllegalArgumentException se a validação falhar
     */
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do campus não pode ser nulo ou vazio.");
        }
        if (nome.length() > NOME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O nome do campus não pode exceder " + NOME_MAX_LENGTH + " caracteres. "
                    + "Tamanho informado: " + nome.length());
        }
        this.nome = nome;
    }

    /**
     * Retorna a sigla do campus.
     *
     * @return sigla do campus
     */
    public String getSigla() {
        return sigla;
    }

    /**
     * Define a sigla do campus.
     *
     * @param sigla sigla do campus
     */
    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    /**
     * Retorna a cidade do campus.
     *
     * @return cidade do campus
     */
    public String getCidade() {
        return cidade;
    }

    /**
     * Define a cidade do campus.
     *
     * @param cidade cidade do campus
     */
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    /**
     * Retorna o estado (UF) do campus.
     *
     * @return estado do campus
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define o estado (UF) do campus.
     *
     * @param estado estado do campus
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Retorna o nome do responsável técnico.
     *
     * @return responsável técnico
     */
    public String getResponsavelTecnico() {
        return responsavelTecnico;
    }

    /**
     * Define o responsável técnico.
     *
     * @param responsavelTecnico nome do responsável técnico
     */
    public void setResponsavelTecnico(String responsavelTecnico) {
        this.responsavelTecnico = responsavelTecnico;
    }

    /**
     * Retorna o contato (e-mail ou telefone) do responsável.
     *
     * @return contato
     */
    public String getContato() {
        return contato;
    }

    /**
     * Define o contato do responsável.
     *
     * @param contato e-mail ou telefone de contato
     */
    public void setContato(String contato) {
        this.contato = contato;
    }

    // -------------------------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------------------------

    /**
     * Verifica se o campus possui ao menos o nome configurado.
     *
     * @return {@code true} se o nome está presente e não vazio
     */
    public boolean isConfigured() {
        return nome != null && !nome.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CampusConfig{"
                + "nome='" + nome + '\''
                + ", sigla='" + sigla + '\''
                + ", cidade='" + cidade + '\''
                + ", estado='" + estado + '\''
                + ", responsavelTecnico='" + responsavelTecnico + '\''
                + ", contato='" + contato + '\''
                + '}';
    }
}
