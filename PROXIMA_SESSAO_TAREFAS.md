# Próxima Sessão - Tarefas Prioritárias

## 🎯 Objetivo: Completar Fase 4 - Integração no MainFrame

---

## 📋 Tarefas Imediatas

### 1. Integrar StatusBarPanel no MainFrame ⭐ PRIORITÁRIO

**Localização:** `src/main/java/com/inventario/view/MainFrame.java`

**Passos:**
```java
// No construtor ou método de inicialização
private StatusBarPanel statusBar;

// Criar instância
statusBar = new StatusBarPanel();

// Adicionar no rodapé
getContentPane().add(statusBar, BorderLayout.SOUTH);
```

**Resultado Esperado:**
```
┌─────────────────────────────────────────────┐
│  [Menu Bar]                                  │
├─────────────────────────────────────────────┤
│                                              │
│  [Conteúdo Principal - Botões dos Módulos]  │
│                                              │
├─────────────────────────────────────────────┤
│  🟢 ONLINE | Última sync: 21/11 14:30       │
│                          [🔄 Sincronizar]    │
└─────────────────────────────────────────────┘
```

---

### 2. Implementar Bloqueio de Módulos em Modo Offline

**Arquivo:** `MainFrame.java`

**Código a Adicionar:**
```java
private OfflineModeManager offlineModeManager;

// No construtor
offlineModeManager = OfflineModeManager.getInstance();
offlineModeManager.addListener(new OfflineModeListener() {
    @Override
    public void onModoOfflineAtivado() {
        SwingUtilities.invokeLater(() -> {
            bloquearModulosOffline();
            statusBar.updateStatus();
        });
    }
    
    @Override
    public void onModoOnlineRestaurado() {
        SwingUtilities.invokeLater(() -> {
            habilitarTodosModulos();
            statusBar.updateStatus();
        });
    }
});

// Verificar modo ao iniciar
verificarModoOffline();
```

**Método de Bloqueio:**
```java
private void bloquearModulosOffline() {
    // Desabilitar botões de módulos não disponíveis
    btnPatrimonios.setEnabled(false);
    btnRelatorios.setEnabled(false);
    btnUsuarios.setEnabled(false);
    btnInventarios.setEnabled(false);
    // ... outros módulos
    
    // Manter apenas coleta habilitada
    btnColeta.setEnabled(true);
    
    // Mostrar mensagem
    JOptionPane.showMessageDialog(this,
        "Sistema em modo offline.\n" +
        "Apenas o módulo de coleta está disponível.",
        "Modo Offline",
        JOptionPane.INFORMATION_MESSAGE);
}

private void habilitarTodosModulos() {
    btnPatrimonios.setEnabled(true);
    btnRelatorios.setEnabled(true);
    btnUsuarios.setEnabled(true);
    btnInventarios.setEnabled(true);
    // ... outros módulos
}

private void verificarModoOffline() {
    if (offlineModeManager.isModoOffline()) {
        bloquearModulosOffline();
    }
}
```

---

### 3. Adicionar Menu "Ferramentas" com Importação

**Código:**
```java
// No método de criação de menus
JMenu menuFerramentas = new JMenu("Ferramentas");
menuFerramentas.setFont(new Font("Arial", Font.BOLD, 14));

JMenuItem itemImportarDados = new JMenuItem("📥 Importar Dados Offline");
itemImportarDados.setFont(new Font("Arial", Font.PLAIN, 13));
itemImportarDados.addActionListener(e -> abrirImportacaoDados());

JMenuItem itemForcarOffline = new JMenuItem("🟡 Forçar Modo Offline");
itemForcarOffline.setFont(new Font("Arial", Font.PLAIN, 13));
itemForcarOffline.addActionListener(e -> forcarModoOffline());

menuFerramentas.add(itemImportarDados);
menuFerramentas.addSeparator();
menuFerramentas.add(itemForcarOffline);

menuBar.add(menuFerramentas);
```

**Métodos:**
```java
private void abrirImportacaoDados() {
    if (offlineModeManager.isModoOffline()) {
        JOptionPane.showMessageDialog(this,
            "Não é possível importar dados em modo offline.\n" +
            "Conecte-se ao servidor primeiro.",
            "Modo Offline",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    ImportacaoDadosDialog dialog = new ImportacaoDadosDialog(this, usuarioLogado);
    dialog.setVisible(true);
    
    // Atualizar status após importação
    if (statusBar != null) {
        statusBar.updateStatus();
    }
}

private void forcarModoOffline() {
    if (!offlineModeManager.isDadosSincronizados()) {
        JOptionPane.showMessageDialog(this,
            "Não é possível forçar modo offline.\n" +
            "Você precisa importar dados primeiro.",
            "Dados Não Sincronizados",
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    int opcao = JOptionPane.showConfirmDialog(this,
        "Deseja forçar o modo offline?\n" +
        "O sistema usará apenas dados locais.",
        "Forçar Modo Offline",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    
    if (opcao == JOptionPane.YES_OPTION) {
        boolean sucesso = offlineModeManager.forcarModoOffline();
        if (sucesso) {
            JOptionPane.showMessageDialog(this,
                "Modo offline ativado.\n" +
                "Apenas o módulo de coleta está disponível.",
                "Modo Offline Ativado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
```

---

### 4. Adicionar Verificação ao Abrir Módulos

**Exemplo para PatrimonioFrame:**
```java
private void abrirPatrimonios() {
    if (!offlineModeManager.verificarAcessoModulo("PATRIMONIOS", this)) {
        return; // Módulo bloqueado
    }
    
    // Abrir normalmente
    PatrimonioFrame frame = new PatrimonioFrame(usuarioLogado);
    frame.setVisible(true);
}
```

**Aplicar para todos os módulos:**
- abrirRelatorios()
- abrirUsuarios()
- abrirInventarios()
- abrirSalas()
- abrirResponsaveis()
- etc.

---

## ✅ Checklist de Implementação

- [ ] Adicionar StatusBarPanel no rodapé do MainFrame
- [ ] Criar campo `offlineModeManager` no MainFrame
- [ ] Adicionar listener de mudança de estado
- [ ] Implementar método `bloquearModulosOffline()`
- [ ] Implementar método `habilitarTodosModulos()`
- [ ] Implementar método `verificarModoOffline()`
- [ ] Adicionar menu "Ferramentas"
- [ ] Adicionar item "Importar Dados Offline"
- [ ] Adicionar item "Forçar Modo Offline"
- [ ] Implementar método `abrirImportacaoDados()`
- [ ] Implementar método `forcarModoOffline()`
- [ ] Adicionar verificação em todos os métodos de abertura de módulos
- [ ] Testar mudança online → offline
- [ ] Testar mudança offline → online
- [ ] Testar bloqueio de módulos
- [ ] Testar importação de dados
- [ ] Testar forçar modo offline

---

## 🧪 Testes a Realizar

### Teste 1: Barra de Status
1. Iniciar sistema online
2. Verificar indicador 🟢 ONLINE
3. Verificar última sincronização
4. Clicar em "Sincronizar Agora"

### Teste 2: Forçar Modo Offline
1. Importar dados
2. Menu Ferramentas > Forçar Modo Offline
3. Verificar indicador 🟡 OFFLINE
4. Verificar módulos bloqueados
5. Verificar apenas COLETA disponível

### Teste 3: Bloqueio de Módulos
1. Entrar em modo offline
2. Tentar abrir Patrimônios
3. Verificar mensagem de bloqueio
4. Tentar abrir Relatórios
5. Verificar mensagem de bloqueio
6. Abrir Coleta
7. Verificar que funciona

### Teste 4: Reconexão
1. Sistema em modo offline
2. Reconectar ao servidor
3. Verificar indicador 🟢 ONLINE
4. Verificar módulos habilitados
5. Verificar mensagem de reconexão

---

## 📊 Progresso Esperado

**Antes:** 50% (Fases 1, 2 e 3 parcial)
**Depois:** 70% (Fases 1, 2, 3 e 4 completas)

---

## 🎯 Resultado Final Esperado

Ao completar estas tarefas, o sistema terá:

1. ✅ Barra de status funcional no rodapé
2. ✅ Indicadores visuais claros (🟢 🟡 🔄)
3. ✅ Bloqueio automático de módulos em offline
4. ✅ Menu de ferramentas com importação
5. ✅ Botão "Forçar Modo Offline"
6. ✅ Listeners de reconexão funcionando
7. ✅ UX clara e intuitiva

---

**Estimativa de Tempo:** 2-3 horas  
**Prioridade:** ALTA  
**Dependências:** Nenhuma (tudo já está criado)
