# 🚀 Implementação Prática - Sincronização Offline/Online

## ✅ Sistema Já Implementado

Boa notícia! O sistema de sincronização offline já está **90% implementado** no projeto. Vou mostrar como usar:

---

## 📦 Componentes Existentes

### 1. OfflineManager ✅
- Gerenciador central do modo offline
- Detecta conectividade automaticamente
- Coordena sincronização

### 2. ColetaOfflineService ✅
- Salva coletas offline/online automaticamente
- Sincroniza coletas pendentes
- Verifica duplicatas

### 3. OfflineDAO ✅
- Operações CRUD no SQLite
- Controle de sincronização
- Metadados e estatísticas

### 4. DataSynchronizer ✅
- Sincronização automática
- Upload/Download de dados
- Resolução de conflitos

### 5. ConnectivityManager ✅
- Monitora conexão de rede
- Notifica mudanças de estado
- Testa conectividade

---

## 🎯 Como Usar - Passo a Passo

### Passo 1: Inicializar no Main

Adicione no `SistemaInventarioApplication.java`:

```java
package com.inventario;

import com.inventario.offline.OfflineManager;
import com.inventario.view.JLogin;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SistemaInventarioApplication {
    
    public static void main(String[] args) {
        try {
            // Configurar Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // ===== INICIALIZAR SISTEMA OFFLINE =====
            System.out.println("Inicializando sistema offline...");
            OfflineManager offlineManager = OfflineManager.getInstance();
            
            try {
                offlineManager.initialize();
                offlineManager.enableOfflineMode();
                System.out.println("✅ Sistema offline inicializado: " + 
                    offlineManager.getCurrentState());
            } catch (Exception e) {
                System.err.println("⚠️ Aviso: Sistema offline não inicializado - " + 
                    e.getMessage());
                System.err.println("   O sistema funcionará apenas em modo online.");
            }
            // ========================================
            
            // Iniciar interface
            SwingUtilities.invokeLater(() -> {
                JLogin login = new JLogin();
                login.setVisible(true);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
```

### Passo 2: Adicionar Painel de Status nas Telas

Crie o arquivo `OfflineStatusPanel.java`:

```java
package com.inventario.view;

import com.inventario.offline.OfflineManager;
import com.inventario.offline.ColetaOfflineService;
import com.inventario.offline.DataSynchronizer;
import javax.swing.*;
import java.awt.*;

public class OfflineStatusPanel extends JPanel {
    
    private JLabel lblStatus;
    private JLabel lblPendentes;
    private JButton btnSincronizar;
    
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
        setPreferredSize(new Dimension(0, 40));
        
        // Label de status
        lblStatus = new JLabel();
        lblStatus.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblStatus);
        
        // Label de pendentes
        lblPendentes = new JLabel();
        add(lblPendentes);
        
        // Botão sincronizar
        btnSincronizar = new JButton("Sincronizar");
        btnSincronizar.setFocusable(false);
        btnSincronizar.addActionListener(e -> sincronizar());
        add(btnSincronizar);
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
                break;
                
            case OFFLINE:
                lblStatus.setText("● OFFLINE");
                lblStatus.setForeground(new Color(231, 76, 60)); // Vermelho
                btnSincronizar.setEnabled(false);
                break;
                
            case SYNCING:
                lblStatus.setText("● SINCRONIZANDO...");
                lblStatus.setForeground(new Color(52, 152, 219)); // Azul
                btnSincronizar.setEnabled(false);
                break;
                
            case ERROR:
                lblStatus.setText("● ERRO");
                lblStatus.setForeground(new Color(231, 76, 60)); // Vermelho
                btnSincronizar.setEnabled(false);
                break;
                
            default:
                lblStatus.setText("● INICIALIZANDO...");
                lblStatus.setForeground(Color.GRAY);
                btnSincronizar.setEnabled(false);
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
                            "Sincronização concluída com sucesso!",
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
}
```

### Passo 3: Adicionar Painel no ColetaFrame_v2

Modifique o `ColetaFrame_v2.java`:

```java
// No início da classe, adicione:
private OfflineStatusPanel offlineStatusPanel;

// No método initComponents(), adicione:
private void initComponents() {
    setLayout(new BorderLayout());
    
    // ===== ADICIONAR PAINEL DE STATUS OFFLINE =====
    offlineStatusPanel = new OfflineStatusPanel();
    add(offlineStatusPanel, BorderLayout.NORTH);
    // ==============================================
    
    // Resto dos componentes...
    JPanel mainPanel = new JPanel();
    // ...
    add(mainPanel, BorderLayout.CENTER);
}
```

### Passo 4: Usar ColetaOfflineService para Salvar

No `ColetaFrame_v2.java`, modifique o método de salvar coleta:

```java
private void salvarColeta() {
    try {
        // Criar objeto Coleta
        Coleta coleta = new Coleta();
        coleta.setIdInventario(inventarioAtual.getId());
        coleta.setIdPatrimonio(patrimonioSelecionado.getId());
        coleta.setNumeroPatrimonio(patrimonioSelecionado.getNumero());
        coleta.setIdParticipanteInventario(participanteAtual.getId());
        coleta.setLocalizacaoEncontrada(txtLocalizacao.getText());
        coleta.setEstadoEncontrado((String) cmbEstado.getSelectedItem());
        coleta.setObservacaoColeta(txtObservacoes.getText());
        coleta.setDataColeta(new Timestamp(System.currentTimeMillis()));
        
        // ===== USAR SERVIÇO OFFLINE =====
        ColetaOfflineService coletaOfflineService = ColetaOfflineService.getInstance();
        int idColeta = coletaOfflineService.salvarColeta(coleta);
        // ================================
        
        if (idColeta > 0) {
            OfflineManager offlineManager = OfflineManager.getInstance();
            String mensagem = "Coleta salva com sucesso!";
            
            if (offlineManager.isOperatingOffline()) {
                mensagem += "\n\n⚠️ Sistema está OFFLINE\n" +
                           "A coleta será sincronizada quando a conexão for restabelecida.";
            } else {
                mensagem += "\n\n✅ Sincronizada com o servidor.";
            }
            
            JOptionPane.showMessageDialog(this, 
                mensagem,
                "Sucesso", 
                JOptionPane.INFORMATION_MESSAGE);
            
            limparFormulario();
        }
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Erro ao salvar coleta: " + e.getMessage(),
            "Erro",
            JOptionPane.ERROR_MESSAGE);
    }
}
```

---

## 🎨 Resultado Visual

Após implementar, você terá:

```
┌─────────────────────────────────────────────────────────┐
│ ● ONLINE  (0 pendentes)  [Sincronizar]                  │ ← Painel de Status
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Coleta de Patrimônios                                  │
│                                                          │
│  Número Patrimônio: [____________]  [Buscar]            │
│  Descrição: Cadeira Giratória                           │
│  Localização: [____________]                            │
│  Estado: [Bom ▼]                                        │
│  Observações: [_________________________]               │
│                                                          │
│                    [Salvar Coleta]                      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

Quando offline:

```
┌─────────────────────────────────────────────────────────┐
│ ● OFFLINE  (5 pendentes)  [Sincronizar]                 │ ← Status Vermelho
├─────────────────────────────────────────────────────────┤
│  ...                                                     │
└─────────────────────────────────────────────────────────┘
```

Quando sincronizando:

```
┌─────────────────────────────────────────────────────────┐
│ ● SINCRONIZANDO...  (5 pendentes)  [Sincronizar]        │ ← Status Azul
├─────────────────────────────────────────────────────────┤
│  ...                                                     │
└─────────────────────────────────────────────────────────┘
```

---

## 🧪 Como Testar

### Teste 1: Modo Offline

1. **Desconecte o banco de dados** (pare o PostgreSQL)
2. **Abra o sistema** - deve mostrar "● OFFLINE"
3. **Faça uma coleta** - deve salvar localmente
4. **Verifique** - deve mostrar "(1 pendentes)"

### Teste 2: Sincronização

1. **Reconecte o banco de dados** (inicie o PostgreSQL)
2. **Clique em "Sincronizar"** - deve mudar para "● SINCRONIZANDO..."
3. **Aguarde** - deve mudar para "● ONLINE (0 pendentes)"
4. **Verifique no banco** - coleta deve estar no PostgreSQL

### Teste 3: Automático

1. **Faça coletas offline**
2. **Reconecte o banco**
3. **Aguarde 5 minutos** - sincronização automática
4. **Verifique** - pendentes devem zerar automaticamente

---

## ⚙️ Configurações

### Alterar Intervalo de Sincronização

```java
// No main() ou em configurações
OfflineManager offlineManager = OfflineManager.getInstance();

// Sincronizar a cada 1 minuto (para testes)
offlineManager.setSyncInterval(1);

// Sincronizar a cada 10 minutos
offlineManager.setSyncInterval(10);

// Sincronizar a cada 30 minutos (padrão)
offlineManager.setSyncInterval(30);
```

### Forçar Modo Offline

```java
// Forçar offline manualmente (para testes)
offlineManager.forceOfflineMode();

// Tentar reconectar
boolean sucesso = offlineManager.tryReconnect();
```

### Ver Estatísticas

```java
Map<String, Object> stats = offlineManager.getOfflineStatistics();

System.out.println("Estado: " + stats.get("current_state"));
System.out.println("Pendentes: " + stats.get("pending_operations"));
System.out.println("Última Sync: " + stats.get("last_sync_time"));
```

---

## 📊 Monitoramento

### Logs do Sistema

O sistema gera logs automáticos:

```
INFO: Inicializando sistema offline
INFO: Sistema iniciado em modo ONLINE - banco e rede conectados
INFO: Salvando coleta online
INFO: Coleta salva online com sucesso - ID: 123
INFO: Conexão perdida - mudando para modo offline
INFO: Salvando coleta offline
INFO: Coleta salva offline com sucesso - ID local: 1
INFO: Conexão estabelecida - mudando para modo online
INFO: Iniciando sincronização manual
INFO: Sincronização concluída: 1 enviados, 0 recebidos
```

### Verificar Banco SQLite

O banco offline fica em: `~/.inventario/offline.db`

```sql
-- Ver coletas pendentes
SELECT * FROM coleta_offline WHERE sincronizado = 0;

-- Ver estatísticas
SELECT COUNT(*) FROM coleta_offline;
SELECT COUNT(*) FROM coleta_offline WHERE sincronizado = 1;
```

---

## 🐛 Troubleshooting

### Problema: "Sistema está offline" mas banco está conectado

**Solução:**
```java
// Forçar verificação de conectividade
ConnectivityManager connManager = ConnectivityManager.getInstance();
boolean online = connManager.forceCheck();
System.out.println("Conectado: " + online);
```

### Problema: Coletas não sincronizam

**Verificar:**
1. Sistema está online? `offlineManager.getCurrentState()`
2. Há pendentes? `coletaOfflineService.getQuantidadeColetasPendentes()`
3. Sincronização automática está ativa? `offlineManager.isOfflineModeEnabled()`

**Forçar sincronização:**
```java
DataSynchronizer.SyncResult result = offlineManager.executarSincronizacaoManual();
System.out.println("Sucesso: " + result.success);
System.out.println("Erro: " + result.errorMessage);
```

### Problema: Erro ao inicializar SQLite

**Solução:**
```java
// Verificar se diretório existe
File offlineDir = new File(System.getProperty("user.home"), ".inventario");
if (!offlineDir.exists()) {
    offlineDir.mkdirs();
}

// Reiniciar sistema offline
offlineManager.restart();
```

---

## ✅ Checklist de Implementação

- [ ] Adicionar inicialização no `main()`
- [ ] Criar `OfflineStatusPanel.java`
- [ ] Adicionar painel no `ColetaFrame_v2`
- [ ] Modificar método `salvarColeta()` para usar `ColetaOfflineService`
- [ ] Testar modo offline
- [ ] Testar sincronização manual
- [ ] Testar sincronização automática
- [ ] Adicionar painel em outras telas (opcional)

---

## 🎉 Pronto!

Após seguir estes passos, seu sistema terá:

✅ Sincronização automática offline/online  
✅ Interface visual de status  
✅ Salvamento automático local quando offline  
✅ Sincronização automática quando reconectar  
✅ Botão manual de sincronização  
✅ Contador de itens pendentes  
✅ Logs detalhados  

---

**Status**: ✅ Pronto para Implementar  
**Tempo Estimado**: 30 minutos  
**Dificuldade**: Fácil (código pronto para copiar/colar)
