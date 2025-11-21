# Índice de Documentação - Sistema de Inventário IFMT

## 📚 Documentação Organizada

---

## 🎯 Modo Offline (NOVO - 100% Implementado)

### Documentos Essenciais

1. **MODO_OFFLINE_GUIA_COMPLETO.md** ⭐
   - Guia completo de uso
   - Arquitetura e componentes
   - Instalação e configuração
   - Testes e troubleshooting
   - **Recomendado para:** Usuários e desenvolvedores

2. **IMPLEMENTACAO_COMPLETA_100_PORCENTO.md**
   - Detalhes técnicos da implementação
   - Estatísticas e métricas
   - Fluxos implementados
   - **Recomendado para:** Desenvolvedores

3. **IMPLEMENTACAO_MODO_OFFLINE_PROGRESSO.md**
   - Histórico de implementação
   - Progresso por fase
   - Arquivos criados e modificados
   - **Recomendado para:** Gerentes de projeto

---

## 🏗️ Arquitetura e Estrutura

### Documentos Principais

1. **structure.md** (Steering)
   - Estrutura do projeto
   - Organização de pacotes
   - Convenções de nomenclatura

2. **tech.md** (Steering)
   - Stack tecnológico
   - Comandos comuns
   - Configurações

3. **product.md** (Steering)
   - Visão geral do produto
   - Propósito e usuários
   - Valor de negócio

---

## 🧹 Clean Architecture (Android)

### Documentos Principais

1. **clean-architecture.md** (Steering)
   - Diretrizes de arquitetura
   - Templates de código
   - Regras de dependência

2. **clean-architecture-progress.md** (Steering)
   - Progresso da migração
   - Componentes implementados
   - Próximos passos

3. **android-clean-migration-status.md** (Steering)
   - Status detalhado da migração
   - Activities migradas
   - Checklist

4. **migration-guide.md** (Steering)
   - Guia passo a passo
   - Ordem de implementação
   - Problemas comuns

---

## 🔄 Sincronização e Endpoints

### Documentos Principais

1. **sync-improvements-summary.md** (Steering)
   - Melhorias na sincronização
   - Batch sync
   - Background sync

2. **endpoints-nao-alterar.md** (Steering) ⚠️
   - **CRÍTICO:** Regras de endpoints
   - URLs que não devem ser alteradas
   - Processo de mudança segura

3. **novos-endpoints-validacao.md** (Steering)
   - Endpoints de validação
   - Integração Android
   - Exemplos de uso

---

## 📱 Funcionalidades Implementadas

### Documentos Principais

1. **implementacao-final-completa.md** (Steering)
   - Todas as funcionalidades
   - Gestão de inventário
   - Autenticação
   - Validação

2. **resumo-funcionalidades-criticas.md** (Steering)
   - Funcionalidades críticas
   - Status de implementação
   - Próximos passos

3. **integracao-validacao-android.md** (Steering)
   - Integração de validação
   - Fluxos de uso
   - Exemplos de UI

---

## 🚀 Produção e Deploy

### Documentos Principais

1. **guia-producao-completo.md** (Steering)
   - Checklist pré-deploy
   - Deploy automatizado
   - Testes de carga
   - Monitoramento
   - Plano de contingência

2. **CHECKLIST_DEPLOY.md**
   - Checklist detalhado
   - Validações necessárias
   - Rollback

---

## 📊 Relatórios e ViewModels

### Documentos Principais

1. **relatorio-mvvm-migration.md** (Steering)
   - Migração do RelatorioFrame
   - Padrão MVVM
   - Exemplo completo

---

## 📝 Documentos de Sessão (Histórico)

### Resumos de Sessões Recentes

- `RESUMO_SESSAO_21NOV_FINAL.md` - Sessão final 21/11
- `RESUMO_SESSAO_21NOV_LIMPEZA_CODIGO.md` - Limpeza de código
- `RESUMO_SESSAO_20NOV_COMPLETO.md` - Sessão 20/11
- `RESUMO_SESSAO_19NOV_FINAL.md` - Sessão 19/11

---

## 🔧 Scripts Úteis

### Limpeza e Manutenção

- `limpar_documentacao_redundante.ps1` - Limpar documentação redundante
- `limpar-coleta-3250.ps1` - Limpar coleta específica
- `diagnostico-postgresql.ps1` - Diagnóstico do banco

### Build e Deploy

- `build-producao.bat` - Build de produção
- `build-desktop.bat` - Build desktop
- `compactar-producao.bat` - Compactar para distribuição

### Testes

- `testar-conexao-postgresql.bat` - Testar conexão
- `testar-sincronizacao-app.bat` - Testar sincronização
- `test-coleta-endpoint.ps1` - Testar endpoint de coleta

---

## 📂 Organização de Pastas

```
MASTER_INVENTÁRIO/
├── src/main/java/com/inventario/     # Código Java
│   ├── model/                         # Entidades
│   ├── dao/                           # DAOs
│   ├── service/                       # Serviços
│   ├── view/                          # Interface Swing
│   ├── offline/                       # Sistema offline ⭐
│   └── mobile/server/                 # API Mobile
│
├── InventarioMobile/                  # App Android
│   └── app/src/main/java/            # Código Kotlin
│
├── sql/                               # Scripts SQL
├── docs/                              # Documentação técnica
├── DOCUMENTAÇÃO/                      # Documentação em PT-BR
├── .kiro/steering/                    # Regras do Kiro ⭐
└── [Documentos .md]                   # Documentação raiz
```

---

## 🎯 Guia Rápido por Perfil

### Para Usuários Finais
1. `MODO_OFFLINE_GUIA_COMPLETO.md` - Como usar modo offline
2. `MANUAL_DO_USUARIO.md` - Manual geral do sistema

### Para Desenvolvedores
1. `MODO_OFFLINE_GUIA_COMPLETO.md` - Arquitetura offline
2. `clean-architecture.md` - Padrões de código
3. `structure.md` - Estrutura do projeto
4. `tech.md` - Stack e comandos

### Para Gerentes de Projeto
1. `IMPLEMENTACAO_COMPLETA_100_PORCENTO.md` - Status geral
2. `IMPLEMENTACAO_MODO_OFFLINE_PROGRESSO.md` - Progresso offline
3. `clean-architecture-progress.md` - Progresso Clean Architecture

### Para DevOps
1. `guia-producao-completo.md` - Deploy e produção
2. `CHECKLIST_DEPLOY.md` - Checklist
3. Scripts de build e teste

---

## 🔍 Como Encontrar Informação

### Por Tópico

**Modo Offline:**
- Guia: `MODO_OFFLINE_GUIA_COMPLETO.md`
- Implementação: `IMPLEMENTACAO_COMPLETA_100_PORCENTO.md`

**Clean Architecture:**
- Diretrizes: `.kiro/steering/clean-architecture.md`
- Progresso: `.kiro/steering/clean-architecture-progress.md`

**Endpoints:**
- Regras: `.kiro/steering/endpoints-nao-alterar.md`
- Novos: `.kiro/steering/novos-endpoints-validacao.md`

**Produção:**
- Guia: `.kiro/steering/guia-producao-completo.md`
- Checklist: `CHECKLIST_DEPLOY.md`

### Por Ação

**Quero implementar modo offline:**
→ `MODO_OFFLINE_GUIA_COMPLETO.md`

**Quero fazer deploy:**
→ `guia-producao-completo.md`

**Quero migrar para Clean Architecture:**
→ `clean-architecture.md` + `migration-guide.md`

**Quero adicionar endpoint:**
→ `endpoints-nao-alterar.md` (ler primeiro!)

**Quero entender a estrutura:**
→ `structure.md`

---

## 📌 Documentos Críticos (Leitura Obrigatória)

1. ⚠️ **endpoints-nao-alterar.md** - NUNCA alterar endpoints sem ler
2. ⭐ **MODO_OFFLINE_GUIA_COMPLETO.md** - Sistema offline completo
3. 🏗️ **clean-architecture.md** - Padrões de código
4. 🚀 **guia-producao-completo.md** - Deploy seguro

---

## 🗑️ Documentos Removidos (Consolidados)

Os seguintes documentos foram consolidados e removidos para evitar redundância:

- 32 documentos sobre modo offline → `MODO_OFFLINE_GUIA_COMPLETO.md`
- Múltiplos resumos de fase → `IMPLEMENTACAO_COMPLETA_100_PORCENTO.md`
- Documentos de planejamento → Implementação final

---

## 📅 Última Atualização

**Data:** 21/11/2025  
**Versão:** 2.0 (Consolidada)  
**Status:** ✅ Documentação organizada e limpa

---

## 💡 Dicas

- Use Ctrl+F para buscar tópicos específicos
- Documentos com ⭐ são essenciais
- Documentos com ⚠️ são críticos
- Steering rules (`.kiro/steering/`) são sempre ativas
- Consulte este índice quando em dúvida

---

**Mantido por:** Sistema de Inventário IFMT  
**Contato:** Equipe de Desenvolvimento
