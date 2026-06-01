package com.inventario.sihcp.mobile.server.dto;

// Validações removidas - feitas no service para suportar coletas sem etiqueta

/**
 * DTO para requisição de registro de coleta mobile
 * 
 * Suporta dois tipos de coleta:
 * 1. Coleta COM etiqueta: numeroPatrimonio é obrigatório
 * 2. Coleta SEM etiqueta: numeroPatrimonio pode ser vazio, mas descricaoItemSemEtiqueta é obrigatório
 * 
 * @author Sistema de Inventário
 * @version 1.1.0
 */
public class MobileColetaRequest {
    
    // Removido @NotBlank - validação customizada no controller/service
    // Para coletas sem etiqueta, este campo pode ser vazio
    private String numeroPatrimonio;
    
    // ID do inventário é opcional - se não informado, usa o inventário ativo
    private Integer idInventario;
    
    // Removido @NotNull - pode vir do contexto de autenticação
    private Integer usuarioId;
    
    private Integer idSala;
    
    private String localizacaoEncontrada;
    
    // Removido @NotBlank - pode ser vazio em coletas antigas offline
    private String estadoEncontrado;
    
    private String observacaoColeta;
    
    private String dataColeta;  // Aceita tanto ISO 8601 quanto timestamp
    
    private Double latitude;
    
    private Double longitude;
    
    private String fotoPatrimonio;
    
    private Boolean semEtiqueta;
    
    private String descricaoItemSemEtiqueta;
    
    private String categoriaItemSemEtiqueta;
    
    private String deviceId;
    
    private String appVersion;
    
    // Métricas de tempo (Analytics)
    private Integer tempoColetaSegundos;
    private Integer tempoScanSegundos;
    private Integer tempoPreenchimentoSegundos;
    private String metodoColeta;
    private String tipoScan;

    /**
     * B3 (auditoria-sync — maio/2026): UUID gerado pelo cliente (app Android)
     * no momento da coleta. Serve como chave de idempotência — o servidor
     * verifica antes do INSERT e, se já existir coleta com o mesmo UUID,
     * retorna a existente em vez de criar duplicata. Elimina o risco de
     * duplicatas causadas por timeout de rede (servidor persiste a coleta,
     * mas a resposta não chega ao app, que reenvia a mesma coleta).
     *
     * Opcional para compatibilidade com clientes antigos que não enviam
     * o campo. Quando nulo, o servidor cai no fluxo antigo de verificação
     * por `numeroPatrimonio + idInventario`.
     */
    private String clienteUuid;
    
    /**
     * Divergência detectada pelo cliente (app Android) ao comparar o item
     * coletado com o cadastro: localização e/ou estado diferentes.
     *
     * Opcional para compatibilidade com clientes antigos. O servidor
     * RECALCULA a divergência a partir dos dados cadastrados do patrimônio
     * (ver {@code MobileColetaService}), usando este valor apenas como
     * fallback quando o recálculo não for possível (ex.: patrimônio sem
     * sala/estado cadastrado). Assim a fonte de verdade é o servidor.
     */
    private Boolean divergencia;

    /**
     * Motivo da divergência, quando houver:
     * {@code LOCALIZACAO_DIFERENTE}, {@code ESTADO_DIFERENTE} ou
     * {@code LOCALIZACAO_E_ESTADO}. Também recalculado no servidor.
     */
    private String motivoDivergencia;
    
    // Construtores
    public MobileColetaRequest() {
        this.dataColeta = String.valueOf(System.currentTimeMillis());
        this.semEtiqueta = false;
    }
    
    // Getters e Setters
    public String getNumeroPatrimonio() {
        return numeroPatrimonio;
    }
    
    public void setNumeroPatrimonio(String numeroPatrimonio) {
        this.numeroPatrimonio = numeroPatrimonio;
    }
    
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Integer getIdSala() {
        return idSala;
    }
    
    public void setIdSala(Integer idSala) {
        this.idSala = idSala;
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
    
    public String getObservacaoColeta() {
        return observacaoColeta;
    }
    
    public void setObservacaoColeta(String observacaoColeta) {
        this.observacaoColeta = observacaoColeta;
    }
    
    public String getDataColeta() {
        return dataColeta;
    }
    
    public void setDataColeta(String dataColeta) {
        this.dataColeta = dataColeta;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getFotoPatrimonio() {
        return fotoPatrimonio;
    }
    
    public void setFotoPatrimonio(String fotoPatrimonio) {
        this.fotoPatrimonio = fotoPatrimonio;
    }
    
    public Boolean getSemEtiqueta() {
        return semEtiqueta;
    }
    
    public void setSemEtiqueta(Boolean semEtiqueta) {
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
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    public Integer getTempoColetaSegundos() {
        return tempoColetaSegundos;
    }
    
    public void setTempoColetaSegundos(Integer tempoColetaSegundos) {
        this.tempoColetaSegundos = tempoColetaSegundos;
    }
    
    public Integer getTempoScanSegundos() {
        return tempoScanSegundos;
    }
    
    public void setTempoScanSegundos(Integer tempoScanSegundos) {
        this.tempoScanSegundos = tempoScanSegundos;
    }
    
    public Integer getTempoPreenchimentoSegundos() {
        return tempoPreenchimentoSegundos;
    }
    
    public void setTempoPreenchimentoSegundos(Integer tempoPreenchimentoSegundos) {
        this.tempoPreenchimentoSegundos = tempoPreenchimentoSegundos;
    }
    
    public String getMetodoColeta() {
        return metodoColeta;
    }
    
    public void setMetodoColeta(String metodoColeta) {
        this.metodoColeta = metodoColeta;
    }
    
    public String getTipoScan() {
        return tipoScan;
    }
    
    public void setTipoScan(String tipoScan) {
        this.tipoScan = tipoScan;
    }

    public String getClienteUuid() {
        return clienteUuid;
    }

    public void setClienteUuid(String clienteUuid) {
        this.clienteUuid = clienteUuid;
    }

    public Boolean getDivergencia() {
        return divergencia;
    }

    public void setDivergencia(Boolean divergencia) {
        this.divergencia = divergencia;
    }

    public String getMotivoDivergencia() {
        return motivoDivergencia;
    }

    public void setMotivoDivergencia(String motivoDivergencia) {
        this.motivoDivergencia = motivoDivergencia;
    }
}
