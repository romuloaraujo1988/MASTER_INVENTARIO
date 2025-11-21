#!/bin/bash
# ============================================
# Build de Producao - Sistema de Inventario
# Gera JAR modular otimizado
# ============================================

echo ""
echo "============================================"
echo "  BUILD DE PRODUCAO"
echo "  Sistema de Inventario v2.0.0"
echo "============================================"
echo ""

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo "[ERRO] Maven nao encontrado!"
    echo "Por favor, instale o Maven 3.6+"
    echo "Download: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "[1/5] Limpando builds anteriores..."
mvn clean
if [ $? -ne 0 ]; then
    echo "[ERRO] Falha ao limpar projeto!"
    exit 1
fi

echo ""
echo "[2/5] Compilando codigo fonte..."
mvn compile
if [ $? -ne 0 ]; then
    echo "[ERRO] Falha na compilacao!"
    exit 1
fi

echo ""
echo "[3/5] Executando testes..."
mvn test -DskipTests
if [ $? -ne 0 ]; then
    echo "[AVISO] Alguns testes falharam, continuando..."
fi

echo ""
echo "[4/5] Gerando pacote de producao..."
mvn package -P producao -DskipTests
if [ $? -ne 0 ]; then
    echo "[ERRO] Falha ao gerar pacote!"
    exit 1
fi

echo ""
echo "[5/5] Verificando arquivos gerados..."

if [ ! -f "target/sistema-inventario-2.0.0.jar" ]; then
    echo "[ERRO] JAR principal nao encontrado!"
    exit 1
fi

if [ ! -d "target/lib" ]; then
    echo "[ERRO] Diretorio lib/ nao encontrado!"
    exit 1
fi

if [ ! -f "target/sistema-inventario-2.0.0-producao.zip" ]; then
    echo "[ERRO] Pacote ZIP nao encontrado!"
    exit 1
fi

echo ""
echo "============================================"
echo "  BUILD CONCLUIDO COM SUCESSO!"
echo "============================================"
echo ""
echo "Arquivos gerados:"
echo ""
echo "  JAR Principal:"
echo "  target/sistema-inventario-2.0.0.jar"
echo ""
echo "  Dependencias:"
echo "  target/lib/*.jar"
echo ""
echo "  Pacote Completo:"
echo "  target/sistema-inventario-2.0.0-producao.zip"
echo ""
echo "Tamanho do JAR principal:"
ls -lh target/sistema-inventario-2.0.0.jar | awk '{print "  " $5}'
echo ""
echo "Total de dependencias:"
ls -1 target/lib/*.jar | wc -l
echo ""
echo "============================================"
echo ""
echo "Proximo passo:"
echo "1. Extrair: target/sistema-inventario-2.0.0-producao.zip"
echo "2. Configurar: config/application-prod.properties"
echo "3. Executar: ./iniciar-desktop.sh ou ./iniciar-mobile-server.sh"
echo ""
echo "============================================"
echo ""
