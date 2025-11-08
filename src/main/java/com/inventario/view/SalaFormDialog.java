package com.inventario.view;

import com.inventario.service.ServiceFactory;
import com.inventario.service.SalaService;
import com.inventario.service.SetorService;
import com.inventario.service.BusinessException;
import com.inventario.model.Sala;
import com.inventario.model.Setor;
import com.inventario.view.ui.ModernButtons;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Diálogo para formulário de Sala (Adicionar/Editar)
 * Sistema de Inventário IFMT
 */
public class SalaFormDialog extends JDialog {
    
    private Sala sala;
    private final SalaService salaService;
    private final SetorService setorService;
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
    
    public SalaFormDialog(Frame parent, Sala sala) {
        super(parent, sala == null ? "Nova Sala" : "Editar Sala", true);
        this.sala = sala;
        
        ServiceFactory factory = ServiceFactory.getInstance();
        this.salaService = factory.getSalaService();
        this.setorService = factory.getSetorService();
        
        initializeComponents();
        carregarSetores();
        
        if (sala != null) {
            preencherFormulario();
        }
    }
    
    private void initializeComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // Layout principal
        setLayout(new BorderLayout());
        
        // Panel do formulário
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        // Panel dos botões
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
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        txtObservacoes = new JTextArea(4, 20);
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
        try {
            List<Setor> setores = setorService.listarTodos();
            cmbSetor.removeAllItems();
            
            // Adicionar item vazio
            cmbSetor.addItem(null);
            
            for (Setor setor : setores) {
                cmbSetor.addItem(setor);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar setores: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
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
            // Se houver erro ao acessar o idSetor, deixa o combo sem seleção
            cmbSetor.setSelectedItem(null);
        }
    }
    
    private void salvarSala() {
        if (!validarFormulario()) {
            return;
        }
        
        try {
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
            
            // Área
            try {
                String areaText = txtAreaM2.getText().trim();
                if (!areaText.isEmpty()) {
                    sala.setAreaM2(Double.parseDouble(areaText.replace(",", ".")));
                } else {
                    sala.setAreaM2(0.0);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Área deve ser um número válido.", 
                    "Erro de Validação", 
                    JOptionPane.ERROR_MESSAGE);
                txtAreaM2.requestFocus();
                return;
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
            
            // Salvar no banco usando o service
            salaService.salvar(sala);
            salvo = true;
            JOptionPane.showMessageDialog(this, 
                "Sala salva com sucesso!", 
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (BusinessException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Erro de Validação", 
                JOptionPane.ERROR_MESSAGE);
            txtNumeroSala.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao processar formulário: " + e.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validarFormulario() {
        // Número da sala
        if (txtNumeroSala.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Número da sala é obrigatório.", 
                "Erro de Validação", 
                JOptionPane.ERROR_MESSAGE);
            txtNumeroSala.requestFocus();
            return false;
        }
        
        // Descrição
        if (txtDescricao.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Descrição é obrigatória.", 
                "Erro de Validação", 
                JOptionPane.ERROR_MESSAGE);
            txtDescricao.requestFocus();
            return false;
        }
        
        // Setor
        if (cmbSetor.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "Setor é obrigatório.", 
                "Erro de Validação", 
                JOptionPane.ERROR_MESSAGE);
            cmbSetor.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public boolean isSalvo() {
        return salvo;
    }
}

