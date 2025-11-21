#!/bin/bash
# ============================================
# Sistema de Inventario - Desktop
# Versao: 2.0.0
# ============================================

echo ""
echo "============================================"
echo "  SISTEMA DE INVENTARIO - DESKTOP"
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

# Configurar memoria JVM
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Configurar encoding
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# Configurar logs
JAVA_OPTS="$JAVA_OPTS -Dlogging.file.name=logs/sistema-inventario.log"

# Configurar profile
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=default"

echo "Iniciando Sistema de Inventario Desktop..."
echo ""

# Criar diretorio de logs se nao existir
mkdir -p logs

# Executar aplicacao
java $JAVA_OPTS -jar sistema-inventario-2.0.0.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERRO] Falha ao iniciar o sistema!"
    echo "Verifique os logs em: logs/sistema-inventario.log"
    exit 1
fi
