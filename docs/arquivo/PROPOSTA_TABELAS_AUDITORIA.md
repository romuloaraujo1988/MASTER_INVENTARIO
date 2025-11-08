# Proposta de Tabelas de Auditoria - Sistema de Inventário IFMT

## 1. Introdução

Este documento apresenta uma proposta completa de tabelas de auditoria para o Sistema de Inventário IFMT, visando garantir rastreabilidade, conformidade e segurança das operações realizadas no sistema.

## 2. Objetivos da Auditoria

- **Rastreabilidade**: Registrar todas as alterações realizadas nos dados
- **Conformidade**: Atender requisitos de auditoria interna e externa
- **Segurança**: Detectar acessos não autorizados e operações suspeitas
- **Recuperação**: Possibilitar restauração de dados em caso de problemas
- **Análise**: Gerar relatórios de atividades e estatísticas de uso

## 3. Tabelas de Auditoria Propostas

### 3.1 TABELA_AUDITORIA_GERAL
**Descrição**: Tabela principal de auditoria que registra todas as operações do sistema.

```sql
CREATE TABLE TABELA_AUDITORIA_GERAL (
    ID BIGSERIAL PRIMARY KEY,
    TABELA_AFETADA VARCHAR(100) NOT NULL,
    OPERACAO VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
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
```

### 3.2 TABELA_AUDITORIA_PATRIMONIO
**Descrição**: Auditoria específica para alterações em patrimônios.

```sql
CREATE TABLE TABELA_AUDITORIA_PATRIMONIO (
    ID BIGSERIAL PRIMARY KEY,
    ID_PATRIMONIO INTEGER NOT NULL,
    NUMERO_PATRIMONIO VARCHAR(50),
    OPERACAO VARCHAR(20) NOT NULL,
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
```

### 3.3 TABELA_AUDITORIA_USUARIO
**Descrição**: Auditoria de operações relacionadas a usuários.

```sql
CREATE TABLE TABELA_AUDITORIA_USUARIO (
    ID BIGSERIAL PRIMARY KEY,
    ID_USUARIO_AFETADO INTEGER,
    LOGIN_AFETADO VARCHAR(100),
    OPERACAO VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, CREATE, UPDATE, DELETE, BLOCK, UNBLOCK
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
```

### 3.4 TABELA_AUDITORIA_INVENTARIO
**Descrição**: Auditoria de operações em inventários.

```sql
CREATE TABLE TABELA_AUDITORIA_INVENTARIO (
    ID BIGSERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    NOME_INVENTARIO VARCHAR(255),
    OPERACAO VARCHAR(50) NOT NULL, -- CREATE, UPDATE, START, FINISH, CANCEL
    ID_USUARIO INTEGER,
    LOGIN_USUARIO VARCHAR(100),
    DATA_OPERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    STATUS_ANTERIOR VARCHAR(50),
    STATUS_NOVO VARCHAR(50),
    DADOS_ALTERADOS JSONB,
    OBSERVACOES TEXT,
    IP_ORIGEM INET,
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);
```

### 3.5 TABELA_AUDITORIA_COLETA
**Descrição**: Auditoria de atividades de coleta de dados.

```sql
CREATE TABLE TABELA_AUDITORIA_COLETA (
    ID BIGSERIAL PRIMARY KEY,
    ID_COLETA INTEGER,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_PATRIMONIO INTEGER NOT NULL,
    ID_COLETOR INTEGER NOT NULL,
    OPERACAO VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE
    DATA_OPERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    STATUS_ANTERIOR VARCHAR(50),
    STATUS_NOVO VARCHAR(50),
    LOCALIZACAO_ANTERIOR VARCHAR(255),
    LOCALIZACAO_NOVA VARCHAR(255),
    OBSERVACOES_ANTERIORES TEXT,
    OBSERVACOES_NOVAS TEXT,
    IP_ORIGEM INET,
    DISPOSITIVO_COLETA VARCHAR(255),
    FOREIGN KEY (ID_COLETA) REFERENCES TABELA_COLETA(ID),
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID),
    FOREIGN KEY (ID_COLETOR) REFERENCES TABELA_COLETOR(ID)
);
```

### 3.6 TABELA_AUDITORIA_ACESSO
**Descrição**: Log de acessos e tentativas de acesso ao sistema.

```sql
CREATE TABLE TABELA_AUDITORIA_ACESSO (
    ID BIGSERIAL PRIMARY KEY,
    LOGIN_TENTATIVA VARCHAR(100),
    ID_USUARIO INTEGER,
    TIPO_ACESSO VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, TIMEOUT, FORCE_LOGOUT
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
```

### 3.7 TABELA_AUDITORIA_RELATORIO
**Descrição**: Auditoria de geração e acesso a relatórios.

```sql
CREATE TABLE TABELA_AUDITORIA_RELATORIO (
    ID BIGSERIAL PRIMARY KEY,
    TIPO_RELATORIO VARCHAR(100) NOT NULL,
    PARAMETROS_RELATORIO JSONB,
    ID_USUARIO INTEGER NOT NULL,
    LOGIN_USUARIO VARCHAR(100),
    DATA_GERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TEMPO_PROCESSAMENTO INTERVAL,
    TAMANHO_ARQUIVO BIGINT,
    FORMATO_SAIDA VARCHAR(20), -- PDF, EXCEL, CSV
    STATUS_GERACAO VARCHAR(50), -- SUCESSO, ERRO, CANCELADO
    ERRO_DETALHES TEXT,
    IP_ORIGEM INET,
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);
```

### 3.8 TABELA_AUDITORIA_CONFIGURACAO
**Descrição**: Auditoria de alterações em configurações do sistema.

```sql
CREATE TABLE TABELA_AUDITORIA_CONFIGURACAO (
    ID BIGSERIAL PRIMARY KEY,
    CHAVE_CONFIGURACAO VARCHAR(255) NOT NULL,
    VALOR_ANTERIOR TEXT,
    VALOR_NOVO TEXT,
    ID_USUARIO INTEGER NOT NULL,
    LOGIN_USUARIO VARCHAR(100),
    DATA_ALTERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CATEGORIA_CONFIG VARCHAR(100),
    DESCRICAO_ALTERACAO TEXT,
    IP_ORIGEM INET,
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);
```

## 4. Índices para Performance

```sql
-- Índices para TABELA_AUDITORIA_GERAL
CREATE INDEX idx_auditoria_geral_tabela ON TABELA_AUDITORIA_GERAL(TABELA_AFETADA);
CREATE INDEX idx_auditoria_geral_data ON TABELA_AUDITORIA_GERAL(DATA_OPERACAO);
CREATE INDEX idx_auditoria_geral_usuario ON TABELA_AUDITORIA_GERAL(ID_USUARIO);
CREATE INDEX idx_auditoria_geral_operacao ON TABELA_AUDITORIA_GERAL(OPERACAO);

-- Índices para TABELA_AUDITORIA_PATRIMONIO
CREATE INDEX idx_auditoria_patrimonio_id ON TABELA_AUDITORIA_PATRIMONIO(ID_PATRIMONIO);
CREATE INDEX idx_auditoria_patrimonio_data ON TABELA_AUDITORIA_PATRIMONIO(DATA_ALTERACAO);
CREATE INDEX idx_auditoria_patrimonio_usuario ON TABELA_AUDITORIA_PATRIMONIO(ID_USUARIO);

-- Índices para TABELA_AUDITORIA_USUARIO
CREATE INDEX idx_auditoria_usuario_afetado ON TABELA_AUDITORIA_USUARIO(ID_USUARIO_AFETADO);
CREATE INDEX idx_auditoria_usuario_data ON TABELA_AUDITORIA_USUARIO(DATA_OPERACAO);
CREATE INDEX idx_auditoria_usuario_operacao ON TABELA_AUDITORIA_USUARIO(OPERACAO);

-- Índices para TABELA_AUDITORIA_ACESSO
CREATE INDEX idx_auditoria_acesso_login ON TABELA_AUDITORIA_ACESSO(LOGIN_TENTATIVA);
CREATE INDEX idx_auditoria_acesso_data ON TABELA_AUDITORIA_ACESSO(DATA_TENTATIVA);
CREATE INDEX idx_auditoria_acesso_ip ON TABELA_AUDITORIA_ACESSO(IP_ORIGEM);
CREATE INDEX idx_auditoria_acesso_sucesso ON TABELA_AUDITORIA_ACESSO(SUCESSO);
```

## 5. Triggers de Auditoria

### 5.1 Trigger para TABELA_PATRIMONIO

```sql
CREATE OR REPLACE FUNCTION audit_patrimonio_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO TABELA_AUDITORIA_PATRIMONIO (
            ID_PATRIMONIO, NUMERO_PATRIMONIO, OPERACAO, 
            VALOR_NOVO, ID_USUARIO, LOGIN_USUARIO
        ) VALUES (
            NEW.ID, NEW.NUMERO, 'INSERT',
            row_to_json(NEW)::text, 
            COALESCE(current_setting('app.current_user_id', true)::integer, 0),
            COALESCE(current_setting('app.current_user_login', true), 'SYSTEM')
        );
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO TABELA_AUDITORIA_PATRIMONIO (
            ID_PATRIMONIO, NUMERO_PATRIMONIO, OPERACAO,
            VALOR_ANTERIOR, VALOR_NOVO, ID_USUARIO, LOGIN_USUARIO
        ) VALUES (
            NEW.ID, NEW.NUMERO, 'UPDATE',
            row_to_json(OLD)::text, row_to_json(NEW)::text,
            COALESCE(current_setting('app.current_user_id', true)::integer, 0),
            COALESCE(current_setting('app.current_user_login', true), 'SYSTEM')
        );
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO TABELA_AUDITORIA_PATRIMONIO (
            ID_PATRIMONIO, NUMERO_PATRIMONIO, OPERACAO,
            VALOR_ANTERIOR, ID_USUARIO, LOGIN_USUARIO
        ) VALUES (
            OLD.ID, OLD.NUMERO, 'DELETE',
            row_to_json(OLD)::text,
            COALESCE(current_setting('app.current_user_id', true)::integer, 0),
            COALESCE(current_setting('app.current_user_login', true), 'SYSTEM')
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_audit_patrimonio
    AFTER INSERT OR UPDATE OR DELETE ON TABELA_PATRIMONIO
    FOR EACH ROW EXECUTE FUNCTION audit_patrimonio_changes();
```

## 6. Views de Auditoria

### 6.1 View de Resumo de Atividades

```sql
CREATE OR REPLACE VIEW vw_resumo_auditoria AS
SELECT 
    DATE(ag.DATA_OPERACAO) as data_operacao,
    ag.TABELA_AFETADA,
    ag.OPERACAO,
    COUNT(*) as total_operacoes,
    COUNT(DISTINCT ag.ID_USUARIO) as usuarios_distintos
FROM TABELA_AUDITORIA_GERAL ag
WHERE ag.DATA_OPERACAO >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(ag.DATA_OPERACAO), ag.TABELA_AFETADA, ag.OPERACAO
ORDER BY data_operacao DESC, total_operacoes DESC;
```

### 6.2 View de Acessos Suspeitos

```sql
CREATE OR REPLACE VIEW vw_acessos_suspeitos AS
SELECT 
    aa.LOGIN_TENTATIVA,
    aa.IP_ORIGEM,
    COUNT(*) as tentativas_falha,
    MIN(aa.DATA_TENTATIVA) as primeira_tentativa,
    MAX(aa.DATA_TENTATIVA) as ultima_tentativa
FROM TABELA_AUDITORIA_ACESSO aa
WHERE aa.SUCESSO = FALSE
    AND aa.DATA_TENTATIVA >= CURRENT_DATE - INTERVAL '24 hours'
GROUP BY aa.LOGIN_TENTATIVA, aa.IP_ORIGEM
HAVING COUNT(*) >= 5
ORDER BY tentativas_falha DESC;
```

## 7. Políticas de Retenção

### 7.1 Configuração de Particionamento por Data

```sql
-- Exemplo para TABELA_AUDITORIA_GERAL
CREATE TABLE TABELA_AUDITORIA_GERAL_2024 PARTITION OF TABELA_AUDITORIA_GERAL
FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE TABELA_AUDITORIA_GERAL_2025 PARTITION OF TABELA_AUDITORIA_GERAL
FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
```

### 7.2 Procedimento de Limpeza Automática

```sql
CREATE OR REPLACE FUNCTION limpar_auditoria_antiga()
RETURNS void AS $$
BEGIN
    -- Manter apenas 2 anos de dados de auditoria geral
    DELETE FROM TABELA_AUDITORIA_GERAL 
    WHERE DATA_OPERACAO < CURRENT_DATE - INTERVAL '2 years';
    
    -- Manter apenas 5 anos de dados de auditoria de patrimônio
    DELETE FROM TABELA_AUDITORIA_PATRIMONIO 
    WHERE DATA_ALTERACAO < CURRENT_DATE - INTERVAL '5 years';
    
    -- Manter apenas 1 ano de logs de acesso
    DELETE FROM TABELA_AUDITORIA_ACESSO 
    WHERE DATA_TENTATIVA < CURRENT_DATE - INTERVAL '1 year';
END;
$$ LANGUAGE plpgsql;
```

## 8. Implementação Recomendada

### 8.1 Fase 1 - Básica
1. Implementar TABELA_AUDITORIA_GERAL
2. Implementar TABELA_AUDITORIA_ACESSO
3. Implementar TABELA_AUDITORIA_USUARIO
4. Criar triggers básicos

### 8.2 Fase 2 - Específica
1. Implementar TABELA_AUDITORIA_PATRIMONIO
2. Implementar TABELA_AUDITORIA_INVENTARIO
3. Implementar TABELA_AUDITORIA_COLETA
4. Criar views de relatórios

### 8.3 Fase 3 - Avançada
1. Implementar TABELA_AUDITORIA_RELATORIO
2. Implementar TABELA_AUDITORIA_CONFIGURACAO
3. Configurar particionamento
4. Implementar limpeza automática

## 9. Benefícios Esperados

- **Conformidade**: Atendimento a requisitos de auditoria
- **Segurança**: Detecção de atividades suspeitas
- **Rastreabilidade**: Histórico completo de alterações
- **Recuperação**: Possibilidade de restaurar dados
- **Análise**: Relatórios de uso e performance
- **Governança**: Controle de acesso e operações

## 10. Considerações de Performance

- Usar particionamento por data para tabelas grandes
- Implementar índices apropriados
- Configurar limpeza automática de dados antigos
- Monitorar crescimento das tabelas de auditoria
- Considerar armazenamento em tablespaces separados

---

**Documento criado em**: $(date)
**Versão**: 1.0
**Status**: Proposta para Implementação