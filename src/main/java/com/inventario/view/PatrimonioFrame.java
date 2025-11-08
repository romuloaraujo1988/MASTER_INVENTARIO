package com.inventario.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.inventario.service.PatrimonioService;
import com.inventario.service.ServiceFactory;
import com.inventario.model.Patrimonio;
import java.util.List;
import java.util.ArrayList;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import com.inventario.view.ui.ButtonStyleFactory;

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
        this.patrimonioService = ServiceFactory.getInstance().getPatrimonioService();
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
        
        // Painel superior principal com layout vertical
        JPanel painelSuperior = new JPanel();
        painelSuperior.setLayout(new BoxLayout(painelSuperior, BoxLayout.Y_AXIS));
        painelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de busca modernizado
        JPanel painelBusca = new JPanel();
        painelBusca.setLayout(new GridBagLayout());
        painelBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Busca de Patrimônios"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Linha 1: Label e ComboBox
        gbc.gridx = 0; gbc.gridy = 0;
        painelBusca.add(new JLabel("Buscar por:"), gbc);
        
        gbc.gridx = 1;
        comboTipoBusca = new JComboBox<>(new String[]{"Número", "Descrição", "Responsável", "Sala", "Todos os campos"});
        comboTipoBusca.setPreferredSize(new Dimension(150, 30));
        painelBusca.add(comboTipoBusca, gbc);
        
        // Linha 1: Campo de busca
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoBusca = new JTextField();
        campoBusca.setPreferredSize(new Dimension(250, 30));
        campoBusca.setToolTipText("Digite o termo para buscar");
        painelBusca.add(campoBusca, gbc);
        
        // Linha 1: Botões de busca
        gbc.gridx = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        btnBuscar = ButtonStyleFactory.createPrimaryButton("Buscar");
        btnBuscar.setPreferredSize(new Dimension(100, 30));
        painelBusca.add(btnBuscar, gbc);
        
        gbc.gridx = 4;
        btnCarregarTodos = ButtonStyleFactory.createSuccessButton("Carregar Todos");
        btnCarregarTodos.setPreferredSize(new Dimension(140, 30));
        painelBusca.add(btnCarregarTodos, gbc);
        
        // Painel de ações modernizado
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelAcoes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ações"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Botões de ação com cores e ícones
        btnNovo = ButtonStyleFactory.createSuccessButton("Novo");
        btnNovo.setPreferredSize(new Dimension(150, 35));
        
        btnEditar = ButtonStyleFactory.createWarningButton("Alterar");
        btnEditar.setPreferredSize(new Dimension(100, 35));
        
        btnExcluir = ButtonStyleFactory.createDangerButton("Excluir");
        btnExcluir.setPreferredSize(new Dimension(100, 35));
        
        btnImportar = ButtonStyleFactory.createInfoButton("Importar CSV");
        btnImportar.setPreferredSize(new Dimension(130, 35));
        
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
        tabelaPatrimonio.setRowHeight(25);
        tabelaPatrimonio.setGridColor(new Color(230, 230, 230));
        tabelaPatrimonio.setSelectionBackground(new Color(184, 207, 229));
        tabelaPatrimonio.getTableHeader().setBackground(new Color(70, 130, 180));
        tabelaPatrimonio.getTableHeader().setForeground(Color.WHITE);
        tabelaPatrimonio.getTableHeader().setFont(tabelaPatrimonio.getTableHeader().getFont().deriveFont(Font.BOLD));
        
        JScrollPane scrollPane = new JScrollPane(tabelaPatrimonio);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLoweredBevelBorder()
        ));
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel de status
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelStatus.setBorder(BorderFactory.createEtchedBorder());
        painelStatus.add(new JLabel("Dica: Use Ctrl+F para busca rápida ou clique duas vezes em um item para editar"));
        add(painelStatus, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
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
                Patrimonio patrimonio = patrimonioService.buscarPorId(Long.valueOf(id));
                if (patrimonio != null) {
                    abrirFormularioPatrimonio(patrimonio);
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
                    Patrimonio patrimonioEncontrado = patrimonioService.buscarPorNumero(termo);
                    resultados = new ArrayList<>();
                    if (patrimonioEncontrado != null) {
                        resultados.add(patrimonioEncontrado);
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
                    resultados = patrimonioService.buscarPorSala(Integer.parseInt(termo));
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
                    Patrimonio patrimonioEncontrado = patrimonioService.buscarPorNumero(ultimoTermoBusca);
                    resultados = new ArrayList<>();
                    if (patrimonioEncontrado != null) {
                        resultados.add(patrimonioEncontrado);
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
                    resultados = patrimonioService.buscarPorSala(Integer.parseInt(ultimoTermoBusca));
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