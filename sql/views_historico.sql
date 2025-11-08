-- =====================================================
-- VIEWS PARA CONSULTA DE HISTÓRICO
-- Sistema de Inventário Patrimonial
-- =====================================================

-- View para histórico completo de responsáveis
CREATE OR REPLACE VIEW vw_historico_responsavel_completo AS
SELECT 
    hr.id,
    hr.patrimonio_id,
    p.numero,
    p.descricao as patrimonio_descricao,
    hr.responsavel_anterior_id,
    ra.nome as responsavel_anterior_nome,
    ra.matricula as responsavel_anterior_matricula,
    hr.responsavel_novo_id,
    rn.nome as responsavel_novo_nome,
    rn.matricula as responsavel_novo_matricula,
    hr.data_mudanca,
    hr.motivo_mudanca,
    hr.usuario_alteracao_id,
    u.nome_completo as usuario_alteracao_nome,
    hr.observacoes,
    hr.created_at,
    hr.updated_at
FROM TABELA_HISTORICO_RESPONSAVEL hr
LEFT JOIN TABELA_PATRIMONIO p ON hr.patrimonio_id = p.id
LEFT JOIN TABELA_RESPONSAVEL ra ON hr.responsavel_anterior_id = ra.id
LEFT JOIN TABELA_RESPONSAVEL rn ON hr.responsavel_novo_id = rn.id
LEFT JOIN TABELA_USUARIO u ON hr.usuario_alteracao_id = u.id
ORDER BY hr.data_mudanca DESC;

-- View para histórico completo de localizações
CREATE OR REPLACE VIEW vw_historico_localizacao_completo AS
SELECT 
    hl.id,
    hl.patrimonio_id,
    p.numero,
    p.descricao as patrimonio_descricao,
    hl.sala_anterior_id,
    sa.descricao as sala_anterior_nome,
    hl.sala_nova_id,
    sn.descricao as sala_nova_nome,
    hl.setor_anterior_id,
    seta.nome as setor_anterior_nome,
    hl.setor_novo_id,
    setn.nome as setor_novo_nome,
    hl.data_mudanca,
    hl.motivo_mudanca,
    hl.usuario_alteracao_id,
    u.nome_completo as usuario_alteracao_nome,
    hl.observacoes,
    hl.created_at,
    hl.updated_at
FROM TABELA_HISTORICO_LOCALIZACAO hl
LEFT JOIN TABELA_PATRIMONIO p ON hl.patrimonio_id = p.id
LEFT JOIN TABELA_SALA sa ON hl.sala_anterior_id = sa.id_sala
LEFT JOIN TABELA_SALA sn ON hl.sala_nova_id = sn.id_sala
LEFT JOIN TABELA_SETOR seta ON hl.setor_anterior_id = seta.id
LEFT JOIN TABELA_SETOR setn ON hl.setor_novo_id = setn.id
LEFT JOIN TABELA_USUARIO u ON hl.usuario_alteracao_id = u.id
ORDER BY hl.data_mudanca DESC;

-- View para histórico consolidado de um patrimônio
CREATE OR REPLACE VIEW vw_historico_patrimonio_consolidado AS
SELECT 
    p.id as patrimonio_id,
    p.numero,
    p.descricao as patrimonio_descricao,
    'RESPONSAVEL' as tipo_mudanca,
    hr.data_mudanca,
    CONCAT('Responsável: ', 
           COALESCE(ra.nome, 'N/A'), ' → ', 
           COALESCE(rn.nome, 'N/A')) as descricao_mudanca,
    hr.motivo_mudanca,
    u.nome_completo as usuario_alteracao,
    hr.observacoes
FROM TABELA_PATRIMONIO p
INNER JOIN TABELA_HISTORICO_RESPONSAVEL hr ON p.id = hr.patrimonio_id
LEFT JOIN TABELA_RESPONSAVEL ra ON hr.responsavel_anterior_id = ra.id
LEFT JOIN TABELA_RESPONSAVEL rn ON hr.responsavel_novo_id = rn.id
LEFT JOIN TABELA_USUARIO u ON hr.usuario_alteracao_id = u.id

UNION ALL

SELECT 
    p.id as patrimonio_id,
    p.numero,
    p.descricao as patrimonio_descricao,
    'LOCALIZACAO' as tipo_mudanca,
    hl.data_mudanca,
    CONCAT('Localização: ', 
           COALESCE(seta.nome, 'N/A'), '/', COALESCE(sa.descricao, 'N/A'), ' → ', 
           COALESCE(setn.nome, 'N/A'), '/', COALESCE(sn.descricao, 'N/A')) as descricao_mudanca,
    hl.motivo_mudanca,
    u.nome_completo as usuario_alteracao,
    hl.observacoes
FROM TABELA_PATRIMONIO p
INNER JOIN TABELA_HISTORICO_LOCALIZACAO hl ON p.id = hl.patrimonio_id
LEFT JOIN TABELA_SALA sa ON hl.sala_anterior_id = sa.id_sala
LEFT JOIN TABELA_SALA sn ON hl.sala_nova_id = sn.id_sala
LEFT JOIN TABELA_SETOR seta ON hl.setor_anterior_id = seta.id
LEFT JOIN TABELA_SETOR setn ON hl.setor_novo_id = setn.id
LEFT JOIN TABELA_USUARIO u ON hl.usuario_alteracao_id = u.id

ORDER BY patrimonio_id, data_mudanca DESC;

-- View para relatório de movimentações por período
CREATE OR REPLACE VIEW vw_relatorio_movimentacoes AS
SELECT 
    DATE(data_mudanca) as data_movimentacao,
    tipo_mudanca,
    COUNT(*) as total_movimentacoes,
    COUNT(DISTINCT patrimonio_id) as patrimonios_afetados
FROM vw_historico_patrimonio_consolidado
GROUP BY DATE(data_mudanca), tipo_mudanca
ORDER BY data_movimentacao DESC, tipo_mudanca;

-- View para patrimônios com mais movimentações
CREATE OR REPLACE VIEW vw_patrimonios_mais_movimentados AS
SELECT 
    patrimonio_id,
    numero,
    patrimonio_descricao,
    COUNT(*) as total_movimentacoes,
    COUNT(CASE WHEN tipo_mudanca = 'RESPONSAVEL' THEN 1 END) as mudancas_responsavel,
    COUNT(CASE WHEN tipo_mudanca = 'LOCALIZACAO' THEN 1 END) as mudancas_localizacao,
    MAX(data_mudanca) as ultima_movimentacao
FROM vw_historico_patrimonio_consolidado
GROUP BY patrimonio_id, numero, patrimonio_descricao
HAVING COUNT(*) > 0
ORDER BY total_movimentacoes DESC, ultima_movimentacao DESC;

-- View para auditoria de usuários que mais fazem alterações
CREATE OR REPLACE VIEW vw_auditoria_usuarios_alteracoes AS
SELECT 
    usuario_alteracao,
    COUNT(*) as total_alteracoes,
    COUNT(CASE WHEN tipo_mudanca = 'RESPONSAVEL' THEN 1 END) as alteracoes_responsavel,
    COUNT(CASE WHEN tipo_mudanca = 'LOCALIZACAO' THEN 1 END) as alteracoes_localizacao,
    MIN(data_mudanca) as primeira_alteracao,
    MAX(data_mudanca) as ultima_alteracao
FROM vw_historico_patrimonio_consolidado
WHERE usuario_alteracao IS NOT NULL
GROUP BY usuario_alteracao
ORDER BY total_alteracoes DESC;

-- Comentários nas views
COMMENT ON VIEW vw_historico_responsavel_completo IS 'View completa do histórico de mudanças de responsáveis';
COMMENT ON VIEW vw_historico_localizacao_completo IS 'View completa do histórico de mudanças de localização';
COMMENT ON VIEW vw_historico_patrimonio_consolidado IS 'View consolidada de todo o histórico de um patrimônio';
COMMENT ON VIEW vw_relatorio_movimentacoes IS 'Relatório de movimentações por período';
COMMENT ON VIEW vw_patrimonios_mais_movimentados IS 'Patrimônios com maior número de movimentações';
COMMENT ON VIEW vw_auditoria_usuarios_alteracoes IS 'Auditoria de usuários que fazem mais alterações';

PRINT 'Views de histórico criadas com sucesso!';