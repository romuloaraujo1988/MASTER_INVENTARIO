package com.inventario.sihcp.view;

import com.inventario.sihcp.dao.SalaDAO;
import com.inventario.sihcp.dao.SetorDAO;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.model.Setor;
import com.inventario.sihcp.util.*;
import com.inventario.sihcp.view.ui.ModernButtons;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Versão refatorada do SalaFormDialog usando classes utilitárias
 * Demonstra eliminação de código duplicado e melhor tratamento de erros
 */
public class SalaFormDialogRefactored extends JDialog {
    
    private Sala sala;
    private SalaDAO salaDAO;
    private SetorDAO setorDAO;
    private boolean salvo = false;
    
    // Componentes do formulário
    private JTextField txtDescricao;
    private JTextField txtNumeroSala;
    private JSpinner spnAndar;
    private JTextField txtBloco;
    private JComboBox<Setor> cmbSetor;
    private JSpinner spnCapacidade;
    private JTextField txtAreaM2;
    private JComboBox<String> cmbTipoSala;
    private JTextArea txtObservacoes;
    
    public SalaFormDialogRefactored(Frame parent, Sala sala, SalaDAO salaDAO, SetorDAO setorDAO) {
        super(parent, sala == null ? "Nova Sala" : "Editar Sala", true);
        this.sala = sala;
        this.salaDAO = salaDAO;
        this.setorDAO = setorDAO;
        
        initializeComponents();
        carregarSetores();
        
        if (sala != null) {
            preencherFormulario();
        }
    }
    
    private void initializeComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        DialogUtils.centerOnScreen(this); // ✅ Usando utilitário
        setResizable(false);
        
        setLayout(new BorderLayout());
        
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Número da Sala
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Número da Sala *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNumeroSala = new JTextField(30);
        panel.add(txtNumeroSala, gbc);
        
        row++;
        
        // Descrição
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Descrição *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDescricao = new JTextField(30);
        panel.add(txtDescricao, gbc);
        
        row++;
        
        // Andar
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Andar:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        spnAndar = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        panel.add(spnAndar, gbc);
        
        row++;
        
        // Bloco
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Bloco:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtBloco = new JTextField(30);
        panel.add(txtBloco, gbc);
        
        row++;
        
        // Setor
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Setor *:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbSetor = new JComboBox<>();
        cmbSetor.setPreferredSize(new Dimension(cmbSetor.getPreferredSize().width, 30));
        cmbSetor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Setor) {
                    setText(((Setor) value).getNome());
                }
                return this;
            }
        });
        panel.add(cmbSetor, gbc);
        
        row++;
        
        // Capacidade
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Capacidade:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        spnCapacidade = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        panel.add(spnCapacidade, gbc);
        
        row++;
        
        // Área (m²)
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Área (m²):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtAreaM2 = new JTextField(30);
        FormUtils.setNumericOnly(txtAreaM2); // ✅ Usando utilitário
        panel.add(txtAreaM2, gbc);
        
        row++;
        
        // Tipo de Sala
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tipo de Sala:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] tiposSala = {"Sala de Aula", "Laboratório", "Sala Modular", "Conteiner", 
                             "Coordenação", "Área Externa", "Almoxarifado", "Outros"};
        cmbTipoSala = new JComboBox<>(tiposSala);
        cmbTipoSala.setEditable(true);
        panel.add(cmbTipoSala, gbc);
        
        row++;
        
        // Observações
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Observações:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 0.4;
        txtObservacoes = new JTextArea(2, 20);
        txtObservacoes.setLineWrap(true);
        txtObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(txtObservacoes);
        panel.add(scrollObservacoes, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JButton btnCancelar = ModernButtons.muted("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        
        JButton btnSalvar = ModernButtons.primary("Salvar");
        btnSalvar.addActionListener(e -> salvarSala());
        
        panel.add(btnCancelar);
        panel.add(btnSalvar);
        
        return panel;
    }
    
    private void carregarSetores() {
        // ✅ Usando ExceptionHandler para tratamento de erros
        ExceptionHandler.executeWithErrorHandling(this, "carregar setores", () -> {
            List<Setor> setores = setorDAO.findAll("NOME");
            cmbSetor.removeAllItems();
            
            // Adicionar item vazio
            cmbSetor.addItem(null);
            
            for (Setor setor : setores) {
                cmbSetor.addItem(setor);
            }
        });
    }
    
    private void preencherFormulario() {
        if (sala == null) return;
        
        txtNumeroSala.setText(sala.getNumeroSala());
        txtDescricao.setText(sala.getDescricao());
        
        // Andar com tratamento de erro
        try {
            if (sala.getAndar() != null) {
                SpinnerNumberModel modelAndar = (SpinnerNumberModel) spnAndar.getModel();
                int minAndar = ((Integer) modelAndar.getMinimum()).intValue();
                int maxAndar = ((Integer) modelAndar.getMaximum()).intValue();
                int andarValue = sala.getAndar();
                
                if (andarValue < minAndar || andarValue > maxAndar) {
                    throw new IllegalArgumentException("Valor " + andarValue + " fora dos limites [" + minAndar + ", " + maxAndar + "]");
                }
                
                spnAndar.setValue(andarValue);
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Erro ao definir andar: " + ex.getMessage());
            spnAndar.setValue(1); // Valor padrão
        }
        
        txtBloco.setText(sala.getBloco());
        
        // Capacidade com tratamento de erro
        try {
            if (sala.getCapacidade() != null) {
                SpinnerNumberModel modelCapacidade = (SpinnerNumberModel) spnCapacidade.getModel();
                int minCapacidade = ((Integer) modelCapacidade.getMinimum()).intValue();
                int maxCapacidade = ((Integer) modelCapacidade.getMaximum()).intValue();
                int capacidadeValue = sala.getCapacidade();
                
                if (capacidadeValue < minCapacidade || capacidadeValue > maxCapacidade) {
                    throw new IllegalArgumentException("Valor " + capacidadeValue + " fora dos limites [" + minCapacidade + ", " + maxCapacidade + "]");
                }
                
                spnCapacidade.setValue(capacidadeValue);
            }
        } catch (IllegalArgumentException ex) {
            System.err.println("Erro ao definir capacidade: " + ex.getMessage());
            spnCapacidade.setValue(0); // Valor padrão
        }
        
        txtAreaM2.setText(String.valueOf(sala.getAreaM2()));
        txtObservacoes.setText(sala.getObservacoes());
        
        // Selecionar tipo de sala
        if (sala.getTipoSala() != null) {
            cmbTipoSala.setSelectedItem(sala.getTipoSala());
        }
        
        // Selecionar setor
        try {
            if (sala.getIdSetor() != null && sala.getIdSetor() > 0) {
                for (int i = 0; i < cmbSetor.getItemCount(); i++) {
                    Setor setor = cmbSetor.getItemAt(i);
                    if (setor != null && setor.getId() == sala.getIdSetor()) {
                        cmbSetor.setSelectedItem(setor);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            cmbSetor.setSelectedItem(null);
        }
    }
    
    private void salvarSala() {
        // ✅ Validação centralizada
        if (!validarFormulario()) {
            return;
        }
        
        // ✅ Usando ExceptionHandler para tratamento de erros
        ExceptionHandler.executeWithErrorHandling(this, "salvar sala", () -> {
            // Criar ou atualizar objeto Sala
            if (sala == null) {
                sala = new Sala();
            }
            
            sala.setNumeroSala(txtNumeroSala.getText().trim());
            sala.setDescricao(txtDescricao.getText().trim());
            sala.setAndar((Integer) spnAndar.getValue());
            sala.setBloco(txtBloco.getText().trim());
            sala.setCapacidade((Integer) spnCapacidade.getValue());
            sala.setObservacoes(txtObservacoes.getText().trim());
            
            // Área - ✅ Validação usando ValidationUtils
            String areaText = txtAreaM2.getText().trim();
            if (!areaText.isEmpty()) {
                ValidationUtils.ValidationResult areaResult = ValidationUtils.validateDecimal(areaText, "Área");
                if (!areaResult.isValid()) {
                    ExceptionHandler.handleValidation(this, areaResult.getErrorMessage());
                    txtAreaM2.requestFocus();
                    return;
                }
                sala.setAreaM2(Double.parseDouble(areaText.replace(",", ".")));
            } else {
                sala.setAreaM2(0.0);
            }
            
            // Tipo de sala
            Object tipoSelecionado = cmbTipoSala.getSelectedItem();
            if (tipoSelecionado != null) {
                sala.setTipoSala(tipoSelecionado.toString());
            }
            
            // Setor
            Setor setorSelecionado = (Setor) cmbSetor.getSelectedItem();
            if (setorSelecionado != null) {
                sala.setIdSetor(setorSelecionado.getId());
            } else {
                sala.setIdSetor(0);
            }
            
            // Verificar se número da sala já existe
            if (salaDAO.salaExiste(sala.getNumeroSala(), sala.getIdSala())) {
                ExceptionHandler.handleBusiness(this, "Já existe uma sala com este número.");
                txtNumeroSala.requestFocus();
                return;
            }
            
            // Salvar no banco
            boolean sucesso;
            if (sala.getIdSala() == 0) {
                sucesso = salaDAO.inserirSalaComSucesso(sala);
            } else {
                sucesso = salaDAO.atualizarSala(sala);
            }
            
            if (sucesso) {
                salvo = true;
                // ✅ Usando DialogUtils para sucesso
                DialogUtils.showSuccess(this, "Sala salva com sucesso!");
                dispose();
            } else {
                // ✅ Usando DialogUtils para erro
                DialogUtils.showError(this, "Erro ao salvar sala.");
            }
        });
    }
    
    private boolean validarFormulario() {
        // ✅ Validação usando ValidationUtils
        
        // Número da sala
        ValidationUtils.ValidationResult numeroResult = ValidationUtils.validateRequired(
            txtNumeroSala.getText(), "Número da sala"
        );
        if (!numeroResult.isValid()) {
            ExceptionHandler.handleValidation(this, numeroResult.getErrorMessage());
            txtNumeroSala.requestFocus();
            return false;
        }
        
        // Descrição
        ValidationUtils.ValidationResult descricaoResult = ValidationUtils.validateRequired(
            txtDescricao.getText(), "Descrição"
        );
        if (!descricaoResult.isValid()) {
            ExceptionHandler.handleValidation(this, descricaoResult.getErrorMessage());
            txtDescricao.requestFocus();
            return false;
        }
        
        // Setor
        if (cmbSetor.getSelectedItem() == null) {
            ExceptionHandler.handleValidation(this, "Setor é obrigatório.");
            cmbSetor.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public boolean isSalvo() {
        return salvo;
    }
}
