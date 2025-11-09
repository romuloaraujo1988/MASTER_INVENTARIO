package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.mobile.server.dto.DashboardStatsDTO;
import com.inventario.model.Patrimonio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST para estatísticas do dashboard mobile
 */
@RestController
@RequestMapping("/api/mobile/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDashboardController.class);
    
    @Autowired
    private com.inventario.dao.PatrimonioDAORefactored patrimonioDAO;
    
    @Autowired
    private com.inventario.dao.ColetaDAO coletaDAO;
    
    @Autowired
    private com.inventario.dao.InventarioDAO inventarioDAO;
    
    @Autowired
    private com.inventario.service.UsuarioService usuarioService;
    
    @Autowired
    private com.inventario.security.JwtTokenProvider jwtTokenProvider;
    
    /**
     * Obter estatísticas do dashboard
     * Endpoint público que aceita token JWT opcional para personalizar estatísticas
     * 
     * @param authHeader header de autorização com token JWT (opcional)
     * @return estatísticas gerais
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            logger.info("=== INICIANDO BUSCA DE ESTATÍSTICAS ===");
            logger.info("PatrimonioDAO injetado: {}", patrimonioDAO != null ? "SIM" : "NÃO");
            logger.info("ColetaDAO injetado: {}", coletaDAO != null ? "SIM" : "NÃO");
            logger.info("InventarioDAO injetado: {}", inventarioDAO != null ? "SIM" : "NÃO");
            logger.info("UsuarioService injetado: {}", usuarioService != null ? "SIM" : "NÃO");
            
            // Buscar inventário ativo (EM_ANDAMENTO)
            com.inventario.model.Inventario inventarioAtivo = null;
            try {
                inventarioAtivo = inventarioDAO.buscarInventarioPorStatus(com.inventario.model.Inventario.STATUS_EM_ANDAMENTO);
                if (inventarioAtivo != null) {
                    logger.info("Inventário ativo encontrado: ID={}, Nome={}", inventarioAtivo.getId(), inventarioAtivo.getNome());
                } else {
                    logger.warn("Nenhum inventário EM_ANDAMENTO encontrado");
                }
            } catch (Exception e) {
                logger.error("Erro ao buscar inventário ativo: {}", e.getMessage(), e);
            }
            
            String username = null;
            com.inventario.model.Usuario usuario = null;
            int patrimoniosColetados = 0;
            
            // Tentar extrair username do token JWT se fornecido
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    username = jwtTokenProvider.getUsernameFromToken(token);
                    logger.info("Username extraído do token JWT: {}", username);
                } catch (Exception e) {
                    logger.warn("Erro ao extrair username do token: {}", e.getMessage());
                }
            }
            
            // Se não conseguiu extrair do token, tentar do SecurityContext
            if (username == null) {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
                        username = authentication.getName();
                        logger.info("Username extraído do SecurityContext: {}", username);
                    }
                } catch (Exception e) {
                    logger.warn("Erro ao extrair username do SecurityContext: {}", e.getMessage());
                }
            }
            
            // Buscar usuário se temos um username
            if (username != null) {
                usuario = usuarioService.buscarPorUsername(username);
                if (usuario != null) {
                    logger.info("Usuário encontrado: {} (ID: {})", usuario.getNomeCompleto(), usuario.getId());
                } else {
                    logger.warn("Usuário '{}' não encontrado no banco", username);
                }
            } else {
                logger.info("Nenhum usuário autenticado, retornando estatísticas gerais");
            }
            
            // Buscar coletas do inventário ativo
            if (inventarioAtivo != null) {
                try {
                    List<com.inventario.model.Coleta> coletasInventario = coletaDAO.buscarPorInventario(inventarioAtivo.getId());
                    patrimoniosColetados = coletasInventario != null ? coletasInventario.size() : 0;
                    logger.info("Total de coletas do inventário ativo (ID={}): {}", inventarioAtivo.getId(), patrimoniosColetados);
                } catch (Exception e) {
                    logger.error("Erro ao buscar coletas do inventário: {}", e.getMessage(), e);
                    patrimoniosColetados = 0;
                }
            } else {
                logger.warn("Sem inventário ativo, patrimoniosColetados = 0");
                patrimoniosColetados = 0;
            }
            
            // Total de patrimônios (pode ser todos ou apenas os atribuídos ao usuário)
            // Por enquanto, vamos usar todos os patrimônios
            logger.info("Buscando todos os patrimônios...");
            List<Patrimonio> todosPatrimonios = null;
            try {
                todosPatrimonios = patrimonioDAO.findAll();
                logger.info("Total de patrimônios encontrados: {}", todosPatrimonios != null ? todosPatrimonios.size() : 0);
                if (todosPatrimonios != null && !todosPatrimonios.isEmpty()) {
                    logger.info("Primeiro patrimônio: ID={}, Numero={}, Descricao={}", 
                        todosPatrimonios.get(0).getId(),
                        todosPatrimonios.get(0).getNumero(),
                        todosPatrimonios.get(0).getDescricao());
                }
            } catch (Exception e) {
                logger.error("ERRO ao buscar patrimônios: {}", e.getMessage(), e);
                todosPatrimonios = new ArrayList<>();
            }
            int totalPatrimonios = todosPatrimonios != null ? todosPatrimonios.size() : 0;
            
            int patrimoniosPendentes = totalPatrimonios - patrimoniosColetados;
            
            // Buscar estatísticas de salas (para futuras expansões)
            // List<Sala> todasSalas = salaDAO.findAll();
            
            // Calcular valor total dos patrimônios
            double valorTotal = 0.0;
            for (Patrimonio p : todosPatrimonios) {
                if (p.getValorAquisicao() != null) {
                    valorTotal += p.getValorAquisicao().doubleValue();
                }
            }
            
            // Buscar número de coletores ativos (usuários que fizeram coletas)
            int coletoresAtivos = 0;
            try {
                List<com.inventario.model.Usuario> todosUsuarios = usuarioService.listarTodos();
                for (com.inventario.model.Usuario u : todosUsuarios) {
                    List<com.inventario.model.Coleta> coletasUsuario = coletaDAO.buscarPorColetor(u.getId());
                    if (!coletasUsuario.isEmpty()) {
                        coletoresAtivos++;
                    }
                }
            } catch (Exception e) {
                logger.warn("Erro ao contar coletores ativos: {}", e.getMessage());
            }
            
            // Buscar divergências (patrimônios com problemas)
            int divergencias = 0;
            // TODO: Implementar lógica para contar divergências reais
            
            DashboardStatsDTO stats = new DashboardStatsDTO(
                totalPatrimonios,
                patrimoniosColetados,
                divergencias,  // Corrigido: era patrimoniosPendentes
                valorTotal,
                coletoresAtivos
            );
            
            logger.info("Estatísticas: Total={}, Coletados={}, Pendentes={}, Valor={}, Coletores={}", 
                    totalPatrimonios, patrimoniosColetados, patrimoniosPendentes, valorTotal, coletoresAtivos);
            
            return ResponseEntity.ok(
                    ApiResponse.success(stats, "Estatísticas carregadas com sucesso"));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar estatísticas do dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar estatísticas", "FETCH_ERROR"));
        }
    }
}
