package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.dao.RelatorioFotoColetaDAO;
import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.service.RelatorioFotoExcelGenerator;
import com.inventario.sihcp.mobile.server.service.RelatorioFotoPDFGenerator;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import com.inventario.sihcp.security.annotation.RequireSupervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para geração do Relatório Fotográfico de Coletas.
 *
 * <h2>Endpoints</h2>
 * <pre>
 * GET /api/mobile/relatorios/fotos/{inventarioId}
 *     Gera e retorna o PDF como download.
 *
 *     Parâmetros (query string):
 *       tipo    — todos | patrimonio | sem_etiqueta | divergencia  (default: todos)
 *       salaId  — ID da sala para filtrar (opcional)
 *       nome    — Nome do inventário para o cabeçalho (opcional)
 *
 * GET /api/mobile/relatorios/fotos/{inventarioId}/info
 *     Retorna metadados (contagem de fotos por tipo) sem gerar o PDF.
 * </pre>
 *
 * <h2>Segurança</h2>
 * <ul>
 *   <li>Geração do PDF: {@code SUPERVISOR+} (relatório gerencial)</li>
 *   <li>Info/contagem: {@code CONSULTA+}</li>
 * </ul>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/relatorios/fotos")
public class MobileRelatorioFotoController {

    private static final Logger logger = LoggerFactory.getLogger(MobileRelatorioFotoController.class);

    @Autowired
    private RelatorioFotoPDFGenerator pdfGenerator;

    @Autowired
    private RelatorioFotoExcelGenerator excelGenerator;

    @Autowired
    private RelatorioFotoColetaDAO relatorioFotoDAO;

    // ─── Geração do PDF ───────────────────────────────────────────────────────

    /**
     * Gera o Relatório Fotográfico de Coletas em PDF.
     *
     * @param inventarioId ID do inventário
     * @param tipo         Filtro: {@code todos}, {@code patrimonio},
     *                     {@code sem_etiqueta} ou {@code divergencia}
     * @param salaId       Filtro opcional por sala
     * @param nome         Nome do inventário para o cabeçalho do PDF
     * @return PDF como download (application/pdf)
     */
    @GetMapping("/{inventarioId}")
    @RequireSupervisor
    public ResponseEntity<?> gerarRelatorioFotografico(
            @PathVariable int inventarioId,
            @RequestParam(value = "tipo",   defaultValue = "todos") String tipo,
            @RequestParam(value = "salaId", required = false)       Integer salaId,
            @RequestParam(value = "nome",   defaultValue = "")      String nome) {

        logger.info("GET /api/mobile/relatorios/fotos/{} — tipo={}, salaId={}", inventarioId, tipo, salaId);

        try {
            byte[] pdfBytes = pdfGenerator.gerarPDF(inventarioId, nome, tipo, salaId);

            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            String nomeArquivo = String.format("relatorio_fotos_inv%d_%s_%s.pdf",
                    inventarioId,
                    tipo.equals("todos") ? "completo" : tipo,
                    new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(nomeArquivo).build());
            headers.setContentLength(pdfBytes.length);

            logger.info("PDF gerado: {} bytes, arquivo={}", pdfBytes.length, nomeArquivo);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Erro ao gerar relatório fotográfico para inventário {}", inventarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erro ao gerar relatório: " + e.getMessage(),
                            "ERRO_RELATORIO_FOTO"));
        }
    }

    // ─── Geração do Excel (.xlsx) ─────────────────────────────────────────────

    /**
     * Gera o Relatório Fotográfico de Coletas em Excel (.xlsx).
     *
     * Mesma fonte de dados e mesmas regras de filtro/segurança do PDF; útil
     * para análises tabulares e filtros do Excel. As fotos vão embutidas
     * na primeira coluna da planilha.
     *
     * @param inventarioId ID do inventário
     * @param tipo         Filtro: {@code todos}, {@code patrimonio},
     *                     {@code sem_etiqueta} ou {@code divergencia}
     * @param salaId       Filtro opcional por sala
     * @param nome         Nome do inventário para o cabeçalho
     * @return XLSX como download
     */
    @GetMapping("/{inventarioId}/excel")
    @RequireSupervisor
    public ResponseEntity<?> gerarRelatorioFotograficoExcel(
            @PathVariable int inventarioId,
            @RequestParam(value = "tipo",   defaultValue = "todos") String tipo,
            @RequestParam(value = "salaId", required = false)       Integer salaId,
            @RequestParam(value = "nome",   defaultValue = "")      String nome) {

        logger.info("GET /api/mobile/relatorios/fotos/{}/excel — tipo={}, salaId={}", inventarioId, tipo, salaId);

        try {
            byte[] xlsxBytes = excelGenerator.gerarExcel(inventarioId, nome, tipo, salaId);

            if (xlsxBytes == null || xlsxBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            String nomeArquivo = String.format("relatorio_fotos_inv%d_%s_%s.xlsx",
                    inventarioId,
                    tipo.equals("todos") ? "completo" : tipo,
                    new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(nomeArquivo).build());
            headers.setContentLength(xlsxBytes.length);

            logger.info("XLSX gerado: {} bytes, arquivo={}", xlsxBytes.length, nomeArquivo);

            return new ResponseEntity<>(xlsxBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Erro ao gerar relatório fotográfico Excel para inventário {}", inventarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erro ao gerar relatório Excel: " + e.getMessage(),
                            "ERRO_RELATORIO_FOTO_EXCEL"));
        }
    }

    // ─── Metadados / contagem ─────────────────────────────────────────────────

    /**
     * Retorna metadados do relatório fotográfico sem gerar o PDF.
     * Útil para o app Android exibir quantas fotos existem antes de baixar.
     *
     * @param inventarioId ID do inventário
     * @return JSON com contagens por tipo
     */
    @GetMapping("/{inventarioId}/info")
    @RequireConsulta
    public ResponseEntity<ApiResponse<Map<String, Object>>> infoRelatorioFotografico(
            @PathVariable int inventarioId) {

        logger.debug("GET /api/mobile/relatorios/fotos/{}/info", inventarioId);

        try {
            int totalFotos      = relatorioFotoDAO.contarColetasComFoto(inventarioId);
            int totalPatrimonio = relatorioFotoDAO.buscarColetasComFoto(inventarioId, "patrimonio",  null).size();
            int totalSemEtiq    = relatorioFotoDAO.buscarColetasComFoto(inventarioId, "sem_etiqueta",null).size();
            int totalDiverg     = relatorioFotoDAO.buscarColetasComFoto(inventarioId, "divergencia", null).size();

            Map<String, Object> info = new HashMap<>();
            info.put("inventarioId",    inventarioId);
            info.put("totalFotos",      totalFotos);
            info.put("patrimonio",      totalPatrimonio);
            info.put("semEtiqueta",     totalSemEtiq);
            info.put("divergencia",     totalDiverg);
            info.put("temFotos",        totalFotos > 0);

            return ResponseEntity.ok(ApiResponse.success(info,
                    totalFotos + " foto(s) disponível(is) para o relatório"));

        } catch (Exception e) {
            logger.error("Erro ao buscar info de fotos para inventário {}", inventarioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Erro ao buscar informações: " + e.getMessage(),
                            "ERRO_INFO_FOTO"));
        }
    }
}
