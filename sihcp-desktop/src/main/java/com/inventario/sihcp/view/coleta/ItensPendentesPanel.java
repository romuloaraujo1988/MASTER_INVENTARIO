package com.inventario.sihcp.view.coleta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.dao.InventarioDAO;
import com.inventario.sihcp.dao.PatrimonioDAO;
import com.inventario.sihcp.model.Inventario;
import com.inventario.sihcp.model.Patrimonio;
import com.inventario.sihcp.model.Sala;
import com.inventario.sihcp.view.ui.StyledButtonFactory;

/**
 * Painel da aba "Itens Pendentes": lista os patrimônios de uma sala que ainda
 * não foram coletados no inventário ativo, mostrando progresso e permitindo
 * exportação.
 *
 * <p>Extraído de {@code ColetaFrame_v2} como parte da refatoração estrutural.
 * O painel é autocontido: recebe os DAOs e dois callbacks no construtor
 * (um para registrar workers no frame e outro para "ir coletar" um patrimônio
 * via duplo-clique).</p>
 */
public class ItensPendentesPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ItensPendentesPanel.class);

    private static final Color COR_FUNDO = new Color(248, 249, 250);
    private static final Color COR_PRIMARIA = new Color(52, 73, 94);

    private final PatrimonioDAO patrimonioDAO;
    private final InventarioDAO inventarioDAO;
    private final Consumer<String> onColetarPatrimonio;
    private final Consumer<SwingWorker<?, ?>> workerRegistrar;

    // Sala atualmente exibida (para os botões internos de atualizar/filtrar)
    private Sala salaAtual;

    // Componentes
    private JTable tabelaPendentes;
    private DefaultTableModel modeloTabelaPendentes;
    private JLabel lblTotalPendentes;
    private JLabel lblProgressoColeta;
    private JTextField campoBuscaPendentes;
    private JButton btnExportarPendentes;
    private JProgressBar progressBarColeta;

    /**
     * @param patrimonioDAO       DAO de patrimônios
     * @param inventarioDAO       DAO de inventários
     * @param onColetarPatrimonio callback chamado ao dar duplo-clique num item
     *                            (recebe o número do patrimônio); tipicamente
     *                            troca para a aba de coleta e dispara a busca
     * @param workerRegistrar     callback para registrar o SwingWorker no frame
     *                            (permite cancelamento ao trocar de sala/fechar)
     */
    public ItensPendentesPanel(PatrimonioDAO patrimonioDAO,
                               InventarioDAO inventarioDAO,
                               Consumer<String> onColetarPatrimonio,
                               Consumer<SwingWorker<?, ?>> workerRegistrar) {
        this.patrimonioDAO = patrimonioDAO;
        this.inventarioDAO = inventarioDAO;
        this.onColetarPatrimonio = onColetarPatrimonio;
        this.workerRegistrar = workerRegistrar;
        construirUI();
    }

    private void construirUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COR_FUNDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ========== PAINEL SUPERIOR: INFORMAÇÕES E PROGRESSO ==========
        JPanel painelSuperior = new JPanel(new BorderLayout(10, 10));
        painelSuperior.setBackground(Color.WHITE);
        painelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        // Painel esquerdo: Título e informações
        JPanel painelInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        painelInfo.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("📋 Itens Pendentes de Coleta");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(COR_PRIMARIA);

        lblTotalPendentes = new JLabel("Selecione uma sala para ver os itens pendentes");
        lblTotalPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTotalPendentes.setForeground(new Color(108, 117, 125));

        lblProgressoColeta = new JLabel("Progresso: -");
        lblProgressoColeta.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgressoColeta.setForeground(new Color(46, 204, 113));

        painelInfo.add(lblTitulo);
        painelInfo.add(lblTotalPendentes);
        painelInfo.add(lblProgressoColeta);

        // Painel direito: Barra de progresso
        JPanel painelProgresso = new JPanel(new BorderLayout(5, 5));
        painelProgresso.setBackground(Color.WHITE);
        painelProgresso.setPreferredSize(new Dimension(300, 60));

        progressBarColeta = new JProgressBar(0, 100);
        progressBarColeta.setStringPainted(true);
        progressBarColeta.setString("0%");
        progressBarColeta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBarColeta.setForeground(new Color(46, 204, 113));
        progressBarColeta.setBackground(new Color(236, 240, 241));
        progressBarColeta.setPreferredSize(new Dimension(280, 25));

        JLabel lblBarraProgresso = new JLabel("Progresso da Coleta na Sala:");
        lblBarraProgresso.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBarraProgresso.setForeground(new Color(108, 117, 125));

        painelProgresso.add(lblBarraProgresso, BorderLayout.NORTH);
        painelProgresso.add(progressBarColeta, BorderLayout.CENTER);

        painelSuperior.add(painelInfo, BorderLayout.WEST);
        painelSuperior.add(painelProgresso, BorderLayout.EAST);

        // ========== PAINEL DE BUSCA ==========
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelBusca.setBackground(Color.WHITE);
        painelBusca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel lblBusca = new JLabel("🔍 Filtrar:");
        lblBusca.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));

        campoBuscaPendentes = new JTextField(25);
        campoBuscaPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        campoBuscaPendentes.setPreferredSize(new Dimension(250, 30));
        campoBuscaPendentes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        campoBuscaPendentes.setToolTipText("Digite número ou descrição para filtrar");

        // Listener para filtrar em tempo real
        campoBuscaPendentes.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarTabela(campoBuscaPendentes.getText());
            }
        });

        btnExportarPendentes = StyledButtonFactory.create("📥 Exportar Lista", new Color(52, 152, 219));
        btnExportarPendentes.setEnabled(false);
        btnExportarPendentes.addActionListener(e -> exportarLista());

        JButton btnAtualizarPendentes = StyledButtonFactory.create("🔄 Atualizar", new Color(46, 204, 113));
        btnAtualizarPendentes.addActionListener(e -> carregarParaSala(salaAtual));

        painelBusca.add(lblBusca);
        painelBusca.add(campoBuscaPendentes);
        painelBusca.add(Box.createHorizontalStrut(20));
        painelBusca.add(btnAtualizarPendentes);
        painelBusca.add(btnExportarPendentes);

        // Combinar painéis superiores
        JPanel painelTopoCompleto = new JPanel(new BorderLayout(0, 10));
        painelTopoCompleto.setBackground(COR_FUNDO);
        painelTopoCompleto.add(painelSuperior, BorderLayout.NORTH);
        painelTopoCompleto.add(painelBusca, BorderLayout.SOUTH);

        // ========== TABELA DE ITENS PENDENTES ==========
        String[] colunasPendentes = {"Número", "Descrição", "Localização Original", "Responsável", "Estado"};
        modeloTabelaPendentes = new DefaultTableModel(colunasPendentes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaPendentes = new JTable(modeloTabelaPendentes);
        tabelaPendentes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPendentes.setRowHeight(28);
        tabelaPendentes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelaPendentes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabelaPendentes.getTableHeader().setBackground(COR_PRIMARIA);
        tabelaPendentes.getTableHeader().setForeground(Color.WHITE);
        tabelaPendentes.setGridColor(new Color(220, 220, 220));
        tabelaPendentes.setSelectionBackground(new Color(52, 152, 219));
        tabelaPendentes.setSelectionForeground(Color.WHITE);
        tabelaPendentes.setBackground(Color.WHITE);

        // Configurar larguras das colunas
        tabelaPendentes.getColumnModel().getColumn(0).setPreferredWidth(100);  // Número
        tabelaPendentes.getColumnModel().getColumn(1).setPreferredWidth(300);  // Descrição
        tabelaPendentes.getColumnModel().getColumn(2).setPreferredWidth(150);  // Localização
        tabelaPendentes.getColumnModel().getColumn(3).setPreferredWidth(150);  // Responsável
        tabelaPendentes.getColumnModel().getColumn(4).setPreferredWidth(100);  // Estado

        // Duplo-clique para ir ao campo de busca com o número preenchido
        tabelaPendentes.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabelaPendentes.getSelectedRow();
                    if (row >= 0) {
                        String numero = (String) modeloTabelaPendentes.getValueAt(row, 0);
                        // Delegar ao frame: trocar para a aba de coleta e buscar
                        if (onColetarPatrimonio != null) {
                            onColetarPatrimonio.accept(numero);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPendentes = new JScrollPane(tabelaPendentes);
        scrollPendentes.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPendentes.setBackground(Color.WHITE);

        // ========== PAINEL INFERIOR: DICA ==========
        JPanel painelDica = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelDica.setBackground(new Color(255, 248, 220));
        painelDica.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));

        JLabel lblDica = new JLabel("💡 Dica: Dê duplo-clique em um item para ir diretamente à tela de coleta com o número preenchido");
        lblDica.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDica.setForeground(new Color(133, 100, 4));
        painelDica.add(lblDica);

        // ========== MONTAGEM FINAL ==========
        add(painelTopoCompleto, BorderLayout.NORTH);
        add(scrollPendentes, BorderLayout.CENTER);
        add(painelDica, BorderLayout.SOUTH);
    }

    // ===================== API pública (usada pelo frame) =====================

    /**
     * Carrega os itens pendentes da sala informada. Passe {@code null} para
     * limpar o painel (nenhuma sala selecionada).
     */
    public void carregarParaSala(Sala sala) {
        this.salaAtual = sala;
        carregarItensPendentes();
    }

    /**
     * Habilita/desabilita os componentes interativos do painel.
     * O botão de exportar só é habilitado se houver linhas na tabela.
     */
    public void setComponentesAtivos(boolean ativo) {
        if (campoBuscaPendentes != null) {
            campoBuscaPendentes.setEnabled(ativo);
        }
        if (tabelaPendentes != null) {
            tabelaPendentes.setEnabled(ativo);
        }
        if (btnExportarPendentes != null) {
            btnExportarPendentes.setEnabled(ativo && modeloTabelaPendentes.getRowCount() > 0);
        }
    }

    // ===================== Lógica interna =====================

    private void carregarItensPendentes() {
        final Sala salaSelecionada = salaAtual;
        if (salaSelecionada == null) {
            lblTotalPendentes.setText("⚠️ Selecione uma sala para ver os itens pendentes");
            lblProgressoColeta.setText("Progresso: -");
            progressBarColeta.setValue(0);
            progressBarColeta.setString("0%");
            modeloTabelaPendentes.setRowCount(0);
            btnExportarPendentes.setEnabled(false);
            return;
        }

        // Mostrar loading
        lblTotalPendentes.setText("⏳ Carregando itens pendentes...");
        tabelaPendentes.setEnabled(false);

        // Executar em background
        SwingWorker<PendentesResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PendentesResult doInBackground() throws Exception {
                int idSala = salaSelecionada.getId();

                // Buscar todos os patrimônios da sala
                List<Patrimonio> todosPatrimonios = patrimonioDAO.buscarPorSala(idSala);

                // Buscar IDs dos patrimônios já coletados no inventário atual
                Inventario inventarioAtivo = inventarioDAO.buscarInventarioAtivo();
                List<Integer> idsColetados = new ArrayList<>();
                if (inventarioAtivo != null) {
                    idsColetados = patrimonioDAO.buscarPatrimoniosColetados(inventarioAtivo.getId());
                }

                // Filtrar apenas os não coletados
                List<Integer> finalIdsColetados = idsColetados;
                List<Patrimonio> pendentes = todosPatrimonios.stream()
                        .filter(p -> !finalIdsColetados.contains(p.getId()))
                        .toList();

                int totalSala = todosPatrimonios.size();
                int totalColetados = totalSala - pendentes.size();
                int totalPendentes = pendentes.size();
                double percentual = totalSala > 0 ? (totalColetados * 100.0 / totalSala) : 0;

                return new PendentesResult(pendentes, totalSala, totalColetados, totalPendentes, percentual);
            }

            @Override
            protected void done() {
                try {
                    PendentesResult resultado = get();

                    // Atualizar labels
                    lblTotalPendentes.setText(String.format("📊 %d itens pendentes de %d na sala %s",
                            resultado.totalPendentes, resultado.totalSala, salaSelecionada.getNumeroSala()));

                    lblProgressoColeta.setText(String.format("✅ Coletados: %d de %d (%.1f%%)",
                            resultado.totalColetados, resultado.totalSala, resultado.percentual));

                    // Atualizar barra de progresso
                    progressBarColeta.setValue((int) resultado.percentual);
                    progressBarColeta.setString(String.format("%.1f%%", resultado.percentual));

                    // Mudar cor da barra baseado no progresso
                    if (resultado.percentual >= 100) {
                        progressBarColeta.setForeground(new Color(46, 204, 113)); // Verde
                    } else if (resultado.percentual >= 50) {
                        progressBarColeta.setForeground(new Color(52, 152, 219)); // Azul
                    } else if (resultado.percentual >= 25) {
                        progressBarColeta.setForeground(new Color(241, 196, 15)); // Amarelo
                    } else {
                        progressBarColeta.setForeground(new Color(231, 76, 60)); // Vermelho
                    }

                    // Preencher tabela
                    modeloTabelaPendentes.setRowCount(0);
                    for (Patrimonio p : resultado.pendentes) {
                        Object[] linha = {
                            p.getNumero(),
                            p.getDescricao() != null ? p.getDescricao() : "-",
                            p.getNomeSala() != null ? p.getNomeSala() : "-",
                            p.getNomeResponsavel() != null ? p.getNomeResponsavel() : "-",
                            p.getEstadoConservacao() != null ? p.getEstadoConservacao() : "-"
                        };
                        modeloTabelaPendentes.addRow(linha);
                    }

                    tabelaPendentes.setEnabled(true);
                    btnExportarPendentes.setEnabled(resultado.totalPendentes > 0);

                    LOG.debug("[PENDENTES] Carregados {} itens pendentes para sala {}",
                            resultado.totalPendentes, salaSelecionada.getNumeroSala());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    tratarErro("Operação interrompida");
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause();
                    String mensagemErro = causa != null ? causa.getMessage() : e.getMessage();
                    tratarErro(mensagemErro);
                }
            }
        };

        if (workerRegistrar != null) {
            workerRegistrar.accept(worker);
        }
        worker.execute();
    }

    private void tratarErro(String mensagemErro) {
        lblTotalPendentes.setText("❌ Erro ao carregar itens pendentes");
        lblProgressoColeta.setText("Progresso: -");
        progressBarColeta.setValue(0);
        tabelaPendentes.setEnabled(true);
        LOG.error("Erro ao carregar itens pendentes: {}", mensagemErro);

        int opcao = JOptionPane.showOptionDialog(this,
                "Erro ao carregar itens pendentes: " + mensagemErro + "\n\n" +
                "Deseja tentar novamente?",
                "Erro ao Carregar Pendentes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{"Tentar Novamente", "Cancelar"},
                "Tentar Novamente");

        if (opcao == JOptionPane.YES_OPTION) {
            carregarItensPendentes(); // Retry
        }
    }

    private void filtrarTabela(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            // Recarregar todos os itens
            carregarItensPendentes();
            return;
        }

        String filtroLower = filtro.toLowerCase().trim();

        for (int i = 0; i < tabelaPendentes.getRowCount(); i++) {
            String numero = String.valueOf(modeloTabelaPendentes.getValueAt(i, 0)).toLowerCase();
            String descricao = String.valueOf(modeloTabelaPendentes.getValueAt(i, 1)).toLowerCase();

            if (numero.contains(filtroLower) || descricao.contains(filtroLower)) {
                tabelaPendentes.setRowSelectionInterval(i, i);
                tabelaPendentes.scrollRectToVisible(tabelaPendentes.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void exportarLista() {
        if (modeloTabelaPendentes.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Não há itens pendentes para exportar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomeSala = salaAtual != null ? salaAtual.getNumeroSala() : "sala";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Lista de Pendentes");
        fileChooser.setSelectedFile(new java.io.File("pendentes_" + nomeSala + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                // Cabeçalho
                writer.println("Número;Descrição;Localização;Responsável;Estado");

                // Dados
                for (int i = 0; i < modeloTabelaPendentes.getRowCount(); i++) {
                    StringBuilder linha = new StringBuilder();
                    for (int j = 0; j < modeloTabelaPendentes.getColumnCount(); j++) {
                        if (j > 0) linha.append(";");
                        Object valor = modeloTabelaPendentes.getValueAt(i, j);
                        linha.append(valor != null ? valor.toString() : "");
                    }
                    writer.println(linha);
                }

                JOptionPane.showMessageDialog(this,
                    "Lista exportada com sucesso!\n" + fileChooser.getSelectedFile().getAbsolutePath(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erro ao exportar: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Resultado da busca de pendentes. */
    private static class PendentesResult {
        final List<Patrimonio> pendentes;
        final int totalSala;
        final int totalColetados;
        final int totalPendentes;
        final double percentual;

        PendentesResult(List<Patrimonio> pendentes, int totalSala, int totalColetados,
                        int totalPendentes, double percentual) {
            this.pendentes = pendentes;
            this.totalSala = totalSala;
            this.totalColetados = totalColetados;
            this.totalPendentes = totalPendentes;
            this.percentual = percentual;
        }
    }
}
