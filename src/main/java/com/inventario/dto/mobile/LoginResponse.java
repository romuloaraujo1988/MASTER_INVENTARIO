package com.inventario.dto.mobile;

import com.inventario.model.PerfilUsuario;

/**
 * DTO para resposta de login da API Mobile
 */
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UsuarioInfo usuario;

    // Construtores
    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, Long expiresIn, UsuarioInfo usuario) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.usuario = usuario;
    }

    // Getters e Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UsuarioInfo getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioInfo usuario) {
        this.usuario = usuario;
    }

    /**
     * Classe interna para informações do usuário
     */
    public static class UsuarioInfo {
        private Integer id;
        private String login;
        private String nomeCompleto;
        private String email;
        private String matricula;
        private PerfilUsuario perfil;
        private Integer idSetor;
        private String nomeSetor;

        // Construtores
        public UsuarioInfo() {}

        public UsuarioInfo(Integer id, String login, String nomeCompleto, String email, 
                          String matricula, PerfilUsuario perfil, Integer idSetor, String nomeSetor) {
            this.id = id;
            this.login = login;
            this.nomeCompleto = nomeCompleto;
            this.email = email;
            this.matricula = matricula;
            this.perfil = perfil;
            this.idSetor = idSetor;
            this.nomeSetor = nomeSetor;
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
    }
}