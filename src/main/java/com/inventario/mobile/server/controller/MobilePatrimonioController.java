package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.MobilePatrimonioDTO;
import com.inventario.mobile.server.service.MobilePatrimonioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador REST para operações de patrimônio mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/patrimonio")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobilePatrimonioController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobilePatrimonioController.class);
    
    @Autowired
    private MobilePatrimonioService patrimonioService;
    
    /**
     * Buscar patrimônio por QR Code
     * 
     * @param qrCode código QR do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorQRCode(@PathVariable String qrCode) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio por QR Code: {} para usuário: {}", qrCode, username);
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorQRCode(qrCode);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por QR Code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônio por número
     * 
     * @param numero número do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/numero/{numero}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorNumero(@PathVariable String numero) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando patrimônio por número: {} para usuário: {}", numero, username);
            
            // ✅ TRATAMENTO ROBUSTO: Validar entrada
            if (numero == null || numero.trim().isEmpty()) {
                logger.warn("Número de patrimônio vazio ou nulo");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Número de patrimônio é obrigatório", "INVALID_INPUT"));
            }
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorNumero(numero.trim());
            
            if (patrimonio != null) {
                logger.info("✓ Patrimônio {} encontrado", numero);
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                logger.info("Patrimônio {} não encontrado", numero);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (SQLException e) {
            logger.error("Erro SQL ao buscar patrimônio por número: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro de banco de dados ao buscar patrimônio", "DATABASE_ERROR"));
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar patrimônio por número: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios por sala
     * 
     * @param salaId ID da sala
     * @return lista de patrimônios
     */
    @GetMapping("/sala/{salaId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorSala(@PathVariable Integer salaId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios da sala {} para usuário: {}", salaId, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorSala(salaId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por sala", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios por setor
     * 
     * @param setorId ID do setor
     * @return lista de patrimônios
     */
    @GetMapping("/setor/{setorId}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorSetor(@PathVariable Integer setorId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônios do setor {} para usuário: {}", setorId, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorSetor(setorId);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por setor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios por responsável com paginação e filtro de coleta
     * 
     * @param idResponsavel ID do responsável
     * @param page página (padrão: 0)
     * @param size tamanho da página (padrão: 50)
     * @param coletado filtro de status de coleta (opcional: true=coletados, false=não coletados, null=todos)
     * @return lista de patrimônios
     */
    @GetMapping("/responsavel/{idResponsavel}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorResponsavel(
            @PathVariable Integer idResponsavel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Boolean coletado) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Buscando patrimônios do responsável {} (page: {}, size: {}, coletado: {}) para usuário: {}", 
                    idResponsavel, page, size, coletado, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorResponsavel(idResponsavel, page, size, coletado);
            
            logger.info("✓ {} patrimônio(s) encontrado(s) para responsável {}", patrimonios.size(), idResponsavel);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por responsável", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios: " + e.getMessage(), "FETCH_ERROR"));
        }
    }
    
    /**
     * Contar total de patrimônios por responsável
     * 
     * @param idResponsavel ID do responsável
     * @return total de patrimônios
     */
    @GetMapping("/responsavel/{idResponsavel}/count")
    public ResponseEntity<ApiResponse<Integer>> contarPatrimoniosPorResponsavel(@PathVariable Integer idResponsavel) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Contando patrimônios do responsável {} para usuário: {}", idResponsavel, username);
            
            int total = patrimonioService.contarPatrimoniosPorResponsavel(idResponsavel);
            
            return ResponseEntity.ok(
                    ApiResponse.success(total, 
                            String.format("Total de %d patrimônio(s) encontrado(s)", total)));
            
        } catch (Exception e) {
            logger.error("Erro ao contar patrimônios por responsável", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao contar patrimônios", "COUNT_ERROR"));
        }
    }
    
    /**
     * Listar todos os patrimônios (com paginação)
     * 
     * @param page página (padrão: 0)
     * @param size tamanho da página (padrão: 50)
     * @return lista de patrimônios
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> listarPatrimonios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("═══════════════════════════════════════════");
            logger.info("LISTANDO PATRIMÔNIOS");
            logger.info("Usuário: {}", username);
            logger.info("Page: {}, Size: {}", page, size);
            logger.info("═══════════════════════════════════════════");
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.listarPatrimonios(page, size);
            
            logger.info("Service retornou {} patrimônios", patrimonios.size());
            logger.info("Tipo da lista: {}", patrimonios.getClass().getName());
            
            ApiResponse<List<MobilePatrimonioDTO>> response = ApiResponse.success(
                patrimonios, 
                String.format("%d patrimônio(s) carregado(s)", patrimonios.size())
            );
            
            logger.info("ApiResponse criado - success: {}, data type: {}", 
                response.isSuccess(), 
                response.getData() != null ? response.getData().getClass().getName() : "null");
            
            if (response.getData() != null) {
                logger.info("Data size: {}", response.getData().size());
                if (!response.getData().isEmpty()) {
                    logger.info("Primeiro item: {}", response.getData().get(0).getCodigo());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("═══════════════════════════════════════════");
            logger.error("ERRO AO LISTAR PATRIMÔNIOS");
            logger.error("Tipo: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            logger.error("Stack trace:", e);
            logger.error("═══════════════════════════════════════════");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao listar patrimônios", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônio por ID
     * 
     * @param id ID do patrimônio
     * @return dados do patrimônio
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> buscarPorId(@PathVariable Integer id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            logger.info("Buscando patrimônio {} para usuário: {}", id, username);
            
            MobilePatrimonioDTO patrimonio = patrimonioService.buscarPorId(id);
            
            if (patrimonio != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(patrimonio, "Patrimônio encontrado"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Patrimônio não encontrado", "NOT_FOUND"));
            }
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônio por ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônio", "FETCH_ERROR"));
        }
    }
    
    /**
     * Verifica se um patrimônio já foi coletado no inventário
     * GET /api/mobile/patrimonio/numero/{numero}/coletado?inventarioId=2
     * 
     * @param numero número do patrimônio
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     * @return informações sobre a coleta
     */
    @GetMapping("/numero/{numero}/coletado")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> verificarSePatrimonioFoiColetado(
            @PathVariable String numero,
            @RequestParam(required = false) Integer inventarioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Verificando se patrimônio {} foi coletado (inventário: {}) para usuário: {}", 
                    numero, inventarioId, username);
            
            java.util.Map<String, Object> resultado = patrimonioService.verificarSePatrimonioFoiColetado(numero, inventarioId);
            
            boolean coletado = (Boolean) resultado.get("coletado");
            String mensagem = coletado 
                    ? "Patrimônio já foi coletado" 
                    : "Patrimônio ainda não foi coletado";
            
            return ResponseEntity.ok(ApiResponse.success(resultado, mensagem));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao verificar coleta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            logger.error("Erro ao verificar se patrimônio foi coletado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao verificar coleta: " + e.getMessage(), "CHECK_ERROR"));
        }
    }
    
    /**
     * Valida um número de patrimônio antes de coletar
     * GET /api/mobile/patrimonio/numero/{numero}/validar
     * 
     * @param numero número do patrimônio
     * @return informações de validação
     */
    @GetMapping("/numero/{numero}/validar")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> validarPatrimonio(@PathVariable String numero) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            logger.info("Validando patrimônio {} para usuário: {}", numero, username);
            
            java.util.Map<String, Object> resultado = patrimonioService.validarPatrimonio(numero);
            
            boolean valido = (Boolean) resultado.get("valido");
            String mensagem = valido 
                    ? "Patrimônio válido e pode ser coletado" 
                    : (String) resultado.get("mensagem");
            
            HttpStatus status = valido ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            
            return ResponseEntity.status(status)
                    .body(ApiResponse.success(resultado, mensagem));
            
        } catch (Exception e) {
            logger.error("Erro ao validar patrimônio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao validar patrimônio: " + e.getMessage(), "VALIDATION_ERROR"));
        }
    }
    
    /**
     * Buscar patrimônios não coletados por descrição
     * GET /api/mobile/patrimonio/descricao/{descricao}
     * 
     * Usado para coleta por descrição (sem etiqueta)
     * 
     * @param descricao descrição do patrimônio (URL encoded)
     * @param inventarioId ID do inventário (opcional, usa ativo se não informado)
     * @return lista de patrimônios não coletados com essa descrição
     */
    @GetMapping("/descricao/{descricao}")
    public ResponseEntity<ApiResponse<List<MobilePatrimonioDTO>>> buscarPorDescricaoNaoColetados(
            @PathVariable String descricao,
            @RequestParam(required = false) Integer inventarioId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";
            
            // Decodificar a descrição (pode vir URL encoded)
            String descricaoDecodificada = java.net.URLDecoder.decode(descricao, "UTF-8");
            
            logger.info("Buscando patrimônios não coletados com descrição '{}' (inventário: {}) para usuário: {}", 
                    descricaoDecodificada, inventarioId, username);
            
            List<MobilePatrimonioDTO> patrimonios = patrimonioService.buscarPorDescricaoNaoColetados(descricaoDecodificada, inventarioId);
            
            logger.info("✓ {} patrimônio(s) encontrado(s) com descrição '{}'", patrimonios.size(), descricaoDecodificada);
            
            return ResponseEntity.ok(
                    ApiResponse.success(patrimonios, 
                            String.format("%d patrimônio(s) encontrado(s)", patrimonios.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar patrimônios por descrição", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar patrimônios: " + e.getMessage(), "FETCH_ERROR"));
        }
    }

}
