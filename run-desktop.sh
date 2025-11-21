#!/bin/bash
# ========================================
# Script para executar aplicação Desktop
# Sistema de Inventário - Versão 2.0.0
# ========================================

echo ""
echo "========================================"
echo "  Sistema de Inventário - Desktop"
echo "  Versão 2.0.0"
echo "========================================"
echo ""

# Verificar se Java está instalado
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java não encontrado!"
    echo "Por favor, instale o Java 21 ou superior."
    exit 1
fi

# Verificar se o JAR existe
if [ ! -f "target/mobile-server/sistema-inventario-2.0.0.jar" ]; then
    echo "[ERRO] JAR não encontrado!"
    echo "Execute primeiro: ./mvnw clean package -P thin-jar -DskipTests"
    exit 1
fi

# Verificar se o diretório lib existe
if [ ! -d "target/lib" ]; then
    echo "[ERRO] Diretório lib/ não encontrado!"
    echo "Execute primeiro: ./mvnw clean package -P thin-jar -DskipTests"
    exit 1
fi

echo "[INFO] Iniciando aplicação desktop..."
echo "[INFO] JAR: target/mobile-server/sistema-inventario-2.0.0.jar"
echo "[INFO] Dependências: target/lib/"
echo ""

# Executar aplicação desktop (SistemaInventarioApplication)
cd target/mobile-server
java -jar sistema-inventario-2.0.0.jar
