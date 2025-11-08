# Análise de Conformidade com a LGPD
## Sistema de Inventário de Patrimônio - IFMT

---

## 📋 RESUMO EXECUTIVO

O sistema **coleta e processa dados pessoais** e, portanto, **está sujeito à LGPD** (Lei nº 13.709/2018). Esta análise identifica os dados coletados, riscos e recomendações para adequação.

---

## 🔍 DADOS PESSOAIS IDENTIFICADOS NO SISTEMA

### 1. **TABELA_USUARIO** (Usuários do Sistema)
**Dados Coletados:**
- ✅ Nome Completo
- ✅ Email
- ✅ Matrícula
- ✅ Login
- ✅ Senha (hash)
- ✅ Data de Criação
- ✅ Data de Último Acesso
- ✅ Tentativas de Login
- ✅ Data de Bloqueio
- ✅ Observações

**Classificação LGPD:** Dados Pessoais (Art. 5º, I)

---

### 2. **TABELA_RESPONSAVEL** (Responsáveis por Patrimônios)
**Dados Coletados:**
- ✅ Nome Completo
- ⚠️ **CPF** (Dado Sensível - Identificação)
- ✅ Email
- ✅ Telefone
- ✅ Cargo
- ✅ Data de Cadastro

**Classificação LGPD:** Dados Pessoais + CPF (requer atenção especial)

---

### 3. **TABELA_COLETA** (Registro de Coletas)
**Dados Coletados:**
- ✅ ID do Coletor (vincula ao usuário)
- ✅ Data/Hora da Coleta
- ✅ Localização (latitude/longitude) - se implementado
- ✅ Observações

**Classificação LGPD:** Dados Pessoais (rastreabilidade de ações)

---

## ⚠️ PRINCIPAIS RISCOS E NÃO CONFORMIDADES

### 🔴 **CRÍTICO - Ausências Graves**

1. **Falta de Termo de Consentimento**
   - ❌ Não há coleta de consentimento explícito dos titulares
   - ❌ Usuários não são informados sobre o tratamento de dados
   - **Artigo violado:** Art. 7º, I e Art. 8º

2. **Ausência de Política de Privacidade**
   - ❌ Não há documento informando como os dados são tratados
   - ❌ Titulares não sabem seus direitos
   - **Artigo violado:** Art. 9º

3. **Falta de Registro de Atividades de Tratamento**
   - ❌ Não há documentação das operações de tratamento
   - **Artigo violado:** Art. 37º

4. **Ausência de DPO (Encarregado de Dados)**
   - ❌ Não há responsável designado pela proteção de dados
   - **Artigo violado:** Art. 41º

5. **Retenção Indefinida de Dados**
   - ❌ Dados não são excluídos após término da finalidade
   - ❌ Não há política de retenção definida
   - **Artigo violado:** Art. 15º e Art. 16º

---

### 🟡 **MÉDIO - Melhorias Necessárias**

6. **Logs de Acesso Insuficientes**
   - ⚠️ Não há registro detalhado de quem acessa quais dados
   - **Recomendação:** Implementar auditoria completa

7. **Criptografia de Dados Sensíveis**
   - ⚠️ CPF armazenado sem criptografia adicional
   - ⚠️ Senhas usam hash (✅ correto), mas outros dados não
   - **Recomendação:** Criptografar CPF e dados sensíveis

8. **Controle de Acesso**
   - ⚠️ Não há controle granular de quem pode ver CPF
   - **Recomendação:** Mascarar CPF para usuários sem permissão

9. **Backup e Recuperação**
   - ⚠️ Não há menção a políticas de backup seguro
   - **Recomendação:** Garantir que backups também sejam protegidos

10. **Direitos dos Titulares**
    - ❌ Não há funcionalidade para:
      - Solicitar cópia dos dados (Portabilidade)
      - Solicitar correção de dados
      - Solicitar exclusão de dados (Direito ao Esquecimento)
      - Revogar consentimento
    - **Artigo violado:** Art. 18º

---

### 🟢 **PONTOS POSITIVOS (Já Implementados)**

✅ **Senhas com Hash (BCrypt)**
   - Senhas não são armazenadas em texto plano
   - Usa algoritmo seguro (BCrypt)

✅ **Controle de Acesso por Perfil**
   - Sistema tem perfis (ADMIN, COORDENADOR, COLETOR, CONSULTA)
   - Limita acesso baseado em função

✅ **Soft Delete (Desativação)**
   - Responsáveis são desativados, não excluídos fisicamente
   - Permite auditoria e rastreabilidade

✅ **Bloqueio de Conta por Tentativas**
   - Protege contra ataques de força bruta

✅ **Validação de Email e CPF**
   - Garante qualidade dos dados coletados

---

## 📝 RECOMENDAÇÕES PARA ADEQUAÇÃO À LGPD

### **FASE 1 - URGENTE (Implementar Imediatamente)**

#### 1. **Criar Política de Privacidade**
```
Documento que deve conter:
- Quais dados são coletados
- Finalidade do tratamento
- Base legal (Art. 7º - execução de contrato, obrigação legal)
- Prazo de retenção
- Direitos dos titulares
- Contato do DPO/Encarregado
- Medidas de segurança
```

#### 2. **Implementar Termo de Consentimento**
```java
// Adicionar na primeira tela de login ou cadastro
- Checkbox "Li e aceito a Política de Privacidade"
- Link para visualizar a política
- Armazenar data/hora do consentimento no banco
```

**Sugestão de Tabela:**
```sql
CREATE TABLE TABELA_CONSENTIMENTO (
    ID SERIAL PRIMARY KEY,
    ID_USUARIO INTEGER REFERENCES TABELA_USUARIO(ID),
    TIPO_CONSENTIMENTO VARCHAR(100), -- 'USO_SISTEMA', 'COLETA_DADOS', etc.
    DATA_CONSENTIMENTO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM VARCHAR(50),
    VERSAO_POLITICA VARCHAR(20),
    CONSENTIMENTO_ATIVO BOOLEAN DEFAULT TRUE
);
```

#### 3. **Designar Encarregado de Dados (DPO)**
```
- Nomear responsável pela proteção de dados
- Publicar contato (email: dpo@ifmt.edu.br)
- Treinar equipe sobre LGPD
```

#### 4. **Criar Política de Retenção de Dados**
```
Exemplo:
- Dados de usuários inativos: 5 anos após inativação
- Logs de acesso: 6 meses
- Dados de coleta: enquanto o inventário estiver ativo + 5 anos
- Após prazo: anonimizar ou excluir
```

---

### **FASE 2 - IMPORTANTE (Implementar em 3-6 meses)**

#### 5. **Implementar Funcionalidades de Direitos dos Titulares**

**5.1. Portabilidade de Dados**
```java
// Adicionar botão "Exportar Meus Dados" no perfil do usuário
public void exportarDadosUsuario(int idUsuario) {
    // Gerar arquivo JSON/PDF com todos os dados do usuário
    // Incluir: dados cadastrais, histórico de coletas, logs de acesso
}
```

**5.2. Correção de Dados**
```java
// Permitir que usuário edite seus próprios dados
// Adicionar tela "Meus Dados" com opção de edição
```

**5.3. Exclusão de Dados (Direito ao Esquecimento)**
```java
// Adicionar funcionalidade "Solicitar Exclusão de Conta"
// Processo:
// 1. Usuário solicita exclusão
// 2. Admin analisa (verificar se há obrigação legal de manter)
// 3. Se aprovado: anonimizar dados ou excluir
```

**Sugestão de Tabela:**
```sql
CREATE TABLE TABELA_SOLICITACAO_TITULAR (
    ID SERIAL PRIMARY KEY,
    ID_USUARIO INTEGER REFERENCES TABELA_USUARIO(ID),
    TIPO_SOLICITACAO VARCHAR(50), -- 'EXCLUSAO', 'PORTABILIDADE', 'CORRECAO'
    DATA_SOLICITACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    STATUS VARCHAR(20), -- 'PENDENTE', 'APROVADA', 'NEGADA'
    DATA_RESPOSTA TIMESTAMP,
    JUSTIFICATIVA TEXT,
    ID_RESPONSAVEL INTEGER REFERENCES TABELA_USUARIO(ID)
);
```

#### 6. **Criptografar Dados Sensíveis**
```java
// Criptografar CPF antes de salvar no banco
public String criptografarCPF(String cpf) {
    // Usar AES-256 ou similar
    // Armazenar chave de criptografia em local seguro (não no código)
}

// Descriptografar apenas quando necessário
public String descriptografarCPF(String cpfCriptografado) {
    // Verificar permissão do usuário antes de descriptografar
}
```

#### 7. **Implementar Auditoria Completa**
```sql
CREATE TABLE TABELA_LOG_ACESSO_DADOS (
    ID SERIAL PRIMARY KEY,
    ID_USUARIO INTEGER REFERENCES TABELA_USUARIO(ID),
    TABELA_ACESSADA VARCHAR(100),
    ID_REGISTRO_ACESSADO INTEGER,
    TIPO_OPERACAO VARCHAR(20), -- 'LEITURA', 'EDICAO', 'EXCLUSAO'
    DATA_HORA TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM VARCHAR(50),
    DADOS_ACESSADOS TEXT -- JSON com campos acessados
);
```

#### 8. **Mascarar Dados Sensíveis na Interface**
```java
// Exibir CPF mascarado para usuários sem permissão
public String mascararCPF(String cpf) {
    if (cpf == null || cpf.length() != 11) return cpf;
    return "***." + cpf.substring(3, 6) + ".***-**";
}

// Exemplo: 123.456.789-10 → ***.456.***-**
```

---

### **FASE 3 - RECOMENDADO (Implementar em 6-12 meses)**

#### 9. **Relatório de Impacto à Proteção de Dados (RIPD)**
```
Documento técnico que avalia:
- Riscos de vazamento de dados
- Medidas de mitigação
- Impacto em caso de incidente
```

#### 10. **Plano de Resposta a Incidentes**
```
Procedimento em caso de vazamento:
1. Identificar a origem e extensão do vazamento
2. Conter o incidente
3. Notificar ANPD em até 72 horas (se houver risco)
4. Notificar titulares afetados
5. Documentar tudo
```

#### 11. **Treinamento Contínuo**
```
- Treinar todos os usuários sobre LGPD
- Criar manual de boas práticas
- Realizar simulações de incidentes
```

---

## 📊 BASES LEGAIS APLICÁVEIS (Art. 7º da LGPD)

Para o sistema de inventário, as bases legais mais adequadas são:

1. **Execução de Contrato** (Art. 7º, V)
   - Usuários são servidores/funcionários do IFMT
   - Tratamento necessário para execução de suas funções

2. **Obrigação Legal** (Art. 7º, II)
   - Inventário patrimonial é obrigação legal de órgãos públicos
   - Lei 4.320/64 e Lei de Responsabilidade Fiscal

3. **Exercício Regular de Direitos** (Art. 7º, VI)
   - Proteção do patrimônio público
   - Prestação de contas

**⚠️ IMPORTANTE:** Mesmo com bases legais, é necessário:
- Informar os titulares sobre o tratamento
- Garantir direitos dos titulares
- Implementar medidas de segurança

---

## 🎯 CHECKLIST DE CONFORMIDADE

### Documentação
- [ ] Política de Privacidade criada e publicada
- [ ] Termo de Consentimento implementado
- [ ] Registro de Atividades de Tratamento documentado
- [ ] DPO/Encarregado designado e publicado
- [ ] Política de Retenção de Dados definida
- [ ] RIPD elaborado
- [ ] Plano de Resposta a Incidentes criado

### Técnico
- [ ] Criptografia de dados sensíveis (CPF)
- [ ] Logs de auditoria implementados
- [ ] Mascaramento de dados na interface
- [ ] Funcionalidade de portabilidade de dados
- [ ] Funcionalidade de correção de dados
- [ ] Funcionalidade de exclusão de dados
- [ ] Backup seguro e criptografado
- [ ] Controle de acesso granular

### Processos
- [ ] Processo de solicitação de direitos dos titulares
- [ ] Processo de análise de solicitações
- [ ] Processo de exclusão/anonimização de dados
- [ ] Treinamento de equipe realizado
- [ ] Canal de comunicação com DPO estabelecido

---

## 💰 PENALIDADES POR NÃO CONFORMIDADE

A LGPD prevê multas de até:
- **R$ 50 milhões por infração**
- **2% do faturamento** (para empresas privadas)
- **Advertências e publicização da infração**
- **Bloqueio ou eliminação dos dados**

**Para órgãos públicos:**
- Não há multa pecuniária, mas há:
  - Advertência pública
  - Responsabilização de gestores
  - Processos administrativos
  - Dano à imagem institucional

---

## 📞 PRÓXIMOS PASSOS RECOMENDADOS

1. **Imediato (Esta Semana)**
   - Designar responsável pela adequação LGPD
   - Iniciar elaboração da Política de Privacidade
   - Mapear todos os dados pessoais no sistema

2. **Curto Prazo (1 Mês)**
   - Publicar Política de Privacidade
   - Implementar Termo de Consentimento
   - Designar DPO
   - Criar política de retenção

3. **Médio Prazo (3-6 Meses)**
   - Implementar funcionalidades de direitos dos titulares
   - Criptografar dados sensíveis
   - Implementar auditoria completa
   - Treinar equipe

4. **Longo Prazo (6-12 Meses)**
   - Elaborar RIPD
   - Criar plano de resposta a incidentes
   - Realizar auditorias periódicas
   - Manter conformidade contínua

---

## 📚 REFERÊNCIAS

- Lei nº 13.709/2018 (LGPD)
- Guia de Boas Práticas da ANPD
- Resolução CD/ANPD nº 2/2022 (Agentes de Tratamento de Pequeno Porte)
- ISO 27001 (Segurança da Informação)
- ISO 27701 (Gestão de Privacidade)

---

**Documento elaborado em:** 20/10/2025  
**Versão:** 1.0  
**Responsável:** Análise Técnica do Sistema
