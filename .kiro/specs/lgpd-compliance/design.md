# Design Document - LGPD Compliance

## Overview

Este documento descreve o design técnico para implementação da conformidade com a LGPD no Sistema de Inventário de Patrimônio do IFMT, focando na Política de Privacidade e Termo de Consentimento. A solução será integrada ao sistema existente de forma não invasiva, utilizando padrões já estabelecidos no código.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Camada de Apresentação                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  JLogin      │  │ Consent      │  │ Privacy      │      │
│  │  (Modificado)│  │ Dialog       │  │ Policy       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Camada de Negócio                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Consent      │  │ Privacy      │  │ Audit        │      │
│  │ Service      │  │ Policy       │  │ Service      │      │
│  │              │  │ Service      │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Camada de Dados                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Consent      │  │ Privacy      │  │ Audit        │      │
│  │ DAO          │  │ Policy       │  │ DAO          │      │
│  │              │  │ DAO          │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     Banco de Dados PostgreSQL                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ TABELA_CONSENTIMENTO                                 │   │
│  │ TABELA_POLITICA_PRIVACIDADE                         │   │
│  │ TABELA_LOG_CONSENTIMENTO                            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

```
Usuário → JLogin → ConsentService → ConsentDAO → Database
                ↓
         ConsentDialog
                ↓
      PrivacyPolicyViewer
```

## Components and Interfaces

### 1. Database Schema

#### TABELA_CONSENTIMENTO
```sql
CREATE TABLE TABELA_CONSENTIMENTO (
    ID SERIAL PRIMARY KEY,
    ID_USUARIO INTEGER NOT NULL REFERENCES TABELA_USUARIO(ID),
    TIPO_CONSENTIMENTO VARCHAR(50) NOT NULL, -- 'USO_SISTEMA', 'COMUNICACOES'
    DATA_CONSENTIMENTO TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM VARCHAR(50),
    VERSAO_POLITICA VARCHAR(20) NOT NULL,
    CONSENTIMENTO_ATIVO BOOLEAN NOT NULL DEFAULT TRUE,
    DATA_REVOGACAO TIMESTAMP,
    MOTIVO_REVOGACAO TEXT,
    USER_AGENT TEXT,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consentimento_usuario ON TABELA_CONSENTIMENTO(ID_USUARIO);
CREATE INDEX idx_consentimento_ativo ON TABELA_CONSENTIMENTO(CONSENTIMENTO_ATIVO);
CREATE INDEX idx_consentimento_versao ON TABELA_CONSENTIMENTO(VERSAO_POLITICA);
```

#### TABELA_POLITICA_PRIVACIDADE
```sql
CREATE TABLE TABELA_POLITICA_PRIVACIDADE (
    ID SERIAL PRIMARY KEY,
    VERSAO VARCHAR(20) NOT NULL UNIQUE,
    TITULO VARCHAR(200) NOT NULL,
    CONTEUDO TEXT NOT NULL,
    DATA_PUBLICACAO TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DATA_VIGENCIA TIMESTAMP NOT NULL,
    ATIVA BOOLEAN NOT NULL DEFAULT TRUE,
    ID_USUARIO_CRIADOR INTEGER REFERENCES TABELA_USUARIO(ID),
    HASH_CONTEUDO VARCHAR(64), -- SHA-256 para integridade
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_politica_versao ON TABELA_POLITICA_PRIVACIDADE(VERSAO);
CREATE INDEX idx_politica_ativa ON TABELA_POLITICA_PRIVACIDADE(ATIVA);
```

#### TABELA_LOG_CONSENTIMENTO
```sql
CREATE TABLE TABELA_LOG_CONSENTIMENTO (
    ID SERIAL PRIMARY KEY,
    ID_CONSENTIMENTO INTEGER REFERENCES TABELA_CONSENTIMENTO(ID),
    ID_USUARIO INTEGER REFERENCES TABELA_USUARIO(ID),
    ACAO VARCHAR(50) NOT NULL, -- 'CONCESSAO', 'REVOGACAO', 'VISUALIZACAO'
    DATA_ACAO TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IP_ORIGEM VARCHAR(50),
    DETALHES TEXT,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_log_consentimento ON TABELA_LOG_CONSENTIMENTO(ID_CONSENTIMENTO);
CREATE INDEX idx_log_usuario ON TABELA_LOG_CONSENTIMENTO(ID_USUARIO);
CREATE INDEX idx_log_data ON TABELA_LOG_CONSENTIMENTO(DATA_ACAO);
```

### 2. Model Classes

#### Consentimento.java
```java
package com.inventario.model;

import java.time.LocalDateTime;

public class Consentimento {
    private Integer id;
    private Integer idUsuario;
    private TipoConsentimento tipoConsentimento;
    private LocalDateTime dataConsentimento;
    private String ipOrigem;
    private String versaoPolitica;
    private Boolean consentimentoAtivo;
    private LocalDateTime dataRevogacao;
    private String motivoRevogacao;
    private String userAgent;
    
    // Getters, Setters, Constructors
}

enum TipoConsentimento {
    USO_SISTEMA("Uso do Sistema", true),
    COMUNICACOES("Receber Comunicações", false);
    
    private String descricao;
    private boolean obrigatorio;
    
    // Constructor, Getters
}
```

#### PoliticaPrivacidade.java
```java
package com.inventario.model;

import java.time.LocalDateTime;

public class PoliticaPrivacidade {
    private Integer id;
    private String versao;
    private String titulo;
    private String conteudo;
    private LocalDateTime dataPublicacao;
    private LocalDateTime dataVigencia;
    private Boolean ativa;
    private Integer idUsuarioCriador;
    private String hashConteudo;
    
    // Getters, Setters, Constructors
}
```

### 3. DAO Layer

#### ConsentimentoDAO.java
```java
package com.inventario.dao;

public class ConsentimentoDAO {
    // Métodos principais:
    
    // Registrar novo consentimento
    public Integer inserirConsentimento(Consentimento consentimento);
    
    // Buscar consentimento ativo do usuário
    public Consentimento buscarConsentimentoAtivo(int idUsuario, String versaoPolitica);
    
    // Verificar se usuário tem consentimento válido
    public boolean temConsentimentoValido(int idUsuario);
    
    // Revogar consentimento
    public boolean revogarConsentimento(int idConsentimento, String motivo);
    
    // Listar histórico de consentimentos do usuário
    public List<Consentimento> listarConsentimentosUsuario(int idUsuario);
    
    // Buscar usuários sem consentimento para versão atual
    public List<Integer> buscarUsuariosSemConsentimentoAtual(String versaoAtual);
    
    // Relatório de auditoria
    public Map<String, Object> gerarRelatorioAuditoria();
}
```

#### PoliticaPrivacidadeDAO.java
```java
package com.inventario.dao;

public class PoliticaPrivacidadeDAO {
    // Métodos principais:
    
    // Buscar política ativa
    public PoliticaPrivacidade buscarPoliticaAtiva();
    
    // Buscar política por versão
    public PoliticaPrivacidade buscarPorVersao(String versao);
    
    // Inserir nova política
    public Integer inserirPolitica(PoliticaPrivacidade politica);
    
    // Atualizar política
    public boolean atualizarPolitica(PoliticaPrivacidade politica);
    
    // Desativar política anterior
    public boolean desativarPoliticasAnteriores();
    
    // Listar histórico de políticas
    public List<PoliticaPrivacidade> listarHistorico();
}
```

### 4. Service Layer

#### ConsentimentoService.java
```java
package com.inventario.service;

public class ConsentimentoService {
    private ConsentimentoDAO consentimentoDAO;
    private PoliticaPrivacidadeDAO politicaDAO;
    private LogConsentimentoDAO logDAO;
    
    // Validar se usuário precisa fornecer consentimento
    public boolean precisaConsentimento(int idUsuario);
    
    // Registrar consentimento com validações
    public boolean registrarConsentimento(int idUsuario, 
                                         List<TipoConsentimento> tipos,
                                         String ipOrigem);
    
    // Revogar consentimento com notificação ao DPO
    public boolean revogarConsentimento(int idUsuario, String motivo);
    
    // Verificar consentimento antes de operações
    public void validarConsentimento(int idUsuario) throws ConsentimentoException;
    
    // Notificar DPO sobre revogação
    private void notificarDPO(int idUsuario, String motivo);
}
```

### 5. View Components

#### ConsentimentoDialog.java
```java
package com.inventario.view;

public class ConsentimentoDialog extends JDialog {
    private JTextArea txtPoliticaResumo;
    private JButton btnVerPoliticaCompleta;
    private JCheckBox chkConsentimentoSistema;
    private JCheckBox chkConsentimentoComunicacoes;
    private JButton btnAceitar;
    private JButton btnRecusar;
    private JLabel lblVersao;
    
    private Usuario usuario;
    private boolean consentimentoConcedido = false;
    
    public ConsentimentoDialog(JFrame parent, Usuario usuario);
    
    private void exibirPoliticaCompleta();
    private void registrarConsentimento();
    private void recusarConsentimento();
}
```

#### PoliticaPrivacidadeViewer.java
```java
package com.inventario.view;

public class PoliticaPrivacidadeViewer extends JDialog {
    private JEditorPane editorPolitica;
    private JButton btnFechar;
    private JButton btnBaixarPDF;
    private JLabel lblVersao;
    private JLabel lblDataPublicacao;
    
    public PoliticaPrivacidadeViewer(JFrame parent, 
                                     PoliticaPrivacidade politica);
    
    private void carregarConteudo();
    private void exportarPDF();
}
```

#### GestaoConsentimentosFrame.java (Admin)
```java
package com.inventario.view;

public class GestaoConsentimentosFrame extends JFrame {
    private JTable tabelaConsentimentos;
    private DefaultTableModel modeloTabela;
    private JTextField campoFiltroUsuario;
    private JComboBox<String> comboFiltroStatus;
    private JButton btnExportarRelatorio;
    private JButton btnAtualizarPolitica;
    
    // Exibir lista de consentimentos
    private void carregarConsentimentos();
    
    // Filtrar por usuário/status
    private void aplicarFiltros();
    
    // Exportar relatório de auditoria
    private void exportarRelatorio();
    
    // Abrir editor de política
    private void abrirEditorPolitica();
}
```

## Data Models

### Consentimento Flow

```
1. Usuário faz login
   ↓
2. Sistema verifica se tem consentimento válido
   ↓
3a. TEM → Permite acesso
3b. NÃO TEM → Exibe ConsentimentoDialog
   ↓
4. Usuário lê política e marca checkboxes
   ↓
5. Sistema registra consentimento no banco
   ↓
6. Sistema registra log da ação
   ↓
7. Permite acesso ao sistema
```

### Revogação Flow

```
1. Usuário acessa "Meu Perfil"
   ↓
2. Clica em "Revogar Consentimento"
   ↓
3. Sistema exibe confirmação com consequências
   ↓
4. Usuário confirma
   ↓
5. Sistema marca consentimento como revogado
   ↓
6. Sistema registra log da revogação
   ↓
7. Sistema envia email ao DPO
   ↓
8. Sistema realiza logout do usuário
```

## Error Handling

### Exceções Customizadas

```java
public class ConsentimentoException extends Exception {
    public ConsentimentoException(String message) {
        super(message);
    }
}

public class PoliticaPrivacidadeException extends Exception {
    public PoliticaPrivacidadeException(String message) {
        super(message);
    }
}
```

### Tratamento de Erros

1. **Consentimento não encontrado**: Redirecionar para tela de consentimento
2. **Erro ao salvar consentimento**: Exibir mensagem e permitir nova tentativa
3. **Política não encontrada**: Usar versão em cache ou exibir erro crítico
4. **Erro de conexão**: Permitir acesso temporário e registrar pendência
5. **Revogação falha**: Registrar tentativa e notificar admin

## Testing Strategy

### Unit Tests

1. **ConsentimentoDAO**
   - Testar inserção de consentimento
   - Testar busca de consentimento ativo
   - Testar revogação de consentimento
   - Testar validação de consentimento

2. **ConsentimentoService**
   - Testar lógica de validação
   - Testar notificação ao DPO
   - Testar registro com múltiplos tipos

3. **PoliticaPrivacidadeDAO**
   - Testar busca de política ativa
   - Testar versionamento
   - Testar histórico

### Integration Tests

1. **Fluxo completo de consentimento**
   - Login → Consentimento → Acesso
   
2. **Fluxo de revogação**
   - Revogar → Notificar → Logout

3. **Atualização de política**
   - Atualizar → Invalidar consentimentos → Solicitar novo

### UI Tests

1. **ConsentimentoDialog**
   - Verificar exibição correta
   - Testar validação de checkboxes
   - Testar navegação

2. **PoliticaPrivacidadeViewer**
   - Verificar carregamento de conteúdo
   - Testar exportação PDF

## Security Considerations

### 1. Proteção de Dados

- **Criptografia**: IP e User-Agent devem ser hasheados
- **Integridade**: Hash SHA-256 do conteúdo da política
- **Auditoria**: Todos os acessos devem ser logados

### 2. Validação de Entrada

- Validar versão da política (formato: X.Y.Z)
- Sanitizar motivo de revogação (evitar SQL injection)
- Validar IP de origem (formato válido)

### 3. Controle de Acesso

- Apenas ADMIN pode atualizar política
- Apenas ADMIN pode ver relatórios de auditoria
- Usuário só pode revogar próprio consentimento

### 4. Logs de Segurança

```java
// Registrar todas as ações críticas
logService.registrar(
    "CONSENTIMENTO_CONCEDIDO",
    idUsuario,
    ipOrigem,
    "Versão: " + versao
);
```

## Performance Considerations

### 1. Caching

```java
// Cache da política ativa em memória
private static PoliticaPrivacidade politicaCache;
private static LocalDateTime cacheExpiration;

public PoliticaPrivacidade buscarPoliticaAtiva() {
    if (politicaCache != null && 
        LocalDateTime.now().isBefore(cacheExpiration)) {
        return politicaCache;
    }
    
    politicaCache = dao.buscarPoliticaAtiva();
    cacheExpiration = LocalDateTime.now().plusHours(1);
    return politicaCache;
}
```

### 2. Índices de Banco

- Índice em `ID_USUARIO` para busca rápida
- Índice em `VERSAO_POLITICA` para validação
- Índice em `CONSENTIMENTO_ATIVO` para filtros

### 3. Lazy Loading

- Carregar conteúdo completo da política apenas quando necessário
- Carregar histórico de consentimentos sob demanda

## Deployment Strategy

### Fase 1: Preparação do Banco
1. Executar scripts de criação de tabelas
2. Inserir política de privacidade inicial (versão 1.0.0)
3. Validar estrutura

### Fase 2: Deploy do Backend
1. Deploy dos DAOs
2. Deploy dos Services
3. Testes de integração

### Fase 3: Deploy do Frontend
1. Modificar JLogin para verificar consentimento
2. Deploy dos dialogs
3. Testes de UI

### Fase 4: Migração de Usuários Existentes
1. Marcar todos os usuários existentes como "requer consentimento"
2. No próximo login, solicitar consentimento
3. Monitorar taxa de aceitação

### Fase 5: Monitoramento
1. Monitorar logs de consentimento
2. Verificar taxa de revogação
3. Ajustar textos se necessário

## Maintenance and Updates

### Atualização da Política

1. Admin cria nova versão no sistema
2. Sistema incrementa versão (1.0.0 → 1.1.0)
3. Sistema marca consentimentos antigos como "requer atualização"
4. Usuários são notificados no próximo login
5. Sistema mantém histórico de todas as versões

### Backup e Recuperação

- Backup diário das tabelas de consentimento
- Retenção de 7 anos (conforme LGPD)
- Procedimento de recuperação documentado

## Compliance Checklist

- [x] Coleta de consentimento explícito
- [x] Informação clara sobre tratamento de dados
- [x] Direito de revogação implementado
- [x] Registro de consentimentos para auditoria
- [x] Versionamento de política
- [x] Notificação ao DPO
- [x] Logs de auditoria
- [x] Segurança dos dados de consentimento
- [x] Interface acessível e clara
- [x] Consentimento granular (obrigatório vs opcional)
