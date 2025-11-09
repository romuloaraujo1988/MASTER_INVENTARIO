package com.inventario.view;

import javax.swing.*;
import java.awt.*;
import com.inventario.model.Responsavel;
import com.inventario.dao.ResponsavelDAO;
import com.inventario.dao.SetorDAORefactored;
import com.inventario.model.Setor;
import java.util.List;

/**
 * Formulário para adicionar/editar responsáveis
 */
public class ResponsavelFormDialog extends JDialog {
    private Responsavel responsavel;
    private boolean confirmado = false;
    
    // Campos do formulário
    private JTextField campoNome;
    private JTextField campoCpf;
    private JTextField campoEmail;
    private JTextField campoTelefone;
    private JTextField campoCargo;
    private JComboBox<Object> comboSetor;
    private JTextArea areaObservacoes;
    
    private JButton btnSalvar, btnCancelar;
    
    public ResponsavelFormDialog(Frame parent, Responsavel responsavel) {
        super(parent, responsavel == null ? "Novo Responsável" : "Editar Responsável", true);
        this.responsavel = responsavel;
        initComponents();
        if (responsavel != null) {
            preencherCampos();
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal com campos
        JPanel painelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nome
        gbc.gridx = 0; gbc.gridy = 0;
        painelCampos.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        campoNome = new JTextField(35);
        painelCampos.add(campoNome, gbc);
        
        // CPF
        gbc.gridx = 0; gbc.gridy = 1;
        painelCampos.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1;
        campoCpf = new JTextField(35);
        painelCampos.add(campoCpf, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        painelCampos.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        campoEmail = new JTextField(35);
        painelCampos.add(campoEmail, gbc);
        
        // Telefone
        gbc.gridx = 0; gbc.gridy = 3;
        painelCampos.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1;
        campoTelefone = new JTextField(35);
        painelCampos.add(campoTelefone, gbc);
        
        // Cargo
        gbc.gridx = 0; gbc.gridy = 4;
        painelCampos.add(new JLabel("Cargo:"), gbc);
        gbc.gridx = 1;
        campoCargo = new JTextField(35);
        painelCampos.add(campoCargo, gbc);
        
        // Setor
        gbc.gridx = 0; gbc.gridy = 5;
        painelCampos.add(new JLabel("Setor:"), gbc);
        gbc.gridx = 1;
        comboSetor = new JComboBox<>();
        carregarSetores();
        painelCampos.add(comboSetor, gbc);
        
        // Observações
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        painelCampos.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        areaObservacoes = new JTextArea(4, 25);
        areaObservacoes.setLineWrap(true);
        areaObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(areaObservacoes);
        painelCampos.add(scrollObservacoes, gbc);
        
        add(painelCampos, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(450, 400);
        setLocationRelativeTo(getParent());
    }
    
    private void configurarEventos() {
        btnSalvar.addActionListener(e -> salvarResponsavel());
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void carregarSetores() {
        comboSetor.addItem("Selecione um setor...");
        try {
            SetorDAORefactored setorDAO = new SetorDAORefactored();
            List<Setor> setores = setorDAO.findAll("NOME");
            for (Setor setor : setores) {
                comboSetor.addItem(setor);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar setores: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void preencherCampos() {
        if (responsavel != null) {
            campoNome.setText(responsavel.getNome());
            campoCpf.setText(responsavel.getCpf());
            campoEmail.setText(responsavel.getEmail());
            campoTelefone.setText(responsavel.getTelefone());
            campoCargo.setText(responsavel.getCargo());
            areaObservacoes.setText(responsavel.getObservacoes());
            
            // Selecionar setor no combo
              if (responsavel.getIdSetor() > 0) {
                  for (int i = 1; i < comboSetor.getItemCount(); i++) {
                      Object item = comboSetor.getItemAt(i);
                      if (item instanceof Setor) {
                          Setor setor = (Setor) item;
                          if (setor.getId() == responsavel.getIdSetor()) {
                              comboSetor.setSelectedIndex(i);
                              break;
                          }
                      }
                  }
              }
        }
    }
    
    private void salvarResponsavel() {
        try {
            // Validar campos obrigatórios
            if (campoNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome é obrigatório.");
                return;
            }
            
            if (campoCpf.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O CPF é obrigatório.");
                return;
            }
            
            // Validar formato do CPF (básico)
            String cpf = campoCpf.getText().replaceAll("[^0-9]", "");
            if (cpf.length() != 11) {
                JOptionPane.showMessageDialog(this, "CPF deve ter 11 dígitos.");
                return;
            }
            
            // Validar email (básico)
            String email = campoEmail.getText().trim();
            if (!email.isEmpty() && !email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Email inválido.");
                return;
            }
            
            // Criar ou atualizar responsável
            if (responsavel == null) {
                responsavel = new Responsavel();
            }
            
            responsavel.setNome(campoNome.getText().trim());
            responsavel.setCpf(cpf);
            responsavel.setEmail(email);
            responsavel.setTelefone(campoTelefone.getText().trim());
            responsavel.setCargo(campoCargo.getText().trim());
            responsavel.setObservacoes(areaObservacoes.getText().trim());
            
            // Definir setor selecionado
             Object setorSelecionado = comboSetor.getSelectedItem();
             if (setorSelecionado != null && setorSelecionado instanceof Setor) {
                 Setor setor = (Setor) setorSelecionado;
                 responsavel.setIdSetor(setor.getId());
             }
             
             try {
                  // Salvar no banco de dados
                  ResponsavelDAO responsavelDAO = new ResponsavelDAO();
                  if (responsavel.getId() == 0) {
                      responsavelDAO.inserirResponsavel(responsavel);
                      JOptionPane.showMessageDialog(this, "Responsável cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                  } else {
                      responsavelDAO.atualizarResponsavel(responsavel);
                      JOptionPane.showMessageDialog(this, "Responsável atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                  }
                  
                  confirmado = true;
                  dispose();
              } catch (Exception e) {
                  JOptionPane.showMessageDialog(this, "Erro ao salvar responsável: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
              }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar responsável: " + e.getMessage());
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Responsavel getResponsavel() {
        return responsavel;
    }
}