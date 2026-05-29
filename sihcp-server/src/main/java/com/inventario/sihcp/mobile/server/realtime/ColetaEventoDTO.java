package com.inventario.sihcp.mobile.server.realtime;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventario.sihcp.mobile.server.dto.MobileColetaResponse;

/**
 * Evento de coleta para broadcast em tempo real via WebSocket.
 *
 * <p>Snapshot enxuto de uma coleta recém-registrada — somente os campos
 * necessários para os clientes (app Android no modo "monitor ao vivo")
 * exibirem o item na lista sem precisar fazer um GET adicional.</p>
 *
 * <p>Por que um DTO próprio em vez de reutilizar
 * {@link MobileColetaResponse}? Três motivos:</p>
 * <ul>
 *   <li><b>Versionamento independente</b> — o contrato do WebSocket pode
 *       evoluir sem mexer no payload do POST de coleta (que é regulado por
 *       smoke tests rigorosos em <code>MobileColetaControllerContratoSmokeTest</code>).</li>
 *   <li><b>Tamanho menor</b> — broadcast multiplica o tráfego pelo número de
 *       clientes conectados. Cada byte conta.</li>
 *   <li><b>Sem campos sensíveis</b> — não enviamos {@code senhaHash},
 *       {@code observacaoColeta} ou outros dados que possam vazar em
 *       conexões mal autenticadas.</li>
 * </ul>
 *
 * <p>O envelope inclui um campo {@code tipo} para suportar futuras extensões
 * (ex.: {@code "COLETA_EXCLUIDA"}, {@code "INVENTARIO_FINALIZADO"}) sem
 * quebrar clientes antigos — basta ignorar tipos desconhecidos no app.</p>
 */
public class ColetaEventoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Tipo do evento — sempre {@code "COLETA_REGISTRADA"} nesta versão. */
    @JsonProperty("tipo")
    private String tipo = "COLETA_REGISTRADA";

    /** ID da coleta criada no banco. */
    @JsonProperty("id")
    private Long id;

    /** Inventário ao qual a coleta pertence (usado para filtrar broadcast). */
    @JsonProperty("inventarioId")
    private Integer inventarioId;

    @JsonProperty("numeroPatrimonio")
    private String numeroPatrimonio;

    @JsonProperty("descricaoPatrimonio")
    private String descricaoPatrimonio;

    @JsonProperty("nomeColetor")
    private String nomeColetor;

    @JsonProperty("nomeSala")
    private String nomeSala;

    @JsonProperty("estadoEncontrado")
    private String estadoEncontrado;

    /** Data formatada (mesma string usada no MobileColetaResponse). */
    @JsonProperty("dataColeta")
    private String dataColeta;

    @JsonProperty("semEtiqueta")
    private Boolean semEtiqueta;

    @JsonProperty("descricaoItemSemEtiqueta")
    private String descricaoItemSemEtiqueta;

    /** Indica se há foto disponível para download. */
    @JsonProperty("temFoto")
    private Boolean temFoto;

    public ColetaEventoDTO() {
    }

    /**
     * Constrói o evento a partir de uma {@link MobileColetaResponse} já populada.
     * Mantém um único ponto de tradução resposta-HTTP → evento-WS para evitar
     * divergências de campos entre os dois canais.
     */
    public static ColetaEventoDTO fromResponse(MobileColetaResponse response) {
        if (response == null) {
            return null;
        }
        ColetaEventoDTO dto = new ColetaEventoDTO();
        dto.id = response.getId();
        dto.inventarioId = response.getIdInventario();
        dto.numeroPatrimonio = response.getNumeroPatrimonio();
        dto.descricaoPatrimonio = response.getDescricaoPatrimonio();
        dto.nomeColetor = response.getNomeColetor();
        dto.nomeSala = response.getNomeSala();
        dto.estadoEncontrado = response.getEstadoEncontrado();
        dto.dataColeta = response.getDataColeta();
        dto.semEtiqueta = response.getSemEtiqueta();
        dto.descricaoItemSemEtiqueta = response.getDescricaoItemSemEtiqueta();
        dto.temFoto = response.getTemFoto();
        return dto;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getInventarioId() { return inventarioId; }
    public void setInventarioId(Integer inventarioId) { this.inventarioId = inventarioId; }

    public String getNumeroPatrimonio() { return numeroPatrimonio; }
    public void setNumeroPatrimonio(String numeroPatrimonio) { this.numeroPatrimonio = numeroPatrimonio; }

    public String getDescricaoPatrimonio() { return descricaoPatrimonio; }
    public void setDescricaoPatrimonio(String descricaoPatrimonio) { this.descricaoPatrimonio = descricaoPatrimonio; }

    public String getNomeColetor() { return nomeColetor; }
    public void setNomeColetor(String nomeColetor) { this.nomeColetor = nomeColetor; }

    public String getNomeSala() { return nomeSala; }
    public void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }

    public String getEstadoEncontrado() { return estadoEncontrado; }
    public void setEstadoEncontrado(String estadoEncontrado) { this.estadoEncontrado = estadoEncontrado; }

    public String getDataColeta() { return dataColeta; }
    public void setDataColeta(String dataColeta) { this.dataColeta = dataColeta; }

    public Boolean getSemEtiqueta() { return semEtiqueta; }
    public void setSemEtiqueta(Boolean semEtiqueta) { this.semEtiqueta = semEtiqueta; }

    public String getDescricaoItemSemEtiqueta() { return descricaoItemSemEtiqueta; }
    public void setDescricaoItemSemEtiqueta(String descricaoItemSemEtiqueta) {
        this.descricaoItemSemEtiqueta = descricaoItemSemEtiqueta;
    }

    public Boolean getTemFoto() { return temFoto; }
    public void setTemFoto(Boolean temFoto) { this.temFoto = temFoto; }
}
