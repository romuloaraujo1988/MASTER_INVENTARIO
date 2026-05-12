package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.service.FotoColetaStorageService;
import com.inventario.sihcp.security.annotation.RequireColetor;
import com.inventario.sihcp.security.annotation.RequireConsulta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para upload e download de fotos de coleta.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/mobile/fotos/upload} — Upload de foto (COLETOR+)</li>
 *   <li>{@code GET  /api/mobile/fotos/{coletaId}} — Download de foto (CONSULTA+)</li>
 *   <li>{@code DELETE /api/mobile/fotos/{coletaId}} — Remover foto (COLETOR+)</li>
 * </ul>
 *
 * <h2>Estrutura de armazenamento</h2>
 * <pre>
 * data/fotos/
 *   inventario_{id}/
 *     {YYYY-MM}/
 *       patrimonio/
 *         coleta_{coletaId}_{numeroPatrimonio}.jpg
 *       sem_etiqueta/
 *         coleta_{coletaId}_SE.jpg
 *       divergencia/
 *         coleta_{coletaId}_{numeroPatrimonio}.jpg
 * </pre>
 *
 * O caminho relativo é gravado na coluna {@code FOTO_PATH} da {@code TABELA_COLETA}.
 *
 * @author Sistema de Inventário IFMT
 * @version 2.22
 */
@RestController
@RequestMapping("/api/mobile/fotos")
public class MobileFotoColetaController {

    private static final Logger logger = LoggerFactory.getLogger(MobileFotoColetaController.class);

    @Autowired
    private FotoColetaStorageService storageService;

    @Autowired
    private ColetaDAO coletaDAO;

    // ─── Upload ───────────────────────────────────────────────────────────────

    /**
     * Upload de foto de coleta.
     *
     * <p>Recebe a foto via multipart/form-data, salva em disco com a estrutura
     * de pastas correta e atualiza o campo {@code FOTO_PATH} da coleta no banco.</p>
     *
     * @param foto          arquivo da foto (multipart)
     * @param coletaId      ID da coleta no servidor
     * @param inventarioId  ID do inventário
     * @param tipo          Tipo da coleta: {@code patrimonio}, {@code sem_etiqueta} ou {@code divergencia}
     * @param identificador Número do patrimônio ou {@code SE} para itens sem etiqueta
     * @return resposta com o caminho relativo da foto salva
     */
    @PostMapping("/upload")
    @RequireColetor
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFoto(
            @RequestParam("foto")          MultipartFile foto,
            @RequestParam("coletaId")      int coletaId,
            @RequestParam("inventarioId")  int inventarioId,
            @RequestParam(value = "tipo",          defaultValue = "patrimonio") String tipo,
            @RequestParam(value = "identificador", defaultValue = "")           String identificador) {

        logger.info("POST /api/mobile/fotos/upload — coletaId={}, inventarioId={}, tipo={}, id={}, tamanho={}KB",
                coletaId, inventarioId, tipo, identificador, foto.getSize() / 1024);

        try {
            if (foto.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Arquivo de foto vazio", "FOTO_VAZIA"));
            }

            // Normalizar identificador: se vazio, usar coletaId como fallback
            String idFinal = (identificador == null || identificador.isBlank())
                    ? String.valueOf(coletaId)
                    : identificador;

            String caminhoRelativo = storageService.salvarFoto(
                    foto.getInputStream(),
                    inventarioId,
                    coletaId,
                    tipo,
                    idFinal,
                    foto.getOriginalFilename(),
                    foto.getSize()
            );

            coletaDAO.atualizarFotoPath(coletaId, caminhoRelativo);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("coletaId",    coletaId);
            resultado.put("fotoPath",    caminhoRelativo);
            resultado.put("tamanhoKB",   foto.getSize() / 1024);
            resultado.put("tipo",        tipo);
            resultado.put("identificador", idFinal);

            logger.info("Foto salva: {} ({}KB)", caminhoRelativo, foto.getSize() / 1024);

            return ResponseEntity.ok(ApiResponse.success(resultado, "Foto salva com sucesso"));

        } catch (IllegalArgumentException e) {
            logger.warn("Foto rejeitada: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "FOTO_INVALIDA"));

        } catch (Exception e) {
            logger.error("Erro ao salvar foto da coleta {}", coletaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao salvar foto: " + e.getMessage(), "ERRO_UPLOAD"));
        }
    }

    // ─── Download ─────────────────────────────────────────────────────────────

    /**
     * Download de foto de coleta.
     *
     * @param coletaId ID da coleta
     * @return bytes da imagem com content-type adequado
     */
    @GetMapping("/{coletaId}")
    @RequireConsulta
    public ResponseEntity<?> downloadFoto(@PathVariable int coletaId) {
        logger.debug("GET /api/mobile/fotos/{}", coletaId);

        try {
            String fotoPath = coletaDAO.buscarFotoPath(coletaId);

            if (fotoPath == null || fotoPath.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Coleta não possui foto", "FOTO_NAO_ENCONTRADA"));
            }

            if (!storageService.fotoExiste(fotoPath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Arquivo de foto não encontrado no disco", "ARQUIVO_NAO_ENCONTRADO"));
            }

            byte[] fotoBytes = storageService.lerFoto(fotoPath);
            String contentType = storageService.getContentType(fotoPath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"coleta_" + coletaId + ".jpg\"")
                    .body(fotoBytes);

        } catch (Exception e) {
            logger.error("Erro ao buscar foto da coleta {}", coletaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar foto: " + e.getMessage(), "ERRO_DOWNLOAD"));
        }
    }

    // ─── Remoção ──────────────────────────────────────────────────────────────

    /**
     * Remove foto de uma coleta.
     *
     * @param coletaId ID da coleta
     * @return confirmação de remoção
     */
    @DeleteMapping("/{coletaId}")
    @RequireColetor
    public ResponseEntity<ApiResponse<Map<String, Object>>> removerFoto(@PathVariable int coletaId) {
        logger.info("DELETE /api/mobile/fotos/{}", coletaId);

        try {
            String fotoPath = coletaDAO.buscarFotoPath(coletaId);

            if (fotoPath != null && !fotoPath.isBlank()) {
                storageService.removerFoto(fotoPath);
            }

            coletaDAO.atualizarFotoPath(coletaId, null);

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("coletaId", coletaId);
            resultado.put("removida", true);

            return ResponseEntity.ok(ApiResponse.success(resultado, "Foto removida com sucesso"));

        } catch (Exception e) {
            logger.error("Erro ao remover foto da coleta {}", coletaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao remover foto: " + e.getMessage(), "ERRO_REMOCAO"));
        }
    }
}
