package com.inventario.sihcp.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.inventario.sihcp.dao.OcorrenciaPatrimonioDAO;
import com.inventario.sihcp.model.Usuario;

/**
 * Diálogo de Ações para Patrimônios Não Encontrados
 * Permite registrar ocorrências, notificar responsáveis e tomar ações
 */
public class AcoesPatrimonioDialog extends JDialog {

    private final int idPatrimonio;
    private final int idInventario;
    private final Usuario usuarioLogado;
    private final Map<String, Object> dadosPatrimonio;
    
    private final OcorrenciaPatrimonioDAO ocorrenciaDAO;
    
    // Componentes
    private JTable tblHistorico;
    private DefaultTableModel modelHistorico;
    private JTextArea txtDescricao;
    private JComboBox<String> cmbTipoOcorrencia;
    private JSpinner spinnerPrazo;
    
    private Runnable onActionCompleted;

    public AcoesPatrimonioDialog(Window owner, Map<String, Object> dadosPatrimonio, 
                                  int idInventario, Usuario usuario) {
        super(owner, "Ações - Patrimônio Não Encontrado", ModalityType.APPLICATION_MODAL);
        
        this.dadosPatrimonio = dadosPatrimonio;
        this.idPatrimonio = (int) dadosPatrimonio.get("id");
        this.idInventario = idInventario;
        this.usuarioLogado = usuario;
        this.ocorrenciaDAO = new OcorrenciaPatrimonioDAO();
        
        initComponents();
        carregarHistorico();
    }

    private void initComponents() {
        setSize(900, 700);
        setLocationRelativeTo(getOwner());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Painel superior - Informações do patrimônio
        mainPanel.add(criarPainelInfo(), BorderLayout.NORTH);

        // Painel central - Abas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📋 Nova Ocorrência", criarPainelNovaOcorrencia());
        tabbedPane.addTab("📜 Histórico", criarPainelHistorico());
        tabbedPane.addTab("⚡ Ações Rápidas", criarPainelAcoesRapidas());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Painel inferior - Botões
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        botoesPanel.add(btnFechar);
        mainPanel.add(botoesPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel criarPainelInfo() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 5));
        panel.setBorder(new TitledBorder("Informações do Patrimônio"));

        panel.add(new JLabel("Número:"));
        panel.add(new JLabel("<html><b>" + dadosPatrimonio.get("numero") + "</b></html>"));
        
        panel.add(new JLabel("Sala Esperada:"));
        panel.add(new JLabel(String.valueOf(dadosPatrimonio.getOrDefault("salaEsperada", "-"))));

        panel.add(new JLabel("Descrição:"));
        String descricao = String.valueOf(dadosPatrimonio.getOrDefault("descricao", "-"));
        if (descricao.length() > 50) descricao = descricao.substring(0, 47) + "...";
        panel.add(new JLabel(descricao));

        panel.add(new JLabel("Responsável:"));
        panel.add(new JLabel(String.valueOf(dadosPatrimonio.getOrDefault("responsavel", "-"))));

        return panel;
    }

    private JPanel criarPainelNovaOcorrencia() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Tipo de ocorrência
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tipo de Ocorrência:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbTipoOcorrencia = new JComboBox<>(new String[]{
            "Notificação ao Responsável",
            "Investigação",
            "Transferência de Responsabilidade",
            "Extravio Confirmado",
            "Solicitação de Baixa"
        });
        formPanel.add(cmbTipoOcorrencia, gbc);

        // Prazo
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Prazo para Resposta:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 7);
        spinnerPrazo = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerPrazo, "dd/MM/yyyy");
        spinnerPrazo.setEditor(editor);
        formPanel.add(spinnerPrazo, gbc);

        // Descrição
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Descrição:"), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 1;
        txtDescricao = new JTextArea(5, 40);
        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescricao), gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        // Botão registrar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegistrar = new JButton("📝 Registrar Ocorrência");
        btnRegistrar.setBackground(new Color(0, 123, 255));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.addActionListener(e -> registrarOcorrencia());
        btnPanel.add(btnRegistrar);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelHistorico() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Tipo", "Status", "Data", "Descrição", "Usuário"};
        modelHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHistorico = new JTable(modelHistorico);
        tblHistorico.setRowHeight(25);
        tblHistorico.getColumnModel().getColumn(0).setMaxWidth(50);
        tblHistorico.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblHistorico.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblHistorico.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblHistorico.getColumnModel().getColumn(4).setPreferredWidth(250);
        tblHistorico.getColumnModel().getColumn(5).setPreferredWidth(100);

        panel.add(new JScrollPane(tblHistorico), BorderLayout.CENTER);

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarHistorico());
        btnPanel.add(btnAtualizar);

        JButton btnResolver = new JButton("✅ Marcar como Resolvida");
        btnResolver.addActionListener(e -> resolverOcorrencia());
        btnPanel.add(btnResolver);

        JButton btnCancelar = new JButton("❌ Cancelar Ocorrência");
        btnCancelar.addActionListener(e -> cancelarOcorrencia());
        btnPanel.add(btnCancelar);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel criarPainelAcoesRapidas() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Botão 1: Notificar Responsável
        JButton btnNotificar = criarBotaoAcao(
            "📧 Notificar Responsável",
            "Enviar notificação solicitando informações",
            new Color(0, 123, 255)
        );
        btnNotificar.addActionListener(e -> acaoNotificarResponsavel());
        panel.add(btnNotificar);

        // Botão 2: Vincular a Item Sem Etiqueta
        JButton btnVincular = criarBotaoAcao(
            "🔗 Vincular a Item Sem Etiqueta",
            "Buscar correspondência com itens sem identificação",
            new Color(108, 117, 125)
        );
        btnVincular.addActionListener(e -> acaoVincularSemEtiqueta());
        panel.add(btnVincular);

        // Botão 3: Registrar Localização
        JButton btnLocalizar = criarBotaoAcao(
            "📍 Informar Nova Localização",
            "Patrimônio foi encontrado em outro local",
            new Color(40, 167, 69)
        );
        btnLocalizar.addActionListener(e -> acaoInformarLocalizacao());
        panel.add(btnLocalizar);

        // Botão 4: Transferir Responsabilidade
        JButton btnTransferir = criarBotaoAcao(
            "👤 Transferir Responsabilidade",
            "Alterar responsável pelo patrimônio",
            new Color(255, 193, 7)
        );
        btnTransferir.addActionListener(e -> acaoTransferirResponsabilidade());
        panel.add(btnTransferir);

        // Botão 5: Marcar como Extraviado
        JButton btnExtravio = criarBotaoAcao(
            "⚠️ Confirmar Extravio",
            "Patrimônio não foi localizado após investigação",
            new Color(220, 53, 69)
        );
        btnExtravio.addActionListener(e -> acaoConfirmarExtravio());
        panel.add(btnExtravio);

        // Botão 6: Solicitar Baixa
        JButton btnBaixa = criarBotaoAcao(
            "📋 Solicitar Baixa Patrimonial",
            "Iniciar processo de baixa do patrimônio",
            new Color(111, 66, 193)
        );
        btnBaixa.addActionListener(e -> acaoSolicitarBaixa());
        panel.add(btnBaixa);

        return panel;
    }

    private JButton criarBotaoAcao(String titulo, String descricao, Color cor) {
        JButton btn = new JButton("<html><center><b>" + titulo + "</b><br><small>" + descricao + "</small></center></html>");
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(200, 80));
        btn.setFocusPainted(false);
        return btn;
    }

    private void carregarHistorico() {
        try {
            List<Map<String, Object>> historico = ocorrenciaDAO.buscarPorPatrimonio(idPatrimonio);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            modelHistorico.setRowCount(0);
            for (Map<String, Object> item : historico) {
                Timestamp data = (Timestamp) item.get("data_abertura");
                String descricao = (String) item.get("descricao");
                if (descricao != null && descricao.length() > 50) {
                    descricao = descricao.substring(0, 47) + "...";
                }
                
                modelHistorico.addRow(new Object[]{
                    item.get("id"),
                    formatarTipo((String) item.get("tipo_ocorrencia")),
                    item.get("status"),
                    data != null ? sdf.format(data) : "",
                    descricao,
                    item.get("usuario_nome")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar histórico: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatarTipo(String tipo) {
        if (tipo == null) return "";
        return switch (tipo) {
            case "NOTIFICACAO_RESPONSAVEL" -> "Notificação";
            case "INVESTIGACAO" -> "Investigação";
            case "TRANSFERENCIA" -> "Transferência";
            case "LOCALIZACAO_ATUALIZADA" -> "Localização";
            case "EXTRAVIO_CONFIRMADO" -> "Extravio";
            case "BAIXA_SOLICITADA" -> "Baixa";
            default -> tipo;
        };
    }

    private String getTipoOcorrencia() {
        int index = cmbTipoOcorrencia.getSelectedIndex();
        return switch (index) {
            case 0 -> OcorrenciaPatrimonioDAO.TIPO_NOTIFICACAO;
            case 1 -> OcorrenciaPatrimonioDAO.TIPO_INVESTIGACAO;
            case 2 -> OcorrenciaPatrimonioDAO.TIPO_TRANSFERENCIA;
            case 3 -> OcorrenciaPatrimonioDAO.TIPO_EXTRAVIO;
            case 4 -> OcorrenciaPatrimonioDAO.TIPO_BAIXA;
            default -> OcorrenciaPatrimonioDAO.TIPO_INVESTIGACAO;
        };
    }

    private void registrarOcorrencia() {
        String descricao = txtDescricao.getText().trim();
        if (descricao.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe a descrição da ocorrência",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            java.util.Date prazo = (java.util.Date) spinnerPrazo.getValue();
            Timestamp dataPrazo = new Timestamp(prazo.getTime());

            ocorrenciaDAO.registrarOcorrencia(
                idPatrimonio, idInventario, usuarioLogado.getId(),
                getTipoOcorrencia(), descricao, dataPrazo
            );

            JOptionPane.showMessageDialog(this, "Ocorrência registrada com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            txtDescricao.setText("");
            carregarHistorico();
            notificarAcaoCompleta();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao registrar ocorrência: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resolverOcorrencia() {
        int row = tblHistorico.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma ocorrência",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String acaoResolucao = JOptionPane.showInputDialog(this, 
            "Descreva a ação de resolução:", "Resolver Ocorrência", JOptionPane.QUESTION_MESSAGE);
        
        if (acaoResolucao != null && !acaoResolucao.trim().isEmpty()) {
            try {
                int idOcorrencia = (int) modelHistorico.getValueAt(row, 0);
                ocorrenciaDAO.atualizarStatus(idOcorrencia, 
                    OcorrenciaPatrimonioDAO.STATUS_RESOLVIDA, acaoResolucao);
                
                JOptionPane.showMessageDialog(this, "Ocorrência resolvida!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                carregarHistorico();
                notificarAcaoCompleta();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelarOcorrencia() {
        int row = tblHistorico.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma ocorrência",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Deseja cancelar esta ocorrência?", "Confirmação", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idOcorrencia = (int) modelHistorico.getValueAt(row, 0);
                ocorrenciaDAO.atualizarStatus(idOcorrencia, 
                    OcorrenciaPatrimonioDAO.STATUS_CANCELADA, "Cancelada pelo usuário");
                
                carregarHistorico();
                notificarAcaoCompleta();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== AÇÕES RÁPIDAS ====================

    private void acaoNotificarResponsavel() {
        String responsavel = String.valueOf(dadosPatrimonio.getOrDefault("responsavel", "Não informado"));
        
        String mensagem = String.format("""
            Será registrada uma notificação ao responsável:
            
            Responsável: %s
            Patrimônio: %s - %s
            
            Deseja continuar?
            """, responsavel, dadosPatrimonio.get("numero"), dadosPatrimonio.get("descricao"));
        
        int confirm = JOptionPane.showConfirmDialog(this, mensagem, 
            "Notificar Responsável", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 7);
                
                String descricao = "Notificação automática ao responsável " + responsavel + 
                    " solicitando informações sobre o patrimônio não localizado.";
                
                ocorrenciaDAO.registrarOcorrencia(
                    idPatrimonio, idInventario, usuarioLogado.getId(),
                    OcorrenciaPatrimonioDAO.TIPO_NOTIFICACAO, descricao,
                    new Timestamp(cal.getTimeInMillis())
                );
                
                JOptionPane.showMessageDialog(this, 
                    "Notificação registrada!\nPrazo para resposta: 7 dias",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                carregarHistorico();
                notificarAcaoCompleta();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acaoVincularSemEtiqueta() {
        // Abrir diálogo de vinculação com itens sem etiqueta
        JOptionPane.showMessageDialog(this, """
                                            Use a aba 'Sugest\u00f5es de Reconcilia\u00e7\u00e3o' na tela principal
                                            para vincular este patrim\u00f4nio a um item sem etiqueta.""",
            "Vincular Item", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void acaoInformarLocalizacao() {
        String novaLocalizacao = JOptionPane.showInputDialog(this, 
            "Informe a nova localização do patrimônio:", 
            "Nova Localização", JOptionPane.QUESTION_MESSAGE);
        
        if (novaLocalizacao != null && !novaLocalizacao.trim().isEmpty()) {
            try {
                String descricao = "Patrimônio localizado em: " + novaLocalizacao;
                
                ocorrenciaDAO.registrarOcorrencia(
                    idPatrimonio, idInventario, usuarioLogado.getId(),
                    OcorrenciaPatrimonioDAO.TIPO_LOCALIZACAO, descricao, null
                );
                
                JOptionPane.showMessageDialog(this, 
                    "Localização registrada!\n\nNova localização: " + novaLocalizacao +
                    "\n\nNota: Atualize o cadastro do patrimônio se necessário.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                carregarHistorico();
                notificarAcaoCompleta();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void acaoTransferirResponsabilidade() {
        String novoResponsavel = JOptionPane.showInputDialog(this, 
            "Informe o nome do novo responsável:", 
            "Transferir Responsabilidade", JOptionPane.QUESTION_MESSAGE);
        
        if (novoResponsavel != null && !novoResponsavel.trim().isEmpty()) {
            String motivo = JOptionPane.showInputDialog(this, 
                "Motivo da transferência:", 
                "Transferir Responsabilidade", JOptionPane.QUESTION_MESSAGE);
            
            if (motivo != null) {
                try {
                    String descricao = "Transferência de responsabilidade para: " + novoResponsavel +
                        "\nMotivo: " + motivo;
                    
                    ocorrenciaDAO.registrarOcorrencia(
                        idPatrimonio, idInventario, usuarioLogado.getId(),
                        OcorrenciaPatrimonioDAO.TIPO_TRANSFERENCIA, descricao, null
                    );
                    
                    JOptionPane.showMessageDialog(this, 
                        "Transferência registrada!\n\nNovo responsável: " + novoResponsavel +
                        "\n\nNota: Atualize o cadastro do patrimônio.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    
                    carregarHistorico();
                    notificarAcaoCompleta();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void acaoConfirmarExtravio() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            """
            ATEN\u00c7\u00c3O: Esta a\u00e7\u00e3o confirma que o patrim\u00f4nio foi extraviado.
            
            Patrim\u00f4nio: """ + dadosPatrimonio.get("numero") + "\n" +
            "Descrição: " + dadosPatrimonio.get("descricao") + "\n\n" +
            "Deseja confirmar o extravio?",
            "Confirmar Extravio", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String justificativa = JOptionPane.showInputDialog(this, 
                "Justificativa para o extravio:", 
                "Confirmar Extravio", JOptionPane.QUESTION_MESSAGE);
            
            if (justificativa != null && !justificativa.trim().isEmpty()) {
                try {
                    String descricao = """
                                       EXTRAVIO CONFIRMADO
                                       Justificativa: """ + justificativa + "\n" +
                        "Responsável no momento: " + dadosPatrimonio.getOrDefault("responsavel", "Não informado");
                    
                    ocorrenciaDAO.registrarOcorrencia(
                        idPatrimonio, idInventario, usuarioLogado.getId(),
                        OcorrenciaPatrimonioDAO.TIPO_EXTRAVIO, descricao, null
                    );
                    
                    JOptionPane.showMessageDialog(this, 
                        "Extravio registrado!\n\nRecomenda-se iniciar processo de baixa patrimonial.",
                        "Extravio Confirmado", JOptionPane.WARNING_MESSAGE);
                    
                    carregarHistorico();
                    notificarAcaoCompleta();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void acaoSolicitarBaixa() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            """
            Ser\u00e1 iniciado o processo de baixa patrimonial.
            
            Patrim\u00f4nio: """ + dadosPatrimonio.get("numero") + "\n" +
            "Descrição: " + dadosPatrimonio.get("descricao") + "\n\n" +
            "Deseja solicitar a baixa?",
            "Solicitar Baixa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String motivo = JOptionPane.showInputDialog(this, 
                "Motivo da solicitação de baixa:", 
                "Solicitar Baixa", JOptionPane.QUESTION_MESSAGE);
            
            if (motivo != null && !motivo.trim().isEmpty()) {
                try {
                    String descricao = """
                                       SOLICITA\u00c7\u00c3O DE BAIXA PATRIMONIAL
                                       Motivo: """ + motivo + "\n" +
                        "Solicitante: " + usuarioLogado.getNomeCompleto();
                    
                    ocorrenciaDAO.registrarOcorrencia(
                        idPatrimonio, idInventario, usuarioLogado.getId(),
                        OcorrenciaPatrimonioDAO.TIPO_BAIXA, descricao, null
                    );
                    
                    JOptionPane.showMessageDialog(this, """
                                                        Solicita\u00e7\u00e3o de baixa registrada!
                                                        
                                                        A solicita\u00e7\u00e3o ser\u00e1 encaminhada para an\u00e1lise.""",
                        "Baixa Solicitada", JOptionPane.INFORMATION_MESSAGE);
                    
                    carregarHistorico();
                    notificarAcaoCompleta();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void notificarAcaoCompleta() {
        if (onActionCompleted != null) {
            onActionCompleted.run();
        }
    }

    public void setOnActionCompleted(Runnable callback) {
        this.onActionCompleted = callback;
    }
}
