# Pacote de Registro de Software — SIHCP Mobile (INPI / NIT-IFMT)

Esta pasta reúne toda a documentação técnica, jurídica e o pacote de código-fonte com resumo digital criptográfico (**SHA-512**) necessários para o pedido de Registro de Programa de Computador do **SIHCP Mobile** junto ao **NIT/IFMT** e ao **INPI** (sistema e-Software).

---

## Estrutura desta Pasta

| Arquivo / Subpasta | Finalidade |
|---|---|
| `00_LEIA_ME_GUIA_REGISTRO_INPI.md` | Este guia com o roteiro completo de conferência e protocolo. |
| `01_Formulario_Identificacao_INPI_SIHCP_Mobile.docx` | Tabela **1. IDENTIFICAÇÃO DO PROGRAMA DE COMPUTADOR** pronta para entrega ao NIT/IFMT ou preenchimento no e-Software, com o hash **SHA-512** real já preenchido. |
| `01_Formulario_Identificacao_INPI_SIHCP_Mobile.md` | Espelho em Markdown do formulário de identificação. |
| `02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.docx` | Memorial Descritivo completo (arquitetura Clean/MVVM, funcionalidades, banco SQLCipher, segurança, stack e conformidade legal). |
| `02_Memorial_Descritivo_e_Arquitetura_SIHCP_Mobile.md` | Espelho em Markdown do memorial descritivo. |
| `03_Inventario_Codigo_Fonte_e_Metricas.md` | Inventário quantitativo e listagem completa dos arquivos-fonte reais de `InventarioMobile`. |
| `04_Pacote_Codigo_Fonte_Guardado_INPI/` | Pasta contendo os arquivos originais imutáveis de guarda (`PDF` e `ZIP`) e o `COMPROVANTE_HASH_INPI.txt`. |

---

## Qual arquivo devo guardar para o Hash SHA-512?

No registro do INPI, **você NÃO envia o código-fonte ao INPI** — você informa apenas o **Resumo Digital Hash (SHA-512)** no formulário e **guarda o arquivo original** sob sigilo/segurança do titular (IFMT / autor).

1. **Opção Padrão (já preenchida no Formulário 01):**
   - **Arquivo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Documentacao_e_Codigo_Fonte_INPI.pdf`
   - **SHA-512:** `0819a3f7e83dcc8397347ba74ea3007c3ffccd9fedeaa26070162c0b0dfdb44fe78abac42f5a95517483b4ca82e8394bc104d63b8cd685a2e6051dc89dbed1b7`
   - *Vantagem:* formato padrão mais aceito pelos NITs universitários/federais, por conter capa, memorial, inventário e listagem de código em documento único e legível.

2. **Opção Alternativa (Pacote ZIP integral com todo `src/main` + PDF):**
   - **Arquivo:** `04_Pacote_Codigo_Fonte_Guardado_INPI/SIHCP_Mobile_Codigo_Fonte_Completo_INPI.zip`
   - **SHA-512:** `f4d6e4ac8fa490a5974a39e34ded58808ad54e24abd1c46d062ef4b7e34c78704b23acfc4eb1150f068aab5abcc70ba98a886eb3bf4b8f7be45955ef476b6f8b`
   - *Quando usar:* caso o NIT do IFMT solicite o hash calculado sobre o arquivo `.zip` completo contendo todos os XMLs e classes Kotlin. Basta copiar este hash para a linha *Resumo digital hash* do Formulário `01`.

---

## Checklist Antes de Protocolar no NIT / INPI

1. Abra `01_Formulario_Identificacao_INPI_SIHCP_Mobile.docx` e preencha os dados pessoais faltantes na Seção 3 (**CPF**, **SIAPE**, **CNPJ do IFMT** e eventuais **coautores/orientadores**).
2. Confirme se o NIT solicita o campo **Data de Publicação** como `09/11/2025` (1ª release APK v1.0.0) ou `18/11/2025` (início oficial do inventário em produção no IFMT).
3. No portal e-Software do INPI (ou formulário do NIT), verifique a seleção exata dos códigos de **Linguagem**, **Campo de Aplicação** e **Tipo de Programa** conforme a tabela oficial vigente no sistema do INPI:
   - Linguagens: `Kotlin`, `XML`, `SQL`, `Java`
   - Campo de Aplicação: `AD-01`, `AD-03`, `GI-01`, `ED-01`
   - Tipo de Programa: `AP-01`, `GI-01`, `TC-01`, `UT-01`
4. Faça backup da pasta `04_Pacote_Codigo_Fonte_Guardado_INPI/` em pelo menos dois locais seguros (ex.: servidor/Drive institucional do IFMT + mídia externa) **sem reabrir/resalvar o PDF ou o ZIP**.
