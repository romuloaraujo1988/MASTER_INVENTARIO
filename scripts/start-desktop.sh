#!/bin/bash
# ============================================
# Sistema de Inventario - Desktop
# Versao: ${project.version}
# ============================================

echo ""
echo "========================================"
echo " Sistema de Inventario - IFMT"
echo " Versao: ${project.version}"
echo "========================================"
echo ""

# Verificar se Java esta instalado
if ! command -v java &> /dev/null; then
    echo "ERRO: Java nao encontrado!"
    echo "Por favor, instale o Java 21 ou superior."
    echo ""
    exit 1
fi

# Verificar versao do Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "AVISO: Java $JAVA_VERSION detectado. Recomendado Java 21 ou superior."
    echo ""
fi

# Configurar memoria JVM
JAVA_OPTS="-Xms512m -Xmx2048m"

# Configurar encoding
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

# Configurar Look and Feel nativo
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    JAVA_OPTS="$JAVA_OPTS -Dapple.laf.useScreenMenuBar=true"
else
    # Linux
    JAVA_OPTS="$JAVA_OPTS -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
fi

# Diretorio de logs
mkdir -p logs

echo "Iniciando aplicacao..."
echo ""

# Executar aplicacao
java $JAVA_OPTS -jar sistema-inventario-${project.version}.jar

if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha ao iniciar a aplicacao."
    echo "Verifique o arquivo de log em logs/sistema-inventario.log"
    echo ""
    exit 1
fi
