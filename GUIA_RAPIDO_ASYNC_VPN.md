# Guia Rápido - Operações Assíncronas para VPN

## 🎯 Problema Resolvido

Operações longas (carregamento de salas, busca de patrimônios) travavam a interface ao acessar banco via VPN.

## ✅ Solução: SwingWorker

Todas as operações de BD agora usam `SwingWorker` para executar em thread separada.

## 📋 Padrão de Implementação

### 1. Criar Método Wrapper (Síncrono)
```java
private void carregarSalas() {
    // Apenas chama o método assíncrono
    carregarSalasAsync();
}
```

### 2. Implementar Método Assíncrono
```java
private void carregarSalasAsync() {
    // 1. Desabilitar componentes
    comboSalas.setEnabled(false);
    
    // 2. Mostrar loading
    JDialog loadingDialog = criarDialogLoading("Título", "Mensagem");
    
    // 3. Criar SwingWorker
    SwingWorker<List<Sala>, String> worker = new SwingWorker<>() {
        @Override
        protected List<Sala> doInBackground() throws Exception {
            // Operação de BD aqui (thread separada)
            return salaInventarioDAO.buscarTodasSalasAtivas();
        }
        
        @Override
        protected void done() {
            // Fechar loading
            loadingDialog.dispose();
            
            try {
                List<Sala> salas = get(); // Pega resultado
                // Atualizar UI com resultado
                comboSalas.removeAllItems();
                for (Sala sala : salas) {
                    comboSalas.addItem(sala);
                }
                comboSalas.setEnabled(true);
            } catch (ExecutionException e) {
                // Tratar erro
                tratarErroCarregamento(e.getCause().getMessage());
            }
        }
    };
    
    // 4. Executar worker
    worker.execute();
    
    // 5. Mostrar dialog (bloqueia até fechar)
    loadingDialog.setVisible(true);
}
```

### 3. Criar Dialog de Loading
```java
private JDialog criarDialogLoading(String titulo, String mensagem) {
    JDialog dialog = new JDialog(this, titulo, true);
    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(350, 150);
    dialog.setLocationRelativeTo(this);
    dialog.setResizable(false);
    
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    
    JLabel lblMensagem = new JLabel("<html><center>" + 
        mensagem.replace("\n", "<br>") + "</center></html>");
    lblMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    
    panel.add(lblMensagem, BorderLayout.CENTER);
    panel.add(progressBar, BorderLayout.SOUTH);
    
    dialog.add(panel);
    return dialog;
}
```

### 4. Tratar Erros com Retry
```java
private void tratarErroCarregamento(String mensagemErro) {
    int opcao = JOptionPane.showOptionDialog(this,
        "Erro: " + mensagemErro + "\n\n" +
        "Isso pode ocorrer devido a conexão lenta via VPN.\n" +
        "Deseja tentar novamente?",
        "Erro de Conexão",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.ERROR_MESSAGE,
        null,
        new String[]{"Tentar Novamente", "Cancelar"},
        "Tentar Novamente");
    
    if (opcao == JOptionPane.YES_OPTION) {
        carregarSalasAsync(); // Retry
    }
}
```

## 🔄 Fluxo Completo

```
Usuário clica/digita
    ↓
Método wrapper (síncrono) é chamado
    ↓
Método assíncrono é chamado
    ↓
Dialog de loading aparece
    ↓
SwingWorker inicia em thread separada
    ↓
Operação de BD executa (sem travar UI)
    ↓
Dialog de loading fecha
    ↓
Resultado é processado na EDT
    ↓
UI é atualizada
    ↓
Se erro: oferece retry
```

## 📊 Componentes Implementados

| Componente | Localização | Descrição |
|-----------|------------|-----------|
| `carregarSalasAsync()` | Linha 2473 | Carrega salas com async |
| `buscarPatrimonioAsync()` | Linha 3302 | Busca patrimônio com async |
| `criarDialogLoading()` | Linha 2620 | Cria dialog de loading |
| `tratarErroCarregamento()` | Linha 2660 | Trata erro com retry |
| `tratarErroBusca()` | Linha 3380 | Trata erro de busca |
| `mostrarLoadingBusca()` | Linha 3360 | Mostra indicador de loading |

## 🧪 Testes Recomendados

### Teste 1: Sem Erro
```
1. Abrir ColetaFrame_v2
2. Observar dialog de loading
3. Verificar que combo foi preenchido
4. Verificar que interface não travou
```

### Teste 2: Com Erro (Simular)
```
1. Desconectar internet
2. Tentar carregar salas
3. Observar dialog de erro
4. Clicar "Tentar Novamente"
5. Reconectar internet
6. Verificar que funcionou
```

### Teste 3: VPN Lento
```
1. Conectar via VPN lenta
2. Abrir ColetaFrame_v2
3. Aguardar carregamento
4. Verificar que interface não travou
5. Verificar que dados foram carregados
```

## 💡 Dicas

### ✅ Fazer
- ✅ Usar SwingWorker para operações de BD
- ✅ Mostrar dialog de loading
- ✅ Oferecer retry em caso de erro
- ✅ Desabilitar componentes durante operação
- ✅ Usar `get()` para pegar resultado
- ✅ Tratar `ExecutionException` e `InterruptedException`

### ❌ Não Fazer
- ❌ Chamar BD diretamente na EDT
- ❌ Deixar interface sem feedback
- ❌ Não oferecer retry
- ❌ Deixar componentes habilitados durante operação
- ❌ Ignorar exceções
- ❌ Usar `Thread` diretamente (usar SwingWorker)

## 🚀 Próximas Operações a Converter

- [ ] `carregarItensPendentes()`
- [ ] `carregarHistoricoColeta()`
- [ ] `carregarDescricoes()`
- [ ] `sincronizarDados()`
- [ ] `exportarRelatorio()`

## 📚 Referências

- **SwingWorker**: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/worker.html
- **EDT**: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
- **Swing Threading**: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html

## 📞 Suporte

Se encontrar problemas:
1. Verificar logs em `logs/sistema-inventario.log`
2. Verificar conexão VPN
3. Verificar timeout do JDBC
4. Testar com internet direta (sem VPN)

---

**Versão**: 1.0.0  
**Data**: 17/12/2025  
**Status**: ✅ Pronto para uso

