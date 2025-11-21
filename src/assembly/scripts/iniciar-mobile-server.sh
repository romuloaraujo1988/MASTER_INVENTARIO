#!/bin/bash
# ============================================
# Sistema de Inventario - Mobile API Server
# Versao: 2.0.0
# ============================================

echo ""
echo "============================================"
echo "  SISTEMA DE INVENTARIO - MOBILE SERVER"
echo "  Versao 2.0.0"
echo "============================================"
echo ""

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java nao encontrado!"
    echo "Por favor, instale o Java 21 ou superior."
    echo "Download: https://adoptium.net/"
    exit 1
fi

# Verificar versao do Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "[ERRO] Java 21 ou superior e necessario!"
    echo "Versao atual: $JAVA_VERSION"
    exit 1
fi

echo "[OK] Java $JAVA_VERSION detectado"
echo ""

# Configurar memoria JVM (servidor precisa de mais memoria)
JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Configurar encoding
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# Configurar logs
JAVA_OPTS="$JAVA_OPTS -Dlogging.file.name=logs/mobile-server.log"

# Configurar profile mobile
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=mobile"

# Configurar porta (padrao 8080)
JAVA_OPTS="$JAVA_OPTS -Dserver.port=8080"

echo "Iniciando Mobile API Server..."
echo "Porta: 8080"
echo "Swagger: http://localhost:8080/inventario/swagger-ui.html"
echo ""

# Criar diretorio de logs se nao existir
mkdir -p logs

# Executar aplicacao
java $JAVA_OPTS -jar sistema-inventario-2.0.0.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERRO] Falha ao iniciar o servidor!"
    echo "Verifique os logs em: logs/mobile-server.log"
    exit 1
fi
