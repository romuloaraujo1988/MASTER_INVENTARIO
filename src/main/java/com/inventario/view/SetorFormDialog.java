package com.inventario.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.inventario.model.Setor;
import com.inventario.service.ServiceFactory;
import com.inventario.service.SetorService;
import com.inventario.service.BusinessException;
import com.inventario.view.ui.ModernButtons;

/**
 * Formulário para adicionar/editar setores
 */
public class SetorFormDialog extends JDialog {
    private Setor setor;
    private boolean confirmado = false;
    
    // Campos do formulário
    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoResponsavel;
    
    private JButton btnSalvar, btnCancelar;
    
    public SetorFormDialog(Frame parent, Setor setor) {
        super(parent, setor == null ? "Novo Setor" : "Editar Setor", true);
        this.setor = setor;
        initComponents();
        aplicarEstiloModerno();
        if (setor != null) {
            preencherCampos();
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));
        
        // Painel principal com título e campos
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(new Color(245, 245, 245));
        painelPrincipal.setBorder(new EmptyBorder(25, 30, 20, 30));
        
        // Título
        JLabel lblTitulo = new JLabel(setor == null ? "📁 Novo Setor" : "✏️ Editar Setor");
        lblTitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 58, 64));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 20, 0));
        painelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        
        // Painel de campos
        JPanel painelCampos = new JPanel();
        painelCampos.setLayout(new BoxLayout(painelCampos, BoxLayout.Y_AXIS));
        painelCampos.setBackground(Color.WHITE);
        painelCampos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        // Campo Nome
        painelCampos.add(criarCampoFormulario("📝 Nome do Setor *", campoNome = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(20));
        
        // Campo Descrição
        painelCampos.add(criarCampoFormulario("📄 Descrição", campoDescricao = new JTextField()));
        painelCampos.add(Box.createVerticalStrut(20));
        
        // Campo Responsável
        painelCampos.add(criarCampoFormulario("👤 Responsável", campoResponsavel = new JTextField()));
        
        painelPrincipal.add(painelCampos, BorderLayout.CENTER);
        add(painelPrincipal, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelBotoes.setBackground(new Color(245, 245, 245));
        painelBotoes.setBorder(new EmptyBorder(20, 30, 25, 30));
        
        btnSalvar = ModernButtons.secondary("💾 Salvar");
        btnCancelar = ModernButtons.muted("❌ Cancelar");
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(500, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private JPanel criarCampoFormulario(String labelTexto, JTextField campo) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelTexto);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        label.setForeground(new Color(73, 80, 87));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        campo.setPreferredSize(new Dimension(400, 35));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        campo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        painel.add(label);
        painel.add(Box.createVerticalStrut(8));
        painel.add(campo);
        
        return painel;
    }
    
    private void aplicarEstiloModerno() {
        // Estilo dos botões agora é fornecido por ModernButtons.
        // Mantemos apenas os efeitos de focus nos campos.
        adicionarEfeitoFocus(campoNome);
        adicionarEfeitoFocus(campoDescricao);
        adicionarEfeitoFocus(campoResponsavel);
    }
    
    private void estilizarBotao(JButton botao, Color corFundo, Color corTexto) {
        botao.setBackground(corFundo);
        botao.setForeground(corTexto);
        botao.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        botao.setBorder(new EmptyBorder(10, 20, 10, 20));
        botao.setFocusPainted(false);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botao.setPreferredSize(new Dimension(120, 40));
    }
    
    private void adicionarEfeitoHover(JButton botao, Color corNormal, Color corHover) {
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botao.setBackground(corHover);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(corNormal);
            }
        });
    }
    
    private void adicionarEfeitoFocus(JTextField campo) {
        Color corNormal = new Color(206, 212, 218);
        Color corFocus = new Color(0, 123, 255);
        
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(corFocus, 2),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(corNormal, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }
    
    private void configurarEventos() {
        btnSalvar.addActionListener(e -> salvarSetor());
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void preencherCampos() {
        if (setor != null) {
            campoNome.setText(setor.getNome());
            campoDescricao.setText(setor.getDescricao());
            campoResponsavel.setText(setor.getResponsavelSetor());
        }
    }
    
    private void salvarSetor() {
        try {
            // Validar campos obrigatórios
            if (campoNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do setor é obrigatório.");
                return;
            }
            

            
            // Criar ou atualizar setor
            if (setor == null) {
                setor = new Setor();
            }
            
            setor.setNome(campoNome.getText().trim());
            setor.setDescricao(campoDescricao.getText().trim());
            setor.setResponsavelSetor(campoResponsavel.getText().trim());
            
            // Salvar no banco de dados
            SetorService setorService = ServiceFactory.getInstance().getSetorService();
            
            try {
                setorService.salvar(setor);
                
                if (setor.getId() == 0) {
                    JOptionPane.showMessageDialog(this, "Setor cadastrado com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Setor atualizado com sucesso!");
                }
                
                confirmado = true;
                dispose();
                
            } catch (BusinessException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar setor: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar setor: " + e.getMessage());
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Setor getSetor() {
        return setor;
    }
}
