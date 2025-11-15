#!/bin/bash

# Script de Deploy em Produção - Sistema de Inventário Mobile
# Automatiza o processo de build e deploy

echo "═══════════════════════════════════════════════════════"
echo "DEPLOY EM PRODUÇÃO - SISTEMA DE INVENTÁRIO MOBILE"
echo "═══════════════════════════════════════════════════════"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para log
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar se está no diretório correto
if [ ! -f "pom.xml" ]; then
    log_error "pom.xml não encontrado. Execute o script na raiz do projeto."
    exit 1
fi

# Passo 1: Verificar Git
log_info "Verificando status do Git..."
if [ -n "$(git status --porcelain)" ]; then
    log_warn "Há mudanças não commitadas no repositório"
    read -p "Deseja continuar? (s/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        log_error "Deploy cancelado"
        exit 1
    fi
fi

# Passo 2: Executar testes
log_info "Executando testes..."
mvn clean test
if [ $? -ne 0 ]; then
    log_error "Testes falharam. Deploy cancelado."
    exit 1
fi
log_info "✓ Testes passaram"

# Passo 3: Build do backend
log_info "Compilando backend..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    log_error "Build do backend falhou"
    exit 1
fi
log_info "✓ Backend compilado"

# Passo 4: Build do Android
log_info "Compilando app Android..."
cd InventarioMobile
./gradlew assembleRelease
if [ $? -ne 0 ]; then
    log_error "Build do Android falhou"
    exit 1
fi
cd ..
log_info "✓ App Android compilado"

# Passo 5: Criar backup do banco
log_info "Criando backup do banco de dados..."
BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
pg_dump -h localhost -U inventario sispatrimonio > "backups/$BACKUP_FILE"
if [ $? -eq 0 ]; then
    log_info "✓ Backup criado: $BACKUP_FILE"
else
    log_warn "Falha ao criar backup (continuando...)"
fi

# Passo 6: Parar servidor atual
log_info "Parando servidor atual..."
pkill -f "sistema-inventario"
sleep 3
log_info "✓ Servidor parado"

# Passo 7: Copiar novo JAR
log_info "Copiando novo JAR..."
cp target/sistema-inventario-*.jar SISTEMA_INVENTARIO_PRODUCAO/
log_info "✓ JAR copiado"

# Passo 8: Iniciar novo servidor
log_info "Iniciando novo servidor..."
cd SISTEMA_INVENTARIO_PRODUCAO
nohup java -jar sistema-inventario-*.jar --spring.profiles.active=mobile > logs/server.log 2>&1 &
SERVER_PID=$!
cd ..
log_info "✓ Servidor iniciado (PID: $SERVER_PID)"

# Passo 9: Aguardar servidor inicializar
log_info "Aguardando servidor inicializar..."
sleep 10

# Passo 10: Verificar saúde do servidor
log_info "Verificando saúde do servidor..."
HEALTH_CHECK=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/mobile/inventario/ativo)
if [ "$HEALTH_CHECK" = "200" ] || [ "$HEALTH_CHECK" = "401" ]; then
    log_info "✓ Servidor está respondendo"
else
    log_error "Servidor não está respondendo corretamente (HTTP $HEALTH_CHECK)"
    log_error "Verifique os logs em SISTEMA_INVENTARIO_PRODUCAO/logs/server.log"
    exit 1
fi

# Passo 11: Copiar APK para distribuição
log_info "Copiando APK para distribuição..."
cp InventarioMobile/app/build/outputs/apk/release/app-release.apk dist/inventario-mobile-$(date +%Y%m%d).apk
log_info "✓ APK copiado"

# Passo 12: Criar tag de versão
VERSION=$(date +%Y.%m.%d-%H%M)
log_info "Criando tag de versão: v$VERSION"
git tag -a "v$VERSION" -m "Deploy em produção - $VERSION"
git push origin "v$VERSION"

echo ""
echo "═══════════════════════════════════════════════════════"
echo -e "${GREEN}DEPLOY CONCLUÍDO COM SUCESSO!${NC}"
echo "═══════════════════════════════════════════════════════"
echo ""
echo "Informações do Deploy:"
echo "  Versão: v$VERSION"
echo "  Backend: SISTEMA_INVENTARIO_PRODUCAO/"
echo "  APK: dist/inventario-mobile-$(date +%Y%m%d).apk"
echo "  Backup: backups/$BACKUP_FILE"
echo "  PID do Servidor: $SERVER_PID"
echo ""
echo "Próximos passos:"
echo "  1. Monitorar logs: tail -f SISTEMA_INVENTARIO_PRODUCAO/logs/server.log"
echo "  2. Testar endpoints críticos"
echo "  3. Distribuir APK para usuários"
echo "  4. Monitorar métricas"
echo ""
