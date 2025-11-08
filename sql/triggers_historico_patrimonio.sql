-- =====================================================
-- TRIGGERS PARA CAPTURA AUTOMÁTICA DE HISTÓRICO
-- Sistema de Inventário Patrimonial
-- =====================================================

-- Função para registrar mudanças de responsável
CREATE OR REPLACE FUNCTION registrar_mudanca_responsavel()
RETURNS TRIGGER AS $$
BEGIN
    -- Verifica se houve mudança no responsável
    IF OLD.id_responsavel IS DISTINCT FROM NEW.id_responsavel THEN
        INSERT INTO TABELA_HISTORICO_RESPONSAVEL (
            patrimonio_id,
            responsavel_anterior_id,
            responsavel_novo_id,
            data_mudanca,
            motivo_mudanca,
            usuario_alteracao_id,
            observacoes
        ) VALUES (
            NEW.id,
            OLD.id_responsavel,
            NEW.id_responsavel,
            CURRENT_TIMESTAMP,
            'Alteração automática via sistema',
            1, -- ID do usuário admin (pode ser parametrizado)
            CONCAT('Responsável alterado de ID ', COALESCE(OLD.id_responsavel::text, 'NULL'), 
                   ' para ID ', COALESCE(NEW.id_responsavel::text, 'NULL'))
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Função para registrar mudanças de localização
CREATE OR REPLACE FUNCTION registrar_mudanca_localizacao()
RETURNS TRIGGER AS $$
BEGIN
    -- Verifica se houve mudança na sala
    IF OLD.id_sala IS DISTINCT FROM NEW.id_sala THEN
        
        INSERT INTO TABELA_HISTORICO_LOCALIZACAO (
            patrimonio_id,
            sala_anterior_id,
            sala_nova_id,
            setor_anterior_id,
            setor_novo_id,
            data_mudanca,
            motivo_mudanca,
            usuario_alteracao_id,
            observacoes
        ) VALUES (
            NEW.id,
            OLD.id_sala,
            NEW.id_sala,
            NULL, -- Setor anterior (não implementado)
            NULL, -- Setor novo (não implementado)
            CURRENT_TIMESTAMP,
            'Alteração automática via sistema',
            1, -- ID do usuário admin (pode ser parametrizado)
            CONCAT('Localização alterada - Sala: ', COALESCE(OLD.id_sala::text, 'NULL'), 
                   ' → ', COALESCE(NEW.id_sala::text, 'NULL'))
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para capturar mudanças de responsável
DROP TRIGGER IF EXISTS trigger_historico_responsavel ON TABELA_PATRIMONIO;
CREATE TRIGGER trigger_historico_responsavel
    AFTER UPDATE ON TABELA_PATRIMONIO
    FOR EACH ROW
    EXECUTE FUNCTION registrar_mudanca_responsavel();

-- Trigger para capturar mudanças de localização
DROP TRIGGER IF EXISTS trigger_historico_localizacao ON TABELA_PATRIMONIO;
CREATE TRIGGER trigger_historico_localizacao
    AFTER UPDATE ON TABELA_PATRIMONIO
    FOR EACH ROW
    EXECUTE FUNCTION registrar_mudanca_localizacao();

-- Função para registrar histórico manual (para uso em aplicações)
CREATE OR REPLACE FUNCTION registrar_historico_manual(
    p_patrimonio_id INTEGER,
    p_tipo_mudanca VARCHAR(20), -- 'responsavel' ou 'localizacao'
    p_responsavel_anterior_id INTEGER DEFAULT NULL,
    p_responsavel_novo_id INTEGER DEFAULT NULL,
    p_sala_anterior_id INTEGER DEFAULT NULL,
    p_sala_nova_id INTEGER DEFAULT NULL,
    p_setor_anterior_id INTEGER DEFAULT NULL,
    p_setor_novo_id INTEGER DEFAULT NULL,
    p_motivo VARCHAR(500) DEFAULT NULL,
    p_usuario_id INTEGER DEFAULT 1,
    p_observacoes TEXT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
BEGIN
    IF p_tipo_mudanca = 'responsavel' THEN
        INSERT INTO TABELA_HISTORICO_RESPONSAVEL (
            patrimonio_id,
            responsavel_anterior_id,
            responsavel_novo_id,
            data_mudanca,
            motivo_mudanca,
            usuario_alteracao_id,
            observacoes
        ) VALUES (
            p_patrimonio_id,
            p_responsavel_anterior_id,
            p_responsavel_novo_id,
            CURRENT_TIMESTAMP,
            COALESCE(p_motivo, 'Registro manual'),
            p_usuario_id,
            p_observacoes
        );
        
    ELSIF p_tipo_mudanca = 'localizacao' THEN
        INSERT INTO TABELA_HISTORICO_LOCALIZACAO (
            patrimonio_id,
            sala_anterior_id,
            sala_nova_id,
            setor_anterior_id,
            setor_novo_id,
            data_mudanca,
            motivo_mudanca,
            usuario_alteracao_id,
            observacoes
        ) VALUES (
            p_patrimonio_id,
            p_sala_anterior_id,
            p_sala_nova_id,
            p_setor_anterior_id,
            p_setor_novo_id,
            CURRENT_TIMESTAMP,
            COALESCE(p_motivo, 'Registro manual'),
            p_usuario_id,
            p_observacoes
        );
    ELSE
        RAISE EXCEPTION 'Tipo de mudança inválido: %. Use "responsavel" ou "localizacao"', p_tipo_mudanca;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Triggers de histórico criados com sucesso!