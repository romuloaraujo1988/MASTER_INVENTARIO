package com.inventario.sihcp.mobile.server.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.sihcp.dao.ColetaDAO;
import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.ParticipanteInventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.dao.UsuarioDAO;
import com.inventario.sihcp.event.DashboardEvent;
import com.inventario.sihcp.event.DashboardEventBus;
import com.inventario.sihcp.event.DashboardEventType;
import com.inventario.sihcp.mobile.server.dto.MobileColetaRequest;
import com.inventario.sihcp.mobile.server.dto.MobileColetaResponse;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Usuario;

/**
 * Serviço para operações de coleta mobile
 * 
 * CONFIGURAÇÃO:
 * - Transações com isolamento READ_COMMITTED
 * - Rollback automático em exceções
 * - Timeout de 30s (coleta individual ~10s + margem)
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@Service
@Transactional(
    isolation = Isolation.READ_COMMITTED,
    timeout = 30,
    rollbackFor = Exception.class
)
public class MobileColetaService {

    private static final Logger logger = LoggerFactory.getLogger(MobileColetaService.class);

    private final ColetaDAO coletaDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;
    private final UsuarioDAO usuarioDAO;
    private final ParticipanteInventarioDAO participanteInventarioDAO;

    public MobileColetaService() {
        this.coletaDAO = new ColetaDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioDAO = new InventarioDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.participanteInventarioDAO = new ParticipanteInventarioDAO();
    }

    /**
     * Registra uma nova coleta
     * 
     * TRANSAÇÃO: Propagation.REQUIRED garante que a coleta seja salva
     * em uma transação. Se falhar, faz rollback automático.
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public MobileColetaResponse registrarColeta(MobileColetaRequest request, String username) throws SQLException {
        logger.debug("Registrando coleta para patrimônio: {} por usuário: {}", 
                request.getNumeroPatrimonio(), username);

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
            logger.debug("Buscando inventário ativo automaticamente...");
            try {
                inventario = inventarioDAO.buscarInventarioAtivo();
                if (inventario != null) {
                    logger.debug("Inventário ativo encontrado: ID={}", inventario.getId());
                } else {
                    logger.warn("Nenhum inventário com status 'EM_ANDAMENTO' encontrado");
                }
            } catch (SQLException e) {
                logger.error("Erro ao buscar inventário ativo: {}", e.getMessage(), e);
            }
        }
        
        if (inventario == null) {
            throw new IllegalArgumentException("Nenhum inventário ativo encontrado. Por favor, inicie um inventário no sistema com status 'EM_ANDAMENTO'.");
        }
        
        // VALIDAÇÃO CRÍTICA 27/12/2025: Verificar se inventário está EM_ANDAMENTO
        if (!inventario.isEmAndamento()) {
            String statusAtual = inventario.getStatusInventario();
            String mensagem;
            
            if (Inventario.STATUS_CONCLUIDO.equals(statusAtual)) {
                mensagem = "INVENTÁRIO FINALIZADO: O inventário '" + inventario.getNome() + 
                          "' já foi encerrado e não aceita mais coletas.";
            } else if (Inventario.STATUS_CANCELADO.equals(statusAtual)) {
                mensagem = "INVENTÁRIO CANCELADO: O inventário '" + inventario.getNome() + 
                          "' foi cancelado e não aceita coletas.";
            } else if (Inventario.STATUS_PLANEJADO.equals(statusAtual)) {
                mensagem = "INVENTÁRIO NÃO INICIADO: O inventário '" + inventario.getNome() + 
                          "' ainda está em planejamento. Aguarde a abertura.";
            } else {
                mensagem = "INVENTÁRIO INDISPONÍVEL: Status atual '" + statusAtual + 
                          "' não permite coletas.";
            }
            
            logger.warn("🚫 Tentativa de coleta em inventário não ativo: ID={}, Nome='{}', Status={}", 
                    inventario.getId(), inventario.getNome(), statusAtual);
            throw new IllegalArgumentException(mensagem);
        }

        // Buscar ID do participante
        Integer idParticipante = participanteInventarioDAO.buscarIdParticipantePorUsuario(
                inventario.getId(), usuario.getId());

        if (idParticipante == null) {
            throw new IllegalArgumentException("Usuário não é participante deste inventário");
        }

        // Criar objeto Coleta
        Coleta coleta = new Coleta();
        // BUGFIX F2 (07/05/2026): Usar o ID do inventário RESOLVIDO (pode ser o do request OU
        // o ativo quando o request vem com null/0), não o bruto do request. Antes gravávamos
        // `request.getIdInventario()` o que, quando o cliente enviava null e o fallback
        // `buscarInventarioAtivo()` era acionado, resultava em INSERT com `ID_INVENTARIO = NULL`
        // → rollback da transação → coleta perdida silenciosamente.
        coleta.setIdInventario(inventario.getId());
        coleta.setIdColetor(usuario.getId());
        coleta.setIdParticipanteInventario(idParticipante);
        coleta.setDataColeta(convertToTimestamp(request.getDataColeta()));
        coleta.setStatusColeta("COLETADO");
        coleta.setObservacaoColeta(request.getObservacaoColeta());
        coleta.setLocalizacaoEncontrada(request.getLocalizacaoEncontrada());
        coleta.setEstadoEncontrado(request.getEstadoEncontrado());
        coleta.setDivergencia(false);
        Boolean semEtiquetaValue = request.getSemEtiqueta();
        coleta.setSemEtiqueta(semEtiquetaValue != null && semEtiquetaValue);

        // Validação customizada: verificar se tem número de patrimônio OU é sem etiqueta
        boolean temNumeroPatrimonio = request.getNumeroPatrimonio() != null && !request.getNumeroPatrimonio().trim().isEmpty();
        boolean isSemEtiqueta = coleta.isSemEtiqueta();
        
        // Se não tem número de patrimônio e não é sem etiqueta, verificar se tem descrição
        // (pode ser coleta por descrição que foi salva offline sem o flag semEtiqueta)
        if (!temNumeroPatrimonio && !isSemEtiqueta) {
            // Verificar se tem descrição - pode ser coleta por descrição
            if (request.getDescricaoItemSemEtiqueta() != null && !request.getDescricaoItemSemEtiqueta().trim().isEmpty()) {
                logger.debug("Coleta sem número de patrimônio mas com descrição - tratando como sem etiqueta");
                coleta.setSemEtiqueta(true);
                isSemEtiqueta = true;
            } else {
                throw new IllegalArgumentException("Número do patrimônio é obrigatório para coletas com etiqueta");
            }
        }
        
        // Se não for item sem etiqueta, buscar patrimônio
        if (!isSemEtiqueta) {
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(request.getNumeroPatrimonio());
            if (patrimonio == null) {
                throw new IllegalArgumentException("Patrimônio não encontrado: " + request.getNumeroPatrimonio());
            }
            coleta.setIdPatrimonio(patrimonio.getId());
            coleta.setLocalizacaoAtual(patrimonio.getNomeSala());
            
            // VERIFICAÇÃO DE DUPLICATA: Verificar se já existe coleta para este patrimônio neste inventário
            Coleta coletaExistente = verificarColetaDuplicada(inventario.getId(), patrimonio.getId());
            if (coletaExistente != null) {
                logger.warn("⚠️ COLETA DUPLICADA DETECTADA: Patrimônio {} já foi coletado no inventário {} por {} em {}",
                        request.getNumeroPatrimonio(), 
                        inventario.getId(),
                        coletaExistente.getNomeColetor(),
                        coletaExistente.getDataColeta());
                
                // Retornar resposta de duplicata ao invés de lançar exceção
                return criarRespostaDuplicada(coletaExistente, request);
            }
        } else {
            // Item sem etiqueta - validar que tem descrição
            if (request.getDescricaoItemSemEtiqueta() == null || request.getDescricaoItemSemEtiqueta().trim().isEmpty()) {
                throw new IllegalArgumentException("Descrição é obrigatória para coletas sem etiqueta");
            }
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
        
        // Métricas de tempo (Analytics)
        coleta.setTempoColetaSegundos(request.getTempoColetaSegundos());
        coleta.setTempoScanSegundos(request.getTempoScanSegundos());
        coleta.setTempoPreenchimentoSegundos(request.getTempoPreenchimentoSegundos());
        coleta.setMetodoColeta(request.getMetodoColeta());
        coleta.setTipoScan(request.getTipoScan());
        
        // Calcular dependências de tempo (hora, dia da semana) com base na data da coleta
        if (coleta.getDataColeta() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(coleta.getDataColeta().getTime());
            coleta.setHoraColeta(cal.get(java.util.Calendar.HOUR_OF_DAY));
            coleta.setDiaSemana(cal.get(java.util.Calendar.DAY_OF_WEEK));
            
            // Definir período
            int hora = coleta.getHoraColeta();
            if (hora >= 6 && hora < 12) {
                coleta.setPeriodoColeta("MANHA");
            } else if (hora >= 12 && hora < 18) {
                coleta.setPeriodoColeta("TARDE");
            } else {
                coleta.setPeriodoColeta("NOITE");
            }
        }

        // Inserir no banco
        coletaDAO.inserirColeta(coleta);

        // Sons são reproduzidos apenas no dispositivo Android, não no servidor

        logger.debug("Coleta registrada com sucesso. ID: {}", coleta.getId());
        
        // Publicar evento para atualização da dashboard (não afeta a transação se falhar)
        publicarEventoColetaSincronizada(coleta, inventario, usuario);

        // Retornar resposta
        return converterParaResponse(coleta, usuario, inventario);
    }

    /**
     * Registra múltiplas coletas em lote
     * Trata duplicatas separadamente sem interromper o processamento
     *
     * BUGFIX F7 (07/05/2026): resposta agora inclui listas estruturadas permitindo que o
     * cliente identifique, por índice da lista original enviada, qual coleta foi de fato
     * persistida e qual falhou. Antes o cliente dependia de regex na string "Patrimônio X:"
     * para identificar falhas, o que era frágil e podia marcar como sincronizadas coletas
     * que na verdade falharam (perda silenciosa).
     *
     * Estrutura da resposta:
     * <pre>
     * {
     *   "total": 10,
     *   "sucesso": 7,
     *   "falhas": 2,
     *   "duplicadas": 1,
     *   "erros": ["Patrimônio 123: motivo", ...],              // retrocompatibilidade
     *   "coletasDuplicadas": ["12345", "67890"],                // retrocompatibilidade
     *   "resultados": [                                         // NOVO - array por índice
     *     {
     *       "indice": 0,                                        // posição na lista enviada
     *       "numeroPatrimonio": "12345",
     *       "status": "SUCESSO" | "DUPLICADA" | "FALHA",
     *       "coletaId": 987,                                    // ID atribuído pelo servidor
     *       "mensagem": "Coleta registrada com sucesso"
     *     }
     *   ]
     * }
     * </pre>
     */
    public Map<String, Object> registrarColetasEmLote(List<MobileColetaRequest> coletas, String username) {
        logger.debug("Registrando {} coletas em lote para usuário: {}", coletas.size(), username);

        int sucesso = 0;
        int falhas = 0;
        int duplicadas = 0;
        List<String> erros = new ArrayList<>();
        List<String> coletasDuplicadas = new ArrayList<>();
        // BUGFIX F7: resultado estruturado por índice
        List<Map<String, Object>> resultados = new ArrayList<>();

        for (int i = 0; i < coletas.size(); i++) {
            MobileColetaRequest request = coletas.get(i);
            Map<String, Object> itemResultado = new HashMap<>();
            itemResultado.put("indice", i);
            itemResultado.put("numeroPatrimonio", request.getNumeroPatrimonio());

            try {
                MobileColetaResponse response = registrarColeta(request, username);

                // Verificar se a resposta indica duplicata
                if (response.getDuplicada() != null && response.getDuplicada()) {
                    duplicadas++;
                    coletasDuplicadas.add(request.getNumeroPatrimonio());
                    logger.debug("Coleta duplicada detectada no lote: {}", request.getNumeroPatrimonio());

                    itemResultado.put("status", "DUPLICADA");
                    itemResultado.put("coletaId", response.getColetaOriginalId());
                    itemResultado.put("mensagem", response.getMensagemDuplicada());
                } else {
                    sucesso++;

                    itemResultado.put("status", "SUCESSO");
                    itemResultado.put("coletaId", response.getId());
                    itemResultado.put("mensagem", "Coleta registrada com sucesso");
                }
            } catch (SQLException | IllegalArgumentException | SecurityException e) {
                falhas++;
                erros.add("Patrimônio " + request.getNumeroPatrimonio() + ": " + e.getMessage());
                logger.error("Erro ao registrar coleta em lote: {}", e.getMessage());

                itemResultado.put("status", "FALHA");
                itemResultado.put("coletaId", null);
                itemResultado.put("mensagem", e.getMessage());
            }

            resultados.add(itemResultado);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", coletas.size());
        resultado.put("sucesso", sucesso);
        resultado.put("falhas", falhas);
        resultado.put("duplicadas", duplicadas);
        resultado.put("erros", erros);
        resultado.put("coletasDuplicadas", coletasDuplicadas);
        // BUGFIX F7: novo campo estruturado por índice
        resultado.put("resultados", resultados);

        logger.debug("Lote processado: {} sucesso, {} falhas, {} duplicadas", sucesso, falhas, duplicadas);
        
        // Publicar evento de batch para atualização da dashboard
        if (sucesso > 0) {
            publicarEventoBatchSincronizado(sucesso, coletas.size());
        }

        return resultado;
    }
    
    /**
     * Publica evento de coleta sincronizada para a dashboard.
     * Este método não lança exceções para não afetar a transação principal.
     */
    private void publicarEventoColetaSincronizada(Coleta coleta, Inventario inventario, Usuario usuario) {
        try {
            DashboardEvent event = DashboardEvent.builder(DashboardEventType.COLETA_SINCRONIZADA)
                .source("MobileColetaService")
                .addMetadata("coletaId", coleta.getId())
                .addMetadata("patrimonioId", coleta.getIdPatrimonio())
                .addMetadata("inventarioId", inventario != null ? inventario.getId() : null)
                .addMetadata("coletorNome", usuario != null ? usuario.getNomeCompleto() : "Desconhecido")
                .addMetadata("numeroPatrimonio", coleta.getNumeroPatrimonio())
                .addAffectedEntityId(coleta.getIdPatrimonio())
                .build();
            
            DashboardEventBus.getInstance().publish(event);
            logger.debug("Evento COLETA_SINCRONIZADA publicado para coleta ID: {}", coleta.getId());
        } catch (Exception e) {
            // Log do erro mas não propaga - não deve afetar a operação principal
            logger.warn("Falha ao publicar evento de coleta sincronizada: {}", e.getMessage());
        }
    }
    
    /**
     * Publica evento de batch sincronizado para a dashboard.
     * Este método não lança exceções para não afetar a transação principal.
     */
    private void publicarEventoBatchSincronizado(int quantidadeSucesso, int totalLote) {
        try {
            DashboardEvent event = DashboardEvent.builder(DashboardEventType.COLETA_SINCRONIZADA)
                .source("MobileColetaService")
                .addMetadata("batchSize", quantidadeSucesso)
                .addMetadata("totalLote", totalLote)
                .addMetadata("isBatch", true)
                .build();
            
            DashboardEventBus.getInstance().publish(event);
            logger.debug("Evento COLETA_SINCRONIZADA (batch) publicado: {} coletas", quantidadeSucesso);
        } catch (Exception e) {
            // Log do erro mas não propaga - não deve afetar a operação principal
            logger.warn("Falha ao publicar evento de batch sincronizado: {}", e.getMessage());
        }
    }

    /**
     * Busca todas as coletas do usuário
     * Se username for null, retorna todas as coletas do sistema
     */
    public List<MobileColetaResponse> buscarTodasColetas(String username) throws SQLException {
        // Se username for null, buscar todas as coletas do sistema
        if (username == null || username.trim().isEmpty()) {
            logger.debug("Username null/vazio - buscando TODAS as coletas do sistema");
            return buscarTodasColetasDoSistema();
        }
        
        logger.debug("Buscando todas as coletas para usuário: {}", username);

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

        logger.debug("Encontradas {} coletas para o usuário {}", responses.size(), username);
        return responses;
    }
    
    /**
     * Conta o total de coletas no banco de dados
     *
     * @return número total de coletas
     */
    public int contarTotalColetas() throws SQLException {
        return contarTotalColetas(null);
    }

    /**
     * Conta total de coletas por inventário.
     * Se idInventario for null, usa o inventário ativo automaticamente.
     *
     * @param idInventario ID do inventário (null = inventário ativo)
     * @return total de coletas
     */
    public int contarTotalColetas(Integer idInventario) throws SQLException {
        Integer idFiltro = resolverInventario(idInventario);
        return coletaDAO.contarTotalColetas(idFiltro);
    }
    
    /**
     * Busca TODAS as coletas do sistema (não apenas do usuário)
     * 
     * @deprecated Use buscarColetasComPaginacaoReal() para evitar vazamento de memória
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public List<MobileColetaResponse> buscarTodasColetasDoSistema() throws SQLException {
        logger.warn("⚠️ MÉTODO DEPRECADO: buscarTodasColetasDoSistema() - Use buscarColetasComPaginacaoReal()");
        // Buscar todas as coletas
        int total = contarTotalColetas();
        Map<String, Object> resultado = buscarColetasComPaginacaoReal(0, Math.max(total, 1000));
        return (List<MobileColetaResponse>) resultado.get("content");
    }
    
    /**
     * MÉTODO OTIMIZADO: Busca coletas com paginação REAL no banco de dados
     * 
     * PROBLEMA RESOLVIDO: Vazamento de memória (200MB → 4GB)
     * ANTES: Carregava TODAS as coletas em memória
     * DEPOIS: Paginação no SQL com LIMIT/OFFSET
     * 
     * @param page número da página (0-based)
     * @param size tamanho da página (máximo 100)
     * @return Map com content, totalElements, totalPages
     */
    public Map<String, Object> buscarColetasComPaginacaoReal(int page, int size) throws SQLException {
        return buscarColetasComPaginacaoReal(page, size, null);
    }

    /**
     * MÉTODO OTIMIZADO: Busca coletas com paginação REAL no banco de dados, filtrado por inventário
     * Se idInventario for null, usa o inventário ativo automaticamente.
     *
     * @param page         número da página (0-based)
     * @param size         tamanho da página
     * @param idInventario ID do inventário (null = inventário ativo)
     * @return Map com content, totalElements, totalPages
     */
    public Map<String, Object> buscarColetasComPaginacaoReal(int page, int size, Integer idInventario) throws SQLException {
        long startTime = System.currentTimeMillis();

        if (size > 5000) {
            logger.warn("⚠️ Tamanho de página muito grande: {}. Considere usar paginação.", size);
        }

        // Resolver inventário ativo se não informado
        Integer idFiltro = resolverInventario(idInventario);

        logger.debug("Buscando coletas com paginação REAL (page={}, size={}, inventarioId={})", page, size, idFiltro);

        // 1. Contar total (query leve) — filtrado por inventário
        int totalElements = coletaDAO.contarTotalColetas(idFiltro);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 2. Buscar apenas a página solicitada (LIMIT/OFFSET no SQL) — filtrado por inventário
        List<Coleta> coletas = coletaDAO.buscarColetasComPaginacao(page, size, idFiltro);

        // 3. Converter para response (os dados já vêm com JOINs, sem N+1)
        List<MobileColetaResponse> responses = new ArrayList<>();
        for (Coleta coleta : coletas) {
            try {
                MobileColetaResponse response = converterColetaParaResponseSimples(coleta);
                responses.add(response);
            } catch (Exception e) {
                logger.error("Erro ao processar coleta ID {}: {}", coleta.getId(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("✓ {} coletas carregadas em {}ms (página {}/{}, total: {}, inventário: {})",
                responses.size(), duration, page + 1, totalPages, totalElements,
                idFiltro != null ? idFiltro : "todos");

        // 4. Montar resposta paginada
        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("page", page);
        result.put("size", size);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("first", page == 0);
        result.put("last", page >= totalPages - 1);

        return result;
    }

    /**
     * Resolve o ID do inventário: usa o informado ou busca o ativo automaticamente.
     *
     * @param idInventario ID informado (pode ser null)
     * @return ID do inventário a usar, ou null se não houver inventário ativo
     */
    private Integer resolverInventario(Integer idInventario) {
        if (idInventario != null) {
            return idInventario;
        }
        try {
            Inventario ativo = inventarioDAO.buscarInventarioAtivo();
            if (ativo != null) {
                logger.debug("Usando inventário ativo: ID={}", ativo.getId());
                return ativo.getId();
            }
        } catch (Exception e) {
            logger.warn("Não foi possível obter inventário ativo: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * MÉTODO OTIMIZADO: Busca coletas do usuário com paginação REAL
     */
    public Map<String, Object> buscarColetasUsuarioComPaginacaoReal(String username, int page, int size) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        // Limitar tamanho máximo
        if (size > 100) size = 100;
        
        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado: " + username);
        }
        
        // Contar total do usuário
        int totalElements = coletaDAO.contarColetasPorUsuario(usuario.getId());
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Buscar página com LIMIT/OFFSET
        List<Coleta> coletas = coletaDAO.buscarColetasPorUsuarioComPaginacao(usuario.getId(), page, size);
        
        // Converter
        List<MobileColetaResponse> responses = new ArrayList<>();
        for (Coleta coleta : coletas) {
            try {
                MobileColetaResponse response = converterColetaParaResponseSimples(coleta);
                responses.add(response);
            } catch (Exception e) {
                logger.error("Erro ao processar coleta ID {}: {}", coleta.getId(), e.getMessage());
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        logger.info("✓ {} coletas do usuário {} em {}ms", responses.size(), username, duration);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", responses);
        result.put("page", page);
        result.put("size", size);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("first", page == 0);
        result.put("last", page >= totalPages - 1);
        
        return result;
    }
    
    /**
     * Converte Coleta para Response usando dados já carregados pelo JOIN
     * Evita queries adicionais (N+1 problem)
     * 
     * CORREÇÃO 01/12/2025: Adicionado nomeSala para exibição correta no app Android
     */
    private MobileColetaResponse converterColetaParaResponseSimples(Coleta coleta) {
        MobileColetaResponse response = new MobileColetaResponse();
        
        response.setId((long) coleta.getId());
        response.setIdInventario(coleta.getIdInventario());
        response.setPatrimonioId(coleta.getIdPatrimonio());
        response.setUsuarioId(coleta.getIdColetor());
        response.setStatusColeta(coleta.getStatusColeta());
        response.setObservacaoColeta(coleta.getObservacaoColeta());
        response.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
        response.setEstadoEncontrado(coleta.getEstadoEncontrado());
        response.setSemEtiqueta(coleta.isSemEtiqueta());
        response.setDescricaoItemSemEtiqueta(coleta.getDescricaoItemSemEtiqueta());
        response.setCategoriaItemSemEtiqueta(coleta.getCategoriaItemSemEtiqueta());
        
        // Dados já carregados pelo JOIN (sem queries adicionais)
        response.setNumeroPatrimonio(coleta.getNumeroPatrimonio());
        response.setDescricaoPatrimonio(coleta.getDescricaoPatrimonio());
        response.setNomeColetor(coleta.getNomeColetor());
        response.setNomeInventario(coleta.getDescricaoInventario());
        
        // CORREÇÃO 02/12/2025: nomeSala no response deve ser onde o item FOI ENCONTRADO
        // Prioridade: localizacaoEncontrada > nomeSala (original) > localizacaoAtual
        // Isso garante que o filtro por sala no app Android funcione corretamente
        String salaParaExibir = coleta.getLocalizacaoEncontrada();
        if (salaParaExibir == null || salaParaExibir.isEmpty()) {
            // Fallback: usar nomeSala (sala original do patrimônio)
            salaParaExibir = coleta.getNomeSala();
        }
        if (salaParaExibir == null || salaParaExibir.isEmpty()) {
            // Último fallback: usar localizacaoAtual
            salaParaExibir = coleta.getLocalizacaoAtual();
        }
        response.setNomeSala(salaParaExibir);
        
        // Formatar data
        if (coleta.getDataColeta() != null) {
            response.setDataColeta(coleta.getDataColetaFormatada());
        }
        
        return response;
    }
    
    /**
     * Busca coletas pendentes de sincronização.
     * OTIMIZADO: usa buscarPendentesPorColetor() com filtro e JOINs no banco (1 query).
     */
    public List<MobileColetaResponse> buscarColetasPendentes(String username) throws SQLException {
        logger.debug("Buscando coletas pendentes para usuário: {}", username);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // 1 query com filtro STATUS_COLETA = 'PENDENTE' e JOINs — sem N+1
        List<Coleta> coletas = coletaDAO.buscarPendentesPorColetor(usuario.getId());
        List<MobileColetaResponse> responses = new ArrayList<>();

        for (Coleta coleta : coletas) {
            responses.add(converterColetaParaResponseSimples(coleta));
        }

        return responses;
    }

    /**
     * Busca histórico de coletas do usuário.
     * OTIMIZADO: usa buscarHistoricoPorColetor() com LIMIT no banco (1 query, sem N+1).
     */
    public List<MobileColetaResponse> buscarHistoricoColetas(String username, int limit) throws SQLException {
        logger.debug("Buscando histórico de coletas para usuário: {} (limit: {})", username, limit);

        Usuario usuario = usuarioDAO.buscarPorLogin(username);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        // 1 query com LIMIT no banco e JOINs — sem carregar tudo em memória, sem N+1
        List<Coleta> coletas = coletaDAO.buscarHistoricoPorColetor(usuario.getId(), limit);
        List<MobileColetaResponse> responses = new ArrayList<>();

        for (Coleta coleta : coletas) {
            responses.add(converterColetaParaResponseSimples(coleta));
        }

        return responses;
    }

    /**
     * Busca coleta por ID
     */
    public MobileColetaResponse buscarColetaPorId(Long id, String username) throws SQLException {
        logger.debug("Buscando coleta {} para usuário: {}", id, username);

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
        logger.debug("Atualizando coleta {} por usuário: {}", id, username);

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
        logger.debug("Excluindo coleta {} por usuário: {}", id, username);

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
        response.setDataColeta(formatDataColeta(coleta.getDataColeta()));  // Formata como String ISO 8601
        response.setStatusColeta(coleta.getStatusColeta());
        response.setObservacaoColeta(coleta.getObservacaoColeta());
        
        String localizacaoEncontrada = coleta.getLocalizacaoEncontrada();
        logger.debug("converterParaResponse: Coleta ID={}, idPatrimonio={}", 
                coleta.getId(), coleta.getIdPatrimonio());
        
        response.setLocalizacaoEncontrada(localizacaoEncontrada);
        response.setEstadoEncontrado(coleta.getEstadoEncontrado());
        response.setNomeColetor(usuario.getNomeCompleto());
        response.setUsuarioId(coleta.getIdColetor());  // Usar ID do coletor da coleta, não do usuário passado
        response.setSemEtiqueta(coleta.isSemEtiqueta());
        response.setSincronizado(true);

        if (coleta.isSemEtiqueta()) {
            response.setDescricaoItemSemEtiqueta(coleta.getDescricaoItemSemEtiqueta());
            response.setCategoriaItemSemEtiqueta(coleta.getCategoriaItemSemEtiqueta());
            logger.debug("Coleta {} é SEM ETIQUETA", coleta.getId());
            
            // Para itens sem etiqueta, usar localizacaoEncontrada como nomeSala
            response.setNomeSala(localizacaoEncontrada);
        } else {
            // Buscar dados do patrimônio - SEMPRE buscar se não for sem etiqueta
            if (coleta.getIdPatrimonio() > 0) {
                try {
                    Patrimonio patrimonio = patrimonioDAO.findById(coleta.getIdPatrimonio());
                    if (patrimonio != null) {
                        response.setPatrimonioId(patrimonio.getId());
                        response.setNumeroPatrimonio(patrimonio.getNumero());
                        response.setDescricaoPatrimonio(patrimonio.getDescricao());
                        response.setIdSala(patrimonio.getIdSala());
                        
                        // CORREÇÃO 02/12/2025: nomeSala deve ser onde o item FOI ENCONTRADO
                        // Prioridade: localizacaoEncontrada > nomeSala original do patrimônio
                        String salaParaExibir = localizacaoEncontrada;
                        if (salaParaExibir == null || salaParaExibir.isEmpty()) {
                            salaParaExibir = patrimonio.getNomeSala();
                        }
                        response.setNomeSala(salaParaExibir);
                    } else {
                        logger.error("Patrimônio não encontrado para ID: {} (Coleta ID: {})", 
                                coleta.getIdPatrimonio(), coleta.getId());
                        // Fallback: usar localizacaoEncontrada
                        response.setNomeSala(localizacaoEncontrada);
                    }
                } catch (SQLException | RuntimeException e) {
                    logger.error("Erro ao buscar patrimônio ID {}: {}", coleta.getIdPatrimonio(), e.getMessage());
                    // Fallback: usar localizacaoEncontrada
                    response.setNomeSala(localizacaoEncontrada);
                }
            } else {
                logger.warn("ID do patrimônio inválido: {} (Coleta ID: {})", 
                        coleta.getIdPatrimonio(), coleta.getId());
                // Fallback: usar localizacaoEncontrada
                response.setNomeSala(localizacaoEncontrada);
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
    
    /**
     * Converte Timestamp para String formatada (ISO 8601)
     * Compatível com app Android que espera String
     */
    private String formatDataColeta(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        LocalDateTime localDateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
        logger.debug("Buscando descrições pendentes para termo: '{}', inventário: {}", termoBusca, idInventario);
        
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
            
            logger.debug("Retornando {} descrições pendentes com {} patrimônios total", 
                descricoes.size(), patrimoniosPendentes.size());
            
            return resultado;
            
        } catch (SQLException | IllegalArgumentException e) {
            logger.error("Erro ao buscar descrições pendentes: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar descrições pendentes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca coletas modificadas desde o último timestamp (sincronização incremental)
     * 
     * @param lastSyncTimestamp timestamp da última sincronização em milissegundos
     * @param inventarioId ID do inventário (opcional)
     * @param limit limite de registros
     * @param offset offset para paginação
     * @return resposta com coletas incrementais
     */
    public com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse> buscarColetasIncrementais(
            Long lastSyncTimestamp, Integer inventarioId, Integer limit, Integer offset) {

        logger.debug("Buscando coletas incrementais: lastSync={}, inventario={}, limit={}, offset={}",
                lastSyncTimestamp, inventarioId, limit, offset);

        try {
            // Converter timestamp para Timestamp SQL
            Timestamp dataUltimaSync = lastSyncTimestamp != null && lastSyncTimestamp > 0
                    ? new Timestamp(lastSyncTimestamp)
                    : new Timestamp(0); // Se não informado, busca todas

            int limitEfetivo = limit != null ? limit : 100;
            int offsetEfetivo = offset != null ? offset : 0;

            // COUNT(*) separado — sem carregar todos os registros em memória
            int totalCount = coletaDAO.contarModificadasDesde(dataUltimaSync, inventarioId);

            // Paginação real no banco com LIMIT/OFFSET e JOINs (sem N+1)
            List<Coleta> coletas = coletaDAO.buscarModificadasDesde(
                    dataUltimaSync, inventarioId, limitEfetivo, offsetEfetivo);

            // Converter usando dados já carregados pelo JOIN — sem queries adicionais
            List<MobileColetaResponse> coletasResponse = new ArrayList<>();
            for (Coleta coleta : coletas) {
                try {
                    coletasResponse.add(converterColetaParaResponseSimples(coleta));
                } catch (Exception e) {
                    logger.warn("Erro ao converter coleta {}: {}", coleta.getId(), e.getMessage());
                }
            }

            // Criar resposta incremental
            com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse> response =
                    new com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<>();

            response.setData(coletasResponse);
            response.setTotalCount(totalCount);
            response.setReturnedCount(coletasResponse.size());
            response.setServerTimestamp(System.currentTimeMillis());
            response.setHasMore((offsetEfetivo + coletasResponse.size()) < totalCount);

            String mensagem = String.format("Sincronização incremental: %d/%d coletas",
                    coletasResponse.size(), totalCount);
            response.setMessage(mensagem);

            logger.debug("Retornando {} coletas de {} total (hasMore: {})",
                    coletasResponse.size(), totalCount, response.getHasMore());

            return response;

        } catch (SQLException | RuntimeException e) {
            logger.error("Erro ao buscar coletas incrementais", e);

            // Retornar resposta vazia em caso de erro
            com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<MobileColetaResponse> response =
                    new com.inventario.sihcp.mobile.server.dto.IncrementalSyncResponse<>();
            response.setData(new ArrayList<>());
            response.setTotalCount(0);
            response.setReturnedCount(0);
            response.setServerTimestamp(System.currentTimeMillis());
            response.setHasMore(false);
            response.setMessage("Erro ao buscar coletas: " + e.getMessage());

            return response;
        }
    }
    
    // ==================== MÉTODOS OTIMIZADOS DE PERFORMANCE ====================
    
    /**
     * Busca todas as coletas do sistema com otimização de performance
     * Usa cache e batch queries para reduzir consultas ao banco em 95%
     * 
     * Performance:
     * - Antes: 40 coletas = 121 queries (~3-5s)
     * - Depois: 40 coletas = 5-7 queries (~200-500ms)
     * 
     * @return lista de coletas otimizada
     * @throws SQLException em caso de erro no banco
     */
    public List<MobileColetaResponse> buscarTodasColetasDoSistemaOtimizado() throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.debug("Buscando todas as coletas (OTIMIZADO)");

        // 1. Buscar todas as coletas (1 query)
        List<Coleta> coletas = coletaDAO.buscarTodas();
        
        if (coletas.isEmpty()) {
            logger.debug("Nenhuma coleta encontrada");
            return new ArrayList<>();
        }

        // 2. Extrair IDs únicos para busca em batch
        java.util.Set<Integer> inventarioIds = new java.util.HashSet<>();
        java.util.Set<Integer> usuarioIds = new java.util.HashSet<>();
        java.util.Set<Integer> patrimonioIds = new java.util.HashSet<>();
        
        for (Coleta coleta : coletas) {
            inventarioIds.add(coleta.getIdInventario());
            usuarioIds.add(coleta.getIdColetor());
            if (coleta.getIdPatrimonio() > 0) {
                patrimonioIds.add(coleta.getIdPatrimonio());
            }
        }

        logger.debug("IDs únicos: {} inventários, {} usuários, {} patrimônios", 
                inventarioIds.size(), usuarioIds.size(), patrimonioIds.size());

        // 3. Buscar todos os dados relacionados em batch (3 queries)
        java.util.Map<Integer, Inventario> inventariosCache = buscarInventariosEmBatch(inventarioIds);
        java.util.Map<Integer, Usuario> usuariosCache = buscarUsuariosEmBatch(usuarioIds);
        java.util.Map<Integer, Patrimonio> patrimoniosCache = buscarPatrimoniosEmBatch(patrimonioIds);

        // 4. Converter coletas usando cache (0 queries adicionais)
        List<MobileColetaResponse> responses = new ArrayList<>();
        
        for (Coleta coleta : coletas) {
            try {
                Inventario inventario = inventariosCache.get(coleta.getIdInventario());
                Usuario coletor = usuariosCache.get(coleta.getIdColetor());
                Patrimonio patrimonio = patrimoniosCache.get(coleta.getIdPatrimonio());
                
                // Se coletor não encontrado, criar um usuário temporário para não perder a coleta
                if (coletor == null) {
                    logger.warn("Coletor ID {} não encontrado para coleta ID {}, usando nome padrão", 
                            coleta.getIdColetor(), coleta.getId());
                    coletor = new Usuario();
                    coletor.setId(coleta.getIdColetor());
                    coletor.setNomeCompleto("Usuário ID " + coleta.getIdColetor() + " (não encontrado)");
                }
                
                MobileColetaResponse response = converterParaResponseComCache(
                    coleta, coletor, inventario, patrimonio
                );
                responses.add(response);
            } catch (Exception e) {
                logger.error("Erro ao processar coleta ID {}: {}", coleta.getId(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.debug("{} coletas processadas em {}ms", responses.size(), duration);
        
        return responses;
    }
    
    /**
     * Busca múltiplos inventários em uma única query
     */
    private java.util.Map<Integer, Inventario> buscarInventariosEmBatch(java.util.Set<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return new java.util.HashMap<>();
        
        logger.debug("📦 Buscando {} inventários em batch", ids.size());
        java.util.Map<Integer, Inventario> cache = new java.util.HashMap<>();
        
        // Buscar todos de uma vez
        List<Inventario> inventarios = inventarioDAO.buscarPorIds(new ArrayList<>(ids));
        
        for (Inventario inv : inventarios) {
            cache.put(inv.getId(), inv);
        }
        
        logger.debug("✓ {} inventários carregados no cache", cache.size());
        return cache;
    }

    /**
     * Busca múltiplos usuários em uma única query
     */
    private java.util.Map<Integer, Usuario> buscarUsuariosEmBatch(java.util.Set<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return new java.util.HashMap<>();
        
        logger.debug("👥 Buscando {} usuários em batch", ids.size());
        java.util.Map<Integer, Usuario> cache = new java.util.HashMap<>();
        
        // Buscar todos de uma vez
        List<Usuario> usuarios = usuarioDAO.buscarPorIds(new ArrayList<>(ids));
        
        for (Usuario user : usuarios) {
            cache.put(user.getId(), user);
        }
        
        logger.debug("✓ {} usuários carregados no cache", cache.size());
        return cache;
    }

    /**
     * Busca múltiplos patrimônios em uma única query
     */
    private java.util.Map<Integer, Patrimonio> buscarPatrimoniosEmBatch(java.util.Set<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return new java.util.HashMap<>();
        
        logger.debug("🏷️ Buscando {} patrimônios em batch", ids.size());
        java.util.Map<Integer, Patrimonio> cache = new java.util.HashMap<>();
        
        // Buscar todos de uma vez
        List<Patrimonio> patrimonios = patrimonioDAO.buscarPorIds(new ArrayList<>(ids));
        
        for (Patrimonio pat : patrimonios) {
            cache.put(pat.getId(), pat);
        }
        
        logger.debug("✓ {} patrimônios carregados no cache", cache.size());
        return cache;
    }

    /**
     * Converte coleta para response usando dados do cache
     * Evita queries adicionais ao banco (otimização crítica)
     */
    private MobileColetaResponse converterParaResponseComCache(
            Coleta coleta, 
            Usuario usuario, 
            Inventario inventario,
            Patrimonio patrimonio) {
        
        MobileColetaResponse response = new MobileColetaResponse();

        response.setId((long) coleta.getId());
        response.setIdInventario(coleta.getIdInventario());
        response.setNomeInventario(inventario != null ? inventario.getNome() : null);
        response.setDataColeta(formatDataColeta(coleta.getDataColeta()));
        response.setStatusColeta(coleta.getStatusColeta());
        response.setObservacaoColeta(coleta.getObservacaoColeta());
        response.setLocalizacaoEncontrada(coleta.getLocalizacaoEncontrada());
        response.setEstadoEncontrado(coleta.getEstadoEncontrado());
        response.setNomeColetor(usuario.getNomeCompleto());
        response.setUsuarioId(coleta.getIdColetor());
        response.setSemEtiqueta(coleta.isSemEtiqueta());
        response.setSincronizado(true);

        if (coleta.isSemEtiqueta()) {
            response.setDescricaoItemSemEtiqueta(coleta.getDescricaoItemSemEtiqueta());
            response.setCategoriaItemSemEtiqueta(coleta.getCategoriaItemSemEtiqueta());
        } else if (patrimonio != null) {
            // Usar patrimônio do cache (sem query adicional)
            logger.debug("✓ Usando patrimônio do cache: ID={}, Numero={}, Descricao={}", 
                    patrimonio.getId(), patrimonio.getNumero(), patrimonio.getDescricao());
            response.setPatrimonioId(patrimonio.getId());
            response.setNumeroPatrimonio(patrimonio.getNumero());
            response.setDescricaoPatrimonio(patrimonio.getDescricao());  // CRÍTICO: Descrição
            response.setIdSala(patrimonio.getIdSala());
            response.setNomeSala(patrimonio.getNomeSala());
        } else {
            // Patrimônio não encontrado no cache
            logger.warn("⚠️ Patrimônio não encontrado no cache para Coleta ID: {} (PatrimonioID: {})", 
                    coleta.getId(), coleta.getIdPatrimonio());
        }

        return response;
    }
    
    /**
     * Busca todas as salas que possuem coletas registradas
     * Útil para popular o filtro de salas na tela de itens coletados
     * 
     * @param inventarioId ID do inventário (opcional, usa o ativo se não informado)
     * @return lista de salas com coletas (nome e quantidade)
     */
    public List<Map<String, Object>> buscarSalasComColetas(Integer inventarioId) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Buscando salas com coletas para inventário: {}", 
                inventarioId != null ? inventarioId : "ATIVO");
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        try {
            // Se não informou inventário, buscar o ativo
            int idInventario = inventarioId != null ? inventarioId : 0;
            if (idInventario == 0) {
                Inventario ativo = inventarioDAO.buscarInventarioAtivo();
                if (ativo != null) {
                    idInventario = ativo.getId();
                }
            }
            
            // Query para buscar salas distintas com coletas
            // Prioriza localizacao_encontrada (onde foi encontrado) sobre nome_sala (localização original)
            String sql = """
                SELECT DISTINCT 
                    COALESCE(c.localizacao_encontrada, p.nome_sala) as nome_sala,
                    COUNT(*) as total_coletas
                FROM tabela_coleta c
                LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
                WHERE c.id_inventario = ?
                AND (c.localizacao_encontrada IS NOT NULL AND c.localizacao_encontrada != '' 
                     OR p.nome_sala IS NOT NULL AND p.nome_sala != '')
                GROUP BY COALESCE(c.localizacao_encontrada, p.nome_sala)
                ORDER BY nome_sala
                """;
            
            try (java.sql.Connection conn = com.inventario.sihcp.util.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, idInventario);
                
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String nomeSala = rs.getString("nome_sala");
                        int totalColetas = rs.getInt("total_coletas");
                        
                        if (nomeSala != null && !nomeSala.trim().isEmpty()) {
                            Map<String, Object> sala = new HashMap<>();
                            sala.put("nome", nomeSala.trim());
                            sala.put("nomeSala", nomeSala.trim());
                            sala.put("localizacaoEncontrada", nomeSala.trim());
                            sala.put("totalColetas", totalColetas);
                            resultado.add(sala);
                        }
                    }
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} salas com coletas encontradas em {}ms", resultado.size(), duration);
            
        } catch (SQLException e) {
            logger.error("Erro ao buscar salas com coletas", e);
            throw e;
        }
        
        return resultado;
    }
    
    // ==================== MÉTODOS PARA TRATAMENTO DE COLETA DUPLICADA ====================
    
    /**
     * Verifica se já existe uma coleta para o patrimônio no inventário
     * 
     * @param idInventario ID do inventário
     * @param idPatrimonio ID do patrimônio
     * @return Coleta existente ou null se não houver duplicata
     */
    private Coleta verificarColetaDuplicada(Integer idInventario, Integer idPatrimonio) {
        if (idInventario == null || idPatrimonio == null || idPatrimonio <= 0) {
            return null;
        }
        
        try {
            return coletaDAO.buscarColetaExistente(idInventario, idPatrimonio);
        } catch (SQLException e) {
            logger.error("Erro ao verificar coleta duplicada: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Cria uma resposta indicando que a coleta é duplicada
     * Inclui informações da coleta original para o usuário
     * 
     * @param coletaExistente Coleta que já existe no banco
     * @param request Request original que tentou criar a duplicata
     * @return Response com status de duplicada
     */
    private MobileColetaResponse criarRespostaDuplicada(Coleta coletaExistente, MobileColetaRequest request) {
        MobileColetaResponse response = new MobileColetaResponse();
        
        // Marcar como duplicada
        response.setDuplicada(true);
        response.setMensagemDuplicada("Este patrimônio já foi coletado neste inventário. Exclua a coleta local para evitar duplicação.");
        
        // Dados da coleta original
        response.setColetaOriginalId((long) coletaExistente.getId());
        response.setDataColetaOriginal(formatDataColeta(coletaExistente.getDataColeta()));
        response.setColetorOriginal(coletaExistente.getNomeColetor());
        
        // Dados do request para referência
        response.setNumeroPatrimonio(request.getNumeroPatrimonio());
        response.setIdInventario(request.getIdInventario());
        response.setStatusColeta("DUPLICADA");
        response.setSincronizado(false);
        
        return response;
    }
}
