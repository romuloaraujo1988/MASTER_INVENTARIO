# Integração SIADS ao Menu Principal

## Como Adicionar ao Sistema

### Opção 1: Menu Relatórios

Adicione a exportação SIADS ao menu de relatórios existente:

```java
// No arquivo principal do sistema (ex: SistemaInventarioApplication.java)

import com.inventario.siads.view.SiadsExportDialog;

// Dentro do método que cria os menus:
private void criarMenus() {
    JMenuBar menuBar = new JMenuBar();
    
    // ... outros menus ...
    
    // Menu Relatórios
    JMenu menuRelatorios = new JMenu("Relatórios");
    menuRelatorios.setMnemonic(KeyEvent.VK_R);
    
    // ... outros itens de menu ...
    
    // Item SIADS
    JMenuItem menuItemSiads = new JMenuItem("Exportar SIADS");
    menuItemSiads.setMnemonic(KeyEvent.VK_S);
    menuItemSiads.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
    menuItemSiads.addActionListener(e -> abrirExportacaoSiads());
    
    menuRelatorios.add(menuItemSiads);
    menuBar.add(menuRelatorios);
    
    setJMenuBar(menuBar);
}

private void abrirExportacaoSiads() {
    SiadsExportDialog dialog = new SiadsExportDialog(this);
    dialog.setVisible(true);
}
```

### Opção 2: Menu Ferramentas

Se preferir, crie um menu específico para ferramentas:

```java
// Menu Ferramentas
JMenu menuFerramentas = new JMenu("Ferramentas");
menuFerramentas.setMnemonic(KeyEvent.VK_F);

JMenuItem menuItemSiads = new JMenuItem("Exportação SIADS");
menuItemSiads.addActionListener(e -> {
    SiadsExportDialog dialog = new SiadsExportDialog(this);
    dialog.setVisible(true);
});

menuFerramentas.add(menuItemSiads);
menuBar.add(menuFerramentas);
```

### Opção 3: Botão na Toolbar

Adicione um botão de acesso rápido na barra de ferramentas:

```java
private void criarToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    
    // ... outros botões ...
    
    // Botão SIADS
    JButton btnSiads = new JButton("SIADS");
    btnSiads.setToolTipText("Exportar dados para SIADS");
    // btnSiads.setIcon(new ImageIcon(getClass().getResource("/icons/siads.png")));
    btnSiads.addActionListener(e -> {
        SiadsExportDialog dialog = new SiadsExportDialog(this);
        dialog.setVisible(true);
    });
    
    toolbar.add(btnSiads);
    
    add(toolbar, BorderLayout.NORTH);
}
```

## Exemplo Completo

Aqui está um exemplo completo de integração:

```java
package com.inventario;

import com.inventario.siads.view.SiadsExportDialog;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SistemaInventarioApplication extends JFrame {
    
    public SistemaInventarioApplication() {
        setTitle("Sistema de Inventário");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        criarMenus();
        
        setVisible(true);
    }
    
    private void criarMenus() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setMnemonic(KeyEvent.VK_A);
        // ... itens do menu arquivo ...
        menuBar.add(menuArquivo);
        
        // Menu Cadastros
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.setMnemonic(KeyEvent.VK_C);
        // ... itens do menu cadastros ...
        menuBar.add(menuCadastros);
        
        // Menu Inventário
        JMenu menuInventario = new JMenu("Inventário");
        menuInventario.setMnemonic(KeyEvent.VK_I);
        // ... itens do menu inventário ...
        menuBar.add(menuInventario);
        
        // Menu Relatórios
        JMenu menuRelatorios = new JMenu("Relatórios");
        menuRelatorios.setMnemonic(KeyEvent.VK_R);
        
        JMenuItem menuItemRelatorioGeral = new JMenuItem("Relatório Geral");
        menuRelatorios.add(menuItemRelatorioGeral);
        
        menuRelatorios.addSeparator();
        
        // SIADS
        JMenuItem menuItemSiads = new JMenuItem("Exportar SIADS");
        menuItemSiads.setMnemonic(KeyEvent.VK_S);
        menuItemSiads.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        menuItemSiads.addActionListener(e -> abrirExportacaoSiads());
        menuRelatorios.add(menuItemSiads);
        
        menuBar.add(menuRelatorios);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("Ajuda");
        menuAjuda.setMnemonic(KeyEvent.VK_J);
        // ... itens do menu ajuda ...
        menuBar.add(menuAjuda);
        
        setJMenuBar(menuBar);
    }
    
    private void abrirExportacaoSiads() {
        try {
            SiadsExportDialog dialog = new SiadsExportDialog(this);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao abrir exportação SIADS:\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SistemaInventarioApplication();
        });
    }
}
```

## Atalhos de Teclado Sugeridos

- **Ctrl+Shift+S**: Abrir exportação SIADS
- **F12**: Acesso rápido (alternativa)

## Ícone Sugerido

Se desejar adicionar um ícone ao menu, coloque um arquivo PNG em:
```
src/main/resources/icons/siads.png
```

Tamanho recomendado: 16x16 ou 24x24 pixels

## Verificação

Após integrar, verifique:

1. ✅ Menu aparece corretamente
2. ✅ Atalho de teclado funciona
3. ✅ Dialog abre sem erros
4. ✅ Botão de configurações está acessível
5. ✅ Exportação funciona corretamente

## Troubleshooting

### Erro ao abrir dialog
- Verifique se todas as classes SIADS estão no classpath
- Confirme que o package `com.inventario.siads` está compilado

### Configurações não salvam
- Verifique permissões de escrita no diretório
- Confirme que o arquivo `siads.properties` pode ser criado

### Exportação falha
- Execute validação de dados primeiro
- Verifique logs do sistema
- Confirme configurações SIADS
