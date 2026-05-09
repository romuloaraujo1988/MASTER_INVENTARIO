package com.inventario.sihcp.mobile.server.controller;

import com.inventario.sihcp.mobile.server.dto.ApiResponse;
import com.inventario.sihcp.mobile.server.dto.DispositivoRegistroDTO;
import com.inventario.sihcp.model.DispositivoMobile;
import com.inventario.sihcp.service.DispositivoMobileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller REST para gerenciamento de dispositivos mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile/dispositivos")
public class MobileDispositivoController {

    private final DispositivoMobileService dispositivoService;

    public MobileDispositivoController() {
        this.dispositivoService = new DispositivoMobileService();
    }

    /**
     * Registra um novo dispositivo
     * POST /api/mobile/dispositivos/registrar
     */
    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<DispositivoMobile>> registrarDispositivo(
            @RequestBody DispositivoRegistroDTO dto) {
        
        try {
            DispositivoMobile dispositivo = new DispositivoMobile();
            dispositivo.setDeviceId(dto.getDeviceId());
            dispositivo.setIdUsuario(dto.getIdUsuario());
            dispositivo.setModelo(dto.getModelo());
            dispositivo.setFabricante(dto.getFabricante());
            dispositivo.setVersaoAndroid(dto.getVersaoAndroid());
            dispositivo.setVersaoApp(dto.getVersaoApp());
            dispositivo.setEnderecoIp(dto.getEnderecoIp());
            dispositivo.setEnderecoMac(dto.getEnderecoMac());

            DispositivoMobile registrado = dispositivoService.registrarDispositivo(dispositivo);

            return ResponseEntity.ok(
                ApiResponse.success(registrado, "Dispositivo registrado com sucesso")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao registrar dispositivo: " + e.getMessage())
            );
        }
    }

    /**
     * Verifica status do dispositivo
     * GET /api/mobile/dispositivos/status/{deviceId}
     */
    @GetMapping("/status/{deviceId}")
    public ResponseEntity<ApiResponse<DispositivoMobile>> verificarStatus(
            @PathVariable String deviceId) {
        
        try {
            var dispositivo = dispositivoService.buscarPorDeviceId(deviceId);

            if (dispositivo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("Dispositivo não encontrado")
                );
            }

            return ResponseEntity.ok(
                ApiResponse.success(dispositivo.get(), "Status do dispositivo")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao verificar status: " + e.getMessage())
            );
        }
    }

    /**
     * Verifica se dispositivo está autorizado
     * GET /api/mobile/dispositivos/autorizado/{deviceId}
     */
    @GetMapping("/autorizado/{deviceId}")
    public ResponseEntity<ApiResponse<Boolean>> verificarAutorizacao(
            @PathVariable String deviceId) {
        
        try {
            boolean autorizado = dispositivoService.isDispositivoAutorizado(deviceId);

            return ResponseEntity.ok(
                ApiResponse.success(autorizado, 
                    autorizado ? "Dispositivo autorizado" : "Dispositivo não autorizado")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao verificar autorização: " + e.getMessage())
            );
        }
    }

    /**
     * Lista todos os dispositivos (Admin)
     * GET /api/mobile/dispositivos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DispositivoMobile>>> listarTodos() {
        try {
            List<DispositivoMobile> dispositivos = dispositivoService.listarTodos();

            return ResponseEntity.ok(
                ApiResponse.success(dispositivos, "Lista de dispositivos")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao listar dispositivos: " + e.getMessage())
            );
        }
    }

    /**
     * Lista dispositivos pendentes (Admin)
     * GET /api/mobile/dispositivos/pendentes
     */
    @GetMapping("/pendentes")
    public ResponseEntity<ApiResponse<List<DispositivoMobile>>> listarPendentes() {
        try {
            List<DispositivoMobile> dispositivos = dispositivoService.listarPendentes();

            return ResponseEntity.ok(
                ApiResponse.success(dispositivos, "Dispositivos pendentes de aprovação")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao listar pendentes: " + e.getMessage())
            );
        }
    }

    /**
     * Aprova um dispositivo (Admin)
     * PUT /api/mobile/dispositivos/{id}/aprovar
     */
    @PutMapping("/{id}/aprovar")
    public ResponseEntity<ApiResponse<Void>> aprovarDispositivo(@PathVariable Integer id) {
        try {
            dispositivoService.aprovarDispositivo(id);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Dispositivo aprovado com sucesso")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao aprovar dispositivo: " + e.getMessage())
            );
        }
    }

    /**
     * Bloqueia um dispositivo (Admin)
     * PUT /api/mobile/dispositivos/{id}/bloquear
     */
    @PutMapping("/{id}/bloquear")
    public ResponseEntity<ApiResponse<Void>> bloquearDispositivo(@PathVariable Integer id) {
        try {
            dispositivoService.bloquearDispositivo(id);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Dispositivo bloqueado com sucesso")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao bloquear dispositivo: " + e.getMessage())
            );
        }
    }

    /**
     * Desbloqueia um dispositivo (Admin)
     * PUT /api/mobile/dispositivos/{id}/desbloquear
     */
    @PutMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse<Void>> desbloquearDispositivo(@PathVariable Integer id) {
        try {
            dispositivoService.desbloquearDispositivo(id);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Dispositivo desbloqueado com sucesso")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao desbloquear dispositivo: " + e.getMessage())
            );
        }
    }

    /**
     * Remove um dispositivo (Admin)
     * DELETE /api/mobile/dispositivos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removerDispositivo(@PathVariable Integer id) {
        try {
            dispositivoService.removerDispositivo(id);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Dispositivo removido com sucesso")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao remover dispositivo: " + e.getMessage())
            );
        }
    }

    /**
     * Registra sincronização
     * POST /api/mobile/dispositivos/{id}/sincronizar
     */
    @PostMapping("/{id}/sincronizar")
    public ResponseEntity<ApiResponse<Void>> registrarSincronizacao(@PathVariable Integer id) {
        try {
            dispositivoService.registrarSincronizacao(id);

            return ResponseEntity.ok(
                ApiResponse.success(null, "Sincronização registrada")
            );

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Erro ao registrar sincronização: " + e.getMessage())
            );
        }
    }
}
