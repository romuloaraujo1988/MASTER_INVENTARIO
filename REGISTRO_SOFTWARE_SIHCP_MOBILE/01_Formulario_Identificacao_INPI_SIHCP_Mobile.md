# 1 . IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR

| Campo | Valor |
|---|---|
| **Título do Programa de Computador** | SIHCP Mobile - Sistema de Histórico e Coleta Patrimonial Mobile |
| **Data da Criação** | 18/10/2025 |
| **Data de Publicação** | 09/11/2025 |
| **Tipo de Linguagem\*** | Kotlin, XML, SQL, Java |
| **Campo de Aplicação\*** | AD-01 (Administração Geral / Gestão Pública); AD-03 (Administração Patrimonial / Controle de Bens e Material); GI-01 (Gerência da Informação / Coleta de Dados); ED-01 (Educação) |
| **Tipo de Programa\*** | AP-01 (Aplicativo para Dispositivos Móveis); GI-01 (Gerência de Informação / Banco de Dados Local); TC-01 (Teleprocessamento / Comunicação e Sincronização de Dados); UT-01 (Utilitário - Leitura Óptica e Relatórios) |
| **Algoritmo hash** | SHA-512 |
| **Resumo digital hash** | `8180c598d1f5163778b6a2224a1aa2df327ee9b660e4ead41186224a02543aab703bf81c59c149e96cbe345e4cbdd96d8af6c11e081e3593c5263383d1ad1206` |
| **Descrição resumida do programa de computador(até 200 palavras)** | O SIHCP Mobile (Sistema de Histórico e Coleta Patrimonial Mobile) é um aplicativo nativo para plataforma Android, desenvolvido em linguagem Kotlin segundo os princípios de Clean Architecture e padrão arquitetural MVVM, destinado à automatização e auditoria do inventário físico de bens patrimoniais em instituições públicas federais. O software permite a leitura óptica de etiquetas via QR Code e código de barras, coleta manual por número de tombamento, busca textual por descrição, registro de itens compostos em lote e cadastramento de bens sem etiqueta com captura fotográfica obrigatória. Projetado sob abordagem offline-first, armazena dados localmente em banco SQLite criptografado com SQLCipher (AES-256) e gerencia chaves via Android Keystore, contando com autenticação JWT e biometria. A sincronização bidirecional com o servidor ocorre em segundo plano via WorkManager com retentativa exponencial, além de monitoramento de coletas ao vivo via WebSocket. O aplicativo detecta divergências cadastrais e de localização em tempo real, emite relatórios em PDF, Excel e CSV, e atende à IN SEDAP nº 205/1988 e ao Decreto nº 12.785/2025. |

\* Podem ser selecionados vários tipos linguagem, programa e aplicação.

> **Contagem de palavras da descrição resumida:** 167 palavras (limite máximo: 200 palavras).

---

## 2 . REFERÊNCIA DO ARQUIVO DE GUARDA DIGITAL (HASH)

- **Arquivo principal vinculado ao hash acima (PDF Oficial):** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf` (554,842 bytes)
  - **SHA-512:** `8180c598d1f5163778b6a2224a1aa2df327ee9b660e4ead41186224a02543aab703bf81c59c149e96cbe345e4cbdd96d8af6c11e081e3593c5263383d1ad1206`
  - **SHA-256:** `948aa05531ae79d9e17c0b7c982ed32b8b9802cfad96d25b4895f13c590c0e22`
- **Pacote ZIP completo alternativo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip` (2,146,662 bytes)
  - **SHA-512:** `7e9214ea87292b72d93301ad9d66cde2b33761aaf1ba3070731debd8e449e2b8924d278940328cbc3026845f66ab75a253d22068606f16cc4d876513e638c5e4`
  - **SHA-256:** `db2b8bbddeafc7d81ed22711d14b1fc32535fe0bbaad15af1f4ce2630a9dca77`

> **Importante:** Conforme o Art. 2º da IN INPI/PR nº 099/2019, o titular deve manter sob guarda inviolável (sem alterar nenhum byte) exatamente o arquivo cujo hash foi submetido no registro.

---

## 3 . IDENTIFICAÇÃO DO TITULAR E DOS AUTORES (PARA PROTOCOLO NIT / INPI)

| Campo | Preenchimento / Observação |
|---|---|
| **Titular dos Direitos Patrimoniais** | Instituto Federal de Educação, Ciência e Tecnologia de Mato Grosso (IFMT) — CNPJ: `[Preencher]` |
| **Autor Principal** | Romulo Rodrigues de Araujo — `romulo.araujo88@gmail.com` — CPF: `[Preencher]` |
| **Coautores / Orientadores** | `[Adicionar se aplicável]` |
| **Versão Registrada** | 2.29.8 (Build / versionCode 84) (13/11/2026) |
| **Produção / Primeira Liberação** | 1ª Release v1.0.0: 09/11/2025 · Início em produção IFMT: 18/11/2025 |
