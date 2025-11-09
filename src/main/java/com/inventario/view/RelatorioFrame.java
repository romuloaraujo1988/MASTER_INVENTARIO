package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.inventario.service.ServiceFactory;
import com.inventario.service.RelatorioService;
import com.inventario.service.InventarioService;
import com.inventario.service.ResponsavelService;
import com.inventario.service.SetorService;
import com.inventario.service.PatrimonioService;
import com.inventario.service.SalaService;
import com.inventario.model.Responsavel;
import com.inventario.model.Setor;
import com.inventario.model.Inventario;
import com.inventario.model.Patrimonio;
import com.inventario.model.Sala;
import com.inventario.util.RelatorioExcelGenerator;
import com.inventario.util.SoundNotification;
import com.inventario.view.ui.ButtonStyleFactory;
import java.text.NumberFormat;
import java.util.Locale;

// === IMPORTS JFREECHART - FASE 2 ===
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Tela para geração e visualização de relatórios
 */
public class RelatorioFrame extends JFrame {

    // Componentes da interface
    private JComboBox<String> comboTipoRelatorio;
    private JSpinner spinnerDataInicio;
    private JSpinner spinnerDataFim;
    private JComboBox<String> comboSetor;
    private JComboBox<String> comboResponsavel;
    private JComboBox<String> comboStatus;
    private JComboBox<String> comboInventario;
    private JCheckBox checkIncluirImagens;
    private JCheckBox checkAgruparPorSetor;
    
    // === FILTROS INTELIGENTES AVANÇADOS - FASE 2 ===
    private JSpinner spinnerValorMinimo;
    private JSpinner spinnerValorMaximo;
    private JComboBox<String> comboSala;
    private JComboBox<String> comboEstadoConservacao;
    private JComboBox<String> comboOperadorLogico;
    private JCheckBox checkFiltrosAvancados;
    private JCheckBox checkExcluirSemValor;

    // === BUSCA EM TEMPO REAL - FASE 2 ===
    private JTextField campoBusca;
    private JButton btnLimparBusca;
    private JLabel labelResultadosBusca;

    // === COMPONENTES GRÁFICOS - FASE 2 ===
    private ChartPanel painelGraficoPizza;
    private ChartPanel painelGraficoBarras;
    private JComboBox<String> comboTipoGrafico;

    // Services
    private final RelatorioService relatorioService;
    private final InventarioService inventarioService;
    private final ResponsavelService responsavelService;
    private final SetorService setorService;
    private final PatrimonioService patrimonioService;
    private final SalaService salaService;
    private RelatorioExcelGenerator excelGenerator;

    private JTable tabelaPreview;
    private DefaultTableModel modeloTabela;
    private JTextArea areaResumo;

    private JButton btnGerar, btnExportar, btnImprimir, btnLimpar;
    private JProgressBar progressBar;
    private JLabel labelStatus;

    public RelatorioFrame() {
        super("Relatórios do Sistema");
        
        // Inicializar services via ServiceFactory
        this.relatorioService = ServiceFactory.getRelatorioService();
        this.inventarioService = ServiceFactory.getInventarioService();
        this.responsavelService = ServiceFactory.getResponsavelService();
        this.setorService = ServiceFactory.getSetorService();
        this.patrimonioService = ServiceFactory.getPatrimonioService();
        this.salaService = ServiceFactory.getSalaService();
        this.excelGenerator = new RelatorioExcelGenerator();
        initComponents();
        aplicarEstiloModerno();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Painel superior com filtros
        JPanel painelFiltros = criarPainelFiltros();
        add(painelFiltros, BorderLayout.NORTH);

        // Painel central com abas
        JTabbedPane abas = new JTabbedPane();

        // Aba Preview
        JPanel abaPreview = criarAbaPreview();
        abas.addTab("Preview", abaPreview);

        // Aba Resumo
        JPanel abaResumo = criarAbaResumo();
        abas.addTab("Resumo", abaResumo);

        // === ABA GRÁFICOS - FASE 2 ===
        JPanel abaGraficos = criarAbaGraficos();
        abas.addTab("Gráficos", abaGraficos);

        add(abas, BorderLayout.CENTER);

        // Painel inferior com botões e status
        JPanel painelInferior = criarPainelInferior();
        add(painelInferior, BorderLayout.SOUTH);

        // Configurar eventos
        configurarEventos();
        
        // Aplicar filtros iniciais baseado no tipo de relatório selecionado
        // (chamado após todos os componentes serem criados)
        atualizarFiltros();

        setSize(1400, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private JPanel criarPainelFiltros() {
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        "Filtros do Relatório",
                        0, 0,
                        new Font(Font.SANS_SERIF, Font.BOLD, 12),
                        new Color(60, 60, 60)),
                new EmptyBorder(10, 15, 15, 15)));

        // Criar painéis organizados
        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.setBackground(Color.WHITE);
        
        painelSuperior.add(criarPainelFiltrosBasicos(), BorderLayout.NORTH);
        painelSuperior.add(criarPainelSelecao(), BorderLayout.CENTER);
        
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBackground(Color.WHITE);
        
        painelInferior.add(criarPainelOpcoes(), BorderLayout.NORTH);
        painelInferior.add(criarPainelFiltrosAvancados(), BorderLayout.CENTER);
        
        painelPrincipal.add(painelSuperior, BorderLayout.NORTH);
        painelPrincipal.add(painelInferior, BorderLayout.CENTER);
        
        return painelPrincipal;
    }

    private JPanel criarPainelFiltrosBasicos() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createTitledBorder("Filtros Básicos"));

        // Tipo de relatório - DESTAQUE PRINCIPAL
        JLabel labelTipo = new JLabel("Tipo de Relatório:");
        labelTipo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        labelTipo.setForeground(new Color(0, 100, 200));
        painel.add(labelTipo);
        
        comboTipoRelatorio = new JComboBox<>(new String[] {
                "Relatório Geral de Patrimônio",
                "Itens Encontrados",
                "Itens Não Encontrados",
                "Itens Sem Plaqueta de Patrimônio",
                "Relatório por Responsável",
                "Itens Não Coletados",
                "Relatório de Divergências",
                "Estatísticas do Inventário",
                "--- RELATÓRIOS AVANÇADOS ---",
                "Relatório Avançado por Setor",
                "Relatório Avançado por Responsável",
                "Relatório Avançado por Período",
                "Estatísticas Avançadas por Setor",
                "Relatório Consolidado Executivo"
        });
        comboTipoRelatorio.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        comboTipoRelatorio.setPreferredSize(new Dimension(300, 30));
        comboTipoRelatorio.setBackground(new Color(240, 248, 255));
        comboTipoRelatorio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 200), 2),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        painel.add(comboTipoRelatorio);

        // Separador visual
        painel.add(new JLabel("  |  "));

        // Data início
        painel.add(new JLabel("Data Início:"));
        spinnerDataInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerDataInicio, "dd/MM/yyyy");
        spinnerDataInicio.setEditor(editorInicio);
        spinnerDataInicio.setPreferredSize(new Dimension(120, 25));
        painel.add(spinnerDataInicio);

        // Data fim
        painel.add(new JLabel("Data Fim:"));
        spinnerDataFim = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFim = new JSpinner.DateEditor(spinnerDataFim, "dd/MM/yyyy");
        spinnerDataFim.setEditor(editorFim);
        spinnerDataFim.setValue(new Date());
        spinnerDataFim.setPreferredSize(new Dimension(120, 25));
        painel.add(spinnerDataFim);

        return painel;
    }

    private JPanel criarPainelSelecao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createTitledBorder("Seleção"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Setor
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Setor:"), gbc);
        
        gbc.gridx = 1;
        comboSetor = new JComboBox<>();
        comboSetor.setPreferredSize(new Dimension(200, 25));
        painel.add(comboSetor, gbc);

        // Responsável
        gbc.gridx = 2;
        painel.add(new JLabel("Responsável:"), gbc);
        
        gbc.gridx = 3;
        comboResponsavel = new JComboBox<>();
        comboResponsavel.setPreferredSize(new Dimension(200, 25));
        painel.add(comboResponsavel, gbc);

        // Inventário - DESTAQUE ESPECIAL
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel labelInventario = new JLabel("Inventário:");
        labelInventario.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        labelInventario.setForeground(new Color(200, 0, 0));
        painel.add(labelInventario, gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        comboInventario = new JComboBox<>();
        comboInventario.setPreferredSize(new Dimension(400, 30));
        comboInventario.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        comboInventario.setBackground(new Color(255, 248, 248));
        comboInventario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 0, 0), 2),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        painel.add(comboInventario, gbc);
        carregarInventarios();

        // Reset gridwidth
        gbc.gridwidth = 1;

        // Situação
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(new JLabel("Situação:"), gbc);
        
        gbc.gridx = 1;
        comboStatus = new JComboBox<>(new String[] {
                "Todos", "Encontrado", "Não Encontrado", "Não Coletado"
        });
        comboStatus.setPreferredSize(new Dimension(150, 25));
        painel.add(comboStatus, gbc);

        return painel;
    }

    private JPanel criarPainelOpcoes() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createTitledBorder("Opções"));

        // Incluir imagens no relatório
        checkIncluirImagens = new JCheckBox("Incluir imagens no relatório");
        checkIncluirImagens.setBackground(Color.WHITE);
        checkIncluirImagens.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        painel.add(checkIncluirImagens);

        // Agrupar por setor
        checkAgruparPorSetor = new JCheckBox("Agrupar por setor");
        checkAgruparPorSetor.setBackground(Color.WHITE);
        checkAgruparPorSetor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        painel.add(checkAgruparPorSetor);

        return painel;
    }

    private JPanel criarPainelFiltrosAvancados() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(Color.WHITE);
        painel.setBorder(BorderFactory.createTitledBorder("Filtros Avançados"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Primeira linha - Filtros de Valor
        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Valor Mínimo (R$):"), gbc);
        
        gbc.gridx = 1;
        spinnerValorMinimo = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999999999.0, 100.0));
        JSpinner.NumberEditor editorMin = new JSpinner.NumberEditor(spinnerValorMinimo, "#,##0.00");
        spinnerValorMinimo.setEditor(editorMin);
        spinnerValorMinimo.setPreferredSize(new Dimension(120, 25));
        painel.add(spinnerValorMinimo, gbc);

        gbc.gridx = 2;
        painel.add(new JLabel("Valor Máximo (R$):"), gbc);
        
        gbc.gridx = 3;
        spinnerValorMaximo = new JSpinner(new SpinnerNumberModel(999999999.0, 0.0, 999999999.0, 100.0));
        JSpinner.NumberEditor editorMax = new JSpinner.NumberEditor(spinnerValorMaximo, "#,##0.00");
        spinnerValorMaximo.setEditor(editorMax);
        spinnerValorMaximo.setPreferredSize(new Dimension(120, 25));
        painel.add(spinnerValorMaximo, gbc);

        // Segunda linha - Filtros de Localização e Estado
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Sala:"), gbc);
        
        gbc.gridx = 1;
        comboSala = new JComboBox<>();
        comboSala.setPreferredSize(new Dimension(150, 25));
        painel.add(comboSala, gbc);

        gbc.gridx = 2;
        painel.add(new JLabel("Estado de Conservação:"), gbc);
        
        gbc.gridx = 3;
        comboEstadoConservacao = new JComboBox<>(new String[] { 
            "Todos", "BOM", "REGULAR", "RUIM", "PÉSSIMO", "NOVO", "USADO" 
        });
        comboEstadoConservacao.setPreferredSize(new Dimension(150, 25));
        painel.add(comboEstadoConservacao, gbc);

        // Terceira linha - Checkboxes de filtros avançados
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        checkFiltrosAvancados = new JCheckBox("Ativar filtros avançados");
        checkFiltrosAvancados.setBackground(Color.WHITE);
        checkFiltrosAvancados.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        painel.add(checkFiltrosAvancados, gbc);

        // Operador lógico e excluir sem valor
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        painel.add(new JLabel("Operador:"), gbc);
        
        gbc.gridx = 3;
        comboOperadorLogico = new JComboBox<>(new String[] { "E (AND)", "OU (OR)" });
        comboOperadorLogico.setPreferredSize(new Dimension(100, 25));
        painel.add(comboOperadorLogico, gbc);

        // Quarta linha - Checkbox adicional
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        checkExcluirSemValor = new JCheckBox("Excluir itens sem valor");
        checkExcluirSemValor.setBackground(Color.WHITE);
        checkExcluirSemValor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        painel.add(checkExcluirSemValor, gbc);

        // Inicializar filtros avançados desabilitados
        spinnerValorMinimo.setEnabled(false);
        spinnerValorMaximo.setEnabled(false);
        comboSala.setEnabled(false);
        comboEstadoConservacao.setEnabled(false);
        comboOperadorLogico.setEnabled(false);
        checkExcluirSemValor.setEnabled(false);

        // Carregar dados
        carregarSetores();
        carregarResponsaveis();
        carregarSalas();

        return painel;
    }

    // Lista para armazenar os inventários carregados
    private List<Inventario> inventariosCarregados = new ArrayList<>();

    private void carregarInventarios() {
        try {
            comboInventario.removeAllItems();
            
            // Carregar todos os inventários do banco de dados
            inventariosCarregados = inventarioService.listarInventarios();

            // Filtrar inventários ativos primeiro
            List<Inventario> inventariosAtivos = inventariosCarregados.stream()
                .filter(inv -> "ATIVO".equals(inv.getStatusInventario()))
                .collect(Collectors.toList());

            // Se não houver inventários ativos, usar todos
            List<Inventario> inventariosParaExibir = inventariosAtivos.isEmpty() ? 
                inventariosCarregados : inventariosAtivos;

            if (inventariosParaExibir.isEmpty()) {
                comboInventario.addItem("Nenhum inventário disponível");
                return;
            }

            // Se houver apenas um inventário, selecioná-lo automaticamente
            if (inventariosParaExibir.size() == 1) {
                Inventario inventario = inventariosParaExibir.get(0);
                String itemText = String.format("%s (%s) - %s",
                        inventario.getNome(),
                        inventario.getAno() != null ? inventario.getAno().toString() : "S/A",
                        inventario.getStatusInventario());
                comboInventario.addItem(itemText);
                comboInventario.setSelectedIndex(0);
                
                // Atualizar a lista para corresponder ao item selecionado
                inventariosCarregados.clear();
                inventariosCarregados.add(inventario);
            } else {
                // Múltiplos inventários - adicionar opção de seleção
                comboInventario.addItem("Selecione um inventário...");
                
                for (Inventario inventario : inventariosParaExibir) {
                    String itemText = String.format("%s (%s) - %s",
                            inventario.getNome(),
                            inventario.getAno() != null ? inventario.getAno().toString() : "S/A",
                            inventario.getStatusInventario());
                    comboInventario.addItem(itemText);
                }
                
                // Atualizar a lista para corresponder aos itens exibidos
                inventariosCarregados = inventariosParaExibir;
                
                // Selecionar o primeiro inventário ativo por padrão
                if (comboInventario.getItemCount() > 1) {
                    comboInventario.setSelectedIndex(1);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar inventários: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);

            // Em caso de erro, adicionar opção padrão
            comboInventario.removeAllItems();
            comboInventario.addItem("Inventário 2024 - ATIVO (Padrão)");
            inventariosCarregados.clear();
        }
    }

    /**
     * Obtém o ID do inventário selecionado no combo
     * 
     * @return ID do inventário selecionado ou -1 se nenhum estiver selecionado
     */
    private int obterIdInventarioSelecionado() {
        int selectedIndex = comboInventario.getSelectedIndex();

        // Se lista vazia ou nenhum item selecionado
        if (selectedIndex < 0 || inventariosCarregados.isEmpty()) {
            return -1;
        }

        // Verificar se o item selecionado é uma mensagem de erro
        String selectedItem = (String) comboInventario.getSelectedItem();
        if (selectedItem != null && selectedItem.contains("Nenhum inventário disponível")) {
            return -1;
        }

        // Se há apenas um inventário (seleção automática)
        if (inventariosCarregados.size() == 1 && selectedIndex == 0) {
            return inventariosCarregados.get(0).getId();
        }

        // Se há múltiplos inventários e "Selecione..." está selecionado
        if (selectedIndex == 0 && selectedItem != null && selectedItem.contains("Selecione")) {
            return -1;
        }

        // Calcular índice correto
        int inventarioIndex = inventariosCarregados.size() == 1 ? 0 : selectedIndex - 1;

        if (inventarioIndex >= 0 && inventarioIndex < inventariosCarregados.size()) {
            return inventariosCarregados.get(inventarioIndex).getId();
        }

        return -1;
    }

    private void carregarSetores() {
        try {
            comboSetor.removeAllItems();
            comboSetor.addItem("Todos");

            List<Setor> setores = setorService.listarSetores();
            for (Setor setor : setores) {
                comboSetor.addItem(setor.getNome());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar setores: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            // Adicionar opções padrão em caso de erro
            comboSetor.addItem("Todos");
            comboSetor.addItem("Administração");
            comboSetor.addItem("TI");
            comboSetor.addItem("RH");
        }
    }

    private void carregarResponsaveis() {
        try {
            comboResponsavel.removeAllItems();
            comboResponsavel.addItem("Todos");

            List<Responsavel> responsaveis = responsavelService.listarResponsaveis();
            for (Responsavel responsavel : responsaveis) {
                comboResponsavel.addItem(responsavel.getNome());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar responsáveis: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            // Adicionar opções padrão em caso de erro
            comboResponsavel.addItem("Todos");
            comboResponsavel.addItem("João Silva");
            comboResponsavel.addItem("Maria Santos");
        }
    }

    private void atualizarResponsaveisPorSetor(String nomeSetor) {
        try {
            comboResponsavel.removeAllItems();
            comboResponsavel.addItem("Todos");

            // Buscar ID do setor pelo nome
            List<Setor> setores = setorService.listarSetores();
            int idSetor = -1;
            for (Setor setor : setores) {
                if (setor.getNome().equals(nomeSetor)) {
                    idSetor = setor.getId();
                    break;
                }
            }

            if (idSetor != -1) {
                List<Responsavel> responsaveis = responsavelService.listarResponsaveisPorSetor(idSetor);
                for (Responsavel responsavel : responsaveis) {
                    comboResponsavel.addItem(responsavel.getNome());
                }
            }
        } catch (Exception e) {
            // Em caso de erro, carregar todos os responsáveis
            carregarResponsaveis();
        }
    }

    private void carregarSalas() {
        try {
            comboSala.removeAllItems();
            comboSala.addItem("Todas as Salas");
            
            List<Sala> salas = salaService.listarSalas();
            for (Sala sala : salas) {
                String displayText = sala.getNumeroSala();
                if (sala.getDescricao() != null && !sala.getDescricao().trim().isEmpty()) {
                    displayText += " - " + sala.getDescricao();
                }
                comboSala.addItem(displayText);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar salas: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar salas: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel criarAbaPreview() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(245, 245, 245));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // === PAINEL DE BUSCA EM TEMPO REAL - FASE 2 ===
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBusca.setBackground(new Color(245, 245, 245));
        
        JLabel labelBusca = new JLabel("Buscar na tabela:");
        labelBusca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        painelBusca.add(labelBusca);
        
        campoBusca = new JTextField(30);
        campoBusca.setToolTipText("Digite para filtrar os resultados em tempo real");
        campoBusca.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        painelBusca.add(campoBusca);
        
        btnLimparBusca = ButtonStyleFactory.createSecondaryButton("Limpar");
        btnLimparBusca.setToolTipText("Limpar campo de busca");
        btnLimparBusca.setPreferredSize(new Dimension(70, 25));
        painelBusca.add(btnLimparBusca);
        
        labelResultadosBusca = new JLabel("0 resultados");
        labelResultadosBusca.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        labelResultadosBusca.setForeground(new Color(100, 100, 100));
        painelBusca.add(labelResultadosBusca);
        
        painel.add(painelBusca, BorderLayout.NORTH);

        // Tabela de preview
        String[] colunas = { "Número", "Descrição", "Sala", "Estado", "Setor/Local", "Responsável", "Situação",
                "Valor" };
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPreview = new JTable(modeloTabela);
        tabelaPreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPreview.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollTabela = new JScrollPane(tabelaPreview);
        scrollTabela.setBackground(Color.WHITE);
        scrollTabela.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        "Preview dos Dados",
                        0, 0,
                        new Font(Font.SANS_SERIF, Font.BOLD, 12),
                        new Color(60, 60, 60)),
                new EmptyBorder(5, 5, 5, 5)));
        painel.add(scrollTabela, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarAbaResumo() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(245, 245, 245));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        areaResumo = new JTextArea();
        areaResumo.setEditable(false);
        areaResumo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaResumo.setBackground(new Color(248, 249, 250));
        areaResumo.setForeground(new Color(60, 60, 60));
        areaResumo.setBorder(new EmptyBorder(15, 15, 15, 15));
        areaResumo.setText(
                "Nenhum relatório gerado ainda.\n\nSelecione os filtros e clique em 'Gerar Relatório' para visualizar o resumo.");

        JScrollPane scrollResumo = new JScrollPane(areaResumo);
        scrollResumo.setBackground(Color.WHITE);
        scrollResumo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        "Resumo Estatístico",
                        0, 0,
                        new Font(Font.SANS_SERIF, Font.BOLD, 12),
                        new Color(60, 60, 60)),
                new EmptyBorder(5, 5, 5, 5)));
        painel.add(scrollResumo, BorderLayout.CENTER);

        return painel;
    }

    // === MÉTODO CRIAR ABA GRÁFICOS - FASE 2 ===
    private JPanel criarAbaGraficos() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(245, 245, 245));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Painel superior com controles
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelControles.setBackground(new Color(245, 245, 245));
        
        JLabel labelTipoGrafico = new JLabel("Tipo de Gráfico:");
        labelTipoGrafico.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        painelControles.add(labelTipoGrafico);
        
        comboTipoGrafico = new JComboBox<>(new String[]{
            "Pizza - Status de Coleta",
            "Barras - Itens por Setor",
            "Barras - Itens por Responsável",
            "Pizza - Estado de Conservação"
        });
        comboTipoGrafico.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        comboTipoGrafico.setPreferredSize(new Dimension(250, 25));
        painelControles.add(comboTipoGrafico);
        
        painel.add(painelControles, BorderLayout.NORTH);

        // Painel central para os gráficos
        JPanel painelGraficos = new JPanel(new GridLayout(1, 2, 10, 10));
        painelGraficos.setBackground(new Color(245, 245, 245));
        
        // Criar gráficos iniciais vazios
        painelGraficoPizza = criarGraficoPizzaVazio();
        painelGraficoBarras = criarGraficoBarrasVazio();
        
        painelGraficos.add(painelGraficoPizza);
        painelGraficos.add(painelGraficoBarras);
        
        painel.add(painelGraficos, BorderLayout.CENTER);

        return painel;
    }

    private ChartPanel criarGraficoPizzaVazio() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Aguardando dados...", 1);
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Status de Coleta",
            dataset,
            true,
            true,
            false
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Pizza"));
        
        return chartPanel;
    }

    private ChartPanel criarGraficoBarrasVazio() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1, "Aguardando", "dados...");
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Itens por Categoria",
            "Categoria",
            "Quantidade",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        chart.setBackgroundPaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Barras"));
        
        return chartPanel;
    }

    private JPanel criarPainelInferior() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(245, 245, 245));
        painel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Painel principal com layout horizontal
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                        "Ações e Controles",
                        0, 0,
                        new Font(Font.SANS_SERIF, Font.BOLD, 12),
                        new Color(60, 60, 60)),
                new EmptyBorder(15, 20, 15, 20)));

        // === PAINEL ESQUERDO - AÇÕES PRINCIPAIS ===
        JPanel painelAcoesPrincipais = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        painelAcoesPrincipais.setBackground(Color.WHITE);
        painelAcoesPrincipais.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 200), 1),
                "Ações Principais",
                0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 11),
                new Color(0, 100, 200)));

        btnGerar = ButtonStyleFactory.createPrimaryButton("📊 Gerar Relatório");
        btnGerar.setPreferredSize(new Dimension(160, 40));
        btnGerar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        btnLimpar = ButtonStyleFactory.createWarningButton("🗑️ Limpar");
        btnLimpar.setPreferredSize(new Dimension(120, 40));

        painelAcoesPrincipais.add(btnGerar);
        painelAcoesPrincipais.add(btnLimpar);

        // === PAINEL CENTRO - EXPORTAÇÃO E IMPRESSÃO ===
        JPanel painelExportacao = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelExportacao.setBackground(Color.WHITE);
        painelExportacao.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 0), 1),
                "Exportação e Impressão",
                0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 11),
                new Color(0, 150, 0)));

        btnExportar = ButtonStyleFactory.createSecondaryButton("📄 PDF");
        btnExportar.setEnabled(false);
        btnExportar.setPreferredSize(new Dimension(100, 35));

        btnImprimir = ButtonStyleFactory.createSecondaryButton("🖨️ Imprimir");
        btnImprimir.setEnabled(false);
        btnImprimir.setPreferredSize(new Dimension(110, 35));

        JButton btnExportarAtual = ButtonStyleFactory.createInfoButton("📊 Excel Atual");
        btnExportarAtual.setPreferredSize(new Dimension(120, 35));
        btnExportarAtual.addActionListener(e -> exportarRelatorioAtual());

        JButton btnExportarGeral = ButtonStyleFactory.createInfoButton("📋 Excel Geral");
        btnExportarGeral.setPreferredSize(new Dimension(120, 35));
        btnExportarGeral.addActionListener(e -> exportarRelatorioGeral());

        painelExportacao.add(btnExportar);
        painelExportacao.add(btnImprimir);
        painelExportacao.add(new JSeparator(SwingConstants.VERTICAL));
        painelExportacao.add(btnExportarAtual);
        painelExportacao.add(btnExportarGeral);

        // === PAINEL DIREITO - ESTATÍSTICAS ===
        JPanel painelEstatisticas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        painelEstatisticas.setBackground(Color.WHITE);
        painelEstatisticas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 0, 150), 1),
                "Estatísticas",
                0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 11),
                new Color(150, 0, 150)));

        JButton btnExportarEstatisticas = ButtonStyleFactory.createInfoButton("📈 Estatísticas");
        btnExportarEstatisticas.setPreferredSize(new Dimension(130, 40));
        btnExportarEstatisticas.addActionListener(e -> exportarEstatisticas());

        painelEstatisticas.add(btnExportarEstatisticas);

        // Organizar painéis horizontalmente
        painelPrincipal.add(painelAcoesPrincipais, BorderLayout.WEST);
        painelPrincipal.add(painelExportacao, BorderLayout.CENTER);
        painelPrincipal.add(painelEstatisticas, BorderLayout.EAST);

        // === PAINEL DE STATUS ===
        JPanel painelStatus = new JPanel(new BorderLayout());
        painelStatus.setBackground(new Color(248, 249, 250));
        painelStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 15, 8, 15)));

        labelStatus = new JLabel("✅ Pronto para gerar relatório");
        labelStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        labelStatus.setForeground(new Color(60, 60, 60));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Aguardando...");
        progressBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        progressBar.setForeground(new Color(0, 120, 215));
        progressBar.setBackground(Color.WHITE);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        progressBar.setVisible(false);

        JPanel statusContainer = new JPanel(new BorderLayout(10, 0));
        statusContainer.setBackground(new Color(248, 249, 250));
        statusContainer.add(labelStatus, BorderLayout.WEST);
        statusContainer.add(progressBar, BorderLayout.CENTER);

        painelStatus.add(statusContainer, BorderLayout.CENTER);

        // Montagem final
        painel.add(painelPrincipal, BorderLayout.CENTER);
        painel.add(painelStatus, BorderLayout.SOUTH);

        return painel;
    }

    private void configurarEventos() {
        btnGerar.addActionListener(e -> gerarRelatorio());
        btnExportar.addActionListener(e -> exportarPDF());
        btnImprimir.addActionListener(e -> imprimirRelatorio());
        btnLimpar.addActionListener(e -> limparRelatorio());

        comboTipoRelatorio.addActionListener(e -> atualizarFiltros());
        
        // Atualizar status quando inventário for alterado
        comboInventario.addActionListener(e -> atualizarFiltros());

        // Atualizar responsáveis quando o setor for alterado
        comboSetor.addActionListener(e -> {
            String setorSelecionado = (String) comboSetor.getSelectedItem();
            if (setorSelecionado != null && !"Todos".equals(setorSelecionado)) {
                atualizarResponsaveisPorSetor(setorSelecionado);
            } else {
                carregarResponsaveis();
            }
        });

        // === LISTENERS DOS FILTROS AVANÇADOS - FASE 2 ===
        
        // Listener para habilitar/desabilitar filtros avançados
        checkFiltrosAvancados.addActionListener(e -> {
            boolean habilitado = checkFiltrosAvancados.isSelected();
            spinnerValorMinimo.setEnabled(habilitado);
            spinnerValorMaximo.setEnabled(habilitado);
            comboSala.setEnabled(habilitado);
            comboEstadoConservacao.setEnabled(habilitado);
            comboOperadorLogico.setEnabled(habilitado);
            checkExcluirSemValor.setEnabled(habilitado);
            
            if (habilitado) {
                labelStatus.setText("Filtros avançados habilitados");
            } else {
                labelStatus.setText("Filtros avançados desabilitados");
            }
        });

        // Listener para validar valores mínimo e máximo
        spinnerValorMinimo.addChangeListener(e -> {
            Double valorMin = (Double) spinnerValorMinimo.getValue();
            Double valorMax = (Double) spinnerValorMaximo.getValue();
            if (valorMin != null && valorMax != null && valorMin > valorMax) {
                spinnerValorMaximo.setValue(valorMin);
                labelStatus.setText("Valor máximo ajustado automaticamente");
            }
        });

        spinnerValorMaximo.addChangeListener(e -> {
            Double valorMin = (Double) spinnerValorMinimo.getValue();
            Double valorMax = (Double) spinnerValorMaximo.getValue();
            if (valorMin != null && valorMax != null && valorMax < valorMin) {
                spinnerValorMinimo.setValue(valorMax);
                labelStatus.setText("Valor mínimo ajustado automaticamente");
            }
        });

        // Listener para atualizar status quando filtros são alterados
        comboSala.addActionListener(e -> {
            if (checkFiltrosAvancados.isSelected()) {
                String sala = (String) comboSala.getSelectedItem();
                if (sala != null && !"Todas as Salas".equals(sala)) {
                    labelStatus.setText("Filtro de sala aplicado: " + sala);
                }
            }
        });

        comboEstadoConservacao.addActionListener(e -> {
            if (checkFiltrosAvancados.isSelected()) {
                String estado = (String) comboEstadoConservacao.getSelectedItem();
                if (estado != null && !"Todos".equals(estado)) {
                    labelStatus.setText("Filtro de conservação aplicado: " + estado);
                }
            }
        });

        // === LISTENERS PARA BUSCA EM TEMPO REAL - FASE 2 ===
        campoBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabelaEmTempoReal();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabelaEmTempoReal();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabelaEmTempoReal();
            }
        });

        btnLimparBusca.addActionListener(e -> {
            campoBusca.setText("");
            filtrarTabelaEmTempoReal();
        });

        // === LISTENER COMBO TIPO GRÁFICO - FASE 2 ===
        comboTipoGrafico.addActionListener(e -> {
            atualizarGraficos();
        });
    }

    private void gerarRelatorio() {
        btnGerar.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        labelStatus.setText("Gerando relatório...");

        String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();
        Date dataInicio = (Date) spinnerDataInicio.getValue();
        Date dataFim = (Date) spinnerDataFim.getValue();
        String setor = (String) comboSetor.getSelectedItem();
        String responsavel = (String) comboResponsavel.getSelectedItem();
        // Validar filtros obrigatórios para relatórios avançados
        if (!validarFiltrosRelatoriosAvancados(tipoRelatorio, setor, responsavel, dataInicio, dataFim)) {
            btnGerar.setEnabled(true);
            progressBar.setVisible(false);
            return;
        }

        // IMPORTANTE: Todos os relatórios são filtrados pelo inventário ativo do
        // sistema
        // O ID do inventário ativo deve ser obtido do contexto da aplicação

        SwingWorker<List<Map<String, Object>>, Integer> worker = new SwingWorker<List<Map<String, Object>>, Integer>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                List<Map<String, Object>> dados = new ArrayList<>();

                try {
                    // Atualizar progresso inicial
                    publish(10);

                    // Verificar se foi cancelado
                    if (isCancelled())
                        return dados;

                    publish(20);

                    // Obter ID do inventário selecionado
                    int idInventario = obterIdInventarioSelecionado();
                    
                    // Verificar se o relatório precisa de inventário
                    boolean precisaInventario = !tipoRelatorio.equals("Relatório Geral de Patrimônio") &&
                                               !tipoRelatorio.equals("Relatório por Responsável");
                    
                    if (precisaInventario && idInventario == -1) {
                        String mensagem = "Para gerar o relatório '" + tipoRelatorio + "' é necessário selecionar um inventário.\n\n";
                        
                        if (comboInventario.getItemCount() == 0 || 
                            (comboInventario.getItemCount() == 1 && 
                             comboInventario.getItemAt(0).contains("Nenhum inventário disponível"))) {
                            mensagem += "Não há inventários disponíveis no sistema.\n" +
                                       "Crie um inventário primeiro na tela de Inventários.";
                        } else {
                            mensagem += "Selecione um inventário na lista acima e tente novamente.";
                        }
                        
                        throw new IllegalStateException(mensagem);
                    }

                    System.out.println("Iniciando geração do relatório: " + tipoRelatorio + " para inventário ID: "
                            + idInventario);

                    switch (tipoRelatorio) {
                        case "Itens Encontrados":
                            publish(40);
                            System.out.println("Consultando itens encontrados...");
                            dados = relatorioService.gerarRelatorioItensEncontrados(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Itens Não Encontrados":
                            publish(40);
                            System.out.println("Consultando itens não encontrados...");
                            dados = relatorioService.gerarRelatorioItensNaoEncontrados(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Itens Sem Plaqueta de Patrimônio":
                            publish(40);
                            System.out.println("Consultando itens sem etiqueta...");
                            dados = relatorioService.gerarRelatorioItensSemEtiqueta(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Relatório por Responsável":
                            publish(40);
                            System.out.println("Consultando relatório por responsável...");
                            if (!"Todos".equals(responsavel)) {
                                // Buscar relatório específico por responsável
                                dados = relatorioService.gerarRelatorioDetalhadoPorResponsavel(idInventario, responsavel);
                            } else {
                                // Se "Todos" estiver selecionado, mostrar todos os itens
                                dados = relatorioService.gerarRelatorioItensEncontrados(idInventario);
                            }
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Itens Não Coletados":
                            publish(40);
                            System.out.println("Consultando itens não coletados...");
                            dados = relatorioService.gerarRelatorioItensNaoColetados(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Relatório de Divergências":
                            publish(40);
                            System.out.println("Consultando divergências...");
                            dados = relatorioService.gerarRelatorioDivergencias(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Estatísticas do Inventário":
                            publish(40);
                            System.out.println("Consultando estatísticas...");
                            dados = relatorioService.gerarEstatisticasGerais(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        // ========== RELATÓRIOS AVANÇADOS ==========
                        case "Relatório Avançado por Setor":
                            publish(40);
                            System.out.println("Consultando relatório avançado por setor...");
                            String setorSelecionado = "Todos".equals(setor) ? "" : setor;
                            dados = relatorioService.gerarRelatorioAvancadoPorSetor(idInventario, setorSelecionado,
                                    dataInicio, dataFim);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Relatório Avançado por Responsável":
                            publish(40);
                            System.out.println("Consultando relatório avançado por responsável...");
                            String responsavelSelecionado = "Todos".equals(responsavel) ? "" : responsavel;
                            dados = relatorioService.gerarRelatorioAvancadoPorResponsavel(idInventario,
                                    responsavelSelecionado, dataInicio, dataFim);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Relatório Avançado por Período":
                            publish(40);
                            System.out.println("Consultando relatório avançado por período...");
                            dados = relatorioService.gerarRelatorioAvancadoPorPeriodo(idInventario, dataInicio, dataFim);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Estatísticas Avançadas por Setor":
                            publish(40);
                            System.out.println("Consultando estatísticas avançadas por setor...");
                            dados = relatorioService.gerarEstatisticasAvancadasPorSetor(idInventario, dataInicio, dataFim);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "Relatório Consolidado Executivo":
                            publish(40);
                            System.out.println("Consultando relatório consolidado executivo...");
                            dados = relatorioService.gerarRelatorioConsolidado(idInventario, dataInicio, dataFim);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                            break;
                        case "--- RELATÓRIOS AVANÇADOS ---":
                            // Item separador - não fazer nada
                            JOptionPane.showMessageDialog(RelatorioFrame.this,
                                    "Por favor, selecione um tipo de relatório válido.",
                                    "Seleção Inválida", JOptionPane.WARNING_MESSAGE);
                            return new ArrayList<>();
                        default:
                            // Relatório geral completo - mostrar todos os patrimônios
                            publish(40);
                            System.out.println("Consultando relatório geral completo...");
                            dados = relatorioService.gerarRelatorioGeralCompleto(idInventario);
                            System.out.println(
                                    "Consulta concluída. Itens encontrados: " + (dados != null ? dados.size() : 0));
                    }

                    // Verificar se foi cancelado após a consulta
                    if (isCancelled()) {
                        System.out.println("Operação cancelada pelo usuário.");
                        return dados;
                    }

                    System.out.println("Atualizando progresso para 80%...");
                    publish(80);

                    // Pequena pausa para mostrar progresso
                    System.out.println("Aguardando 200ms antes de finalizar...");
                    Thread.sleep(200);

                    System.out.println("Finalizando relatório - 100%");
                    publish(100);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Erro ao gerar relatório: " + e.getMessage());

                    // Em caso de erro, usar dados de exemplo
                    publish(60);
                    try {
                        dados = gerarDadosExemplo();
                        publish(90);
                    } catch (Exception ex) {
                        System.err.println("Erro ao gerar dados de exemplo: " + ex.getMessage());
                        // Se até os dados de exemplo falharem, retornar lista vazia
                        dados = new ArrayList<>();
                    }
                }

                return dados;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progresso = chunks.get(chunks.size() - 1);
                progressBar.setValue(progresso);
                progressBar.setString(progresso + "%");
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        labelStatus.setText("Geração de relatório cancelada");
                        return;
                    }

                    List<Map<String, Object>> dados = get();

                    if (dados != null && !dados.isEmpty()) {
                        // === APLICAR FILTROS AVANÇADOS - FASE 2 ===
                        List<Map<String, Object>> dadosFiltrados = aplicarFiltrosAvancados(dados);
                        
                        preencherTabelaComDados(dadosFiltrados);
                        gerarResumoEstatistico(dadosFiltrados);

                        btnExportar.setEnabled(true);
                        btnImprimir.setEnabled(true);
                        
                        // Atualizar status com informações dos filtros
                        String statusMsg = "Relatório gerado com sucesso! (" + dadosFiltrados.size() + " itens";
                        if (checkFiltrosAvancados.isSelected() && dadosFiltrados.size() != dados.size()) {
                            statusMsg += " de " + dados.size() + " filtrados";
                        }
                        statusMsg += ")";
                        labelStatus.setText(statusMsg);
                        
                        // Tocar som de sucesso para relatório geral
                        SoundNotification.playSound(SoundNotification.SoundType.SUCCESS);
                    } else {
                        // Se não há dados, usar dados de exemplo
                        preencherDadosExemplo();
                        gerarResumoEstatistico();
                        atualizarGraficos();

                        btnExportar.setEnabled(true);
                        btnImprimir.setEnabled(true);
                        labelStatus.setText("Relatório gerado com dados de exemplo");
                    }

                } catch (java.util.concurrent.CancellationException e) {
                    labelStatus.setText("Geração de relatório cancelada");
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable cause = e.getCause();
                    String mensagem = cause != null ? cause.getMessage() : e.getMessage();

                    JOptionPane.showMessageDialog(RelatorioFrame.this,
                            "Erro ao gerar relatório: " + mensagem + "\n\nUsando dados de exemplo.",
                            "Erro", JOptionPane.WARNING_MESSAGE);

                    // Em caso de erro, usar dados de exemplo
                    preencherDadosExemplo();
                    gerarResumoEstatistico();
                    atualizarGraficos();

                    btnExportar.setEnabled(true);
                    btnImprimir.setEnabled(true);
                    labelStatus.setText("Erro na consulta - usando dados de exemplo");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RelatorioFrame.this,
                            "Erro inesperado: " + e.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    labelStatus.setText("Erro inesperado ao gerar relatório");
                }

                progressBar.setVisible(false);
                btnGerar.setEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * === MÉTODO DOS FILTROS AVANÇADOS - FASE 2 ===
     * Aplica filtros avançados aos dados do relatório
     */
    private List<Map<String, Object>> aplicarFiltrosAvancados(List<Map<String, Object>> dados) {
        if (!checkFiltrosAvancados.isSelected() || dados == null || dados.isEmpty()) {
            return dados;
        }

        List<Map<String, Object>> dadosFiltrados = new ArrayList<>(dados);

        try {
            // Filtro por valor mínimo e máximo
            Double valorMin = (Double) spinnerValorMinimo.getValue();
            Double valorMax = (Double) spinnerValorMaximo.getValue();
            
            if (valorMin != null && valorMin > 0) {
                dadosFiltrados = dadosFiltrados.stream()
                    .filter(item -> {
                        Object valorObj = item.get("valor");
                        if (valorObj == null) return false;
                        try {
                            double valor = Double.parseDouble(valorObj.toString());
                            return valor >= valorMin;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            }

            if (valorMax != null && valorMax > 0) {
                dadosFiltrados = dadosFiltrados.stream()
                    .filter(item -> {
                        Object valorObj = item.get("valor");
                        if (valorObj == null) return false;
                        try {
                            double valor = Double.parseDouble(valorObj.toString());
                            return valor <= valorMax;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            }

            // Filtro por sala
            String salaSelecionada = (String) comboSala.getSelectedItem();
            if (salaSelecionada != null && !"Todas as Salas".equals(salaSelecionada)) {
                dadosFiltrados = dadosFiltrados.stream()
                    .filter(item -> {
                        Object salaObj = item.get("sala");
                        if (salaObj == null) return false;
                        return salaObj.toString().contains(salaSelecionada);
                    })
                    .collect(Collectors.toList());
            }

            // Filtro por estado de conservação
            String estadoSelecionado = (String) comboEstadoConservacao.getSelectedItem();
            if (estadoSelecionado != null && !"Todos".equals(estadoSelecionado)) {
                dadosFiltrados = dadosFiltrados.stream()
                    .filter(item -> {
                        Object estadoObj = item.get("estado_conservacao");
                        if (estadoObj == null) return false;
                        return estadoObj.toString().equalsIgnoreCase(estadoSelecionado);
                    })
                    .collect(Collectors.toList());
            }

            // Filtro para excluir itens sem valor
            if (checkExcluirSemValor.isSelected()) {
                dadosFiltrados = dadosFiltrados.stream()
                    .filter(item -> {
                        Object valorObj = item.get("valor");
                        if (valorObj == null) return false;
                        try {
                            double valor = Double.parseDouble(valorObj.toString());
                            return valor > 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            }

            System.out.println("Filtros avançados aplicados. Itens antes: " + dados.size() + 
                             ", depois: " + dadosFiltrados.size());

        } catch (Exception e) {
            System.err.println("Erro ao aplicar filtros avançados: " + e.getMessage());
            e.printStackTrace();
            return dados; // Retorna dados originais em caso de erro
        }

        return dadosFiltrados;
    }

    // === MÉTODO PARA BUSCA EM TEMPO REAL - FASE 2 ===
    private void filtrarTabelaEmTempoReal() {
        String textoBusca = campoBusca.getText().toLowerCase().trim();
        
        try {
            // Garantir que a tabela tenha um TableRowSorter
            javax.swing.table.TableRowSorter<DefaultTableModel> sorter;
            if (tabelaPreview.getRowSorter() instanceof javax.swing.table.TableRowSorter) {
                sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) tabelaPreview.getRowSorter();
            } else {
                sorter = new javax.swing.table.TableRowSorter<>(modeloTabela);
                tabelaPreview.setRowSorter(sorter);
            }
            
            if (textoBusca.isEmpty()) {
                // Se não há texto de busca, remove o filtro
                sorter.setRowFilter(null);
                labelResultadosBusca.setText(modeloTabela.getRowCount() + " resultados");
                return;
            }

            // Criar um filtro personalizado
            javax.swing.RowFilter<DefaultTableModel, Object> filtro = new javax.swing.RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    // Verifica se alguma coluna contém o texto de busca
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object valor = entry.getValue(i);
                        if (valor != null && valor.toString().toLowerCase().contains(textoBusca)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            // Aplicar o filtro
            sorter.setRowFilter(filtro);
            
            // Atualizar contador de resultados
            int resultados = tabelaPreview.getRowCount();
            labelResultadosBusca.setText(resultados + " resultado" + (resultados != 1 ? "s" : ""));
            
        } catch (Exception e) {
            System.err.println("Erro ao filtrar tabela: " + e.getMessage());
            e.printStackTrace();
            labelResultadosBusca.setText("Erro na busca");
        }
    }

    // === MÉTODO ATUALIZAR GRÁFICOS - FASE 2 ===
    private void atualizarGraficos() {
        if (modeloTabela.getRowCount() == 0) {
            return; // Não há dados para gerar gráficos
        }

        String tipoGrafico = (String) comboTipoGrafico.getSelectedItem();
        
        try {
            switch (tipoGrafico) {
                case "Pizza - Status de Coleta":
                    atualizarGraficoPizzaStatus();
                    break;
                case "Barras - Itens por Setor":
                    atualizarGraficoBarrasSetor();
                    break;
                case "Barras - Itens por Responsável":
                    atualizarGraficoBarrasResponsavel();
                    break;
                case "Pizza - Estado de Conservação":
                    atualizarGraficoPizzaConservacao();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar gráficos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarGraficoPizzaStatus() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Integer> contadores = new HashMap<>();
        
        // Contar status (assumindo que está na coluna 6)
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            String status = (String) modeloTabela.getValueAt(i, 6);
            if (status != null) {
                contadores.put(status, contadores.getOrDefault(status, 0) + 1);
            }
        }
        
        // Adicionar dados ao dataset
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        // Criar novo gráfico
        JFreeChart chart = ChartFactory.createPieChart(
            "Status de Coleta",
            dataset,
            true,
            true,
            false
        );
        chart.setBackgroundPaint(Color.WHITE);
        
        // Atualizar o painel
        painelGraficoPizza.setChart(chart);
    }

    private void atualizarGraficoBarrasSetor() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> contadores = new HashMap<>();
        
        // Contar por setor (assumindo que está na coluna 4)
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            String setor = (String) modeloTabela.getValueAt(i, 4);
            if (setor != null) {
                contadores.put(setor, contadores.getOrDefault(setor, 0) + 1);
            }
        }
        
        // Adicionar dados ao dataset
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            dataset.addValue(entry.getValue(), "Itens", entry.getKey());
        }
        
        // Criar novo gráfico
        JFreeChart chart = ChartFactory.createBarChart(
            "Itens por Setor",
            "Setor",
            "Quantidade",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        chart.setBackgroundPaint(Color.WHITE);
        
        // Atualizar o painel
        painelGraficoBarras.setChart(chart);
    }

    private void atualizarGraficoBarrasResponsavel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> contadores = new HashMap<>();
        
        // Contar por responsável (assumindo que está na coluna 5)
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            String responsavel = (String) modeloTabela.getValueAt(i, 5);
            if (responsavel != null) {
                contadores.put(responsavel, contadores.getOrDefault(responsavel, 0) + 1);
            }
        }
        
        // Adicionar dados ao dataset
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            dataset.addValue(entry.getValue(), "Itens", entry.getKey());
        }
        
        // Criar novo gráfico
        JFreeChart chart = ChartFactory.createBarChart(
            "Itens por Responsável",
            "Responsável",
            "Quantidade",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        chart.setBackgroundPaint(Color.WHITE);
        
        // Atualizar o painel
        painelGraficoBarras.setChart(chart);
    }

    private void atualizarGraficoPizzaConservacao() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        Map<String, Integer> contadores = new HashMap<>();
        
        // Contar estado de conservação (pode estar em diferentes colunas dependendo do relatório)
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            // Tentar encontrar coluna de estado/conservação
            String estado = null;
            for (int j = 0; j < modeloTabela.getColumnCount(); j++) {
                String nomeColuna = modeloTabela.getColumnName(j).toLowerCase();
                if (nomeColuna.contains("estado") || nomeColuna.contains("conservacao") || nomeColuna.contains("situacao")) {
                    estado = (String) modeloTabela.getValueAt(i, j);
                    break;
                }
            }
            
            if (estado == null) {
                estado = "Não informado";
            }
            
            contadores.put(estado, contadores.getOrDefault(estado, 0) + 1);
        }
        
        // Adicionar dados ao dataset
        for (Map.Entry<String, Integer> entry : contadores.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        // Criar novo gráfico
        JFreeChart chart = ChartFactory.createPieChart(
            "Estado de Conservação",
            dataset,
            true,
            true,
            false
        );
        chart.setBackgroundPaint(Color.WHITE);
        
        // Atualizar o painel
        painelGraficoPizza.setChart(chart);
    }

    private void preencherTabelaComDados(List<Map<String, Object>> dados) {
        // Limpar dados anteriores
        modeloTabela.setRowCount(0);

        if (dados == null || dados.isEmpty()) {
            preencherDadosExemplo();
            return;
        }

        String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();

        // Preencher com dados reais
        for (Map<String, Object> linha : dados) {
            Object[] row = new Object[8];

            // Mapear campos com base nos aliases retornados pelas consultas SQL
            row[0] = linha.get("Número Patrimônio") != null ? linha.get("Número Patrimônio")
                    : linha.get("numero") != null ? linha.get("numero") : linha.get("patrimonio");
            row[1] = linha.get("Descrição") != null ? linha.get("Descrição") : linha.get("descricao");

            // Para relatório sem plaqueta, combinar marca e modelo
            if ("Itens Sem Plaqueta de Patrimônio".equals(tipoRelatorio)) {
                String marca = linha.get("marca") != null ? linha.get("marca").toString() : "";
                String modelo = linha.get("modelo") != null ? linha.get("modelo").toString() : "";
                row[2] = (marca + " " + modelo).trim();
                row[3] = linha.get("quantidade") != null ? linha.get("quantidade") : "1";
                row[4] = linha.get("local") != null ? linha.get("local")
                        : linha.get("Setor") != null ? linha.get("Setor") : linha.get("setor");
            } else {
                // Para outros relatórios, usar campos específicos ou genéricos
                // Priorizar informação da sala onde o patrimônio foi encontrado
                String sala = "";
                if (linha.get("Localização Encontrada") != null) {
                    sala = linha.get("Localização Encontrada").toString();
                } else if (linha.get("Última Localização") != null) {
                    sala = linha.get("Última Localização").toString();
                } else if (linha.get("sala") != null) {
                    sala = linha.get("sala").toString();
                } else if (linha.get("Sala") != null) {
                    sala = linha.get("Sala").toString();
                } else {
                    sala = "N/A";
                }
                row[2] = sala;

                // A coluna Quantidade deve mostrar o Estado do item
                row[3] = linha.get("Estado") != null ? linha.get("Estado")
                        : linha.get("Estado Encontrado") != null ? linha.get("Estado Encontrado")
                                : linha.get("estado_conservacao") != null ? linha.get("estado_conservacao") : "Bom";

                row[4] = linha.get("Setor") != null ? linha.get("Setor")
                        : linha.get("setor") != null ? linha.get("setor") : linha.get("categoria");
            }

            row[5] = linha.get("Responsável") != null ? linha.get("Responsável") : linha.get("responsavel");
            // Situação do patrimônio - priorizar campo situacao
            Object situacaoObj = linha.get("situacao");
            if (situacaoObj != null && !situacaoObj.toString().trim().isEmpty()) {
                row[6] = situacaoObj.toString();
            } else {
                // Fallback para status se situacao não estiver definida
                Object statusObj = linha.get("status");
                if (statusObj != null && !statusObj.toString().trim().isEmpty()) {
                    row[6] = statusObj.toString();
                } else {
                    row[6] = "ATIVO"; // Valor padrão
                }
            }

            // Formatação do valor
            Object valorObj = linha.get("Valor") != null ? linha.get("Valor")
                    : linha.get("valor") != null ? linha.get("valor")
                            : linha.get("valor_aquisicao") != null ? linha.get("valor_aquisicao") : null;
            if (valorObj != null) {
                if (valorObj instanceof Number) {
                    double valor = ((Number) valorObj).doubleValue();
                    if (valor > 0) {
                        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
                        row[7] = formatoMoeda.format(valor);
                    } else {
                        row[7] = "R$ 0,00";
                    }
                } else {
                    String valorStr = valorObj.toString();
                    if (valorStr != null && !valorStr.trim().isEmpty() && !"0".equals(valorStr.trim())) {
                        try {
                            double valor = Double.parseDouble(valorStr);
                            if (valor > 0) {
                                NumberFormat formatoMoeda = NumberFormat
                                        .getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
                                row[7] = formatoMoeda.format(valor);
                            } else {
                                row[7] = "R$ 0,00";
                            }
                        } catch (NumberFormatException e) {
                            row[7] = valorStr;
                        }
                    } else {
                        row[7] = "R$ 0,00";
                    }
                }
            } else {
                row[7] = "R$ 0,00";
            }

            modeloTabela.addRow(row);
        }
    }

    private void preencherDadosExemplo() {
        // Limpar dados anteriores
        modeloTabela.setRowCount(0);

        try {
            // Buscar dados reais do banco de dados
            List<Patrimonio> patrimonios = patrimonioService.listarTodos();

            // Carregar todos os responsáveis e setores uma única vez para otimizar
            List<Responsavel> todosResponsaveis = responsavelService.listarResponsaveis();
            List<Setor> todosSetores = setorService.listarSetores();

            // Criar mapas para busca rápida
            Map<String, Responsavel> mapaResponsaveis = new HashMap<>();
            Map<Integer, Setor> mapaSetores = new HashMap<>();

            // Verificar se todosResponsaveis não é null antes de iterar
            if (todosResponsaveis != null) {
                for (Responsavel resp : todosResponsaveis) {
                    if (resp != null && resp.getNome() != null) {
                        mapaResponsaveis.put(resp.getNome(), resp);
                    }
                }
            }

            for (Setor setor : todosSetores) {
                mapaSetores.put(setor.getId(), setor);
            }

            // Formatador de moeda
            NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

            System.out.println("Iniciando processamento de " + patrimonios.size() + " patrimônios...");
            int contador = 0;

            for (Patrimonio patrimonio : patrimonios) {
                contador++;
                if (contador % 100 == 0) {
                    System.out.println("Processados " + contador + " de " + patrimonios.size() + " patrimônios...");
                }
                Object[] linha = new Object[8];

                linha[0] = patrimonio.getNumero() != null ? patrimonio.getNumero() : "N/A";
                linha[1] = patrimonio.getDescricao() != null ? patrimonio.getDescricao() : "Sem descrição";

                // Usar informação da sala onde o patrimônio está localizado
                String sala = "";
                if (patrimonio.getNomeSala() != null && !patrimonio.getNomeSala().trim().isEmpty()) {
                    sala = patrimonio.getNomeSala();
                } else {
                    sala = "N/A";
                }
                linha[2] = sala;

                // A coluna "Quantidade" deve mostrar o estado de conservação do item
                String estadoConservacao = "Bom"; // Valor padrão
                if (patrimonio.getEstadoConservacao() != null && !patrimonio.getEstadoConservacao().trim().isEmpty()) {
                    estadoConservacao = patrimonio.getEstadoConservacao();
                }
                linha[3] = estadoConservacao;

                // Buscar setor através do responsável usando os mapas
                String nomeSetor = "N/A";
                if (patrimonio.getNomeResponsavel() != null) {
                    try {
                        Responsavel responsavel = mapaResponsaveis.get(patrimonio.getNomeResponsavel());
                        if (responsavel != null && responsavel.getIdSetor() > 0) {
                            Setor setor = mapaSetores.get(responsavel.getIdSetor());
                            if (setor != null) {
                                nomeSetor = setor.getNome();
                            }
                        }
                    } catch (Exception e) {
                        // Se houver erro, manter "N/A"
                    }
                }
                linha[4] = nomeSetor;

                linha[5] = patrimonio.getNomeResponsavel() != null ? patrimonio.getNomeResponsavel()
                        : "Sem responsável";

                // Situação baseada no campo situacao do patrimônio
                String situacao = "ATIVO";
                if (patrimonio.getSituacao() != null && !patrimonio.getSituacao().trim().isEmpty()) {
                    situacao = patrimonio.getSituacao();
                } else {
                    // Fallback para status se situacao não estiver definida
                    situacao = patrimonio.getStatus() != null ? patrimonio.getStatus() : "ATIVO";
                }
                linha[6] = situacao;

                // Valor formatado
                String valorFormatado = "R$ 0,00";
                if (patrimonio.getValorAquisicao() != null) {
                    valorFormatado = formatoMoeda.format(patrimonio.getValorAquisicao());
                }
                linha[7] = valorFormatado;

                modeloTabela.addRow(linha);
            }

            // Atualizar status
            labelStatus.setText("Dados carregados: " + patrimonios.size() + " itens encontrados");

        } catch (Exception e) {
            // Em caso de erro, mostrar mensagem e usar dados de exemplo como fallback
            labelStatus.setText("Erro ao carregar dados: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar dados do banco: " + e.getMessage() + "\nUsando dados de exemplo.",
                    "Erro", JOptionPane.WARNING_MESSAGE);

            // Dados de exemplo como fallback
            Object[][] dadosExemplo = {
                    { "001", "Computador Desktop", "Dell OptiPlex 3070", "1", "TI", "João Silva", "Ativo",
                            "R$ 2.500,00" },
                    { "002", "Monitor LCD", "Samsung 24' LED", "1", "TI", "João Silva", "Ativo", "R$ 800,00" },
                    { "003", "Mesa de Escritório", "Móveis ABC Executive", "1", "Administração", "Maria Santos",
                            "Ativo", "R$ 450,00" }
            };

            for (Object[] linha : dadosExemplo) {
                modeloTabela.addRow(linha);
            }
        }
    }

    private List<Map<String, Object>> gerarDadosExemplo() {
        List<Map<String, Object>> dados = new ArrayList<>();

        try {
            System.out.println("Iniciando busca de dados no banco...");

            // Buscar dados reais do banco de dados
            System.out.println("Buscando patrimônios...");
            List<Patrimonio> patrimonios = patrimonioService.listarTodos();
            System.out.println("Patrimônios encontrados: " + (patrimonios != null ? patrimonios.size() : 0));

            // Carregar todos os responsáveis e setores uma única vez para otimizar
            System.out.println("Buscando responsáveis...");
            List<Responsavel> todosResponsaveis = responsavelService.listarResponsaveis();
            System.out
                    .println("Responsáveis encontrados: " + (todosResponsaveis != null ? todosResponsaveis.size() : 0));

            System.out.println("Buscando setores...");
            List<Setor> todosSetores = setorService.listarSetores();
            System.out.println("Setores encontrados: " + (todosSetores != null ? todosSetores.size() : 0));

            // Criar mapas para busca rápida
            Map<String, Responsavel> mapaResponsaveis = new HashMap<>();
            Map<Integer, Setor> mapaSetores = new HashMap<>();

            // Verificar se todosResponsaveis não é null antes de iterar
            if (todosResponsaveis != null) {
                for (Responsavel resp : todosResponsaveis) {
                    if (resp != null && resp.getNome() != null) {
                        mapaResponsaveis.put(resp.getNome(), resp);
                    }
                }
            }

            // Verificar se todosSetores não é null antes de iterar
            if (todosSetores != null) {
                for (Setor setor : todosSetores) {
                    if (setor != null) {
                        mapaSetores.put(setor.getId(), setor);
                    }
                }
            }

            // Formatador de moeda
            NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

            for (Patrimonio patrimonio : patrimonios) {
                Map<String, Object> item = new HashMap<>();

                item.put("numero", patrimonio.getNumero() != null ? patrimonio.getNumero() : "N/A");
                item.put("descricao", patrimonio.getDescricao() != null ? patrimonio.getDescricao() : "Sem descrição");

                // Combinar marca e modelo
                String marcaModelo = "";
                if (patrimonio.getMarca() != null && !patrimonio.getMarca().trim().isEmpty()) {
                    marcaModelo = patrimonio.getMarca();
                    if (patrimonio.getModelo() != null && !patrimonio.getModelo().trim().isEmpty()) {
                        marcaModelo += " " + patrimonio.getModelo();
                    }
                } else if (patrimonio.getModelo() != null && !patrimonio.getModelo().trim().isEmpty()) {
                    marcaModelo = patrimonio.getModelo();
                } else {
                    marcaModelo = "N/A";
                }
                item.put("marca", marcaModelo);

                item.put("quantidade", "1");

                // Buscar setor através do responsável usando os mapas
                String nomeSetor = "N/A";
                if (patrimonio.getNomeResponsavel() != null) {
                    try {
                        Responsavel responsavel = mapaResponsaveis.get(patrimonio.getNomeResponsavel());
                        if (responsavel != null && responsavel.getIdSetor() > 0) {
                            Setor setor = mapaSetores.get(responsavel.getIdSetor());
                            if (setor != null) {
                                nomeSetor = setor.getNome();
                            }
                        }
                    } catch (Exception e) {
                        // Se houver erro, manter "N/A"
                    }
                }
                item.put("setor", nomeSetor);

                item.put("responsavel",
                        patrimonio.getNomeResponsavel() != null ? patrimonio.getNomeResponsavel() : "Sem responsável");

                // Status de coleta baseado no estado de conservação
                String statusColeta = "COLETADO";
                if (patrimonio.getEstadoConservacao() != null) {
                    switch (patrimonio.getEstadoConservacao().toUpperCase()) {
                        case "DANIFICADO":
                        case "RUIM":
                            statusColeta = "DANIFICADO";
                            break;
                        case "INATIVO":
                            statusColeta = "NAO_ENCONTRADO";
                            break;
                        default:
                            statusColeta = "COLETADO";
                    }
                }
                item.put("status_coleta", statusColeta);

                // Valor formatado
                String valorFormatado = "R$ 0,00";
                if (patrimonio.getValorAquisicao() != null) {
                    valorFormatado = formatoMoeda.format(patrimonio.getValorAquisicao());
                }
                item.put("valor", valorFormatado);

                // Adicionar campo sem_etiqueta (simulado)
                item.put("sem_etiqueta", false);

                dados.add(item);
            }

            System.out.println("Processamento concluído. Total de itens gerados: " + dados.size());

        } catch (Exception e) {
            System.err.println("Erro ao buscar dados do banco: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, usar dados de exemplo como fallback
            String[][] exemploArray = {
                    { "001", "Computador Desktop", "Dell OptiPlex 3070", "1", "TI", "João Silva", "COLETADO",
                            "R$ 2.500,00" },
                    { "002", "Monitor LCD", "Samsung 24' LED", "1", "TI", "João Silva", "COLETADO", "R$ 800,00" },
                    { "003", "Mesa de Escritório", "Móveis ABC Executive", "1", "Administração", "Maria Santos",
                            "NAO_ENCONTRADO", "R$ 450,00" }
            };

            for (String[] linha : exemploArray) {
                Map<String, Object> item = new HashMap<>();
                item.put("numero", linha[0]);
                item.put("descricao", linha[1]);
                item.put("marca", linha[2]);
                item.put("quantidade", linha[3]);
                item.put("setor", linha[4]);
                item.put("responsavel", linha[5]);
                item.put("status_coleta", linha[6]);
                item.put("valor", linha[7]);
                item.put("sem_etiqueta", false);
                dados.add(item);
            }
        }

        return dados;
    }

    private void gerarResumoEstatistico(List<Map<String, Object>> dados) {
        if (dados == null || dados.isEmpty()) {
            gerarResumoEstatistico(); // Usar método original para dados de exemplo
            atualizarGraficos();
            return;
        }

        // Gerar resumo baseado nos dados reais
        int totalItens = dados.size();
        int itensColetados = 0;
        int itensNaoEncontrados = 0;
        int itensDanificados = 0;
        int itensSemEtiqueta = 0;

        for (Map<String, Object> item : dados) {
            String status = (String) item.get("status_coleta");
            Boolean semEtiqueta = (Boolean) item.get("sem_etiqueta");

            if (semEtiqueta != null && semEtiqueta) {
                itensSemEtiqueta++;
            }

            if (status != null) {
                switch (status) {
                    case "COLETADO":
                        itensColetados++;
                        break;
                    case "NAO_ENCONTRADO":
                        itensNaoEncontrados++;
                        break;
                    case "DANIFICADO":
                        itensDanificados++;
                        break;
                }
            }
        }

        StringBuilder resumo = new StringBuilder();
        
        // RESUMO EXECUTIVO
        resumo.append("📊 RESUMO EXECUTIVO - INVENTÁRIO DE PATRIMÔNIO\n");
        resumo.append("==============================================\n\n");
        
        // Indicadores principais
        double percentualColetado = totalItens > 0 ? (itensColetados * 100.0) / totalItens : 0;
        double percentualNaoEncontrado = totalItens > 0 ? (itensNaoEncontrados * 100.0) / totalItens : 0;
        double percentualDanificado = totalItens > 0 ? (itensDanificados * 100.0) / totalItens : 0;
        
        // Status geral do inventário
        String statusGeral;
        String emoji;
        if (percentualColetado >= 90) {
            statusGeral = "EXCELENTE";
            emoji = "🟢";
        } else if (percentualColetado >= 75) {
            statusGeral = "BOM";
            emoji = "🟡";
        } else if (percentualColetado >= 50) {
            statusGeral = "REGULAR";
            emoji = "🟠";
        } else {
            statusGeral = "CRÍTICO";
            emoji = "🔴";
        }
        
        resumo.append(String.format("%s STATUS GERAL: %s (%.1f%% coletado)\n\n", emoji, statusGeral, percentualColetado));
        
        // Métricas principais
        resumo.append("📈 INDICADORES PRINCIPAIS\n");
        resumo.append("-------------------------\n");
        resumo.append(String.format("• Total de Patrimônios: %,d itens\n", totalItens));
        resumo.append(String.format("• Taxa de Coleta: %.1f%% (%,d itens)\n", percentualColetado, itensColetados));
        resumo.append(String.format("• Itens Não Localizados: %.1f%% (%,d itens)\n", percentualNaoEncontrado, itensNaoEncontrados));
        resumo.append(String.format("• Itens Danificados: %.1f%% (%,d itens)\n", percentualDanificado, itensDanificados));
        resumo.append(String.format("• Itens Sem Etiqueta: %,d itens\n\n", itensSemEtiqueta));
        
        // Análise de riscos
        resumo.append("⚠️ ANÁLISE DE RISCOS\n");
        resumo.append("--------------------\n");
        if (percentualNaoEncontrado > 10) {
            resumo.append("🔴 ALTO: Taxa de itens não encontrados acima de 10%\n");
        } else if (percentualNaoEncontrado > 5) {
            resumo.append("🟡 MÉDIO: Taxa de itens não encontrados entre 5-10%\n");
        } else {
            resumo.append("🟢 BAIXO: Taxa de itens não encontrados controlada\n");
        }
        
        if (itensSemEtiqueta > totalItens * 0.05) {
            resumo.append("🔴 ATENÇÃO: Muitos itens sem etiqueta de patrimônio\n");
        }
        
        if (itensDanificados > 0) {
            resumo.append(String.format("⚠️ MANUTENÇÃO: %d itens necessitam reparo\n", itensDanificados));
        }
        
        resumo.append("\n");
        
        // Informações do relatório
        resumo.append("📋 INFORMAÇÕES DO RELATÓRIO\n");
        resumo.append("---------------------------\n");
        resumo.append("Data de Geração: ").append(DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate())).append("\n");
        resumo.append("Tipo de Relatório: ").append(comboTipoRelatorio.getSelectedItem()).append("\n");
        resumo.append("Filtros Aplicados:\n");
        resumo.append("  • Setor: ").append(comboSetor.getSelectedItem()).append("\n");
        resumo.append("  • Responsável: ").append(comboResponsavel.getSelectedItem()).append("\n");
        resumo.append("  • Situação: ").append(comboStatus.getSelectedItem()).append("\n\n");
        
        // Recomendações
        resumo.append("💡 RECOMENDAÇÕES\n");
        resumo.append("----------------\n");
        if (percentualColetado < 75) {
            resumo.append("• Intensificar esforços de localização de patrimônios\n");
        }
        if (itensNaoEncontrados > 0) {
            resumo.append("• Investigar causas dos itens não localizados\n");
        }
        if (itensDanificados > 0) {
            resumo.append("• Programar manutenção para itens danificados\n");
        }
        if (itensSemEtiqueta > 0) {
            resumo.append("• Providenciar etiquetagem dos itens sem identificação\n");
        }
        if (percentualColetado >= 90) {
            resumo.append("• Excelente trabalho! Manter padrão de qualidade\n");
        }

        areaResumo.setText(resumo.toString());
        atualizarGraficos();
    }

    private void gerarResumoEstatistico() {
        StringBuilder resumo = new StringBuilder();
        resumo.append("RELATÓRIO DE PATRIMÔNIO\n");
        resumo.append("========================\n\n");
        resumo.append("Data de Geração: ").append(DateFormatUtils.formatDateTimeFull(DateFormatUtils.nowAsDate()))
                .append("\n");
        resumo.append("Tipo: ").append(comboTipoRelatorio.getSelectedItem()).append("\n\n");

        resumo.append("RESUMO ESTATÍSTICO\n");
        resumo.append("------------------\n");
        resumo.append("Total de Itens: ").append(modeloTabela.getRowCount()).append("\n");

        // Contar por situação
        int ativos = 0, inativos = 0, manutencao = 0;
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            String situacao = (String) modeloTabela.getValueAt(i, 6);
            switch (situacao) {
                case "ATIVO":
                case "Ativo":
                case "COLETADO":
                    ativos++;
                    break;
                case "INATIVO":
                case "Inativo":
                case "NAO_ENCONTRADO":
                    inativos++;
                    break;
                case "PENDENTE":
                case "Em Manutenção":
                case "DANIFICADO":
                    manutencao++;
                    break;
            }
        }

        resumo.append("\nPOR SITUAÇÃO:\n");
        resumo.append("- Ativos/Coletados: ").append(ativos).append("\n");
        resumo.append("- Inativos/Não Encontrados: ").append(inativos).append("\n");
        resumo.append("- Em Manutenção/Danificados: ").append(manutencao).append("\n");

        // Valor total estimado
        resumo.append("\nVALOR TOTAL ESTIMADO: R$ 12.800,00\n");

        resumo.append("\nFILTROS APLICADOS:\n");
        resumo.append("- Setor: ").append(comboSetor.getSelectedItem()).append("\n");
        resumo.append("- Responsável: ").append(comboResponsavel.getSelectedItem()).append("\n");
        resumo.append("- Situação: ").append(comboStatus.getSelectedItem()).append("\n");

        areaResumo.setText(resumo.toString());
    }

    private void exportarPDF() {
        // Verificar se há dados na tabela antes de abrir o dialog
        if (modeloTabela.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Não há dados para exportar. Gere um relatório primeiro.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gerar nome do arquivo com data
        String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();
        String nomeArquivo = gerarNomeArquivoPDF(tipoRelatorio);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos PDF", "pdf"));
        fileChooser.setSelectedFile(new java.io.File(nomeArquivo + ".pdf"));

        int resultado = fileChooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            String arquivo = fileChooser.getSelectedFile().getAbsolutePath();

            // Garantir que o arquivo tenha extensão .pdf
            if (!arquivo.toLowerCase().endsWith(".pdf")) {
                arquivo += ".pdf";
            }

            // Criar variável final para uso na classe interna
            final String arquivoFinal = arquivo;

            labelStatus.setText("Exportando PDF para: " + arquivoFinal);

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                private String mensagemErro = null;

                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Obter título do relatório
                        String tituloRelatorio = "RELATÓRIO: " + tipoRelatorio.toUpperCase();

                        // Gerar relatório PDF usando a classe utilitária com iText7
                        com.inventario.util.RelatorioPDFGenerator.gerarRelatorioPDF(
                                arquivoFinal, tituloRelatorio, modeloTabela, tipoRelatorio);

                    } catch (Exception e) {
                        mensagemErro = "Erro ao gerar PDF: " + e.getMessage();
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (mensagemErro != null) {
                        labelStatus.setText("Erro na exportação PDF");
                        JOptionPane.showMessageDialog(RelatorioFrame.this,
                                mensagemErro,
                                "Erro na Exportação PDF", JOptionPane.ERROR_MESSAGE);
                    } else {
                        labelStatus.setText("Relatório PDF exportado com sucesso!");
                        JOptionPane.showMessageDialog(RelatorioFrame.this,
                                "Relatório PDF exportado com sucesso para:\n" + arquivoFinal,
                                "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void imprimirRelatorio() {
        try {
            // TODO: Implementar impressão real
            boolean imprimir = tabelaPreview.print();
            if (imprimir) {
                labelStatus.setText("Relatório enviado para impressão");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao imprimir: " + e.getMessage(),
                    "Erro de Impressão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparRelatorio() {
        modeloTabela.setRowCount(0);
        areaResumo.setText(
                "Nenhum relatório gerado ainda.\n\nSelecione os filtros e clique em 'Gerar Relatório' para visualizar o resumo.");
        btnExportar.setEnabled(false);
        btnImprimir.setEnabled(false);
        labelStatus.setText("Relatório limpo");
    }

    private void atualizarFiltros() {
        String tipoSelecionado = (String) comboTipoRelatorio.getSelectedItem();

        // Determinar se é um relatório básico ou avançado
        boolean isRelatorioBasico = tipoSelecionado.equals("Itens Encontrados") ||
                                   tipoSelecionado.equals("Itens Não Encontrados") ||
                                   tipoSelecionado.equals("Itens Sem Plaqueta de Patrimônio");
        
        // Habilitar/desabilitar filtros baseado no tipo de relatório
        boolean habilitarSetor = !tipoSelecionado.contains("Geral") && !tipoSelecionado.contains("Estatísticas")
                || tipoSelecionado.contains("Avançado por Setor") || tipoSelecionado.contains("Consolidado");
        comboSetor.setEnabled(habilitarSetor && !isRelatorioBasico);

        boolean habilitarResponsavel = tipoSelecionado.contains("Responsável") || tipoSelecionado.contains("Geral")
                || tipoSelecionado.contains("Avançado por Responsável") || tipoSelecionado.contains("Consolidado");
        comboResponsavel.setEnabled(habilitarResponsavel && !isRelatorioBasico);

        // Para relatórios básicos, desabilitar filtros de data
        boolean habilitarDatas = tipoSelecionado.contains("Avançado") || tipoSelecionado.contains("Consolidado");
        spinnerDataInicio.setEnabled(!isRelatorioBasico);
        spinnerDataFim.setEnabled(!isRelatorioBasico);
        
        // Ocultar/mostrar componentes avançados para relatórios básicos
         if (isRelatorioBasico) {
             // Desabilitar filtros avançados para relatórios básicos
             checkFiltrosAvancados.setEnabled(false);
             checkFiltrosAvancados.setSelected(false);
             // Desabilitar manualmente os filtros avançados
             spinnerValorMinimo.setEnabled(false);
             spinnerValorMaximo.setEnabled(false);
             comboSala.setEnabled(false);
             comboEstadoConservacao.setEnabled(false);
             comboOperadorLogico.setEnabled(false);
             checkExcluirSemValor.setEnabled(false);
         } else {
             checkFiltrosAvancados.setEnabled(true);
         }

        // Destacar campos obrigatórios para relatórios avançados
        if (habilitarDatas) {
            spinnerDataInicio.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(40, 167, 69), 2),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
            spinnerDataFim.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(40, 167, 69), 2),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        } else {
            estilizarSpinner(spinnerDataInicio);
            estilizarSpinner(spinnerDataFim);
        }

        // Destacar campo de inventário se necessário
        boolean precisaInventario = !tipoSelecionado.equals("Relatório Geral de Patrimônio") &&
                                   !tipoSelecionado.equals("Relatório por Responsável");
        
        if (precisaInventario) {
            comboInventario.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        } else {
            comboInventario.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        }

        // Atualizar label de status baseado no tipo selecionado
        // Verificar se há inventário selecionado para relatórios que precisam
        int idInventarioSelecionado = obterIdInventarioSelecionado();
        boolean temInventarioSelecionado = idInventarioSelecionado != -1;
        
        switch (tipoSelecionado) {
            case "Itens Encontrados":
                if (temInventarioSelecionado) {
                    labelStatus.setText("📋 Relatório de itens encontrados - Inventário selecionado");
                } else {
                    labelStatus.setText("📋 Relatório de itens encontrados - REQUER INVENTÁRIO SELECIONADO");
                }
                break;
            case "Itens Não Encontrados":
                if (temInventarioSelecionado) {
                    labelStatus.setText("📋 Relatório de itens não encontrados - Inventário selecionado");
                } else {
                    labelStatus.setText("📋 Relatório de itens não encontrados - REQUER INVENTÁRIO SELECIONADO");
                }
                break;
            case "Itens Sem Plaqueta de Patrimônio":
                if (temInventarioSelecionado) {
                    labelStatus.setText("📋 Relatório de itens sem plaqueta - Inventário selecionado");
                } else {
                    labelStatus.setText("📋 Relatório de itens sem plaqueta - REQUER INVENTÁRIO SELECIONADO");
                }
                break;
            case "Relatório por Responsável":
                labelStatus.setText("👤 Relatório geral por responsável - Inventário opcional");
                break;
            // ========== RELATÓRIOS AVANÇADOS ==========
            case "Relatório Avançado por Setor":
                labelStatus.setText("Relatório detalhado por setor específico com filtro de período");
                break;
            case "Relatório Avançado por Responsável":
                labelStatus.setText("Relatório detalhado por responsável específico com filtro de período");
                break;
            case "Relatório Avançado por Período":
                labelStatus.setText("Relatório de todas as coletas realizadas no período selecionado");
                break;
            case "Estatísticas Avançadas por Setor":
                labelStatus.setText("Estatísticas detalhadas com percentuais e valores por setor");
                break;
            case "Relatório Consolidado Executivo":
                labelStatus.setText("Resumo executivo com indicadores de performance por setor");
                break;
            case "--- RELATÓRIOS AVANÇADOS ---":
                labelStatus.setText("Selecione um tipo de relatório válido");
                break;
            default:
                labelStatus.setText("Pronto para gerar relatório");
        }
        
        // Verificar se há inventários disponíveis para relatórios que precisam
        if (precisaInventario) {
            boolean temInventarios = comboInventario.getItemCount() > 0 && 
                                   !comboInventario.getItemAt(0).contains("Nenhum inventário disponível");
            
            if (!temInventarios) {
                labelStatus.setText("⚠️ ATENÇÃO: Não há inventários disponíveis. Crie um inventário primeiro.");
                btnGerar.setEnabled(false);
            } else {
                btnGerar.setEnabled(true);
            }
        } else {
            btnGerar.setEnabled(true);
        }
    }

    private Icon createIcon(String emoji) {
        // Criar um ícone simples com emoji
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                g.drawString(emoji, x, y + 12);
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    /**
     * Exporta o relatório atualmente exibido para Excel - VERSÃO SIMPLIFICADA SEM THREADS
     * Prioridade máxima: gerar arquivo .xlsx com sucesso
     */
    private void exportarRelatorioAtual() {
        try {
            System.out.println("\n=== EXPORTAÇÃO EXCEL SIMPLIFICADA (SEM THREADS) ===");

            // Verificar dados na tabela
            int linhasTabela = tabelaPreview.getRowCount();
            
            if (linhasTabela == 0) {
                JOptionPane.showMessageDialog(this,
                        "Gere um relatório primeiro antes de exportar.\n\n" +
                                "Clique em 'Gerar Relatório' para carregar os dados na tabela.",
                        "Tabela Vazia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String tipoRelatorio = (String) comboTipoRelatorio.getSelectedItem();
            System.out.println("Tipo de relatório: " + tipoRelatorio);
            System.out.println("Linhas na tabela: " + linhasTabela);

            labelStatus.setText("Preparando exportação...");

            // Exportar diretamente (sem threads)
            boolean sucesso = exportarDadosTabelaOtimizado(tipoRelatorio);

            if (sucesso) {
                labelStatus.setText("Relatório Excel exportado com sucesso!");
                System.out.println("✅ EXPORTAÇÃO CONCLUÍDA");
            } else {
                labelStatus.setText("Falha na exportação");
                System.out.println("❌ EXPORTAÇÃO FALHOU");
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO: " + e.getMessage());
            e.printStackTrace();
            labelStatus.setText("Erro na exportação");
            JOptionPane.showMessageDialog(this,
                    "Erro durante a exportação:\n\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida os filtros obrigatórios para relatórios avançados
     */
    private boolean validarFiltrosRelatoriosAvancados(String tipoRelatorio, String setor, String responsavel,
            Date dataInicio, Date dataFim) {
        // Verificar se é um relatório avançado
        if (!tipoRelatorio.contains("Avançado") && !tipoRelatorio.contains("Consolidado")) {
            return true; // Não é relatório avançado, não precisa validar
        }

        // Verificar se é o separador
        if ("--- RELATÓRIOS AVANÇADOS ---".equals(tipoRelatorio)) {
            JOptionPane.showMessageDialog(this,
                    "Por favor, selecione um tipo de relatório válido.",
                    "Seleção Inválida", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validar período (obrigatório para todos os relatórios avançados)
        if (dataInicio == null || dataFim == null) {
            JOptionPane.showMessageDialog(this,
                    "Para relatórios avançados, é obrigatório definir o período (Data Início e Data Fim).",
                    "Filtros Obrigatórios", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Verificar se data início é anterior à data fim
        if (dataInicio.after(dataFim)) {
            JOptionPane.showMessageDialog(this,
                    "A Data de Início deve ser anterior à Data de Fim.",
                    "Período Inválido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Validações específicas por tipo de relatório
        switch (tipoRelatorio) {
            case "Relatório Avançado por Setor":
                // Setor pode ser "Todos" para relatório geral por setores
                if (setor == null) {
                    JOptionPane.showMessageDialog(this,
                            "Para o Relatório Avançado por Setor, é obrigatório selecionar um setor.",
                            "Setor Obrigatório", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                break;

            case "Relatório Avançado por Responsável":
                // Responsável pode ser "Todos" para relatório geral por responsáveis
                if (responsavel == null) {
                    JOptionPane.showMessageDialog(this,
                            "Para o Relatório Avançado por Responsável, é obrigatório selecionar um responsável.",
                            "Responsável Obrigatório", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                break;

            case "Relatório Avançado por Período":
            case "Estatísticas Avançadas por Setor":
            case "Relatório Consolidado Executivo":
                // Para estes relatórios, apenas o período é obrigatório (já validado acima)
                break;
        }

        return true;
    }

    /**
     * Exporta dados da tabela - VERSÃO SIMPLIFICADA COM DETECÇÃO DE ExceptionInInitializerError
     * Prioridade máxima: gerar arquivo .xlsx OU CSV se Apache POI falhar
     */
    private boolean exportarDadosTabelaOtimizado(String tipoRelatorio) {
        try {
            labelStatus.setText("Preparando dados...");

            // Preparar dados
            List<Map<String, Object>> dadosPreparados = prepararDadosTabela();

            if (dadosPreparados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há dados para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String[] colunas = { "Número Patrimônio", "Descrição", "Sala", "Estado", "Setor/Local",
                    "Responsável", "Situação", "Valor" };
            String[] chaves = { "numero", "descricao", "sala", "estado", "setor", "responsavel", "situacao",
                    "valor" };

            String nomeArquivo = gerarNomeArquivo(tipoRelatorio);
            String tituloRelatorio = "RELATÓRIO: " + tipoRelatorio.toUpperCase();

            // Escolher onde salvar
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel (*.xlsx)", "xlsx"));
            fileChooser.setSelectedFile(new java.io.File(nomeArquivo + ".xlsx"));
            
            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                labelStatus.setText("Cancelado");
                return false;
            }
            
            String caminhoArquivo = fileChooser.getSelectedFile().getAbsolutePath();
            if (!caminhoArquivo.toLowerCase().endsWith(".xlsx")) {
                caminhoArquivo += ".xlsx";
            }
            
            System.out.println("📁 Salvando em: " + caminhoArquivo);

            // Tentar exportar com Apache POI
            try {
                return exportarComApachePOI(dadosPreparados, colunas, chaves, tituloRelatorio, caminhoArquivo);
                
            } catch (ExceptionInInitializerError initError) {
                // CAPTURA ESPECÍFICA DO ExceptionInInitializerError
                System.err.println("❌ ExceptionInInitializerError detectado!");
                System.err.println("Causa: Problema na inicialização do Apache POI");
                initError.printStackTrace();
                
                // Fallback automático para CSV
                System.out.println("🔄 Usando fallback CSV...");
                return exportarComCSVFallback(dadosPreparados, colunas, chaves, tituloRelatorio, caminhoArquivo, initError);
                
            } catch (NoClassDefFoundError classError) {
                // Classe não encontrada
                System.err.println("❌ NoClassDefFoundError: " + classError.getMessage());
                classError.printStackTrace();
                return exportarComCSVFallback(dadosPreparados, colunas, chaves, tituloRelatorio, caminhoArquivo, classError);
                
            } catch (Exception poiError) {
                // Outros erros do Apache POI
                System.err.println("❌ Apache POI falhou: " + poiError.getMessage());
                poiError.printStackTrace();
                return exportarComCSVFallback(dadosPreparados, colunas, chaves, tituloRelatorio, caminhoArquivo, poiError);
            }

        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
            labelStatus.setText("Erro na exportação");
            JOptionPane.showMessageDialog(this, "Erro:\n\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Verifica Apache POI de forma segura sem causar ExceptionInInitializerError
     */
    private boolean verificarApachePoiSeguro() {
        try {
            // Verificar se as classes básicas estão disponíveis
            Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            Class.forName("org.apache.poi.ss.usermodel.Workbook");
            
            // Verificar XMLBeans (causa comum de problemas)
            try {
                Class.forName("org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook");
                System.out.println("✅ Classes Apache POI disponíveis");
                return true;
            } catch (ClassNotFoundException e) {
                System.out.println("⚠️ XMLBeans não disponível: " + e.getMessage());
                return false;
            }
            
        } catch (ExceptionInInitializerError e) {
            // Capturar especificamente ExceptionInInitializerError
            System.out.println("⚠️ ExceptionInInitializerError ao verificar Apache POI - dependências com problema");
            return false;
        } catch (NoClassDefFoundError e) {
            System.out.println("⚠️ NoClassDefFoundError: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("⚠️ Apache POI não disponível: " + e.getMessage());
            return false;
        } catch (Throwable e) {
            // Capturar qualquer outro erro
            System.out.println("⚠️ Erro ao verificar Apache POI: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Exporta usando Apache POI - VERSÃO PROFISSIONAL COM DESIGN ATRATIVO
     * Design moderno e profissional para relatórios Excel
     */
    private boolean exportarComApachePOI(List<Map<String, Object>> dados, String[] colunas, String[] chaves, 
                                         String titulo, String caminhoArquivo) throws Exception {
        
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = null;
        java.io.FileOutputStream fileOut = null;
        
        try {
            System.out.println("=== EXPORTANDO XLSX PROFISSIONAL ===");
            labelStatus.setText("Criando Excel profissional...");
            
            // Criar workbook
            try {
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            } catch (ExceptionInInitializerError initError) {
                System.err.println("❌ ExceptionInInitializerError!");
                throw initError;
            }
            
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Relatório de Patrimônio");
            
            // ===== ESTILOS PROFISSIONAIS =====
            
            // Título
            org.apache.poi.ss.usermodel.CellStyle tituloStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font tituloFont = workbook.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 16);
            tituloFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            tituloStyle.setFont(tituloFont);
            tituloStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            tituloStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            tituloStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            tituloStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            
            // Cabeçalho
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.MEDIUM);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.MEDIUM);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setWrapText(true);
            
            // Dados (linhas pares)
            org.apache.poi.ss.usermodel.CellStyle dadosStyle = workbook.createCellStyle();
            dadosStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dadosStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dadosStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dadosStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            dadosStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            
            // Dados alternados (linhas ímpares)
            org.apache.poi.ss.usermodel.CellStyle dadosAlternadoStyle = workbook.createCellStyle();
            dadosAlternadoStyle.cloneStyleFrom(dadosStyle);
            dadosAlternadoStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            dadosAlternadoStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            
            // Totalizador
            org.apache.poi.ss.usermodel.CellStyle totalStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setFontHeightInPoints((short) 11);
            totalStyle.setFont(totalFont);
            totalStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            totalStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.DOUBLE);
            totalStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.DOUBLE);
            
            // ===== TÍTULO =====
            org.apache.poi.ss.usermodel.Row tituloRow = sheet.createRow(0);
            tituloRow.setHeightInPoints(30);
            org.apache.poi.ss.usermodel.Cell tituloCell = tituloRow.createCell(0);
            tituloCell.setCellValue(titulo);
            tituloCell.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, colunas.length - 1));
            
            // Linha vazia
            sheet.createRow(1);
            
            // Info
            org.apache.poi.ss.usermodel.Row infoRow = sheet.createRow(2);
            org.apache.poi.ss.usermodel.Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Data: " + DateFormatUtils.formatDateTimeFull(new Date()) + 
                                 " | Registros: " + dados.size());
            sheet.createRow(3);
            
            // ===== CABEÇALHOS =====
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(4);
            headerRow.setHeightInPoints(25);
            
            for (int i = 0; i < colunas.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // ===== DADOS =====
            labelStatus.setText("Adicionando dados...");
            
            int linhaAtual = 5;
            for (Map<String, Object> dado : dados) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(linhaAtual);
                row.setHeightInPoints(18);
                
                boolean linhaImpar = (linhaAtual % 2 != 0);
                
                for (int i = 0; i < chaves.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
                    Object valor = dado.get(chaves[i]);
                    
                    if (valor != null) {
                        String valorStr = valor.toString();
                        valorStr = valorStr.replaceAll("[\\x00-\\x1F\\x7F]", "");
                        if (valorStr.length() > 32000) valorStr = valorStr.substring(0, 32000);
                        cell.setCellValue(valorStr);
                    }
                    
                    cell.setCellStyle(linhaImpar ? dadosAlternadoStyle : dadosStyle);
                }
                
                linhaAtual++;
                if (linhaAtual % 500 == 0) {
                    labelStatus.setText("Linha " + (linhaAtual - 5) + "...");
                }
            }
            
            // ===== TOTAL =====
            linhaAtual++;
            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(linhaAtual);
            totalRow.setHeightInPoints(22);
            
            org.apache.poi.ss.usermodel.Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL DE REGISTROS:");
            totalLabelCell.setCellStyle(totalStyle);
            
            org.apache.poi.ss.usermodel.Cell totalValueCell = totalRow.createCell(1);
            totalValueCell.setCellValue(dados.size());
            totalValueCell.setCellStyle(totalStyle);
            
            // ===== AJUSTAR LARGURAS =====
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 3500);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 3500);
            sheet.setColumnWidth(7, 4000);
            
            // Congelar painéis
            sheet.createFreezePane(0, 5);
            
            // ===== SALVAR =====
            labelStatus.setText("Salvando...");
            fileOut = new java.io.FileOutputStream(caminhoArquivo);
            workbook.write(fileOut);
            fileOut.flush();
            
            System.out.println("✅ XLSX PROFISSIONAL: " + dados.size() + " linhas");
            
            labelStatus.setText("Concluído!");
            JOptionPane.showMessageDialog(this,
                    "✅ Relatório Excel Profissional criado!\n\n" +
                    "📁 Local: " + caminhoArquivo + "\n" +
                    "📊 Registros: " + dados.size() + "\n" +
                    "🎨 Design profissional com cores e formatação",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } finally {
            if (fileOut != null) {
                try { fileOut.close(); } catch (Exception e) { }
            }
            if (workbook != null) {
                try { workbook.close(); } catch (Exception e) { }
            }
        }
    }
    
    /**
     * Fallback para CSV quando Apache POI falha
     * Aceita qualquer tipo de erro (Throwable) incluindo ExceptionInInitializerError
     */
    private boolean exportarComCSVFallback(List<Map<String, Object>> dados, String[] colunas, String[] chaves,
                                          String titulo, String caminhoOriginal, Throwable erroOriginal) {
        try {
            // Alterar extensão para .csv
            String caminhoCSV = caminhoOriginal.replaceAll("\\.[^.]*$", "") + ".csv";
            
            labelStatus.setText("Usando formato CSV alternativo...");
            System.out.println("🔄 Fallback para CSV: " + caminhoCSV);
            
            // Exportar CSV
            java.io.FileWriter writer = new java.io.FileWriter(caminhoCSV);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(writer);
            
            // Escrever cabeçalhos
            for (int i = 0; i < colunas.length; i++) {
                bw.write("\"" + colunas[i] + "\"");
                if (i < colunas.length - 1) bw.write(",");
            }
            bw.newLine();
            
            // Escrever dados
            for (Map<String, Object> linha : dados) {
                for (int i = 0; i < chaves.length; i++) {
                    Object valor = linha.get(chaves[i]);
                    String valorStr = (valor != null) ? valor.toString().replace("\"", "\"\"") : "";
                    bw.write("\"" + valorStr + "\"");
                    if (i < chaves.length - 1) bw.write(",");
                }
                bw.newLine();
            }
            
            bw.close();
            
            // Diagnóstico do erro
            String tipoErro = erroOriginal.getClass().getSimpleName();
            String causaRaiz = "";
            
            if (erroOriginal instanceof ExceptionInInitializerError) {
                ExceptionInInitializerError initError = (ExceptionInInitializerError) erroOriginal;
                Throwable causa = initError.getCause();
                if (causa != null) {
                    causaRaiz = "\nCausa raiz: " + causa.getClass().getSimpleName() + " - " + causa.getMessage();
                }
            }
            
            labelStatus.setText("Arquivo CSV criado com sucesso!");
            
            String mensagemFinal = causaRaiz;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Apache POI não disponível!\n\n" +
                        "Erro detectado: " + tipoErro + mensagemFinal + "\n\n" +
                        "✅ SOLUÇÃO AUTOMÁTICA APLICADA:\n" +
                        "• Arquivo exportado em formato CSV\n" +
                        "• 100% compatível com Excel\n" +
                        "• Salvo em: " + caminhoCSV + "\n\n" +
                        "💡 COMO USAR:\n" +
                        "1. Abra o arquivo no Excel\n" +
                        "2. Excel reconhecerá automaticamente o formato\n" +
                        "3. Todos os dados estarão disponíveis\n\n" +
                        "📌 NOTA: Este é um problema de dependências do Apache POI,\n" +
                        "não um erro do sistema. O CSV funciona perfeitamente!",
                        "Exportação CSV - Alternativa Funcional",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Fallback CSV também falhou: " + e.getMessage());
            e.printStackTrace();
            
            labelStatus.setText("Erro no fallback CSV");
            JOptionPane.showMessageDialog(this,
                    "Erro ao exportar CSV alternativo:\n\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Exporta diretamente como CSV quando Apache POI não está disponível
     */
    private boolean exportarComCSVDireto(List<Map<String, Object>> dados, String[] colunas, String[] chaves,
                                         String titulo, String caminhoOriginal) {
        try {
            // Alterar extensão para .csv
            String caminhoCSV = caminhoOriginal.replaceAll("\\.[^.]*$", "") + ".csv";
            
            labelStatus.setText("Exportando como CSV...");
            System.out.println("📊 Exportação CSV direta: " + caminhoCSV);
            
            // Exportar CSV
            java.io.FileWriter writer = new java.io.FileWriter(caminhoCSV);
            java.io.BufferedWriter bw = new java.io.BufferedWriter(writer);
            
            // Escrever cabeçalhos
            for (int i = 0; i < colunas.length; i++) {
                bw.write("\"" + colunas[i] + "\"");
                if (i < colunas.length - 1) bw.write(",");
            }
            bw.newLine();
            
            // Escrever dados
            int contador = 0;
            for (Map<String, Object> linha : dados) {
                for (int i = 0; i < chaves.length; i++) {
                    Object valor = linha.get(chaves[i]);
                    String valorStr = (valor != null) ? valor.toString().replace("\"", "\"\"") : "";
                    bw.write("\"" + valorStr + "\"");
                    if (i < chaves.length - 1) bw.write(",");
                }
                bw.newLine();
                contador++;
                
                if (contador % 100 == 0) {
                    labelStatus.setText("Processando linha " + contador + "...");
                }
            }
            
            bw.close();
            
            final int totalLinhas = contador; // Tornar final para uso no lambda
            labelStatus.setText("Exportação CSV concluída!");
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "ℹ️ Apache POI não disponível\n\n" +
                        "✅ SOLUÇÃO AUTOMÁTICA:\n" +
                        "• Arquivo exportado em formato CSV\n" +
                        "• Totalmente compatível com Excel\n" +
                        "• Salvo em: " + caminhoCSV + "\n" +
                        "• Linhas: " + totalLinhas + "\n\n" +
                        "💡 Para usar Excel nativo:\n" +
                        "1. Reinicie a aplicação\n" +
                        "2. Verifique as dependências Maven",
                        "Exportação CSV",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Exportação CSV direta falhou: " + e.getMessage());
            e.printStackTrace();
            
            labelStatus.setText("Erro na exportação CSV");
            JOptionPane.showMessageDialog(this,
                    "Erro ao exportar CSV:\n\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Prepara dados da tabela para exportação Excel
     */
    private List<Map<String, Object>> prepararDadosTabela() {
        List<Map<String, Object>> dados = new ArrayList<>();

        try {
            System.out.println("\n=== PREPARANDO DADOS DA TABELA ===");
            int totalLinhas = tabelaPreview.getRowCount();
            int totalColunas = tabelaPreview.getColumnCount();

            System.out.println("Linhas na tabela: " + totalLinhas);
            System.out.println("Colunas na tabela: " + totalColunas);

            if (totalLinhas == 0) {
                System.out.println("❌ ERRO: Tabela vazia - nenhuma linha para processar");
                return dados;
            }

            if (totalColunas < 8) {
                System.out.println("⚠️ AVISO: Tabela tem apenas " + totalColunas + " colunas (esperado: 8)");
            }

            // Mostrar nomes das colunas
            System.out.println("Nomes das colunas:");
            for (int j = 0; j < totalColunas; j++) {
                String nomeColuna = tabelaPreview.getColumnName(j);
                System.out.println("  [" + j + "] " + nomeColuna);
            }

            int linhasProcessadas = 0;
            int linhasComErro = 0;

            for (int i = 0; i < totalLinhas; i++) {
                try {
                    Map<String, Object> linha = new HashMap<>();

                    // Extrair dados de cada coluna com sanitização e validação
                    Object numero = (totalColunas > 0) ? tabelaPreview.getValueAt(i, 0) : null;
                    linha.put("numero", sanitizarTexto(numero));

                    Object descricao = (totalColunas > 1) ? tabelaPreview.getValueAt(i, 1) : null;
                    linha.put("descricao", sanitizarTexto(descricao));

                    Object sala = (totalColunas > 2) ? tabelaPreview.getValueAt(i, 2) : null;
                    linha.put("sala", sanitizarTexto(sala));

                    Object estado = (totalColunas > 3) ? tabelaPreview.getValueAt(i, 3) : null;
                    linha.put("estado", sanitizarTexto(estado));

                    Object setor = (totalColunas > 4) ? tabelaPreview.getValueAt(i, 4) : null;
                    linha.put("setor", sanitizarTexto(setor));

                    Object responsavel = (totalColunas > 5) ? tabelaPreview.getValueAt(i, 5) : null;
                    linha.put("responsavel", sanitizarTexto(responsavel));

                    Object situacao = (totalColunas > 6) ? tabelaPreview.getValueAt(i, 6) : null;
                    linha.put("situacao", sanitizarTexto(situacao));

                    Object valor = (totalColunas > 7) ? tabelaPreview.getValueAt(i, 7) : null;
                    linha.put("valor", sanitizarTexto(valor));

                    dados.add(linha);
                    linhasProcessadas++;

                    // Log detalhado das primeiras 3 linhas
                    if (i < 3) {
                        System.out.println("Linha " + (i + 1) + " processada:");
                        System.out.println("  Número: " + linha.get("numero"));
                        System.out.println("  Descrição: " + linha.get("descricao"));
                        System.out.println("  Marca/Modelo: " + linha.get("marca_modelo"));
                        System.out.println("  Estado: " + linha.get("estado"));
                    }

                    if ((i + 1) % 100 == 0) {
                        System.out.println("Preparadas " + (i + 1) + " linhas...");
                    }

                } catch (Exception rowError) {
                    System.err.println("❌ Erro ao processar linha " + (i + 1) + ": " + rowError.getMessage());
                    linhasComErro++;

                    // Adicionar linha com dados de erro para não perder a sequência
                    Map<String, Object> linhaErro = new HashMap<>();
                    linhaErro.put("numero", "ERRO_LINHA_" + (i + 1));
                    linhaErro.put("descricao", "Erro ao processar dados");
                    linhaErro.put("marca_modelo", "");
                    linhaErro.put("estado", "");
                    linhaErro.put("setor", "");
                    linhaErro.put("responsavel", "");
                    linhaErro.put("situacao", "ERRO");
                    linhaErro.put("valor", "R$ 0,00");
                    dados.add(linhaErro);
                }
            }

            System.out.println("\n=== RESULTADO DA PREPARAÇÃO ===");
            System.out.println("✅ Linhas processadas com sucesso: " + linhasProcessadas);
            System.out.println("❌ Linhas com erro: " + linhasComErro);
            System.out.println("📊 Total de dados preparados: " + dados.size());
            System.out.println("===============================\n");

            return dados;

        } catch (Exception e) {
            System.err.println("❌ ERRO CRÍTICO ao preparar dados da tabela: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Sanitiza texto para Excel removendo caracteres problemáticos
     */
    private String sanitizarTexto(Object valor) {
        if (valor == null) {
            return "";
        }

        String texto = valor.toString().trim();

        // Remover caracteres de controle
        texto = texto.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // Remover caracteres Unicode problemáticos
        texto = texto.replaceAll("[\\uFFFE\\uFFFF]", "");

        // Normalizar quebras de linha
        texto = texto.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");

        // Remover múltiplos espaços
        texto = texto.replaceAll("\\s+", " ").trim();

        // Truncar se muito longo (Excel suporta máximo 32.767 caracteres por célula)
        if (texto.length() > 32767) {
            texto = texto.substring(0, 32764) + "...";
        }

        return texto;
    }

    /**
     * Gera nome de arquivo baseado no tipo de relatório
     */
    private String gerarNomeArquivo(String tipoRelatorio) {
        String nome = tipoRelatorio.replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        // Adicionar timestamp para evitar conflitos
        String timestamp = DateFormatUtils.formatDate(new Date()).replace("/", "");

        return "relatorio_" + nome + "_" + timestamp;
    }

    /**
     * Gera nome de arquivo PDF baseado no tipo de relatório com data de exportação
     */
    private String gerarNomeArquivoPDF(String tipoRelatorio) {
        String nome = tipoRelatorio.replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        // Usar formatação de data para nome de arquivo (yyyyMMdd_HHmmss)
        String timestamp = DateFormatUtils.formatForFilename(new Date());

        return "relatorio_" + nome + "_" + timestamp;
    }

    /**
     * Exporta os dados da tabela atual para Excel com tratamento robusto de erros
     * 
     * @deprecated Use exportarDadosTabelaOtimizado instead
     */
    @Deprecated
    private boolean exportarDadosTabela(String nomeArquivo) {
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = null;
        java.io.FileOutputStream fileOut = null;

        try {
            // Verificar se há dados para exportar
            if (tabelaPreview.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Não há dados para exportar. Gere um relatório primeiro.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            System.out.println("=== INICIANDO EXPORTAÇÃO EXCEL ===");
            System.out.println("Nome do arquivo: " + nomeArquivo);
            System.out.println("Linhas na tabela: " + tabelaPreview.getRowCount());
            System.out.println("Colunas na tabela: " + tabelaPreview.getColumnCount());

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos Excel", "xlsx"));
            fileChooser.setSelectedFile(new java.io.File(nomeArquivo + ".xlsx"));

            int resultado = fileChooser.showSaveDialog(this);
            if (resultado != JFileChooser.APPROVE_OPTION) {
                System.out.println("Exportação cancelada pelo usuário");
                return false;
            }

            String arquivo = fileChooser.getSelectedFile().getAbsolutePath();
            if (!arquivo.toLowerCase().endsWith(".xlsx")) {
                arquivo += ".xlsx";
            }

            System.out.println("Caminho do arquivo: " + arquivo);
            labelStatus.setText("Criando workbook Excel...");

            // Criar workbook do Excel com tratamento de erro
            try {
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                System.out.println("✅ Workbook criado com sucesso");
            } catch (Exception e) {
                System.err.println("❌ Erro ao criar workbook: " + e.getMessage());
                throw new RuntimeException("Falha ao criar workbook Excel: " + e.getMessage(), e);
            }

            labelStatus.setText("Criando planilha...");
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Relatório");
            System.out.println("✅ Sheet criada com sucesso");

            labelStatus.setText("Configurando estilos...");
            // Criar estilo para cabeçalho com tratamento de erro
            org.apache.poi.ss.usermodel.CellStyle headerStyle = null;
            try {
                headerStyle = workbook.createCellStyle();
                headerStyle
                        .setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                System.out.println("✅ Estilos criados com sucesso");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao criar estilos, continuando sem formatação: " + e.getMessage());
                headerStyle = null; // Continuar sem estilo
            }

            labelStatus.setText("Adicionando cabeçalhos...");
            // Adicionar cabeçalhos com tratamento de erro
            try {
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tabelaPreview.getColumnCount(); i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    String columnName = tabelaPreview.getColumnName(i);

                    // Sanitizar nome da coluna
                    if (columnName != null && columnName.length() > 32767) {
                        columnName = columnName.substring(0, 32764) + "...";
                    }

                    cell.setCellValue(columnName != null ? columnName : "Coluna " + (i + 1));
                    if (headerStyle != null) {
                        cell.setCellStyle(headerStyle);
                    }
                }
                System.out.println("✅ Cabeçalhos adicionados: " + tabelaPreview.getColumnCount() + " colunas");
            } catch (Exception e) {
                System.err.println("❌ Erro ao adicionar cabeçalhos: " + e.getMessage());
                throw new RuntimeException("Falha ao adicionar cabeçalhos: " + e.getMessage(), e);
            }

            labelStatus.setText("Adicionando dados...");
            // Adicionar dados com tratamento robusto de erro
            int linhasProcessadas = 0;
            try {
                for (int i = 0; i < tabelaPreview.getRowCount(); i++) {
                    try {
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);

                        for (int j = 0; j < tabelaPreview.getColumnCount(); j++) {
                            try {
                                org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                                Object value = tabelaPreview.getValueAt(i, j);

                                if (value != null) {
                                    String stringValue = value.toString();

                                    // Sanitizar valor para Excel
                                    if (stringValue.length() > 32767) {
                                        stringValue = stringValue.substring(0, 32764) + "...";
                                        System.out.println(
                                                "⚠️ Valor truncado na linha " + (i + 1) + ", coluna " + (j + 1));
                                    }

                                    // Remover caracteres problemáticos
                                    stringValue = stringValue.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

                                    cell.setCellValue(stringValue);
                                } else {
                                    cell.setCellValue("");
                                }
                            } catch (Exception cellError) {
                                System.err.println("⚠️ Erro na célula [" + (i + 1) + "," + (j + 1) + "]: "
                                        + cellError.getMessage());
                                // Continuar com célula vazia em caso de erro
                                try {
                                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                                    cell.setCellValue("ERRO_DADOS");
                                } catch (Exception e2) {
                                    // Ignorar se não conseguir nem criar célula vazia
                                }
                            }
                        }

                        linhasProcessadas++;
                        if (linhasProcessadas % 100 == 0) {
                            System.out.println("Processadas " + linhasProcessadas + " linhas...");
                            labelStatus.setText("Processando linha " + linhasProcessadas + "...");
                        }

                    } catch (Exception rowError) {
                        System.err.println("⚠️ Erro na linha " + (i + 1) + ": " + rowError.getMessage());
                        // Continuar com próxima linha
                    }
                }
                System.out.println("✅ Dados adicionados: " + linhasProcessadas + " linhas processadas");
            } catch (Exception e) {
                System.err.println("❌ Erro geral ao adicionar dados: " + e.getMessage());
                if (linhasProcessadas == 0) {
                    throw new RuntimeException("Falha ao processar dados da tabela: " + e.getMessage(), e);
                } else {
                    System.out.println("⚠️ Continuando com " + linhasProcessadas + " linhas processadas");
                }
            }

            labelStatus.setText("Ajustando colunas...");
            // Auto-ajustar colunas com tratamento de erro
            try {
                for (int i = 0; i < tabelaPreview.getColumnCount(); i++) {
                    try {
                        sheet.autoSizeColumn(i);
                    } catch (Exception e) {
                        System.err.println("⚠️ Erro ao ajustar coluna " + i + ": " + e.getMessage());
                        // Continuar com próxima coluna
                    }
                }
                System.out.println("✅ Colunas ajustadas");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao ajustar colunas: " + e.getMessage());
                // Continuar sem ajuste automático
            }

            labelStatus.setText("Salvando arquivo...");
            // Salvar arquivo com tratamento de erro
            try {
                fileOut = new java.io.FileOutputStream(arquivo);
                workbook.write(fileOut);
                fileOut.close();
                fileOut = null; // Marcar como fechado
                System.out.println("✅ Arquivo salvo com sucesso: " + arquivo);
            } catch (Exception e) {
                System.err.println("❌ Erro ao salvar arquivo: " + e.getMessage());
                throw new RuntimeException("Falha ao salvar arquivo Excel: " + e.getMessage(), e);
            }

            // Fechar workbook
            try {
                workbook.close();
                workbook = null; // Marcar como fechado
                System.out.println("✅ Workbook fechado com sucesso");
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao fechar workbook: " + e.getMessage());
                // Não é crítico se não conseguir fechar
            }

            labelStatus.setText("Exportação concluída com sucesso!");
            JOptionPane.showMessageDialog(this,
                    "Relatório exportado com sucesso!\n\nArquivo: " + arquivo +
                            "\nLinhas processadas: " + linhasProcessadas,
                    "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);

            System.out.println("=== EXPORTAÇÃO EXCEL CONCLUÍDA COM SUCESSO ===");
            return true;

        } catch (Exception e) {
            System.err.println("❌ ERRO GERAL NA EXPORTAÇÃO EXCEL:");
            System.err.println("Tipo do erro: " + e.getClass().getSimpleName());
            System.err.println("Mensagem: " + e.getMessage());
            e.printStackTrace();

            labelStatus.setText("Erro na exportação Excel");

            // Determinar tipo de erro e mostrar mensagem apropriada
            String mensagemErro;
            String tipoErro;

            if (e.getMessage() != null) {
                if (e.getMessage().contains("32767")) {
                    tipoErro = "Dados Muito Longos";
                    mensagemErro = "Alguns dados na tabela são muito longos para o Excel.\n" +
                            "O Excel suporta no máximo 32.767 caracteres por célula.\n\n" +
                            "Tente filtrar os dados ou use exportação CSV/TXT.";
                } else if (e.getMessage().contains("CTWorkbook") || e.getMessage().contains("XMLBeans")) {
                    tipoErro = "Problema de Dependências";
                    mensagemErro = "Problema com as bibliotecas do Apache POI.\n\n" +
                            "Soluções:\n" +
                            "1. Reinicie a aplicação\n" +
                            "2. Use exportação CSV/HTML/TXT como alternativa\n" +
                            "3. Contate o suporte técnico";
                } else if (e.getMessage().contains("FileOutputStream") || e.getMessage().contains("IOException")) {
                    tipoErro = "Erro de Arquivo";
                    mensagemErro = "Não foi possível salvar o arquivo.\n\n" +
                            "Verifique:\n" +
                            "1. Se você tem permissão para escrever no local\n" +
                            "2. Se o arquivo não está aberto em outro programa\n" +
                            "3. Se há espaço suficiente no disco";
                } else {
                    tipoErro = "Erro Desconhecido";
                    mensagemErro = "Erro inesperado durante a exportação:\n\n" + e.getMessage() +
                            "\n\nTente usar as alternativas CSV/HTML/TXT.";
                }
            } else {
                tipoErro = "Erro Interno";
                mensagemErro = "Erro interno da aplicação.\n\nTente reiniciar e usar exportação alternativa.";
            }

            JOptionPane.showMessageDialog(this,
                    mensagemErro,
                    "Erro na Exportação Excel - " + tipoErro,
                    JOptionPane.ERROR_MESSAGE);

            return false;

        } finally {
            // Garantir que recursos sejam liberados
            if (fileOut != null) {
                try {
                    fileOut.close();
                    System.out.println("FileOutputStream fechado no finally");
                } catch (Exception e) {
                    System.err.println("Erro ao fechar FileOutputStream no finally: " + e.getMessage());
                }
            }

            if (workbook != null) {
                try {
                    workbook.close();
                    System.out.println("Workbook fechado no finally");
                } catch (Exception e) {
                    System.err.println("Erro ao fechar workbook no finally: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Exporta relatório geral consolidado com tratamento de erro
     */
    private void exportarRelatorioGeral() {
        try {
            int idInventario = obterIdInventarioSelecionado();
            if (idInventario == -1) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um inventário primeiro.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            labelStatus.setText("Gerando relatório geral...");
            System.out.println("=== EXPORTANDO RELATÓRIO GERAL ===");
            System.out.println("ID do inventário: " + idInventario);

            boolean sucesso = false;
            try {
                sucesso = excelGenerator.gerarRelatorioGeral(idInventario, (JComponent) this.getContentPane());
            } catch (Exception e) {
                System.err.println("❌ Erro no RelatorioExcelGenerator.gerarRelatorioGeral: " + e.getMessage());
                e.printStackTrace();

                // Tentar método alternativo usando dados da tabela se houver
                if (tabelaPreview.getRowCount() > 0) {
                    System.out.println("🔄 Tentando método alternativo com dados da tabela...");
                    labelStatus.setText("Tentando método alternativo...");

                    try {
                        sucesso = exportarDadosTabela("Relatorio_Geral_Alternativo");
                        if (sucesso) {
                            JOptionPane.showMessageDialog(this,
                                    "Relatório geral exportado usando método alternativo.\n\n" +
                                            "Os dados da tabela atual foram exportados com sucesso.",
                                    "Exportação Alternativa", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception altError) {
                        System.err.println("❌ Método alternativo também falhou: " + altError.getMessage());
                    }
                }

                if (!sucesso) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao gerar relatório geral:\n\n" + e.getMessage() +
                                    "\n\nTente usar 'Exportar Relatório Atual' ou formatos alternativos (CSV/HTML/TXT).",
                            "Erro na Exportação", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (sucesso) {
                labelStatus.setText("Relatório geral exportado com sucesso!");
                System.out.println("✅ Relatório geral exportado com sucesso");
            } else {
                labelStatus.setText("Falha na exportação do relatório geral");
                System.out.println("❌ Falha na exportação do relatório geral");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro geral em exportarRelatorioGeral: " + e.getMessage());
            e.printStackTrace();
            labelStatus.setText("Erro geral na exportação");
            JOptionPane.showMessageDialog(this,
                    "Erro inesperado: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exporta relatório de estatísticas com tratamento de erro
     */
    private void exportarEstatisticas() {
        try {
            int idInventario = obterIdInventarioSelecionado();
            if (idInventario == -1) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um inventário primeiro.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            labelStatus.setText("Gerando relatório de estatísticas...");
            System.out.println("=== EXPORTANDO RELATÓRIO DE ESTATÍSTICAS ===");
            System.out.println("ID do inventário: " + idInventario);

            boolean sucesso = false;
            try {
                sucesso = excelGenerator.gerarRelatorioEstatisticas(idInventario, (JComponent) this.getContentPane());
            } catch (Exception e) {
                System.err.println("❌ Erro no RelatorioExcelGenerator.gerarRelatorioEstatisticas: " + e.getMessage());
                e.printStackTrace();

                // Tentar método alternativo usando dados da tabela se houver
                if (tabelaPreview.getRowCount() > 0) {
                    System.out.println("🔄 Tentando método alternativo com dados da tabela...");
                    labelStatus.setText("Tentando método alternativo...");

                    try {
                        sucesso = exportarDadosTabela("Estatisticas_Alternativo");
                        if (sucesso) {
                            JOptionPane.showMessageDialog(this,
                                    "Estatísticas exportadas usando método alternativo.\n\n" +
                                            "Os dados da tabela atual foram exportados com sucesso.",
                                    "Exportação Alternativa", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception altError) {
                        System.err.println("❌ Método alternativo também falhou: " + altError.getMessage());
                    }
                }

                if (!sucesso) {
                    JOptionPane.showMessageDialog(this,
                            "Erro ao gerar relatório de estatísticas:\n\n" + e.getMessage() +
                                    "\n\nTente usar 'Exportar Relatório Atual' ou formatos alternativos (CSV/HTML/TXT).",
                            "Erro na Exportação", JOptionPane.ERROR_MESSAGE);
                }
            }

            if (sucesso) {
                labelStatus.setText("Relatório de estatísticas exportado com sucesso!");
                System.out.println("✅ Relatório de estatísticas exportado com sucesso");
            } else {
                labelStatus.setText("Falha na exportação das estatísticas");
                System.out.println("❌ Falha na exportação das estatísticas");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro geral em exportarEstatisticas: " + e.getMessage());
            e.printStackTrace();
            labelStatus.setText("Erro geral na exportação");
            JOptionPane.showMessageDialog(this,
                    "Erro inesperado: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Aplica estilo moderno ao frame
     */
    private void aplicarEstiloModerno() {
        // Configurar cores do frame
        getContentPane().setBackground(new Color(245, 245, 245));

        // Estilizar componentes de filtro
        estilizarComboBox(comboTipoRelatorio);
        estilizarComboBox(comboSetor);
        estilizarComboBox(comboResponsavel);
        estilizarComboBox(comboStatus);
        estilizarComboBox(comboInventario);

        estilizarSpinner(spinnerDataInicio);
        estilizarSpinner(spinnerDataFim);

        estilizarCheckBox(checkIncluirImagens);
        estilizarCheckBox(checkAgruparPorSetor);

        // Estilizar tabela
        estilizarTabela();

        // Estilizar abas
        estilizarAbas();
    }

    private void estilizarComboBox(JComboBox<?> combo) {
        combo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));

        // Estilizar o editor do spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(Color.WHITE);
            ((JSpinner.DefaultEditor) editor).getTextField().setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        }
    }

    private void estilizarCheckBox(JCheckBox checkBox) {
        checkBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        checkBox.setForeground(new Color(60, 60, 60));
        checkBox.setBackground(Color.WHITE);
        checkBox.setFocusPainted(false);
    }

    private void estilizarTabela() {
        if (tabelaPreview != null) {
            // Estilizar fonte e cores
            tabelaPreview.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            tabelaPreview.setSelectionBackground(new Color(184, 207, 229));
            tabelaPreview.setSelectionForeground(Color.BLACK);
            tabelaPreview.setGridColor(new Color(230, 230, 230));
            tabelaPreview.setBackground(Color.WHITE);

            // Estilizar cabeçalho
            if (tabelaPreview.getTableHeader() != null) {
                tabelaPreview.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                tabelaPreview.getTableHeader().setBackground(new Color(248, 249, 250));
                tabelaPreview.getTableHeader().setForeground(new Color(60, 60, 60));
                tabelaPreview.getTableHeader()
                        .setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
            }

            // Renderizador para linhas alternadas e cores por status
            tabelaPreview.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    if (!isSelected) {
                        // Obter o status da coluna "Status da Coleta" (assumindo que é a coluna 3)
                        String status = "";
                        try {
                            if (table.getColumnCount() > 3) {
                                Object statusObj = table.getValueAt(row, 3);
                                status = statusObj != null ? statusObj.toString().trim().toUpperCase() : "";
                            }
                        } catch (Exception e) {
                            // Se houver erro, usar cor padrão
                        }

                        // Aplicar cores baseadas no status
                        Color corFundo = Color.WHITE;
                        Color corTexto = Color.BLACK;
                        
                        switch (status) {
                            case "ENCONTRADO":
                            case "FOUND":
                                corFundo = new Color(212, 237, 218); // Verde claro
                                corTexto = new Color(21, 87, 36);    // Verde escuro
                                break;
                            case "NÃO ENCONTRADO":
                            case "NOT FOUND":
                                corFundo = new Color(248, 215, 218); // Vermelho claro
                                corTexto = new Color(114, 28, 36);   // Vermelho escuro
                                break;
                            case "DANIFICADO":
                            case "DAMAGED":
                                corFundo = new Color(255, 243, 205); // Amarelo claro
                                corTexto = new Color(133, 100, 4);   // Amarelo escuro
                                break;
                            case "NÃO COLETADO":
                            case "NOT COLLECTED":
                                corFundo = new Color(230, 230, 230); // Cinza claro
                                corTexto = new Color(73, 80, 87);    // Cinza escuro
                                break;
                            default:
                                // Linhas alternadas para outros casos
                                if (row % 2 == 0) {
                                    corFundo = Color.WHITE;
                                } else {
                                    corFundo = new Color(248, 249, 250);
                                }
                                break;
                        }
                        
                        c.setBackground(corFundo);
                        c.setForeground(corTexto);
                    }

                    return c;
                }
            });
        }
    }

    private void estilizarAbas() {
        // Buscar o JTabbedPane no layout
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane abas = (JTabbedPane) comp;
                abas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                abas.setBackground(new Color(245, 245, 245));
                abas.setForeground(new Color(60, 60, 60));
                break;
            }
        }
    }
}





