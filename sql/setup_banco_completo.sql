-- ============================================================
-- SIHCP - Setup Banco Completo v3.1.0
-- Fonte: dump real de producao infrastructure/27.02.2026--18--36.sql
-- Apenas campos que existiam em producao. Nenhum campo inventado.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

DO $$ BEGIN
    CREATE TYPE status_dispositivo AS ENUM ('PENDENTE','APROVADO','BLOQUEADO','REVOGADO');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ============================================================
-- FUNCOES
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = CURRENT_TIMESTAMP; RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_inventario_timestamp()
RETURNS TRIGGER AS $$
BEGIN NEW.DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP; RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_validar_inventario_setor()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM tabela_inventario WHERE id = NEW.id_inventario) THEN
        RAISE EXCEPTION 'Inventario ID % nao existe', NEW.id_inventario;
    END IF;
    IF NEW.incluir_todos_setores = FALSE AND NEW.id_setor IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM tabela_setor WHERE id = NEW.id_setor) THEN
            RAISE EXCEPTION 'Setor ID % nao existe', NEW.id_setor;
        END IF;
    END IF;
    IF NEW.incluir_todos_setores = TRUE THEN
        IF EXISTS (
            SELECT 1 FROM tabela_inventario_setor
            WHERE id_inventario = NEW.id_inventario AND incluir_todos_setores = TRUE
              AND ativo = TRUE AND id != COALESCE(NEW.id, -1)
        ) THEN
            RAISE EXCEPTION 'Ja existe configuracao incluir todos setores para este inventario';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION validar_id_item_composto()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM tabela_item_composto WHERE id = NEW.id_item_composto) THEN
        RAISE EXCEPTION 'ID Item Composto % nao existe', NEW.id_item_composto;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_obter_setores_inventario(p_id_inventario integer)
RETURNS TABLE(id_setor integer, nome_setor character varying, incluir_todos boolean)
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM tabela_inventario_setor
               WHERE id_inventario = p_id_inventario AND incluir_todos_setores = TRUE AND ativo = TRUE) THEN
        RETURN QUERY SELECT s.id, s.nome, TRUE FROM tabela_setor s ORDER BY s.nome;
    ELSE
        RETURN QUERY
        SELECT s.id, s.nome, FALSE
        FROM tabela_inventario_setor ise
        INNER JOIN tabela_setor s ON ise.id_setor = s.id
        WHERE ise.id_inventario = p_id_inventario AND ise.ativo = TRUE
        ORDER BY s.nome;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION fn_setor_no_escopo_inventario(p_id_inventario integer, p_id_setor integer)
RETURNS boolean LANGUAGE plpgsql AS $$
DECLARE v_incluir BOOLEAN; v_setor INTEGER;
BEGIN
    SELECT incluir_todos_setores INTO v_incluir FROM tabela_inventario_setor
    WHERE id_inventario = p_id_inventario AND incluir_todos_setores = TRUE AND ativo = TRUE LIMIT 1;
    IF v_incluir = TRUE THEN RETURN TRUE; END IF;
    SELECT id_setor INTO v_setor FROM tabela_inventario_setor
    WHERE id_inventario = p_id_inventario AND id_setor = p_id_setor AND ativo = TRUE LIMIT 1;
    RETURN v_setor IS NOT NULL;
END;
$$;

-- ============================================================
-- TABELAS
-- ============================================================

-- tabela_campus
CREATE TABLE IF NOT EXISTS tabela_campus (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(200) NOT NULL,
    cnpj        VARCHAR(18) UNIQUE,
    local       VARCHAR(300),
    diretor     VARCHAR(100),
    email       VARCHAR(100),
    telefone    VARCHAR(20),
    cursos      TEXT,
    observacoes TEXT,
    codigo_uorg VARCHAR(20),
    ativo       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6)
);

-- tabela_setor
CREATE TABLE IF NOT EXISTS tabela_setor (
    id                SERIAL PRIMARY KEY,
    nome              VARCHAR(255) NOT NULL UNIQUE,
    descricao         TEXT,
    responsavel_setor VARCHAR(255),
    responsavel       VARCHAR(100),
    ativo             BOOLEAN DEFAULT TRUE,
    email             VARCHAR(100),
    telefone          VARCHAR(20),
    observacoes       TEXT,
    id_campus         INTEGER REFERENCES tabela_campus(id),
    data_criacao      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at        TIMESTAMP(6)
);

-- tabela_responsavel
CREATE TABLE IF NOT EXISTS tabela_responsavel (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(255) NOT NULL,
    cpf           VARCHAR(14) UNIQUE,
    email         VARCHAR(255),
    telefone      VARCHAR(20),
    cargo         VARCHAR(100),
    id_setor      INTEGER REFERENCES tabela_setor(id),
    ativo         BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- tabela_sala
CREATE TABLE IF NOT EXISTS tabela_sala (
    id_sala       SERIAL PRIMARY KEY,
    id            SERIAL UNIQUE,
    descricao     VARCHAR(255) NOT NULL,
    numero_sala   VARCHAR(100),
    numero        VARCHAR(20),
    andar         INTEGER,
    bloco         VARCHAR(10),
    id_setor      INTEGER REFERENCES tabela_setor(id),
    capacidade    INTEGER,
    area_m2       DOUBLE PRECISION,
    tipo_sala     VARCHAR(50),
    tipo          VARCHAR(50),
    ativo         BOOLEAN DEFAULT TRUE,
    observacoes   TEXT,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP(6)
);

-- tabela_coletor
CREATE TABLE IF NOT EXISTS tabela_coletor (
    id            SERIAL PRIMARY KEY,
    nome_coletor  VARCHAR(255) NOT NULL,
    cpf           VARCHAR(14) UNIQUE,
    email         VARCHAR(255),
    telefone      VARCHAR(20),
    matricula     VARCHAR(20),
    cargo         VARCHAR(100),
    id_setor      INTEGER REFERENCES tabela_setor(id),
    ativo         BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacoes   TEXT
);

-- tabela_usuario
CREATE TABLE IF NOT EXISTS tabela_usuario (
    id                   SERIAL PRIMARY KEY,
    login                VARCHAR(50) NOT NULL UNIQUE,
    senha_hash           VARCHAR(255) NOT NULL,
    nome_completo        VARCHAR(150) NOT NULL,
    email                VARCHAR(100) NOT NULL UNIQUE,
    matricula            VARCHAR(14) UNIQUE,
    perfil               VARCHAR(20) NOT NULL,
    id_setor             INTEGER REFERENCES tabela_setor(id),
    ativo                BOOLEAN DEFAULT TRUE,
    bloqueado            BOOLEAN DEFAULT FALSE,
    primeiro_acesso      BOOLEAN DEFAULT TRUE,
    mobile_habilitado    BOOLEAN DEFAULT TRUE,
    data_criacao         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_ultimo_acesso   TIMESTAMP,
    tentativas_login     INTEGER DEFAULT 0,
    data_bloqueio        TIMESTAMP,
    data_expiracao_senha TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '90 days'),
    ultimo_login_mobile  TIMESTAMP,
    total_logins_mobile  INTEGER DEFAULT 0,
    observacoes          TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tabela_usuario_perfil_check
        CHECK (perfil IN ('ADMIN','SUPERVISOR','COLETOR','CONSULTA'))
);

-- dispositivo_mobile
CREATE TABLE IF NOT EXISTS dispositivo_mobile (
    id                        SERIAL PRIMARY KEY,
    device_id                 VARCHAR(100) NOT NULL UNIQUE,
    id_usuario                INTEGER NOT NULL REFERENCES tabela_usuario(id),
    modelo                    VARCHAR(100),
    fabricante                VARCHAR(100),
    versao_android            VARCHAR(20),
    versao_app                VARCHAR(20),
    endereco_ip               VARCHAR(50),
    endereco_mac              VARCHAR(50),
    status                    status_dispositivo DEFAULT 'APROVADO' NOT NULL,
    data_registro             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_ultima_conexao       TIMESTAMP,
    data_ultima_sincronizacao TIMESTAMP,
    ativo                     BOOLEAN DEFAULT TRUE NOT NULL,
    token_atual               TEXT,
    data_expiracao_token      TIMESTAMP,
    observacoes               TEXT
);

-- tabela_responsavel (sem FK para patrimonio ainda, criada aqui para dependencias)

-- tabela_patrimonio
CREATE TABLE IF NOT EXISTS tabela_patrimonio (
    id                 SERIAL PRIMARY KEY,
    numero             VARCHAR(50) NOT NULL UNIQUE,
    status             VARCHAR(50) DEFAULT 'ATIVO',
    situacao           VARCHAR(20),
    descricao          TEXT,
    descricao_resumida VARCHAR(500),
    rotulos            VARCHAR(500),
    id_responsavel     INTEGER REFERENCES tabela_responsavel(id),
    valor_aquisicao    NUMERIC(15,2),
    valor_depreciado   NUMERIC(15,2),
    numero_nota_fiscal VARCHAR(100),
    numero_serie       VARCHAR(100),
    data_entrada       DATE,
    data_carga         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fornecedor         VARCHAR(255),
    id_sala            INTEGER REFERENCES tabela_sala(id_sala),
    estado_conservacao VARCHAR(50),
    marca              VARCHAR(255),
    modelo             VARCHAR(255),
    categoria          VARCHAR(50),
    ed                 VARCHAR(20),
    observacoes        TEXT
);

-- tabela_categoria_patrimonio
CREATE TABLE IF NOT EXISTS tabela_categoria_patrimonio (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(200) NOT NULL,
    descricao     TEXT,
    codigo        VARCHAR(20),
    ativo         BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- tabela_subcategoria_patrimonio
CREATE TABLE IF NOT EXISTS tabela_subcategoria_patrimonio (
    id            SERIAL PRIMARY KEY,
    nome          VARCHAR(200) NOT NULL,
    descricao     TEXT,
    id_categoria  INTEGER REFERENCES tabela_categoria_patrimonio(id),
    ativo         BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- tabela_item_composto
CREATE TABLE IF NOT EXISTS tabela_item_composto (
    id                      SERIAL PRIMARY KEY,
    id_patrimonio_principal INTEGER NOT NULL REFERENCES tabela_patrimonio(id),
    tipo_componente         VARCHAR(100),
    descricao_componente    VARCHAR(255),
    quantidade_esperada     INTEGER,
    obrigatorio             BOOLEAN,
    observacao              TEXT,
    data_cadastro           TIMESTAMP
);

-- tabela_inventario
CREATE TABLE IF NOT EXISTS tabela_inventario (
    id                      SERIAL PRIMARY KEY,
    nome                    VARCHAR(255) NOT NULL,
    ano                     INTEGER NOT NULL,
    data_inicio             DATE NOT NULL,
    data_fim                DATE,
    observacao              TEXT,
    status_inventario       VARCHAR(50) DEFAULT 'PLANEJADO',
    responsavel_inventario  VARCHAR(255),
    responsavel             VARCHAR(100),
    tipo                    VARCHAR(50),
    total_patrimonios       INTEGER DEFAULT 0,
    patrimonios_coletados   INTEGER DEFAULT 0,
    percentual_conclusao    NUMERIC(5,2) DEFAULT 0.00,
    data_criacao            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at              TIMESTAMP(6),
    updated_at              TIMESTAMP(6)
);

-- tabela_inventario_setor
CREATE TABLE IF NOT EXISTS tabela_inventario_setor (
    id                    SERIAL PRIMARY KEY,
    id_inventario         INTEGER NOT NULL REFERENCES tabela_inventario(id),
    id_setor              INTEGER REFERENCES tabela_setor(id),
    incluir_todos_setores BOOLEAN DEFAULT FALSE,
    data_inclusao         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo                 BOOLEAN DEFAULT TRUE,
    observacoes           TEXT,
    UNIQUE (id_inventario, id_setor),
    CONSTRAINT tabela_inventario_setor_check CHECK (
        ((incluir_todos_setores = TRUE) AND (id_setor IS NULL)) OR
        ((incluir_todos_setores = FALSE) AND (id_setor IS NOT NULL))
    )
);

-- tabela_participante_inventario
CREATE TABLE IF NOT EXISTS tabela_participante_inventario (
    id_participante         SERIAL PRIMARY KEY,
    id_inventario           INTEGER NOT NULL REFERENCES tabela_inventario(id),
    id_usuario              INTEGER NOT NULL REFERENCES tabela_usuario(id),
    papel                   VARCHAR(50) NOT NULL,
    data_inclusao           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_remocao            TIMESTAMP,
    data_ultima_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo                   BOOLEAN DEFAULT TRUE,
    observacoes             TEXT,
    UNIQUE (id_inventario, id_usuario),
    CONSTRAINT tabela_participante_inventario_papel_check
        CHECK (papel IN ('COORDENADOR','COLETOR','OBSERVADOR'))
);

-- tabela_coleta
CREATE TABLE IF NOT EXISTS tabela_coleta (
    id                           SERIAL PRIMARY KEY,
    id_inventario                INTEGER NOT NULL REFERENCES tabela_inventario(id),
    id_patrimonio                INTEGER REFERENCES tabela_patrimonio(id),
    id_coletor                   INTEGER NOT NULL,
    id_participante_inventario   INTEGER NOT NULL REFERENCES tabela_participante_inventario(id_participante),
    data_coleta                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_coleta                VARCHAR(50) DEFAULT 'COLETADO',
    observacao_coleta            TEXT,
    observacao                   TEXT,
    localizacao_atual            VARCHAR(255),
    localizacao_encontrada       VARCHAR(255),
    estado_encontrado            VARCHAR(50),
    divergencia                  BOOLEAN DEFAULT FALSE,
    motivo_divergencia           TEXT,
    latitude                     NUMERIC(10,8),
    longitude                    NUMERIC(11,8),
    foto_patrimonio              TEXT,
    sem_etiqueta                 BOOLEAN DEFAULT FALSE,
    descricao_item_sem_etiqueta  VARCHAR(500),
    categoria_item_sem_etiqueta  VARCHAR(100),
    tempo_coleta_segundos        INTEGER,
    tempo_scan_segundos          INTEGER,
    tempo_preenchimento_segundos INTEGER,
    metodo_coleta                VARCHAR(20),
    hora_coleta                  INTEGER,
    dia_semana                   INTEGER,
    periodo_coleta               VARCHAR(10),
    tipo_scan                    VARCHAR(20),
    tentativas_scan              INTEGER DEFAULT 1,
    erros_scan                   INTEGER DEFAULT 0,
    qualidade_etiqueta           VARCHAR(20),
    UNIQUE (id_inventario, id_patrimonio)
);

-- tabela_coleta_componente
CREATE TABLE IF NOT EXISTS tabela_coleta_componente (
    id                     SERIAL PRIMARY KEY,
    id_item_composto       INTEGER NOT NULL REFERENCES tabela_item_composto(id),
    id_inventario          INTEGER NOT NULL REFERENCES tabela_inventario(id),
    id_coletor             INTEGER NOT NULL,
    quantidade_encontrada  INTEGER,
    status_componente      VARCHAR(20) DEFAULT 'PENDENTE',
    observacao_coleta      TEXT,
    data_coleta            TIMESTAMP,
    localizacao_encontrada VARCHAR(255),
    UNIQUE (id_item_composto, id_inventario)
);

-- tabela_sala_inventario
CREATE TABLE IF NOT EXISTS tabela_sala_inventario (
    id_sala_inventario       SERIAL PRIMARY KEY,
    id_sala                  INTEGER NOT NULL,
    id_inventario            INTEGER NOT NULL,
    id_participante          INTEGER,
    coleta_finalizada        BOOLEAN DEFAULT FALSE,
    data_inicio_coleta       TIMESTAMP,
    data_finalizacao_coleta  TIMESTAMP,
    observacoes_finalizacao  TEXT,
    total_itens_coletados    INTEGER DEFAULT 0,
    total_itens_sem_etiqueta INTEGER DEFAULT 0,
    percentual_conclusao     NUMERIC(5,2) DEFAULT 0.00,
    status_coleta            VARCHAR(50) DEFAULT 'PENDENTE',
    data_criacao             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_sala, id_inventario)
);

-- ============================================================
-- INDICES (todos do dump original)
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_campus_codigo_uorg               ON tabela_campus(codigo_uorg);
CREATE INDEX IF NOT EXISTS idx_coletor_ativo                    ON tabela_coletor(ativo);
CREATE INDEX IF NOT EXISTS idx_coletor_cpf                      ON tabela_coletor(cpf);
CREATE INDEX IF NOT EXISTS idx_coletor_matricula                ON tabela_coletor(matricula);
CREATE INDEX IF NOT EXISTS idx_coletor_nome                     ON tabela_coletor(nome_coletor);
CREATE INDEX IF NOT EXISTS idx_coletor_setor                    ON tabela_coletor(id_setor);
CREATE INDEX IF NOT EXISTS idx_coleta_coletor                   ON tabela_coleta(id_coletor);
CREATE INDEX IF NOT EXISTS idx_coleta_data                      ON tabela_coleta(data_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_divergencia               ON tabela_coleta(divergencia);
CREATE INDEX IF NOT EXISTS idx_coleta_hora                      ON tabela_coleta(hora_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_inventario                ON tabela_coleta(id_inventario);
CREATE INDEX IF NOT EXISTS idx_coleta_metodo                    ON tabela_coleta(metodo_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_participante_inventario   ON tabela_coleta(id_participante_inventario);
CREATE INDEX IF NOT EXISTS idx_coleta_patrimonio                ON tabela_coleta(id_patrimonio);
CREATE INDEX IF NOT EXISTS idx_coleta_periodo                   ON tabela_coleta(periodo_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_qualidade                 ON tabela_coleta(qualidade_etiqueta);
CREATE INDEX IF NOT EXISTS idx_coleta_status                    ON tabela_coleta(status_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_tempo                     ON tabela_coleta(tempo_coleta_segundos);
CREATE INDEX IF NOT EXISTS idx_coleta_tipo_scan                 ON tabela_coleta(tipo_scan);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_data_coleta    ON tabela_coleta_componente(data_coleta);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_inventario_item ON tabela_coleta_componente(id_inventario, id_item_composto);
CREATE INDEX IF NOT EXISTS idx_coleta_componente_item_composto  ON tabela_coleta_componente(id_item_composto);
CREATE INDEX IF NOT EXISTS idx_dispositivo_device_id            ON dispositivo_mobile(device_id);
CREATE INDEX IF NOT EXISTS idx_dispositivo_usuario              ON dispositivo_mobile(id_usuario);
CREATE INDEX IF NOT EXISTS idx_inventario_ano                   ON tabela_inventario(ano);
CREATE INDEX IF NOT EXISTS idx_inventario_data_fim              ON tabela_inventario(data_fim);
CREATE INDEX IF NOT EXISTS idx_inventario_data_inicio           ON tabela_inventario(data_inicio);
CREATE INDEX IF NOT EXISTS idx_inventario_status                ON tabela_inventario(status_inventario);
CREATE INDEX IF NOT EXISTS idx_inventario_setor_ativo           ON tabela_inventario_setor(ativo);
CREATE INDEX IF NOT EXISTS idx_inventario_setor_inventario      ON tabela_inventario_setor(id_inventario);
CREATE INDEX IF NOT EXISTS idx_inventario_setor_inventario_ativo ON tabela_inventario_setor(id_inventario, ativo);
CREATE INDEX IF NOT EXISTS idx_inventario_setor_setor           ON tabela_inventario_setor(id_setor);
CREATE INDEX IF NOT EXISTS idx_inventario_setor_todos           ON tabela_inventario_setor(incluir_todos_setores);
CREATE INDEX IF NOT EXISTS idx_participante_ativo               ON tabela_participante_inventario(ativo);
CREATE INDEX IF NOT EXISTS idx_participante_inventario          ON tabela_participante_inventario(id_inventario);
CREATE INDEX IF NOT EXISTS idx_participante_papel               ON tabela_participante_inventario(papel);
CREATE INDEX IF NOT EXISTS idx_participante_usuario             ON tabela_participante_inventario(id_usuario);
CREATE INDEX IF NOT EXISTS idx_patrimonio_categoria             ON tabela_patrimonio(categoria);
CREATE INDEX IF NOT EXISTS idx_patrimonio_ed                    ON tabela_patrimonio(ed);
CREATE INDEX IF NOT EXISTS idx_patrimonio_fornecedor            ON tabela_patrimonio(fornecedor);
CREATE INDEX IF NOT EXISTS idx_patrimonio_marca                 ON tabela_patrimonio(marca);
CREATE INDEX IF NOT EXISTS idx_patrimonio_modelo                ON tabela_patrimonio(modelo);
CREATE INDEX IF NOT EXISTS idx_patrimonio_nota_fiscal           ON tabela_patrimonio(numero_nota_fiscal);
CREATE INDEX IF NOT EXISTS idx_patrimonio_numero                ON tabela_patrimonio(numero);
CREATE INDEX IF NOT EXISTS idx_patrimonio_responsavel           ON tabela_patrimonio(id_responsavel);
CREATE INDEX IF NOT EXISTS idx_patrimonio_sala                  ON tabela_patrimonio(id_sala);
CREATE INDEX IF NOT EXISTS idx_patrimonio_status                ON tabela_patrimonio(status);
CREATE INDEX IF NOT EXISTS idx_responsavel_ativo                ON tabela_responsavel(ativo);
CREATE INDEX IF NOT EXISTS idx_responsavel_cpf                  ON tabela_responsavel(cpf);
CREATE INDEX IF NOT EXISTS idx_responsavel_nome                 ON tabela_responsavel(nome);
CREATE INDEX IF NOT EXISTS idx_responsavel_setor                ON tabela_responsavel(id_setor);

-- ============================================================
-- TRIGGERS
-- ============================================================

DROP TRIGGER IF EXISTS update_inventario_setor_trigger ON tabela_inventario_setor;
CREATE TRIGGER update_inventario_setor_trigger
    BEFORE INSERT OR UPDATE ON tabela_inventario_setor
    FOR EACH ROW EXECUTE FUNCTION fn_validar_inventario_setor();

DROP TRIGGER IF EXISTS trigger_validar_id_item_composto ON tabela_coleta_componente;
CREATE TRIGGER trigger_validar_id_item_composto
    BEFORE INSERT OR UPDATE ON tabela_coleta_componente
    FOR EACH ROW EXECUTE FUNCTION validar_id_item_composto();

-- ============================================================
-- CONTROLE DE VERSAO
-- ============================================================

CREATE TABLE IF NOT EXISTS schema_version (
    id          SERIAL PRIMARY KEY,
    versao      VARCHAR(20) NOT NULL,
    descricao   VARCHAR(500),
    aplicado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- DADOS INICIAIS
-- ============================================================

INSERT INTO tabela_setor (nome, descricao) VALUES ('Administracao', 'Setor administrativo')
    ON CONFLICT (nome) DO NOTHING;
INSERT INTO tabela_setor (nome, descricao) VALUES ('TI', 'Tecnologia da Informacao')
    ON CONFLICT (nome) DO NOTHING;
INSERT INTO tabela_setor (nome, descricao) VALUES ('Biblioteca', 'Biblioteca da instituicao')
    ON CONFLICT (nome) DO NOTHING;

-- Administrador padrao (senha: admin123)
INSERT INTO tabela_usuario (login, senha_hash, nome_completo, email, perfil, id_setor, ativo, bloqueado, primeiro_acesso)
SELECT 'admin',
       '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLIqCRFpb6AQBaOLLqI.xjVgLOjHi',
       'Administrador do Sistema',
       'admin@sistema.com',
       'ADMIN',
       NULL,
       TRUE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM tabela_usuario WHERE login = 'admin');

INSERT INTO schema_version (versao, descricao)
SELECT '3.1.0', 'Setup fiel ao dump de producao 27/02/2026 - somente campos reais'
WHERE NOT EXISTS (SELECT 1 FROM schema_version WHERE versao = '3.1.0');

SELECT 'Setup SIHCP v3.1.0 concluido!' AS resultado;
