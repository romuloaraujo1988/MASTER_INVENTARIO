# Comprovante de Resumo Digital Hash — INPI (SIHCP Mobile)

**Data de Geração:** 13/11/2026 21:48:25  
**Programa:** SIHCP Mobile - Sistema de Histórico e Coleta Patrimonial Mobile (`2.29.8 (Build / versionCode 84)`)

## Resumos Digitais Calculados

| Arquivo | Tamanho (bytes) | SHA-512 (Oficial INPI) | SHA-256 |
|---|---:|---|---|
| `SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf` | 554,424 | `0819a3f7e83dcc8397347ba74ea3007c3ffccd9fedeaa26070162c0b0dfdb44fe78abac42f5a95517483b4ca82e8394bc104d63b8cd685a2e6051dc89dbed1b7` | `09edc7fe16c872930a3066fa4eee3bd380b9c1b9b7b1fa0ac2e0de1893401823` |
| `SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip` | 2,173,613 | `f4d6e4ac8fa490a5974a39e34ded58808ad54e24abd1c46d062ef4b7e34c78704b23acfc4eb1150f068aab5abcc70ba98a886eb3bf4b8f7be45955ef476b6f8b` | `3257089696f1b936bbda50c3f2638699cfe663d9612463bb1e97a37f79707f74` |

## Conferência no Windows PowerShell

```powershell
Get-FileHash -Algorithm SHA512 "SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf"
Get-FileHash -Algorithm SHA512 "SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip"
```

> **Regra de Ouro da Guarda Legal:** nunca altere ou gere novamente o arquivo cujo hash SHA-512 foi protocolado no INPI, pois qualquer alteração de 1 único bit muda completamente o resumo digital.
