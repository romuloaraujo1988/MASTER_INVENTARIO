# Scripts para Gerenciar a Porta 8081

Este conjunto de scripts ajuda a verificar e encerrar processos que estão usando a porta 8081.

## Scripts Disponíveis

### 1. `kill-port-8081.ps1` (PowerShell - Interativo)
Script PowerShell completo que mostra informações detalhadas e pede confirmação antes de encerrar processos.

**Uso:**
```powershell
.\kill-port-8081.ps1
```

**Características:**
- ✅ Mostra todos os processos usando a porta 8081
- ✅ Exibe informações detalhadas (PID, nome, caminho)
- ✅ Pede confirmação antes de encerrar
- ✅ Verifica se a porta foi liberada após encerrar

---

### 2. `kill-port-8081-force.ps1` (PowerShell - Automático)
Script PowerShell que encerra automaticamente os processos sem pedir confirmação.

**Uso:**
```powershell
.\kill-port-8081-force.ps1
```

**Características:**
- ⚡ Encerra automaticamente sem confirmação
- ⚡ Mais rápido para uso frequente
- ✅ Verifica se a porta foi liberada

---

### 3. `kill-port-8081.bat` (Batch - Windows)
Script batch tradicional do Windows, compatível com qualquer versão.

**Uso:**
```cmd
kill-port-8081.bat
```

ou simplesmente clique duas vezes no arquivo.

**Características:**
- 🪟 Funciona em qualquer Windows
- ✅ Pede confirmação antes de encerrar
- 📝 Interface simples em linha de comando

---

## Verificar Manualmente a Porta 8081

### PowerShell:
```powershell
netstat -ano | findstr :8081
```

### CMD:
```cmd
netstat -ano | findstr :8081
```

### Identificar o Processo:
```powershell
Get-Process -Id <PID>
```

### Encerrar Manualmente:
```powershell
Stop-Process -Id <PID> -Force
```

ou

```cmd
taskkill /F /PID <PID>
```

---

## Problemas Comuns

### "Não é possível executar scripts neste sistema"
Se você receber um erro de política de execução ao tentar executar scripts PowerShell:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Permissões Insuficientes
Se não conseguir encerrar o processo, execute o PowerShell ou CMD como Administrador:
- Clique com botão direito no PowerShell/CMD
- Selecione "Executar como administrador"

---

## Processo Atual na Porta 8081

Atualmente, o processo **PID 10972** está usando a porta 8081.

Para verificar qual aplicação é:
```powershell
Get-Process -Id 10972 | Select-Object ProcessName, Path
```

---

## Quando Usar

Use estes scripts quando:
- ❌ O servidor Spring Boot não consegue iniciar (porta em uso)
- ❌ Você recebe erro "Address already in use: bind"
- ❌ Precisa reiniciar o servidor mas a porta está ocupada
- ❌ Um processo travou e não liberou a porta

---

## Notas Importantes

⚠️ **Atenção:** Encerrar processos forçadamente pode causar perda de dados não salvos.

💡 **Dica:** Use o script interativo (`kill-port-8081.ps1`) para ver qual processo está usando a porta antes de encerrá-lo.

🔒 **Segurança:** Sempre verifique qual processo você está encerrando para evitar fechar aplicações importantes do sistema.
