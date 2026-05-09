--
-- PostgreSQL database dump
--

\restrict d5Hy3vU4lGRdOOLFSmb8a8SQfLeNrCrU27bsBZu1Z2EdaxfhDCxXvGFrAcM1trZ

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: status_dispositivo; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_dispositivo AS ENUM (
    'PENDENTE',
    'APROVADO',
    'BLOQUEADO',
    'REVOGADO'
);


ALTER TYPE public.status_dispositivo OWNER TO postgres;

--
-- Name: fn_obter_setores_inventario(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_obter_setores_inventario(p_id_inventario integer) RETURNS TABLE(id_setor integer, nome_setor character varying, incluir_todos boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Verificar se inclui todos os setores
    IF EXISTS (
        SELECT 1 FROM TABELA_INVENTARIO_SETOR 
        WHERE ID_INVENTARIO = p_id_inventario 
          AND INCLUIR_TODOS_SETORES = TRUE 
          AND ATIVO = TRUE
    ) THEN
        -- Retornar todos os setores
        RETURN QUERY
        SELECT s.ID, s.NOME, TRUE as incluir_todos
        FROM TABELA_SETOR s
        ORDER BY s.NOME;
    ELSE
        -- Retornar apenas setores específicos
        RETURN QUERY
        SELECT s.ID, s.NOME, FALSE as incluir_todos
        FROM TABELA_INVENTARIO_SETOR ise
        INNER JOIN TABELA_SETOR s ON ise.ID_SETOR = s.ID
        WHERE ise.ID_INVENTARIO = p_id_inventario 
          AND ise.ATIVO = TRUE
        ORDER BY s.NOME;
    END IF;
END;
$$;


ALTER FUNCTION public.fn_obter_setores_inventario(p_id_inventario integer) OWNER TO postgres;

--
-- Name: FUNCTION fn_obter_setores_inventario(p_id_inventario integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.fn_obter_setores_inventario(p_id_inventario integer) IS 'Função que retorna os setores de um inventário';


--
-- Name: fn_setor_no_escopo_inventario(integer, integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_setor_no_escopo_inventario(p_id_inventario integer, p_id_setor integer) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_incluir_todos BOOLEAN;
    v_setor_especifico INTEGER;
BEGIN
    -- Verificar se inclui todos os setores
    SELECT INCLUIR_TODOS_SETORES INTO v_incluir_todos
    FROM TABELA_INVENTARIO_SETOR
    WHERE ID_INVENTARIO = p_id_inventario 
      AND INCLUIR_TODOS_SETORES = TRUE 
      AND ATIVO = TRUE
    LIMIT 1;
    
    IF v_incluir_todos = TRUE THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar se o setor específico está selecionado
    SELECT ID_SETOR INTO v_setor_especifico
    FROM TABELA_INVENTARIO_SETOR
    WHERE ID_INVENTARIO = p_id_inventario 
      AND ID_SETOR = p_id_setor 
      AND ATIVO = TRUE
    LIMIT 1;
    
    RETURN v_setor_especifico IS NOT NULL;
END;
$$;


ALTER FUNCTION public.fn_setor_no_escopo_inventario(p_id_inventario integer, p_id_setor integer) OWNER TO postgres;

--
-- Name: FUNCTION fn_setor_no_escopo_inventario(p_id_inventario integer, p_id_setor integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.fn_setor_no_escopo_inventario(p_id_inventario integer, p_id_setor integer) IS 'Função que verifica se um setor está no escopo de um inventário';


--
-- Name: fn_validar_inventario_setor(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.fn_validar_inventario_setor() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Validar se inventário existe
    IF NOT EXISTS (SELECT 1 FROM TABELA_INVENTARIO WHERE ID = NEW.ID_INVENTARIO) THEN
        RAISE EXCEPTION 'Inventário com ID % não existe', NEW.ID_INVENTARIO;
    END IF;
    
    -- Validar se setor existe (quando não for incluir todos)
    IF NEW.INCLUIR_TODOS_SETORES = FALSE AND NEW.ID_SETOR IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM TABELA_SETOR WHERE ID = NEW.ID_SETOR) THEN
            RAISE EXCEPTION 'Setor com ID % não existe', NEW.ID_SETOR;
        END IF;
    END IF;
    
    -- Validar se já existe configuração "incluir todos" para este inventário
    IF NEW.INCLUIR_TODOS_SETORES = TRUE THEN
        IF EXISTS (
            SELECT 1 FROM TABELA_INVENTARIO_SETOR 
            WHERE ID_INVENTARIO = NEW.ID_INVENTARIO 
              AND INCLUIR_TODOS_SETORES = TRUE 
              AND ATIVO = TRUE
              AND ID != COALESCE(NEW.ID, -1)
        ) THEN
            RAISE EXCEPTION 'Já existe configuração "incluir todos setores" para este inventário';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.fn_validar_inventario_setor() OWNER TO postgres;

--
-- Name: update_inventario_timestamp(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_inventario_timestamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_inventario_timestamp() OWNER TO postgres;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO postgres;

--
-- Name: validar_id_item_composto(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.validar_id_item_composto() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM tabela_item_composto 
        WHERE id = NEW.id_item_composto
    ) THEN
        RAISE EXCEPTION 'ID Item Composto % não existe na tabela_item_composto', 
            NEW.id_item_composto;
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.validar_id_item_composto() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: backup_coleta_componente_20260211; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backup_coleta_componente_20260211 (
    id integer,
    id_item_composto integer,
    id_inventario integer,
    id_coletor integer,
    quantidade_encontrada integer,
    status_componente character varying(20),
    observacao_coleta text,
    data_coleta timestamp without time zone,
    localizacao_encontrada character varying(255)
);


ALTER TABLE public.backup_coleta_componente_20260211 OWNER TO postgres;

--
-- Name: backup_participante_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backup_participante_inventario (
    id_participante integer,
    id_inventario integer,
    id_usuario integer,
    papel character varying(20),
    data_inclusao timestamp without time zone,
    data_remocao timestamp without time zone,
    ativo boolean,
    observacoes text,
    data_ultima_atualizacao timestamp without time zone
);


ALTER TABLE public.backup_participante_inventario OWNER TO postgres;

--
-- Name: backup_tabela_coleta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backup_tabela_coleta (
    id integer,
    id_inventario integer,
    id_patrimonio integer,
    id_coletor integer,
    data_coleta timestamp without time zone,
    status_coleta character varying(50),
    observacao_coleta text,
    localizacao_atual character varying(255),
    localizacao_encontrada character varying(255),
    estado_encontrado character varying(50),
    divergencia boolean,
    motivo_divergencia text,
    latitude numeric(10,8),
    longitude numeric(11,8),
    foto_patrimonio text,
    sem_etiqueta boolean,
    descricao_item_sem_etiqueta character varying(500),
    categoria_item_sem_etiqueta character varying(100),
    id_participante_inventario integer
);


ALTER TABLE public.backup_tabela_coleta OWNER TO postgres;

--
-- Name: dispositivo_mobile; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dispositivo_mobile (
    id integer NOT NULL,
    device_id character varying(100) NOT NULL,
    id_usuario integer NOT NULL,
    modelo character varying(100),
    fabricante character varying(100),
    versao_android character varying(20),
    versao_app character varying(20),
    endereco_ip character varying(50),
    endereco_mac character varying(50),
    status public.status_dispositivo DEFAULT 'APROVADO'::public.status_dispositivo NOT NULL,
    data_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_ultima_conexao timestamp without time zone,
    data_ultima_sincronizacao timestamp without time zone,
    ativo boolean DEFAULT true NOT NULL,
    token_atual text,
    data_expiracao_token timestamp without time zone,
    observacoes text
);


ALTER TABLE public.dispositivo_mobile OWNER TO postgres;

--
-- Name: dispositivo_mobile_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dispositivo_mobile_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.dispositivo_mobile_id_seq OWNER TO postgres;

--
-- Name: dispositivo_mobile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dispositivo_mobile_id_seq OWNED BY public.dispositivo_mobile.id;


--
-- Name: log_remocao_orfaos_20260211; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.log_remocao_orfaos_20260211 (
    id integer NOT NULL,
    id_coleta_componente integer,
    id_item_composto integer,
    id_inventario integer,
    id_coletor integer,
    data_coleta timestamp without time zone,
    data_remocao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    motivo character varying DEFAULT 'REGISTRO ÓRFÃO - FK VIOLATION'::character varying
);


ALTER TABLE public.log_remocao_orfaos_20260211 OWNER TO postgres;

--
-- Name: log_remocao_orfaos_20260211_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.log_remocao_orfaos_20260211_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.log_remocao_orfaos_20260211_id_seq OWNER TO postgres;

--
-- Name: log_remocao_orfaos_20260211_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.log_remocao_orfaos_20260211_id_seq OWNED BY public.log_remocao_orfaos_20260211.id;


--
-- Name: tabela_campus; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_campus (
    id integer NOT NULL,
    ativo boolean NOT NULL,
    cnpj character varying(18),
    cursos text,
    created_at timestamp(6) without time zone,
    diretor character varying(100),
    email character varying(100),
    local character varying(300),
    nome character varying(200) NOT NULL,
    observacoes text,
    telefone character varying(20),
    codigo_uorg character varying(20)
);


ALTER TABLE public.tabela_campus OWNER TO postgres;

--
-- Name: COLUMN tabela_campus.codigo_uorg; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_campus.codigo_uorg IS 'Código da Unidade Organizacional no SIADS';


--
-- Name: tabela_campus_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_campus_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_campus_id_seq OWNER TO postgres;

--
-- Name: tabela_campus_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_campus_id_seq OWNED BY public.tabela_campus.id;


--
-- Name: tabela_categoria_patrimonio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_categoria_patrimonio (
    id integer NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    codigo character varying(20),
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tabela_categoria_patrimonio OWNER TO postgres;

--
-- Name: tabela_categoria_patrimonio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_categoria_patrimonio_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_categoria_patrimonio_id_seq OWNER TO postgres;

--
-- Name: tabela_categoria_patrimonio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_categoria_patrimonio_id_seq OWNED BY public.tabela_categoria_patrimonio.id;


--
-- Name: tabela_coleta; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_coleta (
    id integer NOT NULL,
    id_inventario integer NOT NULL,
    id_patrimonio integer,
    id_coletor integer NOT NULL,
    data_coleta timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    status_coleta character varying(50) DEFAULT 'COLETADO'::character varying,
    observacao_coleta text,
    localizacao_atual character varying(255),
    localizacao_encontrada character varying(255),
    estado_encontrado character varying(50),
    divergencia boolean DEFAULT false,
    motivo_divergencia text,
    latitude numeric(10,8),
    longitude numeric(11,8),
    foto_patrimonio text,
    sem_etiqueta boolean DEFAULT false,
    descricao_item_sem_etiqueta character varying(500),
    categoria_item_sem_etiqueta character varying(100),
    id_participante_inventario integer NOT NULL,
    observacao text,
    tempo_coleta_segundos integer,
    tempo_scan_segundos integer,
    tempo_preenchimento_segundos integer,
    metodo_coleta character varying(20),
    hora_coleta integer,
    dia_semana integer,
    periodo_coleta character varying(10),
    tipo_scan character varying(20),
    tentativas_scan integer DEFAULT 1,
    erros_scan integer DEFAULT 0,
    qualidade_etiqueta character varying(20)
);


ALTER TABLE public.tabela_coleta OWNER TO postgres;

--
-- Name: TABLE tabela_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_coleta IS 'Tabela de coletas realizadas durante o inventário';


--
-- Name: COLUMN tabela_coleta.id_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.id_inventario IS 'Referência ao inventário';


--
-- Name: COLUMN tabela_coleta.id_patrimonio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.id_patrimonio IS 'Referência ao patrimônio coletado';


--
-- Name: COLUMN tabela_coleta.id_coletor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.id_coletor IS 'Referência ao coletor responsável';


--
-- Name: COLUMN tabela_coleta.data_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.data_coleta IS 'Data e hora da coleta';


--
-- Name: COLUMN tabela_coleta.status_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.status_coleta IS 'Status da coleta (COLETADO, NAO_ENCONTRADO, DANIFICADO)';


--
-- Name: COLUMN tabela_coleta.observacao_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.observacao_coleta IS 'Observações sobre a coleta';


--
-- Name: COLUMN tabela_coleta.localizacao_atual; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.localizacao_atual IS 'Localização cadastrada do patrimônio';


--
-- Name: COLUMN tabela_coleta.localizacao_encontrada; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.localizacao_encontrada IS 'Localização onde foi encontrado';


--
-- Name: COLUMN tabela_coleta.estado_encontrado; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.estado_encontrado IS 'Estado de conservação encontrado';


--
-- Name: COLUMN tabela_coleta.divergencia; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.divergencia IS 'Indica se há divergência entre o cadastrado e encontrado';


--
-- Name: COLUMN tabela_coleta.latitude; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.latitude IS 'Coordenada de latitude da coleta';


--
-- Name: COLUMN tabela_coleta.longitude; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.longitude IS 'Coordenada de longitude da coleta';


--
-- Name: COLUMN tabela_coleta.foto_patrimonio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.foto_patrimonio IS 'Caminho/URL da foto do patrimônio';


--
-- Name: COLUMN tabela_coleta.id_participante_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.id_participante_inventario IS 'ReferÃªncia ao participante do inventÃ¡rio que realizou a coleta';


--
-- Name: COLUMN tabela_coleta.tempo_coleta_segundos; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.tempo_coleta_segundos IS 'Tempo total da coleta em segundos';


--
-- Name: COLUMN tabela_coleta.tempo_scan_segundos; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.tempo_scan_segundos IS 'Tempo do scan (QR ou Barcode) em segundos';


--
-- Name: COLUMN tabela_coleta.tempo_preenchimento_segundos; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.tempo_preenchimento_segundos IS 'Tempo de preenchimento do formulÃ¡rio em segundos';


--
-- Name: COLUMN tabela_coleta.metodo_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.metodo_coleta IS 'MÃ©todo: QR_CODE, CODIGO_BARRAS, MANUAL, BUSCA, SEM_ETIQUETA';


--
-- Name: COLUMN tabela_coleta.hora_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.hora_coleta IS 'Hora da coleta (0-23)';


--
-- Name: COLUMN tabela_coleta.dia_semana; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.dia_semana IS 'Dia da semana (1=Dom, 2=Seg, ..., 7=Sab)';


--
-- Name: COLUMN tabela_coleta.periodo_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.periodo_coleta IS 'PerÃ­odo: MANHA, TARDE, NOITE';


--
-- Name: COLUMN tabela_coleta.tipo_scan; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.tipo_scan IS 'Tipo de scan: QR_CODE ou CODIGO_BARRAS';


--
-- Name: COLUMN tabela_coleta.tentativas_scan; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.tentativas_scan IS 'NÃºmero de tentativas atÃ© sucesso no scan';


--
-- Name: COLUMN tabela_coleta.erros_scan; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.erros_scan IS 'NÃºmero de erros durante o scan';


--
-- Name: COLUMN tabela_coleta.qualidade_etiqueta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coleta.qualidade_etiqueta IS 'Qualidade: OTIMA, BOA, REGULAR, RUIM';


--
-- Name: tabela_coleta_componente; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_coleta_componente (
    id integer NOT NULL,
    id_item_composto integer NOT NULL,
    id_inventario integer NOT NULL,
    id_coletor integer NOT NULL,
    quantidade_encontrada integer,
    status_componente character varying(20) DEFAULT 'PENDENTE'::character varying,
    observacao_coleta text,
    data_coleta timestamp without time zone,
    localizacao_encontrada character varying(255)
);


ALTER TABLE public.tabela_coleta_componente OWNER TO postgres;

--
-- Name: tabela_coleta_componente_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_coleta_componente_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_coleta_componente_id_seq OWNER TO postgres;

--
-- Name: tabela_coleta_componente_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_coleta_componente_id_seq OWNED BY public.tabela_coleta_componente.id;


--
-- Name: tabela_coleta_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_coleta_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_coleta_id_seq OWNER TO postgres;

--
-- Name: tabela_coleta_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_coleta_id_seq OWNED BY public.tabela_coleta.id;


--
-- Name: tabela_coletor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_coletor (
    id integer NOT NULL,
    nome_coletor character varying(255) NOT NULL,
    cpf character varying(14),
    email character varying(255),
    telefone character varying(20),
    matricula character varying(20),
    cargo character varying(100),
    id_setor integer,
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    observacoes text
);


ALTER TABLE public.tabela_coletor OWNER TO postgres;

--
-- Name: TABLE tabela_coletor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_coletor IS 'Tabela de coletores que realizam o inventário';


--
-- Name: COLUMN tabela_coletor.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.id IS 'Identificador único do coletor';


--
-- Name: COLUMN tabela_coletor.nome_coletor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.nome_coletor IS 'Nome completo do coletor';


--
-- Name: COLUMN tabela_coletor.cpf; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.cpf IS 'CPF do coletor';


--
-- Name: COLUMN tabela_coletor.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.email IS 'Email de contato do coletor';


--
-- Name: COLUMN tabela_coletor.telefone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.telefone IS 'Telefone de contato';


--
-- Name: COLUMN tabela_coletor.matricula; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.matricula IS 'Matrícula funcional do coletor';


--
-- Name: COLUMN tabela_coletor.cargo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.cargo IS 'Cargo/função do coletor';


--
-- Name: COLUMN tabela_coletor.id_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.id_setor IS 'Setor ao qual o coletor pertence';


--
-- Name: COLUMN tabela_coletor.ativo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.ativo IS 'Indica se o coletor está ativo para coletas';


--
-- Name: COLUMN tabela_coletor.data_cadastro; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.data_cadastro IS 'Data de cadastro do coletor';


--
-- Name: COLUMN tabela_coletor.observacoes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_coletor.observacoes IS 'Observações sobre o coletor';


--
-- Name: tabela_coletor_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_coletor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_coletor_id_seq OWNER TO postgres;

--
-- Name: tabela_coletor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_coletor_id_seq OWNED BY public.tabela_coletor.id;


--
-- Name: tabela_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_inventario (
    id integer NOT NULL,
    nome character varying(255) NOT NULL,
    ano integer NOT NULL,
    data_inicio date NOT NULL,
    data_fim date,
    observacao text,
    status_inventario character varying(50) DEFAULT 'PLANEJADO'::character varying,
    responsavel_inventario character varying(255),
    total_patrimonios integer DEFAULT 0,
    patrimonios_coletados integer DEFAULT 0,
    percentual_conclusao numeric(5,2) DEFAULT 0.00,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_ultima_atualizacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    responsavel character varying(100),
    tipo character varying(50)
);


ALTER TABLE public.tabela_inventario OWNER TO postgres;

--
-- Name: TABLE tabela_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_inventario IS 'Tabela de inventários realizados na instituição';


--
-- Name: COLUMN tabela_inventario.nome; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.nome IS 'Nome/título do inventário';


--
-- Name: COLUMN tabela_inventario.ano; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.ano IS 'Ano de referência do inventário';


--
-- Name: COLUMN tabela_inventario.data_inicio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.data_inicio IS 'Data de início do inventário';


--
-- Name: COLUMN tabela_inventario.data_fim; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.data_fim IS 'Data de conclusão do inventário';


--
-- Name: COLUMN tabela_inventario.observacao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.observacao IS 'Observações gerais sobre o inventário';


--
-- Name: COLUMN tabela_inventario.status_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.status_inventario IS 'Status do inventário (PLANEJADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO)';


--
-- Name: COLUMN tabela_inventario.responsavel_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.responsavel_inventario IS 'Responsável geral pelo inventário';


--
-- Name: COLUMN tabela_inventario.total_patrimonios; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.total_patrimonios IS 'Total de patrimônios a serem inventariados';


--
-- Name: COLUMN tabela_inventario.patrimonios_coletados; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.patrimonios_coletados IS 'Quantidade de patrimônios já coletados';


--
-- Name: COLUMN tabela_inventario.percentual_conclusao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario.percentual_conclusao IS 'Percentual de conclusão do inventário';


--
-- Name: tabela_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_inventario_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_inventario_id_seq OWNER TO postgres;

--
-- Name: tabela_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_inventario_id_seq OWNED BY public.tabela_inventario.id;


--
-- Name: tabela_inventario_setor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_inventario_setor (
    id integer NOT NULL,
    id_inventario integer NOT NULL,
    id_setor integer,
    incluir_todos_setores boolean DEFAULT false,
    data_inclusao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ativo boolean DEFAULT true,
    observacoes text,
    CONSTRAINT tabela_inventario_setor_check CHECK ((((incluir_todos_setores = true) AND (id_setor IS NULL)) OR ((incluir_todos_setores = false) AND (id_setor IS NOT NULL))))
);


ALTER TABLE public.tabela_inventario_setor OWNER TO postgres;

--
-- Name: TABLE tabela_inventario_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_inventario_setor IS 'Relaciona inventários com setores específicos para controle de escopo';


--
-- Name: COLUMN tabela_inventario_setor.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.id IS 'Identificador único do relacionamento';


--
-- Name: COLUMN tabela_inventario_setor.id_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.id_inventario IS 'Referência ao inventário';


--
-- Name: COLUMN tabela_inventario_setor.id_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.id_setor IS 'Referência ao setor (NULL quando incluir todos)';


--
-- Name: COLUMN tabela_inventario_setor.incluir_todos_setores; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.incluir_todos_setores IS 'Indica se o inventário inclui todos os setores';


--
-- Name: COLUMN tabela_inventario_setor.data_inclusao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.data_inclusao IS 'Data de inclusão do relacionamento';


--
-- Name: COLUMN tabela_inventario_setor.ativo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.ativo IS 'Indica se o relacionamento está ativo';


--
-- Name: COLUMN tabela_inventario_setor.observacoes; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_inventario_setor.observacoes IS 'Observações sobre o relacionamento';


--
-- Name: tabela_inventario_setor_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_inventario_setor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_inventario_setor_id_seq OWNER TO postgres;

--
-- Name: tabela_inventario_setor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_inventario_setor_id_seq OWNED BY public.tabela_inventario_setor.id;


--
-- Name: tabela_item_composto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_item_composto (
    id integer NOT NULL,
    id_patrimonio_principal integer NOT NULL,
    tipo_componente character varying(100),
    descricao_componente character varying(255),
    quantidade_esperada integer,
    obrigatorio boolean,
    observacao text,
    data_cadastro timestamp without time zone
);


ALTER TABLE public.tabela_item_composto OWNER TO postgres;

--
-- Name: tabela_item_composto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_item_composto_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_item_composto_id_seq OWNER TO postgres;

--
-- Name: tabela_item_composto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_item_composto_id_seq OWNED BY public.tabela_item_composto.id;


--
-- Name: tabela_participante_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_participante_inventario (
    id_participante integer NOT NULL,
    id_inventario integer NOT NULL,
    id_usuario integer NOT NULL,
    papel character varying(50) NOT NULL,
    data_inclusao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ativo boolean DEFAULT true,
    observacoes text,
    data_remocao timestamp without time zone,
    data_ultima_atualizacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tabela_participante_inventario_papel_check CHECK (((papel)::text = ANY (ARRAY[('COORDENADOR'::character varying)::text, ('COLETOR'::character varying)::text, ('OBSERVADOR'::character varying)::text])))
);


ALTER TABLE public.tabela_participante_inventario OWNER TO postgres;

--
-- Name: tabela_participante_inventario_id_participante_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_participante_inventario_id_participante_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_participante_inventario_id_participante_seq OWNER TO postgres;

--
-- Name: tabela_participante_inventario_id_participante_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_participante_inventario_id_participante_seq OWNED BY public.tabela_participante_inventario.id_participante;


--
-- Name: tabela_patrimonio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_patrimonio (
    id integer NOT NULL,
    numero character varying(50) NOT NULL,
    status character varying(50) DEFAULT 'ATIVO'::character varying,
    descricao text,
    rotulos character varying(500),
    id_responsavel integer,
    valor_aquisicao numeric(15,2),
    valor_depreciado numeric(15,2),
    numero_nota_fiscal character varying(100),
    numero_serie character varying(100),
    data_entrada date,
    data_carga timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fornecedor character varying(255),
    id_sala integer,
    estado_conservacao character varying(50),
    marca character varying(255),
    modelo character varying(255),
    categoria character varying(50),
    descricao_resumida character varying(500),
    observacoes text,
    situacao character varying(20),
    ed character varying(20)
);


ALTER TABLE public.tabela_patrimonio OWNER TO postgres;

--
-- Name: TABLE tabela_patrimonio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_patrimonio IS 'Tabela principal de patrimônios da instituição';


--
-- Name: COLUMN tabela_patrimonio.numero; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.numero IS 'Número único do patrimônio';


--
-- Name: COLUMN tabela_patrimonio.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.status IS 'Status do patrimônio (ATIVO, INATIVO, BAIXADO)';


--
-- Name: COLUMN tabela_patrimonio.descricao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.descricao IS 'Descrição detalhada do patrimônio';


--
-- Name: COLUMN tabela_patrimonio.rotulos; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.rotulos IS 'Rótulos/tags para categorização';


--
-- Name: COLUMN tabela_patrimonio.valor_aquisicao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.valor_aquisicao IS 'Valor de aquisição do patrimônio';


--
-- Name: COLUMN tabela_patrimonio.valor_depreciado; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.valor_depreciado IS 'Valor atual depreciado';


--
-- Name: COLUMN tabela_patrimonio.numero_nota_fiscal; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.numero_nota_fiscal IS 'NÃºmero da Nota Fiscal de aquisiÃ§Ã£o';


--
-- Name: COLUMN tabela_patrimonio.fornecedor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.fornecedor IS 'Nome do fornecedor';


--
-- Name: COLUMN tabela_patrimonio.estado_conservacao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.estado_conservacao IS 'Estado de conservação (NOVO, BOM, REGULAR, RUIM)';


--
-- Name: COLUMN tabela_patrimonio.marca; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.marca IS 'Marca do patrimônio';


--
-- Name: COLUMN tabela_patrimonio.modelo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.modelo IS 'Modelo do patrimônio';


--
-- Name: COLUMN tabela_patrimonio.categoria; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.categoria IS 'Categoria do patrimÃ´nio definida pela IA treinada';


--
-- Name: COLUMN tabela_patrimonio.ed; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_patrimonio.ed IS 'Elemento de Despesa (SIADS)';


--
-- Name: tabela_patrimonio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_patrimonio_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_patrimonio_id_seq OWNER TO postgres;

--
-- Name: tabela_patrimonio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_patrimonio_id_seq OWNED BY public.tabela_patrimonio.id;


--
-- Name: tabela_responsavel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_responsavel (
    id integer NOT NULL,
    nome character varying(255) NOT NULL,
    cpf character varying(14),
    email character varying(255),
    telefone character varying(20),
    cargo character varying(100),
    id_setor integer,
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tabela_responsavel OWNER TO postgres;

--
-- Name: TABLE tabela_responsavel; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_responsavel IS 'Tabela de responsáveis pelos patrimônios';


--
-- Name: COLUMN tabela_responsavel.nome; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.nome IS 'Nome completo do responsável';


--
-- Name: COLUMN tabela_responsavel.cpf; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.cpf IS 'CPF do responsável';


--
-- Name: COLUMN tabela_responsavel.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.email IS 'Email de contato';


--
-- Name: COLUMN tabela_responsavel.telefone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.telefone IS 'Telefone de contato';


--
-- Name: COLUMN tabela_responsavel.cargo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.cargo IS 'Cargo/função do responsável';


--
-- Name: COLUMN tabela_responsavel.id_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.id_setor IS 'Setor ao qual o responsável pertence';


--
-- Name: COLUMN tabela_responsavel.ativo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_responsavel.ativo IS 'Indica se o responsável está ativo';


--
-- Name: tabela_responsavel_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_responsavel_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_responsavel_id_seq OWNER TO postgres;

--
-- Name: tabela_responsavel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_responsavel_id_seq OWNED BY public.tabela_responsavel.id;


--
-- Name: tabela_sala; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_sala (
    id_sala integer NOT NULL,
    descricao character varying(255) NOT NULL,
    numero_sala character varying(100),
    andar integer,
    bloco character varying(10),
    id_setor integer,
    capacidade integer,
    area_m2 double precision,
    tipo_sala character varying(50),
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    observacoes text,
    id integer NOT NULL,
    created_at timestamp(6) without time zone,
    numero character varying(20),
    tipo character varying(50)
);


ALTER TABLE public.tabela_sala OWNER TO postgres;

--
-- Name: TABLE tabela_sala; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_sala IS 'Tabela de salas/locais onde ficam os patrimônios';


--
-- Name: COLUMN tabela_sala.id_sala; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.id_sala IS 'Identificador único da sala';


--
-- Name: COLUMN tabela_sala.descricao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.descricao IS 'Descrição da sala ou local';


--
-- Name: COLUMN tabela_sala.numero_sala; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.numero_sala IS 'Número identificador da sala';


--
-- Name: COLUMN tabela_sala.andar; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.andar IS 'Andar onde fica a sala';


--
-- Name: COLUMN tabela_sala.bloco; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.bloco IS 'Bloco do prédio';


--
-- Name: COLUMN tabela_sala.id_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.id_setor IS 'Setor responsável pela sala';


--
-- Name: COLUMN tabela_sala.capacidade; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.capacidade IS 'Capacidade de pessoas na sala';


--
-- Name: COLUMN tabela_sala.area_m2; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.area_m2 IS 'Área da sala em metros quadrados';


--
-- Name: COLUMN tabela_sala.tipo_sala; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.tipo_sala IS 'Tipo da sala (ADMINISTRATIVA, LABORATORIO, AULA, DEPOSITO)';


--
-- Name: COLUMN tabela_sala.ativo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_sala.ativo IS 'Indica se a sala está ativa';


--
-- Name: tabela_sala_id_sala_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_sala_id_sala_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_sala_id_sala_seq OWNER TO postgres;

--
-- Name: tabela_sala_id_sala_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_sala_id_sala_seq OWNED BY public.tabela_sala.id_sala;


--
-- Name: tabela_sala_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_sala_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_sala_id_seq OWNER TO postgres;

--
-- Name: tabela_sala_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_sala_id_seq OWNED BY public.tabela_sala.id;


--
-- Name: tabela_sala_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_sala_inventario (
    id_sala_inventario integer NOT NULL,
    id_sala integer NOT NULL,
    id_inventario integer NOT NULL,
    id_participante integer,
    coleta_finalizada boolean DEFAULT false,
    data_inicio_coleta timestamp without time zone,
    data_finalizacao_coleta timestamp without time zone,
    observacoes_finalizacao text,
    total_itens_coletados integer DEFAULT 0,
    total_itens_sem_etiqueta integer DEFAULT 0,
    percentual_conclusao numeric(5,2) DEFAULT 0.00,
    status_coleta character varying(50) DEFAULT 'PENDENTE'::character varying,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tabela_sala_inventario OWNER TO postgres;

--
-- Name: tabela_sala_inventario_id_sala_inventario_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_sala_inventario_id_sala_inventario_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_sala_inventario_id_sala_inventario_seq OWNER TO postgres;

--
-- Name: tabela_sala_inventario_id_sala_inventario_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_sala_inventario_id_sala_inventario_seq OWNED BY public.tabela_sala_inventario.id_sala_inventario;


--
-- Name: tabela_setor; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_setor (
    id integer NOT NULL,
    nome character varying(255) NOT NULL,
    descricao text,
    responsavel_setor character varying(255),
    ativo boolean DEFAULT true,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp(6) without time zone,
    email character varying(100),
    id_campus integer,
    observacoes text,
    responsavel character varying(100),
    telefone character varying(20)
);


ALTER TABLE public.tabela_setor OWNER TO postgres;

--
-- Name: TABLE tabela_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_setor IS 'Tabela de setores da instituição';


--
-- Name: COLUMN tabela_setor.nome; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_setor.nome IS 'Nome do setor';


--
-- Name: COLUMN tabela_setor.descricao; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_setor.descricao IS 'Descrição detalhada do setor';


--
-- Name: COLUMN tabela_setor.responsavel_setor; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_setor.responsavel_setor IS 'Responsável pelo setor';


--
-- Name: COLUMN tabela_setor.ativo; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_setor.ativo IS 'Indica se o setor está ativo';


--
-- Name: tabela_setor_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_setor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_setor_id_seq OWNER TO postgres;

--
-- Name: tabela_setor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_setor_id_seq OWNED BY public.tabela_setor.id;


--
-- Name: tabela_subcategoria_patrimonio; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_subcategoria_patrimonio (
    id integer NOT NULL,
    nome character varying(200) NOT NULL,
    descricao text,
    id_categoria integer,
    ativo boolean DEFAULT true,
    data_cadastro timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.tabela_subcategoria_patrimonio OWNER TO postgres;

--
-- Name: tabela_subcategoria_patrimonio_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_subcategoria_patrimonio_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_subcategoria_patrimonio_id_seq OWNER TO postgres;

--
-- Name: tabela_subcategoria_patrimonio_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_subcategoria_patrimonio_id_seq OWNED BY public.tabela_subcategoria_patrimonio.id;


--
-- Name: tabela_usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tabela_usuario (
    id integer NOT NULL,
    login character varying(50) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    nome_completo character varying(150) NOT NULL,
    email character varying(100) NOT NULL,
    matricula character varying(14),
    perfil character varying(20) NOT NULL,
    id_setor integer,
    data_criacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_ultimo_acesso timestamp without time zone,
    tentativas_login integer DEFAULT 0,
    data_bloqueio timestamp without time zone,
    data_expiracao_senha timestamp without time zone DEFAULT (CURRENT_TIMESTAMP + '90 days'::interval),
    observacoes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ativo boolean DEFAULT true,
    bloqueado boolean DEFAULT false,
    primeiro_acesso boolean DEFAULT true,
    mobile_habilitado boolean DEFAULT true,
    ultimo_login_mobile timestamp without time zone,
    total_logins_mobile integer DEFAULT 0,
    CONSTRAINT tabela_usuario_perfil_check CHECK (((perfil)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('SUPERVISOR'::character varying)::text, ('COLETOR'::character varying)::text, ('CONSULTA'::character varying)::text])))
);


ALTER TABLE public.tabela_usuario OWNER TO postgres;

--
-- Name: TABLE tabela_usuario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.tabela_usuario IS 'Tabela de usuários do sistema';


--
-- Name: COLUMN tabela_usuario.login; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_usuario.login IS 'Login único do usuário';


--
-- Name: COLUMN tabela_usuario.senha_hash; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_usuario.senha_hash IS 'Hash da senha (bcrypt)';


--
-- Name: COLUMN tabela_usuario.perfil; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.tabela_usuario.perfil IS 'Perfil de acesso: ADMIN, SUPERVISOR, COLETOR, CONSULTA';


--
-- Name: tabela_usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.tabela_usuario_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tabela_usuario_id_seq OWNER TO postgres;

--
-- Name: tabela_usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.tabela_usuario_id_seq OWNED BY public.tabela_usuario.id;


--
-- Name: vw_auditoria_integridade_coleta_componente; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vw_auditoria_integridade_coleta_componente AS
 SELECT cc.id AS id_coleta_componente,
    cc.id_item_composto,
        CASE
            WHEN (ic.id IS NULL) THEN 'ÓRFÃO'::text
            ELSE 'OK'::text
        END AS status_integridade,
    cc.id_inventario,
    cc.id_coletor,
    cc.data_coleta,
    ic.id_patrimonio_principal,
    ic.tipo_componente
   FROM (public.tabela_coleta_componente cc
     LEFT JOIN public.tabela_item_composto ic ON ((cc.id_item_composto = ic.id)));


ALTER VIEW public.vw_auditoria_integridade_coleta_componente OWNER TO postgres;

--
-- Name: vw_inventario_setores; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vw_inventario_setores AS
 SELECT i.id AS id_inventario,
    i.nome AS nome_inventario,
    i.status_inventario,
        CASE
            WHEN (bool_or(ise.incluir_todos_setores) = true) THEN 'TODOS OS SETORES'::text
            ELSE string_agg((s.nome)::text, ', '::text ORDER BY (s.nome)::text)
        END AS setores_incluidos,
    bool_or(ise.incluir_todos_setores) AS incluir_todos_setores,
    count(
        CASE
            WHEN (ise.id_setor IS NOT NULL) THEN 1
            ELSE NULL::integer
        END) AS quantidade_setores_especificos
   FROM ((public.tabela_inventario i
     LEFT JOIN public.tabela_inventario_setor ise ON (((i.id = ise.id_inventario) AND (ise.ativo = true))))
     LEFT JOIN public.tabela_setor s ON ((ise.id_setor = s.id)))
  GROUP BY i.id, i.nome, i.status_inventario
  ORDER BY i.id;


ALTER VIEW public.vw_inventario_setores OWNER TO postgres;

--
-- Name: VIEW vw_inventario_setores; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.vw_inventario_setores IS 'View que mostra os setores selecionados para cada inventário';


--
-- Name: vw_monitoramento_integridade_diario; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vw_monitoramento_integridade_diario AS
 SELECT 'INTEGRIDADE'::text AS metrica,
    count(*) AS valor,
        CASE
            WHEN (count(*) = 0) THEN 'OK'::text
            ELSE 'ALERTA'::text
        END AS status
   FROM (public.tabela_coleta_componente cc
     LEFT JOIN public.tabela_item_composto ic ON ((cc.id_item_composto = ic.id)))
  WHERE (ic.id IS NULL)
UNION ALL
 SELECT 'TOTAL_REGISTROS'::text AS metrica,
    count(*) AS valor,
    'INFO'::text AS status
   FROM public.tabela_coleta_componente
UNION ALL
 SELECT 'NOVOS_24H'::text AS metrica,
    count(*) AS valor,
    'INFO'::text AS status
   FROM public.tabela_coleta_componente
  WHERE (tabela_coleta_componente.data_coleta >= (CURRENT_TIMESTAMP - '24:00:00'::interval));


ALTER VIEW public.vw_monitoramento_integridade_diario OWNER TO postgres;

--
-- Name: vw_salas_escopo_inventario; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vw_salas_escopo_inventario AS
 SELECT DISTINCT i.id AS id_inventario,
    i.nome AS nome_inventario,
    sl.id_sala,
    sl.descricao AS descricao_sala,
    s.id AS id_setor,
    s.nome AS nome_setor
   FROM (((public.tabela_inventario i
     JOIN public.tabela_inventario_setor ise ON (((i.id = ise.id_inventario) AND (ise.ativo = true))))
     LEFT JOIN public.tabela_setor s ON ((((ise.incluir_todos_setores = false) AND (ise.id_setor = s.id)) OR (ise.incluir_todos_setores = true))))
     JOIN public.tabela_sala sl ON ((((ise.incluir_todos_setores = false) AND (sl.id_setor = s.id)) OR ((ise.incluir_todos_setores = true) AND (sl.id_setor IS NOT NULL)))))
  WHERE
        CASE
            WHEN (ise.incluir_todos_setores = true) THEN true
            ELSE (s.id IS NOT NULL)
        END
  ORDER BY i.id, s.nome, sl.descricao;


ALTER VIEW public.vw_salas_escopo_inventario OWNER TO postgres;

--
-- Name: VIEW vw_salas_escopo_inventario; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.vw_salas_escopo_inventario IS 'View que lista todas as salas que fazem parte do escopo de cada inventário';


--
-- Name: dispositivo_mobile id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dispositivo_mobile ALTER COLUMN id SET DEFAULT nextval('public.dispositivo_mobile_id_seq'::regclass);


--
-- Name: log_remocao_orfaos_20260211 id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.log_remocao_orfaos_20260211 ALTER COLUMN id SET DEFAULT nextval('public.log_remocao_orfaos_20260211_id_seq'::regclass);


--
-- Name: tabela_campus id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_campus ALTER COLUMN id SET DEFAULT nextval('public.tabela_campus_id_seq'::regclass);


--
-- Name: tabela_categoria_patrimonio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_categoria_patrimonio ALTER COLUMN id SET DEFAULT nextval('public.tabela_categoria_patrimonio_id_seq'::regclass);


--
-- Name: tabela_coleta id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta ALTER COLUMN id SET DEFAULT nextval('public.tabela_coleta_id_seq'::regclass);


--
-- Name: tabela_coleta_componente id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta_componente ALTER COLUMN id SET DEFAULT nextval('public.tabela_coleta_componente_id_seq'::regclass);


--
-- Name: tabela_coletor id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coletor ALTER COLUMN id SET DEFAULT nextval('public.tabela_coletor_id_seq'::regclass);


--
-- Name: tabela_inventario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario ALTER COLUMN id SET DEFAULT nextval('public.tabela_inventario_id_seq'::regclass);


--
-- Name: tabela_inventario_setor id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario_setor ALTER COLUMN id SET DEFAULT nextval('public.tabela_inventario_setor_id_seq'::regclass);


--
-- Name: tabela_item_composto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_item_composto ALTER COLUMN id SET DEFAULT nextval('public.tabela_item_composto_id_seq'::regclass);


--
-- Name: tabela_participante_inventario id_participante; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_participante_inventario ALTER COLUMN id_participante SET DEFAULT nextval('public.tabela_participante_inventario_id_participante_seq'::regclass);


--
-- Name: tabela_patrimonio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_patrimonio ALTER COLUMN id SET DEFAULT nextval('public.tabela_patrimonio_id_seq'::regclass);


--
-- Name: tabela_responsavel id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_responsavel ALTER COLUMN id SET DEFAULT nextval('public.tabela_responsavel_id_seq'::regclass);


--
-- Name: tabela_sala id_sala; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala ALTER COLUMN id_sala SET DEFAULT nextval('public.tabela_sala_id_sala_seq'::regclass);


--
-- Name: tabela_sala id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala ALTER COLUMN id SET DEFAULT nextval('public.tabela_sala_id_seq'::regclass);


--
-- Name: tabela_sala_inventario id_sala_inventario; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala_inventario ALTER COLUMN id_sala_inventario SET DEFAULT nextval('public.tabela_sala_inventario_id_sala_inventario_seq'::regclass);


--
-- Name: tabela_setor id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_setor ALTER COLUMN id SET DEFAULT nextval('public.tabela_setor_id_seq'::regclass);


--
-- Name: tabela_subcategoria_patrimonio id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_subcategoria_patrimonio ALTER COLUMN id SET DEFAULT nextval('public.tabela_subcategoria_patrimonio_id_seq'::regclass);


--
-- Name: tabela_usuario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_usuario ALTER COLUMN id SET DEFAULT nextval('public.tabela_usuario_id_seq'::regclass);


--
-- Name: dispositivo_mobile dispositivo_mobile_device_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dispositivo_mobile
    ADD CONSTRAINT dispositivo_mobile_device_id_key UNIQUE (device_id);


--
-- Name: dispositivo_mobile dispositivo_mobile_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dispositivo_mobile
    ADD CONSTRAINT dispositivo_mobile_pkey PRIMARY KEY (id);


--
-- Name: log_remocao_orfaos_20260211 log_remocao_orfaos_20260211_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.log_remocao_orfaos_20260211
    ADD CONSTRAINT log_remocao_orfaos_20260211_pkey PRIMARY KEY (id);


--
-- Name: tabela_campus tabela_campus_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_campus
    ADD CONSTRAINT tabela_campus_pkey PRIMARY KEY (id);


--
-- Name: tabela_categoria_patrimonio tabela_categoria_patrimonio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_categoria_patrimonio
    ADD CONSTRAINT tabela_categoria_patrimonio_pkey PRIMARY KEY (id);


--
-- Name: tabela_coleta_componente tabela_coleta_componente_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta_componente
    ADD CONSTRAINT tabela_coleta_componente_pkey PRIMARY KEY (id);


--
-- Name: tabela_coleta tabela_coleta_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta
    ADD CONSTRAINT tabela_coleta_pkey PRIMARY KEY (id);


--
-- Name: tabela_coletor tabela_coletor_cpf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coletor
    ADD CONSTRAINT tabela_coletor_cpf_key UNIQUE (cpf);


--
-- Name: tabela_coletor tabela_coletor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coletor
    ADD CONSTRAINT tabela_coletor_pkey PRIMARY KEY (id);


--
-- Name: tabela_inventario tabela_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario
    ADD CONSTRAINT tabela_inventario_pkey PRIMARY KEY (id);


--
-- Name: tabela_inventario_setor tabela_inventario_setor_id_inventario_id_setor_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario_setor
    ADD CONSTRAINT tabela_inventario_setor_id_inventario_id_setor_key UNIQUE (id_inventario, id_setor);


--
-- Name: tabela_inventario_setor tabela_inventario_setor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario_setor
    ADD CONSTRAINT tabela_inventario_setor_pkey PRIMARY KEY (id);


--
-- Name: tabela_item_composto tabela_item_composto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_item_composto
    ADD CONSTRAINT tabela_item_composto_pkey PRIMARY KEY (id);


--
-- Name: tabela_participante_inventario tabela_participante_inventario_id_inventario_id_usuario_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_participante_inventario
    ADD CONSTRAINT tabela_participante_inventario_id_inventario_id_usuario_key UNIQUE (id_inventario, id_usuario);


--
-- Name: tabela_participante_inventario tabela_participante_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_participante_inventario
    ADD CONSTRAINT tabela_participante_inventario_pkey PRIMARY KEY (id_participante);


--
-- Name: tabela_patrimonio tabela_patrimonio_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_patrimonio
    ADD CONSTRAINT tabela_patrimonio_numero_key UNIQUE (numero);


--
-- Name: tabela_patrimonio tabela_patrimonio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_patrimonio
    ADD CONSTRAINT tabela_patrimonio_pkey PRIMARY KEY (id);


--
-- Name: tabela_responsavel tabela_responsavel_cpf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_responsavel
    ADD CONSTRAINT tabela_responsavel_cpf_key UNIQUE (cpf);


--
-- Name: tabela_responsavel tabela_responsavel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_responsavel
    ADD CONSTRAINT tabela_responsavel_pkey PRIMARY KEY (id);


--
-- Name: tabela_sala_inventario tabela_sala_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala_inventario
    ADD CONSTRAINT tabela_sala_inventario_pkey PRIMARY KEY (id_sala_inventario);


--
-- Name: tabela_sala tabela_sala_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala
    ADD CONSTRAINT tabela_sala_pkey PRIMARY KEY (id_sala);


--
-- Name: tabela_setor tabela_setor_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_setor
    ADD CONSTRAINT tabela_setor_nome_key UNIQUE (nome);


--
-- Name: tabela_setor tabela_setor_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_setor
    ADD CONSTRAINT tabela_setor_pkey PRIMARY KEY (id);


--
-- Name: tabela_subcategoria_patrimonio tabela_subcategoria_patrimonio_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_subcategoria_patrimonio
    ADD CONSTRAINT tabela_subcategoria_patrimonio_pkey PRIMARY KEY (id);


--
-- Name: tabela_usuario tabela_usuario_cpf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_usuario
    ADD CONSTRAINT tabela_usuario_cpf_key UNIQUE (matricula);


--
-- Name: tabela_usuario tabela_usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_usuario
    ADD CONSTRAINT tabela_usuario_email_key UNIQUE (email);


--
-- Name: tabela_usuario tabela_usuario_login_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_usuario
    ADD CONSTRAINT tabela_usuario_login_key UNIQUE (login);


--
-- Name: tabela_usuario tabela_usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_usuario
    ADD CONSTRAINT tabela_usuario_pkey PRIMARY KEY (id);


--
-- Name: tabela_coleta_componente uk_coleta_componente_item_inventario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta_componente
    ADD CONSTRAINT uk_coleta_componente_item_inventario UNIQUE (id_item_composto, id_inventario);


--
-- Name: tabela_coleta uk_coleta_inventario_patrimonio; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta
    ADD CONSTRAINT uk_coleta_inventario_patrimonio UNIQUE (id_inventario, id_patrimonio);


--
-- Name: CONSTRAINT uk_coleta_inventario_patrimonio ON tabela_coleta; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON CONSTRAINT uk_coleta_inventario_patrimonio ON public.tabela_coleta IS 'Impede que um patrimônio seja coletado mais de uma vez no mesmo inventário';


--
-- Name: tabela_campus uk_lsu7ohajx17i5mwy9ufkakj7r; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_campus
    ADD CONSTRAINT uk_lsu7ohajx17i5mwy9ufkakj7r UNIQUE (cnpj);


--
-- Name: tabela_sala_inventario uk_sala_inventario; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala_inventario
    ADD CONSTRAINT uk_sala_inventario UNIQUE (id_sala, id_inventario);


--
-- Name: idx_campus_codigo_uorg; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_campus_codigo_uorg ON public.tabela_campus USING btree (codigo_uorg);


--
-- Name: idx_coleta_coletor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_coletor ON public.tabela_coleta USING btree (id_coletor);


--
-- Name: idx_coleta_componente_data_coleta; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_componente_data_coleta ON public.tabela_coleta_componente USING btree (data_coleta);


--
-- Name: idx_coleta_componente_inventario_item; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_componente_inventario_item ON public.tabela_coleta_componente USING btree (id_inventario, id_item_composto);


--
-- Name: idx_coleta_componente_item_composto; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_componente_item_composto ON public.tabela_coleta_componente USING btree (id_item_composto);


--
-- Name: idx_coleta_data; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_data ON public.tabela_coleta USING btree (data_coleta);


--
-- Name: idx_coleta_divergencia; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_divergencia ON public.tabela_coleta USING btree (divergencia);


--
-- Name: idx_coleta_hora; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_hora ON public.tabela_coleta USING btree (hora_coleta);


--
-- Name: idx_coleta_inventario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_inventario ON public.tabela_coleta USING btree (id_inventario);


--
-- Name: idx_coleta_metodo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_metodo ON public.tabela_coleta USING btree (metodo_coleta);


--
-- Name: idx_coleta_participante_inventario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_participante_inventario ON public.tabela_coleta USING btree (id_participante_inventario);


--
-- Name: idx_coleta_patrimonio; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_patrimonio ON public.tabela_coleta USING btree (id_patrimonio);


--
-- Name: idx_coleta_periodo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_periodo ON public.tabela_coleta USING btree (periodo_coleta);


--
-- Name: idx_coleta_qualidade; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_qualidade ON public.tabela_coleta USING btree (qualidade_etiqueta);


--
-- Name: idx_coleta_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_status ON public.tabela_coleta USING btree (status_coleta);


--
-- Name: idx_coleta_tempo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_tempo ON public.tabela_coleta USING btree (tempo_coleta_segundos);


--
-- Name: idx_coleta_tipo_scan; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coleta_tipo_scan ON public.tabela_coleta USING btree (tipo_scan);


--
-- Name: idx_coletor_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coletor_ativo ON public.tabela_coletor USING btree (ativo);


--
-- Name: idx_coletor_cpf; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coletor_cpf ON public.tabela_coletor USING btree (cpf);


--
-- Name: idx_coletor_matricula; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coletor_matricula ON public.tabela_coletor USING btree (matricula);


--
-- Name: idx_coletor_nome; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coletor_nome ON public.tabela_coletor USING btree (nome_coletor);


--
-- Name: idx_coletor_setor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_coletor_setor ON public.tabela_coletor USING btree (id_setor);


--
-- Name: idx_dispositivo_device_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dispositivo_device_id ON public.dispositivo_mobile USING btree (device_id);


--
-- Name: idx_dispositivo_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dispositivo_usuario ON public.dispositivo_mobile USING btree (id_usuario);


--
-- Name: idx_inventario_ano; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_ano ON public.tabela_inventario USING btree (ano);


--
-- Name: idx_inventario_data_fim; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_data_fim ON public.tabela_inventario USING btree (data_fim);


--
-- Name: idx_inventario_data_inicio; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_data_inicio ON public.tabela_inventario USING btree (data_inicio);


--
-- Name: idx_inventario_setor_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_setor_ativo ON public.tabela_inventario_setor USING btree (ativo);


--
-- Name: idx_inventario_setor_inventario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_setor_inventario ON public.tabela_inventario_setor USING btree (id_inventario);


--
-- Name: idx_inventario_setor_inventario_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_setor_inventario_ativo ON public.tabela_inventario_setor USING btree (id_inventario, ativo);


--
-- Name: idx_inventario_setor_setor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_setor_setor ON public.tabela_inventario_setor USING btree (id_setor);


--
-- Name: idx_inventario_setor_todos; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_setor_todos ON public.tabela_inventario_setor USING btree (incluir_todos_setores);


--
-- Name: idx_inventario_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_inventario_status ON public.tabela_inventario USING btree (status_inventario);


--
-- Name: idx_participante_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_participante_ativo ON public.tabela_participante_inventario USING btree (ativo);


--
-- Name: idx_participante_inventario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_participante_inventario ON public.tabela_participante_inventario USING btree (id_inventario);


--
-- Name: idx_participante_papel; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_participante_papel ON public.tabela_participante_inventario USING btree (papel);


--
-- Name: idx_participante_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_participante_usuario ON public.tabela_participante_inventario USING btree (id_usuario);


--
-- Name: idx_patrimonio_categoria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_categoria ON public.tabela_patrimonio USING btree (categoria);


--
-- Name: idx_patrimonio_ed; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_ed ON public.tabela_patrimonio USING btree (ed);


--
-- Name: idx_patrimonio_fornecedor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_fornecedor ON public.tabela_patrimonio USING btree (fornecedor);


--
-- Name: idx_patrimonio_marca; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_marca ON public.tabela_patrimonio USING btree (marca);


--
-- Name: idx_patrimonio_modelo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_modelo ON public.tabela_patrimonio USING btree (modelo);


--
-- Name: idx_patrimonio_nota_fiscal; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_nota_fiscal ON public.tabela_patrimonio USING btree (numero_nota_fiscal);


--
-- Name: idx_patrimonio_numero; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_numero ON public.tabela_patrimonio USING btree (numero);


--
-- Name: idx_patrimonio_responsavel; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_responsavel ON public.tabela_patrimonio USING btree (id_responsavel);


--
-- Name: idx_patrimonio_sala; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_sala ON public.tabela_patrimonio USING btree (id_sala);


--
-- Name: idx_patrimonio_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_patrimonio_status ON public.tabela_patrimonio USING btree (status);


--
-- Name: idx_responsavel_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_responsavel_ativo ON public.tabela_responsavel USING btree (ativo);


--
-- Name: idx_responsavel_cpf; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_responsavel_cpf ON public.tabela_responsavel USING btree (cpf);


--
-- Name: idx_responsavel_nome; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_responsavel_nome ON public.tabela_responsavel USING btree (nome);


--
-- Name: idx_responsavel_setor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_responsavel_setor ON public.tabela_responsavel USING btree (id_setor);


--
-- Name: idx_sala_andar; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_andar ON public.tabela_sala USING btree (andar);


--
-- Name: idx_sala_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_ativo ON public.tabela_sala USING btree (ativo);


--
-- Name: idx_sala_bloco; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_bloco ON public.tabela_sala USING btree (bloco);


--
-- Name: idx_sala_inventario_inventario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_inventario_inventario ON public.tabela_sala_inventario USING btree (id_inventario);


--
-- Name: idx_sala_inventario_sala; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_inventario_sala ON public.tabela_sala_inventario USING btree (id_sala);


--
-- Name: idx_sala_numero; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_numero ON public.tabela_sala USING btree (numero_sala);


--
-- Name: idx_sala_setor; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sala_setor ON public.tabela_sala USING btree (id_setor);


--
-- Name: idx_setor_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_setor_ativo ON public.tabela_setor USING btree (ativo);


--
-- Name: idx_setor_nome; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_setor_nome ON public.tabela_setor USING btree (nome);


--
-- Name: idx_usuario_ativo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_ativo ON public.tabela_usuario USING btree (ativo);


--
-- Name: idx_usuario_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_email ON public.tabela_usuario USING btree (email);


--
-- Name: idx_usuario_login; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_login ON public.tabela_usuario USING btree (login);


--
-- Name: idx_usuario_perfil; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_perfil ON public.tabela_usuario USING btree (perfil);


--
-- Name: tabela_inventario_setor tg_validar_inventario_setor; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER tg_validar_inventario_setor BEFORE INSERT OR UPDATE ON public.tabela_inventario_setor FOR EACH ROW EXECUTE FUNCTION public.fn_validar_inventario_setor();


--
-- Name: tabela_coleta_componente trg_validar_id_item_composto; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_validar_id_item_composto BEFORE INSERT OR UPDATE ON public.tabela_coleta_componente FOR EACH ROW EXECUTE FUNCTION public.validar_id_item_composto();


--
-- Name: tabela_inventario trigger_update_inventario_timestamp; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_inventario_timestamp BEFORE UPDATE ON public.tabela_inventario FOR EACH ROW EXECUTE FUNCTION public.update_inventario_timestamp();


--
-- Name: tabela_usuario update_usuario_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_usuario_updated_at BEFORE UPDATE ON public.tabela_usuario FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: tabela_coleta_componente fk_coleta_componente_item_composto; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta_componente
    ADD CONSTRAINT fk_coleta_componente_item_composto FOREIGN KEY (id_item_composto) REFERENCES public.tabela_item_composto(id) ON DELETE CASCADE;


--
-- Name: tabela_coleta_componente fk_coleta_componente_patrimonio; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta_componente
    ADD CONSTRAINT fk_coleta_componente_patrimonio FOREIGN KEY (id_inventario) REFERENCES public.tabela_patrimonio(id) ON DELETE CASCADE;


--
-- Name: tabela_coleta fk_coleta_participante_inventario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta
    ADD CONSTRAINT fk_coleta_participante_inventario FOREIGN KEY (id_participante_inventario) REFERENCES public.tabela_participante_inventario(id_participante) ON DELETE RESTRICT;


--
-- Name: dispositivo_mobile fk_dispositivo_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dispositivo_mobile
    ADD CONSTRAINT fk_dispositivo_usuario FOREIGN KEY (id_usuario) REFERENCES public.tabela_usuario(id) ON DELETE CASCADE;


--
-- Name: tabela_sala_inventario fk_inventario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala_inventario
    ADD CONSTRAINT fk_inventario FOREIGN KEY (id_inventario) REFERENCES public.tabela_inventario(id);


--
-- Name: tabela_item_composto fk_item_composto_principal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_item_composto
    ADD CONSTRAINT fk_item_composto_principal FOREIGN KEY (id_patrimonio_principal) REFERENCES public.tabela_patrimonio(id) ON DELETE CASCADE;


--
-- Name: tabela_sala_inventario fk_sala; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala_inventario
    ADD CONSTRAINT fk_sala FOREIGN KEY (id_sala) REFERENCES public.tabela_sala(id_sala);


--
-- Name: tabela_subcategoria_patrimonio fk_subcategoria_categoria; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_subcategoria_patrimonio
    ADD CONSTRAINT fk_subcategoria_categoria FOREIGN KEY (id_categoria) REFERENCES public.tabela_categoria_patrimonio(id) ON DELETE SET NULL;


--
-- Name: tabela_coleta tabela_coleta_id_inventario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta
    ADD CONSTRAINT tabela_coleta_id_inventario_fkey FOREIGN KEY (id_inventario) REFERENCES public.tabela_inventario(id);


--
-- Name: tabela_coleta tabela_coleta_id_patrimonio_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coleta
    ADD CONSTRAINT tabela_coleta_id_patrimonio_fkey FOREIGN KEY (id_patrimonio) REFERENCES public.tabela_patrimonio(id);


--
-- Name: tabela_coletor tabela_coletor_id_setor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_coletor
    ADD CONSTRAINT tabela_coletor_id_setor_fkey FOREIGN KEY (id_setor) REFERENCES public.tabela_setor(id);


--
-- Name: tabela_inventario_setor tabela_inventario_setor_id_inventario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario_setor
    ADD CONSTRAINT tabela_inventario_setor_id_inventario_fkey FOREIGN KEY (id_inventario) REFERENCES public.tabela_inventario(id) ON DELETE CASCADE;


--
-- Name: tabela_inventario_setor tabela_inventario_setor_id_setor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_inventario_setor
    ADD CONSTRAINT tabela_inventario_setor_id_setor_fkey FOREIGN KEY (id_setor) REFERENCES public.tabela_setor(id);


--
-- Name: tabela_participante_inventario tabela_participante_inventario_id_inventario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_participante_inventario
    ADD CONSTRAINT tabela_participante_inventario_id_inventario_fkey FOREIGN KEY (id_inventario) REFERENCES public.tabela_inventario(id) ON DELETE CASCADE;


--
-- Name: tabela_participante_inventario tabela_participante_inventario_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_participante_inventario
    ADD CONSTRAINT tabela_participante_inventario_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.tabela_usuario(id) ON DELETE CASCADE;


--
-- Name: tabela_patrimonio tabela_patrimonio_id_responsavel_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_patrimonio
    ADD CONSTRAINT tabela_patrimonio_id_responsavel_fkey FOREIGN KEY (id_responsavel) REFERENCES public.tabela_responsavel(id);


--
-- Name: tabela_patrimonio tabela_patrimonio_id_sala_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_patrimonio
    ADD CONSTRAINT tabela_patrimonio_id_sala_fkey FOREIGN KEY (id_sala) REFERENCES public.tabela_sala(id_sala);


--
-- Name: tabela_responsavel tabela_responsavel_id_setor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_responsavel
    ADD CONSTRAINT tabela_responsavel_id_setor_fkey FOREIGN KEY (id_setor) REFERENCES public.tabela_setor(id);


--
-- Name: tabela_sala tabela_sala_id_setor_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tabela_sala
    ADD CONSTRAINT tabela_sala_id_setor_fkey FOREIGN KEY (id_setor) REFERENCES public.tabela_setor(id);


--
-- PostgreSQL database dump complete
--

\unrestrict d5Hy3vU4lGRdOOLFSmb8a8SQfLeNrCrU27bsBZu1Z2EdaxfhDCxXvGFrAcM1trZ

