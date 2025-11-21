package com.inventario.dao;

import com.inventario.util.DatabaseConnection;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.*;

/**
 * DAO para geração de relatórios de coleta de inventário.
 * Contém métodos para gerar diversos tipos de relatórios baseados nos dados de
 * coleta.
 * 
 * IMPORTANTE: Todos os relatórios são vinculados ao inventário ativo do
 * sistema,
 * garantindo que apenas dados do inventário em andamento sejam exibidos.
 * Sistema de Inventário IFMT
 */
@Repository
public class RelatorioColetaDAO {

    /**
     * Gera relatório de itens encontrados durante a coleta
     * IMPORTANTE: Este método trabalha apenas com o inventário ativo do sistema
     * 
     * @param idInventario ID do inventário ativo
     * @return Lista de mapas com dados dos itens encontrados
     */
    public List<Map<String, Object>> gerarRelatorioItensEncontrados(int idInventario) {
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "marca",
                    p.MODELO as "modelo",
                    r.NOME as "Responsável",
                    s.NOME as "Setor",
                    c.LOCALIZACAO_ENCONTRADA as "Localização Encontrada",
                    c.ESTADO_ENCONTRADO as "Estado",
                    c.DATA_COLETA as "Data Coleta",
                    c.OBSERVACAO_COLETA as "Observações",
                    CASE WHEN c.SEM_ETIQUETA THEN 'Sim' ELSE 'Não' END as "Sem Etiqueta",
                    p.STATUS as "situacao",
                    p.VALOR_AQUISICAO as "Valor"
                FROM TABELA_COLETA c
                INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                WHERE c.ID_INVENTARIO = ?
                  AND c.STATUS_COLETA = 'COLETADO'
                ORDER BY p.NUMERO
                """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Gera relatório de itens NÃO ENCONTRADOS durante a coleta
     * 
     * LÓGICA SIMPLIFICADA (IGUAL A "NÃO COLETADOS"):
     * - Patrimônios ATIVOS que NÃO estão na TABELA_COLETA do inventário
     * - Ou seja: patrimônios que ainda não foram coletados
     * 
     * NOTA: Este relatório tem a mesma lógica de "Itens Não Coletados"
     * pois ambos representam patrimônios pendentes de coleta.
     * 
     * @param idInventario ID do inventário ativo
     * @return Lista de mapas com dados dos itens não encontrados/não coletados
     */
    public List<Map<String, Object>> gerarRelatorioItensNaoEncontrados(int idInventario) {
        System.out.println("\n=== RELATÓRIO ITENS NÃO ENCONTRADOS (SIMPLIFICADO) ===");
        System.out.println("📋 Inventário ID: " + idInventario);

        // Query CORRIGIDA:
        // 1. STATUS = 'Ativo' (primeira maiúscula, como está no banco)
        // 2. NOT EXISTS ao invés de NOT IN (evita problema com NULLs)
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    COALESCE(p.MARCA, '') as "marca",
                    COALESCE(p.MODELO, '') as "modelo",
                    COALESCE(r.NOME, 'Sem Responsável') as "Responsável",
                    COALESCE(s.NOME, 'Sem Setor') as "Setor",
                    COALESCE(sa.NUMERO_SALA, 'N/A') as "Sala",
                    COALESCE(sa.DESCRICAO, '') as "Localização Cadastrada",
                    'NÃO ENCONTRADO' as "Estado",
                    'Não coletado' as "Situação",
                    COALESCE(p.VALOR_AQUISICAO, 0) as "Valor",
                    p.STATUS as "status"
                FROM TABELA_PATRIMONIO p
                LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID
                WHERE p.STATUS = 'Ativo'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM TABELA_COLETA c
                      WHERE c.ID_PATRIMONIO = p.ID
                        AND c.ID_INVENTARIO = ?
                  )
                ORDER BY
                    COALESCE(s.NOME, 'Sem Setor'),
                    COALESCE(sa.NUMERO_SALA, 'N/A'),
                    COALESCE(r.NOME, 'Sem Responsável'),
                    p.NUMERO
                """;

        System.out.println("🔍 Query SIMPLES: Patrimônios ATIVOS que NÃO estão na TABELA_COLETA");

        List<Map<String, Object>> resultado = executarConsulta(sql, idInventario);
        System.out.println("✅ Resultado: " + resultado.size() + " itens não encontrados/não coletados");

        if (resultado.isEmpty()) {
            System.out.println("ℹ️ Todos os patrimônios ativos foram coletados neste inventário!");
        } else {
            System.out.println("📊 Primeiros 5 itens:");
            for (int i = 0; i < Math.min(5, resultado.size()); i++) {
                Map<String, Object> item = resultado.get(i);
                System.out.println("  " + (i + 1) + ". " + item.get("Número Patrimônio") +
                        " - " + item.get("Descrição") +
                        " (Setor: " + item.get("Setor") + ")");
            }
        }
        System.out.println("=====================================\n");

        return resultado;
    }

    /**
     * Gera relatório de itens por responsável
     * IMPORTANTE: Este método trabalha apenas com o inventário ativo do sistema
     * 
     * @param idInventario ID do inventário ativo
     * @return Lista de mapas com dados agrupados por responsável
     */
    public List<Map<String, Object>> gerarRelatorioPorResponsavel(int idInventario) {
        String sql = """
                SELECT
                    r.NOME as "Responsável",
                    s.NOME as "Setor",
                    COUNT(p.ID) as "Total Itens",
                    COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as "Encontrados",
                    COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as "Não Encontrados",
                    COUNT(CASE WHEN c.SEM_ETIQUETA = TRUE THEN 1 END) as "Sem Etiqueta",
                    ROUND(
                        (COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) * 100.0 /
                         NULLIF(COUNT(p.ID), 0)), 2
                    ) as "% Encontrados"
                FROM TABELA_RESPONSAVEL r
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE r.ATIVO = TRUE
                GROUP BY r.ID, r.NOME, s.NOME
                HAVING COUNT(p.ID) > 0
                ORDER BY r.NOME
                """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Gera relatório de itens encontrados sem etiqueta patrimonial
     * 
     * @param idInventario ID do inventário
     * @return Lista com dados dos itens sem etiqueta
     */
    public List<Map<String, Object>> gerarRelatorioItensSemEtiqueta(int idInventario) {
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "marca",
                    p.MODELO as "modelo",
                    r.NOME as "Responsável",
                    s.NOME as "Setor",
                    c.LOCALIZACAO_ENCONTRADA as "Localização Encontrada",
                    c.ESTADO_ENCONTRADO as "Estado",
                    c.DATA_COLETA as "Data Coleta",
                    c.OBSERVACAO_COLETA as "Observações",
                    p.STATUS as "situacao",
                    p.VALOR_AQUISICAO as "Valor"
                FROM TABELA_COLETA c
                INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                WHERE c.ID_INVENTARIO = ?
                  AND c.SEM_ETIQUETA = TRUE
                ORDER BY r.NOME, p.NUMERO
                """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Gera relatório de itens NÃO COLETADOS no inventário
     * 
     * DIFERENÇA ENTRE "NÃO ENCONTRADOS" E "NÃO COLETADOS":
     * - NÃO ENCONTRADOS: Patrimônios que foram buscados mas não localizados
     * fisicamente
     * - NÃO COLETADOS: Patrimônios que ainda não foram processados/verificados no
     * inventário
     * 
     * LÓGICA IMPLEMENTADA:
     * 1. Busca todos os patrimônios ATIVOS do sistema
     * 2. Verifica quais NÃO possuem registro na tabela COLETA para o inventário
     * especificado
     * 3. Retorna lista completa com informações cadastrais (localização,
     * responsável, setor)
     * 
     * CASOS DE USO:
     * - Identificar patrimônios que ainda precisam ser coletados
     * - Planejar rotas de coleta por setor/sala
     * - Acompanhar progresso do inventário
     * 
     * @param idInventario ID do inventário ativo
     * @return Lista de mapas com dados dos itens não coletados
     */
    public List<Map<String, Object>> gerarRelatorioItensNaoColetados(int idInventario) {
        System.out.println("\n=== RELATÓRIO ITENS NÃO COLETADOS (DEBUG) ===");
        System.out.println("📋 Inventário ID: " + idInventario);

        // PASSO 1: Verificar quantos patrimônios ATIVOS existem
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verificar TODOS os patrimônios (sem filtro de STATUS)
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM TABELA_PATRIMONIO")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("📊 Total de patrimônios NO SISTEMA (todos): " + rs.getInt(1));
                    }
                }
            }

            // Verificar com STATUS = 'ATIVO' (maiúsculo)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE STATUS = 'ATIVO'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("✅ Total com STATUS = 'ATIVO' (maiúsculo): " + rs.getInt(1));
                    }
                }
            }

            // Verificar com STATUS = 'Ativo' (primeira maiúscula)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE STATUS = 'Ativo'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 0) {
                            System.out.println("✅ Total com STATUS = 'Ativo' (primeira maiúscula): " + count);
                        }
                    }
                }
            }

            // Verificar TODOS os valores distintos de STATUS
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT DISTINCT STATUS, COUNT(*) FROM TABELA_PATRIMONIO GROUP BY STATUS")) {
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("📋 Valores de STATUS no banco:");
                    while (rs.next()) {
                        System.out.println("   - '" + rs.getString(1) + "': " + rs.getInt(2) + " patrimônios");
                    }
                }
            }

            // PASSO 2: Verificar quantos patrimônios estão na COLETA deste inventário
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT ID_PATRIMONIO) FROM TABELA_COLETA WHERE ID_INVENTARIO = ?")) {
                ps.setInt(1, idInventario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(
                                "✅ Total de patrimônios na COLETA do inventário " + idInventario + ": " + rs.getInt(1));
                    }
                }
            }

            // PASSO 3: Calcular quantos DEVERIAM aparecer no relatório
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM TABELA_PATRIMONIO p " +
                            "WHERE p.STATUS = 'ATIVO' " +
                            "AND p.ID NOT IN (SELECT ID_PATRIMONIO FROM TABELA_COLETA WHERE ID_INVENTARIO = ?)")) {
                ps.setInt(1, idInventario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("✅ Total ESPERADO no relatório (ATIVOS - COLETADOS): " + rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao obter métricas: " + e.getMessage());
            e.printStackTrace();
        }

        // Query CORRIGIDA:
        // 1. STATUS = 'Ativo' (primeira maiúscula, como está no banco)
        // 2. NOT EXISTS ao invés de NOT IN (evita problema com NULLs)
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    COALESCE(p.MARCA, '') as "marca",
                    COALESCE(p.MODELO, '') as "modelo",
                    COALESCE(r.NOME, 'Sem Responsável') as "Responsável",
                    COALESCE(s.NOME, 'Sem Setor') as "Setor",
                    COALESCE(sa.NUMERO_SALA, 'N/A') as "Sala",
                    COALESCE(sa.DESCRICAO, '') as "Localização Cadastrada",
                    'PENDENTE' as "Estado",
                    'Aguardando coleta' as "Situação",
                    COALESCE(p.VALOR_AQUISICAO, 0) as "Valor",
                    p.STATUS as "status"
                FROM TABELA_PATRIMONIO p
                LEFT JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID
                WHERE p.STATUS = 'Ativo'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM TABELA_COLETA c
                      WHERE c.ID_PATRIMONIO = p.ID
                        AND c.ID_INVENTARIO = ?
                  )
                ORDER BY
                    COALESCE(s.NOME, 'Sem Setor'),
                    COALESCE(sa.NUMERO_SALA, 'N/A'),
                    COALESCE(r.NOME, 'Sem Responsável'),
                    p.NUMERO
                """;

        System.out.println("🔍 Executando query principal...");

        List<Map<String, Object>> resultado = executarConsulta(sql, idInventario);
        System.out.println("✅ Resultado REAL: " + resultado.size() + " itens retornados");

        if (resultado.isEmpty()) {
            System.out.println("⚠️ ATENÇÃO: Query retornou 0 resultados!");
            System.out.println("   Possíveis causas:");
            System.out.println("   1. Todos os patrimônios ATIVOS já foram coletados");
            System.out.println("   2. Não há patrimônios com STATUS = 'ATIVO'");
            System.out.println("   3. Problema no ID do inventário");
        } else {
            System.out.println("📊 Primeiros 5 itens não coletados:");
            for (int i = 0; i < Math.min(5, resultado.size()); i++) {
                Map<String, Object> item = resultado.get(i);
                System.out.println("  " + (i + 1) + ". " + item.get("Número Patrimônio") +
                        " - " + item.get("Descrição") +
                        " (Setor: " + item.get("Setor") + ")");
            }
        }
        System.out.println("=====================================\n");

        return resultado;
    }

    /**
     * Gera relatório detalhado por responsável específico
     * 
     * @param idInventario  ID do inventário
     * @param idResponsavel ID do responsável
     * @return Lista com dados detalhados do responsável
     */
    public List<Map<String, Object>> gerarRelatorioDetalhadoResponsavel(int idInventario, int idResponsavel) {
        String sql = """
                SELECT
                    r.NOME as "Responsável",
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "marca",
                    p.MODELO as "modelo",
                    CASE
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 'Encontrado'
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 'Não Encontrado'
                        WHEN c.STATUS_COLETA IS NULL THEN 'Não Coletado'
                        ELSE c.STATUS_COLETA
                    END as "Status",
                    CASE WHEN c.SEM_ETIQUETA = TRUE THEN 'Sim' ELSE 'Não' END as "Sem Etiqueta",
                    COALESCE(c.LOCALIZACAO_ENCONTRADA, c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização",
                    c.OBSERVACAO_COLETA as "Observações",
                    p.STATUS as "situacao",
                    p.VALOR_AQUISICAO as "Valor"
                FROM TABELA_RESPONSAVEL r
                INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE r.ID = ?
                ORDER BY
                    CASE
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1
                        WHEN c.STATUS_COLETA IS NULL THEN 2
                        ELSE 3
                    END,
                    p.NUMERO
                """;

        return executarConsulta(sql, idInventario, idResponsavel);
    }

    /**
     * Gera relatório detalhado por responsável
     * 
     * @param idInventario    ID do inventário
     * @param nomeResponsavel Nome do responsável
     * @return Lista de patrimônios do responsável
     */
    /**
     * Gera relatório detalhado por responsável
     * 
     * @param idInventario    ID do inventário
     * @param nomeResponsavel Nome do responsável
     * @param status          Status da coleta (opcional)
     * @return Lista de patrimônios do responsável
     */
    public List<Map<String, Object>> gerarRelatorioDetalhadoPorResponsavel(int idInventario, String nomeResponsavel,
            String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.NUMERO as numero,
                    p.DESCRICAO as descricao,
                    p.MARCA as marca,
                    p.MODELO as modelo,
                    r.NOME as responsavel,
                    s.NOME as setor,
                    c.STATUS_COLETA as status_coleta,
                    c.OBSERVACAO_COLETA as observacao,
                    c.LOCALIZACAO_ENCONTRADA as "Localização Encontrada",
                    c.SEM_ETIQUETA as sem_etiqueta,
                    p.STATUS as situacao,
                    p.VALOR_AQUISICAO as valor
                FROM TABELA_PATRIMONIO p
                JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                LEFT JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE r.NOME ILIKE ?
                """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idInventario);
        parametros.add("%" + nomeResponsavel + "%");

        // Adicionar filtro de status se fornecido
        if (status != null && !status.trim().isEmpty() && !"Todos".equals(status)) {
            if ("Não Coletado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA IS NULL");
            } else if ("Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'COLETADO'");
            } else if ("Não Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'NAO_ENCONTRADO'");
            } else {
                sql.append(" AND c.STATUS_COLETA = ?");
                parametros.add(status);
            }
        }

        sql.append(" ORDER BY p.NUMERO");

        return executarConsulta(sql.toString(), parametros.toArray());
    }

    /**
     * Gera estatísticas gerais do inventário
     * 
     * @param idInventario ID do inventário
     * @return Lista com estatísticas formatadas
     */
    public List<Map<String, Object>> gerarEstatisticasGerais(int idInventario) {
        List<Map<String, Object>> estatisticas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total de patrimônios
            String sqlTotal = "SELECT COUNT(*) as total FROM TABELA_PATRIMONIO";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotal);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("descricao", "Total de Patrimônios");
                    item.put("valor", rs.getInt("total"));
                    item.put("tipo", "TOTAL");
                    estatisticas.add(item);
                }
            }

            // Estatísticas de coleta
            String sqlColeta = """
                    SELECT
                        COUNT(*) as total_coletados,
                        COUNT(CASE WHEN STATUS_COLETA = 'COLETADO' THEN 1 END) as coletados,
                        COUNT(CASE WHEN STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as nao_encontrados,
                        COUNT(CASE WHEN STATUS_COLETA = 'DANIFICADO' THEN 1 END) as danificados,
                        COUNT(CASE WHEN SEM_ETIQUETA = true THEN 1 END) as sem_etiqueta
                    FROM TABELA_COLETA
                    WHERE ID_INVENTARIO = ?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlColeta)) {
                stmt.setInt(1, idInventario);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Adicionar cada estatística como um item
                        Map<String, Object> coletados = new HashMap<>();
                        coletados.put("descricao", "Itens Coletados");
                        coletados.put("valor", rs.getInt("coletados"));
                        coletados.put("tipo", "COLETADO");
                        estatisticas.add(coletados);

                        Map<String, Object> naoEncontrados = new HashMap<>();
                        naoEncontrados.put("descricao", "Itens Não Encontrados");
                        naoEncontrados.put("valor", rs.getInt("nao_encontrados"));
                        naoEncontrados.put("tipo", "NAO_ENCONTRADO");
                        estatisticas.add(naoEncontrados);

                        Map<String, Object> danificados = new HashMap<>();
                        danificados.put("descricao", "Itens Danificados");
                        danificados.put("valor", rs.getInt("danificados"));
                        danificados.put("tipo", "DANIFICADO");
                        estatisticas.add(danificados);

                        Map<String, Object> semEtiqueta = new HashMap<>();
                        semEtiqueta.put("descricao", "Itens Sem Etiqueta");
                        semEtiqueta.put("valor", rs.getInt("sem_etiqueta"));
                        semEtiqueta.put("tipo", "SEM_ETIQUETA");
                        estatisticas.add(semEtiqueta);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return estatisticas;
    }

    /**
     * Gera relatório de divergências encontradas durante a coleta
     * 
     * @param idInventario ID do inventário
     * @return Lista com divergências encontradas
     */
    /**
     * Gera relatório de divergências encontradas durante a coleta (Manual)
     * 
     * @param idInventario ID do inventário
     * @return Lista com divergências encontradas
     */
    public List<Map<String, Object>> gerarRelatorioDivergencias(int idInventario) {
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "marca",
                    p.MODELO as "modelo",
                    r.NOME as "Responsável",
                    c.LOCALIZACAO_ATUAL as "Localização Cadastrada",
                    c.LOCALIZACAO_ENCONTRADA as "Localização Encontrada",
                    c.ESTADO_ENCONTRADO as "Estado Encontrado",
                    c.MOTIVO_DIVERGENCIA as "Motivo da Divergência",
                    c.DATA_COLETA as "Data Coleta",
                    c.OBSERVACAO_COLETA as "Observações",
                    p.STATUS as "situacao",
                    p.VALOR_AQUISICAO as "Valor"
                FROM TABELA_COLETA c
                INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                WHERE c.ID_INVENTARIO = ?
                  AND c.DIVERGENCIA = TRUE
                ORDER BY c.DATA_COLETA DESC
                """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Gera relatório de divergências automáticas (Localização e Estado)
     * 
     * @param idInventario ID do inventário
     * @return Lista com divergências detectadas automaticamente
     */
    public List<Map<String, Object>> gerarRelatorioDivergenciasAutomaticas(int idInventario) {
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    COALESCE(c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização Cadastrada",
                    COALESCE(c.LOCALIZACAO_ENCONTRADA, 'Não informado') as "Localização Encontrada",
                    p.ESTADO_CONSERVAÇÃO as "Estado Cadastrado",
                    c.ESTADO_ENCONTRADO as "Estado Encontrado",
                    CASE
                        WHEN c.DIVERGENCIA = TRUE THEN 'Sim'
                        ELSE 'Não'
                    END as "Flag Divergência",
                    c.MOTIVO_DIVERGENCIA as "Motivo",
                    CASE
                        WHEN c.LOCALIZACAO_ENCONTRADA IS NOT NULL AND c.LOCALIZACAO_ATUAL <> c.LOCALIZACAO_ENCONTRADA THEN 'Localização'
                        WHEN c.ESTADO_ENCONTRADO IS NOT NULL AND p.ESTADO_CONSERVAÇÃO <> c.ESTADO_ENCONTRADO THEN 'Estado'
                        WHEN c.DIVERGENCIA = TRUE THEN 'Manual'
                        ELSE 'Outro'
                    END as "Tipo Divergência"
                FROM TABELA_COLETA c
                INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
                WHERE c.ID_INVENTARIO = ?
                  AND (
                      (c.LOCALIZACAO_ENCONTRADA IS NOT NULL AND c.LOCALIZACAO_ATUAL <> c.LOCALIZACAO_ENCONTRADA) OR
                      (c.ESTADO_ENCONTRADO IS NOT NULL AND p.ESTADO_CONSERVAÇÃO <> c.ESTADO_ENCONTRADO) OR
                      c.DIVERGENCIA = TRUE
                  )
                ORDER BY p.NUMERO
                """;

        return executarConsulta(sql, idInventario);
    }

    /**
     * Método auxiliar para executar consultas SQL
     * 
     * @param sql        Consulta SQL
     * @param parametros Parâmetros da consulta
     * @return Lista com resultados
     */
    private List<Map<String, Object>> executarConsulta(String sql, Object... parametros) {
        List<Map<String, Object>> resultados = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Definir parâmetros
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        linha.put(columnName, value);
                    }

                    resultados.add(linha);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao executar consulta de relatório: " + e.getMessage());
            e.printStackTrace();
        }

        return resultados;
    }

    /**
     * Conta total de patrimônios por responsável
     * 
     * @param idResponsavel ID do responsável
     * @return Número total de patrimônios
     */
    public int contarPatrimoniosResponsavel(int idResponsavel) {
        String sql = "SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ID_RESPONSAVEL = ? AND STATUS = 'ATIVO'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idResponsavel);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao contar patrimônios do responsável: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Busca todos os responsáveis que possuem patrimônios
     * 
     * @return Lista de responsáveis
     */
    public List<Map<String, Object>> listarResponsaveisComPatrimonios() {
        String sql = """
                SELECT DISTINCT
                    r.ID,
                    r.NOME,
                    s.NOME as SETOR,
                    COUNT(p.ID) as TOTAL_PATRIMONIOS
                FROM TABELA_RESPONSAVEL r
                INNER JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                WHERE r.ATIVO = TRUE AND p.STATUS = 'ATIVO'
                GROUP BY r.ID, r.NOME, s.NOME
                ORDER BY r.NOME
                """;

        return executarConsulta(sql);
    }

    // ========== RELATÓRIOS AVANÇADOS ==========

    /**
     * Gera relatório avançado por setor específico
     * 
     * @param idInventario ID do inventário
     * @param nomeSetor    Nome do setor
     * @param dataInicio   Data de início do período (opcional)
     * @param dataFim      Data de fim do período (opcional)
     * @return Lista com dados detalhados do setor
     */
    /**
     * Gera relatório avançado por setor específico
     * 
     * @param idInventario ID do inventário
     * @param nomeSetor    Nome do setor
     * @param dataInicio   Data de início do período (opcional)
     * @param dataFim      Data de fim do período (opcional)
     * @param status       Status da coleta (opcional)
     * @return Lista com dados detalhados do setor
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorSetor(int idInventario, String nomeSetor,
            java.util.Date dataInicio, java.util.Date dataFim, String status) {
        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            s.NOME as "Setor",
                            r.NOME as "Responsável",
                            p.NUMERO as "Número Patrimônio",
                            p.DESCRICAO as "Descrição",
                            p.MARCA as "Marca",
                            p.MODELO as "Modelo",
                            CASE
                                WHEN c.STATUS_COLETA = 'COLETADO' THEN 'Encontrado'
                                WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 'Não Encontrado'
                                WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 'Danificado'
                                WHEN c.STATUS_COLETA IS NULL THEN 'Não Coletado'
                                ELSE c.STATUS_COLETA
                            END as "Status Coleta",
                            CASE WHEN c.SEM_ETIQUETA = TRUE THEN 'Sim' ELSE 'Não' END as "Sem Etiqueta",
                            COALESCE(c.LOCALIZACAO_ENCONTRADA, c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização Encontrada",
                            c.OBSERVACAO_COLETA as "Observações",
                            p.STATUS as "Situação",
                            p.VALOR_AQUISICAO as "Valor",
                            COALESCE(sa.NUMERO_SALA, 'Não informado') as "Sala"
                        FROM TABELA_SETOR s
                        INNER JOIN TABELA_RESPONSAVEL r ON s.ID = r.ID_SETOR
                        INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                        LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
                        LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                        WHERE p.STATUS = 'ATIVO'
                        """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idInventario);

        // Adicionar filtro de setor apenas se especificado
        if (nomeSetor != null && !nomeSetor.trim().isEmpty()) {
            sql.append(" AND s.NOME ILIKE ?");
            parametros.add("%" + nomeSetor + "%");
        }

        // Adicionar filtro de período se fornecido
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND (c.DATA_COLETA IS NULL OR c.DATA_COLETA BETWEEN ? AND ?)");
            parametros.add(new java.sql.Date(dataInicio.getTime()));
            parametros.add(new java.sql.Date(dataFim.getTime()));
        }

        // Adicionar filtro de status se fornecido
        if (status != null && !status.trim().isEmpty() && !"Todos".equals(status)) {
            if ("Não Coletado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA IS NULL");
            } else if ("Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'COLETADO'");
            } else if ("Não Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'NAO_ENCONTRADO'");
            } else {
                sql.append(" AND c.STATUS_COLETA = ?");
                parametros.add(status);
            }
        }

        sql.append("""
                ORDER BY
                    s.NOME,
                    r.NOME,
                    CASE
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1
                        WHEN c.STATUS_COLETA IS NULL THEN 2
                        ELSE 3
                    END,
                    p.NUMERO
                """);

        return executarConsulta(sql.toString(), parametros.toArray());
    }

    /**
     * Gera relatório avançado por responsável específico com período
     * 
     * @param idInventario    ID do inventário
     * @param nomeResponsavel Nome do responsável
     * @param dataInicio      Data de início do período (opcional)
     * @param dataFim         Data de fim do período (opcional)
     * @return Lista com dados detalhados do responsável
     */
    /**
     * Gera relatório avançado por responsável específico com período e status
     * 
     * @param idInventario    ID do inventário
     * @param nomeResponsavel Nome do responsável
     * @param dataInicio      Data de início do período (opcional)
     * @param dataFim         Data de fim do período (opcional)
     * @param status          Status da coleta (opcional)
     * @return Lista com dados detalhados do responsável
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorResponsavel(int idInventario, String nomeResponsavel,
            java.util.Date dataInicio, java.util.Date dataFim, String status) {
        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            r.NOME as "Responsável",
                            s.NOME as "Setor",
                            p.NUMERO as "Número Patrimônio",
                            p.DESCRICAO as "Descrição",
                            p.MARCA as "Marca",
                            p.MODELO as "Modelo",
                            CASE
                                WHEN c.STATUS_COLETA = 'COLETADO' THEN 'Encontrado'
                                WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 'Não Encontrado'
                                WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 'Danificado'
                                WHEN c.STATUS_COLETA IS NULL THEN 'Não Coletado'
                                ELSE c.STATUS_COLETA
                            END as "Status Coleta",
                            CASE WHEN c.SEM_ETIQUETA = TRUE THEN 'Sim' ELSE 'Não' END as "Sem Etiqueta",
                            COALESCE(c.LOCALIZACAO_ENCONTRADA, c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização Encontrada",
                            c.OBSERVACAO_COLETA as "Observações",
                            p.STATUS as "Situação",
                            p.VALOR_AQUISICAO as "Valor",
                            COALESCE(sa.NUMERO_SALA, 'Não informado') as "Sala"
                        FROM TABELA_RESPONSAVEL r
                        INNER JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                        INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                        LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
                        LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                        WHERE p.STATUS = 'ATIVO'
                        """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idInventario);

        // Adicionar filtro de responsável apenas se especificado
        if (nomeResponsavel != null && !nomeResponsavel.trim().isEmpty()) {
            sql.append(" AND r.NOME ILIKE ?");
            parametros.add("%" + nomeResponsavel + "%");
        }

        // Adicionar filtro de período se fornecido
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND (c.DATA_COLETA IS NULL OR c.DATA_COLETA BETWEEN ? AND ?)");
            parametros.add(new java.sql.Date(dataInicio.getTime()));
            parametros.add(new java.sql.Date(dataFim.getTime()));
        }

        // Adicionar filtro de status se fornecido
        if (status != null && !status.trim().isEmpty() && !"Todos".equals(status)) {
            if ("Não Coletado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA IS NULL");
            } else if ("Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'COLETADO'");
            } else if ("Não Encontrado".equals(status)) {
                sql.append(" AND c.STATUS_COLETA = 'NAO_ENCONTRADO'");
            } else {
                sql.append(" AND c.STATUS_COLETA = ?");
                parametros.add(status);
            }
        }

        sql.append("""
                ORDER BY
                    r.NOME,
                    CASE
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1
                        WHEN c.STATUS_COLETA IS NULL THEN 2
                        ELSE 3
                    END,
                    p.NUMERO
                """);

        return executarConsulta(sql.toString(), parametros.toArray());
    }

    /**
     * Gera relatório avançado por período específico
     * 
     * @param idInventario ID do inventário
     * @param dataInicio   Data de início do período
     * @param dataFim      Data de fim do período
     * @return Lista com dados do período
     */
    public List<Map<String, Object>> gerarRelatorioAvancadoPorPeriodo(int idInventario, java.util.Date dataInicio,
            java.util.Date dataFim) {
        String sql = """
                SELECT
                    c.DATA_COLETA as "Data Coleta",
                    s.NOME as "Setor",
                    r.NOME as "Responsável",
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "Marca",
                    p.MODELO as "Modelo",
                    CASE
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 'Encontrado'
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 'Não Encontrado'
                        WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 'Danificado'
                        ELSE c.STATUS_COLETA
                    END as "Status Coleta",
                    CASE WHEN c.SEM_ETIQUETA = TRUE THEN 'Sim' ELSE 'Não' END as "Sem Etiqueta",
                    COALESCE(c.LOCALIZACAO_ENCONTRADA, c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização Encontrada",
                    c.OBSERVACAO_COLETA as "Observações",
                    p.STATUS as "Situação",
                    p.VALOR_AQUISICAO as "Valor",
                    COALESCE(sa.NUMERO_SALA, 'Não informado') as "Sala"
                FROM TABELA_COLETA c
                INNER JOIN TABELA_PATRIMONIO p ON c.ID_PATRIMONIO = p.ID
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                INNER JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
                WHERE c.ID_INVENTARIO = ?
                  AND p.STATUS = 'ATIVO'
                  AND c.DATA_COLETA BETWEEN ? AND ?
                ORDER BY c.DATA_COLETA DESC, s.NOME, r.NOME, p.NUMERO
                """;

        return executarConsulta(sql, idInventario,
                new java.sql.Date(dataInicio.getTime()),
                new java.sql.Date(dataFim.getTime()));
    }

    /**
     * Gera estatísticas avançadas por setor
     * 
     * @param idInventario ID do inventário
     * @param dataInicio   Data de início do período (opcional)
     * @param dataFim      Data de fim do período (opcional)
     * @return Lista com estatísticas por setor
     */
    public List<Map<String, Object>> gerarEstatisticasAvancadasPorSetor(int idInventario, java.util.Date dataInicio,
            java.util.Date dataFim) {
        StringBuilder sql = new StringBuilder(
                """
                        SELECT
                            s.NOME as "Setor",
                            COUNT(p.ID) as "Total Patrimônios",
                            COUNT(c.ID) as "Total Coletados",
                            COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as "Encontrados",
                            COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as "Não Encontrados",
                            COUNT(CASE WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 1 END) as "Danificados",
                            COUNT(CASE WHEN c.SEM_ETIQUETA = TRUE THEN 1 END) as "Sem Etiqueta",
                            ROUND(
                                (COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) * 100.0 /
                                 NULLIF(COUNT(p.ID), 0)), 2
                            ) as "% Encontrados",
                            ROUND(
                                (COUNT(c.ID) * 100.0 / NULLIF(COUNT(p.ID), 0)), 2
                            ) as "% Progresso",
                            SUM(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN COALESCE(p.VALOR_AQUISICAO, 0) ELSE 0 END) as "Valor Encontrado",
                            SUM(COALESCE(p.VALOR_AQUISICAO, 0)) as "Valor Total"
                        FROM TABELA_SETOR s
                        INNER JOIN TABELA_RESPONSAVEL r ON s.ID = r.ID_SETOR
                        INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                        LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                        WHERE p.STATUS = 'ATIVO'
                        """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idInventario);

        // Adicionar filtro de período se fornecido
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND (c.DATA_COLETA IS NULL OR c.DATA_COLETA BETWEEN ? AND ?)");
            parametros.add(new java.sql.Date(dataInicio.getTime()));
            parametros.add(new java.sql.Date(dataFim.getTime()));
        }

        sql.append("""
                GROUP BY s.ID, s.NOME
                HAVING COUNT(p.ID) > 0
                ORDER BY s.NOME
                """);

        return executarConsulta(sql.toString(), parametros.toArray());
    }

    /**
     * Gera relatório consolidado com resumo executivo
     * 
     * @param idInventario ID do inventário
     * @param dataInicio   Data de início do período (opcional)
     * @param dataFim      Data de fim do período (opcional)
     * @return Lista com dados consolidados
     */
    public List<Map<String, Object>> gerarRelatorioConsolidado(int idInventario, java.util.Date dataInicio,
            java.util.Date dataFim) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    'RESUMO EXECUTIVO' as "Categoria",
                    s.NOME as "Setor",
                    COUNT(p.ID) as "Total Patrimônios",
                    COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) as "Encontrados",
                    COUNT(CASE WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1 END) as "Não Encontrados",
                    COUNT(CASE WHEN c.STATUS_COLETA IS NULL THEN 1 END) as "Não Coletados",
                    ROUND(
                        (COUNT(CASE WHEN c.STATUS_COLETA = 'COLETADO' THEN 1 END) * 100.0 /
                         NULLIF(COUNT(p.ID), 0)), 2
                    ) as "% Sucesso",
                    SUM(COALESCE(p.VALOR_AQUISICAO, 0)) as "Valor Total"
                FROM TABELA_SETOR s
                INNER JOIN TABELA_RESPONSAVEL r ON s.ID = r.ID_SETOR
                INNER JOIN TABELA_PATRIMONIO p ON r.ID = p.ID_RESPONSAVEL
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE p.STATUS = 'ATIVO'
                """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idInventario);

        // Adicionar filtro de período se fornecido
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND (c.DATA_COLETA IS NULL OR c.DATA_COLETA BETWEEN ? AND ?)");
            parametros.add(new java.sql.Date(dataInicio.getTime()));
            parametros.add(new java.sql.Date(dataFim.getTime()));
        }

        sql.append("""
                GROUP BY s.ID, s.NOME
                HAVING COUNT(p.ID) > 0
                ORDER BY "% Sucesso" DESC, s.NOME
                """);

        return executarConsulta(sql.toString(), parametros.toArray());
    }

    /**
     * Gera relatório geral completo com todos os patrimônios e seus status
     * Fornece uma visão abrangente de todo o inventário incluindo itens coletados,
     * não coletados e não encontrados
     * 
     * @param idInventario ID do inventário ativo
     * @return Lista completa com todos os patrimônios e seus status de coleta
     */
    public List<Map<String, Object>> gerarRelatorioGeralCompleto(int idInventario) {
        String sql = """
                SELECT
                    p.NUMERO as "Número Patrimônio",
                    p.DESCRICAO as "Descrição",
                    p.MARCA as "Marca",
                    p.MODELO as "Modelo",
                    r.NOME as "Responsável",
                    s.NOME as "Setor",
                    COALESCE(sa.NUMERO_SALA, 'Não informado') as "Sala",
                    CASE
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 'Encontrado'
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 'Não Encontrado'
                        WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 'Danificado'
                        WHEN c.STATUS_COLETA IS NULL THEN 'Não Coletado'
                        ELSE c.STATUS_COLETA
                    END as "Status Coleta",
                    CASE
                        WHEN c.SEM_ETIQUETA = TRUE THEN 'Sim'
                        WHEN c.SEM_ETIQUETA = FALSE THEN 'Não'
                        ELSE 'N/A'
                    END as "Sem Etiqueta",
                    COALESCE(c.LOCALIZACAO_ENCONTRADA, c.LOCALIZACAO_ATUAL, 'Não informado') as "Localização Encontrada",
                    c.ESTADO_ENCONTRADO as "Estado Encontrado",
                    c.DATA_COLETA as "Data Coleta",
                    c.OBSERVACAO_COLETA as "Observações",
                    p.STATUS as "Situação Patrimônio",
                    COALESCE(p.VALOR_AQUISICAO, 0) as "Valor Aquisição",
                    p.DATA_ENTRADA as "Data Aquisição",
                    CASE
                        WHEN c.DIVERGENCIA = TRUE THEN 'Sim'
                        WHEN c.DIVERGENCIA = FALSE THEN 'Não'
                        ELSE 'N/A'
                    END as "Possui Divergência",
                    c.MOTIVO_DIVERGENCIA as "Motivo Divergência"
                FROM TABELA_PATRIMONIO p
                INNER JOIN TABELA_RESPONSAVEL r ON p.ID_RESPONSAVEL = r.ID
                INNER JOIN TABELA_SETOR s ON r.ID_SETOR = s.ID
                LEFT JOIN TABELA_SALA sa ON p.ID_SALA = sa.ID_SALA
                LEFT JOIN TABELA_COLETA c ON p.ID = c.ID_PATRIMONIO AND c.ID_INVENTARIO = ?
                WHERE p.STATUS = 'ATIVO'
                ORDER BY
                    CASE
                        WHEN c.STATUS_COLETA = 'NAO_ENCONTRADO' THEN 1
                        WHEN c.STATUS_COLETA IS NULL THEN 2
                        WHEN c.STATUS_COLETA = 'DANIFICADO' THEN 3
                        WHEN c.STATUS_COLETA = 'COLETADO' THEN 4
                        ELSE 5
                    END,
                    s.NOME, r.NOME, p.NUMERO
                """;

        return executarConsulta(sql, idInventario);
    }
}
