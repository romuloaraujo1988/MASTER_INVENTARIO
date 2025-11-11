package com.inventario.view;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.inventario.model.Patrimonio;
import com.inventario.model.Sala;
import com.inventario.model.Responsavel;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.dao.SalaDAORefactored;
import com.inventario.dao.ResponsavelDAO;

/**
 * Formulário para adicionar/editar patrimônios
 */
public class PatrimonioFormDialog extends JDialog {
    private Patrimonio patrimonio;
    private boolean confirmado = false;
    
    // Campos do formulário
    private JTextField campoNumero;
    private JTextField campoDescricao;
    private JTextField campoDescricaoResumida;
    private JTextField campoMarca;
    private JTextField campoModelo;
    private JTextField campoNumeroSerie;
    private JComboBox<String> comboEstado;
    private JComboBox<String> comboSituacao;
    private JComboBox<SalaItem> comboSala;
    private JComboBox<ResponsavelItem> comboResponsavel;
    
    // Classes auxiliares para os comboboxes
    private static class SalaItem {
        private final Sala sala;
        
        public SalaItem(Sala sala) {
            this.sala = sala;
        }
        
        public Sala getSala() {
            return sala;
        }
        
        @Override
        public String toString() {
            if (sala == null) return "Selecione uma sala...";
            return sala.getNumeroSala();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SalaItem salaItem = (SalaItem) obj;
            if (sala == null && salaItem.sala == null) return true;
            if (sala == null || salaItem.sala == null) return false;
            return sala.getIdSala() == salaItem.sala.getIdSala();
        }
    }
    
    private static class ResponsavelItem {
        private final Responsavel responsavel;
        
        public ResponsavelItem(Responsavel responsavel) {
            this.responsavel = responsavel;
        }
        
        public Responsavel getResponsavel() {
            return responsavel;
        }
        
        @Override
        public String toString() {
            if (responsavel == null) return "Selecione um responsável...";
            return responsavel.getNome() + (responsavel.getCargo() != null ? " - " + responsavel.getCargo() : "");
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ResponsavelItem responsavelItem = (ResponsavelItem) obj;
            if (responsavel == null && responsavelItem.responsavel == null) return true;
            if (responsavel == null || responsavelItem.responsavel == null) return false;
            return responsavel.getId() == responsavelItem.responsavel.getId();
        }
    }
    private JTextField campoValor;
    private JSpinner spinnerDataAquisicao;
    private JTextArea areaObservacoes;
    
    private JButton btnSalvar, btnCancelar;
    
    public PatrimonioFormDialog(Frame parent, Patrimonio patrimonio) {
        super(parent, patrimonio == null ? "Novo Patrimônio" : "Editar Patrimônio", true);
        this.patrimonio = patrimonio;
        initComponents();
        if (patrimonio != null) {
            preencherCampos();
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal com campos usando BoxLayout
        JPanel painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));
        painelCampos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Dados do Patrimônio"),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        painelCampos.setBackground(Color.WHITE);
        
        // Criar painéis para cada campo
        campoNumero = new JTextField(25);
        boolean novoRegistro = (patrimonio == null || patrimonio.getId() == 0);
        campoNumero.setEditable(novoRegistro); // Editável apenas no cadastro manual
        campoNumero.setBackground(novoRegistro ? Color.WHITE : new Color(245, 245, 245));
        painelCampos.add(criarPainelCampo("Número:", campoNumero));
        painelCampos.add(Box.createVerticalStrut(8));
        
        painelCampos.add(criarPainelCampo("Descrição:", campoDescricao = new JTextField(25)));
        painelCampos.add(Box.createVerticalStrut(8));
        
        // Campo de descrição resumida com botão para gerar automaticamente
        JPanel painelDescricaoResumida = new JPanel(new BorderLayout(5, 5));
        painelDescricaoResumida.setBackground(Color.WHITE);
        JLabel labelDescricaoResumida = new JLabel("Descrição Resumida:");
        labelDescricaoResumida.setFont(labelDescricaoResumida.getFont().deriveFont(Font.BOLD));
        labelDescricaoResumida.setPreferredSize(new Dimension(140, labelDescricaoResumida.getPreferredSize().height));
        painelDescricaoResumida.add(labelDescricaoResumida, BorderLayout.WEST);
        
        JPanel painelCampoResumo = new JPanel(new BorderLayout(5, 0));
        painelCampoResumo.setBackground(Color.WHITE);
        campoDescricaoResumida = new JTextField(25);
        campoDescricaoResumida.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        campoDescricaoResumida.setPreferredSize(new Dimension(250, 25));
        
        btnGerarResumo = new JButton("Gerar");
        btnGerarResumo.setPreferredSize(new Dimension(60, 25));
        btnGerarResumo.setBackground(new Color(40, 167, 69));
        btnGerarResumo.setForeground(Color.WHITE);
        btnGerarResumo.setFocusPainted(false);
        btnGerarResumo.setBorder(BorderFactory.createRaisedBevelBorder());
        btnGerarResumo.setToolTipText("Gerar resumo automaticamente da descrição");
        
        painelCampoResumo.add(campoDescricaoResumida, BorderLayout.CENTER);
        painelCampoResumo.add(btnGerarResumo, BorderLayout.EAST);
        painelDescricaoResumida.add(painelCampoResumo, BorderLayout.CENTER);
        painelDescricaoResumida.setMaximumSize(new Dimension(Integer.MAX_VALUE, painelDescricaoResumida.getPreferredSize().height));
        
        painelCampos.add(painelDescricaoResumida);
        painelCampos.add(Box.createVerticalStrut(8));
        
        painelCampos.add(criarPainelCampo("Marca:", campoMarca = new JTextField(25)));
        painelCampos.add(Box.createVerticalStrut(8));
        
        painelCampos.add(criarPainelCampo("Modelo:", campoModelo = new JTextField(25)));
        painelCampos.add(Box.createVerticalStrut(8));
        
        painelCampos.add(criarPainelCampo("Número de Série:", campoNumeroSerie = new JTextField(25)));
        painelCampos.add(Box.createVerticalStrut(8));
        
        comboEstado = new JComboBox<>(new String[]{"BOM", "REGULAR", "RUIM", "INSERVÍVEL"});
        comboEstado.setPreferredSize(new Dimension(200, 25));
        painelCampos.add(criarPainelCampo("Estado:", comboEstado));
        painelCampos.add(Box.createVerticalStrut(8));
        
        comboSituacao = new JComboBox<>(new String[]{"ATIVO", "PENDENTE", "BAIXADO", "ESTORNADO"});
        comboSituacao.setPreferredSize(new Dimension(200, 25));
        painelCampos.add(criarPainelCampo("Situação:", comboSituacao));
        painelCampos.add(Box.createVerticalStrut(8));
        
        comboSala = new JComboBox<>();
        comboSala.setPreferredSize(new Dimension(200, 25));
        carregarSalas();
        painelCampos.add(criarPainelCampo("Sala:", comboSala));
        painelCampos.add(Box.createVerticalStrut(8));
        
        comboResponsavel = new JComboBox<>();
        comboResponsavel.setPreferredSize(new Dimension(200, 25));
        carregarResponsaveis();
        painelCampos.add(criarPainelCampo("Responsável:", comboResponsavel));
        painelCampos.add(Box.createVerticalStrut(8));
        
        painelCampos.add(criarPainelCampo("Valor:", campoValor = new JTextField(25)));
        painelCampos.add(Box.createVerticalStrut(8));
        
        spinnerDataAquisicao = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerDataAquisicao, "dd/MM/yyyy");
        spinnerDataAquisicao.setEditor(editor);
        spinnerDataAquisicao.setPreferredSize(new Dimension(200, 25));
        painelCampos.add(criarPainelCampo("Data de Aquisição:", spinnerDataAquisicao));
        painelCampos.add(Box.createVerticalStrut(8));
        
        // Observações com área de texto
        JPanel painelObservacoes = new JPanel(new BorderLayout(5, 5));
        JLabel labelObservacoes = new JLabel("Observações:");
        labelObservacoes.setFont(labelObservacoes.getFont().deriveFont(Font.BOLD));
        labelObservacoes.setPreferredSize(new Dimension(140, labelObservacoes.getPreferredSize().height));
        painelObservacoes.add(labelObservacoes, BorderLayout.WEST);
        areaObservacoes = new JTextArea(4, 25);
        areaObservacoes.setLineWrap(true);
        areaObservacoes.setWrapStyleWord(true);
        areaObservacoes.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollObservacoes = new JScrollPane(areaObservacoes);
        scrollObservacoes.setPreferredSize(new Dimension(300, 80));
        painelObservacoes.add(scrollObservacoes, BorderLayout.CENTER);
        painelObservacoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        painelCampos.add(painelObservacoes);
        
        // Adicionar painel principal em um scroll
        JScrollPane scrollPrincipal = new JScrollPane(painelCampos);
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPrincipal, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        painelBotoes.setBackground(new Color(240, 240, 240));
        painelBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        
        btnSalvar = new JButton("Salvar");
        btnSalvar.setPreferredSize(new Dimension(100, 35));
        btnSalvar.setBackground(new Color(0, 123, 255));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setBorder(BorderFactory.createRaisedBevelBorder());
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createRaisedBevelBorder());
        
        painelBotoes.add(btnCancelar);
        painelBotoes.add(btnSalvar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(750, 650);
        setLocationRelativeTo(getParent());
    }
    
    private JPanel criarPainelCampo(String labelTexto, JComponent componente) {
        JPanel painel = new JPanel(new BorderLayout(10, 5));
        painel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelTexto);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setPreferredSize(new Dimension(140, label.getPreferredSize().height));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        painel.add(label, BorderLayout.WEST);
        
        // Adicionar borda sutil aos campos de texto
        if (componente instanceof JTextField) {
            JTextField textField = (JTextField) componente;
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
            textField.setPreferredSize(new Dimension(300, 25));
        }
        
        painel.add(componente, BorderLayout.CENTER);
        painel.setMaximumSize(new Dimension(Integer.MAX_VALUE, painel.getPreferredSize().height));
        return painel;
    }
    
    private JButton btnGerarResumo; // Adicionar como campo da classe
    
    private void configurarEventos() {
        btnSalvar.addActionListener(e -> salvarPatrimonio());
        btnCancelar.addActionListener(e -> dispose());
        
        // Configurar evento do botão gerar resumo
        if (btnGerarResumo != null) {
            btnGerarResumo.addActionListener(e -> gerarResumoDescricao());
        }
    }
    
    private void gerarResumoDescricao() {
        String descricao = campoDescricao.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite uma descrição primeiro para gerar o resumo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Usar o serviço de resumo
            String resumo = com.inventario.service.DescricaoResumoService.gerarResumo(descricao);
            campoDescricaoResumida.setText(resumo);
            
            // Mostrar mensagem de sucesso
            JOptionPane.showMessageDialog(this, "Resumo gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar resumo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void carregarSalas() {
        try {
            comboSala.removeAllItems();
            comboSala.addItem(new SalaItem(null)); // Item padrão
            
            // === SWING: Usar DAO diretamente (sem Spring) ===
            SalaDAORefactored salaDAO = new SalaDAORefactored();
            List<Sala> salas = salaDAO.listarSalas();
            
            for (Sala sala : salas) {
                comboSala.addItem(new SalaItem(sala));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar salas: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void carregarResponsaveis() {
        try {
            comboResponsavel.removeAllItems();
            comboResponsavel.addItem(new ResponsavelItem(null)); // Item padrão
            
            // === SWING: Usar DAO diretamente (sem Spring) ===
            ResponsavelDAO responsavelDAO = new ResponsavelDAO();
            List<Responsavel> responsaveis = responsavelDAO.findAll();
            
            for (Responsavel responsavel : responsaveis) {
                comboResponsavel.addItem(new ResponsavelItem(responsavel));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar responsáveis: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void preencherCampos() {
        if (patrimonio != null) {
            campoNumero.setText(patrimonio.getNumero());
            campoDescricao.setText(patrimonio.getDescricao());
            // Posicionar cursor no início do campo de descrição
            campoDescricao.setCaretPosition(0);
            
            // Preencher descrição resumida
            campoDescricaoResumida.setText(patrimonio.getDescricaoResumidaOuGerada());
            campoMarca.setText(patrimonio.getMarca());
        campoModelo.setText(patrimonio.getModelo());
            campoNumeroSerie.setText(patrimonio.getNumeroSerie());
            
            if (patrimonio.getEstadoConservacao() != null) {
                comboEstado.setSelectedItem(patrimonio.getEstadoConservacao());
            }
            
            if (patrimonio.getSituacao() != null) {
                comboSituacao.setSelectedItem(patrimonio.getSituacao());
            } else {
                comboSituacao.setSelectedItem("ATIVO"); // Padrão
            }
            
            if (patrimonio.getValorAquisicao() != null) {
                campoValor.setText(patrimonio.getValorAquisicao().toString());
            }
            
            if (patrimonio.getDataAquisicao() != null) {
                spinnerDataAquisicao.setValue(patrimonio.getDataAquisicao());
            }
            
            areaObservacoes.setText(patrimonio.getObservacoes());
            
            // Selecionar sala correta
            if (patrimonio.getIdSala() > 0) {
                for (int i = 0; i < comboSala.getItemCount(); i++) {
                    SalaItem item = comboSala.getItemAt(i);
                    if (item.getSala() != null && item.getSala().getIdSala() == patrimonio.getIdSala()) {
                        comboSala.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Selecionar responsável correto
            if (patrimonio.getIdResponsavel() > 0) {
                for (int i = 0; i < comboResponsavel.getItemCount(); i++) {
                    ResponsavelItem item = comboResponsavel.getItemAt(i);
                    if (item.getResponsavel() != null && item.getResponsavel().getId() == patrimonio.getIdResponsavel()) {
                        comboResponsavel.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    
    private void salvarPatrimonio() {
        try {
            // Validar campos obrigatórios
            if (campoNumero.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O número do patrimônio é obrigatório.");
                return;
            }
            
            if (campoDescricao.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "A descrição é obrigatória.");
                return;
            }
            
            // Criar ou atualizar patrimônio
            if (patrimonio == null) {
                patrimonio = new Patrimonio();
            }
            
            patrimonio.setNumero(campoNumero.getText().trim());
            patrimonio.setDescricao(campoDescricao.getText().trim());
            patrimonio.setDescricaoResumida(campoDescricaoResumida.getText().trim());
            patrimonio.setMarca(campoMarca.getText().trim());
        patrimonio.setModelo(campoModelo.getText().trim());
            patrimonio.setNumeroSerie(campoNumeroSerie.getText().trim());
            patrimonio.setEstadoConservacao((String) comboEstado.getSelectedItem());
            patrimonio.setSituacao((String) comboSituacao.getSelectedItem());
            patrimonio.setObservacoes(areaObservacoes.getText().trim());
            
            // Definir sala selecionada
            SalaItem salaItem = (SalaItem) comboSala.getSelectedItem();
            if (salaItem != null && salaItem.getSala() != null) {
                patrimonio.setIdSala(salaItem.getSala().getIdSala());
            } else {
                patrimonio.setIdSala(0);
            }
            
            // Definir responsável selecionado
            ResponsavelItem responsavelItem = (ResponsavelItem) comboResponsavel.getSelectedItem();
            if (responsavelItem != null && responsavelItem.getResponsavel() != null) {
                patrimonio.setIdResponsavel(responsavelItem.getResponsavel().getId());
            } else {
                patrimonio.setIdResponsavel(0);
            }
            
            // Valor
            String valorTexto = campoValor.getText().trim();
            if (!valorTexto.isEmpty()) {
                try {
                    patrimonio.setValorAquisicao(new BigDecimal(valorTexto));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Valor inválido.");
                    return;
                }
            }
            
            // Data de aquisição
            patrimonio.setDataAquisicao((Date) spinnerDataAquisicao.getValue());
            
            // Salvar no banco de dados usando DAO diretamente (Swing não usa Spring)
            PatrimonioDAO patrimonioDAO = new PatrimonioDAO();
            
            if (patrimonio.getId() == 0) {
                patrimonioDAO.insert(patrimonio);
                JOptionPane.showMessageDialog(this, "Patrimônio criado com sucesso!");
            } else {
                patrimonioDAO.update(patrimonio);
                JOptionPane.showMessageDialog(this, "Patrimônio atualizado com sucesso!");
            }
            
            confirmado = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar patrimônio: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Patrimonio getPatrimonio() {
        return patrimonio;
    }
}
