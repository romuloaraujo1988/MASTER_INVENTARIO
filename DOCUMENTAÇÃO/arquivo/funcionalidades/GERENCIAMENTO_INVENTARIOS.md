# Gerenciamento de Inventários - Sistema IFMT

## Visão Geral

Este documento detalha o sistema de gerenciamento de inventários, incluindo operações de ciclo de vida, seleção de participantes e regras de negócio.

## Estados do Inventário

### 1. PLANEJADO
- **Descrição**: Inventário criado, aguardando abertura
- **Operações Permitidas**: 
  - Editar informações básicas
  - Adicionar/remover participantes
  - Abrir inventário
  - Excluir inventário (se sem coletas)
- **Transições Possíveis**: PLANEJADO → ABERTO

### 2. ABERTO
- **Descrição**: Inventário em andamento, coleta ativa
- **Operações Permitidas**:
  - Realizar coletas
  - Adicionar/remover participantes
  - Fechar inventário
  - Cancelar inventário
- **Transições Possíveis**: ABERTO → FECHADO, ABERTO → CANCELADO
- **Restrições**: Apenas um inventário pode estar ABERTO por vez

### 3. FECHADO
- **Descrição**: Inventário finalizado, não permite mais coletas
- **Operações Permitidas**:
  - Visualizar dados
  - Gerar relatórios
  - Reabrir inventário (apenas responsável)
- **Transições Possíveis**: FECHADO → REABERTO

### 4. REABERTO
- **Descrição**: Inventário reaberto para correções
- **Operações Permitidas**:
  - Realizar coletas adicionais
  - Corrigir dados
  - Fechar novamente
  - Cancelar inventário
- **Transições Possíveis**: REABERTO → FECHADO, REABERTO → CANCELADO

### 5. CANCELADO
- **Descrição**: Inventário cancelado
- **Operações Permitidas**:
  - Visualizar dados
  - Gerar relatórios
- **Transições Possíveis**: Nenhuma (estado final)

### 6. EXCLUÍDO
- **Descrição**: Inventário removido do sistema
- **Condições**: Apenas inventários sem coletas podem ser excluídos

## Operações de Inventário

### 1. Abrir Inventário

**Pré-condições**:
- Status deve ser PLANEJADO
- Não pode haver outro inventário ABERTO
- Deve ter pelo menos um participante com papel COORDENADOR

**Processo**:
1. Validar pré-condições
2. Atualizar status para ABERTO
3. Registrar data de início
4. Registrar log de auditoria
5. Notificar participantes

**Pós-condições**:
- Status = ABERTO
- Coletas podem ser iniciadas
- Outros inventários não podem ser abertos

### 2. Fechar Inventário

**Pré-condições**:
- Status deve ser ABERTO ou REABERTO
- Usuário deve ser COORDENADOR ou ADMINISTRADOR

**Processo**:
1. Validar pré-condições
2. Calcular estatísticas finais
3. Atualizar status para FECHADO
4. Registrar data de fim
5. Registrar log de auditoria
6. Gerar relatório automático

**Pós-condições**:
- Status = FECHADO
- Coletas não são mais permitidas
- Relatórios finais disponíveis

### 3. Reabrir Inventário

**Pré-condições**:
- Status deve ser FECHADO
- Usuário deve ser o responsável do inventário ou ADMINISTRADOR
- Não pode haver outro inventário ABERTO

**Processo**:
1. Validar pré-condições
2. Solicitar justificativa
3. Atualizar status para REABERTO
4. Registrar log de auditoria
5. Notificar participantes

**Pós-condições**:
- Status = REABERTO
- Coletas podem ser retomadas
- Histórico de reabertura mantido

### 4. Cancelar Inventário

**Pré-condições**:
- Status deve ser ABERTO ou REABERTO
- Usuário deve ser COORDENADOR ou ADMINISTRADOR

**Processo**:
1. Validar pré-condições
2. Solicitar motivo do cancelamento
3. Confirmar operação
4. Atualizar status para CANCELADO
5. Registrar log de auditoria
6. Notificar participantes

**Pós-condições**:
- Status = CANCELADO
- Coletas não são mais permitidas
- Dados preservados para consulta

### 5. Excluir Inventário

**Pré-condições**:
- Status deve ser PLANEJADO ou CANCELADO
- Não deve ter coletas registradas
- Usuário deve ser ADMINISTRADOR

**Processo**:
1. Validar pré-condições
2. Verificar ausência de coletas
3. Confirmar exclusão
4. Remover participantes
5. Excluir inventário
6. Registrar log de auditoria

**Pós-condições**:
- Inventário removido do sistema
- Dados não recuperáveis

## Gestão de Participantes

### Papéis dos Participantes

#### COORDENADOR
- **Responsabilidades**:
  - Gerenciar o inventário
  - Coordenar equipe de coleta
  - Aprovar alterações
  - Fechar/reabrir inventário
- **Restrições**:
  - Apenas um coordenador ativo por inventário
  - Não pode ser removido se for o único coordenador

#### COLETOR
- **Responsabilidades**:
  - Realizar coletas de patrimônio
  - Registrar observações
  - Reportar divergências
- **Restrições**:
  - Pode ser adicionado/removido a qualquer momento
  - Múltiplos coletores permitidos

#### OBSERVADOR
- **Responsabilidades**:
  - Acompanhar progresso
  - Visualizar relatórios
  - Não pode realizar coletas
- **Restrições**:
  - Acesso somente leitura
  - Múltiplos observadores permitidos

### Operações com Participantes

#### Adicionar Participante

**Pré-condições**:
- Inventário deve estar em status PLANEJADO, ABERTO ou REABERTO
- Usuário não deve já ser participante
- Usuário deve ter perfil compatível com o papel

**Processo**:
1. Validar pré-condições
2. Verificar permissões
3. Definir papel do participante
4. Registrar participação
5. Notificar usuário

#### Remover Participante

**Pré-condições**:
- Inventário deve estar em status PLANEJADO, ABERTO ou REABERTO
- Não pode remover o único coordenador
- Usuário deve ter permissão

**Processo**:
1. Validar pré-condições
2. Verificar se não é único coordenador
3. Marcar como inativo
4. Registrar data de remoção
5. Notificar usuário

#### Alterar Papel

**Pré-condições**:
- Inventário deve estar em status PLANEJADO, ABERTO ou REABERTO
- Novo papel deve ser válido
- Não pode deixar inventário sem coordenador

**Processo**:
1. Validar pré-condições
2. Verificar regras de negócio
3. Atualizar papel
4. Registrar alteração
5. Notificar usuário

## Interface de Usuário

### Melhorias no InventarioFrame

#### Novos Botões
- **Abrir**: Abre inventário selecionado
- **Fechar**: Fecha inventário em andamento
- **Reabrir**: Reabre inventário fechado
- **Cancelar**: Cancela inventário em andamento
- **Excluir**: Remove inventário do sistema
- **Participantes**: Gerencia participantes do inventário

#### Validações de Interface
- Botões habilitados/desabilitados conforme estado
- Confirmações de segurança para operações críticas
- Mensagens de erro claras e específicas
- Indicadores visuais de status

#### Colunas Adicionais na Tabela
- **Status**: Estado atual do inventário
- **Participantes**: Quantidade de participantes ativos
- **Coordenador**: Nome do coordenador principal
- **Última Atividade**: Data da última coleta ou alteração

### SelecionarParticipantesDialog

#### Componentes
- **Lista de Usuários Disponíveis**: Usuários que podem ser adicionados
- **Lista de Participantes Atuais**: Participantes já selecionados
- **Combo de Papel**: Seleção do papel do participante
- **Campo de Busca**: Filtro por nome ou perfil
- **Botões de Ação**: Adicionar, Remover, Salvar, Cancelar

#### Funcionalidades
- Busca em tempo real de usuários
- Validação de papéis
- Prevenção de duplicatas
- Confirmação de alterações
- Histórico de participações

## Logs de Auditoria

### Eventos Registrados
- Criação de inventário
- Abertura de inventário
- Fechamento de inventário
- Reabertura de inventário
- Cancelamento de inventário
- Exclusão de inventário
- Adição de participante
- Remoção de participante
- Alteração de papel

### Informações do Log
- Data/hora da operação
- Usuário que executou
- Tipo de operação
- ID do inventário
- Dados antes/depois (quando aplicável)
- Justificativa (quando solicitada)
- IP do usuário

## Regras de Negócio

### Regras Gerais
1. Apenas um inventário pode estar ABERTO por vez
2. Inventário FECHADO pode ser REABERTO apenas pelo responsável
3. Inventário CANCELADO não pode ser reaberto
4. Inventário só pode ser EXCLUÍDO se não tiver coletas
5. Participantes só podem ser alterados em inventários PLANEJADO, ABERTO ou REABERTO

### Regras de Participação
1. Todo inventário deve ter pelo menos um COORDENADOR
2. Apenas um COORDENADOR ativo por inventário
3. COORDENADOR não pode ser removido se for o único
4. Múltiplos COLETORES e OBSERVADORES são permitidos
5. Usuário não pode participar do mesmo inventário com papéis diferentes

### Regras de Permissão
1. ADMINISTRADOR pode executar todas as operações
2. COORDENADOR pode gerenciar seu inventário
3. COLETOR pode apenas realizar coletas
4. OBSERVADOR tem acesso somente leitura
5. Usuário comum não pode acessar inventários

## Implementação Técnica

### Estrutura de Classes

```java
// Modelo de dados
public class ParticipanteInventario {
    private int id;
    private int idInventario;
    private int idUsuario;
    private String nomeUsuario;
    private String papel;
    private Date dataInclusao;
    private Date dataRemocao;
    private boolean ativo;
    private String observacoes;
}

// DAO para operações
public class InventarioDAO {
    // Operações de ciclo de vida
    public void abrirInventario(int idInventario) throws SQLException;
    public void fecharInventario(int idInventario) throws SQLException;
    public void reabrirInventario(int idInventario, String justificativa) throws SQLException;
    public void cancelarInventario(int idInventario, String motivo) throws SQLException;
    public void excluirInventario(int idInventario) throws SQLException;
    
    // Validações
    public boolean podeAbrir(int idInventario) throws SQLException;
    public boolean podeFechar(int idInventario) throws SQLException;
    public boolean podeReabrir(int idInventario) throws SQLException;
    public boolean podeCancelar(int idInventario) throws SQLException;
    public boolean podeExcluir(int idInventario) throws SQLException;
    
    // Gestão de participantes
    public void adicionarParticipante(int idInventario, int idUsuario, String papel) throws SQLException;
    public void removerParticipante(int idInventario, int idUsuario) throws SQLException;
    public void alterarPapelParticipante(int idInventario, int idUsuario, String novoPapel) throws SQLException;
    public List<ParticipanteInventario> listarParticipantes(int idInventario) throws SQLException;
    
    // Consultas
    public Inventario obterInventarioAberto() throws SQLException;
    public boolean temInventarioAberto() throws SQLException;
    public List<Inventario> listarInventariosPorStatus(String status) throws SQLException;
}

// Interface de seleção de participantes
public class SelecionarParticipantesDialog extends JDialog {
    private int idInventario;
    private JList<Usuario> listaUsuariosDisponiveis;
    private JList<ParticipanteInventario> listaParticipantesSelecionados;
    private JComboBox<String> comboPapel;
    private JTextField campoFiltro;
    private JButton btnAdicionar, btnRemover, btnSalvar, btnCancelar;
    
    public SelecionarParticipantesDialog(JFrame parent, int idInventario);
    private void carregarUsuariosDisponiveis();
    private void carregarParticipantesAtuais();
    private void adicionarParticipante();
    private void removerParticipante();
    private void salvarAlteracoes();
    private void filtrarUsuarios();
}
```

### Scripts SQL

- `script_tabela_participante_inventario.sql`: Criação da tabela de participantes
- Triggers para validação automática
- Views para consultas otimizadas
- Índices para performance

## Próximos Passos

1. **Implementar InventarioDAO** com todas as operações
2. **Criar SelecionarParticipantesDialog** com interface completa
3. **Atualizar InventarioFrame** com novos botões e validações
4. **Implementar logs de auditoria** para rastreabilidade
5. **Criar testes unitários** para validar regras de negócio
6. **Documentar APIs** para integração futura

---

**Versão**: 1.0  
**Data**: $(date)  
**Responsável**: Equipe de Desenvolvimento