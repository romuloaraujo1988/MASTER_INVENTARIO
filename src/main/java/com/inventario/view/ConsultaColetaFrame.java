package com.inventario.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.dao.ColetaDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.model.Coleta;
import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;

/**
 * Tela para consulta de coletas por número de patrimônio
 * Permite pesquisar um patrimônio e ver todas as coletas realizadas
 */
public class ConsultaColetaFrame extends JFrame {
    
    private static final Logger LOG = LoggerFactory.getLogger(ConsultaColetaFrame.class);
    
    // Componentes de pesquisa
    private JTextField txtNumeroPatrimonio;
    private JButton btnPesquisar;
    private JButton btnLimpar;
    private JComboBox<Inventario> comboInventario;
    
    // Painel de informações do patrimônio
    private JPanel painelInfoPatrimonio;
    private JLabel lblNumero;
    private JLabel lblDescricao;
    private JLabel lblSala;
    private JLabel lblResponsavel;
    private JLabel lblEstado;
    private JLabel lblStatusColeta;
    
    // Tabela de coletas
    private JTable tabelaColetas;
    private DefaultTableModel modeloTabela;
    
    // DAOs
    private final ColetaDAO coletaDAO;
    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;
    
    // Cores
    private static final Color COR_FUNDO = new Color(245, 245, 245);
    private static final Color COR_PRIMARIA = new Color(41, 128, 185);
    private static final Color COR_SUCESSO = new Color(39, 174, 96);
    private static final Color COR_ALERTA = new Color(243, 156, 18);
    private static final Color COR_ERRO = new Color(231, 76, 60);
    
    public ConsultaColetaFrame() {
        this.coletaDAO = new ColetaDAO();
        this.patrimonioDAO = new PatrimonioDAO();
        this.inventarioDAO = new InventarioDAO();
        
        initComponents();
        carregarInventarios();
        configurarEventos();
        
        setTitle("Consulta de Coletas por Patrimônio");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COR_FUNDO);
        
        // Painel superior - Pesquisa
        JPanel painelPesquisa = criarPainelPesquisa();
        add(painelPesquisa, BorderLayout.NORTH);
        
        // Painel central - Informações e Tabela
        JPanel painelCentral = new JPanel(new BorderLayout(10, 10));
        painelCentral.setOpaque(false);
        painelCentral.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        // Painel de informações do patrimônio
        painelInfoPatrimonio = criarPainelInfoPatrimonio();
        painelInfoPatrimonio.setVisible(false);
        painelCentral.add(painelInfoPatrimonio, BorderLayout.NORTH);
        
        // Tabela de coletas
        JPanel painelTabela = criarPainelTabela();
        painelCentral.add(painelTabela, BorderLayout.CENTER);
        
        add(painelCentral, BorderLayout.CENTER);
    }
    
    private JPanel criarPainelPesquisa() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_PRIMARIA);
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("🔍 Consulta de Coletas por Patrimônio");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        // Painel de campos
        JPanel painelCampos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        painelCampos.setOpaque(false);
        
        // Campo número patrimônio
        JLabel lblNumPat = new JLabel("Número do Patrimônio:");
        lblNumPat.setForeground(Color.WHITE);
        lblNumPat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        painelCampos.add(lblNumPat);
        
        txtNumeroPatrimonio = new JTextField(15);
        txtNumeroPatrimonio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNumeroPatrimonio.setToolTipText("Digite o número do patrimônio para pesquisar");
        painelCampos.add(txtNumeroPatrimonio);
        
        // Combo inventário (opcional)
        JLabel lblInv = new JLabel("Inventário:");
        lblInv.setForeground(Color.WHITE);
        lblInv.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        painelCampos.add(lblInv);
        
        comboInventario = new JComboBox<>();
        comboInventario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboInventario.setPreferredSize(new Dimension(200, 30));
        comboInventario.setToolTipText("Filtrar por inventário (opcional)");
        painelCampos.add(comboInventario);
        
        // Botões
        btnPesquisar = criarBotao("Pesquisar", COR_SUCESSO);
        painelCampos.add(btnPesquisar);
        
        btnLimpar = criarBotao("Limpar", COR_ALERTA);
        painelCampos.add(btnLimpar);
        
        painel.add(painelCampos, BorderLayout.CENTER);
        
        return painel;
    }
    
    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 30));
        return btn;
    }

    private JPanel criarPainelInfoPatrimonio() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título do painel
        JLabel lblTitulo = new JLabel("📦 Informações do Patrimônio");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(COR_PRIMARIA);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        painel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Linha 1: Número e Descrição
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(criarLabelCampo("Número:"), gbc);
        
        lblNumero = criarLabelValor("-");
        gbc.gridx = 1;
        painel.add(lblNumero, gbc);
        
        gbc.gridx = 2;
        painel.add(criarLabelCampo("Descrição:"), gbc);
        
        lblDescricao = criarLabelValor("-");
        lblDescricao.setPreferredSize(new Dimension(300, 25));
        gbc.gridx = 3;
        painel.add(lblDescricao, gbc);
        
        // Linha 2: Sala e Responsável
        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(criarLabelCampo("Sala:"), gbc);
        
        lblSala = criarLabelValor("-");
        gbc.gridx = 1;
        painel.add(lblSala, gbc);
        
        gbc.gridx = 2;
        painel.add(criarLabelCampo("Responsável:"), gbc);
        
        lblResponsavel = criarLabelValor("-");
        gbc.gridx = 3;
        painel.add(lblResponsavel, gbc);
        
        // Linha 3: Estado e Status de Coleta
        gbc.gridx = 0; gbc.gridy = 3;
        painel.add(criarLabelCampo("Estado:"), gbc);
        
        lblEstado = criarLabelValor("-");
        gbc.gridx = 1;
        painel.add(lblEstado, gbc);
        
        gbc.gridx = 2;
        painel.add(criarLabelCampo("Status:"), gbc);
        
        lblStatusColeta = criarLabelValor("-");
        lblStatusColeta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 3;
        painel.add(lblStatusColeta, gbc);
        
        return painel;
    }
    
    private JLabel criarLabelCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));
        return lbl;
    }
    
    private JLabel criarLabelValor(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(50, 50, 50));
        return lbl;
    }
    
    private JPanel criarPainelTabela() {
        JPanel painel = new JPanel(new BorderLayout(5, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Título
        JLabel lblTitulo = new JLabel("📋 Histórico de Coletas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(COR_PRIMARIA);
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabela
        String[] colunas = {
            "ID", "Inventário", "Data/Hora", "Coletor", "Status", 
            "Estado Encontrado", "Local Encontrado", "Divergência", "Observações"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaColetas = new JTable(modeloTabela);
        tabelaColetas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaColetas.setRowHeight(28);
        tabelaColetas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaColetas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaColetas.getTableHeader().setBackground(COR_PRIMARIA);
        tabelaColetas.getTableHeader().setForeground(Color.WHITE);
        
        // Configurar larguras das colunas
        tabelaColetas.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tabelaColetas.getColumnModel().getColumn(1).setPreferredWidth(150);  // Inventário
        tabelaColetas.getColumnModel().getColumn(2).setPreferredWidth(130);  // Data/Hora
        tabelaColetas.getColumnModel().getColumn(3).setPreferredWidth(120);  // Coletor
        tabelaColetas.getColumnModel().getColumn(4).setPreferredWidth(100);  // Status
        tabelaColetas.getColumnModel().getColumn(5).setPreferredWidth(100);  // Estado
        tabelaColetas.getColumnModel().getColumn(6).setPreferredWidth(150);  // Local
        tabelaColetas.getColumnModel().getColumn(7).setPreferredWidth(80);   // Divergência
        tabelaColetas.getColumnModel().getColumn(8).setPreferredWidth(200);  // Observações
        
        // Renderizador para colorir status
        tabelaColetas.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        tabelaColetas.getColumnModel().getColumn(7).setCellRenderer(new DivergenciaCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tabelaColetas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        painel.add(scrollPane, BorderLayout.CENTER);
        
        return painel;
    }
    
    private void carregarInventarios() {
        try {
            comboInventario.removeAllItems();
            comboInventario.addItem(null); // Opção "Todos"
            
            List<Inventario> inventarios = inventarioDAO.findAll();
            for (Inventario inv : inventarios) {
                comboInventario.addItem(inv);
            }
            
            // Renderer para mostrar "Todos" quando null
            comboInventario.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, 
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value == null) {
                        setText("Todos os inventários");
                    } else {
                        Inventario inv = (Inventario) value;
                        setText(inv.getNome());
                    }
                    return this;
                }
            });
            
        } catch (SQLException e) {
            LOG.error("Erro ao carregar inventários", e);
        }
    }
    
    private void configurarEventos() {
        // Pesquisar ao pressionar Enter
        txtNumeroPatrimonio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    pesquisar();
                }
            }
        });
        
        // Botão pesquisar
        btnPesquisar.addActionListener(e -> pesquisar());
        
        // Botão limpar
        btnLimpar.addActionListener(e -> limpar());
    }

    private void pesquisar() {
        String numero = txtNumeroPatrimonio.getText().trim();
        
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Digite o número do patrimônio para pesquisar.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
            txtNumeroPatrimonio.requestFocus();
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            // Buscar patrimônio
            Patrimonio patrimonio = patrimonioDAO.buscarPorNumero(numero);
            
            if (patrimonio == null) {
                painelInfoPatrimonio.setVisible(false);
                modeloTabela.setRowCount(0);
                JOptionPane.showMessageDialog(this, 
                    "Patrimônio não encontrado com o número: " + numero, 
                    "Não Encontrado", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Exibir informações do patrimônio
            exibirInfoPatrimonio(patrimonio);
            
            // Buscar coletas
            List<Coleta> coletas = coletaDAO.buscarPorPatrimonio(patrimonio.getId());
            
            // Filtrar por inventário se selecionado
            Inventario invSelecionado = (Inventario) comboInventario.getSelectedItem();
            if (invSelecionado != null) {
                coletas = coletas.stream()
                    .filter(c -> c.getIdInventario() == invSelecionado.getId())
                    .toList();
            }
            
            // Preencher tabela
            preencherTabela(coletas);
            
            // Atualizar status de coleta
            atualizarStatusColeta(coletas);
            
            LOG.info("Pesquisa realizada para patrimônio {}: {} coletas encontradas", numero, coletas.size());
            
        } catch (SQLException e) {
            LOG.error("Erro ao pesquisar patrimônio", e);
            JOptionPane.showMessageDialog(this, 
                "Erro ao pesquisar: " + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void exibirInfoPatrimonio(Patrimonio patrimonio) {
        lblNumero.setText(patrimonio.getNumero());
        lblDescricao.setText(patrimonio.getDescricao() != null ? patrimonio.getDescricao() : "-");
        lblSala.setText(patrimonio.getNomeSala() != null ? patrimonio.getNomeSala() : "-");
        lblResponsavel.setText(patrimonio.getNomeResponsavel() != null ? patrimonio.getNomeResponsavel() : "-");
        lblEstado.setText(patrimonio.getEstadoConservacao() != null ? patrimonio.getEstadoConservacao() : "-");
        
        painelInfoPatrimonio.setVisible(true);
    }
    
    private void atualizarStatusColeta(List<Coleta> coletas) {
        if (coletas.isEmpty()) {
            lblStatusColeta.setText("❌ NÃO COLETADO");
            lblStatusColeta.setForeground(COR_ERRO);
        } else {
            lblStatusColeta.setText("✅ COLETADO (" + coletas.size() + " vez" + (coletas.size() > 1 ? "es" : "") + ")");
            lblStatusColeta.setForeground(COR_SUCESSO);
        }
    }
    
    private void preencherTabela(List<Coleta> coletas) {
        modeloTabela.setRowCount(0);
        
        for (Coleta coleta : coletas) {
            Object[] linha = {
                coleta.getId(),
                coleta.getDescricaoInventario() != null ? coleta.getDescricaoInventario() : "-",
                coleta.getDataColetaFormatada(),
                coleta.getNomeColetor() != null ? coleta.getNomeColetor() : "-",
                coleta.getStatusColeta() != null ? coleta.getStatusColeta() : "-",
                coleta.getEstadoEncontrado() != null ? coleta.getEstadoEncontrado() : "-",
                coleta.getLocalizacaoEncontrada() != null ? coleta.getLocalizacaoEncontrada() : "-",
                coleta.isDivergencia() ? "SIM" : "NÃO",
                coleta.getObservacaoColeta() != null ? coleta.getObservacaoColeta() : "-"
            };
            modeloTabela.addRow(linha);
        }
    }
    
    private void limpar() {
        txtNumeroPatrimonio.setText("");
        comboInventario.setSelectedIndex(0);
        painelInfoPatrimonio.setVisible(false);
        modeloTabela.setRowCount(0);
        txtNumeroPatrimonio.requestFocus();
    }
    
    // Renderizador para colorir status
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String status = value.toString();
                switch (status) {
                    case "COLETADO":
                        setBackground(new Color(212, 237, 218));
                        setForeground(new Color(21, 87, 36));
                        break;
                    case "NAO_ENCONTRADO":
                        setBackground(new Color(248, 215, 218));
                        setForeground(new Color(114, 28, 36));
                        break;
                    case "DIVERGENCIA":
                        setBackground(new Color(255, 243, 205));
                        setForeground(new Color(133, 100, 4));
                        break;
                    default:
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                }
            } else {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return this;
        }
    }
    
    // Renderizador para colorir divergência
    private class DivergenciaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && value != null) {
                String divergencia = value.toString();
                if ("SIM".equals(divergencia)) {
                    setBackground(new Color(255, 243, 205));
                    setForeground(new Color(133, 100, 4));
                } else {
                    setBackground(new Color(212, 237, 218));
                    setForeground(new Color(21, 87, 36));
                }
            } else {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return this;
        }
    }
    
    /**
     * Método estático para abrir a tela de consulta
     */
    public static void abrir() {
        SwingUtilities.invokeLater(() -> {
            ConsultaColetaFrame frame = new ConsultaColetaFrame();
            frame.setVisible(true);
        });
    }
    
    /**
     * Método estático para abrir a tela já com um número de patrimônio
     */
    public static void abrir(String numeroPatrimonio) {
        SwingUtilities.invokeLater(() -> {
            ConsultaColetaFrame frame = new ConsultaColetaFrame();
            frame.txtNumeroPatrimonio.setText(numeroPatrimonio);
            frame.setVisible(true);
            frame.pesquisar();
        });
    }
}
