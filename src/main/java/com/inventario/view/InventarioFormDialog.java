package com.inventario.view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.inventario.model.Inventario;
import com.inventario.model.Responsavel;
import com.inventario.model.Setor;
import com.inventario.model.Usuario;
import com.inventario.model.ParticipanteInventario;
import com.inventario.service.ServiceFactory;
import com.inventario.service.ResponsavelService;
import com.inventario.service.SetorService;
import com.inventario.service.InventarioService;
import com.inventario.service.UsuarioService;
import com.inventario.service.BusinessException;

/**
 * Formulário para criar novos inventários
 */
public class InventarioFormDialog extends JDialog {
    private Inventario inventario;
    private boolean confirmado = false;
    
    // Campos do formulário
    private JTextField campoDescricao;
    private JSpinner spinnerDataInicio;
    private JSpinner spinnerDataFim;
    private JComboBox<String> comboResponsavel;
    private JComboBox<String> comboTipo;
    private JTextArea areaObservacoes;
    private JCheckBox checkIncluirTodos;
    private JList<Setor> listaSetores;
    
    private JButton btnSalvar, btnCancelar;
    
    // Lista de setores para controle
    private List<Setor> todosSetores;
    private DefaultListModel<Setor> modeloListaSetores;
    
    // Componentes para seleção de participantes
    private JCheckBox checkIncluirTodosUsuarios;
    private JList<Usuario> listaUsuarios;
    private DefaultListModel<Usuario> modeloListaUsuarios;
    private List<Usuario> todosUsuarios;
    
    // Componentes para busca e gerenciamento de participantes
    private JTextField campoBuscaParticipante;
    private JList<Usuario> listaBuscaUsuarios;
    private DefaultListModel<Usuario> modeloBuscaUsuarios;
    private JList<Usuario> listaParticipantesSelecionados;
    private DefaultListModel<Usuario> modeloParticipantesSelecionados;
    private JButton btnAdicionarParticipante;
    private JButton btnRemoverParticipante;
    
    public InventarioFormDialog(Frame parent, Inventario inventario) {
        super(parent, inventario == null ? "Novo Inventário" : "Editar Inventário", true);
        this.inventario = inventario;
        initComponents();
        if (inventario != null) {
            preencherCampos();
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel principal com abas
        JTabbedPane abas = new JTabbedPane();
        
        // Aba 1: Informações Gerais
        JPanel abaGeral = criarAbaGeral();
        abas.addTab("Informações Gerais", abaGeral);
        
        // Aba 2: Escopo
        JPanel abaEscopo = criarAbaEscopo();
        abas.addTab("Escopo do Inventário", abaEscopo);
        
        // Aba 3: Participantes
        JPanel abaParticipantes = criarAbaParticipantes();
        abas.addTab("Participantes", abaParticipantes);
        
        add(abas, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        // Definir texto do botão baseado no contexto (novo ou edição)
        String textoBotao = (inventario == null) ? "Criar Inventário" : "Salvar Alterações";
        btnSalvar = new JButton(textoBotao);
        btnCancelar = new JButton("Cancelar");
        
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Configurar eventos
        configurarEventos();
        
        setSize(600, 500);
        setLocationRelativeTo(getParent());
    }
    
    private JPanel criarAbaGeral() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Descrição
        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        campoDescricao = new JTextField(35);
        painel.add(campoDescricao, gbc);
        
        // Data de início
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(new JLabel("Data de Início:"), gbc);
        gbc.gridx = 1;
        spinnerDataInicio = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorInicio = new JSpinner.DateEditor(spinnerDataInicio, "dd/MM/yyyy");
        spinnerDataInicio.setEditor(editorInicio);
        spinnerDataInicio.setValue(new Date());
        painel.add(spinnerDataInicio, gbc);
        
        // Data de fim
        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(new JLabel("Data de Fim:"), gbc);
        gbc.gridx = 1;
        spinnerDataFim = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFim = new JSpinner.DateEditor(spinnerDataFim, "dd/MM/yyyy");
        spinnerDataFim.setEditor(editorFim);
        // Definir data fim como 30 dias após o início
        Date dataFim = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000));
        spinnerDataFim.setValue(dataFim);
        painel.add(spinnerDataFim, gbc);
        
        // Responsável
        gbc.gridx = 0; gbc.gridy = 3;
        painel.add(new JLabel("Responsável:"), gbc);
        gbc.gridx = 1;
        comboResponsavel = new JComboBox<>();
        carregarResponsaveis();
        painel.add(comboResponsavel, gbc);
        
        // Tipo de inventário
        gbc.gridx = 0; gbc.gridy = 4;
        painel.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        comboTipo = new JComboBox<>(new String[]{"ANUAL", "INICIAL", "TRANSFERÊNCIA DE RESPONSABILIDADE", "EXTINÇÃO","EVENTUAL"});
        painel.add(comboTipo, gbc);
        
        // Observações
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        painel.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        areaObservacoes = new JTextArea(4, 25);
        areaObservacoes.setLineWrap(true);
        areaObservacoes.setWrapStyleWord(true);
        JScrollPane scrollObservacoes = new JScrollPane(areaObservacoes);
        painel.add(scrollObservacoes, gbc);
        
        return painel;
    }
    
    private JPanel criarAbaParticipantes() {
        JPanel painel = new JPanel(new BorderLayout());
        
        // Painel superior com opções
        JPanel painelOpcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkIncluirTodosUsuarios = new JCheckBox("Incluir todos os usuários ativos", false);
        painelOpcoes.add(checkIncluirTodosUsuarios);
        painel.add(painelOpcoes, BorderLayout.NORTH);
        
        // Painel principal dividido em duas partes
        JPanel painelPrincipal = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Painel esquerdo - Busca de usuários
        JPanel painelBusca = new JPanel(new BorderLayout());
        painelBusca.setBorder(BorderFactory.createTitledBorder("Buscar Usuários"));
        
        // Campo de busca
        JPanel painelCampoBusca = new JPanel(new BorderLayout());
        painelCampoBusca.add(new JLabel("Buscar por nome ou login:"), BorderLayout.NORTH);
        campoBuscaParticipante = new JTextField();
        painelCampoBusca.add(campoBuscaParticipante, BorderLayout.CENTER);
        painelBusca.add(painelCampoBusca, BorderLayout.NORTH);
        
        // Lista de usuários para busca
        modeloBuscaUsuarios = new DefaultListModel<>();
        carregarUsuarios();
        listaBuscaUsuarios = new JList<>(modeloBuscaUsuarios);
        listaBuscaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configurar renderer personalizado
        DefaultListCellRenderer rendererBusca = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Usuario) {
                    Usuario usuario = (Usuario) value;
                    setText(usuario.getNomeCompleto() + " (" + usuario.getLogin() + ")");
                }
                return this;
            }
        };
        listaBuscaUsuarios.setCellRenderer(rendererBusca);
        
        JScrollPane scrollBusca = new JScrollPane(listaBuscaUsuarios);
        painelBusca.add(scrollBusca, BorderLayout.CENTER);
        
        // Botão adicionar
        btnAdicionarParticipante = new JButton("Adicionar →");
        painelBusca.add(btnAdicionarParticipante, BorderLayout.SOUTH);
        
        // Painel direito - Participantes selecionados
        JPanel painelSelecionados = new JPanel(new BorderLayout());
        painelSelecionados.setBorder(BorderFactory.createTitledBorder("Participantes do Inventário"));
        
        modeloParticipantesSelecionados = new DefaultListModel<>();
        listaParticipantesSelecionados = new JList<>(modeloParticipantesSelecionados);
        listaParticipantesSelecionados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaParticipantesSelecionados.setCellRenderer(rendererBusca);
        
        JScrollPane scrollSelecionados = new JScrollPane(listaParticipantesSelecionados);
        painelSelecionados.add(scrollSelecionados, BorderLayout.CENTER);
        
        // Botão remover
        btnRemoverParticipante = new JButton("← Remover");
        painelSelecionados.add(btnRemoverParticipante, BorderLayout.SOUTH);
        
        // Adicionar painéis ao painel principal
        painelPrincipal.add(painelBusca);
        painelPrincipal.add(painelSelecionados);
        painel.add(painelPrincipal, BorderLayout.CENTER);
        
        // Configurar eventos
        configurarEventosParticipantes();
        
        return painel;
    }
    
    private JPanel criarAbaEscopo() {
        JPanel painel = new JPanel(new BorderLayout());
        
        // Painel superior com opções
        JPanel painelOpcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkIncluirTodos = new JCheckBox("Incluir todos os setores", true);
        painelOpcoes.add(checkIncluirTodos);
        painel.add(painelOpcoes, BorderLayout.NORTH);
        
        // Lista de setores
        JPanel painelSetores = new JPanel(new BorderLayout());
        painelSetores.add(new JLabel("Setores a serem inventariados:"), BorderLayout.NORTH);
        
        modeloListaSetores = new DefaultListModel<>();
        carregarSetores();
        listaSetores = new JList<>(modeloListaSetores);
        listaSetores.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listaSetores.setEnabled(false); // Inicialmente desabilitada
        
        // Configurar renderer personalizado para exibir nome do setor
        listaSetores.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Setor) {
                    setText(((Setor) value).getNome());
                }
                return this;
            }
        });
        
        JScrollPane scrollSetores = new JScrollPane(listaSetores);
        painelSetores.add(scrollSetores, BorderLayout.CENTER);
        painel.add(painelSetores, BorderLayout.CENTER);
        
        // Configurar evento do checkbox
        checkIncluirTodos.addActionListener(e -> {
            boolean incluirTodos = checkIncluirTodos.isSelected();
            listaSetores.setEnabled(!incluirTodos);
            if (incluirTodos) {
                listaSetores.clearSelection();
            }
        });
        
        return painel;
    }
    
    private void configurarEventos() {
        btnSalvar.addActionListener(e -> salvarInventario());
        btnCancelar.addActionListener(e -> dispose());
    }
    
    private void configurarEventosParticipantes() {
        // Evento do checkbox "Incluir todos os usuários"
        checkIncluirTodosUsuarios.addActionListener(e -> {
            boolean incluirTodos = checkIncluirTodosUsuarios.isSelected();
            campoBuscaParticipante.setEnabled(!incluirTodos);
            listaBuscaUsuarios.setEnabled(!incluirTodos);
            listaParticipantesSelecionados.setEnabled(!incluirTodos);
            btnAdicionarParticipante.setEnabled(!incluirTodos);
            btnRemoverParticipante.setEnabled(!incluirTodos);
            
            if (incluirTodos) {
                modeloParticipantesSelecionados.clear();
                campoBuscaParticipante.setText("");
                filtrarUsuarios("");
            }
        });
        
        // Evento do campo de busca
        campoBuscaParticipante.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String textoBusca = campoBuscaParticipante.getText().toLowerCase().trim();
                filtrarUsuarios(textoBusca);
            }
        });
        
        // Evento do botão adicionar
        btnAdicionarParticipante.addActionListener(e -> adicionarParticipante());
        
        // Evento do botão remover
        btnRemoverParticipante.addActionListener(e -> removerParticipante());
        
        // Duplo clique para adicionar usuário
        listaBuscaUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaBuscaUsuarios.getSelectedValue() != null) {
                    adicionarParticipante();
                }
            }
        });
        
        // Duplo clique para remover usuário
        listaParticipantesSelecionados.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && listaParticipantesSelecionados.getSelectedValue() != null) {
                    removerParticipante();
                }
            }
        });
    }
    
    private void carregarResponsaveis() {
        try {
            System.out.println("[DEBUG] Iniciando carregamento de responsáveis...");
            comboResponsavel.addItem("Selecione um responsável...");
            
            ResponsavelService responsavelService = ServiceFactory.getResponsavelService();
            System.out.println("[DEBUG] ResponsavelService obtido com sucesso");
            
            List<Responsavel> responsaveis = responsavelService.listarAtivos();
            System.out.println("[DEBUG] Quantidade de responsáveis encontrados: " + responsaveis.size());
            
            if (responsaveis.isEmpty()) {
                System.out.println("[AVISO] Nenhum responsável encontrado no banco de dados!");
                carregarResponsaveisExemplo();
                
                JOptionPane.showMessageDialog(this, 
                    "Nenhum responsável encontrado no banco de dados.\n" +
                    "Foram carregados responsáveis de exemplo.\n" +
                    "Cadastre responsáveis no sistema para usar dados reais.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                for (Responsavel resp : responsaveis) {
                    System.out.println("[DEBUG] Adicionando responsável: " + resp.getNome());
                    comboResponsavel.addItem(resp.getNome());
                }
                System.out.println("[DEBUG] Responsáveis carregados com sucesso!");
            }
            
        } catch (Exception e) {
            System.err.println("[ERRO] Erro ao carregar responsáveis: " + e.getMessage());
            e.printStackTrace();
            
            // Carregar responsáveis de exemplo em caso de erro
            carregarResponsaveisExemplo();
            
            // Mostrar erro para o usuário
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar responsáveis do banco de dados:\n" + e.getMessage() +
                "\n\nForam carregados responsáveis de exemplo.\n" +
                "Verifique a conexão com o banco de dados.", 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarResponsaveisExemplo() {
        System.out.println("[DEBUG] Carregando responsáveis de exemplo...");
        
        // Limpar combo (exceto o primeiro item "Selecione...")
        while (comboResponsavel.getItemCount() > 1) {
            comboResponsavel.removeItemAt(1);
        }
        
        // Adicionar responsáveis de exemplo
        String[] responsaveisExemplo = {
            "João Silva - Administrador",
            "Maria Santos - Coordenadora TI",
            "Pedro Oliveira - Gerente Financeiro",
            "Ana Costa - Supervisora RH",
            "Carlos Ferreira - Responsável Almoxarifado"
        };
        
        for (String responsavel : responsaveisExemplo) {
            comboResponsavel.addItem(responsavel);
            System.out.println("[DEBUG] Adicionado responsável de exemplo: " + responsavel);
        }
        
        System.out.println("[DEBUG] Responsáveis de exemplo carregados!");
    }
    
    private void carregarSetores() {
        try {
            SetorService setorService = ServiceFactory.getSetorService();
            todosSetores = setorService.listarTodos();
            
            modeloListaSetores.clear();
            for (Setor setor : todosSetores) {
                modeloListaSetores.addElement(setor);
            }
            
            System.out.println("[DEBUG] Carregados " + todosSetores.size() + " setores");
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar setores: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback para setores de exemplo em caso de erro
            todosSetores = new ArrayList<>();
            String[] nomesSetores = {"Administração", "Tecnologia da Informação", 
                                   "Recursos Humanos", "Financeiro", "Almoxarifado"};
            
            for (int i = 0; i < nomesSetores.length; i++) {
                Setor setor = new Setor();
                setor.setId(i + 1);
                setor.setNome(nomesSetores[i]);
                todosSetores.add(setor);
                modeloListaSetores.addElement(setor);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar setores do banco de dados.\n" +
                "Foram carregados setores de exemplo.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void carregarUsuarios() {
        try {
            System.out.println("[DEBUG] Iniciando carregamento de usuários...");
            
            UsuarioService usuarioService = ServiceFactory.getUsuarioService();
            todosUsuarios = usuarioService.listarTodos();
            
            System.out.println("[DEBUG] Quantidade de usuários encontrados: " + todosUsuarios.size());
            
            modeloBuscaUsuarios.clear();
            for (Usuario usuario : todosUsuarios) {
                modeloBuscaUsuarios.addElement(usuario);
                System.out.println("[DEBUG] Adicionando usuário: " + usuario.getNomeCompleto());
            }
            
            System.out.println("[DEBUG] Usuários carregados com sucesso!");
            
        } catch (Exception e) {
            System.err.println("[ERRO] Erro ao carregar usuários: " + e.getMessage());
            e.printStackTrace();
            
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar usuários do banco de dados:\n" + e.getMessage(), 
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarUsuarios(String textoBusca) {
        modeloBuscaUsuarios.clear();
        
        if (todosUsuarios != null) {
            for (Usuario usuario : todosUsuarios) {
                // Verificar se o usuário já está na lista de selecionados
                boolean jaAdicionado = false;
                for (int i = 0; i < modeloParticipantesSelecionados.size(); i++) {
                    Usuario usuarioSelecionado = modeloParticipantesSelecionados.getElementAt(i);
                    if (usuarioSelecionado.getId() == usuario.getId()) {
                        jaAdicionado = true;
                        break;
                    }
                }
                
                // Se não foi adicionado e atende ao filtro, incluir na lista
                if (!jaAdicionado) {
                    if (textoBusca.isEmpty() || 
                        usuario.getNomeCompleto().toLowerCase().contains(textoBusca) ||
                        usuario.getLogin().toLowerCase().contains(textoBusca)) {
                        modeloBuscaUsuarios.addElement(usuario);
                    }
                }
            }
        }
    }
    
    private void adicionarParticipante() {
        Usuario usuarioSelecionado = listaBuscaUsuarios.getSelectedValue();
        if (usuarioSelecionado != null) {
            // Verificar se o usuário já está na lista
            boolean jaExiste = false;
            for (int i = 0; i < modeloParticipantesSelecionados.size(); i++) {
                Usuario usuario = modeloParticipantesSelecionados.getElementAt(i);
                if (usuario.getId() == usuarioSelecionado.getId()) {
                    jaExiste = true;
                    break;
                }
            }
            
            if (!jaExiste) {
                modeloParticipantesSelecionados.addElement(usuarioSelecionado);
                // Atualizar a lista de busca para remover o usuário adicionado
                String textoBusca = campoBuscaParticipante.getText().toLowerCase().trim();
                filtrarUsuarios(textoBusca);
                
                System.out.println("[DEBUG] Participante adicionado: " + usuarioSelecionado.getNomeCompleto());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Este usuário já foi adicionado como participante.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Selecione um usuário para adicionar.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void removerParticipante() {
        Usuario usuarioSelecionado = listaParticipantesSelecionados.getSelectedValue();
        if (usuarioSelecionado != null) {
            modeloParticipantesSelecionados.removeElement(usuarioSelecionado);
            // Atualizar a lista de busca para incluir o usuário removido
            String textoBusca = campoBuscaParticipante.getText().toLowerCase().trim();
            filtrarUsuarios(textoBusca);
            
            System.out.println("[DEBUG] Participante removido: " + usuarioSelecionado.getNomeCompleto());
        } else {
            JOptionPane.showMessageDialog(this, 
                "Selecione um participante para remover.", 
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void preencherCampos() {
        if (inventario != null) {
            campoDescricao.setText(inventario.getNome());
            
            if (inventario.getDataInicio() != null) {
                spinnerDataInicio.setValue(inventario.getDataInicio());
            }
            
            if (inventario.getDataFim() != null) {
                spinnerDataFim.setValue(inventario.getDataFim());
            }
            
            if (inventario.getTipoInventario() != null) {
                comboTipo.setSelectedItem(inventario.getTipoInventario());
            }
            
            // Selecionar o responsável no combobox
            if (inventario.getResponsavelInventario() != null && !inventario.getResponsavelInventario().trim().isEmpty()) {
                String responsavelInventario = inventario.getResponsavelInventario();
                System.out.println("[DEBUG] Tentando selecionar responsável: " + responsavelInventario);
                
                // Procurar o responsável no combobox
                boolean encontrado = false;
                for (int i = 0; i < comboResponsavel.getItemCount(); i++) {
                    String item = comboResponsavel.getItemAt(i);
                    if (item != null && item.equals(responsavelInventario)) {
                        comboResponsavel.setSelectedIndex(i);
                        encontrado = true;
                        System.out.println("[DEBUG] Responsável selecionado com sucesso: " + item);
                        break;
                    }
                }
                
                if (!encontrado) {
                    System.out.println("[AVISO] Responsável não encontrado no combobox: " + responsavelInventario);
                    // Adicionar o responsável ao combobox se não existir
                    comboResponsavel.addItem(responsavelInventario);
                    comboResponsavel.setSelectedItem(responsavelInventario);
                    System.out.println("[DEBUG] Responsável adicionado e selecionado: " + responsavelInventario);
                }
            }
            
            areaObservacoes.setText(inventario.getObservacao());
            
            // Carregar configuração de setores do inventário
            carregarConfiguracaoSetores();
            
            // Carregar configuração de participantes do inventário
            carregarConfiguracaoParticipantes();
        }
    }
    
    /**
     * Carrega a configuração de setores de um inventário existente
     */
    private void carregarConfiguracaoSetores() {
        if (inventario == null || inventario.getId() == null) {
            return;
        }
        
        try {
            // Verificar se inclui todos os setores
            // TODO: Implementar método no serviço para verificar se inclui todos
            boolean incluiTodos = false; // Por enquanto, assume que não inclui todos
            checkIncluirTodos.setSelected(incluiTodos);
            listaSetores.setEnabled(!incluiTodos);
            
            if (!incluiTodos) {
                // Buscar setores específicos do inventário
                // TODO: Implementar busca de setores do inventário no serviço
                List<Setor> setoresInventario = new ArrayList<>();
                
                // Selecionar os setores na lista
                List<Integer> indicesSelecionar = new ArrayList<>();
                for (Setor setorInventario : setoresInventario) {
                    for (int i = 0; i < modeloListaSetores.size(); i++) {
                        Setor setorLista = modeloListaSetores.getElementAt(i);
                        if (setorLista.getId() == setorInventario.getId()) {
                            indicesSelecionar.add(i);
                            break;
                        }
                    }
                }
                
                // Converter para array de int
                int[] indices = indicesSelecionar.stream().mapToInt(Integer::intValue).toArray();
                listaSetores.setSelectedIndices(indices);
                
                System.out.println("[DEBUG] Carregados " + setoresInventario.size() + " setores do inventário");
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar configuração de setores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void salvarInventario() {
        try {
            // Validar campos obrigatórios
            if (campoDescricao.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "A descrição é obrigatória.");
                return;
            }
            
            if (comboResponsavel.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Selecione um responsável.");
                return;
            }
            
            // Validar datas
            Date dataInicio = (Date) spinnerDataInicio.getValue();
            Date dataFim = (Date) spinnerDataFim.getValue();
            
            if (dataFim.before(dataInicio)) {
                JOptionPane.showMessageDialog(this, "A data de fim deve ser posterior à data de início.");
                return;
            }
            
            // Determinar se é criação ou atualização
            boolean isNovoInventario = (inventario == null);
            
            // Criar inventário se for novo
            if (isNovoInventario) {
                inventario = new Inventario();
            }
            
            // Preencher dados do inventário
            inventario.setNome(campoDescricao.getText().trim());
            inventario.setDataInicio(dataInicio);
            inventario.setDataFim(dataFim);
            inventario.setTipoInventario((String) comboTipo.getSelectedItem());
            inventario.setObservacao(areaObservacoes.getText().trim());
            
            // Definir responsável selecionado
            String responsavelSelecionado = (String) comboResponsavel.getSelectedItem();
            inventario.setResponsavelInventario(responsavelSelecionado);
            
            // Definir status apenas para novos inventários
            if (isNovoInventario) {
                inventario.setStatusInventario(Inventario.STATUS_EM_ANDAMENTO); // Status ABERTO para novos inventários
                inventario.setPercentualConclusao(new java.math.BigDecimal("0.00"));
            }
            
            // Salvar no banco de dados
            InventarioService inventarioService = ServiceFactory.getInventarioService();
            boolean sucesso;
            String mensagemSucesso;
            
            if (isNovoInventario) {
                sucesso = inventarioService.salvar(inventario);
                mensagemSucesso = "Inventário criado com sucesso!";
            } else {
                sucesso = inventarioService.atualizar(inventario);
                mensagemSucesso = "Inventário atualizado com sucesso!";
            }
            
            // Se o inventário foi salvo com sucesso, salvar configuração de setores e participantes
            if (sucesso) {
                boolean sucessoSetores = salvarConfiguracaoSetores();
                boolean sucessoParticipantes = salvarConfiguracaoParticipantes();
                
                if (!sucessoSetores || !sucessoParticipantes) {
                    String mensagemErro = "Inventário salvo, mas houve erro ao configurar:";
                    if (!sucessoSetores) mensagemErro += "\n- Setores";
                    if (!sucessoParticipantes) mensagemErro += "\n- Participantes";
                    mensagemErro += "\n\nVerifique as configurações posteriormente.";
                    
                    JOptionPane.showMessageDialog(this, mensagemErro, "Aviso", JOptionPane.WARNING_MESSAGE);
                }
                
                confirmado = true;
                dispose();
                JOptionPane.showMessageDialog(getParent(), mensagemSucesso);
            } else {
                String operacao = isNovoInventario ? "criar" : "atualizar";
                JOptionPane.showMessageDialog(this, "Erro ao " + operacao + " o inventário. Tente novamente.");
            }
            
        } catch (Exception e) {
            String operacao = (inventario == null) ? "criar" : "atualizar";
            JOptionPane.showMessageDialog(this, "Erro ao " + operacao + " inventário: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public Inventario getInventario() {
        return inventario;
    }
    
    /**
     * Salva a configuração de setores do inventário
     */
    private boolean salvarConfiguracaoSetores() {
        try {
            boolean incluirTodos = checkIncluirTodos.isSelected();
            List<Integer> idsSetoresSelecionados = new ArrayList<>();
            
            if (!incluirTodos) {
                // Obter setores selecionados
                List<Setor> setoresSelecionados = listaSetores.getSelectedValuesList();
                
                if (setoresSelecionados.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Selecione pelo menos um setor ou marque 'Incluir todos os setores'.", 
                        "Validação", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                
                for (Setor setor : setoresSelecionados) {
                    idsSetoresSelecionados.add(setor.getId());
                }
                
                System.out.println("[DEBUG] Setores selecionados: " + idsSetoresSelecionados.size());
            } else {
                System.out.println("[DEBUG] Configurado para incluir todos os setores");
            }
            
            // Salvar configuração
            InventarioService inventarioService = ServiceFactory.getInventarioService();
            boolean sucesso = inventarioService.salvarConfiguracaoSetores(
                inventario.getId(), incluirTodos, idsSetoresSelecionados);
            
            if (sucesso) {
                System.out.println("[DEBUG] Configuração de setores salva com sucesso");
            } else {
                System.err.println("[ERRO] Falha ao salvar configuração de setores");
            }
            
            return sucesso;
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar configuração de setores: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Carrega a configuração de participantes de um inventário existente
     */
    private void carregarConfiguracaoParticipantes() {
        if (inventario == null || inventario.getId() == null) {
            return;
        }
        
        try {
            InventarioService inventarioService = ServiceFactory.getInventarioService();
            
            // Buscar participantes do inventário
            List<ParticipanteInventario> participantes = inventarioService.buscarParticipantes(inventario.getId());
            
            if (participantes.isEmpty()) {
                // Se não há participantes específicos, assume que inclui todos
                checkIncluirTodosUsuarios.setSelected(true);
                modeloParticipantesSelecionados.clear();
            } else {
                // Há participantes específicos
                checkIncluirTodosUsuarios.setSelected(false);
                modeloParticipantesSelecionados.clear();
                
                // Adicionar os usuários participantes à lista de selecionados
                for (ParticipanteInventario participante : participantes) {
                    for (Usuario usuario : todosUsuarios) {
                        if (usuario.getId() == participante.getIdUsuario()) {
                            modeloParticipantesSelecionados.addElement(usuario);
                            System.out.println("[DEBUG] Adicionando participante: " + usuario.getNomeCompleto());
                            break;
                        }
                    }
                }
                
                // Atualizar a lista de busca para remover os usuários já selecionados
                filtrarUsuarios("");
            }
            
            System.out.println("[DEBUG] Carregados " + participantes.size() + " participantes do inventário");
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar configuração de participantes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Salva a configuração de participantes do inventário
     */
    private boolean salvarConfiguracaoParticipantes() {
        try {
            boolean incluirTodos = checkIncluirTodosUsuarios.isSelected();
            List<Integer> idsUsuariosSelecionados = new ArrayList<>();
            
            if (!incluirTodos) {
                // Obter usuários selecionados da lista de participantes
                for (int i = 0; i < modeloParticipantesSelecionados.size(); i++) {
                    Usuario usuario = modeloParticipantesSelecionados.getElementAt(i);
                    idsUsuariosSelecionados.add(usuario.getId());
                }
                
                if (idsUsuariosSelecionados.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Adicione pelo menos um participante ou marque 'Incluir todos os usuários ativos'.", 
                        "Validação", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                
                System.out.println("[DEBUG] Usuários selecionados: " + idsUsuariosSelecionados.size());
            } else {
                // Se incluir todos, adicionar todos os usuários ativos
                if (todosUsuarios != null) {
                    for (Usuario usuario : todosUsuarios) {
                        idsUsuariosSelecionados.add(usuario.getId());
                    }
                }
                System.out.println("[DEBUG] Configurado para incluir todos os usuários (" + idsUsuariosSelecionados.size() + ")");
            }
            
            // Salvar configuração usando o serviço
            InventarioService inventarioService = ServiceFactory.getInventarioService();
            boolean sucesso = inventarioService.salvarConfiguracaoParticipantes(
                inventario.getId(), incluirTodos, idsUsuariosSelecionados);
            
            if (sucesso) {
                System.out.println("[DEBUG] Configuração de participantes salva com sucesso");
            } else {
                System.err.println("[ERRO] Falha ao salvar configuração de participantes");
            }
            
            return sucesso;
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar configuração de participantes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}