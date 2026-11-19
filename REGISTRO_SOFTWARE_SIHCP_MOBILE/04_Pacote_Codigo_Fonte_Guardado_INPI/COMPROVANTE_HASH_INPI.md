# Comprovante de Resumo Digital Hash — INPI (SIHCP Mobile)

**Data de Geração:** 19/11/2026 11:30:41  
**Programa:** SIHCP Mobile - Sistema de Histórico e Coleta Patrimonial Mobile (`2.29.8 (Build / versionCode 84)`)

## Resumos Digitais Calculados

| Arquivo | Tamanho (bytes) | SHA-512 (Oficial INPI) | SHA-256 |
|---|---:|---|---|
| `SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf` | 554,842 | `8180c598d1f5163778b6a2224a1aa2df327ee9b660e4ead41186224a02543aab703bf81c59c149e96cbe345e4cbdd96d8af6c11e081e3593c5263383d1ad1206` | `948aa05531ae79d9e17c0b7c982ed32b8b9802cfad96d25b4895f13c590c0e22` |
| `SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip` | 2,146,662 | `7e9214ea87292b72d93301ad9d66cde2b33761aaf1ba3070731debd8e449e2b8924d278940328cbc3026845f66ab75a253d22068606f16cc4d876513e638c5e4` | `db2b8bbddeafc7d81ed22711d14b1fc32535fe0bbaad15af1f4ce2630a9dca77` |

## Conferência no Windows PowerShell

```powershell
Get-FileHash -Algorithm SHA512 "SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf"
Get-FileHash -Algorithm SHA512 "SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip"
```

> **Regra de Ouro da Guarda Legal:** nunca altere ou gere novamente o arquivo cujo hash SHA-512 foi protocolado no INPI, pois qualquer alteração de 1 único bit muda completamente o resumo digital.
