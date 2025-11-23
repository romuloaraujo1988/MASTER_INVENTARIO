package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.inventario.service.PatrimonioService;
import com.inventario.model.Patrimonio;
import com.inventario.repository.impl.PatrimonioRepositoryImpl;
import com.inventario.dao.PatrimonioDAO;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Tela principal para gerenciamento de patrimônios
 * Permite visualizar, adicionar, editar e excluir patrimônios
 */
public class PatrimonioFrame extends JFrame {
    private JTable tabelaPatrimonio;
    private DefaultTableModel modeloTabela;
    private final PatrimonioService patrimonioService;
    private JTextField campoBusca;
    private JComboBox<String> comboTipoBusca;
    private JButton btnNovo, btnEditar, btnExcluir, btnBuscar, btnImportar, btnCarregarTodos;
    
    // Variáveis para manter o estado da última busca
    private String ultimoTermoBusca = "";
    private String ultimoTipoBusca = "";
    private boolean ultimaBuscaFoiCarregarTodos = false;
    
    public PatrimonioFrame() {
        // Instantiate service directly (no Spring context in Swing app)
        // Create the full dependency chain: DAO -> Repository -> Service
        PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
        PatrimonioRepositoryImpl patrimonioRepository = new PatrimonioRepositoryImpl(patrimonioDAO);
        this.patrimonioService = new PatrimonioService(patrimonioRepository);
        initComponents();
    }
    
    public PatrimonioFrame(PatrimonioService patrimonioService) {
        this.patrimonioService = patrimonioService;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Gerenciamento de Patrimônios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Definir ícone da janela
        setIconImages(com.inventario.util.IconManager.getAppIconImages());
        
        // Painel superior principal com layout vertical
        JPanel painelSuperior = new JPanel();
        painelSuperior.setLayout(new BoxLayout(painelSuperior, BoxLayout.Y_AXIS));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelSuperior.setBackground(new Color(236, 240, 241));
        
        // Painel de busca modernizado
        JPanel painelBusca = new JPanel();
        painelBusca.setLayout(new GridBagLayout());
        painelBusca.setBackground(Color.WHITE);
        painelBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "🔍 Busca de Patrimônios",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI Symbol", Font.BOLD, 13),
                new Color(52, 152, 219)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Linha 1: Label e ComboBox
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblBuscarPor = new JLabel("Buscar por:");
        lblBuscarPor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelBusca.add(lblBuscarPor, gbc);
        
        gbc.gridx = 1;
        comboTipoBusca = new JComboBox<>(new String[]{"Número", "Descrição", "Responsável", "Sala", "Todos os campos"});
        comboTipoBusca.setPreferredSize(new Dimension(150, 32));
        comboTipoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        painelBusca.add(comboTipoBusca, gbc);
        
        // Linha 1: Campo de busca
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoBusca = new JTextField();
        campoBusca.setPreferredSize(new Dimension(250, 32));
        campoBusca.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoBusca.setToolTipText("Digite o termo para buscar");
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        painelBusca.add(campoBusca, gbc);
        
        // Linha 1: Botões de busca
        gbc.gridx = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        btnBuscar = createModernButton("🔍 Buscar", new Color(52, 152, 219));
        btnBuscar.setPreferredSize(new Dimension(120, 32));
        painelBusca.add(btnBuscar, gbc);
        
        gbc.gridx = 4;
        btnCarregarTodos = createModernButton("📋 Carregar Todos", new Color(46, 204, 113));
        btnCarregarTodos.setPreferredSize(new Dimension(160, 32));
        painelBusca.add(btnCarregarTodos, gbc);
        
        // Painel de ações modernizado
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelAcoes.setBackground(Color.WHITE);
        painelAcoes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "⚙️ Ações",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI Symbol", Font.BOLD, 13),
                new Color(155, 89, 182)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Botões de ação com emoticons e cores
        btnNovo = createModernButton("➕ Novo Patrimônio", new Color(46, 204, 113));
        btnNovo.setPreferredSize(new Dimension(180, 38));
        
        btnEditar = createModernButton("✏️ Editar", new Color(241, 196, 15));
        btnEditar.setPreferredSize(new Dimension(120, 38));
        
        btnExcluir = createModernButton("🗑️ Excluir", new Color(231, 76, 60));
        btnExcluir.setPreferredSize(new Dimension(120, 38));
        
        btnImportar = createModernButton("📥 Importar CSV", new Color(52, 152, 219));
        btnImportar.setPreferredSize(new Dimension(150, 38));
        
        painelAcoes.add(btnNovo);
        painelAcoes.add(btnEditar);
        painelAcoes.add(btnExcluir);
        painelAcoes.add(btnImportar);
        
        // Adicionar painéis ao painel superior
        painelSuperior.add(painelBusca);
        painelSuperior.add(Box.createVerticalStrut(10));
        painelSuperior.add(painelAcoes);
        
        add(painelSuperior, BorderLayout.NORTH);
        
        // Tabela central modernizada
        String[] colunas = {"ID", "Número", "Descrição", "Marca", "Modelo", "Estado", "Situação", "Sala", "Responsável"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaPatrimonio = new JTable(modeloTabela);
        tabelaPatrimonio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPatrimonio.setRowHeight(28);
        tabelaPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaPatrimonio.setGridColor(new Color(224, 224, 224));
        tabelaPatrimonio.setSelectionBackground(new Color(52, 152, 219));
        tabelaPatrimonio.setSelectionForeground(Color.WHITE);
        tabelaPatrimonio.setShowGrid(true);
        tabelaPatrimonio.setIntercellSpacing(new Dimension(1, 1));
        
        // Cabeçalho da tabela com estilo moderno
        tabelaPatrimonio.getTableHeader().setBackground(new Color(44, 62, 80));
        tabelaPatrimonio.getTableHeader().setForeground(Color.WHITE);
        tabelaPatrimonio.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaPatrimonio.getTableHeader().setPreferredSize(new Dimension(0, 35));
        tabelaPatrimonio.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(52, 152, 219)));
        
        // Renderizador customizado para linhas alternadas
        tabelaPatrimonio.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(245, 245, 245));
                    }
                    c.setForeground(new Color(44, 62, 80));
                } else {
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                }
                
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tabelaPatrimonio);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 10, 10, 10),
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel de status modernizado
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        painelStatus.setBackground(new Color(44, 62, 80));
        painelStatus.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(52, 152, 219)));
        
        JLabel lblDica = new JLabel("💡 Dica: Clique duas vezes em um item para editar");
        lblDica.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 11));
        lblDica.setForeground(new Color(236, 240, 241));
        painelStatus.add(lblDica);
        
        add(painelStatus, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
    }
    
    /**
     * Cria um botão moderno com emoticon, gradiente e efeito hover
     */
    private JButton createModernButton(String texto, Color corBase) {
        JButton button = new JButton() {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Cor de fundo com gradiente
                Color topColor = isHovered ? corBase.brighter() : corBase;
                Color bottomColor = isHovered ? corBase : corBase.darker();

                java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Borda sutil
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.dispose();

                // Desenhar texto com emoticon
                g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Usar fonte que suporta emoticons
                g2d.setFont(new Font("Segoe UI Symbol", Font.BOLD, 13));
                g2d.setColor(Color.WHITE);
                
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(texto);
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Sombra do texto
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(texto, textX + 1, textY + 1);
                
                // Texto principal
                g2d.setColor(Color.WHITE);
                g2d.drawString(texto, textX, textY);

                g2d.dispose();
            }
        };

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, true);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                try {
                    java.lang.reflect.Field field = button.getClass().getDeclaredField("isHovered");
                    field.setAccessible(true);
                    field.set(button, false);
                    button.repaint();
                } catch (Exception e) {
                    // Fallback silencioso
                }
            }
        });
        
        return button;
    }
    
    private void configurarEventos() {
        btnNovo.addActionListener(e -> abrirFormularioPatrimonio(null));
        btnEditar.addActionListener(e -> editarPatrimonio());
        btnExcluir.addActionListener(e -> excluirPatrimonio());
        btnBuscar.addActionListener(e -> buscarPatrimonios());
        btnImportar.addActionListener(e -> importarCSV());
        btnCarregarTodos.addActionListener(e -> carregarPatrimonios());
        
        // Permitir busca ao pressionar Enter no campo de busca
        campoBusca.addActionListener(e -> buscarPatrimonios());
        
        // Limpar tabela quando o campo estiver vazio
        campoBusca.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (campoBusca.getText().trim().isEmpty()) {
                    modeloTabela.setRowCount(0);
                }
            }
        });
        
        // Duplo clique para editar
        tabelaPatrimonio.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editarPatrimonio();
                }
            }
        });
    }
    
    private void carregarPatrimonios() {
        try {
            modeloTabela.setRowCount(0);
            List<Patrimonio> patrimonios = patrimonioService.listarTodos();
            for (Patrimonio p : patrimonios) {
                modeloTabela.addRow(new Object[]{
                    p.getId(),
                    p.getNumero(),
                    p.getDescricao(),
                    p.getMarca(),
                    p.getModelo(),
                    p.getEstadoConservacao(),
                    p.getSituacao() != null ? p.getSituacao() : "ATIVO",
                    p.getNomeSala() != null ? p.getNomeSala() : "Não definida",
                    p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "Não definido"
                });
            }
            // Armazenar estado da busca
            ultimoTermoBusca = "";
            ultimoTipoBusca = "";
            ultimaBuscaFoiCarregarTodos = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar patrimônios: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void abrirFormularioPatrimonio(Patrimonio patrimonio) {
        PatrimonioFormDialog dialog = new PatrimonioFormDialog(this, patrimonio);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            // Manter os resultados da pesquisa anterior
            recarregarDadosAtuais();
        }
    }
    
    private void editarPatrimonio() {
        int linhaSelecionada = tabelaPatrimonio.getSelectedRow();
        if (linhaSelecionada >= 0) {
            try {
                Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                Optional<Patrimonio> patrimonioOpt = patrimonioService.buscarPorId(id);
                if (patrimonioOpt.isPresent()) {
                    abrirFormularioPatrimonio(patrimonioOpt.get());
                } else {
                    JOptionPane.showMessageDialog(this, "Patrimônio não encontrado.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar patrimônio: " + e.getMessage(), 
                    "Erro", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um patrimônio para editar.");
        }
    }
    
    private void excluirPatrimonio() {
        int linhaSelecionada = tabelaPatrimonio.getSelectedRow();
        if (linhaSelecionada >= 0) {
            String numeroPatrimonio = (String) modeloTabela.getValueAt(linhaSelecionada, 1);
            int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Tem certeza que deseja excluir o patrimônio " + numeroPatrimonio + "?", 
                "Confirmar Exclusão", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirmacao == JOptionPane.YES_OPTION) {
                try {
                    Integer id = (Integer) modeloTabela.getValueAt(linhaSelecionada, 0);
                    patrimonioService.excluir(id);
                    JOptionPane.showMessageDialog(this, "Patrimônio excluído com sucesso!");
                    recarregarDadosAtuais();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Erro ao excluir patrimônio: " + e.getMessage(), 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um patrimônio para excluir.");
        }
    }
    
    private void buscarPatrimonios() {
        String termo = campoBusca.getText().trim();
        if (termo.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Digite um termo para buscar.", 
                "Busca", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            modeloTabela.setRowCount(0);
            List<Patrimonio> resultados = null;
            
            String tipoBusca = (String) comboTipoBusca.getSelectedItem();
            
            switch (tipoBusca) {
                case "Número":
                    Optional<Patrimonio> patrimonioEncontrado = patrimonioService.buscarPorNumero(termo);
                    resultados = new ArrayList<>();
                    if (patrimonioEncontrado.isPresent()) {
                        resultados.add(patrimonioEncontrado.get());
                    }
                    break;
                case "Descrição":
                    resultados = buscarPorDescricao(termo);
                    break;
                case "Responsável":
                    resultados = buscarPorResponsavel(termo);
                    break;
                case "Sala":
                    resultados = buscarPorSala(termo);
                    break;
                case "Todos os campos":
                default:
                    // For "all fields" search, search across all fields
                    resultados = patrimonioService.listarTodos().stream()
                        .filter(p -> 
                            (p.getNumero() != null && p.getNumero().toLowerCase().contains(termo.toLowerCase())) ||
                            (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(termo.toLowerCase())) ||
                            (p.getMarca() != null && p.getMarca().toLowerCase().contains(termo.toLowerCase())) ||
                            (p.getModelo() != null && p.getModelo().toLowerCase().contains(termo.toLowerCase()))
                        )
                        .collect(java.util.stream.Collectors.toList());
                    break;
            }
            
            // Armazenar estado da busca
            ultimoTermoBusca = termo;
            ultimoTipoBusca = tipoBusca;
            ultimaBuscaFoiCarregarTodos = false;
            
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Nenhum patrimônio encontrado com o termo: " + termo, 
                    "Busca", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Patrimonio p : resultados) {
                    modeloTabela.addRow(new Object[]{
                        p.getId(),
                        p.getNumero(),
                        p.getDescricao(),
                        p.getMarca(),
                        p.getModelo(),
                        p.getEstadoConservacao(),
                        p.getSituacao() != null ? p.getSituacao() : "ATIVO",
                        p.getNomeSala() != null ? p.getNomeSala() : "Não definida",
                        p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "Não definido"
                    });
                }
                JOptionPane.showMessageDialog(this, 
                    "Encontrados " + resultados.size() + " patrimônio(s).", 
                    "Busca", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao buscar patrimônios: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private List<Patrimonio> buscarPorDescricao(String descricao) throws Exception {
        // Método auxiliar para buscar apenas por descrição
        return patrimonioService.listarTodos().stream()
            .filter(p -> p.getDescricao() != null && p.getDescricao().toLowerCase().contains(descricao.toLowerCase()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Patrimonio> buscarPorResponsavel(String nomeResponsavel) throws Exception {
        // Método auxiliar para buscar por responsável
        return patrimonioService.listarTodos().stream()
            .filter(p -> p.getIdResponsavel() > 0)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Patrimonio> buscarPorSala(String nomeSala) throws Exception {
        // Método auxiliar para buscar por sala (usando nome da sala)
        return patrimonioService.listarTodos().stream()
            .filter(p -> p.getIdSala() > 0)
            .collect(java.util.stream.Collectors.toList());
    }
    
    private void importarCSV() {
        try {
            ImportacaoCSVFrame importacaoFrame = new ImportacaoCSVFrame();
            importacaoFrame.setVisible(true);
            
            // Recarregar dados após fechar a janela de importação
            importacaoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    recarregarDadosAtuais();
                }
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao abrir importação CSV: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Recarrega os dados mantendo o estado da última busca realizada
     */
    private void recarregarDadosAtuais() {
        if (ultimaBuscaFoiCarregarTodos) {
            // Se a última ação foi carregar todos, recarrega todos
            carregarPatrimonios();
        } else if (!ultimoTermoBusca.isEmpty()) {
            // Se havia uma busca ativa, reexecuta a busca
            // Temporariamente define os valores nos campos
            String termoAtual = campoBusca.getText();
            String tipoAtual = (String) comboTipoBusca.getSelectedItem();
            
            campoBusca.setText(ultimoTermoBusca);
            comboTipoBusca.setSelectedItem(ultimoTipoBusca);
            
            // Executa a busca sem mostrar mensagens
            executarBuscaSilenciosa();
            
            // Restaura os valores originais dos campos
            campoBusca.setText(termoAtual);
            comboTipoBusca.setSelectedItem(tipoAtual);
        }
        // Se não havia busca nem carregamento, não faz nada (tabela fica vazia)
    }
    
    /**
     * Executa uma busca sem mostrar mensagens de confirmação
     */
    private void executarBuscaSilenciosa() {
        try {
            modeloTabela.setRowCount(0);
            List<Patrimonio> resultados = null;
            
            switch (ultimoTipoBusca) {
                case "Número":
                    Optional<Patrimonio> patrimonioEncontrado = patrimonioService.buscarPorNumero(ultimoTermoBusca);
                    resultados = new ArrayList<>();
                    if (patrimonioEncontrado.isPresent()) {
                        resultados.add(patrimonioEncontrado.get());
                    }
                    break;
                case "Descrição":
                    resultados = buscarPorDescricao(ultimoTermoBusca);
                    break;
                case "Responsável":
                    resultados = buscarPorResponsavel(ultimoTermoBusca);
                    break;
                case "Sala":
                    resultados = buscarPorSala(ultimoTermoBusca);
                    break;
                case "Todos os campos":
                default:
                    // For "all fields" search, search across all fields
                    resultados = patrimonioService.listarTodos().stream()
                        .filter(p -> 
                            (p.getNumero() != null && p.getNumero().toLowerCase().contains(ultimoTermoBusca.toLowerCase())) ||
                            (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(ultimoTermoBusca.toLowerCase())) ||
                            (p.getMarca() != null && p.getMarca().toLowerCase().contains(ultimoTermoBusca.toLowerCase())) ||
                            (p.getModelo() != null && p.getModelo().toLowerCase().contains(ultimoTermoBusca.toLowerCase()))
                        )
                        .collect(java.util.stream.Collectors.toList());
                    break;
            }
            
            for (Patrimonio p : resultados) {
                modeloTabela.addRow(new Object[]{
                    p.getId(),
                    p.getNumero(),
                    p.getDescricao(),
                    p.getMarca(),
                    p.getModelo(),
                    p.getEstadoConservacao(),
                    p.getSituacao() != null ? p.getSituacao() : "ATIVO",
                    p.getNomeSala() != null ? p.getNomeSala() : "Não definida",
                    p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "Não definido"
                });
            }
        } catch (Exception e) {
            // Em caso de erro, carrega todos os patrimônios
            carregarPatrimonios();
        }
    }
}