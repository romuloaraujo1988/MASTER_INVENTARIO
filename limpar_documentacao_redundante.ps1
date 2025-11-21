# Script para limpar documentação redundante do modo offline
# Mantém apenas os documentos essenciais

Write-Host "=== LIMPEZA DE DOCUMENTAÇÃO REDUNDANTE ===" -ForegroundColor Cyan
Write-Host ""

# Documentos a MANTER (essenciais)
$manter = @(
    "MODO_OFFLINE_GUIA_COMPLETO.md",
    "IMPLEMENTACAO_COMPLETA_100_PORCENTO.md",
    "IMPLEMENTACAO_MODO_OFFLINE_PROGRESSO.md"
)

# Documentos a REMOVER (redundantes sobre modo offline)
$remover = @(
    "APK_LOGIN_OFFLINE_BIOMETRIA_COMPILADO.md",
    "CORRECAO_SINCRONIZACAO_OFFLINE.md",
    "CORRECOES_ENDPOINT_OFFLINE_SYNC.md",
    "ENDPOINT_SINCRONIZACAO_OFFLINE_DEDICADO.md",
    "FASE5_GARANTIR_COLETA_OFFLINE.md",
    "FLUXO_LOGIN_OFFLINE_COMPLETO.md",
    "INDICE_LOGIN_OFFLINE_PIN.md",
    "INSTRUCOES_MODO_OFFLINE_EQUIPE.md",
    "INTEGRACAO_COLETAFRAME_OFFLINE.md",
    "LOGIN_OFFLINE_BIOMETRIA_ANALISE.md",
    "LOGIN_OFFLINE_IMPLEMENTACAO_PROGRESSO.md",
    "LOGIN_OFFLINE_IMPLEMENTADO_SUCESSO.md",
    "MODO_OFFLINE_COMPLETO_ANALISE.md",
    "MODO_OFFLINE_COMPLETO.md",
    "MODO_OFFLINE_RESUMO_FINAL.md",
    "PARTE1_SYNC_OFFLINE.md",
    "PLANO_LOGIN_OFFLINE_SEM_BIOMETRIA.md",
    "PLANO_MODO_OFFLINE_COMPLETO.md",
    "README_MODO_OFFLINE.md",
    "RESUMO_FINAL_IMPLEMENTACAO_OFFLINE.md",
    "RESUMO_FINAL_MODO_OFFLINE.md",
    "RESUMO_LOGIN_OFFLINE_PIN.md",
    "RESUMO_MODO_OFFLINE_EXECUTIVO.md",
    "RESUMO_SESSAO_21NOV_MODO_OFFLINE.md",
    "SINCRONIZACAO_DADOS_OFFLINE.md",
    "SINCRONIZACAO_OFFLINE_CORRIGIDA_FINAL.md",
    "SINCRONIZACAO_OFFLINE_IMPLEMENTADA.md",
    "STATUS_ENDPOINT_OFFLINE_SYNC.md",
    "TESTE_MODO_OFFLINE.md",
    "FASE4_INTEGRACAO_MAINFRAME_COMPLETA.md",
    "RESUMO_SESSAO_21NOV_FASE4.md",
    "RESUMO_SESSAO_21NOV_FASE5_INICIO.md"
)

Write-Host "📋 DOCUMENTOS A MANTER:" -ForegroundColor Green
foreach ($doc in $manter) {
    if (Test-Path $doc) {
        Write-Host "  ✅ $doc" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  $doc (não encontrado)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🗑️  DOCUMENTOS A REMOVER:" -ForegroundColor Yellow
$removidos = 0
$naoEncontrados = 0

foreach ($doc in $remover) {
    if (Test-Path $doc) {
        Write-Host "  🗑️  Removendo: $doc" -ForegroundColor Yellow
        Remove-Item $doc -Force
        $removidos++
    } else {
        $naoEncontrados++
    }
}

Write-Host ""
Write-Host "=== RESUMO ===" -ForegroundColor Cyan
Write-Host "  Documentos mantidos: $($manter.Count)" -ForegroundColor Green
Write-Host "  Documentos removidos: $removidos" -ForegroundColor Yellow
Write-Host "  Não encontrados: $naoEncontrados" -ForegroundColor Gray
Write-Host ""
Write-Host "✅ Limpeza concluída!" -ForegroundColor Green
Write-Host ""
Write-Host "📚 Documentação essencial:" -ForegroundColor Cyan
Write-Host "  → MODO_OFFLINE_GUIA_COMPLETO.md (Guia de uso)"
Write-Host "  → IMPLEMENTACAO_COMPLETA_100_PORCENTO.md (Detalhes técnicos)"
Write-Host "  → IMPLEMENTACAO_MODO_OFFLINE_PROGRESSO.md (Histórico)"
