package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.config.CampusConfig;
import com.inventario.sihcp.config.DatabaseConfigManager;
import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.CampusInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para informações públicas do campus.
 *
 * <p>Endpoint público (sem autenticação JWT) que permite ao app Android
 * identificar o campus ao qual está conectado antes de realizar o login.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code GET /api/mobile/campus/info} — retorna nome, sigla e cidade do campus</li>
 * </ul>
 *
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/campus")
// @CrossOrigin removido — ver MobileSecurityConfig.corsConfigurationSource() (spec correcoes-seguranca Req 6.1)
public class MobileCampusController {

    private static final Logger logger = LoggerFactory.getLogger(MobileCampusController.class);

    @Autowired
    private DatabaseConfigManager databaseConfigManager;

    /**
     * Retorna as informações públicas do campus configurado nesta instalação.
     *
     * <p>Este endpoint é público (não requer token JWT) para que o app Android
     * possa exibir o nome do campus na tela de login antes de o usuário se
     * autenticar.</p>
     *
     * <p>Compatibilidade retroativa: se o {@code configuracao_banco.json} não
     * contiver a seção {@code campus}, os campos do DTO serão {@code null} e a
     * resposta ainda será HTTP 200, garantindo que instalações existentes
     * continuem funcionando.</p>
     *
     * @return {@link ApiResponse} contendo {@link CampusInfoDTO} com nome, sigla
     *         e cidade do campus
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<CampusInfoDTO>> getCampusInfo() {
        logger.info("GET /api/mobile/campus/info — consultando informações do campus");

        try {
            CampusConfig campusConfig = databaseConfigManager.getCampusConfig();

            CampusInfoDTO dto = new CampusInfoDTO(
                    campusConfig.getNome(),
                    campusConfig.getSigla(),
                    campusConfig.getCidade()
            );

            logger.info("Informações do campus retornadas: {}", dto);

            return ResponseEntity.ok(
                    ApiResponse.success(dto, "Informações do campus")
            );

        } catch (Exception e) {
            logger.error("Erro ao obter informações do campus", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Erro ao obter informações do campus", "CAMPUS_INFO_ERROR"));
        }
    }
}
