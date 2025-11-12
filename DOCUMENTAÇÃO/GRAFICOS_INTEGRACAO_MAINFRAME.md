# 🔗 Integração de Gráficos no MainFrame

## Como Adicionar Dashboard ao Menu Principal

### Passo 1: Adicionar Item de Menu

No arquivo `MainFrame.java`, adicione um item de menu para abrir o dashboard:

```java
private void criarMenuRelatorios() {
    JMenu menuRelatorios = new JMenu("Relatórios");
    menuRelatorios.setFont(new Font("Arial", Font.PLAIN, 14));
    
    // Item Dashboard
    JMenuItem itemDashboard = new JMenuItem("📊 Dashboard Estatístico");
    itemDashboard.setFont(new Font("Arial", Font.PLAIN, 14));
    itemDashboard.addActionListener(e -> abrirDashboard());
    
    // Outros itens de relatório...
    JMenuItem itemRelatorioGeral = new JMenuItem("📄 Relatório Geral");
    // ...
    
    menuRelatorios.add(itemDashboard);
    menuRelatorios.addSeparator();
    menuRelatorios.add(itemRelatorioGeral);
    // ...
    
    return menuRelatorios;
}
```

### Passo 2: Implementar Método de Abertura

```java
private void abrirDashboard() {
    try {
        // Buscar inventário ativo (se houver)
        Integer idInventarioAtivo = buscarInventarioAtivo();
        
        // Criar e exibir dashboard
        DashboardFrame dashboard = new DashboardFrame(usuarioLogado, idInventarioAtivo);
        dashboard.setVisible(true);
        
    } catch (Exception e) {
        System.err.println("Erro ao abrir dashboard: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Erro ao abrir dashboard: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}

private Integer buscarInventarioAtivo() {
    try {
        // Buscar inventário em andamento
        InventarioDAO inventarioDAO = new InventarioDAO();
        List<Inventario> inventarios = inventarioDAO.buscarInventariosEmAndamento();
        
        if (!inventarios.isEmpty()) {
            return inventarios.get(0).getId();
        }
    } catch (Exception e) {
        System.err.println("Erro ao buscar inventário ativo: " + e.getMessage());
    }
    
    return null; // Sem inventário ativo
}
```

### Passo 3: Adicionar Botão na Toolbar (Opcional)

```java
private void criarToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    // Botão Dashboard
    JButton btnDashboard = new JButton("📊 Dashboard");
    btnDashboard.setFont(new Font("Arial", Font.PLAIN, 12));
    btnDashboard.setToolTipText("Abrir dashboard estatístico");
    btnDashboard.addActionListener(e -> abrirDashboard());
    
    toolbar.add(btnDashboard);
    toolbar.addSeparator();
    
    // Outros botões...
    
    add(toolbar, BorderLayout.NORTH);
}
```

---

## Integração com Tela de Inventário

### Adicionar Botão no InventarioFrame

```java
// No InventarioFrame.java
private void criarBotoes() {
    JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    
    JButton btnDashboard = new JButton("📊 Ver Estatísticas");
    btnDashboard.addActionListener(e -> abrirDashboardInventario());
    
    painelBotoes.add(btnDashboard);
    // Outros botões...
    
    return painelBotoes;
}

private void abrirDashboardInventario() {
    // Pegar inventário selecionado na tabela
    int selectedRow = tabelaInventarios.getSelectedRow();
    
    if (selectedRow >= 0) {
        Integer idInventario = (Integer) tabelaInventarios.getValueAt(selectedRow, 0);
        
        DashboardFrame dashboard = new DashboardFrame(usuarioLogado, idInventario);
        dashboard.setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this,
            "Selecione um inventário para ver as estatísticas.",
            "Aviso",
            JOptionPane.WARNING_MESSAGE);
    }
}
```

---

## Integração com Tela de Coleta

### Mostrar Progresso em Tempo Real

```java
// No ColetaFrame_v2.java
private void atualizarEstatisticas() {
    if (inventarioAtual != null) {
        ChartDataDAO chartDAO = new ChartDataDAO();
        Map<String, Object> stats = chartDAO.buscarEstatisticasGerais(inventarioAtual.getId());
        
        int total = (int) stats.get("totalPatrimonios");
        int coletados = (int) stats.get("totalColetados");
        double percentual = (double) stats.get("percentualConcluido");
        
        labelEstatisticas.setText(String.format(
            "Progresso: %d/%d (%.1f%%)",
            coletados, total, percentual
        ));
    }
}

// Chamar após cada coleta
private void registrarColeta() {
    // ... código de registro ...
    
    // Atualizar estatísticas
    atualizarEstatisticas();
}
```

### Adicionar Mini-Gráfico de Progresso

```java
private JPanel criarPainelProgresso() {
    JPanel painel = new JPanel(new BorderLayout());
    painel.setBorder(BorderFactory.createTitledBorder("Progresso da Coleta"));
    
    // Criar gráfico pequeno
    InventarioChartService chartService = new InventarioChartService();
    JFreeChart chart = chartService.createProgressoColetaChart(inventarioAtual.getId());
    
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(300, 200));
    chartPanel.setMouseWheelEnabled(false);
    
    painel.add(chartPanel, BorderLayout.CENTER);
    
    return painel;
}
```

---

## Atalhos de Teclado

### Adicionar Atalho para Dashboard

```java
// No MainFrame.java
private void configurarAtalhos() {
    // Ctrl+D para Dashboard
    KeyStroke ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlD, "abrirDashboard");
    getRootPane().getActionMap().put("abrirDashboard", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            abrirDashboard();
        }
    });
}
```

---

## Exemplo Completo de Integração

```java
public class MainFrame extends JFrame {
    
    private Usuario usuarioLogado;
    private DashboardFrame dashboardAberto;
    
    public MainFrame(Usuario usuario) {
        this.usuarioLogado = usuario;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Sistema de Inventário - IFMT");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Criar menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(criarMenuArquivo());
        menuBar.add(criarMenuInventario());
        menuBar.add(criarMenuRelatorios()); // ← Adicionar aqui
        menuBar.add(criarMenuAjuda());
        setJMenuBar(menuBar);
        
        // Configurar atalhos
        configurarAtalhos();
        
        setLocationRelativeTo(null);
    }
    
    private JMenu criarMenuRelatorios() {
        JMenu menu = new JMenu("Relatórios");
        
        // Dashboard
        JMenuItem itemDashboard = new JMenuItem("📊 Dashboard Estatístico");
        itemDashboard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        itemDashboard.addActionListener(e -> abrirDashboard());
        
        menu.add(itemDashboard);
        menu.addSeparator();
        
        // Outros relatórios...
        
        return menu;
    }
    
    private void abrirDashboard() {
        // Se já existe um dashboard aberto, trazer para frente
        if (dashboardAberto != null && dashboardAberto.isVisible()) {
            dashboardAberto.toFront();
            dashboardAberto.requestFocus();
            return;
        }
        
        try {
            Integer idInventario = buscarInventarioAtivo();
            dashboardAberto = new DashboardFrame(usuarioLogado, idInventario);
            dashboardAberto.setVisible(true);
            
            // Limpar referência quando fechar
            dashboardAberto.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    dashboardAberto = null;
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao abrir dashboard: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erro ao abrir dashboard: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private Integer buscarInventarioAtivo() {
        try {
            InventarioDAO dao = new InventarioDAO();
            List<Inventario> inventarios = dao.buscarInventariosEmAndamento();
            return inventarios.isEmpty() ? null : inventarios.get(0).getId();
        } catch (Exception e) {
            System.err.println("Erro ao buscar inventário: " + e.getMessage());
            return null;
        }
    }
    
    private void configurarAtalhos() {
        KeyStroke ctrlD = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlD, "dashboard");
        getRootPane().getActionMap().put("dashboard", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirDashboard();
            }
        });
    }
}
```

---

## Checklist de Integração

- [ ] Adicionar item de menu "Dashboard"
- [ ] Implementar método `abrirDashboard()`
- [ ] Implementar método `buscarInventarioAtivo()`
- [ ] Adicionar atalho Ctrl+D
- [ ] Testar abertura do dashboard
- [ ] Testar com inventário ativo
- [ ] Testar sem inventário ativo
- [ ] Adicionar botão na toolbar (opcional)
- [ ] Integrar com InventarioFrame
- [ ] Integrar com ColetaFrame

---

**Versão**: 1.0.0  
**Data**: 11/11/2025
