# Teste do Mecanismo de Instância Única

## Como Testar

### Teste Manual

1. **Compile o projeto**:
   ```bash
   mvn clean compile
   ```

2. **Execute a primeira instância**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.inventario.view.JLogin"
   ```
   
   ✅ **Resultado esperado**: O sistema deve iniciar normalmente e exibir a tela de login.

3. **Em outro terminal, execute uma segunda instância**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.inventario.view.JLogin"
   ```
   
   ✅ **Resultado esperado**: 
   - Uma mensagem de aviso deve ser exibida
   - A segunda instância deve ser encerrada automaticamente
   - A primeira instância deve continuar rodando normalmente

### Teste com JAR

1. **Gere o JAR do projeto**:
   ```bash
   mvn clean package
   ```

2. **Execute a primeira instância**:
   ```bash
   java -jar target/inventario-1.0.jar
   ```

3. **Execute uma segunda instância** (em outro terminal ou clicando duas vezes no JAR):
   ```bash
   java -jar target/inventario-1.0.jar
   ```

### Verificação do Arquivo de Lock

Durante a execução, você pode verificar se o arquivo de lock foi criado:

**Windows (PowerShell)**:
```powershell
Get-ChildItem $env:TEMP | Where-Object { $_.Name -eq ".sihcp_instance.lock" }
```

**Linux/macOS**:
```bash
ls -la /tmp/.sihcp_instance.lock
```

## Cenários de Teste

### ✅ Cenário 1: Primeira Instância
- **Ação**: Iniciar o sistema
- **Esperado**: Sistema inicia normalmente
- **Verificação**: Arquivo `.sihcp_instance.lock` é criado no diretório temporário

### ✅ Cenário 2: Segunda Instância
- **Ação**: Tentar iniciar outra instância com a primeira ainda rodando
- **Esperado**: Mensagem de aviso é exibida e segunda instância não inicia
- **Verificação**: Apenas uma janela do sistema está aberta

### ✅ Cenário 3: Fechamento Normal
- **Ação**: Fechar o sistema normalmente
- **Esperado**: Sistema fecha e arquivo de lock é deletado
- **Verificação**: Arquivo `.sihcp_instance.lock` não existe mais

### ✅ Cenário 4: Reinicialização
- **Ação**: Fechar o sistema e iniciar novamente
- **Esperado**: Sistema inicia normalmente
- **Verificação**: Nova instância consegue adquirir o lock

### ⚠️ Cenário 5: Encerramento Anormal (Crash)
- **Ação**: Forçar encerramento do sistema (kill process)
- **Esperado**: Arquivo de lock pode permanecer
- **Solução**: Deletar manualmente o arquivo `.sihcp_instance.lock`

## Checklist de Testes

- [ ] Sistema inicia normalmente na primeira execução
- [ ] Arquivo de lock é criado no diretório temporário
- [ ] Segunda instância exibe mensagem de aviso
- [ ] Segunda instância não inicia
- [ ] Primeira instância continua funcionando
- [ ] Arquivo de lock é deletado ao fechar o sistema
- [ ] Sistema pode ser reiniciado após fechamento normal
- [ ] Mensagem de aviso é clara e informativa

## Resultados Esperados

| Teste | Resultado Esperado | Status |
|-------|-------------------|--------|
| Primeira instância | Inicia normalmente | ✅ |
| Segunda instância | Bloqueada com mensagem | ✅ |
| Arquivo de lock | Criado e deletado corretamente | ✅ |
| Reinicialização | Funciona normalmente | ✅ |

## Troubleshooting

### Problema: Sistema não inicia mesmo sem outra instância

**Diagnóstico**:
```bash
# Verificar se o arquivo de lock existe
ls -la /tmp/.sihcp_instance.lock  # Linux/macOS
dir %TEMP%\.sihcp_instance.lock   # Windows
```

**Solução**:
```bash
# Deletar o arquivo de lock manualmente
rm /tmp/.sihcp_instance.lock      # Linux/macOS
del %TEMP%\.sihcp_instance.lock   # Windows
```

### Problema: Múltiplas instâncias conseguem iniciar

**Possíveis causas**:
1. Permissões incorretas no diretório temporário
2. Sistema de arquivos não suporta file locking
3. Erro na implementação

**Verificação**:
- Verificar logs do sistema
- Verificar permissões do diretório temporário
- Testar em outro sistema operacional

---

**Data do teste**: _____/_____/_____  
**Testador**: _____________________  
**Resultado**: ☐ Aprovado ☐ Reprovado  
**Observações**: _____________________
