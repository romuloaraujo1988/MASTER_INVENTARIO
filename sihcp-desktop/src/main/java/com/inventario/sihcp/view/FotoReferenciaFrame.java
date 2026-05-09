package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.inventario.sihcp.model.DescricaoResumo;
import com.inventario.sihcp.model.FotoReferencia;
import com.inventario.sihcp.service.FotoReferenciaService;
import com.inventario.sihcp.service.FotoReferenciaService.EstatisticasFoto;
import com.inventario.sihcp.service.FotoReferenciaService.ServiceException;
import com.inventario.sihcp.view.ui.ButtonStyleFactory;

/**
 * Tela para gerenciamento de fotos de referência de patrimônios.
 * 
 * Permite:
 * - Visualizar descrições únicas de patrimônios
 * - Cadastrar fotos de referência para cada descrição
 * - Filtrar descrições em tempo real
 * - Visualizar preview das fotos
 * - Excluir fotos existentes
 * 
 * Feature: foto-referencia-descricao
 * Requirements: 1.1, 1.5, 1.7, 7.1, 7.2
 * 
 * @author Sistema de Inventário
 * @since 2.9.0
 */
public class FotoReferenciaFrame extends JFrame {
    
    private static final int MAX_FILE_SIZE_MB = 2;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final int PREVIEW_SIZE = 200;
    
    // Componentes da UI
    private JTable tabelaDescricoes;
    private DefaultTableModel modeloTabela;
    private JTextField campoBusca;
    private JLabel lblPreview;
    private JLabel lblInfoFoto;
    private JLabel lblEstatisticas;
    private JButton btnSelecionarImagem;
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnAtualizar;
    
    // Estado
    private final FotoReferenciaService service;
    private byte[] imagemSelecionada;
    private DescricaoResumo descricaoSelecionada;
    private final String usuarioLogado;
    private List<DescricaoResumo> todasDescricoes;
    
    /**
     * Construtor padrão
     */
    public FotoReferenciaFrame() {
        this.service = new FotoReferenciaService();
        this.usuarioLogado = System.getProperty("user.name", "admin");
        initComponents();
        carregarDados();
    }
    
    /**
     * Construtor com injeção de dependências (para testes)
     */
    public FotoReferenciaFrame(FotoReferenciaService service, String usuario) {
        this.service = service;
        this.usuarioLogado = usuario;
        initComponents();
        carregarDados();
    }
    
    private void initComponents() {
        setTitle("📷 Fotos de Referência de Patrimônios");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Definir ícone da janela
        setIconImages(com.inventario.sihcp.util.IconManager.getAppIconImages());
        
        // Painel principal com padding
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelPrincipal.setBackground(new Color(236, 240, 241));
        
        // Painel superior: busca e estatísticas
        painelPrincipal.add(criarPainelSuperior(), BorderLayout.NORTH);
        
        // Painel central: tabela e preview
        painelPrincipal.add(criarPainelCentral(), BorderLayout.CENTER);
        
        // Painel inferior: ações
        painelPrincipal.add(criarPainelAcoes(), BorderLayout.SOUTH);
        
        add(painelPrincipal);
        
        // Configurar eventos
        configurarEventos();
        
        // Tamanho e posição
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    /**
     * Cria o painel superior com busca e estatísticas
     */
    private JPanel criarPainelSuperior() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(new Color(236, 240, 241));
        painel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Painel de busca (esquerda)
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelBusca.setBackground(new Color(236, 240, 241));
        
        JLabel lblBuscar = new JLabel("🔍 Buscar descrição:");
        lblBuscar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        painelBusca.add(lblBuscar);
        
        campoBusca = new JTextField(30);
        campoBusca.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        campoBusca.setToolTipText("Digite para filtrar descrições em tempo real");
        painelBusca.add(campoBusca);
        
        painel.add(painelBusca, BorderLayout.WEST);
        
        // Painel de estatísticas (direita)
        JPanel painelStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelStats.setBackground(new Color(236, 240, 241));
        
        lblEstatisticas = new JLabel("Carregando estatísticas...");
        lblEstatisticas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lblEstatisticas.setForeground(new Color(100, 100, 100));
        painelStats.add(lblEstatisticas);
        
        btnAtualizar = ButtonStyleFactory.createPrimaryButton("🔄 Atualizar");
        btnAtualizar.setToolTipText("Recarregar lista de descrições");
        painelStats.add(btnAtualizar);
        
        painel.add(painelStats, BorderLayout.EAST);
        
        return painel;
    }
    
    /**
     * Cria o painel central com tabela e preview
     */
    private JPanel criarPainelCentral() {
        JPanel painel = new JPanel(new BorderLayout(10, 0));
        painel.setBackground(new Color(236, 240, 241));
        
        // Tabela de descrições (esquerda)
        painel.add(criarPainelTabela(), BorderLayout.CENTER);
        
        // Preview da foto (direita)
        painel.add(criarPainelPreview(), BorderLayout.EAST);
        
        return painel;
    }
    
    /**
     * Cria o painel da tabela de descrições
     */
    private JPanel criarPainelTabela() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        
        // Título
        JLabel lblTitulo = new JLabel("  Descrições de Patrimônios");
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblTitulo.setBackground(new Color(248, 249, 250));
        lblTitulo.setOpaque(true);
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        // Modelo da tabela
        String[] colunas = {"Descrição Normalizada", "Qtd. Patrimônios", "Tem Foto"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Integer.class;
                return String.class;
            }
        };
        
        tabelaDescricoes = new JTable(modeloTabela);
        tabelaDescricoes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaDescricoes.setRowHeight(28);
        tabelaDescricoes.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Configurar larguras das colunas
        tabelaDescricoes.getColumnModel().getColumn(0).setPreferredWidth(400);
        tabelaDescricoes.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaDescricoes.getColumnModel().getColumn(2).setPreferredWidth(80);
        
        // Configurar cabeçalho
        JTableHeader header = tabelaDescricoes.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 35));
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        header.setBackground(new Color(248, 249, 250));
        header.setForeground(new Color(73, 80, 87));
        
        // Renderizador para linhas alternadas e indicador de foto
        tabelaDescricoes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                } else {
                    c.setBackground(new Color(0, 123, 255, 50));
                }
                
                // Coluna "Tem Foto" com ícone
                if (column == 2 && value != null) {
                    String temFoto = value.toString();
                    if ("Sim".equals(temFoto)) {
                        setText("✅ Sim");
                        setForeground(new Color(40, 167, 69));
                    } else {
                        setText("❌ Não");
                        setForeground(new Color(220, 53, 69));
                    }
                } else {
                    setForeground(Color.BLACK);
                }
                
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });
        
        // Renderizador para coluna numérica
        tabelaDescricoes.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tabelaDescricoes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        painel.add(scrollPane, BorderLayout.CENTER);
        
        return painel;
    }

    /**
     * Cria o painel de preview da foto
     */
    private JPanel criarPainelPreview() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(Color.WHITE);
        painel.setPreferredSize(new Dimension(280, 0));
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Título
        JLabel lblTitulo = new JLabel("Preview da Foto", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        lblTitulo.setForeground(new Color(52, 73, 94));
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        // Área de preview
        JPanel painelImagem = new JPanel(new BorderLayout());
        painelImagem.setBackground(new Color(248, 249, 250));
        painelImagem.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        lblPreview = new JLabel("Selecione uma descrição", SwingConstants.CENTER);
        lblPreview.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        lblPreview.setForeground(new Color(150, 150, 150));
        lblPreview.setPreferredSize(new Dimension(PREVIEW_SIZE, PREVIEW_SIZE));
        painelImagem.add(lblPreview, BorderLayout.CENTER);
        
        painel.add(painelImagem, BorderLayout.CENTER);
        
        // Informações da foto
        JPanel painelInfo = new JPanel(new BorderLayout());
        painelInfo.setBackground(Color.WHITE);
        
        lblInfoFoto = new JLabel("", SwingConstants.CENTER);
        lblInfoFoto.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        lblInfoFoto.setForeground(new Color(100, 100, 100));
        painelInfo.add(lblInfoFoto, BorderLayout.CENTER);
        
        painel.add(painelInfo, BorderLayout.SOUTH);
        
        return painel;
    }
    
    /**
     * Cria o painel de ações (botões)
     */
    private JPanel criarPainelAcoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painel.setBackground(new Color(236, 240, 241));
        painel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        
        btnSelecionarImagem = ButtonStyleFactory.createPrimaryButton("📁 Selecionar Imagem");
        btnSelecionarImagem.setEnabled(false);
        btnSelecionarImagem.setToolTipText("Selecione uma descrição primeiro");
        painel.add(btnSelecionarImagem);
        
        btnSalvar = ButtonStyleFactory.createSuccessButton("💾 Salvar Foto");
        btnSalvar.setEnabled(false);
        btnSalvar.setToolTipText("Selecione uma imagem primeiro");
        painel.add(btnSalvar);
        
        btnExcluir = ButtonStyleFactory.createDangerButton("🗑️ Excluir Foto");
        btnExcluir.setEnabled(false);
        btnExcluir.setToolTipText("Selecione uma descrição com foto");
        painel.add(btnExcluir);
        
        return painel;
    }
    
    /**
     * Configura os eventos dos componentes
     */
    private void configurarEventos() {
        // Filtro em tempo real
        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filtrarDescricoes(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filtrarDescricoes(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filtrarDescricoes(); }
        });
        
        // Seleção na tabela
        tabelaDescricoes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onDescricaoSelecionada();
            }
        });
        
        // Duplo clique para selecionar imagem
        tabelaDescricoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selecionarImagem();
                }
            }
        });
        
        // Botões
        btnSelecionarImagem.addActionListener(e -> selecionarImagem());
        btnSalvar.addActionListener(e -> salvarFoto());
        btnExcluir.addActionListener(e -> excluirFoto());
        btnAtualizar.addActionListener(e -> carregarDados());
    }
    
    /**
     * Carrega os dados iniciais
     */
    private void carregarDados() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<DescricaoResumo> descricoes;
            private EstatisticasFoto estatisticas;
            private Exception erro;
            
            @Override
            protected Void doInBackground() {
                try {
                    descricoes = service.buscarDescricoesUnicas();
                    estatisticas = service.obterEstatisticas();
                } catch (ServiceException e) {
                    erro = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                
                if (erro != null) {
                    JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                        "Erro ao carregar dados: " + erro.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                todasDescricoes = descricoes;
                atualizarTabela(descricoes);
                atualizarEstatisticas(estatisticas);
                limparSelecao();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Atualiza a tabela com as descrições
     */
    private void atualizarTabela(List<DescricaoResumo> descricoes) {
        modeloTabela.setRowCount(0);
        
        for (DescricaoResumo desc : descricoes) {
            modeloTabela.addRow(new Object[]{
                desc.getDescricaoNormalizada(),
                desc.getQuantidadePatrimonios(),
                desc.isPossuiFoto() ? "Sim" : "Não"
            });
        }
    }
    
    /**
     * Atualiza o label de estatísticas
     */
    private void atualizarEstatisticas(EstatisticasFoto stats) {
        if (stats == null) {
            lblEstatisticas.setText("Estatísticas indisponíveis");
            return;
        }
        
        String texto = String.format(
            "📊 Total: %d fotos | 📷 Com foto: %d | ❌ Sem foto: %d | 💾 Armazenamento: %s",
            stats.getTotalFotos(),
            stats.getTotalFotos(),
            stats.getDescricoesSemFoto(),
            formatarTamanho(stats.getUsoArmazenamentoBytes())
        );
        lblEstatisticas.setText(texto);
    }
    
    /**
     * Formata tamanho em bytes para exibição
     */
    private String formatarTamanho(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    /**
     * Filtra as descrições conforme texto digitado
     * Property 5: Filtro de Busca - case-insensitive
     */
    private void filtrarDescricoes() {
        if (todasDescricoes == null) return;
        
        String filtro = campoBusca.getText().trim().toLowerCase();
        
        if (filtro.isEmpty()) {
            atualizarTabela(todasDescricoes);
        } else {
            List<DescricaoResumo> filtradas = todasDescricoes.stream()
                .filter(d -> d.getDescricaoNormalizada().toLowerCase().contains(filtro))
                .toList();
            atualizarTabela(filtradas);
        }
        
        limparSelecao();
    }
    
    /**
     * Chamado quando uma descrição é selecionada na tabela
     */
    private void onDescricaoSelecionada() {
        int linha = tabelaDescricoes.getSelectedRow();
        
        if (linha < 0) {
            limparSelecao();
            return;
        }
        
        String descricao = (String) modeloTabela.getValueAt(linha, 0);
        int qtdPatrimonios = (Integer) modeloTabela.getValueAt(linha, 1);
        boolean temFoto = "Sim".equals(modeloTabela.getValueAt(linha, 2));
        
        descricaoSelecionada = new DescricaoResumo(descricao, qtdPatrimonios, temFoto, 0);
        
        // Habilitar botões
        btnSelecionarImagem.setEnabled(true);
        btnSelecionarImagem.setToolTipText("Selecionar imagem para: " + descricao);
        btnExcluir.setEnabled(temFoto);
        
        // Carregar preview se tiver foto
        if (temFoto) {
            carregarPreview(descricao);
        } else {
            lblPreview.setIcon(null);
            lblPreview.setText("Sem foto cadastrada");
            lblInfoFoto.setText(String.format("Beneficiará %d patrimônios", qtdPatrimonios));
        }
        
        // Limpar imagem selecionada
        imagemSelecionada = null;
        btnSalvar.setEnabled(false);
    }
    
    /**
     * Carrega o preview da foto existente
     */
    private void carregarPreview(String descricao) {
        SwingWorker<Optional<FotoReferencia>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<FotoReferencia> doInBackground() {
                try {
                    return service.buscarPorDescricao(descricao);
                } catch (ServiceException e) {
                    return Optional.empty();
                }
            }
            
            @Override
            protected void done() {
                try {
                    Optional<FotoReferencia> fotoOpt = get();
                    
                    if (fotoOpt.isPresent()) {
                        FotoReferencia foto = fotoOpt.get();
                        exibirImagem(foto.getImagemBlob());
                        lblInfoFoto.setText(String.format(
                            "Tamanho: %s | Cadastrada em: %s",
                            formatarTamanho(foto.getTamanhoBytes()),
                            foto.getDataCadastro() != null ? 
                                foto.getDataCadastro().toLocalDateTime().toLocalDate().toString() : "N/A"
                        ));
                    } else {
                        lblPreview.setIcon(null);
                        lblPreview.setText("Foto não encontrada");
                        lblInfoFoto.setText("");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    lblPreview.setIcon(null);
                    lblPreview.setText("Erro ao carregar");
                    lblInfoFoto.setText(e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Exibe uma imagem no preview
     */
    private void exibirImagem(byte[] dados) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(dados));
            if (img != null) {
                Image scaled = img.getScaledInstance(PREVIEW_SIZE, PREVIEW_SIZE, Image.SCALE_SMOOTH);
                lblPreview.setIcon(new ImageIcon(scaled));
                lblPreview.setText("");
            } else {
                lblPreview.setIcon(null);
                lblPreview.setText("Imagem inválida");
            }
        } catch (IOException e) {
            lblPreview.setIcon(null);
            lblPreview.setText("Erro ao exibir imagem");
        }
    }
    
    /**
     * Limpa a seleção atual
     */
    private void limparSelecao() {
        descricaoSelecionada = null;
        imagemSelecionada = null;
        
        lblPreview.setIcon(null);
        lblPreview.setText("Selecione uma descrição");
        lblInfoFoto.setText("");
        
        btnSelecionarImagem.setEnabled(false);
        btnSalvar.setEnabled(false);
        btnExcluir.setEnabled(false);
    }
    
    /**
     * Abre diálogo para selecionar imagem
     * Requirements: 1.2, 1.6
     */
    private void selecionarImagem() {
        if (descricaoSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma descrição primeiro.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Imagem de Referência");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Imagens (JPG, PNG)", "jpg", "jpeg", "png"
        ));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        int resultado = fileChooser.showOpenDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            
            // Validar tamanho
            if (arquivo.length() > MAX_FILE_SIZE_BYTES) {
                JOptionPane.showMessageDialog(this,
                    String.format("""
                                  O arquivo excede o tamanho m\u00e1ximo de %d MB.
                                  Tamanho do arquivo: %.2f MB""",
                        MAX_FILE_SIZE_MB, arquivo.length() / (1024.0 * 1024.0)),
                    "Arquivo muito grande", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                byte[] dados = Files.readAllBytes(arquivo.toPath());
                
                // Validar se é imagem válida
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(dados));
                if (img == null) {
                    JOptionPane.showMessageDialog(this,
                        "O arquivo selecionado não é uma imagem válida.",
                        "Arquivo inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Armazenar e exibir preview
                imagemSelecionada = dados;
                exibirImagem(dados);
                
                lblInfoFoto.setText(String.format(
                    "Arquivo: %s | Tamanho: %s | %dx%d px",
                    arquivo.getName(),
                    formatarTamanho(arquivo.length()),
                    img.getWidth(), img.getHeight()
                ));
                
                btnSalvar.setEnabled(true);
                btnSalvar.setToolTipText("Salvar foto para: " + descricaoSelecionada.getDescricaoNormalizada());
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao ler o arquivo: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Salva a foto de referência
     * Requirements: 1.4
     */
    private void salvarFoto() {
        if (descricaoSelecionada == null || imagemSelecionada == null) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma descrição e uma imagem primeiro.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirmar se já existe foto
        if (descricaoSelecionada.isPossuiFoto()) {
            int confirmacao = JOptionPane.showConfirmDialog(this, """
                                                                  Esta descri\u00e7\u00e3o j\u00e1 possui uma foto cadastrada.
                                                                  Deseja substituir a foto existente?""",
                "Confirmar Substituição",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSalvar.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Exception erro;
            
            @Override
            protected Boolean doInBackground() {
                try {
                    service.salvarFotoReferencia(
                        descricaoSelecionada.getDescricaoNormalizada(),
                        imagemSelecionada,
                        usuarioLogado
                    );
                    return true;
                } catch (ServiceException e) {
                    erro = e;
                    return false;
                }
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                            String.format("""
                                          Foto salva com sucesso!
                                          
                                          Descri\u00e7\u00e3o: %s
                                          Patrim\u00f4nios beneficiados: %d""",
                                descricaoSelecionada.getDescricaoNormalizada(),
                                descricaoSelecionada.getQuantidadePatrimonios()),
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Recarregar dados
                        carregarDados();
                    } else {
                        String mensagem = erro != null ? erro.getMessage() : "Erro desconhecido";
                        JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                            "Erro ao salvar foto: " + mensagem,
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        btnSalvar.setEnabled(true);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                        "Erro ao salvar foto: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    btnSalvar.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Exclui a foto de referência (soft delete)
     * Requirements: 7.2, 7.3
     */
    private void excluirFoto() {
        if (descricaoSelecionada == null || !descricaoSelecionada.isPossuiFoto()) {
            JOptionPane.showMessageDialog(this,
                "Selecione uma descrição que possua foto.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            String.format("""
                          Tem certeza que deseja excluir a foto de refer\u00eancia?
                          
                          Descri\u00e7\u00e3o: %s
                          Patrim\u00f4nios afetados: %d
                          
                          Esta a\u00e7\u00e3o n\u00e3o pode ser desfeita.""",
                descricaoSelecionada.getDescricaoNormalizada(),
                descricaoSelecionada.getQuantidadePatrimonios()),
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnExcluir.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Exception erro;
            
            @Override
            protected Boolean doInBackground() {
                try {
                    // Buscar a foto para obter o ID
                    Optional<FotoReferencia> fotoOpt = service.buscarPorDescricao(
                        descricaoSelecionada.getDescricaoNormalizada()
                    );
                    
                    if (fotoOpt.isPresent()) {
                        service.excluirFotoReferencia(fotoOpt.get().getId());
                        return true;
                    }
                    return false;
                } catch (ServiceException e) {
                    erro = e;
                    return false;
                }
            }
            
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                            "Foto excluída com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Recarregar dados
                        carregarDados();
                    } else {
                        String mensagem = erro != null ? erro.getMessage() : "Foto não encontrada";
                        JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                            "Erro ao excluir foto: " + mensagem,
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        btnExcluir.setEnabled(true);
                    }
                } catch (HeadlessException | InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FotoReferenciaFrame.this,
                        "Erro ao excluir foto: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    btnExcluir.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Método para obter a lista de descrições filtradas (para testes)
     */
    public List<String> getDescricoesFiltradas() {
        java.util.ArrayList<String> resultado = new java.util.ArrayList<>();
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            resultado.add((String) modeloTabela.getValueAt(i, 0));
        }
        return resultado;
    }
    
    /**
     * Método para definir o texto de busca (para testes)
     */
    public void setTextoBusca(String texto) {
        campoBusca.setText(texto);
    }
    
    /**
     * Método para obter a descrição selecionada (para testes)
     */
    public DescricaoResumo getDescricaoSelecionada() {
        return descricaoSelecionada;
    }
}
