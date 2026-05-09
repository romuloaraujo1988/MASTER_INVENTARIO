package com.inventario.sihcp.mobile.server.dto;

import java.util.List;

/**
 * DTO para sincronização offline completa
 * Retorna TODOS os dados necessários para o app funcionar offline
 * 
 * CRÍTICO: Com 10.000+ patrimônios, este endpoint deve ser otimizado
 * 
 * v2.2: Endpoint dedicado para sincronização offline
 */
public class MobileOfflineDataDTO {
    
    private List<PatrimonioOfflineDTO> patrimonios;
    private List<SalaOfflineDTO> salas;
    private List<ResponsavelOfflineDTO> responsaveis;
    private MetadataDTO metadata;
    
    public MobileOfflineDataDTO() {
    }
    
    public MobileOfflineDataDTO(
        List<PatrimonioOfflineDTO> patrimonios,
        List<SalaOfflineDTO> salas,
        List<ResponsavelOfflineDTO> responsaveis,
        MetadataDTO metadata
    ) {
        this.patrimonios = patrimonios;
        this.salas = salas;
        this.responsaveis = responsaveis;
        this.metadata = metadata;
    }
    
    // Getters e Setters
    public List<PatrimonioOfflineDTO> getPatrimonios() {
        return patrimonios;
    }
    
    public void setPatrimonios(List<PatrimonioOfflineDTO> patrimonios) {
        this.patrimonios = patrimonios;
    }
    
    public List<SalaOfflineDTO> getSalas() {
        return salas;
    }
    
    public void setSalas(List<SalaOfflineDTO> salas) {
        this.salas = salas;
    }
    
    public List<ResponsavelOfflineDTO> getResponsaveis() {
        return responsaveis;
    }
    
    public void setResponsaveis(List<ResponsavelOfflineDTO> responsaveis) {
        this.responsaveis = responsaveis;
    }
    
    public MetadataDTO getMetadata() {
        return metadata;
    }
    
    public void setMetadata(MetadataDTO metadata) {
        this.metadata = metadata;
    }
    
    /**
     * DTO simplificado de patrimônio para offline
     * Apenas campos essenciais para reduzir tamanho
     */
    public static class PatrimonioOfflineDTO {
        private Long id;
        private String numeroPatrimonio;
        private String descricao;
        private String marca;
        private String modelo;
        private String estado;
        private Integer salaId;
        private String salaNome;
        private Integer responsavelId;
        private String responsavelNome;
        private Boolean coletado;
        
        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getNumeroPatrimonio() { return numeroPatrimonio; }
        public void setNumeroPatrimonio(String numeroPatrimonio) { this.numeroPatrimonio = numeroPatrimonio; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        
        public Integer getSalaId() { return salaId; }
        public void setSalaId(Integer salaId) { this.salaId = salaId; }
        
        public String getSalaNome() { return salaNome; }
        public void setSalaNome(String salaNome) { this.salaNome = salaNome; }
        
        public Integer getResponsavelId() { return responsavelId; }
        public void setResponsavelId(Integer responsavelId) { this.responsavelId = responsavelId; }
        
        public String getResponsavelNome() { return responsavelNome; }
        public void setResponsavelNome(String responsavelNome) { this.responsavelNome = responsavelNome; }
        
        public Boolean getColetado() { return coletado; }
        public void setColetado(Boolean coletado) { this.coletado = coletado; }
    }
    
    /**
     * DTO simplificado de sala para offline
     */
    public static class SalaOfflineDTO {
        private Integer id;
        private String nome;
        private Boolean ativa;
        
        // Getters e Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public Boolean getAtiva() { return ativa; }
        public void setAtiva(Boolean ativa) { this.ativa = ativa; }
    }
    
    /**
     * DTO simplificado de responsável para offline
     */
    public static class ResponsavelOfflineDTO {
        private Integer id;
        private String nome;
        private String cpf;
        
        // Getters e Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
    }
    
    /**
     * Metadados da sincronização
     */
    public static class MetadataDTO {
        private Long timestamp;
        private Integer totalPatrimonios;
        private Integer totalSalas;
        private Integer totalResponsaveis;
        private Integer inventarioAtivoId;
        private String inventarioAtivoNome;
        private String versaoServidor;
        
        // Getters e Setters
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        
        public Integer getTotalPatrimonios() { return totalPatrimonios; }
        public void setTotalPatrimonios(Integer totalPatrimonios) { this.totalPatrimonios = totalPatrimonios; }
        
        public Integer getTotalSalas() { return totalSalas; }
        public void setTotalSalas(Integer totalSalas) { this.totalSalas = totalSalas; }
        
        public Integer getTotalResponsaveis() { return totalResponsaveis; }
        public void setTotalResponsaveis(Integer totalResponsaveis) { this.totalResponsaveis = totalResponsaveis; }
        
        public Integer getInventarioAtivoId() { return inventarioAtivoId; }
        public void setInventarioAtivoId(Integer inventarioAtivoId) { this.inventarioAtivoId = inventarioAtivoId; }
        
        public String getInventarioAtivoNome() { return inventarioAtivoNome; }
        public void setInventarioAtivoNome(String inventarioAtivoNome) { this.inventarioAtivoNome = inventarioAtivoNome; }
        
        public String getVersaoServidor() { return versaoServidor; }
        public void setVersaoServidor(String versaoServidor) { this.versaoServidor = versaoServidor; }
    }
}
