package com.inventario.sihcp.view;

import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.print.EtiquetaConfig;
import com.inventario.sihcp.print.EtiquetaPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Editor Profissional de Etiquetas Patrimoniais (Estilo Bartender)
 * Arquitetura de 3 painéis: Camadas | Canvas | Inspetor
 */
public class ImpressaoEtiquetasDialog extends JDialog {
    
    private final List<Patrimonio> patrimonios;
    private final EtiquetaConfig config;
    
    // Elementos de UI - Inspetor
    private JTextField txtCabecalho;
    private JSpinner spnX, spnY, spnWidth, spnHeight, spnFontSize;
    private JSpinner spnPaperW, spnPaperH, spnCols;
    private JLabel lblSelectedName;
    private JToggleButton btnSnapGrid;
    
    // Estado do Editor
    private int selectedElement = -1; // -1=Nenhum, 0=Cabecalho, 1=QRCode, 2=Numero, 3=Descricao, 4=MarcaModelo
    private final PreviewPanel pnlCanvas;
    private boolean impressaoConfirmada = false;
    
    private double zoomScale = 5.0;
    private boolean snapToGrid = true;
    private int dragMode = 0; // 0=move, 1=resize_br

    // Constantes de Design
    private static final Color COLOR_WORKSPACE = new Color(45, 45, 48);
    private static final Color COLOR_ACCENT = new Color(0, 120, 215);
    private static final Color COLOR_SIDEBAR = new Color(245, 245, 245);

    public ImpressaoEtiquetasDialog(Frame parent, List<Patrimonio> patrimonios) {
        super(parent, "Editor de Design de Etiquetas - Estilo Bartender", true);
        this.patrimonios = patrimonios;
        this.config = new EtiquetaConfig();
        
        this.pnlCanvas = new PreviewPanel();
        
        initComponents();
        setupLayout();
        setupEvents();
        
        setSize(1280, 800);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        txtCabecalho = new JTextField(config.getCabecalho());
        spnX = createSpinner(0, -500, 500);
        spnY = createSpinner(0, -500, 500);
        spnWidth = createSpinner(1, 1, 500);
        spnHeight = createSpinner(1, 1, 500);
        spnFontSize = createSpinner(10, 5, 72);
        
        int w = Math.max(1, Math.min(500, config.getLargura()));
        int h = Math.max(1, Math.min(500, config.getAltura()));
        int c = Math.max(1, Math.min(10, config.getColunas()));
        
        spnPaperW = createSpinner(w, 1, 500);
        spnPaperH = createSpinner(h, 1, 500);
        spnCols = createSpinner(c, 1, 10);
        
        lblSelectedName = new JLabel("Nenhum selecionado");
        lblSelectedName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSelectedName.setForeground(COLOR_ACCENT);
        
        btnSnapGrid = new JToggleButton("Snap to Grid", true);
        btnSnapGrid.addActionListener(e -> snapToGrid = btnSnapGrid.isSelected());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // --- TOOLBAR SUPERIOR ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JButton btnZoomOut = new JButton("Zoom -");
        btnZoomOut.addActionListener(e -> { zoomScale = Math.max(1.0, zoomScale - 1.0); pnlCanvas.repaint(); });
        JButton btnZoomIn = new JButton("Zoom +");
        btnZoomIn.addActionListener(e -> { zoomScale = Math.min(15.0, zoomScale + 1.0); pnlCanvas.repaint(); });
        
        toolBar.add(new JLabel(" Ferramentas de Design: "));
        toolBar.addSeparator();
        toolBar.add(btnZoomOut);
        toolBar.add(btnZoomIn);
        toolBar.addSeparator();
        toolBar.add(btnSnapGrid);
        
        add(toolBar, BorderLayout.NORTH);

        // --- PAINEL ESQUERDO: CAMADAS ---
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBorder(new TitledBorder("Camadas de Impressão"));
        pnlLeft.setPreferredSize(new Dimension(200, 0));
        pnlLeft.setBackground(COLOR_SIDEBAR);

        pnlLeft.add(createLayerToggle("Cabeçalho", config.getCabecalho() != null, e -> { selectedElement = 0; updateInspector(); pnlCanvas.repaint(); }));
        pnlLeft.add(createLayerToggle("QR Code", config.isIncluirQRCode(), e -> { config.setIncluirQRCode(((JCheckBox)e.getSource()).isSelected()); selectedElement = 1; updateInspector(); pnlCanvas.repaint(); }));
        pnlLeft.add(createLayerToggle("Número Patrimônio", true, e -> { selectedElement = 2; updateInspector(); pnlCanvas.repaint(); }));
        pnlLeft.add(createLayerToggle("Descrição", config.isIncluirDescricao(), e -> { config.setIncluirDescricao(((JCheckBox)e.getSource()).isSelected()); selectedElement = 3; updateInspector(); pnlCanvas.repaint(); }));
        pnlLeft.add(createLayerToggle("Marca/Modelo", config.isIncluirMarcaModelo(), e -> { config.setIncluirMarcaModelo(((JCheckBox)e.getSource()).isSelected()); selectedElement = 4; updateInspector(); pnlCanvas.repaint(); }));

        // --- PAINEL DIREITO: INSPETOR ---
        JPanel pnlRight = new JPanel(new GridBagLayout());
        pnlRight.setPreferredSize(new Dimension(300, 0));
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlRight.setBackground(COLOR_SIDEBAR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        pnlRight.add(new JLabel("PROPRIEDADES DO ELEMENTO:"), gbc);
        gbc.gridy++;
        pnlRight.add(lblSelectedName, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(15, 5, 2, 5);
        pnlRight.add(new JLabel("Texto Fixo (se aplicável):"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 5, 5);
        pnlRight.add(txtCabecalho, gbc);
        txtCabecalho.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (selectedElement == 0) {
                    config.setCabecalho(txtCabecalho.getText());
                    pnlCanvas.repaint();
                }
            }
        });
        
        gbc.gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        pnlRight.add(new JLabel("Posição e Dimensão:"), gbc);
        
        JPanel pnlPos = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlPos.setOpaque(false);
        pnlPos.add(createFieldRow("X (mm):", spnX));
        pnlPos.add(createFieldRow("Y (mm):", spnY));
        pnlPos.add(createFieldRow("Larg. QR:", spnWidth));
        pnlPos.add(createFieldRow("Fonte (pt):", spnFontSize));
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 5, 5, 5);
        pnlRight.add(pnlPos, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        pnlRight.add(new JSeparator(), gbc);
        gbc.gridy++;
        pnlRight.add(new JLabel("CONFIGURAÇÃO DA PÁGINA"), gbc);
        gbc.gridy++;
        pnlRight.add(createFieldRow("Largura (mm):", spnPaperW), gbc);
        gbc.gridy++;
        pnlRight.add(createFieldRow("Altura (mm):", spnPaperH), gbc);
        gbc.gridy++;
        pnlRight.add(createFieldRow("Colunas:", spnCols), gbc);

        // Espaçador inferior
        gbc.gridy++;
        gbc.weighty = 1.0;
        pnlRight.add(new JPanel() {{ setOpaque(false); }}, gbc);

        // --- ÁREA CENTRAL: CANVAS ---
        JScrollPane scrollCanvas = new JScrollPane(pnlCanvas);
        scrollCanvas.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        scrollCanvas.setBackground(COLOR_WORKSPACE);
        scrollCanvas.getViewport().setBackground(COLOR_WORKSPACE);

        // Montagem final
        JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCanvas, pnlRight);
        splitRight.setDividerLocation(800);
        splitRight.setResizeWeight(1.0);
        splitRight.setBorder(null);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlLeft, splitRight);
        splitMain.setDividerLocation(200);
        splitMain.setBorder(null);

        add(splitMain, BorderLayout.CENTER);

        // --- RODAPÉ ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        JButton btnSalvar = new JButton("Salvar Template");
        btnSalvar.addActionListener(e -> { config.save(); JOptionPane.showMessageDialog(this, "Template salvo!"); });
        
        JButton btnImprimir = new JButton("Imprimir Lote");
        btnImprimir.setBackground(COLOR_ACCENT);
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(e -> imprimirLote());

        pnlFooter.add(btnSalvar);
        pnlFooter.add(btnImprimir);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && selectedElement != -1 && pnlCanvas.isFocusOwner()) {
                int code = e.getKeyCode();
                int dx = 0, dy = 0;
                if (code == KeyEvent.VK_LEFT) dx = -1;
                if (code == KeyEvent.VK_RIGHT) dx = 1;
                if (code == KeyEvent.VK_UP) dy = -1;
                if (code == KeyEvent.VK_DOWN) dy = 1;
                
                if (dx != 0 || dy != 0) {
                    moveElement(selectedElement, dx, dy);
                    updateInspector();
                    pnlCanvas.repaint();
                    return true;
                }
            }
            return false;
        });

        spnX.addChangeListener(e -> { updateConfigFromInspector(); pnlCanvas.repaint(); });
        spnY.addChangeListener(e -> { updateConfigFromInspector(); pnlCanvas.repaint(); });
        spnWidth.addChangeListener(e -> { updateConfigFromInspector(); pnlCanvas.repaint(); });
        spnFontSize.addChangeListener(e -> { updateConfigFromInspector(); pnlCanvas.repaint(); });
        spnPaperW.addChangeListener(e -> { config.setLargura((int)spnPaperW.getValue()); pnlCanvas.repaint(); });
        spnPaperH.addChangeListener(e -> { config.setAltura((int)spnPaperH.getValue()); pnlCanvas.repaint(); });
        spnCols.addChangeListener(e -> { config.setColunas((int)spnCols.getValue()); pnlCanvas.repaint(); });
    }

    private void updateInspector() {
        switch(selectedElement) {
            case 0: 
                lblSelectedName.setText("CABEÇALHO");
                spnX.setValue(config.getxCabecalho());
                spnY.setValue(config.getyCabecalho());
                spnWidth.setValue(config.getLargura());
                spnFontSize.setValue(config.getTamanhoFonteCabecalho());
                txtCabecalho.setEnabled(true);
                spnFontSize.setEnabled(true);
                break;
            case 1:
                lblSelectedName.setText("QR CODE");
                spnX.setValue(config.getxQRCode());
                spnY.setValue(config.getyQRCode());
                spnWidth.setValue(config.getTamanhoQRCode());
                txtCabecalho.setEnabled(false);
                spnFontSize.setEnabled(false);
                break;
            case 2:
                lblSelectedName.setText("NÚMERO PATRIMÔNIO");
                spnX.setValue(config.getxNumero());
                spnY.setValue(config.getyNumero());
                spnFontSize.setValue(config.getTamanhoFonteNumero());
                txtCabecalho.setEnabled(false);
                spnFontSize.setEnabled(true);
                break;
            case 3:
                lblSelectedName.setText("DESCRIÇÃO");
                spnX.setValue(config.getxDescricao());
                spnY.setValue(config.getyDescricao());
                spnFontSize.setValue(config.getTamanhoFonteDescricao());
                txtCabecalho.setEnabled(false);
                spnFontSize.setEnabled(true);
                break;
            case 4:
                lblSelectedName.setText("MARCA/MODELO");
                spnX.setValue(config.getxMarcaModelo());
                spnY.setValue(config.getyMarcaModelo());
                spnFontSize.setValue(config.getTamanhoFonteMarcaModelo());
                txtCabecalho.setEnabled(false);
                spnFontSize.setEnabled(true);
                break;
            default:
                lblSelectedName.setText("NENHUM SELECIONADO");
                txtCabecalho.setEnabled(false);
                spnFontSize.setEnabled(false);
        }
    }

    private void updateConfigFromInspector() {
        int valX = (int)spnX.getValue();
        int valY = (int)spnY.getValue();
        int valW = (int)spnWidth.getValue();
        int valFont = (int)spnFontSize.getValue();
        
        switch(selectedElement) {
            case 0: config.setxCabecalho(valX); config.setyCabecalho(valY); config.setTamanhoFonteCabecalho(valFont); break;
            case 1: config.setxQRCode(valX); config.setyQRCode(valY); config.setTamanhoQRCode(valW); break;
            case 2: config.setxNumero(valX); config.setyNumero(valY); config.setTamanhoFonteNumero(valFont); break;
            case 3: config.setxDescricao(valX); config.setyDescricao(valY); config.setTamanhoFonteDescricao(valFont); break;
            case 4: config.setxMarcaModelo(valX); config.setyMarcaModelo(valY); config.setTamanhoFonteMarcaModelo(valFont); break;
        }
    }

    private void moveElement(int element, int dx, int dy) {
        switch (element) {
            case 0: config.setxCabecalho(config.getxCabecalho() + dx); config.setyCabecalho(config.getyCabecalho() + dy); break;
            case 1: config.setxQRCode(config.getxQRCode() + dx); config.setyQRCode(config.getyQRCode() + dy); break;
            case 2: config.setxNumero(config.getxNumero() + dx); config.setyNumero(config.getyNumero() + dy); break;
            case 3: config.setxDescricao(config.getxDescricao() + dx); config.setyDescricao(config.getyDescricao() + dy); break;
            case 4: config.setxMarcaModelo(config.getxMarcaModelo() + dx); config.setyMarcaModelo(config.getyMarcaModelo() + dy); break;
        }
    }

    private void resizeElement(int element, int dw, int dh) {
        if (element == 1) { // Só QR Code suporta resize interativo por enquanto
            int newSize = Math.max(5, config.getTamanhoQRCode() + dw);
            config.setTamanhoQRCode(newSize);
        }
    }

    private void imprimirLote() {
        try {
            EtiquetaPrinter printer = new EtiquetaPrinter(config);
            printer.imprimir(patrimonios);
            impressaoConfirmada = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    // --- HELPERS ---
    private JSpinner createSpinner(int val, int min, int max) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, 1));
    }

    private JPanel createFieldRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(80, 20));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JCheckBox createLayerToggle(String name, boolean sel, ActionListener l) {
        JCheckBox cb = new JCheckBox(name, sel);
        cb.setOpaque(false);
        cb.addActionListener(l);
        cb.setBorder(new EmptyBorder(8, 10, 8, 10));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }

    // --- CANVAS PANEL ---
    private class PreviewPanel extends JPanel {
        private static final double MM_TO_POINTS = 2.83465;

        public PreviewPanel() {
            setBackground(COLOR_WORKSPACE);
            setFocusable(true);
            
            MouseAdapter dragHandler = new MouseAdapter() {
                private Point lastPoint;
                private int startX, startY;
                
                @Override
                public void mousePressed(MouseEvent e) { 
                    requestFocusInWindow();
                    lastPoint = e.getPoint();
                    startX = config.getxQRCode(); // Guardar posições iniciais se precisar
                    
                    int target = detectElementAt(e.getPoint());
                    if (target >= 100) { // Clicou no resize handle (100 + elemento)
                        dragMode = 1;
                        selectedElement = target - 100;
                    } else {
                        dragMode = 0;
                        selectedElement = target;
                    }
                    updateInspector();
                    repaint();
                }
                
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selectedElement == -1) return;
                    
                    int dx = (int)((e.getX() - lastPoint.x) / zoomScale);
                    int dy = (int)((e.getY() - lastPoint.y) / zoomScale);
                    
                    if (dx != 0 || dy != 0) {
                        if (dragMode == 0) {
                            moveElement(selectedElement, dx, dy);
                        } else if (dragMode == 1) {
                            resizeElement(selectedElement, dx, dy);
                        }
                        
                        // Snap logic aplicado na coordenada absoluta ao soltar ou mover
                        if (snapToGrid) {
                            snapElement(selectedElement);
                        }
                        
                        lastPoint = e.getPoint();
                        updateInspector();
                        repaint();
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (snapToGrid && selectedElement != -1) {
                        snapElement(selectedElement);
                        updateInspector();
                        repaint();
                    }
                }
            };
            addMouseListener(dragHandler);
            addMouseMotionListener(dragHandler);
        }
        
        private void snapElement(int element) {
            switch (element) {
                case 0: config.setxCabecalho(Math.round(config.getxCabecalho())); config.setyCabecalho(Math.round(config.getyCabecalho())); break;
                case 1: config.setxQRCode(Math.round(config.getxQRCode())); config.setyQRCode(Math.round(config.getyQRCode())); break;
                case 2: config.setxNumero(Math.round(config.getxNumero())); config.setyNumero(Math.round(config.getyNumero())); break;
                case 3: config.setxDescricao(Math.round(config.getxDescricao())); config.setyDescricao(Math.round(config.getyDescricao())); break;
                case 4: config.setxMarcaModelo(Math.round(config.getxMarcaModelo())); config.setyMarcaModelo(Math.round(config.getyMarcaModelo())); break;
            }
        }

        private int detectElementAt(Point p) {
            int ox = Math.max(40, (getWidth() - (int)(config.getLargura() * zoomScale)) / 2);
            int oy = 60;
            double mx = (p.x - ox) / zoomScale;
            double my = (p.y - oy) / zoomScale;
            
            // Checar handles de resize primeiro
            if (isHit(mx, my, config.getxQRCode() + config.getTamanhoQRCode() - 2, config.getyQRCode() + 2, 4, 4)) return 101; // Resize QR
            
            // Checar corpos
            if (config.isIncluirQRCode() && isHit(mx, my, config.getxQRCode(), config.getyQRCode(), config.getTamanhoQRCode(), config.getTamanhoQRCode())) return 1;
            if (isHit(mx, my, config.getxCabecalho(), config.getyCabecalho(), config.getLargura(), 5)) return 0;
            if (isHit(mx, my, config.getxNumero(), config.getyNumero(), 30, 5)) return 2;
            if (config.isIncluirDescricao() && isHit(mx, my, config.getxDescricao(), config.getyDescricao(), 30, 5)) return 3;
            if (config.isIncluirMarcaModelo() && isHit(mx, my, config.getxMarcaModelo(), config.getyMarcaModelo(), 30, 5)) return 4;
            
            return -1;
        }

        private boolean isHit(double mx, double my, double ex, double ey, double ew, double eh) {
            return mx >= ex && mx <= ex + ew && my >= ey - eh && my <= ey;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = (int)(config.getLargura() * zoomScale);
            int h = (int)(config.getAltura() * zoomScale);
            int ox = Math.max(40, (getWidth() - w) / 2);
            int oy = 60;

            // --- DESENHAR RÉGUAS E GRID ---
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            for (int i = 0; i <= config.getLargura(); i += 5) {
                int x = ox + (int)(i * zoomScale);
                g2d.drawLine(x, oy - 15, x, oy - 5);
                if (i % 10 == 0) g2d.drawString(String.valueOf(i), x - 5, oy - 18);
            }
            for (int i = 0; i <= config.getAltura(); i += 5) {
                int y = oy + (int)(i * zoomScale);
                g2d.drawLine(ox - 15, y, ox - 5, y);
                if (i % 10 == 0) g2d.drawString(String.valueOf(i), ox - 35, y + 5);
            }

            if (snapToGrid) {
                g2d.setColor(new Color(60, 60, 60, 100));
                for(int i=0; i<w; i+=zoomScale) g2d.drawLine(ox+i, oy, ox+i, oy+h);
                for(int i=0; i<h; i+=zoomScale) g2d.drawLine(ox, oy+i, ox+w, oy+i);
            }

            // --- PREVIEW DE LOTE ---
            if (config.getColunas() > 1) {
                int ox2 = ox + w + (int)(3 * zoomScale);
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRect(ox2, oy, w, h);
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawRect(ox2, oy, w, h);
            }

            // Etiqueta Principal
            g2d.setColor(Color.BLACK);
            g2d.fillRect(ox + 5, oy + 5, w, h); // Sombra
            g2d.setColor(Color.WHITE);
            g2d.fillRect(ox, oy, w, h); // Fundo
            
            // Renderização do Conteúdo
            Graphics2D gLabel = (Graphics2D) g2d.create();
            gLabel.translate(ox, oy);
            gLabel.scale(zoomScale / MM_TO_POINTS, zoomScale / MM_TO_POINTS);
            
            EtiquetaPrinter printer = new EtiquetaPrinter(config);
            Patrimonio p = patrimonios.isEmpty() ? new Patrimonio() : patrimonios.get(0);
            if (p.getNumeroPatrimonio() == null || p.getNumeroPatrimonio().isEmpty()) {
                p.setNumeroPatrimonio("000123");
                p.setDescricao("Item de Demonstração");
                p.setMarca("MARCA");
                p.setModelo("MODELO XYZ");
            }
            
            printer.desenharEtiqueta(gLabel, p, 0, 0);
            gLabel.dispose();

            // --- SELEÇÃO WYSIWYG E HANDLES ---
            if (selectedElement != -1) {
                g2d.setColor(COLOR_ACCENT);
                g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[]{4f, 4f}, 0.0f));
                
                int sx=0, sy=0, sw=0, sh=0;
                if(selectedElement == 0) { sx=config.getxCabecalho(); sy=config.getyCabecalho(); sw=config.getLargura(); sh=5; }
                else if(selectedElement == 1) { sx=config.getxQRCode(); sy=config.getyQRCode(); sw=config.getTamanhoQRCode(); sh=config.getTamanhoQRCode(); }
                else if(selectedElement == 2) { sx=config.getxNumero(); sy=config.getyNumero(); sw=30; sh=5; }
                else if(selectedElement == 3) { sx=config.getxDescricao(); sy=config.getyDescricao(); sw=30; sh=5; }
                else if(selectedElement == 4) { sx=config.getxMarcaModelo(); sy=config.getyMarcaModelo(); sw=30; sh=5; }
                
                int bx = (int)(ox + sx*zoomScale);
                int by = (int)(oy + (sy-sh)*zoomScale);
                int bw = (int)(sw*zoomScale);
                int bh = (int)(sh*zoomScale);
                
                g2d.drawRect(bx, by, bw, bh);
                
                // Handles de Resize (só no QR code por enquanto)
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.0f));
                if (selectedElement == 1) {
                    g2d.fillRect(bx+bw-4, by+bh-4, 8, 8); // Bottom-Right
                    g2d.setColor(COLOR_ACCENT);
                    g2d.drawRect(bx+bw-4, by+bh-4, 8, 8);
                }
            }

            g2d.dispose();
            setPreferredSize(new Dimension(ox + w * 2 + 100, h + 200));
            revalidate();
        }
    }
}