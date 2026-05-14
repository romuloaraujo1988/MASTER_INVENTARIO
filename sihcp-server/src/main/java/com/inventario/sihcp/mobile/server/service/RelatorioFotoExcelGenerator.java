package com.inventario.sihcp.mobile.server.service;

import com.inventario.sihcp.dao.RelatorioFotoColetaDAO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Gerador de Relatório Fotográfico de Coletas em Excel (.xlsx).
 *
 * Espelho do {@link RelatorioFotoPDFGenerator}, mas em formato planilha:
 * cada coleta vira uma linha e a foto é embutida na primeira coluna.
 * Útil para análises tabulares e filtros do Excel.
 *
 * <h2>Layout do relatório</h2>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────────────┐
 * │ SIHCP — Relatório Fotográfico de Coletas                                │
 * │ Inventário #3 · Gerado em 13/05/2026 14:30 · Total: 12 fotos           │
 * ├──────┬──────────┬───────────┬─────────────┬────────┬────────┬──────────┤
 * │ Foto │ Tipo     │ Nº Patr.  │ Descrição   │ Estado │ Local  │ ...      │
 * ├──────┼──────────┼───────────┼─────────────┼────────┼────────┼──────────┤
 * │[IMG] │ sem_etiq │ —         │ Cadeira ... │ BOM    │ Sala 5 │ ...      │
 * │[IMG] │ patr.    │ 12345     │ Notebook    │ BOM    │ Sala 1 │ ...      │
 * └──────┴──────────┴───────────┴─────────────┴────────┴────────┴──────────┘
 * </pre>
 *
 * Fotos ausentes no disco são substituídas pelo texto "(sem foto)".
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@Service
public class RelatorioFotoExcelGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioFotoExcelGenerator.class);

    /** Altura da linha de cada coleta (em pontos). Suficiente para comportar a foto. */
    private static final short ALTURA_LINHA_COLETA = 100;

    /** Cabeçalhos da planilha (ordem importa). */
    private static final String[] CABECALHOS = {
            "Foto", "Tipo", "Nº Patrimônio", "Descrição", "Categoria",
            "Estado", "Localização", "Data da Coleta", "Coletor",
            "Sala de Origem", "Observação", "Motivo Divergência"
    };

    @Autowired
    private RelatorioFotoColetaDAO relatorioFotoDAO;

    @Autowired
    private FotoColetaStorageService storageService;

    /**
     * Gera o Excel do relatório fotográfico e retorna os bytes.
     *
     * @param idInventario   ID do inventário
     * @param nomeInventario Nome do inventário (para o cabeçalho)
     * @param tipo           Filtro de tipo: {@code todos}, {@code patrimonio},
     *                       {@code sem_etiqueta} ou {@code divergencia}
     * @param idSala         Filtro opcional por sala (null = todas)
     * @return bytes do XLSX gerado, ou array vazio se não houver dados
     */
    public byte[] gerarExcel(int idInventario, String nomeInventario, String tipo, Integer idSala) {
        List<Map<String, Object>> coletas;
        try {
            coletas = relatorioFotoDAO.buscarColetasComFoto(idInventario, tipo, idSala);
        } catch (Exception e) {
            logger.error("Falha ao buscar coletas com foto para Excel — inv={}", idInventario, e);
            return new byte[0];
        }

        if (coletas.isEmpty()) {
            logger.info("Nenhuma coleta com foto para inventário {} (tipo={})", idInventario, tipo);
            return new byte[0];
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Relatório Fotográfico");
            sheet.setDefaultRowHeightInPoints(20);

            // Estilos reutilizáveis
            CellStyle estiloTitulo    = criarEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = criarEstiloSubtitulo(workbook);
            CellStyle estiloCabecalho = criarEstiloCabecalho(workbook);
            CellStyle estiloCelula    = criarEstiloCelula(workbook);

            // Linha 0 — Título
            Row linhaTitulo = sheet.createRow(0);
            linhaTitulo.setHeightInPoints(28);
            Cell celulaTitulo = linhaTitulo.createCell(0);
            celulaTitulo.setCellValue("SIHCP — Relatório Fotográfico de Coletas");
            celulaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, CABECALHOS.length - 1));

            // Linha 1 — Subtítulo
            Row linhaSubtitulo = sheet.createRow(1);
            linhaSubtitulo.setHeightInPoints(20);
            Cell celulaSub = linhaSubtitulo.createCell(0);
            String dataGeracao = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            String subtituloInventario = (nomeInventario == null || nomeInventario.isBlank())
                    ? "Inventário #" + idInventario
                    : "Inventário: " + nomeInventario;
            celulaSub.setCellValue(subtituloInventario
                    + " · Gerado em " + dataGeracao
                    + " · Total: " + coletas.size() + " foto(s)"
                    + " · Filtro: " + descricaoTipo(tipo));
            celulaSub.setCellStyle(estiloSubtitulo);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, CABECALHOS.length - 1));

            // Linha 2 — vazia (espaçador)
            // Linha 3 — Cabeçalhos
            Row linhaCabecalho = sheet.createRow(3);
            linhaCabecalho.setHeightInPoints(22);
            for (int i = 0; i < CABECALHOS.length; i++) {
                Cell c = linhaCabecalho.createCell(i);
                c.setCellValue(CABECALHOS[i]);
                c.setCellStyle(estiloCabecalho);
            }

            // Linhas 4..N — Coletas
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper creationHelper = workbook.getCreationHelper();
            int rowIdx = 4;

            for (Map<String, Object> coleta : coletas) {
                Row linha = sheet.createRow(rowIdx);
                linha.setHeightInPoints(ALTURA_LINHA_COLETA);

                // Coluna 0: Foto (embutida)
                Cell celulaFoto = linha.createCell(0);
                celulaFoto.setCellStyle(estiloCelula);
                inserirFoto(workbook, drawing, creationHelper, coleta, rowIdx, celulaFoto);

                preencherCelula(linha, 1, descricaoTipo((String) coleta.get("tipo")), estiloCelula);
                preencherCelula(linha, 2, (String) coleta.get("numeroPatrimonio"), estiloCelula);
                preencherCelula(linha, 3, (String) coleta.get("descricao"), estiloCelula);
                preencherCelula(linha, 4, (String) coleta.get("categoria"), estiloCelula);
                preencherCelula(linha, 5, (String) coleta.get("estadoEncontrado"), estiloCelula);
                preencherCelula(linha, 6, (String) coleta.get("localizacaoEncontrada"), estiloCelula);
                preencherCelula(linha, 7, formatarData(coleta.get("dataColeta")), estiloCelula);
                preencherCelula(linha, 8, (String) coleta.get("nomeColetor"), estiloCelula);
                preencherCelula(linha, 9, (String) coleta.get("nomeSalaOrigem"), estiloCelula);
                preencherCelula(linha, 10, (String) coleta.get("observacao"), estiloCelula);
                preencherCelula(linha, 11, (String) coleta.get("motivoDivergencia"), estiloCelula);

                rowIdx++;
            }

            // Larguras das colunas (em "characters * 256")
            sheet.setColumnWidth(0, 22 * 256);   // Foto
            sheet.setColumnWidth(1, 14 * 256);   // Tipo
            sheet.setColumnWidth(2, 14 * 256);   // Nº Patrimônio
            sheet.setColumnWidth(3, 40 * 256);   // Descrição
            sheet.setColumnWidth(4, 18 * 256);   // Categoria
            sheet.setColumnWidth(5, 14 * 256);   // Estado
            sheet.setColumnWidth(6, 24 * 256);   // Localização
            sheet.setColumnWidth(7, 18 * 256);   // Data
            sheet.setColumnWidth(8, 22 * 256);   // Coletor
            sheet.setColumnWidth(9, 22 * 256);   // Sala
            sheet.setColumnWidth(10, 30 * 256);  // Observação
            sheet.setColumnWidth(11, 24 * 256);  // Motivo

            // Filtro automático no cabeçalho (linha 3)
            sheet.setAutoFilter(new CellRangeAddress(3, 3 + coletas.size(), 0, CABECALHOS.length - 1));

            // Congelar cabeçalho
            sheet.createFreezePane(0, 4);

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            logger.error("Erro ao gerar XLSX do relatório fotográfico", e);
            return new byte[0];
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private void preencherCelula(Row linha, int coluna, String valor, CellStyle estilo) {
        Cell c = linha.createCell(coluna);
        c.setCellValue(valor == null ? "" : valor);
        c.setCellStyle(estilo);
    }

    /**
     * Insere a foto no centro da célula da linha. Se a foto não estiver
     * disponível no disco (ou der erro de leitura), a célula recebe o texto
     * "(sem foto)" como fallback — não falha o relatório inteiro.
     */
    private void inserirFoto(Workbook workbook, Drawing<?> drawing,
                             CreationHelper creationHelper,
                             Map<String, Object> coleta,
                             int rowIdx, Cell celulaFoto) {
        String fotoPath = (String) coleta.get("fotoPath");
        if (fotoPath == null || !storageService.fotoExiste(fotoPath)) {
            celulaFoto.setCellValue("(sem foto)");
            return;
        }
        try {
            byte[] fotoBytes = storageService.lerFoto(fotoPath);
            int pictureIdx = workbook.addPicture(fotoBytes, detectarTipoImagem(fotoPath));

            ClientAnchor anchor = creationHelper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(rowIdx);
            anchor.setCol2(1);
            anchor.setRow2(rowIdx + 1);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);

            Picture pic = drawing.createPicture(anchor, pictureIdx);
            // Pequeno padding para não colar na borda da célula
            pic.resize(0.95, 0.95);
        } catch (Exception e) {
            logger.warn("Falha ao inserir foto {}: {}", fotoPath, e.getMessage());
            celulaFoto.setCellValue("(erro ao carregar)");
        }
    }

    private int detectarTipoImagem(String fotoPath) {
        String lower = fotoPath.toLowerCase();
        if (lower.endsWith(".png"))  return Workbook.PICTURE_TYPE_PNG;
        if (lower.endsWith(".jpeg") || lower.endsWith(".jpg")) return Workbook.PICTURE_TYPE_JPEG;
        // Fallback — POI lê a maioria dos JPEGs mesmo sem extensão correta.
        return Workbook.PICTURE_TYPE_JPEG;
    }

    private String formatarData(Object valor) {
        if (valor == null) return "";
        if (valor instanceof Timestamp ts) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts);
        }
        if (valor instanceof Date d) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(d);
        }
        return valor.toString();
    }

    private String descricaoTipo(String tipo) {
        if (tipo == null) return "Todos";
        return switch (tipo.toLowerCase()) {
            case "patrimonio"    -> "Patrimônio";
            case "sem_etiqueta"  -> "Sem Etiqueta";
            case "divergencia"   -> "Divergência";
            default              -> "Todos";
        };
    }

    // ─── Estilos ────────────────────────────────────────────────────────────

    private CellStyle criarEstiloTitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Azul IFMT (#1A3A6B)
        if (s instanceof org.apache.poi.xssf.usermodel.XSSFCellStyle xssf) {
            xssf.setFillForegroundColor(new XSSFColor(new Color(0x1A, 0x3A, 0x6B), null));
        } else {
            s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        }
        return s;
    }

    private CellStyle criarEstiloSubtitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle criarEstiloCabecalho(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        if (s instanceof org.apache.poi.xssf.usermodel.XSSFCellStyle xssf) {
            xssf.setFillForegroundColor(new XSSFColor(new Color(0x2E, 0x6D, 0xA8), null));
        } else {
            s.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        }
        aplicarBordas(s);
        return s;
    }

    private CellStyle criarEstiloCelula(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        aplicarBordas(s);
        return s;
    }

    private void aplicarBordas(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }
}
