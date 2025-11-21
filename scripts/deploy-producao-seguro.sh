#!/bin/bash

###############################################################################
# Script de Deploy Seguro - Sistema de Inventário Mobile
# Versão: 1.0
# Data: 16/11/2025
#
# Uso: ./deploy-producao-seguro.sh [backend|android|full]
###############################################################################

set -e  # Parar em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
BACKUP_DIR="backup/$(date +%Y%m%d_%H%M%S)"
LOG_FILE="logs/deploy_$(date +%Y%m%d_%H%M%S).log"
DB_NAME="sispatrimonio"
DB_USER="inventario"

# Funções auxiliares
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}✅ $1${NC}" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}❌ $1${NC}" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}⚠️  $1${NC}" | tee -a "$LOG_FILE"
}

confirm() {
    read -p "$(echo -e ${YELLOW}$1${NC}) [y/N]: " response
    case "$response" in
        [yY][eE][sS]|[yY]) 
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

# Verificar pré-requisitos
check_prerequisites() {
    log "Verificando pré-requisitos..."
    
    # Verificar se está no diretório correto
    if [ ! -f "pom.xml" ]; then
        error "Execute este script no diretório raiz do projeto"
        exit 1
    fi
    
    # Verificar Java
    if ! command -v java &> /dev/null; then
        error "Java não encontrado"
        exit 1
    fi
    
    # Verificar Maven
    if [ ! -f "mvnw" ]; then
        error "Maven wrapper não encontrado"
        exit 1
    fi
    
    # Verificar PostgreSQL
    if ! command -v psql &> /dev/null; then
        warning "psql não encontrado - backup do banco não será possível"
    fi
    
    success "Pré-requisitos OK"
}

# Criar backup do banco de dados
backup_database() {
    log "Criando backup do banco de dados..."
    
    mkdir -p "$BACKUP_DIR"
    
    if command -v pg_dump &> /dev/null; then
        pg_dump -h localhost -U "$DB_USER" "$DB_NAME" > "$BACKUP_DIR/database_backup.sql"
        
        if [ $? -eq 0 ]; then
            success "Backup do banco criado: $BACKUP_DIR/database_backup.sql"
        else
            error "Falha ao criar backup do banco"
            return 1
        fi
    else
        warning "pg_dump não disponível - pulando backup do banco"
    fi
}

# Criar backup do código
backup_code() {
    log "Criando backup do código..."
    
    mkdir -p "$BACKUP_DIR"
    
    # Backup do JAR atual
    if [ -f "target/sistema-inventario-*.jar" ]; then
        cp target/sistema-inventario-*.jar "$BACKUP_DIR/"
        success "Backup do JAR criado"
    fi
    
    # Tag no Git
    git tag -a "v2.0.0-backup-$(date +%Y%m%d_%H%M%S)" -m "Backup antes do deploy"
    git push origin --tags
    
    success "Backup do código criado"
}

# Parar servidor backend
stop_backend() {
    log "Parando servidor backend..."
    
    # Encontrar processo Java
    PID=$(ps aux | grep java | grep MobileApiApplication | grep -v grep | awk '{print $2}')
    
    if [ -z "$PID" ]; then
        warning "Servidor backend não está rodando"
        return 0
    fi
    
    log "Processo encontrado: PID $PID"
    
    # Parar gracefully
    kill -15 "$PID"
    
    # Aguardar até 30 segundos
    for i in {1..30}; do
        if ! ps -p "$PID" > /dev/null 2>&1; then
            success "Servidor parado com sucesso"
            return 0
        fi
        sleep 1
    done
    
    # Forçar se necessário
    warning "Forçando parada do servidor..."
    kill -9 "$PID"
    sleep 2
    
    if ! ps -p "$PID" > /dev/null 2>&1; then
        success "Servidor parado (forçado)"
    else
        error "Falha ao parar servidor"
        return 1
    fi
}

# Compilar backend
compile_backend() {
    log "Compilando backend..."
    
    ./mvnw clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        success "Backend compilado com sucesso"
    else
        error "Falha na compilação do backend"
        return 1
    fi
}

# Iniciar servidor backend
start_backend() {
    log "Iniciando servidor backend..."
    
    nohup java -jar target/sistema-inventario-*.jar \
        --spring.profiles.active=mobile \
        > logs/server.log 2>&1 &
    
    PID=$!
    log "Servidor iniciado com PID: $PID"
    
    # Aguardar inicialização
    log "Aguardando inicialização do servidor..."
    for i in {1..60}; do
        if grep -q "Started MobileApiApplication" logs/server.log; then
            success "Servidor iniciado com sucesso"
            return 0
        fi
        sleep 1
    done
    
    error "Timeout ao iniciar servidor"
    return 1
}

# Testar backend
test_backend() {
    log "Testando backend..."
    
    # Aguardar servidor estar pronto
    sleep 5
    
    # Testar endpoint de saúde
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/mobile/dashboard/stats)
    
    if [ "$response" = "200" ] || [ "$response" = "401" ]; then
        success "Backend respondendo corretamente (HTTP $response)"
    else
        error "Backend não está respondendo corretamente (HTTP $response)"
        return 1
    fi
}

# Compilar Android
compile_android() {
    log "Compilando Android..."
    
    cd InventarioMobile
    
    # Limpar build anterior
    ./gradlew clean
    
    # Compilar release
    ./gradlew assembleRelease
    
    if [ $? -eq 0 ]; then
        success "Android compilado com sucesso"
        cd ..
    else
        error "Falha na compilação do Android"
        cd ..
        return 1
    fi
}

# Assinar APK
sign_apk() {
    log "Assinando APK..."
    
    APK_UNSIGNED="InventarioMobile/app/build/outputs/apk/release/app-release-unsigned.apk"
    APK_SIGNED="InventarioMobile/app/build/outputs/apk/release/app-release-v2.0.1.apk"
    KEYSTORE="inventario.keystore"
    
    if [ ! -f "$KEYSTORE" ]; then
        warning "Keystore não encontrado - APK não será assinado"
        return 0
    fi
    
    # Assinar
    jarsigner -verbose \
        -sigalg SHA256withRSA \
        -digestalg SHA-256 \
        -keystore "$KEYSTORE" \
        "$APK_UNSIGNED" \
        inventario
    
    # Otimizar
    zipalign -v 4 "$APK_UNSIGNED" "$APK_SIGNED"
    
    if [ -f "$APK_SIGNED" ]; then
        success "APK assinado: $APK_SIGNED"
    else
        error "Falha ao assinar APK"
        return 1
    fi
}

# Rollback
rollback() {
    error "Iniciando rollback..."
    
    # Parar servidor
    stop_backend
    
    # Restaurar JAR
    if [ -f "$BACKUP_DIR/sistema-inventario-*.jar" ]; then
        cp "$BACKUP_DIR/sistema-inventario-*.jar" target/
        success "JAR restaurado"
    fi
    
    # Iniciar servidor
    start_backend
    
    success "Rollback concluído"
}

# Deploy backend
deploy_backend() {
    log "========================================="
    log "DEPLOY BACKEND"
    log "========================================="
    
    if ! confirm "Deseja continuar com o deploy do backend?"; then
        warning "Deploy cancelado pelo usuário"
        exit 0
    fi
    
    # Backup
    backup_database || { error "Falha no backup do banco"; exit 1; }
    backup_code || { error "Falha no backup do código"; exit 1; }
    
    # Parar servidor
    stop_backend || { error "Falha ao parar servidor"; exit 1; }
    
    # Compilar
    compile_backend || { error "Falha na compilação"; rollback; exit 1; }
    
    # Iniciar
    start_backend || { error "Falha ao iniciar servidor"; rollback; exit 1; }
    
    # Testar
    test_backend || { error "Falha nos testes"; rollback; exit 1; }
    
    success "========================================="
    success "DEPLOY BACKEND CONCLUÍDO COM SUCESSO"
    success "========================================="
}

# Deploy Android
deploy_android() {
    log "========================================="
    log "DEPLOY ANDROID"
    log "========================================="
    
    if ! confirm "Deseja continuar com o deploy do Android?"; then
        warning "Deploy cancelado pelo usuário"
        exit 0
    fi
    
    # Compilar
    compile_android || { error "Falha na compilação"; exit 1; }
    
    # Assinar
    sign_apk || warning "APK não assinado"
    
    success "========================================="
    success "DEPLOY ANDROID CONCLUÍDO COM SUCESSO"
    success "========================================="
    success "APK disponível em: InventarioMobile/app/build/outputs/apk/release/"
}

# Deploy completo
deploy_full() {
    log "========================================="
    log "DEPLOY COMPLETO (BACKEND + ANDROID)"
    log "========================================="
    
    if ! confirm "Deseja continuar com o deploy completo?"; then
        warning "Deploy cancelado pelo usuário"
        exit 0
    fi
    
    deploy_backend
    deploy_android
    
    success "========================================="
    success "DEPLOY COMPLETO CONCLUÍDO COM SUCESSO"
    success "========================================="
}

# Main
main() {
    log "========================================="
    log "SCRIPT DE DEPLOY SEGURO"
    log "Versão: 1.0"
    log "Data: $(date)"
    log "========================================="
    
    check_prerequisites
    
    case "${1:-}" in
        backend)
            deploy_backend
            ;;
        android)
            deploy_android
            ;;
        full)
            deploy_full
            ;;
        *)
            echo "Uso: $0 [backend|android|full]"
            echo ""
            echo "Opções:"
            echo "  backend  - Deploy apenas do backend"
            echo "  android  - Deploy apenas do Android"
            echo "  full     - Deploy completo (backend + android)"
            exit 1
            ;;
    esac
    
    log "========================================="
    log "Log completo salvo em: $LOG_FILE"
    log "Backup salvo em: $BACKUP_DIR"
    log "========================================="
}

# Executar
main "$@"
