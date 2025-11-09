package com.inventario.service;

import com.inventario.dao.DispositivoMobileDAO;
import com.inventario.model.DispositivoMobile;
import com.inventario.model.StatusDispositivo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciamento de dispositivos mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class DispositivoMobileService {

    private final DispositivoMobileDAO dispositivoDAO;

    public DispositivoMobileService() {
        this.dispositivoDAO = new DispositivoMobileDAO();
    }

    /**
     * Registra um novo dispositivo ou atualiza se já existir
     */
    public DispositivoMobile registrarDispositivo(DispositivoMobile dispositivo) throws SQLException {
        // Validações
        if (dispositivo.getDeviceId() == null || dispositivo.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("Device ID é obrigatório");
        }
        
        if (dispositivo.getIdUsuario() == null) {
            throw new IllegalArgumentException("ID do usuário é obrigatório");
        }

        return dispositivoDAO.registrarDispositivo(dispositivo);
    }

    /**
     * Busca dispositivo por ID
     */
    public Optional<DispositivoMobile> buscarPorId(Integer id) throws SQLException {
        return dispositivoDAO.buscarPorId(id);
    }

    /**
     * Busca dispositivo por Device ID
     */
    public Optional<DispositivoMobile> buscarPorDeviceId(String deviceId) throws SQLException {
        return dispositivoDAO.buscarPorDeviceId(deviceId);
    }

    /**
     * Lista todos os dispositivos
     */
    public List<DispositivoMobile> listarTodos() throws SQLException {
        return dispositivoDAO.listarTodos();
    }

    /**
     * Lista dispositivos pendentes de aprovação
     */
    public List<DispositivoMobile> listarPendentes() throws SQLException {
        return dispositivoDAO.listarPorStatus(StatusDispositivo.PENDENTE);
    }

    /**
     * Lista dispositivos aprovados
     */
    public List<DispositivoMobile> listarAprovados() throws SQLException {
        return dispositivoDAO.listarPorStatus(StatusDispositivo.APROVADO);
    }

    /**
     * Lista dispositivos bloqueados
     */
    public List<DispositivoMobile> listarBloqueados() throws SQLException {
        return dispositivoDAO.listarPorStatus(StatusDispositivo.BLOQUEADO);
    }

    /**
     * Lista dispositivos de um usuário
     */
    public List<DispositivoMobile> listarPorUsuario(Integer idUsuario) throws SQLException {
        return dispositivoDAO.listarPorUsuario(idUsuario);
    }

    /**
     * Aprova um dispositivo
     */
    public void aprovarDispositivo(Integer id) throws SQLException {
        Optional<DispositivoMobile> dispositivo = buscarPorId(id);
        
        if (dispositivo.isEmpty()) {
            throw new IllegalArgumentException("Dispositivo não encontrado");
        }

        dispositivoDAO.atualizarStatus(id, StatusDispositivo.APROVADO);
        dispositivoDAO.alterarAtivacao(id, true);
    }

    /**
     * Bloqueia um dispositivo
     */
    public void bloquearDispositivo(Integer id) throws SQLException {
        Optional<DispositivoMobile> dispositivo = buscarPorId(id);
        
        if (dispositivo.isEmpty()) {
            throw new IllegalArgumentException("Dispositivo não encontrado");
        }

        dispositivoDAO.atualizarStatus(id, StatusDispositivo.BLOQUEADO);
        dispositivoDAO.alterarAtivacao(id, false);
    }

    /**
     * Desbloqueia um dispositivo
     */
    public void desbloquearDispositivo(Integer id) throws SQLException {
        Optional<DispositivoMobile> dispositivo = buscarPorId(id);
        
        if (dispositivo.isEmpty()) {
            throw new IllegalArgumentException("Dispositivo não encontrado");
        }

        dispositivoDAO.atualizarStatus(id, StatusDispositivo.APROVADO);
        dispositivoDAO.alterarAtivacao(id, true);
    }

    /**
     * Rejeita um dispositivo
     */
    public void rejeitarDispositivo(Integer id) throws SQLException {
        Optional<DispositivoMobile> dispositivo = buscarPorId(id);
        
        if (dispositivo.isEmpty()) {
            throw new IllegalArgumentException("Dispositivo não encontrado");
        }

        dispositivoDAO.atualizarStatus(id, StatusDispositivo.REJEITADO);
        dispositivoDAO.alterarAtivacao(id, false);
    }

    /**
     * Atualiza token do dispositivo
     */
    public void atualizarToken(Integer id, String token, LocalDateTime dataExpiracao) throws SQLException {
        dispositivoDAO.atualizarToken(id, token, dataExpiracao);
    }

    /**
     * Registra sincronização do dispositivo
     */
    public void registrarSincronizacao(Integer id) throws SQLException {
        dispositivoDAO.registrarSincronizacao(id);
    }

    /**
     * Remove um dispositivo
     */
    public void removerDispositivo(Integer id) throws SQLException {
        dispositivoDAO.remover(id);
    }

    /**
     * Verifica se dispositivo está autorizado
     */
    public boolean isDispositivoAutorizado(String deviceId) throws SQLException {
        Optional<DispositivoMobile> dispositivo = buscarPorDeviceId(deviceId);
        
        if (dispositivo.isEmpty()) {
            return false;
        }

        return dispositivo.get().isAprovado();
    }

    /**
     * Conta dispositivos por status
     */
    public int contarPorStatus(StatusDispositivo status) throws SQLException {
        return dispositivoDAO.listarPorStatus(status).size();
    }

    /**
     * Conta dispositivos pendentes
     */
    public int contarPendentes() throws SQLException {
        return contarPorStatus(StatusDispositivo.PENDENTE);
    }
}
