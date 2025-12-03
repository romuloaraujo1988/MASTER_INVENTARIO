package com.inventario.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;
import java.util.Objects;

/**
 * Classe que representa uma Coleta no sistema de inventário
 * Registra as coletas realizadas durante o processo de inventário
 */
public class Coleta {
    
    private int id;
    private int idInventario;
    private int idPatrimonio;
    private int idColetor; // Campo mantido para compatibilidade durante migração
    private int idParticipanteInventario; // Nova referência para TABELA_PARTICIPANTE_INVENTARIO
    private Timestamp dataColeta;
    private String statusColeta;
    private String observacaoColeta;
    private String localizacaoAtual;
    private String localizacaoEncontrada;
    private String estadoEncontrado;
    private boolean divergencia;
    private String motivoDivergencia;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String fotoPatrimonio;
    
    // Campos para itens sem etiqueta
    private boolean semEtiqueta;
    private String descricaoItemSemEtiqueta;
    private String categoriaItemSemEtiqueta;
    
    // Campos transientes para exibição
    private String numeroPatrimonio;
    private String descricaoPatrimonio;
    private String nomeColetor;
    private String descricaoInventario;
    private String nomeSala;  // Nome da sala original do patrimônio (do JOIN)
    
    // Formatação de datas centralizada em DateFormatUtils
    
    // Constantes para status de coleta
    public static final String STATUS_COLETADO = "COLETADO";
    public static final String STATUS_NAO_ENCONTRADO = "NAO_ENCONTRADO";
    public static final String STATUS_DANIFICADO = "DANIFICADO";
    public static final String STATUS_DIVERGENCIA = "DIVERGENCIA";
    
    // Constantes para estados de conservação
    public static final String ESTADO_OTIMO = "Ótimo";
    public static final String ESTADO_BOM = "Bom";
    public static final String ESTADO_REGULAR = "Regular";
    public static final String ESTADO_RUIM = "Ruim";
    public static final String ESTADO_PESSIMO = "Péssimo";
    
    // Construtores
    public Coleta() {
        this.dataColeta = new Timestamp(System.currentTimeMillis());
        this.statusColeta = STATUS_COLETADO;
        this.divergencia = false;
    }
    
    public Coleta(int idInventario, int idPatrimonio, int idColetor) {
        this();
        this.idInventario = idInventario;
        this.idPatrimonio = idPatrimonio;
        this.idColetor = idColetor;
    }
    
    public Coleta(int idInventario, int idPatrimonio, int idParticipanteInventario, boolean useParticipante) {
        this();
        this.idInventario = idInventario;
        this.idPatrimonio = idPatrimonio;
        this.idParticipanteInventario = idParticipanteInventario;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(int idInventario) {
        this.idInventario = idInventario;
    }
    
    public int getIdPatrimonio() {
        return idPatrimonio;
    }
    
    public void setIdPatrimonio(int idPatrimonio) {
        this.idPatrimonio = idPatrimonio;
    }
    
    public int getIdColetor() {
        return idColetor;
    }
    
    public void setIdColetor(int idColetor) {
        this.idColetor = idColetor;
    }
    
    public int getIdParticipanteInventario() {
        return idParticipanteInventario;
    }
    
    public void setIdParticipanteInventario(int idParticipanteInventario) {
        this.idParticipanteInventario = idParticipanteInventario;
    }
    
    public Timestamp getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(Timestamp dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public String getStatusColeta() {
        return statusColeta;
    }
    
    public void setStatusColeta(String statusColeta) {
        this.statusColeta = statusColeta;
    }
    
    public String getObservacaoColeta() {
        return observacaoColeta;
    }
    
    public void setObservacaoColeta(String observacaoColeta) {
        this.observacaoColeta = observacaoColeta;
    }
    
    public String getLocalizacaoAtual() {
        return localizacaoAtual;
    }
    
    public void setLocalizacaoAtual(String localizacaoAtual) {
        this.localizacaoAtual = localizacaoAtual;
    }
    
    public String getLocalizacaoEncontrada() {
        return localizacaoEncontrada;
    }
    
    public void setLocalizacaoEncontrada(String localizacaoEncontrada) {
        this.localizacaoEncontrada = localizacaoEncontrada;
    }
    
    public String getEstadoEncontrado() {
        return estadoEncontrado;
    }
    
    public void setEstadoEncontrado(String estadoEncontrado) {
        this.estadoEncontrado = estadoEncontrado;
    }
    
    public boolean isDivergencia() {
        return divergencia;
    }
    
    public void setDivergencia(boolean divergencia) {
        this.divergencia = divergencia;
    }
    
    public String getMotivoDivergencia() {
        return motivoDivergencia;
    }
    
    public void setMotivoDivergencia(String motivoDivergencia) {
        this.motivoDivergencia = motivoDivergencia;
    }
    
    public BigDecimal getLatitude() {
        return latitude;
    }
    
    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }
    
    public BigDecimal getLongitude() {
        return longitude;
    }
    
    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
    
    public String getFotoPatrimonio() {
        return fotoPatrimonio;
    }
    
    public void setFotoPatrimonio(String fotoPatrimonio) {
        this.fotoPatrimonio = fotoPatrimonio;
    }
    
    // Getters e Setters para itens sem etiqueta
    public boolean isSemEtiqueta() {
        return semEtiqueta;
    }
    
    public void setSemEtiqueta(boolean semEtiqueta) {
        this.semEtiqueta = semEtiqueta;
    }
    
    public String getDescricaoItemSemEtiqueta() {
        return descricaoItemSemEtiqueta;
    }
    
    public void setDescricaoItemSemEtiqueta(String descricaoItemSemEtiqueta) {
        this.descricaoItemSemEtiqueta = descricaoItemSemEtiqueta;
    }
    
    public String getCategoriaItemSemEtiqueta() {
        return categoriaItemSemEtiqueta;
    }
    
    public void setCategoriaItemSemEtiqueta(String categoriaItemSemEtiqueta) {
        this.categoriaItemSemEtiqueta = categoriaItemSemEtiqueta;
    }
    
    // Campos transientes
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public String getDescricaoPatrimonio() {
        return descricaoPatrimonio;
    }
    
    public void setDescricaoPatrimonio(String descricaoPatrimonio) {
        this.descricaoPatrimonio = descricaoPatrimonio;
    }
    
    public String getNomeColetor() {
        return nomeColetor;
    }
    
    public void setNomeColetor(String nomeColetor) {
        this.nomeColetor = nomeColetor;
    }
    
    public String getDescricaoInventario() {
        return descricaoInventario;
    }
    
    public void setDescricaoInventario(String descricaoInventario) {
        this.descricaoInventario = descricaoInventario;
    }
    
    public String getNomeSala() {
        return nomeSala;
    }
    
    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }
    
    // Métodos utilitários
    public String getDataColetaFormatada() {
        return DateFormatUtils.formatWithDefault(dataColeta, "Não informado");
    }
    
    public String getStatusColetaDescricao() {
        switch (statusColeta != null ? statusColeta : "") {
            case STATUS_COLETADO:
                return "Coletado";
            case STATUS_NAO_ENCONTRADO:
                return "Não Encontrado";
            case STATUS_DANIFICADO:
                return "Danificado";
            case STATUS_DIVERGENCIA:
                return "Divergência";
            default:
                return statusColeta;
        }
    }
    
    public String getDivergenciaDescricao() {
        return divergencia ? "Sim" : "Não";
    }
    
    public boolean temCoordenadas() {
        return latitude != null && longitude != null;
    }
    
    public String getCoordenadasFormatadas() {
        if (temCoordenadas()) {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
        return "";
    }
    
    public boolean temFoto() {
        return fotoPatrimonio != null && !fotoPatrimonio.trim().isEmpty();
    }
    
    public boolean temDivergenciaLocalizacao() {
        return localizacaoAtual != null && localizacaoEncontrada != null &&
               !localizacaoAtual.equals(localizacaoEncontrada);
    }
    
    // Métodos utilitários para itens sem etiqueta
    public boolean isItemSemEtiquetaValido() {
        if (!semEtiqueta) {
            return true; // Se não é sem etiqueta, não precisa validar
        }
        
        // Para itens sem etiqueta, validar campos obrigatórios
        return descricaoItemSemEtiqueta != null && 
               descricaoItemSemEtiqueta.trim().length() >= 10 &&
               categoriaItemSemEtiqueta != null && 
               !categoriaItemSemEtiqueta.trim().isEmpty() &&
               localizacaoEncontrada != null && 
               !localizacaoEncontrada.trim().isEmpty();
    }
    
    public String getDescricaoCompleta() {
        if (semEtiqueta) {
            return descricaoItemSemEtiqueta != null ? descricaoItemSemEtiqueta : "Item sem etiqueta";
        } else {
            return descricaoPatrimonio != null ? descricaoPatrimonio : "";
        }
    }
    
    public String getIdentificacao() {
        if (semEtiqueta) {
            return "SEM ETIQUETA - " + (categoriaItemSemEtiqueta != null ? categoriaItemSemEtiqueta : "Não categorizado");
        } else {
            return numeroPatrimonio != null ? numeroPatrimonio : "";
        }
    }
    
    // Métodos de validação
    public boolean isValida() {
        // Validação básica
        boolean validacaoBasica = idInventario > 0 && idColetor > 0 &&
                                 statusColeta != null && !statusColeta.trim().isEmpty();
        
        if (!validacaoBasica) {
            return false;
        }
        
        // Se é item sem etiqueta, validar campos específicos
        if (semEtiqueta) {
            return isItemSemEtiquetaValido();
        } else {
            // Se não é sem etiqueta, deve ter patrimônio
            return idPatrimonio > 0;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coleta coleta = (Coleta) obj;
        return id == coleta.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Coleta{id=%d, patrimonio=%s, coletor=%s, status=%s, data=%s}",
                id, numeroPatrimonio, nomeColetor, statusColeta, getDataColetaFormatada());
    }
}