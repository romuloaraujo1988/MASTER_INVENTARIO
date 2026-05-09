package com.inventario.sihcp.mobile.server.dto;

/**
 * DTO com as informações públicas do campus para o app Android.
 *
 * <p>Retornado pelo endpoint {@code GET /api/mobile/campus/info} (público, sem
 * autenticação), permitindo que o app identifique o campus ao qual está
 * conectado antes de realizar o login.</p>
 *
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class CampusInfoDTO {

    private String nome;
    private String sigla;
    private String cidade;

    public CampusInfoDTO() {
    }

    public CampusInfoDTO(String nome, String sigla, String cidade) {
        this.nome = nome;
        this.sigla = sigla;
        this.cidade = cidade;
    }

    // ─── Getters e Setters ────────────────────────────────────────────────────

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    @Override
    public String toString() {
        return "CampusInfoDTO{nome='" + nome + "', sigla='" + sigla + "', cidade='" + cidade + "'}";
    }
}
