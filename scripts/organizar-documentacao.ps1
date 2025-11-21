# Script de Organização da Documentação
# Versão: 1.0.0
# Data: 16/11/2025

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Organização da Documentação v1.0.0" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Criar backup
Write-Host "[1/6] Criando backup..." -ForegroundColor Yellow
$backupDir = "docs/backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
Copy-Item -Path "*.md" -Destination $backupDir -Force
Write-Host "✅ Backup criado em: $backupDir" -ForegroundColor Green
Write-Host ""

# 2. Criar estrutura de diretórios
Write-Host "[2/6] Criando estrutura de diretórios..." -ForegroundColor Yellow
$dirs = @(
    "docs/archive/correcoes",
    "docs/archive/migracao",
    "docs/archive/versoes-antigas",
    "docs/archive/funcionalidades",
    "docs/archive/arquitetura",
    "docs/archive/otimizacoes",
    "docs/archive/integracoes",
    "docs/archive/infraestrutura",
    "docs/archive/diversos"
)

foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Write-Host "  ✓ $dir" -ForegroundColor Gray
}
Write-Host "✅ Estrutura criada" -ForegroundColor Green
Write-Host ""

# 3. Mover arquivos
Write-Host "[3/6] Movendo arquivos..." -ForegroundColor Yellow

# Correções
$correcoes = @(
    "CORRECAO_DADOS_GRAFICOS.md",
    "CORRECAO_ENDPOINT_DASHBOARD.md",
    "CORRECAO_INSTABILIDADE_APP.md",
    "CORRECAO_SPINNER_SALAS_COLETAS.md",
    "CORRECAO_TELA_ITENS_COLETADOS.md",
    "CORRECOES_DASHBOARD_GRAFICO.md",
    "CORRECOES_FINALIZADAS_16NOV.md",
    "CORRECOES_GRAFICOS.md",
    "DASHBOARD_CORRECOES_RESUMO.md",
    "DIAGNOSTICO_DASHBOARD.md",
    "INSTRUCOES_CORRECAO_DASHBOARD.md",
    "PROBLEMA_COMBOBOX_RESPONSAVEIS.md",
    "RESUMO_ENDPOINT_RESPONSAVEIS.md",
    "SOLUCAO_RESPONSAVEIS_NAO_CARREGAM.md",
    "SOLUCAO-RESPONSAVEIS.md"
)

foreach ($file in $correcoes) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/correcoes/" -Force
        Write-Host "  ✓ $file → correcoes/" -ForegroundColor Gray
    }
}

# Migração
$migracao = @(
    "CLEAN_ARCHITECTURE_SUMMARY.md",
    "IMPLEMENTACAO_COMPLETA.md",
    "MIGRACAO_CONCLUIDA.md",
    "MIGRACAO_FINAL.md",
    "MIGRACAO_STATUS.md",
    "REFATORACAO_MVVM_COMPLETA.md"
)

foreach ($file in $migracao) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/migracao/" -Force
        Write-Host "  ✓ $file → migracao/" -ForegroundColor Gray
    }
}

# Versões antigas
$versoes = @(
    "RESUMO_FINAL_COMPLETO.md",
    "RESUMO_OTIMIZACOES.md",
    "RESUMO_VERIFICACAO_MAPEAMENTOS.md",
    "STATUS-APP-ANDROID.md",
    "STATUS_PAGINACAO_SALAS.md",
    "VERSAO_2.0.0_RESUMO.md"
)

foreach ($file in $versoes) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/versoes-antigas/" -Force
        Write-Host "  ✓ $file → versoes-antigas/" -ForegroundColor Gray
    }
}

# Funcionalidades
$funcionalidades = @(
    "API_MOBILE_DESCRICOES_PENDENTES.md",
    "FUNCIONALIDADE_FILTRO_PENDENTES.md",
    "IMPLEMENTACAO_PAGINACAO_COLETAS.md",
    "PAGINACAO_COLETAS_IMPLEMENTADA.md",
    "PARTE1_SYNC_OFFLINE.md",
    "PARTE2_ANDROID_DATABASE.md",
    "PARTE3_ANDROID_SYNC_SERVICE.md",
    "PARTE4_ANDROID_UI_SYNC.md",
    "PARTE5_LAYOUT_E_RESUMO.md"
)

foreach ($file in $funcionalidades) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/funcionalidades/" -Force
        Write-Host "  ✓ $file → funcionalidades/" -ForegroundColor Gray
    }
}

# Arquitetura
$arquitetura = @(
    "ARQUITETURA_ESCALAVEL.md",
    "ARQUITETURAS_MODERNAS_APPS.md",
    "ESCALABILIDADE_RESUMO.md",
    "GUIA_ESCALABILIDADE.md",
    "README_ESCALABILIDADE_INICIO_AQUI.md"
)

foreach ($file in $arquitetura) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/arquitetura/" -Force
        Write-Host "  ✓ $file → arquitetura/" -ForegroundColor Gray
    }
}

# Otimizações
$otimizacoes = @(
    "CONFIGURAR_COMPRESSAO_SERVIDOR.md",
    "OTIMIZACAO_APP_MOBILE_COLETA.md",
    "OTIMIZACOES_REDE_IMPLEMENTADAS.md",
    "RECOMENDACOES_APP_ANDROID.md"
)

foreach ($file in $otimizacoes) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/otimizacoes/" -Force
        Write-Host "  ✓ $file → otimizacoes/" -ForegroundColor Gray
    }
}

# Integrações
$integracoes = @(
    "SIADS_ANALISE_CAMPOS.md",
    "SIADS_INSTRUCOES.md",
    "SIADS_INTEGRACAO_MENU.md"
)

foreach ($file in $integracoes) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/integracoes/" -Force
        Write-Host "  ✓ $file → integracoes/" -ForegroundColor Gray
    }
}

# Infraestrutura
if (Test-Path "DOCKER_QUICKSTART.md") {
    Move-Item -Path "DOCKER_QUICKSTART.md" -Destination "docs/archive/infraestrutura/" -Force
    Write-Host "  ✓ DOCKER_QUICKSTART.md → infraestrutura/" -ForegroundColor Gray
}

# Diversos
$diversos = @(
    "ICONES_SISTEMA.md",
    "INDICE_DOCUMENTACAO.md",
    "INICIO_RAPIDO.md",
    "PROXIMOS_PASSOS.md"
)

foreach ($file in $diversos) {
    if (Test-Path $file) {
        Move-Item -Path $file -Destination "docs/archive/diversos/" -Force
        Write-Host "  ✓ $file → diversos/" -ForegroundColor Gray
    }
}

Write-Host "✅ Arquivos movidos" -ForegroundColor Green
Write-Host ""

# 4. Deletar arquivos vazios
Write-Host "[4/6] Deletando arquivos vazios..." -ForegroundColor Yellow
$vazios = @(
    "README_ESCALABILIDADE.md"
)

foreach ($file in $vazios) {
    if (Test-Path $file) {
        Remove-Item -Path $file -Force
        Write-Host "  ✓ $file deletado" -ForegroundColor Gray
    }
}
Write-Host "✅ Arquivos vazios removidos" -ForegroundColor Green
Write-Host ""

# 5. Atualizar README
Write-Host "[5/6] Atualizando README.md..." -ForegroundColor Yellow
if (Test-Path "README_NOVO.md") {
    Copy-Item -Path "README_NOVO.md" -Destination "README.md" -Force
    Write-Host "✅ README.md atualizado" -ForegroundColor Green
} else {
    Write-Host "⚠️  README_NOVO.md não encontrado" -ForegroundColor Yellow
}
Write-Host ""

# 6. Relatório final
Write-Host "[6/6] Gerando relatório..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Organização Concluída!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Contar arquivos
$totalMovidos = (Get-ChildItem -Path "docs/archive" -Recurse -File).Count
$totalNaRaiz = (Get-ChildItem -Path "." -Filter "*.md" -File).Count

Write-Host "📊 Estatísticas:" -ForegroundColor Cyan
Write-Host "  • Arquivos movidos: $totalMovidos" -ForegroundColor White
Write-Host "  • Arquivos na raiz: $totalNaRaiz" -ForegroundColor White
Write-Host "  • Backup criado: $backupDir" -ForegroundColor White
Write-Host ""

Write-Host "📁 Estrutura final:" -ForegroundColor Cyan
Write-Host "  MASTER_INVENTARIO/" -ForegroundColor White
Write-Host "  ├── README.md" -ForegroundColor Green
Write-Host "  ├── DOCUMENTACAO_CONSOLIDADA.md" -ForegroundColor Green
Write-Host "  ├── CHANGELOG.md" -ForegroundColor Green
Write-Host "  ├── ESTATISTICAS_ESTRATEGICAS_INVENTARIO.md" -ForegroundColor Green
Write-Host "  ├── APRESENTACAO_VALOR_DIGITALIZACAO.md" -ForegroundColor Green
Write-Host "  ├── IMPLEMENTACAO_ESTATISTICAS_APP.md" -ForegroundColor Green
Write-Host "  ├── PLANO_IMPLANTACAO_PRODUCAO.md" -ForegroundColor Green
Write-Host "  ├── CHECKLIST_DEPLOY.md" -ForegroundColor Green
Write-Host "  ├── RESUMO_EXECUTIVO_DEPLOY.md" -ForegroundColor Green
Write-Host "  ├── VERIFICACAO_MAPEAMENTOS.md" -ForegroundColor Green
Write-Host "  └── docs/archive/" -ForegroundColor Yellow
Write-Host "      ├── correcoes/" -ForegroundColor Gray
Write-Host "      ├── migracao/" -ForegroundColor Gray
Write-Host "      ├── versoes-antigas/" -ForegroundColor Gray
Write-Host "      ├── funcionalidades/" -ForegroundColor Gray
Write-Host "      ├── arquitetura/" -ForegroundColor Gray
Write-Host "      ├── otimizacoes/" -ForegroundColor Gray
Write-Host "      ├── integracoes/" -ForegroundColor Gray
Write-Host "      ├── infraestrutura/" -ForegroundColor Gray
Write-Host "      └── diversos/" -ForegroundColor Gray
Write-Host ""

Write-Host "✅ Documentação organizada com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "📚 Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Revisar README.md" -ForegroundColor White
Write-Host "  2. Revisar DOCUMENTACAO_CONSOLIDADA.md" -ForegroundColor White
Write-Host "  3. Fazer commit: git commit -m 'docs: Organizar documentação'" -ForegroundColor White
Write-Host "  4. Criar tag: git tag v2.0.1-docs" -ForegroundColor White
Write-Host ""
