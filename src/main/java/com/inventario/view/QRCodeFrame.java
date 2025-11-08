package com.inventario.view;

import com.inventario.service.PatrimonioService;
import com.inventario.service.ServiceFactory;
import com.inventario.model.Patrimonio;
import com.inventario.model.QRCode;
import com.inventario.service.QRCodeService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Frame para gerenciamento de QR Codes
 */
public class QRCodeFrame extends JFrame {
    
    private final QRCodeService qrCodeService;
    private final PatrimonioService patrimonioService;
    
    // Componentes da interface
    private JTable tabelaQRCodes;
    private DefaultTableModel modeloTabela;
    private JTextField campoNumeroPatrimonio;
    private JSpinner spinnerTamanho;
    private JLabel labelImagemQR;
    private JButton btnGerar;
    private JButton btnRegenerar;
    private JButton btnVisualizar;
    private JButton btnSalvarImagem;
    private JButton btnDesativar;
    private JButton btnAtualizar;
    private JLabel labelEstatisticas;
    
    // QR Code selecionado
    private QRCode qrCodeSelecionado;
    
    public QRCodeFrame() {
        this.qrCodeService = new QRCodeService();
        this.patrimonioService = ServiceFactory.getInstance().getPatrimonioService();
        initComponents();
        setupLayout();
        setupEventListeners();
        carregarQRCodes();
        atualizarEstatisticas();
        
        setTitle("Gerenciamento de QR Codes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        // Tabela de QR Codes
        String[] colunas = {"ID", "Patrimônio", "Número", "Descrição", "Data Geração", "Tamanho", "Ativo"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaQRCodes = new JTable(modeloTabela);
        tabelaQRCodes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Campos de entrada
        campoNumeroPatrimonio = new JTextField(15);
        spinnerTamanho = new JSpinner(new SpinnerNumberModel(200, 100, 500, 50));
        
        // Label para exibir imagem do QR Code
        labelImagemQR = new JLabel();
        labelImagemQR.setHorizontalAlignment(SwingConstants.CENTER);
        labelImagemQR.setVerticalAlignment(SwingConstants.CENTER);
        labelImagemQR.setBorder(BorderFactory.createLoweredBevelBorder());
        labelImagemQR.setPreferredSize(new Dimension(250, 250));
        labelImagemQR.setText("Selecione um QR Code para visualizar");
        
        // Botões
        btnGerar = new JButton("Gerar QR Code");
        btnRegenerar = new JButton("Regenerar");
        btnVisualizar = new JButton("Visualizar");
        btnSalvarImagem = new JButton("Salvar Imagem");
        btnDesativar = new JButton("Desativar");
        btnAtualizar = new JButton("Atualizar Lista");
        
        // Label de estatísticas
        labelEstatisticas = new JLabel();
        labelEstatisticas.setFont(labelEstatisticas.getFont().deriveFont(Font.BOLD));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel superior - Geração de QR Code
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(new TitledBorder("Gerar QR Code"));
        
        JPanel panelEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEntrada.add(new JLabel("Número do Patrimônio:"));
        panelEntrada.add(campoNumeroPatrimonio);
        panelEntrada.add(new JLabel("Tamanho:"));
        panelEntrada.add(spinnerTamanho);
        panelEntrada.add(new JLabel("px"));
        panelEntrada.add(btnGerar);
        
        panelSuperior.add(panelEntrada, BorderLayout.NORTH);
        panelSuperior.add(labelEstatisticas, BorderLayout.SOUTH);
        
        // Panel central - Tabela e visualização
        JPanel panelCentral = new JPanel(new BorderLayout());
        
        // Tabela
        JScrollPane scrollTabela = new JScrollPane(tabelaQRCodes);
        scrollTabela.setPreferredSize(new Dimension(600, 400));
        
        // Panel de visualização
        JPanel panelVisualizacao = new JPanel(new BorderLayout());
        panelVisualizacao.setBorder(new TitledBorder("Visualização do QR Code"));
        panelVisualizacao.add(labelImagemQR, BorderLayout.CENTER);
        
        // Botões de ação
        JPanel panelBotoesVisualizacao = new JPanel(new FlowLayout());
        panelBotoesVisualizacao.add(btnVisualizar);
        panelBotoesVisualizacao.add(btnSalvarImagem);
        panelBotoesVisualizacao.add(btnRegenerar);
        panelBotoesVisualizacao.add(btnDesativar);
        panelVisualizacao.add(panelBotoesVisualizacao, BorderLayout.SOUTH);
        
        panelCentral.add(scrollTabela, BorderLayout.CENTER);
        panelCentral.add(panelVisualizacao, BorderLayout.EAST);
        
        // Panel inferior - Botões de controle
        JPanel panelInferior = new JPanel(new FlowLayout());
        panelInferior.add(btnAtualizar);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEventListeners() {
        // Seleção na tabela
        tabelaQRCodes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tabelaQRCodes.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (Integer) modeloTabela.getValueAt(selectedRow, 0);
                    carregarQRCodeSelecionado(id);
                }
            }
        });
        
        // Botão Gerar
        btnGerar.addActionListener(e -> gerarQRCode());
        
        // Botão Regenerar
        btnRegenerar.addActionListener(e -> regenerarQRCode());
        
        // Botão Visualizar
        btnVisualizar.addActionListener(e -> visualizarQRCode());
        
        // Botão Salvar Imagem
        btnSalvarImagem.addActionListener(e -> salvarImagemQRCode());
        
        // Botão Desativar
        btnDesativar.addActionListener(e -> desativarQRCode());
        
        // Botão Atualizar
        btnAtualizar.addActionListener(e -> {
            carregarQRCodes();
            atualizarEstatisticas();
        });
    }
    
    private void gerarQRCode() {
        String numeroPatrimonio = campoNumeroPatrimonio.getText().trim();
        
        if (numeroPatrimonio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o número do patrimônio.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Busca o patrimônio
            Patrimonio patrimonio = patrimonioService.buscarPorNumero(numeroPatrimonio);
            if (patrimonio == null) {
                JOptionPane.showMessageDialog(this, "Patrimônio não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verifica se já tem QR Code ativo
            if (qrCodeService.patrimonioTemQRCodeAtivo(patrimonio.getId())) {
                int opcao = JOptionPane.showConfirmDialog(this, 
                    "Este patrimônio já possui um QR Code ativo. Deseja regenerar?", 
                    "QR Code Existente", 
                    JOptionPane.YES_NO_OPTION);
                
                if (opcao == JOptionPane.YES_OPTION) {
                    regenerarQRCodePatrimonio(patrimonio);
                }
                return;
            }
            
            // Gera o QR Code
            int tamanho = (Integer) spinnerTamanho.getValue();
            QRCode qrCode = qrCodeService.gerarQRCode(patrimonio, tamanho);
            
            JOptionPane.showMessageDialog(this, "QR Code gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            // Atualiza a interface
            carregarQRCodes();
            atualizarEstatisticas();
            campoNumeroPatrimonio.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar QR Code: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void regenerarQRCode() {
        if (qrCodeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um QR Code na tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Busca o patrimônio
            Patrimonio patrimonio = patrimonioService.buscarPorId(Long.valueOf(qrCodeSelecionado.getIdPatrimonio()));
            if (patrimonio == null) {
                JOptionPane.showMessageDialog(this, "Patrimônio não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            regenerarQRCodePatrimonio(patrimonio);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao regenerar QR Code: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void regenerarQRCodePatrimonio(Patrimonio patrimonio) throws Exception {
        int tamanho = (Integer) spinnerTamanho.getValue();
        QRCode novoQRCode = qrCodeService.regenerarQRCode(patrimonio, tamanho);
        
        JOptionPane.showMessageDialog(this, "QR Code regenerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        
        // Atualiza a interface
        carregarQRCodes();
        atualizarEstatisticas();
        
        // Seleciona o novo QR Code
        selecionarQRCodeNaTabela(novoQRCode.getId());
    }
    
    private void visualizarQRCode() {
        if (qrCodeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um QR Code na tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            byte[] imagemBytes = qrCodeService.gerarImagemQRCode(qrCodeSelecionado);
            ImageIcon icon = new ImageIcon(imagemBytes);
            
            // Redimensiona a imagem para caber no label
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
            labelImagemQR.setIcon(new ImageIcon(scaledImg));
            labelImagemQR.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao visualizar QR Code: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void salvarImagemQRCode() {
        if (qrCodeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um QR Code na tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Imagem do QR Code");
            fileChooser.setSelectedFile(new File("QRCode_" + qrCodeSelecionado.getNumeroPatrimonio() + ".png"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                
                byte[] imagemBytes = qrCodeService.gerarImagemQRCode(qrCodeSelecionado);
                
                try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                    fos.write(imagemBytes);
                }
                
                JOptionPane.showMessageDialog(this, "Imagem salva com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar imagem: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void desativarQRCode() {
        if (qrCodeSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um QR Code na tabela.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int opcao = JOptionPane.showConfirmDialog(this, 
            "Deseja realmente desativar este QR Code?", 
            "Confirmar Desativação", 
            JOptionPane.YES_NO_OPTION);
        
        if (opcao == JOptionPane.YES_OPTION) {
            try {
                boolean sucesso = qrCodeService.desativar(qrCodeSelecionado.getId());
                
                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "QR Code desativado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarQRCodes();
                    atualizarEstatisticas();
                    labelImagemQR.setIcon(null);
                    labelImagemQR.setText("Selecione um QR Code para visualizar");
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao desativar QR Code.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao desativar QR Code: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void carregarQRCodes() {
        try {
            List<QRCode> qrCodes = qrCodeService.listarAtivos();
            
            // Limpa a tabela
            modeloTabela.setRowCount(0);
            
            // Adiciona os QR Codes
            for (QRCode qrCode : qrCodes) {
                Object[] linha = {
                    qrCode.getId(),
                    qrCode.getIdPatrimonio(),
                    qrCode.getNumeroPatrimonio(),
                    qrCode.getDescricaoPatrimonio(),
                    qrCode.getDataGeracaoFormatada(),
                    qrCode.getTamanho() + "px",
                    qrCode.isAtivo() ? "Sim" : "Não"
                };
                modeloTabela.addRow(linha);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar QR Codes: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void carregarQRCodeSelecionado(int id) {
        try {
            qrCodeSelecionado = qrCodeService.buscarPorId(id);
            
            if (qrCodeSelecionado != null) {
                // Habilita botões
                btnVisualizar.setEnabled(true);
                btnSalvarImagem.setEnabled(true);
                btnRegenerar.setEnabled(true);
                btnDesativar.setEnabled(qrCodeSelecionado.isAtivo());
                
                // Carrega automaticamente a visualização
                visualizarQRCode();
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar QR Code: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void selecionarQRCodeNaTabela(int id) {
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            if ((Integer) modeloTabela.getValueAt(i, 0) == id) {
                tabelaQRCodes.setRowSelectionInterval(i, i);
                break;
            }
        }
    }
    
    private void atualizarEstatisticas() {
        try {
            Map<String, Object> stats = qrCodeService.gerarEstatisticas();
            
            String texto = String.format(
                "Total: %d | Ativos: %d | Inativos: %d",
                stats.get("totalQRCodes"),
                stats.get("qrCodesAtivos"),
                stats.get("qrCodesInativos")
            );
            
            labelEstatisticas.setText(texto);
            
        } catch (SQLException e) {
            labelEstatisticas.setText("Erro ao carregar estatísticas");
            e.printStackTrace();
        }
    }
}