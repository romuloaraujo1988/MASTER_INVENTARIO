package com.inventario.mobile.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para resposta de sincronização mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class MobileSyncResponse {
    
    @JsonProperty("syncTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime syncTime;
    
    @JsonProperty("patrimoniosAtualizados")
    private List<MobilePatrimonioDTO> patrimoniosAtualizados;
    
    @JsonProperty("patrimoniosNovos")
    private List<MobilePatrimonioDTO> patrimoniosNovos;
    
    @JsonProperty("patrimoniosRemovidos")
    private List<Long> patrimoniosRemovidos;
    
    @JsonProperty("totalProcessados")
    private Integer totalProcessados;
    
    @JsonProperty("totalErros")
    private Integer totalErros;
    
    @JsonProperty("erros")
    private List<String> erros;
    
    // Construtores
    public MobileSyncResponse() {
        this.syncTime = LocalDateTime.now();
    }
    
    public MobileSyncResponse(List<MobilePatrimonioDTO> patrimoniosAtualizados,
                            List<MobilePatrimonioDTO> patrimoniosNovos,
                            List<Long> patrimoniosRemovidos) {
        this();
        this.patrimoniosAtualizados = patrimoniosAtualizados;
        this.patrimoniosNovos = patrimoniosNovos;
        this.patrimoniosRemovidos = patrimoniosRemovidos;
    }
    
    // Getters e Setters
    public LocalDateTime getSyncTime() {
        return syncTime;
    }
    
    public void setSyncTime(LocalDateTime syncTime) {
        this.syncTime = syncTime;
    }
    
    public List<MobilePatrimonioDTO> getPatrimoniosAtualizados() {
        return patrimoniosAtualizados;
    }
    
    public void setPatrimoniosAtualizados(List<MobilePatrimonioDTO> patrimoniosAtualizados) {
        this.patrimoniosAtualizados = patrimoniosAtualizados;
    }
    
    public List<MobilePatrimonioDTO> getPatrimoniosNovos() {
        return patrimoniosNovos;
    }
    
    public void setPatrimoniosNovos(List<MobilePatrimonioDTO> patrimoniosNovos) {
        this.patrimoniosNovos = patrimoniosNovos;
    }
    
    public List<Long> getPatrimoniosRemovidos() {
        return patrimoniosRemovidos;
    }
    
    public void setPatrimoniosRemovidos(List<Long> patrimoniosRemovidos) {
        this.patrimoniosRemovidos = patrimoniosRemovidos;
    }
    
    public Integer getTotalProcessados() {
        return totalProcessados;
    }
    
    public void setTotalProcessados(Integer totalProcessados) {
        this.totalProcessados = totalProcessados;
    }
    
    public Integer getTotalErros() {
        return totalErros;
    }
    
    public void setTotalErros(Integer totalErros) {
        this.totalErros = totalErros;
    }
    
    public List<String> getErros() {
        return erros;
    }
    
    public void setErros(List<String> erros) {
        this.erros = erros;
    }
}