package com.inventario.sihcp.mobile.server.service;

import com.inventario.sihcp.dao.RelatorioFotoColetaDAO;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Gerador de Relatório Fotográfico de Coletas em PDF.
 *
 * <h2>Layout do relatório</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │  SIHCP — Relatório Fotográfico de Coletas                   │
 * │  Inventário #3 · Gerado em 08/05/2026 14:30 · 12 fotos     │
 * ├──────────────────────────────────────────────────────────────┤
 * │  [FOTO 150×112]  │  Nº 12345 — Cadeira Giratória            │
 * │                  │  📍 Local: Sala 101 (Bloco A)            │
 * │                  │  🏷️  Tipo: Patrimônio                    │
 * │                  │  ⚙️  Estado: BOM                         │
 * │                  │  📅 Data: 08/05/2026 10:30               │
 * │                  │  👤 Coletor: João Silva                  │
 * │                  │  📝 Obs: Patrimônio em bom estado        │
 * ├──────────────────────────────────────────────────────────────┤
 * │  [FOTO 150×112]  │  Item Sem Etiqueta — Cadeira de Escritório│
 * │                  │  📍 Local: Corredor Bloco B              │
 * │                  │  🏷️  Tipo: Sem Etiqueta · Móveis         │
 * │                  │  ⚙️  Estado: REGULAR                     │
 * │                  │  ...                                     │
 * └──────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * Cada card ocupa uma linha da tabela. Fotos ausentes no disco são
 * substituídas por um placeholder cinza com mensagem.
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@Service
public class RelatorioFotoPDFGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioFotoPDFGenerator.class);

    // Cores
    private static final DeviceRgb COR_CABECALHO    = new DeviceRgb(0x1A, 0x3A, 0x6B); // azul IFMT
    private static final DeviceRgb COR_SUBTITULO    = new DeviceRgb(0x2E, 0x6D, 0xA8);
    private static final DeviceRgb COR_LINHA_PAR    = new DeviceRgb(0xF5, 0xF7, 0xFA);
    private static final DeviceRgb COR_BORDA        = new DeviceRgb(0xCC, 0xD6, 0xE0);
    private static final DeviceRgb COR_BADGE_SE     = new DeviceRgb(0xE6, 0x7E, 0x22); // laranja sem etiqueta
    private static final DeviceRgb COR_BADGE_DIV    = new DeviceRgb(0xC0, 0x39, 0x2B); // vermelho divergência
    private static final DeviceRgb COR_BADGE_PAT    = new DeviceRgb(0x27, 0xAE, 0x60); // verde patrimônio

    // Dimensões da foto no PDF (pontos)
    private static final float FOTO_LARGURA  = 150f;
    private static final float FOTO_ALTURA   = 112f;

    @Autowired
    private RelatorioFotoColetaDAO relatorioFotoDAO;

    @Autowired
    private FotoColetaStorageService storageService;

    /**
     * Gera o PDF do relatório fotográfico e retorna os bytes.
     *
     * @param idInventario    ID do inventário
     * @param nomeInventario  Nome do inventário (para o cabeçalho)
     * @param tipo            Filtro de tipo: {@code todos}, {@code patrimonio},
     *                        {@code sem_etiqueta} ou {@code divergencia}
     * @param idSala          Filtro opcional por sala (null = todas)
     * @return bytes do PDF gerado
     * @throws Exception se ocorrer erro na geração
     */
    public byte[] gerarPDF(int idInventario, String nomeInventario,
                           String tipo, Integer idSala) throws Exception {

        List<Map<String, Object>> coletas = relatorioFotoDAO.buscarColetasComFoto(
                idInventario, tipo, idSala);

        logger.info("Gerando relatório fotográfico: inventário={}, tipo={}, sala={}, fotos={}",
                idInventario, tipo, idSala, coletas.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4)) {

            document.setMargins(30, 25, 30, 25);

            PdfFont fontNormal = carregarFonte("/fonts/font-regular.ttf");
            PdfFont fontBold   = carregarFonte("/fonts/font-bold.ttf");

            // ── Cabeçalho ────────────────────────────────────────────────────
            adicionarCabecalho(document, fontBold, fontNormal,
                    idInventario, nomeInventario, tipo, coletas.size());

            if (coletas.isEmpty()) {
                document.add(new Paragraph("Nenhuma coleta com foto encontrada para os filtros selecionados.")
                        .setFont(fontNormal).setFontSize(11)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(40));
                return baos.toByteArray();
            }

            // ── Cards de coleta ───────────────────────────────────────────────
            for (int i = 0; i < coletas.size(); i++) {
                Map<String, Object> coleta = coletas.get(i);
                boolean linhaEscura = (i % 2 == 1);
                adicionarCardColeta(document, fontBold, fontNormal, coleta, linhaEscura);
            }

            // ── Rodapé ────────────────────────────────────────────────────────
            document.add(new Paragraph(
                    "\nRelatorio gerado pelo SIHCP - Sistema de Historico e Coleta Patrimonial - IFMT")
                    .setFont(fontNormal).setFontSize(7)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(20));
        }

        logger.info("PDF gerado: {} bytes, {} cards", baos.size(), coletas.size());
        return baos.toByteArray();
    }

    // ─── Cabeçalho ────────────────────────────────────────────────────────────

    private void adicionarCabecalho(Document doc, PdfFont bold, PdfFont normal,
                                    int idInventario, String nomeInventario,
                                    String tipo, int total) throws IOException {

        // Título principal
        doc.add(new Paragraph("SIHCP — Relatório Fotográfico de Coletas")
                .setFont(bold).setFontSize(16)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(COR_CABECALHO)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10).setMarginBottom(0));

        // Subtítulo com metadados
        String tipoLabel = switch (tipo == null ? "todos" : tipo.toLowerCase()) {
            case "sem_etiqueta" -> "Itens Sem Etiqueta";
            case "divergencia"  -> "Divergências";
            case "patrimonio"   -> "Patrimônios";
            default             -> "Todos os Tipos";
        };

        String dataGeracao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        doc.add(new Paragraph(
                String.format("Inventário #%d — %s  ·  Filtro: %s  ·  %d foto(s)  ·  Gerado em %s",
                        idInventario,
                        nomeInventario != null ? nomeInventario : "",
                        tipoLabel, total, dataGeracao))
                .setFont(normal).setFontSize(9)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(COR_SUBTITULO)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5).setMarginBottom(12));
    }

    // ─── Card de coleta ───────────────────────────────────────────────────────

    private void adicionarCardColeta(Document doc, PdfFont bold, PdfFont normal,
                                     Map<String, Object> coleta, boolean linhaEscura) throws IOException {

        // Tabela de 2 colunas: foto | dados
        Table card = new Table(new float[]{FOTO_LARGURA, UnitValue.createPercentValue(100).getValue()})
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(6)
                .setBorder(new SolidBorder(COR_BORDA, 0.5f));

        com.itextpdf.kernel.colors.Color bgCard = linhaEscura ? COR_LINHA_PAR : ColorConstants.WHITE;

        // ── Célula da foto ────────────────────────────────────────────────────
        Cell celulaFoto = new Cell()
                .setWidth(FOTO_LARGURA)
                .setHeight(FOTO_ALTURA + 10)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBackgroundColor(bgCard)
                .setBorder(new SolidBorder(COR_BORDA, 0.5f))
                .setPadding(4);

        String fotoPath = (String) coleta.get("fotoPath");
        if (fotoPath != null && storageService.fotoExiste(fotoPath)) {
            try {
                byte[] fotoBytes = storageService.lerFoto(fotoPath);
                Image img = new Image(ImageDataFactory.create(fotoBytes))
                        .setWidth(FOTO_LARGURA - 8)
                        .setHeight(FOTO_ALTURA - 8)
                        .setAutoScale(false);
                celulaFoto.add(img);
            } catch (Exception e) {
                logger.warn("Erro ao carregar foto {}: {}", fotoPath, e.getMessage());
                celulaFoto.add(placeholderFoto(normal, "Erro ao carregar foto"));
            }
        } else {
            celulaFoto.add(placeholderFoto(normal, "Foto não disponível"));
        }

        card.addCell(celulaFoto);

        // ── Célula dos dados ──────────────────────────────────────────────────
        Cell celulaDados = new Cell()
                .setVerticalAlignment(VerticalAlignment.TOP)
                .setBackgroundColor(bgCard)
                .setBorder(new SolidBorder(COR_BORDA, 0.5f))
                .setPadding(8);

        String tipo              = (String) coleta.getOrDefault("tipo", "patrimonio");
        String numero            = (String) coleta.getOrDefault("numeroPatrimonio", "");
        String descricao         = (String) coleta.getOrDefault("descricao", "");
        String categoria         = (String) coleta.getOrDefault("categoria", "");
        String local             = (String) coleta.getOrDefault("localizacaoEncontrada", "");
        String estado            = (String) coleta.getOrDefault("estadoEncontrado", "");
        String coletor           = (String) coleta.getOrDefault("nomeColetor", "");
        String obs               = (String) coleta.getOrDefault("observacao", "");
        String salaOrigem        = (String) coleta.getOrDefault("nomeSalaOrigem", "");
        String motivoDivergencia = (String) coleta.get("motivoDivergencia"); // pode ser null
        Object dataObj           = coleta.get("dataColeta");

        // Garantir que nenhum campo seja null (COALESCE no SQL pode falhar com dados legados)
        if (tipo == null)     tipo = "patrimonio";
        if (numero == null)   numero = "";
        if (descricao == null) descricao = "";
        if (categoria == null) categoria = "";
        if (local == null)    local = "";
        if (estado == null)   estado = "";
        if (coletor == null)  coletor = "";
        if (obs == null)      obs = "";
        if (salaOrigem == null) salaOrigem = "";

        // Título do card
        String titulo = switch (tipo) {
            case "sem_etiqueta" -> "Item Sem Etiqueta — " + truncar(descricao, 60);
            case "divergencia"  -> "Nº " + numero + " — " + truncar(descricao, 50) + " ⚠";
            default             -> "Nº " + numero + " — " + truncar(descricao, 55);
        };

        celulaDados.add(new Paragraph(titulo)
                .setFont(bold).setFontSize(10)
                .setFontColor(COR_CABECALHO)
                .setMarginBottom(4));

        // Badge de tipo
        DeviceRgb corBadge = switch (tipo) {
            case "sem_etiqueta" -> COR_BADGE_SE;
            case "divergencia"  -> COR_BADGE_DIV;
            default             -> COR_BADGE_PAT;
        };
        String labelBadge = switch (tipo) {
            case "sem_etiqueta" -> "SEM ETIQUETA" + (categoria.isBlank() ? "" : " · " + categoria);
            case "divergencia"  -> "DIVERGÊNCIA" + (motivoDivergencia != null ? " · " + motivoDivergencia : "");
            default             -> "PATRIMÔNIO";
        };

        celulaDados.add(new Paragraph(labelBadge)
                .setFont(bold).setFontSize(7)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(corBadge)
                .setPaddingLeft(5).setPaddingRight(5).setPaddingTop(2).setPaddingBottom(2)
                .setMarginBottom(5));

        // Dados em grade compacta
        // Dados — campos variam conforme o tipo de coleta
        adicionarCamposDados(celulaDados, normal, tipo, local, salaOrigem,
                estado, dataObj, coletor, obs, motivoDivergencia);

        card.addCell(celulaDados);
        doc.add(card);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Adiciona os campos de dados ao card, variando conforme o tipo de coleta. */
    private void adicionarCamposDados(Cell celulaDados, PdfFont normal,
                                      String tipo, String local, String salaOrigem,
                                      String estado, Object dataObj, String coletor,
                                      String obs, String motivoDivergencia) throws IOException {

        if ("sem_etiqueta".equals(tipo)) {
            // Itens sem etiqueta: apenas campos relevantes, sem número/sala de origem
            if (!local.isBlank()) {
                celulaDados.add(linha(normal, "Local encontrado:", local));
            }
            if (!estado.isBlank()) {
                celulaDados.add(linha(normal, "Estado:", estado));
            }
            if (dataObj != null) {
                celulaDados.add(linha(normal, "Data da coleta:", formatarData(dataObj)));
            }
            if (!coletor.isBlank() && !coletor.equals("Desconhecido")) {
                celulaDados.add(linha(normal, "Coletor:", coletor));
            }
            if (!obs.isBlank()) {
                celulaDados.add(linha(normal, "Observacoes:", truncar(obs, 120)));
            }

        } else {
            // Patrimônio normal ou divergência: todos os campos
            if (!local.isBlank()) {
                celulaDados.add(linha(normal, "Local encontrado:", local));
            }
            if (!salaOrigem.isBlank() && !salaOrigem.equals(local)) {
                celulaDados.add(linha(normal, "Sala de origem:", salaOrigem));
            }
            if (!estado.isBlank()) {
                celulaDados.add(linha(normal, "Estado:", estado));
            }
            if (dataObj != null) {
                celulaDados.add(linha(normal, "Data da coleta:", formatarData(dataObj)));
            }
            if (!coletor.isBlank() && !coletor.equals("Desconhecido")) {
                celulaDados.add(linha(normal, "Coletor:", coletor));
            }
            if ("divergencia".equals(tipo) && motivoDivergencia != null && !motivoDivergencia.isBlank()) {
                celulaDados.add(linha(normal, "Motivo divergencia:", motivoDivergencia));
            }
            if (!obs.isBlank()) {
                celulaDados.add(linha(normal, "Observacoes:", truncar(obs, 120)));
            }
        }
    }

    private String formatarData(Object dataObj) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(dataObj instanceof Timestamp ? (Timestamp) dataObj : new Date());
        } catch (Exception e) {
            return dataObj.toString();
        }
    }

    /** Cria um parágrafo de linha "rótulo: valor" com fonte compacta. */
    private Paragraph linha(PdfFont fontNormal, String rotulo, String valor) throws IOException {
        PdfFont fontBold = carregarFonte("/fonts/font-bold.ttf");
        return new Paragraph()
                .add(new com.itextpdf.layout.element.Text(rotulo + " ").setFont(fontBold).setFontSize(8))
                .add(new com.itextpdf.layout.element.Text(valor).setFont(fontNormal).setFontSize(8))
                .setMarginBottom(2);
    }

    /**
     * Carrega uma fonte TrueType do classpath e a embute no PDF.
     * Fallback para Helvetica se o arquivo não for encontrado.
     */
    private PdfFont carregarFonte(String classpathPath) {
        try (InputStream is = getClass().getResourceAsStream(classpathPath)) {
            if (is == null) {
                logger.warn("Fonte não encontrada no classpath: {} — usando Helvetica", classpathPath);
                return PdfFontFactory.createFont(
                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            }
            byte[] bytes = is.readAllBytes();
            return PdfFontFactory.createFont(
                    bytes,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (Exception e) {
            logger.warn("Erro ao carregar fonte {}: {} — usando Helvetica", classpathPath, e.getMessage());
            try {
                return PdfFontFactory.createFont(
                        com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            } catch (IOException ex) {
                throw new RuntimeException("Falha ao carregar fonte fallback", ex);
            }
        }
    }

    /** Placeholder cinza quando a foto não está disponível. */
    private Paragraph placeholderFoto(PdfFont font, String mensagem) {
        return new Paragraph(mensagem)
                .setFont(font).setFontSize(7)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(40);
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() > max ? texto.substring(0, max - 3) + "..." : texto;
    }
}
