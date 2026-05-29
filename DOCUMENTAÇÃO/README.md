# 📚 Documentação SIHCP — Pasta DOCUMENTAÇÃO/

Esta pasta concentra a documentação **mais recente** do app SIHCP Mobile, organizada por tópico. Para documentação histórica e manuais por público-alvo, consulte também a pasta `docs/`.

---

## 🆕 Documentação Atual (v2.29.5)

### Visão Geral
- [**DOCUMENTACAO_COMPLETA_APP_v2.29.5.md**](DOCUMENTACAO_COMPLETA_APP_v2.29.5.md) — Documento mestre consolidado (RECOMENDADO)

### Features e Correções Recentes
- [**CONFIGURACAO_INICIAL_IP_SERVIDOR.md**](CONFIGURACAO_INICIAL_IP_SERVIDOR.md) — Tela de configuração inicial (v2.29.5)
- [**CORRECAO_COLETA_EXCLUIDA_V2.29.5_HIBRIDA.md**](CORRECAO_COLETA_EXCLUIDA_V2.29.5_HIBRIDA.md) — Solução híbrida online/offline
- [**CORRECAO_COLETA_EXCLUIDA_V2.29.4.md**](CORRECAO_COLETA_EXCLUIDA_V2.29.4.md) — Iteração anterior (apenas online)
- [**PROBLEMA_COLETA_EXCLUIDA_AINDA_DETECTADA.md**](PROBLEMA_COLETA_EXCLUIDA_AINDA_DETECTADA.md) — Análise inicial do bug

---

## 📖 Manuais por Público (em `docs/manuais/`)

| Manual | Público | Conteúdo |
|--------|---------|----------|
| [MANUAL_COLETOR.md](../docs/manuais/MANUAL_COLETOR.md) | Coletor em campo | Como usar o app Android |
| [MANUAL_ADMINISTRADOR.md](../docs/manuais/MANUAL_ADMINISTRADOR.md) | TI / Servidor | Instalação e operação do servidor |
| [MANUAL_OPERADOR.md](../docs/manuais/MANUAL_OPERADOR.md) | Operador desktop | Aplicativo Swing de gestão |

---

## 🏗️ Documentação Técnica (em `docs/`)

- [`docs/GUIA_MOBILE.md`](../docs/GUIA_MOBILE.md) — Arquitetura mobile completa
- [`docs/GUIA_FUNCIONALIDADES.md`](../docs/GUIA_FUNCIONALIDADES.md) — Features detalhadas
- [`docs/HISTORICO_CORRECOES.md`](../docs/HISTORICO_CORRECOES.md) — Histórico de bugs corrigidos
- [`docs/SERVIDOR_MOBILE_MODULAR.md`](../docs/SERVIDOR_MOBILE_MODULAR.md) — Modularização do servidor
- [`docs/PADROES_DE_PROJETO.md`](../docs/PADROES_DE_PROJETO.md) — Padrões usados no projeto

---

## 🎯 Steering Rules (em `.kiro/steering/`)

Diretrizes obrigatórias para desenvolvimento:

- `clean-architecture.md` — Padrão arquitetural
- `endpoints-nao-alterar.md` — Endpoints críticos
- `security-roles.md` — Matriz de permissões
- `sync-improvements-summary.md` — Sincronização avançada
- `database-verification.md` — Verificação de banco
- `tech.md` — Stack tecnológica
- `structure.md` — Estrutura do projeto
- `product.md` — Visão do produto

---

## 🔄 Histórico de Builds

- [`BUILD_HISTORY.md`](../BUILD_HISTORY.md) — Histórico recente (v2.29.x)
- [`docs/historico_e_reunioes/BUILD_HISTORY.md`](../docs/historico_e_reunioes/BUILD_HISTORY.md) — Histórico antigo

---

## 📅 Últimas Atualizações

| Data | Documento | Mudança |
|------|-----------|---------|
| 22/05/2026 | DOCUMENTACAO_COMPLETA_APP_v2.29.5.md | Criado documento mestre |
| 22/05/2026 | CONFIGURACAO_INICIAL_IP_SERVIDOR.md | Nova feature de setup inicial |
| 22/05/2026 | CORRECAO_COLETA_EXCLUIDA_V2.29.5_HIBRIDA.md | Solução final híbrida |
| 22/05/2026 | docs/manuais/MANUAL_COLETOR.md | Atualizado para v2.29.5 |

---

## 🚀 Como Usar Esta Documentação

### Sou usuário final (coletor)
→ Leia [`MANUAL_COLETOR.md`](../docs/manuais/MANUAL_COLETOR.md)

### Sou administrador / TI
→ Leia [`MANUAL_ADMINISTRADOR.md`](../docs/manuais/MANUAL_ADMINISTRADOR.md) e [`DOCUMENTACAO_COMPLETA_APP_v2.29.5.md`](DOCUMENTACAO_COMPLETA_APP_v2.29.5.md)

### Sou desenvolvedor
→ Leia [`DOCUMENTACAO_COMPLETA_APP_v2.29.5.md`](DOCUMENTACAO_COMPLETA_APP_v2.29.5.md) + [`docs/GUIA_MOBILE.md`](../docs/GUIA_MOBILE.md) + steering rules em `.kiro/steering/`

### Quero entender uma feature específica
→ Procure pelo nome da feature nos arquivos desta pasta

### Estou debuggando um bug
→ Consulte [`docs/HISTORICO_CORRECOES.md`](../docs/HISTORICO_CORRECOES.md) e [`BUILD_HISTORY.md`](../BUILD_HISTORY.md)

---

**Mantido por:** Equipe de Desenvolvimento SIHCP
**Versão atual do app:** 2.29.5 (Build 81)
**Última revisão:** 22/05/2026
