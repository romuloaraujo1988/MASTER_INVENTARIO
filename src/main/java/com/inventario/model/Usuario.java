package com.inventario.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade que representa um Usuário do sistema
 * Corresponde à TABELA_USUARIO no banco de dados
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class Usuario {

    private Integer id;
    private String login;
    private String senhaHash;
    private String nomeCompleto;
    private String email;
    private String matricula;
    private PerfilUsuario perfil;
    private Integer idSetor;
    private String nomeSetor; // Para JOIN com TABELA_SETOR
    private Boolean ativo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimoAcesso;
    private Integer tentativasLogin;
    private Boolean bloqueado;
    private LocalDateTime dataBloqueio;
    private LocalDateTime dataExpiracaoSenha;
    private Boolean primeiroAcesso;
    private String observacoes;
    private LocalDateTime dataAtualizacao;

    // Construtores
    public Usuario() {
        this.ativo = true;
        this.tentativasLogin = 0;
        this.bloqueado = false;
        this.primeiroAcesso = true;
        this.dataCriacao = LocalDateTime.now();
        this.dataExpiracaoSenha = LocalDateTime.now().plusDays(90);
    }

    public Usuario(String login, String senhaHash, String nomeCompleto, String email, String matricula, PerfilUsuario perfil) {
        this();
        this.login = login;
        this.senhaHash = senhaHash;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.matricula = matricula;
        this.perfil = perfil;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public Integer getIdSetor() {
        return idSetor;
    }

    public void setIdSetor(Integer idSetor) {
        this.idSetor = idSetor;
    }

    public String getNomeSetor() {
        return nomeSetor;
    }

    public void setNomeSetor(String nomeSetor) {
        this.nomeSetor = nomeSetor;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataUltimoAcesso() {
        return dataUltimoAcesso;
    }

    public void setDataUltimoAcesso(LocalDateTime dataUltimoAcesso) {
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    public Integer getTentativasLogin() {
        return tentativasLogin;
    }

    public void setTentativasLogin(Integer tentativasLogin) {
        this.tentativasLogin = tentativasLogin;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public LocalDateTime getDataBloqueio() {
        return dataBloqueio;
    }

    public void setDataBloqueio(LocalDateTime dataBloqueio) {
        this.dataBloqueio = dataBloqueio;
    }

    public LocalDateTime getDataExpiracaoSenha() {
        return dataExpiracaoSenha;
    }

    public void setDataExpiracaoSenha(LocalDateTime dataExpiracaoSenha) {
        this.dataExpiracaoSenha = dataExpiracaoSenha;
    }

    public Boolean getPrimeiroAcesso() {
        return primeiroAcesso;
    }

    public void setPrimeiroAcesso(Boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // Métodos utilitários
    public void bloquear() {
        this.bloqueado = true;
        this.dataBloqueio = LocalDateTime.now();
    }

    public void desbloquear() {
        this.bloqueado = false;
        this.dataBloqueio = null;
        this.tentativasLogin = 0;
    }

    public void incrementarTentativasLogin() {
        this.tentativasLogin++;
    }

    public void resetarTentativasLogin() {
        this.tentativasLogin = 0;
    }

    public void registrarAcesso() {
        this.dataUltimoAcesso = LocalDateTime.now();
        this.resetarTentativasLogin();
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuario.ADMIN;
    }

    public boolean isColetor() {
        return perfil == PerfilUsuario.COLETOR;
    }

    public boolean isConsultor() {
        return perfil == PerfilUsuario.CONSULTA;
    }

    public void marcarPrimeiroAcessoRealizado() {
        this.primeiroAcesso = false;
    }

    public boolean precisaTrocarSenha() {
        return Boolean.TRUE.equals(primeiroAcesso) || 
               (dataExpiracaoSenha != null && dataExpiracaoSenha.isBefore(LocalDateTime.now()));
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", nomeCompleto='" + nomeCompleto + '\'' +
                ", email='" + email + '\'' +
                ", matricula='" + matricula + '\'' +
                ", perfil=" + perfil +
                ", ativo=" + ativo +
                ", bloqueado=" + bloqueado +
                ", tentativasLogin=" + tentativasLogin +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) &&
               Objects.equals(login, usuario.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}