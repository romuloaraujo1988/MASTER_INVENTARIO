# Manuais do SIHCP por Público-Alvo

Sistema de Histórico e Coleta Patrimonial — Instituto Federal de Mato Grosso (IFMT)

**Versão do sistema:** 2.21.0-security (maio/2026)

---

Esta pasta contém três manuais independentes, cada um focado em um tipo de usuário do SIHCP. Escolha o que se aplica ao seu papel:

| Manual | Para quem | O que ensina |
|---|---|---|
| [**MANUAL_COLETOR.md**](MANUAL_COLETOR.md) | Coletor de campo | Usar o app Android para realizar coletas patrimoniais |
| [**MANUAL_ADMINISTRADOR.md**](MANUAL_ADMINISTRADOR.md) | Administrador / TI | Instalar, configurar e operar o servidor da API Mobile |
| [**MANUAL_OPERADOR.md**](MANUAL_OPERADOR.md) | Operador desktop | Usar o aplicativo desktop Swing para gestão patrimonial |

---

## Resumo do sistema

O SIHCP tem três componentes que trabalham juntos:

```
┌─────────────────────┐       ┌──────────────────────┐       ┌─────────────────────┐
│ APP ANDROID         │       │ SERVIDOR             │       │ APLICATIVO DESKTOP  │
│ (Kotlin)            │◄─────►│ Spring Boot API REST │◄─────►│ (Java Swing)        │
│ coleta em campo     │ HTTP  │ autenticação JWT     │ JDBC  │ administração       │
└─────────────────────┘       └──────────┬───────────┘       └─────────────────────┘
                                         │
                                         ▼
                               ┌────────────────────┐
                               │ PostgreSQL         │
                               │ sispatrimonio      │
                               └────────────────────┘
```

- **App Android** — coletores registram patrimônios em campo (QR Code, manual, por descrição, sem etiqueta) com suporte offline.
- **Servidor** — API REST que recebe coletas, autentica usuários via JWT e valida regras de negócio. Rodando em Spring Boot com porta `8081` por padrão.
- **Desktop Swing** — ferramenta de administração para cadastros, relatórios, controle de inventários e operações em lote.

Cada manual foca no componente relevante para o público-alvo, sem ocultar as integrações.
