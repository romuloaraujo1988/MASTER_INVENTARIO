package com.inventario.sihcp.dto;

/**
 * Enum para representar os formatos de exportação disponíveis para o histórico.
 */
public enum FormatoExportacao {
    
    PDF("PDF", "application/pdf", ".pdf"),
    EXCEL("Excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
    
    private final String descricao;
    private final String mimeType;
    private final String extensao;
    
    FormatoExportacao(String descricao, String mimeType, String extensao) {
        this.descricao = descricao;
        this.mimeType = mimeType;
        this.extensao = extensao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getExtensao() {
        return extensao;
    }
    
    /**
     * Retorna o formato baseado na extensão do arquivo
     */
    public static FormatoExportacao fromExtensao(String extensao) {
        for (FormatoExportacao formato : values()) {
            if (formato.getExtensao().equalsIgnoreCase(extensao)) {
                return formato;
            }
        }
        throw new IllegalArgumentException("Extensão não suportada: " + extensao);
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
