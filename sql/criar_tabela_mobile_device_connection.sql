-- Tabela para rastrear conexões de dispositivos móveis
-- Armazena histórico de conexões e permite monitoramento em tempo real

CREATE TABLE IF NOT EXISTS mobile_device_connection (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    device_model VARCHAR(255),
    android_version VARCHAR(50),
    app_version VARCHAR(50),
    ip_address VARCHAR(45),
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    disconnected_at TIMESTAMP,
    request_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_mobile_device_username ON mobile_device_connection(username);
CREATE INDEX IF NOT EXISTS idx_mobile_device_connected_at ON mobile_device_connection(connected_at);
CREATE INDEX IF NOT EXISTS idx_mobile_device_last_activity ON mobile_device_connection(last_activity);
CREATE INDEX IF NOT EXISTS idx_mobile_device_active ON mobile_device_connection(disconnected_at) WHERE disconnected_at IS NULL;

-- Comentários
COMMENT ON TABLE mobile_device_connection IS 'Rastreamento de dispositivos móveis conectados ao servidor';
COMMENT ON COLUMN mobile_device_connection.device_id IS 'Identificador único do dispositivo (UUID)';
COMMENT ON COLUMN mobile_device_connection.username IS 'Nome do usuário logado no dispositivo';
COMMENT ON COLUMN mobile_device_connection.device_model IS 'Modelo do dispositivo (ex: Samsung Galaxy S21)';
COMMENT ON COLUMN mobile_device_connection.android_version IS 'Versão do Android (ex: 13)';
COMMENT ON COLUMN mobile_device_connection.app_version IS 'Versão do aplicativo mobile (ex: 1.2.0)';
COMMENT ON COLUMN mobile_device_connection.ip_address IS 'Endereço IP do dispositivo';
COMMENT ON COLUMN mobile_device_connection.connected_at IS 'Data/hora da primeira conexão';
COMMENT ON COLUMN mobile_device_connection.last_activity IS 'Data/hora da última atividade';
COMMENT ON COLUMN mobile_device_connection.disconnected_at IS 'Data/hora da desconexão (NULL se ainda conectado)';
COMMENT ON COLUMN mobile_device_connection.request_count IS 'Número total de requisições feitas';

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_mobile_device_connection_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_mobile_device_connection_updated_at
    BEFORE UPDATE ON mobile_device_connection
    FOR EACH ROW
    EXECUTE FUNCTION update_mobile_device_connection_updated_at();

-- View para conexões ativas (últimos 5 minutos)
CREATE OR REPLACE VIEW mobile_active_connections AS
SELECT 
    device_id,
    username,
    device_model,
    android_version,
    app_version,
    ip_address,
    connected_at,
    last_activity,
    request_count,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - connected_at))/60 AS connection_duration_minutes,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_activity))/60 AS idle_minutes
FROM mobile_device_connection
WHERE disconnected_at IS NULL
  AND last_activity > CURRENT_TIMESTAMP - INTERVAL '5 minutes'
ORDER BY last_activity DESC;

COMMENT ON VIEW mobile_active_connections IS 'Conexões móveis ativas (últimos 5 minutos)';

-- Procedure para limpar conexões antigas (mais de 30 dias)
CREATE OR REPLACE FUNCTION cleanup_old_mobile_connections()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM mobile_device_connection
    WHERE disconnected_at IS NOT NULL
      AND disconnected_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_mobile_connections() IS 'Remove conexões desconectadas há mais de 30 dias';

-- Dados de exemplo (opcional - remover em produção)
-- INSERT INTO mobile_device_connection (device_id, username, device_model, android_version, app_version, ip_address)
-- VALUES 
--     ('device-001', 'admin', 'Samsung Galaxy S21', '13', '1.2.0', '192.168.1.100'),
--     ('device-002', 'operador1', 'Xiaomi Redmi Note 10', '12', '1.2.0', '192.168.1.101');

COMMIT;
