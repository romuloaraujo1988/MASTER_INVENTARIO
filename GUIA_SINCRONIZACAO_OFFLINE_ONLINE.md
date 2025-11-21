# 🔄 Guia de Implementação - Sincronização Offline para Online

## 📋 Visão Geral

Sistema completo de sincronização automática de dados offline para online, com suporte a:
- ✅ Detecção automática de conectividade
- ✅ Armazenamento local em SQLite
- ✅ Sincronização automática quando online
- ✅ Resolução de conflitos
- ✅ Retry automático em falhas
- ✅ Interface visual de status

---

## 🏗️ Arquitetura Atual

### Componentes Existentes

```
┌─────────────────────────────────────────────────────────────┐
│                    OfflineManager                            │
│  - Gerenciador central do modo offline                      │
│  - Coordena conectividade e sincronização                   │
│  - Estados: ONLINE, OFFLINE, SYNCING, ERROR                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Connectivity │ │   SQLite     │ │    Data      │
│   Manager    │ │  Connection  │ │ Synchronizer │
│              │ │              │ │              │
│ - Monitora   │ │ - Banco      │ │ - Executa    │
│   rede       │ │   local      │ │   sync       │
│ - Detecta    │ │ - Tabelas    │ │ - Resolve    │
│   mudanças   │ │   offline    │ │   conflitos  │
└──────────────┘ └──────────────┘ └──────────────┘
        │              │              │
        └──────────────┼──────────────┘
                       │
                       ▼
              ┌──────────────┐
              │  OfflineDAO  │
              │              │
              │ - CRUD local │
              │ - Controle   │
              │   de sync    │
              └──────────────┘
```

---

## 🚀 Como Usar o Sistema Offline

### 1. Inicialização no Aplicativo

```java
// No método main() ou na inicialização do sistema
public class SistemaInventarioApplication {
    
    public static void main(String[] args) {
        try {
            // Inicializar sistema offline
            OfflineManager offlineManager = OfflineManager.getInstance();
            offlineManager.initialize();
            
            // Habilitar modo offline
            offlineManager.enableOfflineMode();
            
            System.out.println("Sistema offline inicializado: " + 
                offlineManager.getCurrentState());
            
            // Continuar com inicialização normal
            // ...
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar sistema offline: " + e.getMessage());
        }
    }
}
```

### 2. Salvando Coletas (Automático Offline/Online)

```java
// No ColetaFrame_v2 ou qualquer tela de coleta
public class ColetaFrame_v2 extends JFrame {
    
    private ColetaOfflineService coletaOfflineService;
    
    public ColetaFrame_v2() {
        // Inicializar serviço
        this.coletaOfflineService = ColetaOfflineService.getInstance();
    }
    
    private void salvarColeta() {
        try {
            Coleta coleta = new Coleta();
            // Preencher dados da coleta
            coleta.setIdInventario(inventarioAtual.getId());
            coleta.setIdPatrimonio(patrimonioSelecionado.getId());
            coleta.setNumeroPatrimonio(patrimonioSelecionado.getNumero());
            coleta.setDataColeta(new Timestamp(System.currentTimeMillis()));
            // ... outros campos
            
            // Salvar (automático offline/online)
            int idColeta = coletaOfflineService.salvarColeta(coleta);
            
            if (idColeta > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Coleta salva com sucesso!\n" +
                    (offlineManager.isOperatingOffline() ? 
                        "Será sincronizada quando online." : 
                        "Sincronizada com o servidor."),
                    "Sucesso", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar coleta: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
```

### 3. Sincronização Manual

```java
// Botão "Sincronizar" na interface
private void btnSincronizarActionPerformed(ActionEvent evt) {
    // Verificar se está online
    if (offlineManager.isOperatingOffline()) {
        JOptionPane.showMessageDialog(this,
            "Sistema está offline. Conecte-se à rede para sincronizar.",
            "Offline",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Executar sincronização em thread separada
    new Thread(() -> {
        try {
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(true);
                lblStatus.setText("Sincronizando...");
            });
            
            // Executar sincronização
            DataSynchronizer.SyncResult result = 
                offlineManager.executarSincronizacaoManual();
            
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(false);
                
                if (result.success) {
                    lblStatus.setText("Sincronização concluída!");
                    JOptionPane.showMessageDialog(this,
                        String.format(
                            "Sincronização concluída com sucesso!\n\n" +
                            "Coletas sincronizadas: %d\n" +
                            "Patrimônios sincronizados: %d\n" +
                            "Tempo: %.2f segundos",
                            result.coletasSincronizadas,
                            result.patrimoniosSincronizados,
                            result.tempoDecorrido / 1000.0
                        ),
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    lblStatus.setText("Erro na sincronização");
                    JOptionPane.showMessageDialog(this,
                        "Erro na sincronização:\n" + result.errorMessage,
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(false);
                lblStatus.setText("Erro");
                JOptionPane.showMessageDialog(this,
                    "Erro ao sincronizar: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }, "SyncThread").start();
}
```

---

## 🎨 Interface Visual de Status

### Painel de Status Offline

```java
import javax.swing.*;
import java.awt.*;

public class OfflineStatusPanel extends JPanel {
    
    private JLabel lblStatus;
    private JLabel lblPendentes;
    private JButton btnSincronizar;
    private JButton btnForcarOffline;
    private JButton btnReconectar;
    
    private OfflineManager offlineManager;
    private ColetaOfflineService coletaOfflineService;
    
    public OfflineStatusPanel() {
        this.offlineManager = OfflineManager.getInstance();
        this.coletaOfflineService = ColetaOfflineService.getInstance();
        
        initComponents();
        setupListeners();
        atualizarStatus();
    }
    
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEtchedBorder());
        
        // Label de status
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblStatus);
        
        // Label de pendentes
        lblPendentes = new JLabel();
        add(lblPendentes);
        
        // Botão sincronizar
        btnSincronizar = new JButton("Sincronizar");
        btnSincronizar.addActionListener(e -> sincronizar());
        add(btnSincronizar);
        
        // Botão forçar offline
        btnForcarOffline = new JButton("Forçar Offline");
        btnForcarOffline.addActionListener(e -> forcarOffline());
        add(btnForcarOffline);
        
        // Botão reconectar
        btnReconectar = new JButton("Reconectar");
        btnReconectar.addActionListener(e -> reconectar());
        add(btnReconectar);
    }
    
    private void setupListeners() {
        // Listener de mudanças de estado
        offlineManager.addStateListener((oldState, newState) -> {
            SwingUtilities.invokeLater(this::atualizarStatus);
        });
        
        // Timer para atualizar periodicamente
        Timer timer = new Timer(5000, e -> atualizarStatus());
        timer.start();
    }
    
    private void atualizarStatus() {
        OfflineManager.OfflineState state = offlineManager.getCurrentState();
        int pendentes = coletaOfflineService.getQuantidadeColetasPendentes();
        
        // Atualizar label de status
        switch (state) {
            case ONLINE:
                lblStatus.setText("● ONLINE");
                lblStatus.setForeground(new Color(46, 204, 113)); // Verde
                btnSincronizar.setEnabled(true);
                btnForcarOffline.setEnabled(true);
                btnReconectar.setEnabled(false);
                break;
                
            case OFFLINE:
                lblStatus.setText("● OFFLINE");
                lblStatus.setForeground(new Color(231, 76, 60)); // Vermelho
                btnSincronizar.setEnabled(false);
                btnForcarOffline.setEnabled(false);
                btnReconectar.setEnabled(true);
                break;
                
            case SYNCING:
                lblStatus.setText("● SINCRONIZANDO...");
                lblStatus.setForeground(new Color(52, 152, 219)); // Azul
                btnSincronizar.setEnabled(false);
                btnForcarOffline.setEnabled(false);
                btnReconectar.setEnabled(false);
                break;
                
            case ERROR:
                lblStatus.setText("● ERRO");
                lblStatus.setForeground(new Color(231, 76, 60)); // Vermelho
                btnSincronizar.setEnabled(false);
                btnForcarOffline.setEnabled(false);
                btnReconectar.setEnabled(true);
                break;
                
            default:
                lblStatus.setText("● INICIALIZANDO...");
                lblStatus.setForeground(Color.GRAY);
                btnSincronizar.setEnabled(false);
                btnForcarOffline.setEnabled(false);
                btnReconectar.setEnabled(false);
        }
        
        // Atualizar pendentes
        if (pendentes > 0) {
            lblPendentes.setText(String.format("(%d pendentes)", pendentes));
            lblPendentes.setForeground(new Color(243, 156, 18)); // Laranja
        } else {
            lblPendentes.setText("(0 pendentes)");
            lblPendentes.setForeground(Color.GRAY);
        }
    }
    
    private void sincronizar() {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    btnSincronizar.setEnabled(false);
                    btnSincronizar.setText("Sincronizando...");
                });
                
                DataSynchronizer.SyncResult result = 
                    offlineManager.executarSincronizacaoManual();
                
                SwingUtilities.invokeLater(() -> {
                    btnSincronizar.setEnabled(true);
                    btnSincronizar.setText("Sincronizar");
                    
                    if (result.success) {
                        JOptionPane.showMessageDialog(this,
                            String.format("Sincronização concluída!\n\n" +
                                "Coletas: %d\nPatrimônios: %d",
                                result.coletasSincronizadas,
                                result.patrimoniosSincronizados),
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Erro: " + result.errorMessage,
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                    atualizarStatus();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnSincronizar.setEnabled(true);
                    btnSincronizar.setText("Sincronizar");
                    JOptionPane.showMessageDialog(this,
                        "Erro ao sincronizar: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "SyncThread").start();
    }
    
    private void forcarOffline() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja forçar o sistema para modo offline?\n" +
            "Todas as operações serão salvas localmente.",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            offlineManager.forceOfflineMode();
            atualizarStatus();
        }
    }
    
    private void reconectar() {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    btnReconectar.setEnabled(false);
                    btnReconectar.setText("Reconectando...");
                });
                
                boolean sucesso = offlineManager.tryReconnect();
                
                SwingUtilities.invokeLater(() -> {
                    btnReconectar.setEnabled(true);
                    btnReconectar.setText("Reconectar");
                    
                    if (sucesso) {
                        JOptionPane.showMessageDialog(this,
                            "Reconectado com sucesso!\nSincronização iniciada.",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Não foi possível reconectar.\nVerifique sua conexão.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    }
                    
                    atualizarStatus();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    btnReconectar.setEnabled(true);
                    btnReconectar.setText("Reconectar");
                    JOptionPane.showMessageDialog(this,
                        "Erro ao reconectar: " + e.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "ReconnectThread").start();
    }
}
```

---

## 📊 Monitoramento e Estatísticas

### Tela de Estatísticas Offline

```java
public class OfflineStatisticsDialog extends JDialog {
    
    private OfflineManager offlineManager;
    private JTextArea txtStats;
    
    public OfflineStatisticsDialog(Frame parent) {
        super(parent, "Estatísticas Offline", true);
        this.offlineManager = OfflineManager.getInstance();
        
        initComponents();
        carregarEstatisticas();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        
        // Área de texto
        txtStats = new JTextArea();
        txtStats.setEditable(false);
        txtStats.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(txtStats);
        add(scrollPane, BorderLayout.CENTER);
        
        // Botões
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> carregarEstatisticas());
        panelButtons.add(btnAtualizar);
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        panelButtons.add(btnFechar);
        
        add(panelButtons, BorderLayout.SOUTH);
    }
    
    private void carregarEstatisticas() {
        try {
            Map<String, Object> stats = offlineManager.getOfflineStatistics();
            
            StringBuilder sb = new StringBuilder();
            sb.append("=== ESTATÍSTICAS DO SISTEMA OFFLINE ===\n\n");
            
            // Estado atual
            sb.append("Estado: ").append(stats.get("current_state")).append("\n");
            sb.append("Modo Offline: ").append(
                (Boolean) stats.get("offline_mode_enabled") ? "Habilitado" : "Desabilitado"
            ).append("\n");
            sb.append("Conectado: ").append(
                (Boolean) stats.get("connected") ? "Sim" : "Não"
            ).append("\n\n");
            
            // Sincronização
            sb.append("=== SINCRONIZAÇÃO ===\n");
            sb.append("Auto Sync: ").append(
                (Boolean) stats.get("auto_sync_enabled") ? "Habilitado" : "Desabilitado"
            ).append("\n");
            sb.append("Intervalo: ").append(stats.get("sync_interval")).append(" minutos\n");
            sb.append("Operações Pendentes: ").append(stats.get("pending_operations")).append("\n");
            sb.append("Última Sync: ").append(
                stats.get("last_sync_time") != null ? stats.get("last_sync_time") : "Nunca"
            ).append("\n\n");
            
            // Banco de dados
            sb.append("=== BANCO DE DADOS LOCAL ===\n");
            sb.append("Patrimônios: ").append(stats.get("local_patrimonio_count")).append("\n");
            sb.append("Coletas: ").append(stats.get("local_coleta_count")).append("\n");
            sb.append("Inventários: ").append(stats.get("local_inventario_count")).append("\n");
            sb.append("Participantes: ").append(stats.get("local_participante_inventario_count")).append("\n\n");
            
            // Informações do banco
            if (stats.containsKey("database_info")) {
                sb.append(stats.get("database_info"));
            }
            
            txtStats.setText(sb.toString());
            txtStats.setCaretPosition(0);
            
        } catch (Exception e) {
            txtStats.setText("Erro ao carregar estatísticas:\n" + e.getMessage());
        }
    }
}
```

---

## ⚙️ Configurações Avançadas

### Configurar Intervalo de Sincronização

```java
// Sincronizar a cada 5 minutos
offlineManager.setSyncInterval(5);

// Sincronizar a cada 30 minutos (padrão)
offlineManager.setSyncInterval(30);

// Sincronizar a cada hora
offlineManager.setSyncInterval(60);
```

### Estratégia de Resolução de Conflitos

```java
// Servidor sempre ganha
offlineManager.setConflictResolution(
    DataSynchronizer.ConflictResolution.SERVER_WINS
);

// Cliente sempre ganha
offlineManager.setConflictResolution(
    DataSynchronizer.ConflictResolution.CLIENT_WINS
);

// Mais recente ganha
offlineManager.setConflictResolution(
    DataSynchronizer.ConflictResolution.LAST_WRITE_WINS
);

// Manual (pede ao usuário)
offlineManager.setConflictResolution(
    DataSynchronizer.ConflictResolution.MANUAL
);
```

---

## 🔧 Manutenção

### Limpeza de Dados Antigos

```java
// Limpar dados sincronizados antigos
offlineManager.cleanupOfflineData();
```

### Reiniciar Sistema Offline

```java
// Reiniciar completamente
offlineManager.restart();
```

### Desligar Sistema Offline

```java
// Ao fechar aplicação
offlineManager.shutdown();
```

---

## 📝 Checklist de Implementação

### Fase 1: Inicialização ✅
- [x] OfflineManager criado
- [x] SQLiteConnection configurado
- [x] Tabelas offline criadas
- [x] ConnectivityManager implementado

### Fase 2: Operações Básicas ✅
- [x] Salvar coletas offline
- [x] Listar coletas pendentes
- [x] Marcar como sincronizado

### Fase 3: Sincronização ⏳
- [ ] Implementar DataSynchronizer completo
- [ ] Testar sincronização automática
- [ ] Implementar resolução de conflitos
- [ ] Adicionar retry automático

### Fase 4: Interface ⏳
- [ ] Adicionar OfflineStatusPanel nas telas
- [ ] Criar tela de estatísticas
- [ ] Adicionar indicadores visuais
- [ ] Implementar notificações

### Fase 5: Testes 🔜
- [ ] Testar modo offline completo
- [ ] Testar sincronização automática
- [ ] Testar resolução de conflitos
- [ ] Testar recuperação de erros

---

## 🎯 Próximos Passos

1. **Implementar DataSynchronizer completo**
   - Sincronização de coletas
   - Sincronização de patrimônios
   - Resolução de conflitos

2. **Adicionar Interface Visual**
   - OfflineStatusPanel em todas as telas
   - Tela de estatísticas
   - Notificações de sincronização

3. **Testes Completos**
   - Cenários offline/online
   - Sincronização automática
   - Recuperação de erros

4. **Documentação**
   - Manual do usuário
   - Guia de troubleshooting
   - FAQ

---

**Status**: 🟡 Em Implementação  
**Última Atualização**: 20/11/2025  
**Versão**: 1.0.0
