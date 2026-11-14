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
| **Resumo digital hash** | `0819a3f7e83dcc8397347ba74ea3007c3ffccd9fedeaa26070162c0b0dfdb44fe78abac42f5a95517483b4ca82e8394bc104d63b8cd685a2e6051dc89dbed1b7` |
| **Descrição resumida do programa de computador(até 200 palavras)** | O SIHCP Mobile (Sistema de Histórico e Coleta Patrimonial Mobile) é um aplicativo nativo para plataforma Android, desenvolvido em linguagem Kotlin segundo os princípios de Clean Architecture e padrão arquitetural MVVM, destinado à automatização e auditoria do inventário físico de bens patrimoniais em instituições públicas federais. O software permite a leitura óptica de etiquetas via QR Code e código de barras, coleta manual por número de tombamento, busca textual por descrição, registro de itens compostos em lote e cadastramento de bens sem etiqueta com captura fotográfica obrigatória. Projetado sob abordagem offline-first, armazena dados localmente em banco SQLite criptografado com SQLCipher (AES-256) e gerencia chaves via Android Keystore, contando com autenticação JWT e biometria. A sincronização bidirecional com o servidor ocorre em segundo plano via WorkManager com retentativa exponencial, além de monitoramento de coletas ao vivo via WebSocket. O aplicativo detecta divergências cadastrais e de localização em tempo real, emite relatórios em PDF, Excel e CSV, e atende à IN SEDAP nº 205/1988 e ao Decreto nº 12.785/2025. |

\* Podem ser selecionados vários tipos linguagem, programa e aplicação.

> **Contagem de palavras da descrição resumida:** 167 palavras (limite máximo: 200 palavras).

---

## 2 . REFERÊNCIA DO ARQUIVO DE GUARDA DIGITAL (HASH)

- **Arquivo principal vinculado ao hash acima (PDF Oficial):** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf` (554,424 bytes)
  - **SHA-512:** `0819a3f7e83dcc8397347ba74ea3007c3ffccd9fedeaa26070162c0b0dfdb44fe78abac42f5a95517483b4ca82e8394bc104d63b8cd685a2e6051dc89dbed1b7`
  - **SHA-256:** `09edc7fe16c872930a3066fa4eee3bd380b9c1b9b7b1fa0ac2e0de1893401823`
- **Pacote ZIP completo alternativo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip` (2,173,613 bytes)
  - **SHA-512:** `f4d6e4ac8fa490a5974a39e34ded58808ad54e24abd1c46d062ef4b7e34c78704b23acfc4eb1150f068aab5abcc70ba98a886eb3bf4b8f7be45955ef476b6f8b`
  - **SHA-256:** `3257089696f1b936bbda50c3f2638699cfe663d9612463bb1e97a37f79707f74`

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
