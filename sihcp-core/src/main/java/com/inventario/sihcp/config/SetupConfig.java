package com.inventario.sihcp.config;

/**
 * POJO que representa todas as entradas coletadas pelo setup interativo do SIHCP.
 *
 * <p>Esta classe é usada pelo script de setup automatizado ({@code setup.ps1} /
 * {@code setup.sh}) para transportar os dados informados pelo
 * Administrador_Campus até o {@link ConfigGenerator}, que os serializa no
 * arquivo {@code configuracao_banco.json}.</p>
 *
 * <p>Exemplo de uso:</p>
 * <pre>{@code
 * SetupConfig cfg = new SetupConfig();
 * cfg.setNomeCampus("IFMT - Campus Cuiabá");
 * cfg.setSiglaCampus("CBA");
 * cfg.setPortaApi(8080);
 * // ...
 * String json = ConfigGenerator.generateConfigJson(cfg);
 * }</pre>
 *
 * @author Sistema de Inventário IFMT
 * @version 2.7.0
 * @see ConfigGenerator
 */
public class SetupConfig {

    // -------------------------------------------------------------------------
    // Dados do campus
    // -------------------------------------------------------------------------

    /** Nome completo do campus (obrigatório, máx. 100 caracteres). */
    private String nomeCampus;

    /** Sigla do campus (ex.: "CBA"). */
    private String siglaCampus;

    /** Cidade onde o campus está localizado. */
    private String cidadeCampus;

    /** UF do estado (ex.: "MT"). */
    private String estadoCampus;

    /** Nome do responsável técnico pela implantação. */
    private String responsavelTecnico;

    /** E-mail ou telefone de contato do responsável técnico. */
    private String contato;

    // -------------------------------------------------------------------------
    // Configuração da API
    // -------------------------------------------------------------------------

    /** Porta TCP em que a API Mobile irá escutar (intervalo: 1024–65535). */
    private int portaApi;

    // -------------------------------------------------------------------------
    // Configuração do PostgreSQL
    // -------------------------------------------------------------------------

    /** Host do servidor PostgreSQL (padrão: {@code localhost}). */
    private String hostPg;

    /** Porta do servidor PostgreSQL (padrão: {@code 5432}). */
    private int portaPg;

    /** Nome do banco de dados (padrão: {@code sispatrimonio}). */
    private String banco;

    /** Usuário do PostgreSQL. */
    private String usuarioPg;

    /** Senha do usuário do PostgreSQL. */
    private String senhaPg;

    // -------------------------------------------------------------------------
    // Credenciais do administrador SIHCP
    // -------------------------------------------------------------------------

    /** Senha do usuário {@code admin} do SIHCP (será armazenada com BCrypt). */
    private String senhaAdmin;

    // =========================================================================
    // Construtores
    // =========================================================================

    /**
     * Construtor padrão (necessário para frameworks de serialização).
     */
    public SetupConfig() {
    }

    /**
     * Construtor completo com todos os campos.
     *
     * @param nomeCampus         nome do campus
     * @param siglaCampus        sigla do campus
     * @param cidadeCampus       cidade do campus
     * @param estadoCampus       UF do estado
     * @param responsavelTecnico nome do responsável técnico
     * @param contato            e-mail ou telefone de contato
     * @param portaApi           porta da API (1024–65535)
     * @param hostPg             host do PostgreSQL
     * @param portaPg            porta do PostgreSQL
     * @param banco              nome do banco de dados
     * @param usuarioPg          usuário do PostgreSQL
     * @param senhaPg            senha do PostgreSQL
     * @param senhaAdmin         senha do administrador SIHCP
     */
    public SetupConfig(String nomeCampus, String siglaCampus, String cidadeCampus,
                       String estadoCampus, String responsavelTecnico, String contato,
                       int portaApi,
                       String hostPg, int portaPg, String banco,
                       String usuarioPg, String senhaPg,
                       String senhaAdmin) {
        this.nomeCampus = nomeCampus;
        this.siglaCampus = siglaCampus;
        this.cidadeCampus = cidadeCampus;
        this.estadoCampus = estadoCampus;
        this.responsavelTecnico = responsavelTecnico;
        this.contato = contato;
        this.portaApi = portaApi;
        this.hostPg = hostPg;
        this.portaPg = portaPg;
        this.banco = banco;
        this.usuarioPg = usuarioPg;
        this.senhaPg = senhaPg;
        this.senhaAdmin = senhaAdmin;
    }

    // =========================================================================
    // Getters e Setters — Dados do campus
    // =========================================================================

    public String getNomeCampus() {
        return nomeCampus;
    }

    public void setNomeCampus(String nomeCampus) {
        this.nomeCampus = nomeCampus;
    }

    public String getSiglaCampus() {
        return siglaCampus;
    }

    public void setSiglaCampus(String siglaCampus) {
        this.siglaCampus = siglaCampus;
    }

    public String getCidadeCampus() {
        return cidadeCampus;
    }

    public void setCidadeCampus(String cidadeCampus) {
        this.cidadeCampus = cidadeCampus;
    }

    public String getEstadoCampus() {
        return estadoCampus;
    }

    public void setEstadoCampus(String estadoCampus) {
        this.estadoCampus = estadoCampus;
    }

    public String getResponsavelTecnico() {
        return responsavelTecnico;
    }

    public void setResponsavelTecnico(String responsavelTecnico) {
        this.responsavelTecnico = responsavelTecnico;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    // =========================================================================
    // Getters e Setters — API
    // =========================================================================

    public int getPortaApi() {
        return portaApi;
    }

    public void setPortaApi(int portaApi) {
        this.portaApi = portaApi;
    }

    // =========================================================================
    // Getters e Setters — PostgreSQL
    // =========================================================================

    public String getHostPg() {
        return hostPg;
    }

    public void setHostPg(String hostPg) {
        this.hostPg = hostPg;
    }

    public int getPortaPg() {
        return portaPg;
    }

    public void setPortaPg(int portaPg) {
        this.portaPg = portaPg;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getUsuarioPg() {
        return usuarioPg;
    }

    public void setUsuarioPg(String usuarioPg) {
        this.usuarioPg = usuarioPg;
    }

    public String getSenhaPg() {
        return senhaPg;
    }

    public void setSenhaPg(String senhaPg) {
        this.senhaPg = senhaPg;
    }

    // =========================================================================
    // Getters e Setters — Admin SIHCP
    // =========================================================================

    public String getSenhaAdmin() {
        return senhaAdmin;
    }

    public void setSenhaAdmin(String senhaAdmin) {
        this.senhaAdmin = senhaAdmin;
    }

    // =========================================================================
    // Utilitários
    // =========================================================================

    @Override
    public String toString() {
        return "SetupConfig{"
                + "nomeCampus='" + nomeCampus + '\''
                + ", siglaCampus='" + siglaCampus + '\''
                + ", cidadeCampus='" + cidadeCampus + '\''
                + ", estadoCampus='" + estadoCampus + '\''
                + ", responsavelTecnico='" + responsavelTecnico + '\''
                + ", contato='" + contato + '\''
                + ", portaApi=" + portaApi
                + ", hostPg='" + hostPg + '\''
                + ", portaPg=" + portaPg
                + ", banco='" + banco + '\''
                + ", usuarioPg='" + usuarioPg + '\''
                + ", senhaPg='[PROTEGIDA]'"
                + ", senhaAdmin='[PROTEGIDA]'"
                + '}';
    }
}
