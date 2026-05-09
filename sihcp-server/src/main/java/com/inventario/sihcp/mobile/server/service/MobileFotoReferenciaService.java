package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.inventario.sihcp.dao.FotoReferenciaDAO;
import com.inventario.sihcp.mobile.server.dto.MobileFotoReferenciaDTO;
import com.inventario.sihcp.mobile.server.dto.PagedResponse;
import com.inventario.sihcp.model.FotoReferencia;
import com.inventario.sihcp.util.DescricaoNormalizador;

/**
 * Serviço para operações de Foto de Referência na API Mobile.
 * 
 * Fornece endpoints para sincronização de fotos de referência entre
 * servidor e app Android, com suporte a delta sync e paginação.
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
@Service
public class MobileFotoReferenciaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileFotoReferenciaService.class);
    
    // Limites de paginação
    public static final int TAMANHO_PAGINA_PADRAO = 20;
    public static final int TAMANHO_PAGINA_MAXIMO = 50;

    /**
     * SQLState do PostgreSQL para "undefined_table" (relação não existe).
     * Código 42P01 é padrão SQL:2008 / PostgreSQL.
     */
    private static final String PG_UNDEFINED_TABLE = "42P01";
    
    private final FotoReferenciaDAO fotoReferenciaDAO;
    
    public MobileFotoReferenciaService() {
        this.fotoReferenciaDAO = new FotoReferenciaDAO();
    }

    /**
     * Verifica se a SQLException é causada por tabela inexistente no banco.
     * Suporta PostgreSQL (42P01) e H2/HSQL (como fallback pela mensagem).
     *
     * @param e Exceção SQL capturada
     * @return true se o erro é de tabela/relação inexistente
     */
    private boolean isTabelaNaoExiste(SQLException e) {
        if (PG_UNDEFINED_TABLE.equals(e.getSQLState())) {
            return true;
        }
        // Fallback para outros bancos (H2, HSQL)
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return msg.contains("does not exist") || msg.contains("não existe") ||
               msg.contains("table not found") || msg.contains("relation") && msg.contains("exist");
    }
    
    /**
     * Busca fotos de referência atualizadas desde um timestamp (delta sync).
     * Suporta paginação para evitar sobrecarga de memória.
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização (null para todas)
     * @param pagina Número da página (0-indexed)
     * @param tamanho Tamanho da página (max 50)
     * @return Resposta paginada com fotos de referência
     */
    public PagedResponse<MobileFotoReferenciaDTO> buscarFotosAtualizadas(
            Long ultimaAtualizacao, int pagina, int tamanho) throws SQLException {
        
        logger.debug("Buscando fotos atualizadas - ultimaAtualizacao: {}, pagina: {}, tamanho: {}",
            ultimaAtualizacao, pagina, tamanho);
        
        // Validar e ajustar tamanho da página
        tamanho = Math.min(Math.max(1, tamanho), TAMANHO_PAGINA_MAXIMO);
        pagina = Math.max(0, pagina);
        
        int offset = pagina * tamanho;
        
        // Determinar timestamp de referência
        Timestamp desde = ultimaAtualizacao != null && ultimaAtualizacao > 0 
            ? new Timestamp(ultimaAtualizacao) 
            : new Timestamp(0);
        
        try {
            // Buscar fotos com paginação
            List<FotoReferencia> fotos = fotoReferenciaDAO.buscarAtualizadasDesde(desde, tamanho, offset);
            int totalElementos = fotoReferenciaDAO.contarAtualizadasDesde(desde);
            
            // Converter para DTOs
            List<MobileFotoReferenciaDTO> dtos = fotos.stream()
                .map(MobileFotoReferenciaDTO::new)
                .collect(Collectors.toList());
            
            logger.debug("Retornando {} fotos de {} total", dtos.size(), totalElementos);
            return new PagedResponse<>(dtos, pagina, tamanho, totalElementos);

        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela '{}' não existe no banco de dados. Retornando lista vazia. "
                    + "Crie a tabela para habilitar o recurso de fotos de referência. ERRO: {}",
                    "tabela_foto_referencia", e.getMessage());
                return new PagedResponse<>(new ArrayList<>(), pagina, tamanho, 0);
            }
            throw e;
        }
    }
    
    /**
     * Busca foto de referência por ID.
     * 
     * @param id ID da foto
     * @return DTO da foto ou null se não encontrada
     */
    public MobileFotoReferenciaDTO buscarPorId(int id) throws SQLException {
        logger.debug("Buscando foto por ID: {}", id);
        
        try {
            FotoReferencia foto = fotoReferenciaDAO.findById(id);
            if (foto == null || !foto.isAtivo()) {
                logger.debug("Foto não encontrada ou inativa: {}", id);
                return null;
            }
            return new MobileFotoReferenciaDTO(foto);
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando null para id={}. ERRO: {}",
                    id, e.getMessage());
                return null;
            }
            throw e;
        }
    }
    
    /**
     * Busca foto de referência por descrição.
     * A descrição é normalizada antes da busca.
     * 
     * @param descricao Descrição do patrimônio
     * @return DTO da foto ou null se não encontrada
     */
    public MobileFotoReferenciaDTO buscarPorDescricao(String descricao) throws SQLException {
        if (descricao == null || descricao.trim().isEmpty()) {
            logger.debug("Descrição vazia ou nula");
            return null;
        }
        
        // Normalizar descrição antes de buscar
        String descricaoNormalizada = DescricaoNormalizador.normalizar(descricao);
        logger.debug("Buscando foto por descrição normalizada: {}", descricaoNormalizada);
        
        try {
            FotoReferencia foto = fotoReferenciaDAO.buscarPorDescricao(descricaoNormalizada);
            if (foto == null) {
                // Tentar busca parcial
                foto = fotoReferenciaDAO.buscarPorDescricaoLike(descricaoNormalizada);
            }
            if (foto == null || !foto.isAtivo()) {
                logger.debug("Foto não encontrada para descrição: {}", descricaoNormalizada);
                return null;
            }
            return new MobileFotoReferenciaDTO(foto);
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando null para descrição='{}'. ERRO: {}",
                    descricaoNormalizada, e.getMessage());
                return null;
            }
            throw e;
        }
    }
    
    /**
     * Busca múltiplas fotos por lista de descrições.
     * Útil para pré-carregar fotos de uma lista de patrimônios.
     * 
     * @param descricoes Lista de descrições
     * @return Mapa de descrição normalizada -> DTO
     */
    public Map<String, MobileFotoReferenciaDTO> buscarPorDescricoes(List<String> descricoes) throws SQLException {
        if (descricoes == null || descricoes.isEmpty()) {
            return new HashMap<>();
        }
        
        logger.debug("Buscando fotos para {} descrições", descricoes.size());
        
        Map<String, MobileFotoReferenciaDTO> resultado = new HashMap<>();
        
        for (String descricao : descricoes) {
            if (descricao == null || descricao.trim().isEmpty()) {
                continue;
            }
            
            String descricaoNormalizada = DescricaoNormalizador.normalizar(descricao);
            
            // Evitar buscas duplicadas
            if (resultado.containsKey(descricaoNormalizada)) {
                continue;
            }
            
            try {
                FotoReferencia foto = fotoReferenciaDAO.buscarPorDescricao(descricaoNormalizada);
                if (foto != null && foto.isAtivo()) {
                    resultado.put(descricaoNormalizada, new MobileFotoReferenciaDTO(foto));
                }
            } catch (SQLException e) {
                logger.warn("Erro ao buscar foto para descrição: {}", descricaoNormalizada, e);
            }
        }
        
        logger.debug("Encontradas {} fotos de {} descrições", resultado.size(), descricoes.size());
        return resultado;
    }
    
    /**
     * Verifica se há atualizações disponíveis desde um timestamp.
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Mapa com informações de atualização
     */
    public Map<String, Object> verificarAtualizacoes(Long ultimaAtualizacao) throws SQLException {
        Timestamp desde = ultimaAtualizacao != null && ultimaAtualizacao > 0 
            ? new Timestamp(ultimaAtualizacao) 
            : new Timestamp(0);
        
        int quantidadeAtualizada;
        try {
            quantidadeAtualizada = fotoReferenciaDAO.contarAtualizadasDesde(desde);
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando 0 atualizações. ERRO: {}",
                    e.getMessage());
                quantidadeAtualizada = 0;
            } else {
                throw e;
            }
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("temAtualizacoes", quantidadeAtualizada > 0);
        resultado.put("quantidadeAtualizada", quantidadeAtualizada);
        resultado.put("timestamp", System.currentTimeMillis());
        
        return resultado;
    }
    
    /**
     * Obtém estatísticas de fotos de referência.
     * 
     * @return Mapa com estatísticas
     */
    public Map<String, Object> obterEstatisticas() throws SQLException {
        long[] stats;
        try {
            stats = fotoReferenciaDAO.obterEstatisticas();
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando estatísticas zeradas. ERRO: {}",
                    e.getMessage());
                stats = new long[]{0L, 0L, 0L};
            } else {
                throw e;
            }
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalFotos", stats[0]);
        resultado.put("totalDescricoes", stats[1]);
        resultado.put("usoArmazenamentoBytes", stats[2]);
        resultado.put("usoArmazenamentoKB", stats[2] / 1024.0);
        resultado.put("usoArmazenamentoMB", stats[2] / (1024.0 * 1024.0));
        resultado.put("timestamp", System.currentTimeMillis());
        resultado.put("tabelaDisponivel", stats[0] >= 0);
        
        return resultado;
    }
    
    /**
     * Lista todas as fotos ativas (sem paginação).
     * ATENÇÃO: Usar apenas para conjuntos pequenos de dados.
     * 
     * @return Lista de DTOs
     */
    public List<MobileFotoReferenciaDTO> listarTodas() throws SQLException {
        logger.debug("Listando todas as fotos ativas");
        
        try {
            List<FotoReferencia> fotos = fotoReferenciaDAO.listarAtivas();
            return fotos.stream()
                .map(MobileFotoReferenciaDTO::new)
                .collect(Collectors.toList());
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando lista vazia. ERRO: {}",
                    e.getMessage());
                return new ArrayList<>();
            }
            throw e;
        }
    }
    
    /**
     * Lista fotos apenas com metadados (sem imagem Base64).
     * Útil para verificar quais fotos o cliente precisa baixar.
     * 
     * @param ultimaAtualizacao Timestamp da última sincronização
     * @return Lista de DTOs sem imagem
     */
    public List<MobileFotoReferenciaDTO> listarMetadados(Long ultimaAtualizacao) throws SQLException {
        Timestamp desde = ultimaAtualizacao != null && ultimaAtualizacao > 0 
            ? new Timestamp(ultimaAtualizacao) 
            : new Timestamp(0);
        
        try {
            // Buscar todas as fotos atualizadas (sem limite para metadados)
            List<FotoReferencia> fotos = fotoReferenciaDAO.buscarAtualizadasDesde(desde, 1000, 0);
            // Converter para DTOs sem imagem
            return fotos.stream()
                .map(foto -> new MobileFotoReferenciaDTO(foto, false))
                .collect(Collectors.toList());
        } catch (SQLException e) {
            if (isTabelaNaoExiste(e)) {
                logger.warn("Tabela 'tabela_foto_referencia' não existe. Retornando metadados vazios. ERRO: {}",
                    e.getMessage());
                return new ArrayList<>();
            }
            throw e;
        }
    }
    
    /**
     * Busca fotos por lista de hashes.
     * Útil para verificar quais fotos o cliente já possui.
     * 
     * @param hashes Lista de hashes SHA-256
     * @return Lista de hashes que existem no servidor
     */
    public List<String> verificarHashesExistentes(List<String> hashes) throws SQLException {
        if (hashes == null || hashes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<FotoReferencia> todasFotos = fotoReferenciaDAO.listarAtivas();
        
        return todasFotos.stream()
            .map(FotoReferencia::getHashImagem)
            .filter(hashes::contains)
            .collect(Collectors.toList());
    }
}
