package com.inventario.mobile.server.service;

import com.inventario.dao.FotoReferenciaDAO;
import com.inventario.model.FotoReferencia;
import com.inventario.mobile.server.dto.MobileFotoReferenciaDTO;
import com.inventario.mobile.server.dto.PagedResponse;
import com.inventario.util.DescricaoNormalizador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    private final FotoReferenciaDAO fotoReferenciaDAO;
    private final DescricaoNormalizador normalizador;
    
    public MobileFotoReferenciaService() {
        this.fotoReferenciaDAO = new FotoReferenciaDAO();
        this.normalizador = new DescricaoNormalizador();
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
        
        // Buscar fotos com paginação
        List<FotoReferencia> fotos = fotoReferenciaDAO.buscarAtualizadasDesde(desde, tamanho, offset);
        int totalElementos = fotoReferenciaDAO.contarAtualizadasDesde(desde);
        
        // Converter para DTOs
        List<MobileFotoReferenciaDTO> dtos = fotos.stream()
            .map(MobileFotoReferenciaDTO::new)
            .collect(Collectors.toList());
        
        // Calcular total de páginas
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanho);
        
        logger.debug("Retornando {} fotos de {} total", dtos.size(), totalElementos);
        
        return new PagedResponse<>(
            dtos,
            pagina,
            tamanho,
            totalElementos,
            totalPaginas
        );
    }
    
    /**
     * Busca foto de referência por ID.
     * 
     * @param id ID da foto
     * @return DTO da foto ou null se não encontrada
     */
    public MobileFotoReferenciaDTO buscarPorId(int id) throws SQLException {
        logger.debug("Buscando foto por ID: {}", id);
        
        FotoReferencia foto = fotoReferenciaDAO.findById(id);
        
        if (foto == null || !foto.isAtivo()) {
            logger.debug("Foto não encontrada ou inativa: {}", id);
            return null;
        }
        
        return new MobileFotoReferenciaDTO(foto);
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
        String descricaoNormalizada = normalizador.normalizar(descricao);
        logger.debug("Buscando foto por descrição normalizada: {}", descricaoNormalizada);
        
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
            
            String descricaoNormalizada = normalizador.normalizar(descricao);
            
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
        
        int quantidadeAtualizada = fotoReferenciaDAO.contarAtualizadasDesde(desde);
        
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
        long[] stats = fotoReferenciaDAO.obterEstatisticas();
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalFotos", stats[0]);
        resultado.put("totalDescricoes", stats[1]);
        resultado.put("usoArmazenamentoBytes", stats[2]);
        resultado.put("usoArmazenamentoKB", stats[2] / 1024.0);
        resultado.put("usoArmazenamentoMB", stats[2] / (1024.0 * 1024.0));
        resultado.put("timestamp", System.currentTimeMillis());
        
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
        
        List<FotoReferencia> fotos = fotoReferenciaDAO.listarAtivas();
        
        return fotos.stream()
            .map(MobileFotoReferenciaDTO::new)
            .collect(Collectors.toList());
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
        
        // Buscar todas as fotos atualizadas (sem limite para metadados)
        List<FotoReferencia> fotos = fotoReferenciaDAO.buscarAtualizadasDesde(desde, 1000, 0);
        
        // Converter para DTOs sem imagem
        return fotos.stream()
            .map(foto -> new MobileFotoReferenciaDTO(foto, false))
            .collect(Collectors.toList());
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
