# HistoricoColetaDialog - Guia de Uso

## Descrição

O `HistoricoColetaDialog` é um diálogo modal que exibe o histórico completo de coletas de um patrimônio, com funcionalidades de:

- ✅ Visualização de histórico em tabela formatada
- ✅ Filtros por inventário, coletor e período
- ✅ Exportação para PDF e Excel
- ✅ Estatísticas do histórico
- ✅ Carregamento assíncrono
- ✅ Interface responsiva e moderna

## Como Usar

### Uso Básico (apenas com ID)

```java
// Abrir diálogo com ID do patrimônio
HistoricoColetaDialog dialog = new HistoricoColetaDialog(parentFrame, patrimonioId);
dialog.setVisible(true);
```

### Uso Completo (com dados do patrimônio)

```java
// Abrir diálogo com informações completas
HistoricoColetaDialog dialog = new HistoricoColetaDialog(
    parentFrame,
    patrimonioId,
    "12345",                    // Número do patrimônio
    "CADEIRA GIRATÓRIA PRETA"   // Descrição do patrimônio
);
dialog.setVisible(true);
```

### Exemplo em um Botão

```java
btnHistorico.addActionListener(e -> {
    try {
        // Obter ID do patrimônio selecionado
        int patrimonioId = obterPatrimonioSelecionado();
        
        // Criar e exibir diálogo
        HistoricoColetaDialog dialog = new HistoricoColetaDialog(
            this,
            patrimonioId,
            txtNumeroPatrimonio.getText(),
            txtDescricaoPatrimonio.getText()
        );
        
        dialog.setVisible(true);
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            "Erro ao abrir histórico: " + ex.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE
        );
    }
});
```

### Recarregar Dados

```java
// Criar diálogo
HistoricoColetaDialog dialog = new HistoricoColetaDialog(parentFrame, patrimonioId);

// Exibir
dialog.setVisible(true);

// Recarregar após alterações externas
dialog.recarregar();
```

### Atualizar Dados do Patrimônio

```java
// Criar diálogo
HistoricoColetaDialog dialog = new HistoricoColetaDialog(parentFrame, patrimonioId);

// Atualizar informações exibidas
dialog.setDadosPatrimonio("54321", "MESA DE ESCRITÓRIO");

// Exibir
dialog.setVisible(true);
```

## Funcionalidades Disponíveis

### 1. Filtros

O usuário pode filtrar o histórico por:
- **Inventário**: Selecionar um inventário específico
- **Coletor**: Filtrar por coletor específico
- **Período**: Definir data de início e fim

### 2. Exportação

Dois formatos disponíveis:
- **PDF**: Relatório formatado com estatísticas
- **Excel**: Planilha com dados tabulados

### 3. Estatísticas

Exibe automaticamente:
- Total de coletas
- Período (primeira e última coleta)
- Mudanças de localização
- Mudanças de estado de conservação

### 4. Tabela Interativa

- Cores destacam mudanças (localização em amarelo, estado em vermelho)
- Colunas redimensionáveis
- Scroll automático
- Formatação profissional

## Propriedades do Diálogo

- **Tamanho padrão**: 1200x700 pixels
- **Tamanho mínimo**: 900x500 pixels
- **Modal**: Sim (bloqueia janela pai)
- **Centralizado**: Sim (relativo ao pai)
- **Redimensionável**: Sim

## Dependências

O diálogo depende de:
- `HistoricoColetaPanel` - Painel principal
- `HistoricoColetaService` - Serviço de negócio
- `HistoricoTableModel` - Modelo da tabela
- `HistoricoTableCellRenderer` - Renderizador customizado
- DTOs: `HistoricoColetaDTO`, `EstatisticasHistoricoDTO`, `FiltroHistoricoDTO`

## Exemplo Completo

```java
public class PatrimonioFrame extends JFrame {
    
    private JTable tabelaPatrimonios;
    private JButton btnVerHistorico;
    
    private void initComponents() {
        btnVerHistorico = new JButton("Ver Histórico");
        btnVerHistorico.addActionListener(e -> abrirHistorico());
        
        // ... outros componentes
    }
    
    private void abrirHistorico() {
        // Obter patrimônio selecionado
        int linhaSelecionada = tabelaPatrimonios.getSelectedRow();
        
        if (linhaSelecionada < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Selecione um patrimônio para ver o histórico.",
                "Aviso",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Obter dados da linha selecionada
        int patrimonioId = (int) tabelaPatrimonios.getValueAt(linhaSelecionada, 0);
        String numero = (String) tabelaPatrimonios.getValueAt(linhaSelecionada, 1);
        String descricao = (String) tabelaPatrimonios.getValueAt(linhaSelecionada, 2);
        
        // Criar e exibir diálogo
        HistoricoColetaDialog dialog = new HistoricoColetaDialog(
            this,
            patrimonioId,
            numero,
            descricao
        );
        
        dialog.setVisible(true);
    }
}
```

## Notas Importantes

1. **Thread Safety**: O carregamento de dados é feito em background usando `SwingWorker`
2. **Validação**: Filtros de data são validados antes de aplicar
3. **Exportação**: Usuário escolhe local e nome do arquivo
4. **Erro Handling**: Erros são tratados e exibidos ao usuário
5. **Performance**: Tabela otimizada para grandes volumes de dados

## Versão

- **Versão**: 1.0.0
- **Data**: 17/01/2026
- **Status**: ✅ Implementado e testado
