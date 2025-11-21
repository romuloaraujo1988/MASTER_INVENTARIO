#!/bin/bash
# ============================================
# Build Desktop - JAR Não Monolítico
# Sistema de Inventário - Aplicação Desktop
# ============================================

set -e  # Parar em caso de erro

echo ""
echo "========================================"
echo "  BUILD DESKTOP - JAR NAO MONOLITICO"
echo "========================================"
echo ""

# Limpar builds anteriores
echo "[1/4] Limpando builds anteriores..."
mvn clean

# Compilar e empacotar com profile thin-jar
echo ""
echo "[2/4] Compilando e empacotando aplicacao desktop..."
mvn package -P thin-jar -DskipTests

# Criar estrutura de distribuição
echo ""
echo "[3/4] Criando estrutura de distribuicao..."

# Criar diretórios
mkdir -p dist/desktop/lib
mkdir -p dist/desktop/config

# Copiar JAR principal
cp target/sistema-inventario-2.0.0.jar dist/desktop/

# Copiar dependências
cp -r target/lib/* dist/desktop/lib/

# Copiar arquivos de configuração
if [ -f "src/main/resources/application.properties" ]; then
    cp src/main/resources/application.properties dist/desktop/config/
fi

# Criar scripts de execução
echo ""
echo "[4/4] Criando scripts de execucao..."

# Script Windows
cat > dist/desktop/executar-desktop.bat << 'EOF'
@echo off
REM ============================================
REM Sistema de Inventario - Aplicacao Desktop
REM ============================================

echo Iniciando Sistema de Inventario Desktop...
echo.

REM Verificar Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Java nao encontrado!
    echo Instale Java 21 ou superior
    pause
    exit /b 1
)

REM Executar aplicacao
java -jar sistema-inventario-2.0.0.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERRO: Falha ao executar aplicacao
    pause
    exit /b 1
)
EOF

# Script Linux/Mac
cat > dist/desktop/executar-desktop.sh << 'EOF'
#!/bin/bash
# ============================================
# Sistema de Inventario - Aplicacao Desktop
# ============================================

echo "Iniciando Sistema de Inventario Desktop..."
echo

# Verificar Java
if ! command -v java &> /dev/null; then
    echo "ERRO: Java nao encontrado!"
    echo "Instale Java 21 ou superior"
    exit 1
fi

# Executar aplicacao
java -jar sistema-inventario-2.0.0.jar

if [ $? -ne 0 ]; then
    echo
    echo "ERRO: Falha ao executar aplicacao"
    exit 1
fi
EOF

# Tornar script executável
chmod +x dist/desktop/executar-desktop.sh

# Criar README
cat > dist/desktop/README.txt << EOF
============================================
SISTEMA DE INVENTARIO - APLICACAO DESKTOP
============================================

VERSAO: 2.0.0
DATA: $(date +%Y-%m-%d)

ESTRUTURA:
  sistema-inventario-2.0.0.jar  - Aplicacao principal (~2MB)
  lib/                          - Dependencias externas (~150MB)
  config/                       - Arquivos de configuracao
  executar-desktop.bat          - Script Windows
  executar-desktop.sh           - Script Linux/Mac

REQUISITOS:
  - Java 21 ou superior
  - PostgreSQL 12+ (configurado e rodando)
  - 4GB RAM minimo
  - 500MB espaco em disco

EXECUCAO:
  Windows: Execute executar-desktop.bat
  Linux/Mac: Execute ./executar-desktop.sh
  Manual: java -jar sistema-inventario-2.0.0.jar

CONFIGURACAO:
  1. Configure o banco de dados em:
     Windows: %USERPROFILE%\.inventario\configuracao_banco.json
     Linux/Mac: ~/.inventario/configuracao_banco.json

  2. Exemplo de configuracao:
     {
       "host": "localhost",
       "porta": "5432",
       "database": "sispatrimonio",
       "usuario": "inventario",
       "senha": "sua_senha"
     }

PRIMEIRO USO:
  1. Instale PostgreSQL
  2. Crie o banco de dados: sispatrimonio
  3. Execute os scripts SQL em sql/
  4. Configure o arquivo configuracao_banco.json
  5. Execute a aplicacao

SUPORTE:
  Email: suporte@inventario.com
  Documentacao: docs/

EOF

# Calcular tamanhos
echo ""
echo "========================================"
echo "  BUILD CONCLUIDO COM SUCESSO!"
echo "========================================"
echo ""
echo "Estrutura criada em: dist/desktop/"
echo ""
echo "Arquivos gerados:"
ls -lh dist/desktop/*.jar
echo "  + $(pwd)/dist/desktop/lib/ (dependencias)"
echo "  + executar-desktop.bat"
echo "  + executar-desktop.sh"
echo "  + README.txt"
echo ""

# Mostrar tamanho do JAR principal
jar_size=$(du -h dist/desktop/sistema-inventario-2.0.0.jar | cut -f1)
echo "Tamanho JAR principal: $jar_size"

# Mostrar tamanho total das dependências
lib_size=$(du -sh dist/desktop/lib | cut -f1)
echo "Tamanho dependencias: $lib_size"

echo ""
echo "Para executar:"
echo "  cd dist/desktop"
echo "  ./executar-desktop.sh"
echo ""
