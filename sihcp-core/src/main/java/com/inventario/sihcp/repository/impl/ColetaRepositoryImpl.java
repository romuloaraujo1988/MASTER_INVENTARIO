package com.inventario.sihcp.repository.impl;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.exception.RepositoryException;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.repository.ColetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do Repository de Coleta
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Repository
public class ColetaRepositoryImpl implements ColetaRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ColetaRepositoryImpl.class);
    
    private final ColetaDAO coletaDAO;
    
    @Autowired
    public ColetaRepositoryImpl(ColetaDAO coletaDAO) {
        this.coletaDAO = coletaDAO;
    }
    
    @Override
    public Optional<Coleta> findById(Integer id) {
        try {
            logger.debug("Buscando coleta por ID: {}", id);
            Coleta coleta = coletaDAO.buscarPorId(id);  // ✅ Método correto do DAO
            return Optional.ofNullable(coleta);
        } catch (SQLException e) {
            logger.error("Erro ao buscar coleta por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<Coleta> findAll() {
        try {
            logger.debug("Listando todas as coletas");
            return coletaDAO.listarTodas();  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao listar coletas", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Coleta> findByInventario(Integer idInventario) {
        try {
            logger.debug("Buscando coletas do inventário: {}", idInventario);
            return coletaDAO.buscarPorInventario(idInventario);  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas do inventário: {}", idInventario, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Coleta> findBySala(Integer idSala) {
        try {
            logger.debug("Buscando coletas da sala: {}", idSala);
            return coletaDAO.buscarColetasPorSala(idSala);  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas da sala: {}", idSala, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Coleta> findByUsuario(Integer idUsuario) {
        try {
            logger.debug("Buscando coletas do usuário: {}", idUsuario);
            return coletaDAO.buscarPorColetor(idUsuario);  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao buscar coletas do usuário: {}", idUsuario, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public Optional<Coleta> findByNumeroPatrimonio(String numeroPatrimonio) {
        try {
            logger.debug("Buscando coleta por número de patrimônio: {}", numeroPatrimonio);
            // Buscar por patrimônio e retornar a primeira coleta encontrada
            List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(
                buscarIdPatrimonioPorNumero(numeroPatrimonio)
            );
            return coletas.isEmpty() ? Optional.empty() : Optional.of(coletas.get(0));
        } catch (SQLException e) {
            logger.error("Erro ao buscar coleta por número de patrimônio: {}", numeroPatrimonio, e);
            return Optional.empty();
        }
    }
    
    @Override
    public Coleta save(Coleta coleta) {
        try {
            // Se getId() retorna int primitivo, apenas verificar se é maior que 0
            if (coleta.getId() > 0) {
                logger.info("Atualizando coleta: {}", coleta.getId());
                coletaDAO.atualizarColeta(coleta);  // ✅ Método correto do DAO
            } else {
                logger.info("Inserindo nova coleta: {}", coleta.getNumeroPatrimonio());
                coletaDAO.inserirColeta(coleta);  // ✅ Método correto do DAO
            }
            return coleta;
        } catch (SQLException e) {
            logger.error("Erro ao salvar coleta: {}", coleta.getNumeroPatrimonio(), e);
            throw new RepositoryException("Erro ao salvar coleta", e);
        }
    }
    
    @Override
    public void delete(Integer id) {
        try {
            logger.info("Excluindo coleta: {}", id);
            coletaDAO.excluirColeta(id);  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao excluir coleta: {}", id, e);
            throw new RepositoryException("Erro ao excluir coleta", e);
        }
    }
    
    @Override
    public long countByInventario(Integer idInventario) {
        try {
            logger.debug("Contando coletas do inventário: {}", idInventario);
            return coletaDAO.contarColetasPorInventario(idInventario);  // ✅ Método correto do DAO
        } catch (SQLException e) {
            logger.error("Erro ao contar coletas do inventário: {}", idInventario, e);
            return 0;
        }
    }
    
    @Override
    public boolean existsByNumeroPatrimonio(String numeroPatrimonio) {
        try {
            logger.debug("Verificando se patrimônio já foi coletado: {}", numeroPatrimonio);
            int idPatrimonio = buscarIdPatrimonioPorNumero(numeroPatrimonio);
            if (idPatrimonio > 0) {
                // Verificar se existe coleta para este patrimônio
                List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(idPatrimonio);
                return !coletas.isEmpty();
            }
            return false;
        } catch (SQLException e) {
            logger.error("Erro ao verificar coleta do patrimônio: {}", numeroPatrimonio, e);
            return false;
        }
    }
    
    /**
     * Método auxiliar para buscar ID do patrimônio por número
     */
    private int buscarIdPatrimonioPorNumero(String numeroPatrimonio) throws SQLException {
        String sql = "SELECT ID FROM TABELA_PATRIMONIO WHERE NUMERO = ?";
        
        try (Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, numeroPatrimonio);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        
        return 0;
    }
}
