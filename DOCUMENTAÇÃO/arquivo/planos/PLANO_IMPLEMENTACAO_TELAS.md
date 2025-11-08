# Plano de Implementação das Telas do Sistema

## Visão Geral
Este documento apresenta um plano detalhado para implementação das telas do Sistema de Inventário IFMT, organizando o desenvolvimento de forma estruturada para evitar erros fatais e garantir a qualidade do sistema.

## Status Atual das Telas

### ✅ Telas Implementadas
- **LoginFrame.java** - Tela de login básica
- **ModernLoginFrame.java** - Tela de login moderna (recém-ajustada)
- **MainFrame.java** - Tela principal com dashboard
- **PatrimonioFrame.java** - Gerenciamento de patrimônio
- **InventarioFrame.java** - Gerenciamento de inventários
- **ResponsavelFrame.java** - Gerenciamento de responsáveis
- **SalaFrame.java** - Gerenciamento de salas
- **SetorFrame.java** - Gerenciamento de setores
- **RelatorioFrame.java** - Geração de relatórios
- **ConfiguracaoBancoDialog.java** - Configuração do banco

### 🔄 Telas em Desenvolvimento
- **PatrimonioFormDialog.java** - Formulário de patrimônio
- **InventarioFormDialog.java** - Formulário de inventário
- **ResponsavelFormDialog.java** - Formulário de responsável
- **SalaFormDialog.java** - Formulário de sala
- **SetorFormDialog.java** - Formulário de setor

### ❌ Telas Pendentes (Críticas)
- **UsuarioFrame.java** - Gerenciamento de usuários
- **ColetaFrame.java** - Interface de coleta de inventário
- **DashboardFrame.java** - Dashboard avançado
- **ImportacaoFrame.java** - Importação de dados SUAP

## Plano de Implementação por Fases

### Fase 1: Autenticação e Navegação (Prioridade Alta)
**Prazo: 1-2 semanas**

#### 1.1 Sistema de Autenticação
- [ ] Finalizar validações no ModernLoginFrame
- [ ] Implementar recuperação de senha
- [ ] Sistema de perfis de usuário
- [ ] Controle de sessão

#### 1.2 Navegação Principal
- [ ] Melhorar MainFrame com navegação consistente
- [ ] Implementar breadcrumbs
- [ ] Sistema de notificações
- [ ] Controle de permissões por tela

### Fase 2: Gestão de Pessoas (Prioridade Alta)
**Prazo: 2-3 semanas**

#### 2.1 UsuarioFrame (CRÍTICO)
```java
// Estrutura base necessária:
public class UsuarioFrame extends JFrame {
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private UsuarioDAO usuarioDAO;
    private JTextField campoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnResetSenha;
    
    // Funcionalidades essenciais:
    // - CRUD completo de usuários
    // - Gestão de perfis e permissões
    // - Reset de senhas
    // - Ativação/desativação de contas
}
```

#### 2.2 Melhorias em ResponsavelFrame
- [ ] Validações robustas
- [ ] Integração com patrimônio
- [ ] Histórico de responsabilidades

### Fase 3: Gerenciamento de Inventários (Prioridade Crítica)
**Prazo: 2-3 semanas**

#### 3.1 Sistema de Gerenciamento de Inventários (CRÍTICO)

**Funcionalidades Essenciais:**

##### 3.1.1 Operações de Inventário
```java
// Estrutura base necessária no InventarioDAO:
public class InventarioDAO {
    // Operações de ciclo de vida do inventário
    public void abrirInventario(int idInventario) throws SQLException;
    public void fecharInventario(int idInventario) throws SQLException;
    public void reabrirInventario(int idInventario) throws SQLException;
    public void cancelarInventario(int idInventario, String motivo) throws SQLException;
    public void excluirInventario(int idInventario) throws SQLException;
    
    // Validações de estado
    public boolean podeAbrir(int idInventario) throws SQLException;
    public boolean podeFechar(int idInventario) throws SQLException;
    public boolean podeReabrir(int idInventario) throws SQLException;
    public boolean podeCancelar(int idInventario) throws SQLException;
    public boolean podeExcluir(int idInventario) throws SQLException;
    
    // Gestão de participantes
    public void adicionarParticipante(int idInventario, int idUsuario, String papel) throws SQLException;
    public void removerParticipante(int idInventario, int idUsuario) throws SQLException;
    public List<Usuario> listarParticipantes(int idInventario) throws SQLException;
}
```

##### 3.1.2 Estados do Inventário
- **PLANEJADO**: Inventário criado, aguardando abertura
- **ABERTO**: Inventário em andamento, coleta ativa
- **FECHADO**: Inventário finalizado, não permite mais coletas
- **REABERTO**: Inventário reaberto para correções
- **CANCELADO**: Inventário cancelado
- **EXCLUÍDO**: Inventário removido do sistema

##### 3.1.3 Regras de Negócio
- [ ] Apenas um inventário pode estar ABERTO por vez
- [ ] Inventário FECHADO pode ser REABERTO apenas pelo responsável
- [ ] Inventário CANCELADO não pode ser reaberto
- [ ] Inventário só pode ser EXCLUÍDO se não tiver coletas
- [ ] Participantes só podem ser alterados em inventários PLANEJADO ou ABERTO

##### 3.1.4 Seleção de Participantes
```java
// Estrutura para gestão de participantes:
public class ParticipanteInventario {
    private int idInventario;
    private int idUsuario;
    private String nomeUsuario;
    private String papel; // COORDENADOR, COLETOR, OBSERVADOR
    private Date dataInclusao;
    private boolean ativo;
}

// Interface para seleção de participantes:
public class SelecionarParticipantesDialog extends JDialog {
    private JList<Usuario> listaUsuariosDisponiveis;
    private JList<ParticipanteInventario> listaParticipantesSelecionados;
    private JComboBox<String> comboPapel;
    private JButton btnAdicionar, btnRemover;
    
    // Funcionalidades:
    // - Buscar usuários por nome/perfil
    // - Definir papel do participante
    // - Validar permissões
    // - Salvar seleção
}
```

#### 3.2 Melhorias no InventarioFrame
- [ ] Implementar botões para operações de inventário
- [ ] Adicionar validações de estado
- [ ] Integrar seleção de participantes
- [ ] Implementar confirmações de segurança
- [ ] Adicionar logs de auditoria

#### 3.3 Coleta de Inventário
**Prazo: 3-4 semanas**

##### 3.3.1 ColetaFrame (CRÍTICO)
```java
// Estrutura base necessária:
public class ColetaFrame extends JFrame {
    private JTable tabelaItens;
    private JTextField campoCodigoBarras;
    private JButton btnIniciarColeta, btnFinalizarColeta;
    private JLabel lblStatus, lblProgresso;
    private JProgressBar progressBar;
    private int idInventarioAtivo;
    
    // Funcionalidades essenciais:
    // - Verificar inventário aberto
    // - Leitura de código de barras
    // - Validação de patrimônio
    // - Registro de observações
    // - Controle de status (encontrado/não encontrado)
    // - Sincronização em tempo real
}
```

##### 3.3.2 Funcionalidade de Itens Sem Patrimônio (NOVA RECOMENDAÇÃO)
**Prioridade: Alta**

**Contexto**: Durante a auditoria do sistema, foi identificada a necessidade de implementar funcionalidades para coleta de itens que não possuem etiqueta de patrimônio ou número de patrimônio.

**Implementações Necessárias:**

###### 3.3.2.1 Atualização do Modelo de Dados
```sql
-- Adicionar campo na tabela de coleta
ALTER TABLE TABELA_COLETA ADD COLUMN SEM_ETIQUETA BOOLEAN DEFAULT FALSE;

-- Índice para otimizar consultas
CREATE INDEX idx_coleta_sem_etiqueta ON TABELA_COLETA(SEM_ETIQUETA);
```

###### 3.3.2.2 Atualização da Classe Coleta
```java
public class Coleta {
    // Campos existentes...
    private boolean semEtiqueta;
    
    // Getters e Setters
    public boolean isSemEtiqueta() { return semEtiqueta; }
    public void setSemEtiqueta(boolean semEtiqueta) { this.semEtiqueta = semEtiqueta; }
}
```

###### 3.3.2.3 Atualização do ColetaFrame
```java
public class ColetaFrame extends JFrame {
    // Componentes existentes...
    private JCheckBox chkSemEtiqueta;
    private JTextArea txtDescricaoItem;
    private JComboBox<String> cmbCategoriaItem;
    
    // Funcionalidades para itens sem etiqueta:
    // - Checkbox "Sem Etiqueta" para marcar itens sem patrimônio
    // - Campo de descrição livre para itens sem etiqueta
    // - Seleção de categoria para classificação
    // - Validação: quando "Sem Etiqueta" marcado, desabilitar campo patrimônio
    // - Registro de localização onde foi encontrado
    // - Foto obrigatória para itens sem etiqueta
}
```

###### 3.3.2.4 Regras de Negócio
- [ ] Quando "Sem Etiqueta" estiver marcado:
  - [ ] Desabilitar campo de número de patrimônio
  - [ ] Tornar obrigatório: descrição, categoria e localização
  - [ ] Exigir pelo menos uma foto do item
  - [ ] Registrar coordenadas GPS se disponível
- [ ] Validações específicas:
  - [ ] Não permitir patrimônio e "sem etiqueta" simultaneamente
  - [ ] Descrição mínima de 10 caracteres para itens sem etiqueta
  - [ ] Categoria obrigatória para classificação

###### 3.3.2.5 Relatórios Específicos
```java
// Novos métodos no RelatorioColetaDAO
public List<Coleta> gerarRelatorioItensSemEtiqueta(int idInventario);
public int contarItensSemEtiqueta(int idInventario);
public Map<String, Integer> estatisticasItensSemEtiqueta(int idInventario);
```

**Relatórios a implementar:**
- [ ] Relatório de Itens Sem Etiqueta por Inventário
- [ ] Relatório de Itens Sem Etiqueta por Localização
- [ ] Relatório de Itens Sem Etiqueta por Categoria
- [ ] Dashboard com estatísticas de itens sem etiqueta

##### 3.3.3 Integração com Hardware
- [ ] Suporte a leitores de código de barras
- [ ] Interface para câmera/webcam
- [ ] Validação offline/online
- [ ] Captura de GPS para itens sem etiqueta

### Fase 4: Relatórios e Dashboard (Prioridade Média)
**Prazo: 2-3 semanas**

#### 4.1 Melhorias no RelatorioFrame
- [ ] Novos tipos de relatório
- [ ] Exportação em múltiplos formatos
- [ ] Agendamento de relatórios
- [ ] Relatórios gráficos

#### 4.2 DashboardFrame Avançado
- [ ] Indicadores em tempo real
- [ ] Gráficos interativos
- [ ] Alertas e notificações
- [ ] Métricas de performance

### Fase 5: Importação e Integração (Prioridade Média)
**Prazo: 2-3 semanas**

#### 5.1 ImportacaoFrame
- [ ] Interface para importação SUAP
- [ ] Validação de dados
- [ ] Mapeamento de campos
- [ ] Relatório de importação
- [ ] Backup automático

## Estratégias para Evitar Erros Fatais

### 1. Padrões de Desenvolvimento

#### 1.1 Estrutura Padrão de Telas
```java
public class [Nome]Frame extends JFrame {
    // Componentes da interface
    private JTable tabela;
    private DefaultTableModel modelo;
    private [Entidade]DAO dao;
    
    // Construtor
    public [Nome]Frame() {
        dao = new [Entidade]DAO();
        initComponents();
        carregarDados();
    }
    
    // Métodos obrigatórios
    private void initComponents() { /* Layout */ }
    private void carregarDados() { /* Dados */ }
    private void setupEventListeners() { /* Eventos */ }
    private boolean validarDados() { /* Validação */ }
}
```

#### 1.2 Tratamento de Erros
```java
// Padrão para operações de banco
try {
    dao.operacao();
    JOptionPane.showMessageDialog(this, "Operação realizada com sucesso!");
    carregarDados();
} catch (SQLException e) {
    logger.error("Erro na operação", e);
    JOptionPane.showMessageDialog(this, 
        "Erro: " + e.getMessage(), 
        "Erro", 
        JOptionPane.ERROR_MESSAGE);
}
```

### 2. Validações Obrigatórias

#### 2.1 Validação de Entrada
- [ ] Campos obrigatórios
- [ ] Formato de dados
- [ ] Limites de caracteres
- [ ] Caracteres especiais

#### 2.2 Validação de Negócio
- [ ] Regras de integridade
- [ ] Permissões de usuário
- [ ] Estados válidos
- [ ] Dependências entre entidades

### 3. Gerenciamento de Estado

#### 3.1 Controle de Sessão
- [ ] Timeout automático
- [ ] Validação de permissões
- [ ] Log de atividades
- [ ] Backup de dados não salvos

#### 3.2 Sincronização de Dados
- [ ] Refresh automático
- [ ] Controle de concorrência
- [ ] Versionamento de dados
- [ ] Resolução de conflitos

### 4. Testes e Qualidade

#### 4.1 Testes Unitários
- [ ] Validações de entrada
- [ ] Lógica de negócio
- [ ] Operações de banco
- [ ] Tratamento de erros

#### 4.2 Testes de Interface
- [ ] Navegação entre telas
- [ ] Responsividade
- [ ] Usabilidade
- [ ] Acessibilidade

## Cronograma Sugerido

| Semana | Atividade | Responsável | Status |
|--------|-----------|-------------|--------|
| 1 | **Gerenciamento de Inventários** | Dev | 🔄 |
| | - Atualizar InventarioDAO | | |
| | - Criar tabela participantes | | |
| | - Implementar operações de inventário | | |
| 2 | **Seleção de Participantes** | Dev | ⏳ |
| | - SelecionarParticipantesDialog | | |
| | - Integração com InventarioFrame | | |
| 2-3 | **Itens Sem Patrimônio (NOVA)** | Dev | ⏳ |
| | - Atualizar banco de dados (campo SEM_ETIQUETA) | | |
| | - Atualizar classe Coleta e ColetaDAO | | |
| | - Implementar interface no ColetaFrame | | |
| | - Criar relatórios específicos | | |
| 4 | Implementar UsuarioFrame | Dev | ⏳ |
| 5 | Implementar ColetaFrame base | Dev | ⏳ |
| 6 | Integração com hardware | Dev | ⏳ |
| 7 | Melhorias em relatórios | Dev | ⏳ |
| 8 | Dashboard avançado | Dev | ⏳ |
| 9 | ImportacaoFrame | Dev | ⏳ |
| 10 | Testes e refinamentos | QA | ⏳ |

## Próximos Passos Imediatos

### 1. Implementar Gerenciamento de Inventários (PRIORIDADE MÁXIMA)
**Prazo: Esta Semana**

#### 1.1 Atualizar InventarioDAO
- [ ] Implementar método `abrirInventario(int id)`
- [ ] Implementar método `fecharInventario(int id)`
- [ ] Implementar método `reabrirInventario(int id)`
- [ ] Implementar método `cancelarInventario(int id, String motivo)`
- [ ] Implementar método `excluirInventario(int id)`
- [ ] Adicionar validações de estado
- [ ] Implementar logs de auditoria

#### 1.2 Criar Tabela de Participantes
```sql
CREATE TABLE IF NOT EXISTS TABELA_PARTICIPANTE_INVENTARIO (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_USUARIO INTEGER NOT NULL,
    PAPEL VARCHAR(50) NOT NULL, -- COORDENADOR, COLETOR, OBSERVADOR
    DATA_INCLUSAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ATIVO BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (ID_INVENTARIO) REFERENCES TABELA_INVENTARIO(ID),
    FOREIGN KEY (ID_USUARIO) REFERENCES TABELA_USUARIO(ID)
);
```

#### 1.3 Implementar SelecionarParticipantesDialog
- [ ] Criar interface de seleção
- [ ] Implementar busca de usuários
- [ ] Adicionar validações de papel
- [ ] Integrar com InventarioFrame

#### 1.4 Atualizar InventarioFrame
- [ ] Adicionar botões: Abrir, Fechar, Reabrir, Cancelar, Excluir
- [ ] Implementar validações de estado
- [ ] Adicionar botão "Participantes"
- [ ] Implementar confirmações de segurança
- [ ] Atualizar carregamento de dados

#### 1.5 Implementar Funcionalidade de Itens Sem Patrimônio (NOVA RECOMENDAÇÃO)
**Prazo: Próximas 2 Semanas**

##### 1.5.1 Atualização do Banco de Dados
- [ ] Executar script para adicionar campo `SEM_ETIQUETA` na `TABELA_COLETA`
- [ ] Criar índice para otimização de consultas
- [ ] Testar integridade dos dados existentes

##### 1.5.2 Atualização do Modelo
- [ ] Adicionar campo `semEtiqueta` na classe `Coleta.java`
- [ ] Implementar getters e setters
- [ ] Atualizar `ColetaDAO` para suportar o novo campo
- [ ] Adicionar validações no DAO

##### 1.5.3 Atualização da Interface
- [ ] Adicionar checkbox "Sem Etiqueta" no `ColetaFrame`
- [ ] Implementar campos adicionais: descrição e categoria
- [ ] Adicionar validações de interface
- [ ] Implementar lógica de habilitação/desabilitação de campos

##### 1.5.4 Relatórios Específicos
- [ ] Implementar `gerarRelatorioItensSemEtiqueta()` no `RelatorioColetaDAO`
- [ ] Adicionar opção no `RelatorioFrame` para relatórios de itens sem etiqueta
- [ ] Criar estatísticas no dashboard para itens sem etiqueta
- [ ] Implementar exportação de relatórios específicos

### 2. Implementar UsuarioFrame (Próxima Semana)
- [ ] Criar estrutura básica
- [ ] Implementar CRUD
- [ ] Adicionar validações
- [ ] Testes básicos

### 3. Implementar ColetaFrame (Semana Seguinte)
- [ ] Interface básica
- [ ] Verificação de inventário aberto
- [ ] Integração com PatrimonioDAO
- [ ] Validações de coleta
- [ ] Sistema de status

### 4. Estabelecer Padrões
- [ ] Documentar padrões de código
- [ ] Criar templates de telas
- [ ] Definir convenções de nomenclatura
- [ ] Estabelecer processo de code review

## Observações Importantes

1. **Backup Regular**: Fazer backup do código antes de grandes mudanças
2. **Versionamento**: Usar Git para controle de versão
3. **Documentação**: Manter documentação atualizada
4. **Testes**: Testar cada funcionalidade antes de integrar
5. **Performance**: Monitorar performance das consultas
6. **Segurança**: Validar todas as entradas do usuário
7. **Itens Sem Patrimônio**: 
   - Implementar validações rigorosas para evitar inconsistências
   - Garantir que fotos sejam obrigatórias para itens sem etiqueta
   - Testar cenários de migração de dados existentes
   - Documentar processo de treinamento para usuários
   - Considerar impacto nos relatórios existentes

---

**Última Atualização**: $(date)
**Versão**: 1.0
**Responsável**: Equipe de Desenvolvimento