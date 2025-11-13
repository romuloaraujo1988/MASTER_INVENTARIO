package com.inventario.mobile.server.service;

import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.model.Coleta;
import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;
import com.inventario.model.Usuario;
import com.inventario.util.SoundNotification;
import com.inventario.mobile.server.dto.MobileColetaRequest;
import com.inventario.mobile.server.dto.MobileColetaResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para operações de coleta mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Service
public class MobileColetaService {

    private static final Logger logger = LoggerFactory.getLogger(MobileColetaService.class);

    private final ColetaDAO coletaDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;
    private final UsuarioDAORefactored usuarioDAO;
    private final ParticipanteInventarioDAO participanteInventarioDAO;

    public MobileColetaService() {
        this.coletaDAO = new ColetaDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioDAO = new InventarioDAO();
        this.usuarioDAO = new UsuarioDAORefactored();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }

    /**
     * Registra uma nova coleta
     */
    public MobileColetaResponse registrarColeta(MobileColetaRequest request, String username) throws SQLException {
        logger.info("Registrando coleta para patrimônio: {} por usuário: {} (usuarioId: {})",
                request.getNumeroPatrimonio(), username, request.getUsuarioId());

        // Buscar usuário - prioriza username, mas aceita usuarioId se não houver username
        Usuario usuario = null;
        
        if (username != null && !username.isEmpty()) {
            usuario = usuarioDAO.buscarPorLogin(username);
            if (usuario == null) {
                throw new IllegalArgumentException("Usuário não encontrado: " + username);
            }
        } else if (request.getUsuarioId() != null) {
            usuario = usuarioDAO.findById(request.getUsuarioId());
            if (usuario == null) {
                throw new IllegalArgumentException("Usuário não encontrado com ID: " + request.getUsuarioId());
            }
        } else {
            throw new IllegalArgumentException("É obrigatório informar o usuário (username ou usuarioId)");
        }

        // Buscar inventário - se não informado ou inválido, busca o ativo
        Inventario inventario = null;
        
        if (request.getIdInventario() != null && request.getIdInventario() > 0) {
            try {
                inventario = inventarioDAO.findById(request.getIdInventario());
            } catch (SQLException e) {
                logger.warn("Erro ao buscar inventário por ID {}: {}", request.getIdInventario(), e.getMessage());
            }
        }
        
        // Se não encontrou ou não foi informado, busca o inventário ativo
        if (inventario == null) {
            logger.info("Buscando inventário ativo automaticamente...");
            try {
                inventario = inventarioDAO.buscarInventarioAtivo();
                if (inventario != null) {
                    logger.info("Inventário ativo encontrado: ID={}, Nome={}, Status={}", 
                            inventario.getId(), inventario.getNome(), inventario.getStatusInventario());
                } else {
                    logger.warn("Nenhum inventário com status 'EM_ANDAMENTO' foi encontrado no banco de dados");
                    
                    // Tentar listar todos os inventários para debug
                    try {
                        List<Inventario> todosInventarios = inventarioDAO.findAll();
                        logger.info("Total de inventários no banco: {}", todosInventarios.size());
                        for (Inventario inv : todosInventarios) {
                            logger.info("  - ID={}, Nome={}, Status={}", 
                                    inv.getId(), inv.getNome(), inv.getStatusInventario());
                        }
                    } catch (SQLException ex) {
                        logger.error("Erro ao listar inventários: {}", ex.getMessage());
                    }
                }
            } catch (SQLException e) {
                logger.error("Erro ao buscar inventário ativo: {}", e.getMessage(), e);
            }
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Nenhum inventário ativo encontrado. Por favor, inicie um inventário no sistema com status 'EM_ANDAMENTO'.");
        }

        // Buscar ID do participante
        Integer idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                inventario.getId(), usuario.getId());

        if (idParticipante == null) {
            throw new IllegalArgumentException("Usuário não é participante deste inventário");
        }

        // Criar objeto Coleta
        Coleta coleta = new Coleta();
        coleta.setIdInventario(request.getIdInventario());
        coleta.setIdColetor(usuario.getId());
        coleta.setIdParticipanteInventario(idParticipante);
        coleta.setDataColeta(convertToTimestamp(request.getDataColeta()));
        coleta.setStatusColeta("COLETADO");
        coleta.setObservacaoColeta(request.getObservacaoColeta());
        coleta.setLocalizacaoEncontrada(request.getLocalizacaoEncontrada());
        coleta.setEstadoEncontrado(request.getEstadoEncontrado());
        coleta.setDivergencia(false);
        coleta.setSemEtiqueta(request.getSemEtiqueta() != null ? request.getSemEtiqueta() : false);

        // Se não for item sem etiqueta, buscar patrimônio
        if (!coleta.isSemEtiqueta()) {
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(request.getNumeroPatrimonio());
            if (patrimonio == null) {
                throw new IllegalArgumentException("Patrimônio não encontrado: " + request.getNumeroPatrimonio());
            }
            coleta.setIdPatrimonio(patrimonio.getId());
            coleta.setLocalizacaoAtual(patrimonio.getNomeSala());
        } else {
            // Item sem etiqueta
            coleta.setDescricaoItemSemEtiqueta(request.getDescricaoItemSemEtiqueta());
            coleta.setCategoriaItemSemEtiqueta(request.getCategoriaItemSemEtiqueta());
        }

        // Geolocalização
        if (request.getLatitude() != null && request.getLongitude() != null) {
            coleta.setLatitude(java.math.BigDecimal.valueOf(request.getLatitude()));
            coleta.setLongitude(java.math.BigDecimal.valueOf(request.getLongitude()));
        }

        // Foto
        if (request.getFotoPatrimonio() != null) {
            coleta.setFotoPatrimonio(request.getFotoPatrimonio());
        }

        // Inserir no banco
        coletaDAO.inserirColeta(coleta);

        // Reproduzir som de sucesso
        SoundNotification.playColetaSalvaSound();

        logger.info("Coleta registrada com sucesso. ID: {}", coleta.getId());

        // Retornar resposta
        return converterParaResponse(coleta, usuario, inventario);
    }

    /**
     * Registra múltiplas coletas em lote
     */
    public Map<String, Object> registrarColetasEmLote(List<MobileColetaRequest> coletas, String username) {
        logger.info("Registrando {} coletas em lote para usuário: {}", coletas.size(), username);

        int sucesso = 0;
        int falhas = 0;
        List<String> erros = new ArrayList<>();

        for (MobileColetaRequest request : coletas) {
            try {
                registrarColeta(request, username);
                sucesso++;
            } catch (Exception e) {
                falhas++;
                erros.add("Patrimônio " + request.getNumeroPatrimonio() + ": " + e.getMessage());
                logger.error("Erro ao registrar coleta em lote", e);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", coletas.size());
        resultado.put("sucesso", sucesso);
        resultado.put("falhas", falhas);
        resultado.put("erros", erros);

        logger.info("Lote processado: {} sucesso, {} falhas", sucesso, falhas);

        return resultado;
    }

    /**
     * Busca todas as coletas do usuário
     */
    public List<MobileColetaResponse> buscarTodasColetas(String username) throws SQLException {
        logger.info("Buscando todas as coletas para usuário: {}", username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        List<Coleta> coletas = coletaDAO.buscarPorColetor(usuario.getId());
        List<MobileColetaResponse> responses = new ArrayList<>();

        for (Coleta coleta : coletas) {
            Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
            // Buscar o usuário que fez a coleta, não o usuário logado
            Usuario coletor = usuarioDAO.findById(coleta.getIdColetor());
            if (coletor == null) {
                logger.warn("Coletor não encontrado para coleta ID {}, usando usuário logado", coleta.getId());
                coletor = usuario;
            }
            responses.add(converterParaResponse(coleta, coletor, inventario));
        }

        logger.info("Encontradas {} coletas para o usuário {}", responses.size(), username);
        return responses;
    }
    
    /**
     * Busca TODAS as coletas do sistema (não apenas do usuário)
     * Usado para visualização geral de coletas
     */
    public List<MobileColetaResponse> buscarTodasColetasDoSistema() throws SQLException {
        logger.info("Buscando todas as coletas do sistema");

        List<Coleta> coletas = coletaDAO.buscarTodas();
        List<MobileColetaResponse> responses = new ArrayList<>();

        for (Coleta coleta : coletas) {
            try {
                Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
                // Buscar o usuário que fez a coleta
                Usuario coletor = usuarioDAO.findById(coleta.getIdColetor());
                if (coletor != null) {
                    responses.add(converterParaResponse(coleta, coletor, inventario));
                } else {
                    logger.warn("Coletor não encontrado para coleta ID {}, pulando", coleta.getId());
                }
            } catch (Exception e) {
                logger.error("Erro ao processar coleta ID {}: {}", coleta.getId(), e.getMessage());
            }
        }

        logger.info("Encontradas {} coletas no sistema", responses.size());
        return responses;
    }
    
    /**
     * Busca coletas pendentes de sincronização
     */
    public List<MobileColetaResponse> buscarColetasPendentes(String username) throws SQLException {
        logger.info("Buscando coletas pendentes para usuário: {}", username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        List<Coleta> coletas = coletaDAO.buscarPorColetor(usuario.getId());
        List<MobileColetaResponse> responses = new ArrayList<>();

        for (Coleta coleta : coletas) {
            if ("PENDENTE".equals(coleta.getStatusColeta())) {
                Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
                responses.add(converterParaResponse(coleta, usuario, inventario));
            }
        }

        return responses;
    }

    /**
     * Busca histórico de coletas do usuário
     */
    public List<MobileColetaResponse> buscarHistoricoColetas(String username, int limit) throws SQLException {
        logger.info("Buscando histórico de coletas para usuário: {} (limit: {})", username, limit);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        List<Coleta> coletas = coletaDAO.buscarPorColetor(usuario.getId());
        List<MobileColetaResponse> responses = new ArrayList<>();

        int count = 0;
        for (Coleta coleta : coletas) {
            if (count >= limit)
                break;

            Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
            responses.add(converterParaResponse(coleta, usuario, inventario));
            count++;
        }

        return responses;
    }

    /**
     * Busca coleta por ID
     */
    public MobileColetaResponse buscarColetaPorId(Long id, String username) throws SQLException {
        logger.info("Buscando coleta {} para usuário: {}", id, username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        Coleta coleta = coletaDAO.buscarPorId(id.intValue());
        if (coleta == null) {
            return null;
        }

        // Verificar se o usuário tem permissão para ver esta coleta
        if (coleta.getIdColetor() != usuario.getId() &&
                !"ADMIN".equals(usuario.getPerfil().name())) {
            throw new SecurityException("Sem permissão para acessar esta coleta");
        }

        Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
        return converterParaResponse(coleta, usuario, inventario);
    }

    /**
     * Atualiza uma coleta existente
     */
    public MobileColetaResponse atualizarColeta(Long id, MobileColetaRequest request, String username)
            throws SQLException {
        logger.info("Atualizando coleta {} por usuário: {}", id, username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        Coleta coleta = coletaDAO.buscarPorId(id.intValue());
        if (coleta == null) {
            throw new IllegalArgumentException("Coleta não encontrada");
        }

        // Verificar permissão
        if (coleta.getIdColetor() != usuario.getId() &&
                !"ADMIN".equals(usuario.getPerfil().name())) {
            throw new SecurityException("Sem permissão para atualizar esta coleta");
        }

        // Atualizar campos
        coleta.setObservacaoColeta(request.getObservacaoColeta());
        coleta.setEstadoEncontrado(request.getEstadoEncontrado());
        coleta.setLocalizacaoEncontrada(request.getLocalizacaoEncontrada());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            coleta.setLatitude(java.math.BigDecimal.valueOf(request.getLatitude()));
            coleta.setLongitude(java.math.BigDecimal.valueOf(request.getLongitude()));
        }

        coletaDAO.atualizarColeta(coleta);

        Inventario inventario = inventarioDAO.findById(coleta.getIdInventario());
        return converterParaResponse(coleta, usuario, inventario);
    }

    /**
     * Exclui uma coleta (apenas admin)
     */
    public boolean excluirColeta(Long id, String username) throws SQLException {
        logger.info("Excluindo coleta {} por usuário: {}", id, username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // Apenas admin pode excluir
        if (!"ADMIN".equals(usuario.getPerfil().name())) {
            throw new SecurityException("Apenas administradores podem excluir coletas");
        }

        coletaDAO.excluirColeta(id.intValue());
        return true;
    }

    // Métodos auxiliares

    private MobileColetaResponse converterParaResponse(Coleta coleta, Usuario usuario, Inventario inventario)
            throws SQLException {
        MobileColetaResponse response = new MobileColetaResponse();

        response.setId((long) coleta.getId());
        response.setIdInventario(coleta.getIdInventario());
        response.setNomeInventario(inventario != null ? inventario.getNome() : null);
        response.setDataColeta(convertToLocalDateTime(coleta.getDataColeta()));
        response.setStatusColeta(coleta.getStatusColeta());
        response.setObservacaoColeta(coleta.getObservacaoColeta());
        response.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
        response.setEstadoEncontrado(coleta.getEstadoEncontrado());
        response.setNomeColetor(usuario.getNomeCompleto());
        response.setUsuarioId(coleta.getIdColetor());  // Usar ID do coletor da coleta, não do usuário passado
        response.setSemEtiqueta(coleta.isSemEtiqueta());
        response.setSincronizado(true);

        if (coleta.isSemEtiqueta()) {
            response.setDescricaoItemSemEtiqueta(coleta.getDescricaoItemSemEtiqueta());
            response.setCategoriaItemSemEtiqueta(coleta.getCategoriaItemSemEtiqueta());
        } else {
            // Buscar dados do patrimônio
            if (coleta.getIdPatrimonio() > 0) {
                Patrimonio patrimonio = patrimonioDAO.findById(coleta.getIdPatrimonio());
                if (patrimonio != null) {
                    logger.debug("Patrimônio encontrado: ID={}, Numero={}, Descricao={}", 
                            patrimonio.getId(), patrimonio.getNumero(), patrimonio.getDescricao());
                    response.setPatrimonioId(patrimonio.getId());  // Adicionar ID do patrimônio
                    response.setNumeroPatrimonio(patrimonio.getNumero());
                    response.setDescricaoPatrimonio(patrimonio.getDescricao());
                    response.setIdSala(patrimonio.getIdSala());
                    response.setNomeSala(patrimonio.getNomeSala());
                } else {
                    logger.warn("Patrimônio não encontrado para ID: {}", coleta.getIdPatrimonio());
                }
            } else {
                logger.warn("ID do patrimônio é 0 ou negativo: {}", coleta.getIdPatrimonio());
            }
        }

        return response;
    }

    private Timestamp convertToTimestamp(String dataColeta) {
        if (dataColeta == null || dataColeta.isEmpty()) {
            return new Timestamp(System.currentTimeMillis());
        }
        
        try {
            // Tenta converter de timestamp (número)
            long timestamp = Long.parseLong(dataColeta);
            return new Timestamp(timestamp);
        } catch (NumberFormatException e) {
            // Se não for número, tenta converter de ISO 8601
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dataColeta);
                return Timestamp.valueOf(localDateTime);
            } catch (Exception ex) {
                logger.warn("Formato de data inválido: {}. Usando data atual.", dataColeta);
                return new Timestamp(System.currentTimeMillis());
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Busca descrições de patrimônios pendentes de coleta
     * Retorna apenas descrições que ainda não foram coletadas no inventário
     * 
     * @param termoBusca termo para buscar na descrição
     * @param idInventario ID do inventário (se null, busca o ativo)
     * @return Map com descrições pendentes e estatísticas
     */
    public Map<String, Object> buscarDescricoesPendentes(String termoBusca, Integer idInventario) {
        logger.info("Buscando descrições pendentes para termo: '{}', inventário: {}", termoBusca, idInventario);
        
        if (termoBusca == null || termoBusca.trim().isEmpty()) {
            throw new IllegalArgumentException("Termo de busca não pode ser vazio");
        }
        
        try {
            // Se não informou inventário, buscar o ativo
            if (idInventario == null) {
                Inventario inventarioAtivo = inventarioDAO.buscarPorStatus("EM_ANDAMENTO");
                if (inventarioAtivo != null) {
                    idInventario = inventarioAtivo.getId();
                } else {
                    throw new IllegalArgumentException("Nenhum inventário ativo encontrado");
                }
            }
            
            // Buscar patrimônios pela descrição
            List<Patrimonio> todosPatrimonios = patrimonioDAO.buscarPorDescricao(termoBusca);
            
            if (todosPatrimonios.isEmpty()) {
                return Map.of(
                    "descricoes", List.of(),
                    "total", 0,
                    "mensagem", "Nenhuma descrição pendente encontrada"
                );
            }
            
            // Filtrar apenas os não coletados
            final Integer finalIdInventario = idInventario;
            List<Patrimonio> patrimoniosPendentes = new ArrayList<>();
            for (Patrimonio p : todosPatrimonios) {
                if (!coletaDAO.coletaExiste(finalIdInventario, p.getId())) {
                    patrimoniosPendentes.add(p);
                }
            }
            
            // Agrupar por descrição e contar
            Map<String, Integer> contagemPorDescricao = new HashMap<>();
            for (Patrimonio p : patrimoniosPendentes) {
                String desc = p.getDescricao();
                if (desc != null && !desc.trim().isEmpty()) {
                    contagemPorDescricao.put(desc, contagemPorDescricao.getOrDefault(desc, 0) + 1);
                }
            }
            
            // Criar lista de descrições com contagem
            List<Map<String, Object>> descricoes = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : contagemPorDescricao.entrySet()) {
                descricoes.add(Map.of(
                    "descricao", entry.getKey(),
                    "quantidadePendente", entry.getValue()
                ));
            }
            
            // Montar resposta
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("descricoes", descricoes);
            resultado.put("total", descricoes.size());
            resultado.put("totalPatrimoniosPendentes", patrimoniosPendentes.size());
            resultado.put("mensagem", String.format(
                "Encontradas %d descrição(ões) com %d patrimônio(s) pendente(s)",
                descricoes.size(), patrimoniosPendentes.size()
            ));
            
            logger.info("Retornando {} descrições pendentes com {} patrimônios total", 
                descricoes.size(), patrimoniosPendentes.size());
            
            return resultado;
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições pendentes: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar descrições pendentes: " + e.getMessage(), e);
        }
    }
}
