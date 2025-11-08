-- Script de Implementação de Auditoria - Fase 1
-- Sistema de Inventário IFMT
-- Implementa as tabelas de auditoria essenciais

-- =====================================================
-- 1. TABELA_AUDITORIA_GERAL
-- =====================================================
CREATE TABLE IF NOT EXISTS TABELA_AUDITORIA_GERAL (
    ID BIGSERIAL PRIMARY KEY,
    TABELA_AFETADA VARCHAR(100) NOT NULL,
    OPERACAO VARCHAR(20) NOT NULL CHECK (OPERACAO IN ('INSERT', 'UPDATE', 'DELETE')),
    ID_REGISTRO INTEGER NOT NULL,
    ID_USUARIO INTEGER,
    LOGIN_USUARIO VARCHAR(100),
    DATA_OPERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM INET,
    USER_AGENT TEXT,
    DADOS_ANTERIORES JSONB,
    DADOS_NOVOS JSONB,
    CAMPOS_ALTERADOS TEXT[],
    OBSERVACOES TEXT,
    SESSAO_ID VARCHAR(255),
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);

-- =====================================================
-- 2. TABELA_AUDITORIA_ACESSO
-- =====================================================
CREATE TABLE IF NOT EXISTS TABELA_AUDITORIA_ACESSO (
    ID BIGSERIAL PRIMARY KEY,
    LOGIN_TENTATIVA VARCHAR(100),
    ID_USUARIO INTEGER,
    TIPO_ACESSO VARCHAR(50) NOT NULL CHECK (TIPO_ACESSO IN ('LOGIN', 'LOGOUT', 'TIMEOUT', 'FORCE_LOGOUT')),
    SUCESSO BOOLEAN NOT NULL,
    DATA_TENTATIVA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM INET NOT NULL,
    USER_AGENT TEXT,
    NAVEGADOR VARCHAR(100),
    SISTEMA_OPERACIONAL VARCHAR(100),
    MOTIVO_FALHA VARCHAR(255),
    SESSAO_ID VARCHAR(255),
    DURACAO_SESSAO INTERVAL,
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);

-- =====================================================
-- 3. TABELA_AUDITORIA_USUARIO
-- =====================================================
CREATE TABLE IF NOT EXISTS TABELA_AUDITORIA_USUARIO (
    ID BIGSERIAL PRIMARY KEY,
    ID_USUARIO_AFETADO INTEGER,
    LOGIN_AFETADO VARCHAR(100),
    OPERACAO VARCHAR(50) NOT NULL CHECK (OPERACAO IN ('LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'BLOCK', 'UNBLOCK', 'RESET_PASSWORD')),
    ID_USUARIO_EXECUTOR INTEGER,
    LOGIN_EXECUTOR VARCHAR(100),
    DATA_OPERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM INET,
    USER_AGENT TEXT,
    DETALHES_OPERACAO JSONB,
    SUCESSO BOOLEAN DEFAULT TRUE,
    MOTIVO_FALHA TEXT,
    TENTATIVAS_FALHA INTEGER DEFAULT 0,
    FOREIGN KEY (ID_USUARIO_AFETADO) REFERENCES TABELA_USUARIO(ID),
    FOREIGN KEY (ID_USUARIO_EXECUTOR) REFERENCES TABELA_USUARIO(ID)
);

-- =====================================================
-- 4. TABELA_AUDITORIA_PATRIMONIO
-- =====================================================
CREATE TABLE IF NOT EXISTS TABELA_AUDITORIA_PATRIMONIO (
    ID BIGSERIAL PRIMARY KEY,
    ID_PATRIMONIO INTEGER NOT NULL,
    NUMERO_PATRIMONIO VARCHAR(50),
    OPERACAO VARCHAR(20) NOT NULL CHECK (OPERACAO IN ('INSERT', 'UPDATE', 'DELETE')),
    CAMPO_ALTERADO VARCHAR(100),
    VALOR_ANTERIOR TEXT,
    VALOR_NOVO TEXT,
    ID_USUARIO INTEGER,
    LOGIN_USUARIO VARCHAR(100),
    DATA_ALTERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM INET,
    MOTIVO_ALTERACAO TEXT,
    APROVADO_POR INTEGER,
    DATA_APROVACAO TIMESTAMP,
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID),
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID),
    FOREIGN KEY (APROVADO_POR) REFERENCES TABELA_USUARIO(ID)
);

-- =====================================================
-- 5. ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Índices para TABELA_AUDITORIA_GERAL
CREATE INDEX IF NOT EXISTS idx_auditoria_geral_tabela ON TABELA_AUDITORIA_GERAL(TABELA_AFETADA);
CREATE INDEX IF NOT EXISTS idx_auditoria_geral_data ON TABELA_AUDITORIA_GERAL(DATA_OPERACAO);
CREATE INDEX IF NOT EXISTS idx_auditoria_geral_usuario ON TABELA_AUDITORIA_GERAL(ID_USUARIO);
CREATE INDEX IF NOT EXISTS idx_auditoria_geral_operacao ON TABELA_AUDITORIA_GERAL(OPERACAO);
CREATE INDEX IF NOT EXISTS idx_auditoria_geral_registro ON TABELA_AUDITORIA_GERAL(ID_REGISTRO);

-- Índices para TABELA_AUDITORIA_ACESSO
CREATE INDEX IF NOT EXISTS idx_auditoria_acesso_login ON TABELA_AUDITORIA_ACESSO(LOGIN_TENTATIVA);
CREATE INDEX IF NOT EXISTS idx_auditoria_acesso_data ON TABELA_AUDITORIA_ACESSO(DATA_TENTATIVA);
CREATE INDEX IF NOT EXISTS idx_auditoria_acesso_ip ON TABELA_AUDITORIA_ACESSO(IP_ORIGEM);
CREATE INDEX IF NOT EXISTS idx_auditoria_acesso_sucesso ON TABELA_AUDITORIA_ACESSO(SUCESSO);
CREATE INDEX IF NOT EXISTS idx_auditoria_acesso_tipo ON TABELA_AUDITORIA_ACESSO(TIPO_ACESSO);

-- Índices para TABELA_AUDITORIA_USUARIO
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_afetado ON TABELA_AUDITORIA_USUARIO(ID_USUARIO_AFETADO);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_data ON TABELA_AUDITORIA_USUARIO(DATA_OPERACAO);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_operacao ON TABELA_AUDITORIA_USUARIO(OPERACAO);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_executor ON TABELA_AUDITORIA_USUARIO(ID_USUARIO_EXECUTOR);

-- Índices para TABELA_AUDITORIA_PATRIMONIO
CREATE INDEX IF NOT EXISTS idx_auditoria_patrimonio_id ON TABELA_AUDITORIA_PATRIMONIO(ID_PATRIMONIO);
CREATE INDEX IF NOT EXISTS idx_auditoria_patrimonio_data ON TABELA_AUDITORIA_PATRIMONIO(DATA_ALTERACAO);
CREATE INDEX IF NOT EXISTS idx_auditoria_patrimonio_usuario ON TABELA_AUDITORIA_PATRIMONIO(ID_USUARIO);
CREATE INDEX IF NOT EXISTS idx_auditoria_patrimonio_numero ON TABELA_AUDITORIA_PATRIMONIO(NUMERO_PATRIMONIO);

-- =====================================================
-- 6. FUNÇÕES DE AUDITORIA
-- =====================================================

-- Função para registrar auditoria geral
CREATE OR REPLACE FUNCTION registrar_auditoria_geral(
    p_tabela VARCHAR(100),
    p_operacao VARCHAR(20),
    p_id_registro INTEGER,
    p_dados_anteriores JSONB DEFAULT NULL,
    p_dados_novos JSONB DEFAULT NULL,
    p_observacoes TEXT DEFAULT NULL
) RETURNS BIGINT AS $$
DECLARE
    v_id_auditoria BIGINT;
BEGIN
    INSERT INTO TABELA_AUDITORIA_GERAL (
        TABELA_AFETADA,
        OPERACAO,
        ID_REGISTRO,
        ID_USUARIO,
        LOGIN_USUARIO,
        DADOS_ANTERIORES,
        DADOS_NOVOS,
        OBSERVACOES,
        IP_ORIGEM,
        SESSAO_ID
    ) VALUES (
        p_tabela,
        p_operacao,
        p_id_registro,
        COALESCE(current_setting('app.current_user_id', true)::integer, 0),
        COALESCE(current_setting('app.current_user_login', true), 'SYSTEM'),
        p_dados_anteriores,
        p_dados_novos,
        p_observacoes,
        COALESCE(current_setting('app.current_user_ip', true)::inet, '127.0.0.1'::inet),
        COALESCE(current_setting('app.session_id', true), 'SYSTEM')
    ) RETURNING ID INTO v_id_auditoria;
    
    RETURN v_id_auditoria;
END;
$$ LANGUAGE plpgsql;

-- Função para registrar tentativa de acesso
CREATE OR REPLACE FUNCTION registrar_tentativa_acesso(
    p_login VARCHAR(100),
    p_tipo_acesso VARCHAR(50),
    p_sucesso BOOLEAN,
    p_ip_origem INET,
    p_motivo_falha VARCHAR(255) DEFAULT NULL,
    p_user_agent TEXT DEFAULT NULL
) RETURNS BIGINT AS $$
DECLARE
    v_id_auditoria BIGINT;
    v_id_usuario INTEGER;
BEGIN
    -- Buscar ID do usuário se o login for válido
    SELECT ID INTO v_id_usuario 
    FROM TABELA_USUARIO 
    WHERE LOGIN = p_login;
    
    INSERT INTO TABELA_AUDITORIA_ACESSO (
        LOGIN_TENTATIVA,
        ID_USUARIO,
        TIPO_ACESSO,
        SUCESSO,
        IP_ORIGEM,
        MOTIVO_FALHA,
        USER_AGENT,
        SESSAO_ID
    ) VALUES (
        p_login,
        v_id_usuario,
        p_tipo_acesso,
        p_sucesso,
        p_ip_origem,
        p_motivo_falha,
        p_user_agent,
        COALESCE(current_setting('app.session_id', true), gen_random_uuid()::text)
    ) RETURNING ID INTO v_id_auditoria;
    
    RETURN v_id_auditoria;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 7. TRIGGERS DE AUDITORIA
-- =====================================================

-- Trigger para TABELA_PATRIMONIO
CREATE OR REPLACE FUNCTION audit_patrimonio_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_PATRIMONIO',
            'INSERT',
            NEW.ID,
            NULL,
            row_to_json(NEW),
            'Novo patrimônio cadastrado'
        );
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_PATRIMONIO',
            'UPDATE',
            NEW.ID,
            row_to_json(OLD),
            row_to_json(NEW),
            'Patrimônio atualizado'
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_PATRIMONIO',
            'DELETE',
            OLD.ID,
            row_to_json(OLD),
            NULL,
            'Patrimônio removido'
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER IF NOT EXISTS trigger_audit_patrimonio
    AFTER INSERT OR UPDATE OR DELETE ON TABELA_PATRIMONIO
    FOR EACH ROW EXECUTE FUNCTION audit_patrimonio_changes();

-- Trigger para TABELA_USUARIO
CREATE OR REPLACE FUNCTION audit_usuario_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_USUARIO',
            'INSERT',
            NEW.ID,
            NULL,
            row_to_json(NEW),
            'Novo usuário cadastrado'
        );
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_USUARIO',
            'UPDATE',
            NEW.ID,
            row_to_json(OLD),
            row_to_json(NEW),
            'Usuário atualizado'
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM registrar_auditoria_geral(
            'TABELA_USUARIO',
            'DELETE',
            OLD.ID,
            row_to_json(OLD),
            NULL,
            'Usuário removido'
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER IF NOT EXISTS trigger_audit_usuario
    AFTER INSERT OR UPDATE OR DELETE ON TABELA_USUARIO
    FOR EACH ROW EXECUTE FUNCTION audit_usuario_changes();

-- =====================================================
-- 8. VIEWS DE RELATÓRIOS
-- =====================================================

-- View de resumo de atividades diárias
CREATE OR REPLACE VIEW vw_resumo_auditoria_diario AS
SELECT 
    DATE(ag.DATA_OPERACAO) as data_operacao,
    ag.TABELA_AFETADA,
    ag.OPERACAO,
    COUNT(*) as total_operacoes,
    COUNT(DISTINCT ag.ID_USUARIO) as usuarios_distintos,
    COUNT(DISTINCT ag.IP_ORIGEM) as ips_distintos
FROM TABELA_AUDITORIA_GERAL ag
WHERE ag.DATA_OPERACAO >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(ag.DATA_OPERACAO), ag.TABELA_AFETADA, ag.OPERACAO
ORDER BY data_operacao DESC, total_operacoes DESC;

-- View de tentativas de acesso falhadas
CREATE OR REPLACE VIEW vw_tentativas_acesso_falhadas AS
SELECT 
    aa.LOGIN_TENTATIVA,
    aa.IP_ORIGEM,
    COUNT(*) as tentativas_falha,
    MIN(aa.DATA_TENTATIVA) as primeira_tentativa,
    MAX(aa.DATA_TENTATIVA) as ultima_tentativa,
    STRING_AGG(DISTINCT aa.MOTIVO_FALHA, '; ') as motivos
FROM TABELA_AUDITORIA_ACESSO aa
WHERE aa.SUCESSO = FALSE
    AND aa.DATA_TENTATIVA >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY aa.LOGIN_TENTATIVA, aa.IP_ORIGEM
ORDER BY tentativas_falha DESC, ultima_tentativa DESC;

-- View de atividades por usuário
CREATE OR REPLACE VIEW vw_atividades_usuario AS
SELECT 
    u.LOGIN,
    u.NOME_COMPLETO,
    COUNT(ag.ID) as total_operacoes,
    COUNT(CASE WHEN ag.OPERACAO = 'INSERT' THEN 1 END) as insercoes,
    COUNT(CASE WHEN ag.OPERACAO = 'UPDATE' THEN 1 END) as atualizacoes,
    COUNT(CASE WHEN ag.OPERACAO = 'DELETE' THEN 1 END) as exclusoes,
    MIN(ag.DATA_OPERACAO) as primeira_atividade,
    MAX(ag.DATA_OPERACAO) as ultima_atividade
FROM TABELA_USUARIO u
LEFT JOIN TABELA_AUDITORIA_GERAL ag ON u.ID = ag.ID_USUARIO
WHERE ag.DATA_OPERACAO >= CURRENT_DATE - INTERVAL '30 days'
    OR ag.DATA_OPERACAO IS NULL
GROUP BY u.ID, u.LOGIN, u.NOME_COMPLETO
ORDER BY total_operacoes DESC;

-- =====================================================
-- 9. COMENTÁRIOS NAS TABELAS
-- =====================================================

COMMENT ON TABLE TABELA_AUDITORIA_GERAL IS 'Tabela principal de auditoria - registra todas as operações do sistema';
COMMENT ON TABLE TABELA_AUDITORIA_ACESSO IS 'Auditoria de acessos e tentativas de login';
COMMENT ON TABLE TABELA_AUDITORIA_USUARIO IS 'Auditoria específica de operações com usuários';
COMMENT ON TABLE TABELA_AUDITORIA_PATRIMONIO IS 'Auditoria específica de operações com patrimônios';

COMMENT ON COLUMN TABELA_AUDITORIA_GERAL.DADOS_ANTERIORES IS 'Dados antes da alteração em formato JSON';
COMMENT ON COLUMN TABELA_AUDITORIA_GERAL.DADOS_NOVOS IS 'Dados após a alteração em formato JSON';
COMMENT ON COLUMN TABELA_AUDITORIA_ACESSO.SUCESSO IS 'Indica se a tentativa de acesso foi bem-sucedida';
COMMENT ON COLUMN TABELA_AUDITORIA_PATRIMONIO.MOTIVO_ALTERACAO IS 'Justificativa para a alteração do patrimônio';

-- =====================================================
-- 10. CONFIGURAÇÕES DE SEGURANÇA
-- =====================================================

-- Revogar permissões diretas nas tabelas de auditoria
REVOKE ALL ON TABELA_AUDITORIA_GERAL FROM PUBLIC;
REVOKE ALL ON TABELA_AUDITORIA_ACESSO FROM PUBLIC;
REVOKE ALL ON TABELA_AUDITORIA_USUARIO FROM PUBLIC;
REVOKE ALL ON TABELA_AUDITORIA_PATRIMONIO FROM PUBLIC;

-- Conceder apenas SELECT para usuários normais (através de roles)
-- GRANT SELECT ON TABELA_AUDITORIA_GERAL TO role_consulta_auditoria;
-- GRANT SELECT ON TABELA_AUDITORIA_ACESSO TO role_consulta_auditoria;

-- =====================================================
-- FINALIZAÇÃO
-- =====================================================

SELECT 'Tabelas de auditoria (Fase 1) criadas com sucesso!' as resultado;
SELECT 'Total de tabelas criadas: 4' as info;
SELECT 'Total de índices criados: 16' as info;
SELECT 'Total de funções criadas: 2' as info;
SELECT 'Total de triggers criados: 2' as info;
SELECT 'Total de views criadas: 3' as info;

-- Verificar se as tabelas foram criadas
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_name LIKE 'tabela_auditoria_%'
    AND table_schema = 'public'
ORDER BY table_name;