# Migração do RelatorioFrame para MVVM

## Status Atual

✅ **ViewModel Criado**: `RelatorioViewModel.java`
✅ **State Criado**: `RelatorioState.java`
⏳ **View (RelatorioFrame)**: Precisa ser refatorado para usar ViewModel

## Arquitetura Implementada

```
┌─────────────────────────────────────────────────────────────┐
│                    RelatorioFrame (View)                     │
│  - Apenas UI e renderização                                  │
│  - Observa mudanças no ViewModel                             │
│  - Delega ações para ViewModel                               │
└──────────────────────┬──────────────────────────────────────┘
                       │ observa
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              RelatorioViewModel (ViewModel)                  │
│  - Gerencia RelatorioState                                   │
│  - Coordena operações de negócio                             │
│  - Notifica View sobre mudanças                              │
└──────────────────────┬──────────────────────────────────────┘
                       │ usa (temporário)
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    DAOs (Data Layer)                         │
│  - RelatorioColetaDAO                                        │
│  - InventarioDAO, SetorDAO, etc.                             │
│  TODO: Migrar para Use Cases                                 │
└─────────────────────────────────────────────────────────────┘
```

## Como Refatorar o RelatorioFrame

### 1. Remover Acesso Direto aos DAOs

**ANTES (Errado):**
```java
public class RelatorioFrame extends JFrame {
    private RelatorioColetaDAO relatorioDAO;
    private InventarioDAO inventarioDAO;
    
    public RelatorioFrame() {
        this.relatorioDAO = new RelatorioColetaDAO();
        this.inventarioDAO = new InventarioDAO();
    }
    
    private void gerarRelatorio() {
        List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
        preencherTabela(dados);
    }
}
```

**DEPOIS (Correto):**
```java
public class RelatorioFrame extends JFrame {
    private final RelatorioViewModel viewModel;
    
    public RelatorioFrame() {
        this.viewModel = new RelatorioViewModel();
        observarViewModel();
    }
    
    private void observarViewModel() {
        viewModel.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName())) {
                atualizarUI((RelatorioState) evt.getNewValue());
            }
        });
    }
    
    private void gerarRelatorio() {
        // Delegar para ViewModel
        viewModel.gerarRelatorio(tipoRelatorio, idInventario, setor, responsavel, dataInicio, dataFim);
    }
    
    private void atualizarUI(RelatorioState state) {
        if (state instanceof RelatorioState.Loading) {
            mostrarLoading();
        } else if (state instanceof RelatorioState.Success) {
            RelatorioState.Success success = (RelatorioState.Success) state;
            preencherTabela(success.getDados());
            esconderLoading();
        } else if (state instanceof RelatorioState.Error) {
            RelatorioState.Error error = (RelatorioState.Error) state;
            mostrarErro(error.getMessage());
            esconderLoading();
        }
    }
}
```

### 2. Padrão Observer para Mudanças de Estado

```java
private void observarViewModel() {
    viewModel.addPropertyChangeListener(evt -> {
        if ("state".equals(evt.getPropertyName())) {
            RelatorioState newState = (RelatorioState) evt.getNewValue();
            
            // Atualizar UI baseado no estado
            SwingUtilities.invokeLater(() -> {
                atualizarUI(newState);
            });
        }
    });
}
```

### 3. Separação de Responsabilidades

**View (RelatorioFrame):**
- ✅ Criar componentes Swing
- ✅ Renderizar dados na tela
- ✅ Capturar eventos do usuário
- ✅ Observar mudanças no ViewModel
- ❌ Lógica de negócio
- ❌ Acesso direto a DAOs
- ❌ Validações complexas

**ViewModel (RelatorioViewModel):**
- ✅ Gerenciar estado da UI
- ✅ Coordenar operações de negócio
- ✅ Validar entrada do usuário
- ✅ Notificar View sobre mudanças
- ❌ Conhecer componentes Swing
- ❌ Manipular UI diretamente

## Benefícios da Migração

1. **Testabilidade**: ViewModel pode ser testado sem UI
2. **Separação de Responsabilidades**: View só cuida de UI
3. **Reusabilidade**: ViewModel pode ser usado em diferentes Views
4. **Manutenibilidade**: Mudanças de negócio não afetam UI
5. **Preparação para Use Cases**: Facilita migração futura

## Próximos Passos

1. ✅ Criar `RelatorioViewModel`
2. ✅ Criar `RelatorioState`
3. ⏳ Refatorar `RelatorioFrame` para usar ViewModel
4. 🔜 Criar Use Cases (BuscarRelatorioUseCase, etc.)
5. 🔜 Migrar ViewModel para usar Use Cases ao invés de DAOs

## Exemplo Completo de Método Refatorado

```java
// ANTES: Método com lógica de negócio na View
private void btnGerarActionPerformed() {
    try {
        progressBar.setVisible(true);
        labelStatus.setText("Gerando relatório...");
        
        int idInventario = obterIdInventarioSelecionado();
        if (idInventario == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um inventário");
            return;
        }
        
        List<Map<String, Object>> dados = relatorioDAO.gerarRelatorioItensEncontrados(idInventario);
        
        if (dados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum dado encontrado");
        } else {
            preencherTabela(dados);
            labelStatus.setText("Relatório gerado com sucesso!");
        }
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
    } finally {
        progressBar.setVisible(false);
    }
}

// DEPOIS: View delega para ViewModel
private void btnGerarActionPerformed() {
    String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();
    int idInventario = obterIdInventarioSelecionado();
    String setor = (String) comboSetor.getSelectedItem();
    String responsavel = (String) comboResponsavel.getSelectedItem();
    Date dataInicio = (Date) spinnerDataInicio.getValue();
    Date dataFim = (Date) spinnerDataFim.getValue();
    
    // Delegar tudo para ViewModel
    viewModel.gerarRelatorio(tipoRelatorio, idInventario, setor, responsavel, dataInicio, dataFim);
}

// Método separado para atualizar UI baseado no estado
private void atualizarUI(RelatorioState state) {
    if (state instanceof RelatorioState.Idle) {
        progressBar.setVisible(false);
        labelStatus.setText("Pronto para gerar relatório");
        
    } else if (state instanceof RelatorioState.Loading) {
        progressBar.setVisible(true);
        labelStatus.setText("Gerando relatório...");
        btnGerar.setEnabled(false);
        
    } else if (state instanceof RelatorioState.Success) {
        RelatorioState.Success success = (RelatorioState.Success) state;
        progressBar.setVisible(false);
        labelStatus.setText("Relatório gerado com sucesso!");
        btnGerar.setEnabled(true);
        
        List<Map<String, Object>> dados = success.getDados();
        if (dados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum dado encontrado");
        } else {
            preencherTabela(dados);
            gerarResumoEstatistico(dados);
        }
        
    } else if (state instanceof RelatorioState.Error) {
        RelatorioState.Error error = (RelatorioState.Error) state;
        progressBar.setVisible(false);
        labelStatus.setText("Erro ao gerar relatório");
        btnGerar.setEnabled(true);
        JOptionPane.showMessageDialog(this, error.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
```

## Checklist de Migração

- [ ] Substituir DAOs por ViewModel no construtor
- [ ] Adicionar PropertyChangeListener para observar estado
- [ ] Criar método `atualizarUI(RelatorioState)` centralizado
- [ ] Refatorar todos os botões para delegar ao ViewModel
- [ ] Remover lógica de negócio dos event handlers
- [ ] Remover validações complexas da View
- [ ] Testar todos os fluxos de relatório
- [ ] Verificar tratamento de erros
- [ ] Documentar mudanças

## Referências

- `clean-architecture.md` - Diretrizes gerais
- `migration-guide.md` - Guia de migração
- `clean-architecture-progress.md` - Progresso da implementação
