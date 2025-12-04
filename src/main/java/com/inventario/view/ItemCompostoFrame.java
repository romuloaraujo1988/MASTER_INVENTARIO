package com.inventario.view;

import com.inventario.model.Patrimonio;
import com.inventario.model.Usuario;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.ItemCompostoDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame para gestão de itens compostos
 * Permite marcar patrimônios como compostos e gerenciar seus componentes
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class ItemCompostoFrame extends JFrame {
    
    private Usuario usuarioLogado;
    private PatrimonioDAO patrimonioDAO;
    private ItemCompostoDAO itemCompostoDAO;
    
    // Componentes de busca
    private JTextField txtNumeroPatrimonio;
    private JButton btnBuscar;
    private JButton btnLimpar;
    
    // Dados do patrimônio
    private JLabel lblPatrimonioId;
    private JLabel lblDescricao;
    private JLabel lblSala;
    private JLabel lblResponsavel;
    private JLabel lblValor;
    private JCheckBox chkItemComposto;
    private JCheckBox chkDeteccaoAutomatica;
    
    // Tabela de componentes
    private JTable tblComponentes;
    private DefaultTableModel modelComponentes;
    private JButton btnAdicionarComponente;
    private JButton btnEditarComponente;
    private JButton btnRemoverComponente;
    private JButton btnDetectarPadroes;
    
    // Botões de ação
    private JButton btnSalvar;
    private JButton btnCancelar;
    
    // Dados
    private Patrimonio patrimonioAtual;
    private List<ComponenteItem> componentes;
    
    public ItemCompostoFrame(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        this.patrimonioDAO = new PatrimonioDAO();
        this.itemCompostoDAO = new ItemCompostoDAO();
        this.componentes = new ArrayList<>();
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        setTitle("Gestão de Itens Compostos");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Definir ícone
        setIconImages(com.inventario.util.IconManager.getAppIconImages());
    }
    
    private void initializeComponents() {
        // Painel de busca
        txtNumeroPatrimonio = new JTextField(20);
        txtNumeroPatrimonio.setFont(new Font("Arial", Font.PLAIN, 14));
        
        btnBuscar = createModernButton("Buscar", new Color(52, 152, 219));
        btnLimpar = createModernButton("Limpar", new Color(149, 165, 166));
        
        // Labels de dados do patrimônio
        lblPatrimonioId = createDataLabel("");
        lblDescricao = createDataLabel("");
        lblSala = createDataLabel("");
        lblResponsavel = createDataLabel("");
        lblValor = createDataLabel("");
        
        // Checkboxes
        chkItemComposto = new JCheckBox("Marcar como Item Composto");
        chkItemComposto.setFont(new Font("Arial", Font.BOLD, 13));
        chkItemComposto.setEnabled(false);
        
        chkDeteccaoAutomatica = new JCheckBox("Detecção Automática Ativada");
        chkDeteccaoAutomatica.setFont(new Font("Arial", Font.PLAIN, 12));
        chkDeteccaoAutomatica.setEnabled(false);
        
        // Tabela de componentes
        String[] colunas = {"Tipo", "Descrição", "Qtd. Esperada", "Ordem"};
        modelComponentes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblComponentes = new JTable(modelComponentes);
        tblComponentes.setFont(new Font("Arial", Font.PLAIN, 12));
        tblComponentes.setRowHeight(25);
        tblComponentes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tblComponentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Botões de componentes
        btnAdicionarComponente = createModernButton("➕ Adicionar", new Color(46, 204, 113));
        btnEditarComponente = createModernButton("✏️ Editar", new Color(241, 196, 15));
        btnRemoverComponente = createModernButton("🗑️ Remover", new Color(231, 76, 60));
        btnDetectarPadroes = createModernButton("🔍 Detectar Padrões", new Color(155, 89, 182));
        
        btnAdicionarComponente.setEnabled(false);
        btnEditarComponente.setEnabled(false);
        btnRemoverComponente.setEnabled(false);
        btnDetectarPadroes.setEnabled(false);
        
        // Botões de ação
        btnSalvar = createModernButton("💾 Salvar", new Color(39, 174, 96));
        btnCancelar = createModernButton("❌ Cancelar", new Color(192, 57, 43));
        
        btnSalvar.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel principal com margem
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Painel superior - Busca
        JPanel panelBusca = createPanelBusca();
        
        // Painel central - Dados e Componentes
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setOpaque(false);
        
        JPanel panelDados = createPanelDados();
        JPanel panelComponentes = createPanelComponentes();
        
        panelCentral.add(panelDados, BorderLayout.NORTH);
        panelCentral.add(panelComponentes, BorderLayout.CENTER);
        
        // Painel inferior - Botões de ação
        JPanel panelAcoes = createPanelAcoes();
        
        mainPanel.add(panelBusca, BorderLayout.NORTH);
        mainPanel.add(panelCentral, BorderLayout.CENTER);
        mainPanel.add(panelAcoes, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createPanelBusca() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTitulo = new JLabel("🔍 Buscar Patrimônio:");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        
        panel.add(lblTitulo);
        panel.add(txtNumeroPatrimonio);
        panel.add(btnBuscar);
        panel.add(btnLimpar);
        
        return panel;
    }
    
    private JPanel createPanelDados() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Dados do Patrimônio",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(52, 152, 219)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Linha 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(createLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(lblPatrimonioId, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("Valor:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(lblValor, gbc);
        
        // Linha 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(createLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        panel.add(lblDescricao, gbc);
        gbc.gridwidth = 1;
        
        // Linha 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(createLabel("Sala:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(lblSala, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(createLabel("Responsável:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        panel.add(lblResponsavel, gbc);
        
        // Linha 4 - Checkboxes
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(chkItemComposto, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        panel.add(chkDeteccaoAutomatica, gbc);
        
        return panel;
    }
    
    private JPanel createPanelComponentes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            "Componentes do Item",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 13),
            new Color(46, 204, 113)
        ));
        
        // Tabela com scroll
        JScrollPane scrollPane = new JScrollPane(tblComponentes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotoes.setBackground(Color.WHITE);
        panelBotoes.add(btnAdicionarComponente);
        panelBotoes.add(btnEditarComponente);
        panelBotoes.add(btnRemoverComponente);
        panelBotoes.add(Box.createHorizontalStrut(20));
        panelBotoes.add(btnDetectarPadroes);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPanelAcoes() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        panel.add(btnSalvar);
        panel.add(btnCancelar);
        
        return panel;
    }
    
    private void setupEventListeners() {
        // Buscar patrimônio
        btnBuscar.addActionListener(e -> buscarPatrimonio());
        txtNumeroPatrimonio.addActionListener(e -> buscarPatrimonio());
        
        // Limpar formulário
        btnLimpar.addActionListener(e -> limparFormulario());
        
        // Checkbox item composto
        chkItemComposto.addActionListener(e -> {
            boolean isComposto = chkItemComposto.isSelected();
            habilitarComponentes(isComposto);
        });
        
        // Gerenciar componentes
        btnAdicionarComponente.addActionListener(e -> adicionarComponente());
        btnEditarComponente.addActionListener(e -> editarComponente());
        btnRemoverComponente.addActionListener(e -> removerComponente());
        btnDetectarPadroes.addActionListener(e -> detectarPadroes());
        
        // Seleção na tabela
        tblComponentes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = tblComponentes.getSelectedRow() != -1;
                btnEditarComponente.setEnabled(hasSelection && chkItemComposto.isSelected());
                btnRemoverComponente.setEnabled(hasSelection && chkItemComposto.isSelected());
            }
        });
        
        // Salvar e cancelar
        btnSalvar.addActionListener(e -> salvarItemComposto());
        btnCancelar.addActionListener(e -> dispose());
        
        // Fechar janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (formularioAlterado()) {
                    int opcao = JOptionPane.showConfirmDialog(
                        ItemCompostoFrame.this,
                        "Existem alterações não salvas. Deseja realmente sair?",
                        "Confirmar Saída",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                dispose();
            }
        });
    }
    
    private void buscarPatrimonio() {
        String numero = txtNumeroPatrimonio.getText().trim();
        
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Digite o número do patrimônio.",
                "Campo Obrigatório",
                JOptionPane.WARNING_MESSAGE);
            txtNumeroPatrimonio.requestFocus();
            return;
        }
        
        try {
            // Buscar patrimônio no banco
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
            
            if (patrimonio == null) {
                JOptionPane.showMessageDialog(this,
                    "Patrimônio não encontrado: " + numero,
                    "Não Encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Carregar dados do patrimônio
            carregarPatrimonio(patrimonio);
            
            // TODO: Verificar se já é item composto e carregar componentes
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao buscar patrimônio: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void carregarPatrimonio(Patrimonio patrimonio) {
        this.patrimonioAtual = patrimonio;
        
        // Preencher labels
        lblPatrimonioId.setText(String.valueOf(patrimonio.getId()));
        lblDescricao.setText(patrimonio.getDescricao());
        lblSala.setText(patrimonio.getNomeSala() != null ? patrimonio.getNomeSala() : "Sem sala");
        lblResponsavel.setText(patrimonio.getNomeResponsavel() != null ? 
            patrimonio.getNomeResponsavel() : "Sem responsável");
        lblValor.setText(String.format("R$ %.2f", patrimonio.getValor() != null ? patrimonio.getValor() : BigDecimal.ZERO));
        
        // Habilitar controles
        chkItemComposto.setEnabled(true);
        chkDeteccaoAutomatica.setEnabled(true);
        btnSalvar.setEnabled(true);
        
        // Limpar componentes
        componentes.clear();
        modelComponentes.setRowCount(0);
        
        // Verificar se já é item composto e carregar componentes
        try {
            if (itemCompostoDAO.isItemComposto(patrimonio.getId())) {
                chkItemComposto.setSelected(true);
                habilitarComponentes(true);
                
                // Carregar componentes existentes
                java.util.List<java.util.Map<String, Object>> comps = itemCompostoDAO.buscarComponentes(patrimonio.getId());
                for (java.util.Map<String, Object> comp : comps) {
                    ComponenteItem componente = new ComponenteItem(
                        (String) comp.get("tipo"),
                        (String) comp.get("descricao"),
                        (Integer) comp.get("quantidadeEsperada"),
                        componentes.size() + 1
                    );
                    componentes.add(componente);
                    
                    modelComponentes.addRow(new Object[]{
                        componente.getTipo(),
                        componente.getDescricao(),
                        componente.getQuantidadeEsperada(),
                        componente.getOrdem()
                    });
                }
                
                JOptionPane.showMessageDialog(this,
                    "Este patrimônio já é um item composto com " + comps.size() + " componente(s).",
                    "Item Composto Existente",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar item composto: " + e.getMessage());
        }
    }
    
    private void habilitarComponentes(boolean habilitar) {
        btnAdicionarComponente.setEnabled(habilitar);
        btnDetectarPadroes.setEnabled(habilitar);
        
        if (!habilitar) {
            btnEditarComponente.setEnabled(false);
            btnRemoverComponente.setEnabled(false);
        }
    }
    
    private void adicionarComponente() {
        ComponenteDialog dialog = new ComponenteDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            ComponenteItem componente = dialog.getComponente();
            componentes.add(componente);
            
            // Adicionar na tabela
            modelComponentes.addRow(new Object[]{
                componente.getTipo(),
                componente.getDescricao(),
                componente.getQuantidadeEsperada(),
                componente.getOrdem()
            });
        }
    }
    
    private void editarComponente() {
        int selectedRow = tblComponentes.getSelectedRow();
        if (selectedRow == -1) return;
        
        ComponenteItem componente = componentes.get(selectedRow);
        ComponenteDialog dialog = new ComponenteDialog(this, componente);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            ComponenteItem componenteEditado = dialog.getComponente();
            componentes.set(selectedRow, componenteEditado);
            
            // Atualizar tabela
            modelComponentes.setValueAt(componenteEditado.getTipo(), selectedRow, 0);
            modelComponentes.setValueAt(componenteEditado.getDescricao(), selectedRow, 1);
            modelComponentes.setValueAt(componenteEditado.getQuantidadeEsperada(), selectedRow, 2);
            modelComponentes.setValueAt(componenteEditado.getOrdem(), selectedRow, 3);
        }
    }
    
    private void removerComponente() {
        int selectedRow = tblComponentes.getSelectedRow();
        if (selectedRow == -1) return;
        
        int opcao = JOptionPane.showConfirmDialog(this,
            "Deseja realmente remover este componente?",
            "Confirmar Remoção",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (opcao == JOptionPane.YES_OPTION) {
            componentes.remove(selectedRow);
            modelComponentes.removeRow(selectedRow);
        }
    }
    
    private void detectarPadroes() {
        if (patrimonioAtual == null) return;
        
        // TODO: Implementar detecção automática de padrões
        JOptionPane.showMessageDialog(this,
            "Funcionalidade de detecção automática em desenvolvimento.\n" +
            "Em breve será possível detectar componentes automaticamente\n" +
            "baseado na descrição do patrimônio.",
            "Em Desenvolvimento",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void salvarItemComposto() {
        if (patrimonioAtual == null) {
            JOptionPane.showMessageDialog(this,
                "Nenhum patrimônio selecionado.",
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!chkItemComposto.isSelected()) {
            JOptionPane.showMessageDialog(this,
                "Marque a opção 'Item Composto' para salvar.",
                "Validação",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (componentes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Adicione pelo menos um componente.",
                "Validação",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            boolean jaEraItemComposto = itemCompostoDAO.isItemComposto(patrimonioAtual.getId());
            java.util.Map<String, Object> infoColetas = null;
            int componentesAntigos = 0;
            
            // Verificar se já existe e se há coletas no inventário ativo
            if (jaEraItemComposto) {
                // Verificar coletas existentes
                infoColetas = itemCompostoDAO.verificarColetasExistentes(patrimonioAtual.getId(), null);
                boolean temColetas = (Boolean) infoColetas.get("temColetas");
                componentesAntigos = (Integer) infoColetas.get("totalComponentes");
                int componentesColetados = (Integer) infoColetas.get("componentesColetados");
                String inventarioNome = (String) infoColetas.get("inventarioNome");
                
                // Verificar se está adicionando novos componentes (mais do que tinha antes)
                boolean adicionandoNovos = componentes.size() > componentesAntigos;
                int novosComponentes = componentes.size() - componentesAntigos;
                
                if (temColetas && adicionandoNovos) {
                    // Aviso especial: há coletas e está adicionando novos componentes
                    StringBuilder mensagem = new StringBuilder();
                    mensagem.append("⚠️ ATENÇÃO: Este item composto já possui coletas registradas!\n\n");
                    mensagem.append("📋 Inventário: ").append(inventarioNome).append("\n");
                    mensagem.append("📊 Componentes coletados: ").append(componentesColetados)
                            .append("/").append(componentesAntigos).append("\n\n");
                    mensagem.append("Você está adicionando ").append(novosComponentes)
                            .append(" novo(s) componente(s).\n\n");
                    mensagem.append("⚡ Os novos componentes precisarão ser coletados\n");
                    mensagem.append("   na tela 'Coleta de Itens Compostos' para que\n");
                    mensagem.append("   o status do item fique COMPLETO.\n\n");
                    mensagem.append("Deseja continuar?");
                    
                    int opcao = JOptionPane.showConfirmDialog(this,
                        mensagem.toString(),
                        "Aviso - Novos Componentes Pendentes de Coleta",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else if (temColetas) {
                    // Aviso: há coletas mas não está adicionando novos
                    int opcao = JOptionPane.showConfirmDialog(this,
                        "Este patrimônio já possui componentes cadastrados e coletas registradas.\n" +
                        "Inventário: " + inventarioNome + "\n" +
                        "Componentes coletados: " + componentesColetados + "/" + componentesAntigos + "\n\n" +
                        "Deseja substituir os componentes?\n" +
                        "⚠️ As coletas existentes serão mantidas para componentes equivalentes.",
                        "Confirmar Substituição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                } else {
                    // Sem coletas, apenas confirmar substituição
                    int opcao = JOptionPane.showConfirmDialog(this,
                        "Este patrimônio já possui componentes cadastrados.\n" +
                        "Deseja substituir pelos novos componentes?",
                        "Confirmar Substituição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    
                    if (opcao != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                // Remover componentes antigos
                java.util.List<java.util.Map<String, Object>> compsAntigos = 
                    itemCompostoDAO.buscarComponentes(patrimonioAtual.getId());
                for (java.util.Map<String, Object> comp : compsAntigos) {
                    itemCompostoDAO.removerComponente((Integer) comp.get("id"));
                }
            }
            
            // Salvar novos componentes no banco
            for (ComponenteItem comp : componentes) {
                itemCompostoDAO.adicionarComponente(
                    patrimonioAtual.getId(),
                    comp.getTipo(),
                    comp.getDescricao(),
                    comp.getQuantidadeEsperada(),
                    true, // obrigatório
                    null  // observação
                );
            }
            
            // Mensagem de sucesso com aviso sobre coleta se necessário
            StringBuilder msgSucesso = new StringBuilder();
            msgSucesso.append("Item composto salvo com sucesso!\n");
            msgSucesso.append("Patrimônio: ").append(patrimonioAtual.getNumero()).append("\n");
            msgSucesso.append("Componentes: ").append(componentes.size());
            
            if (jaEraItemComposto && infoColetas != null && (Boolean) infoColetas.get("temColetas")) {
                int novos = componentes.size() - componentesAntigos;
                if (novos > 0) {
                    msgSucesso.append("\n\n💡 Lembre-se: ").append(novos)
                              .append(" novo(s) componente(s) precisam ser coletados!");
                }
            }
            
            JOptionPane.showMessageDialog(this,
                msgSucesso.toString(),
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE);
            
            limparFormulario();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar item composto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void limparFormulario() {
        patrimonioAtual = null;
        txtNumeroPatrimonio.setText("");
        lblPatrimonioId.setText("");
        lblDescricao.setText("");
        lblSala.setText("");
        lblResponsavel.setText("");
        lblValor.setText("");
        chkItemComposto.setSelected(false);
        chkDeteccaoAutomatica.setSelected(false);
        chkItemComposto.setEnabled(false);
        chkDeteccaoAutomatica.setEnabled(false);
        componentes.clear();
        modelComponentes.setRowCount(0);
        habilitarComponentes(false);
        btnSalvar.setEnabled(false);
        txtNumeroPatrimonio.requestFocus();
    }
    
    private boolean formularioAlterado() {
        return patrimonioAtual != null && 
               (chkItemComposto.isSelected() || !componentes.isEmpty());
    }
    
    // Métodos auxiliares para criar componentes
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }
    
    private JLabel createDataLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        
        // Efeito hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    // Classe interna para representar um componente
    public static class ComponenteItem {
        private String tipo;
        private String descricao;
        private int quantidadeEsperada;
        private int ordem;
        
        public ComponenteItem(String tipo, String descricao, int quantidadeEsperada, int ordem) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.quantidadeEsperada = quantidadeEsperada;
            this.ordem = ordem;
        }
        
        // Getters e Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public int getQuantidadeEsperada() { return quantidadeEsperada; }
        public void setQuantidadeEsperada(int quantidadeEsperada) { 
            this.quantidadeEsperada = quantidadeEsperada; 
        }
        
        public int getOrdem() { return ordem; }
        public void setOrdem(int ordem) { this.ordem = ordem; }
    }
}
