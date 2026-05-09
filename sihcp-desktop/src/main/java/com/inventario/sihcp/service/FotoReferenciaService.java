package com.inventario.sihcp.service;

import com.inventario.sihcp.dao.FotoReferenciaDAO;
import com.inventario.sihcp.model.DescricaoResumo;
import com.inventario.sihcp.model.FotoReferencia;
import com.inventario.sihcp.util.DescricaoNormalizador;
import com.inventario.sihcp.util.ImageProcessor;
import com.inventario.sihcp.util.ImageProcessor.ImageProcessingException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciamento de fotos de referência de patrimônios.
 * 
 * Responsável por:
 * - Cadastrar e atualizar fotos de referência
 * - Processar imagens (redimensionar, comprimir)
 * - Normalizar descrições para agrupamento
 * - Fornecer estatísticas de uso
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class FotoReferenciaService {
    
    private final FotoReferenciaDAO fotoReferenciaDAO;
    private final ImageProcessor imageProcessor;
    
    /**
     * Construtor padrão
     */
    public FotoReferenciaService() {
        this.fotoReferenciaDAO = new FotoReferenciaDAO();
        this.imageProcessor = new ImageProcessor();
    }
    
    /**
     * Construtor com injeção de dependências (para testes)
     */
    public FotoReferenciaService(FotoReferenciaDAO dao, ImageProcessor processor) {
        this.fotoReferenciaDAO = dao;
        this.imageProcessor = processor;
    }
    
    /**
     * Busca todas as descrições únicas de patrimônios com informação de foto.
     * 
     * @return Lista de resumos de descrições
     * @throws ServiceException Se houver erro na busca
     */
    public List<DescricaoResumo> buscarDescricoesUnicas() throws ServiceException {
        try {
            return fotoReferenciaDAO.listarDescricoesUnicas();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar descrições únicas", e);
        }
    }
    
    /**
     * Busca descrições únicas filtradas por texto.
     * 
     * @param filtro Texto para filtrar (case-insensitive)
     * @return Lista de resumos filtrados
     * @throws ServiceException Se houver erro na busca
     */
    public List<DescricaoResumo> buscarDescricoesUnicasFiltradas(String filtro) throws ServiceException {
        try {
            return fotoReferenciaDAO.listarDescricoesUnicasFiltradas(filtro);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar descrições filtradas", e);
        }
    }
    
    /**
     * Salva uma foto de referência para uma descrição.
     * Processa a imagem (redimensiona e comprime) antes de salvar.
     * 
     * @param descricao Descrição do patrimônio (será normalizada)
     * @param imagemOriginal Bytes da imagem original
     * @param usuarioCadastro Login do usuário que está cadastrando
     * @return FotoReferencia salva
     * @throws ServiceException Se houver erro no processamento ou salvamento
     */
    public FotoReferencia salvarFotoReferencia(String descricao, byte[] imagemOriginal, 
                                               String usuarioCadastro) throws ServiceException {
        // Validar entrada
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new ServiceException("Descrição não pode ser vazia");
        }
        
        if (imagemOriginal == null || imagemOriginal.length == 0) {
            throw new ServiceException("Imagem não pode ser vazia");
        }
        
        try {
            // 1. Normalizar descrição
            String descricaoNormalizada = DescricaoNormalizador.normalizar(descricao);
            
            // 2. Processar imagem (redimensionar e comprimir)
            byte[] thumbnail = imageProcessor.processarParaThumbnail(imagemOriginal);
            
            // 3. Calcular hash
            String hash = imageProcessor.calcularHash(thumbnail);
            
            // 4. Criar objeto FotoReferencia
            FotoReferencia foto = new FotoReferencia();
            foto.setDescricaoNormalizada(descricaoNormalizada);
            foto.setImagemBlob(thumbnail);
            foto.setHashImagem(hash);
            foto.setTamanhoBytes(thumbnail.length);
            foto.setUsuarioCadastro(usuarioCadastro);
            
            // 5. Salvar ou atualizar
            fotoReferenciaDAO.salvarOuAtualizar(foto);
            
            return foto;
            
        } catch (ImageProcessingException e) {
            throw new ServiceException("Erro ao processar imagem: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao salvar foto de referência", e);
        }
    }
    
    /**
     * Busca foto de referência por descrição.
     * 
     * @param descricao Descrição do patrimônio
     * @return Optional com a foto ou vazio se não encontrada
     * @throws ServiceException Se houver erro na busca
     */
    public Optional<FotoReferencia> buscarPorDescricao(String descricao) throws ServiceException {
        if (descricao == null || descricao.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            // Tentar busca exata primeiro
            FotoReferencia foto = fotoReferenciaDAO.buscarPorDescricao(descricao);
            
            if (foto == null) {
                // Tentar com descrição normalizada
                String normalizada = DescricaoNormalizador.normalizar(descricao);
                foto = fotoReferenciaDAO.buscarPorDescricao(normalizada);
            }
            
            if (foto == null) {
                // Tentar busca parcial (LIKE)
                foto = fotoReferenciaDAO.buscarPorDescricaoLike(descricao);
            }
            
            return Optional.ofNullable(foto);
            
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar foto por descrição", e);
        }
    }
    
    /**
     * Busca foto de referência por ID.
     * 
     * @param id ID da foto
     * @return Optional com a foto ou vazio se não encontrada
     * @throws ServiceException Se houver erro na busca
     */
    public Optional<FotoReferencia> buscarPorId(int id) throws ServiceException {
        try {
            FotoReferencia foto = fotoReferenciaDAO.findById(id);
            return Optional.ofNullable(foto);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar foto por ID", e);
        }
    }
    
    /**
     * Lista todas as fotos de referência ativas.
     * 
     * @return Lista de fotos ativas
     * @throws ServiceException Se houver erro na busca
     */
    public List<FotoReferencia> listarTodas() throws ServiceException {
        try {
            return fotoReferenciaDAO.listarAtivas();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar fotos de referência", e);
        }
    }
    
    /**
     * Busca fotos atualizadas desde um timestamp (para sincronização).
     * 
     * @param desde Timestamp de referência
     * @param pagina Número da página (0-based)
     * @param tamanhoPagina Tamanho da página (máximo 50)
     * @return Lista de fotos atualizadas
     * @throws ServiceException Se houver erro na busca
     */
    public List<FotoReferencia> buscarAtualizadasDesde(Timestamp desde, int pagina, 
                                                       int tamanhoPagina) throws ServiceException {
        try {
            // Limitar tamanho da página
            int tamanho = Math.min(tamanhoPagina, 50);
            int offset = pagina * tamanho;
            
            return fotoReferenciaDAO.buscarAtualizadasDesde(desde, tamanho, offset);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar fotos atualizadas", e);
        }
    }
    
    /**
     * Conta fotos atualizadas desde um timestamp.
     * 
     * @param desde Timestamp de referência
     * @return Quantidade de fotos atualizadas
     * @throws ServiceException Se houver erro na contagem
     */
    public int contarAtualizadasDesde(Timestamp desde) throws ServiceException {
        try {
            return fotoReferenciaDAO.contarAtualizadasDesde(desde);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao contar fotos atualizadas", e);
        }
    }
    
    /**
     * Exclui uma foto de referência (soft delete).
     * 
     * @param id ID da foto
     * @throws ServiceException Se houver erro na exclusão
     */
    public void excluirFotoReferencia(int id) throws ServiceException {
        try {
            int afetados = fotoReferenciaDAO.excluirSoft(id);
            if (afetados == 0) {
                throw new ServiceException("Foto de referência não encontrada");
            }
        } catch (SQLException e) {
            throw new ServiceException("Erro ao excluir foto de referência", e);
        }
    }
    
    /**
     * Obtém estatísticas de fotos de referência.
     * 
     * @return EstatisticasFoto com totais e uso de armazenamento
     * @throws ServiceException Se houver erro na obtenção
     */
    public EstatisticasFoto obterEstatisticas() throws ServiceException {
        try {
            long[] stats = fotoReferenciaDAO.obterEstatisticas();
            
            EstatisticasFoto estatisticas = new EstatisticasFoto();
            estatisticas.setTotalFotos((int) stats[0]);
            estatisticas.setTotalDescricoes((int) stats[1]);
            estatisticas.setUsoArmazenamentoBytes(stats[2]);
            estatisticas.setDescricoesSemFoto((int) (stats[1] - stats[0]));
            
            return estatisticas;
            
        } catch (SQLException e) {
            throw new ServiceException("Erro ao obter estatísticas", e);
        }
    }
    
    /**
     * Normaliza uma descrição de patrimônio.
     * 
     * @param descricao Descrição original
     * @return Descrição normalizada
     */
    public String normalizarDescricao(String descricao) {
        return DescricaoNormalizador.normalizar(descricao);
    }
    
    /**
     * Verifica se existe foto para uma descrição.
     * 
     * @param descricao Descrição do patrimônio
     * @return true se existe foto
     * @throws ServiceException Se houver erro na verificação
     */
    public boolean existeFotoParaDescricao(String descricao) throws ServiceException {
        try {
            String normalizada = DescricaoNormalizador.normalizar(descricao);
            return fotoReferenciaDAO.existeFotoParaDescricao(normalizada);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao verificar existência de foto", e);
        }
    }
    
    /**
     * Classe interna para estatísticas de fotos.
     */
    public static class EstatisticasFoto {
        private int totalFotos;
        private int totalDescricoes;
        private int descricoesSemFoto;
        private long usoArmazenamentoBytes;
        
        public int getTotalFotos() {
            return totalFotos;
        }
        
        public void setTotalFotos(int totalFotos) {
            this.totalFotos = totalFotos;
        }
        
        public int getTotalDescricoes() {
            return totalDescricoes;
        }
        
        public void setTotalDescricoes(int totalDescricoes) {
            this.totalDescricoes = totalDescricoes;
        }
        
        public int getDescricoesSemFoto() {
            return descricoesSemFoto;
        }
        
        public void setDescricoesSemFoto(int descricoesSemFoto) {
            this.descricoesSemFoto = descricoesSemFoto;
        }
        
        public long getUsoArmazenamentoBytes() {
            return usoArmazenamentoBytes;
        }
        
        public void setUsoArmazenamentoBytes(long usoArmazenamentoBytes) {
            this.usoArmazenamentoBytes = usoArmazenamentoBytes;
        }
        
        public String getUsoArmazenamentoFormatado() {
            if (usoArmazenamentoBytes < 1024) {
                return usoArmazenamentoBytes + " bytes";
            } else if (usoArmazenamentoBytes < 1024 * 1024) {
                return String.format("%.1f KB", usoArmazenamentoBytes / 1024.0);
            } else {
                return String.format("%.1f MB", usoArmazenamentoBytes / (1024.0 * 1024.0));
            }
        }
        
        public double getPercentualComFoto() {
            if (totalDescricoes == 0) return 0;
            return (totalFotos * 100.0) / totalDescricoes;
        }
    }
    
    /**
     * Exceção específica do service.
     */
    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
        
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
