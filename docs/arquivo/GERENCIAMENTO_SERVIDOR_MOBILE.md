# Gerenciamento do Servidor Mobile

## 📱 Visão Geral

O Sistema de Inventário agora possui um **Gerenciador de Servidor Mobile** integrado à interface desktop, permitindo que administradores controlem o servidor mobile diretamente pelo sistema.

## 🔐 Controle de Acesso

**IMPORTANTE:** O gerenciamento do servidor mobile é **exclusivo para administradores**.

- ✅ Usuários com perfil **ADMIN** têm acesso completo
- ❌ Outros perfis (COLETOR, CONSULTA) não visualizam esta opção

## 🎯 Como Acessar

1. Faça login no sistema com uma conta de **administrador**
2. No menu superior, clique em **Administração**
3. Selecione **Servidor Mobile**

## 🛠️ Funcionalidades

### 1. Visualizar Status
- Status atual do servidor (Rodando/Parado/Iniciando/Parando)
- Porta utilizada (8081)
- PID do processo
- URL da API
- Endereços de rede disponíveis

### 2. Iniciar Servidor
- Clique no botão **"Iniciar Servidor"**
- Aguarde a confirmação (pode levar alguns minutos)
- O servidor será iniciado automaticamente na porta 8081

### 3. Parar Servidor
- Clique no botão **"Parar Servidor"**
- Confirme a ação (isso desconectará todos os dispositivos móveis)
- O servidor será encerrado de forma segura

### 4. Reiniciar Servidor
- Clique no botão **"Reiniciar Servidor"**
- O servidor será parado e iniciado novamente
- Útil após alterações de configuração

### 5. Atualizar Status
- Clique no botão **"Atualizar Status"**
- Atualiza as informações exibidas em tempo real

## 📊 Informações Exibidas

### Status do Servidor
- ✓ **Rodando** (verde): Servidor ativo e aceitando conexões
- ✗ **Parado** (vermelho): Servidor não está rodando
- ⟳ **Iniciando** (laranja): Servidor está sendo iniciado
- ⟳ **Parando** (laranja): Servidor está sendo encerrado
- ⚠ **Erro** (vermelho): Ocorreu um erro

### Endereços de Rede
O painel exibe todos os endereços IP disponíveis para conexão:
```
Wi-Fi: http://192.168.1.100:8081/inventario/api/mobile
Ethernet: http://192.168.0.50:8081/inventario/api/mobile
```

## 📱 Configuração no Smartphone

Para conectar o aplicativo mobile ao servidor:

1. Anote o endereço IP exibido no painel
2. Abra o aplicativo mobile no smartphone
3. Vá em **Configurações**
4. Configure:
   - **IP do Servidor**: 192.168.1.100 (exemplo)
   - **Porta**: 8081
   - **Contexto**: /inventario

5. Teste a conexão

## 🔧 Scripts Auxiliares

### gerenciar-servidor.ps1
Script PowerShell para gerenciar o servidor via linha de comando:

```powershell
# Ver status
.\gerenciar-servidor.ps1 status

# Iniciar servidor
.\gerenciar-servidor.ps1 start

# Parar servidor
.\gerenciar-servidor.ps1 stop

# Reiniciar servidor
.\gerenciar-servidor.ps1 restart
```

### liberar-porta-8081.ps1
Script para liberar a porta 8081 caso esteja ocupada:

```powershell
.\liberar-porta-8081.ps1
```

## ⚠️ Solução de Problemas

### Erro: "Porta 8081 já está em uso"

**Solução 1: Usar o Gerenciador Integrado**
1. Abra o gerenciador no sistema desktop
2. Clique em "Parar Servidor"
3. Aguarde alguns segundos
4. Clique em "Iniciar Servidor"

**Solução 2: Usar Script de Liberação**
```powershell
.\liberar-porta-8081.ps1
```

**Solução 3: Manual**
```powershell
# Encontrar processo na porta 8081
netstat -ano | findstr :8081

# Encerrar processo (substitua PID pelo número encontrado)
taskkill /F /PID [PID]
```

### Servidor não inicia

1. Verifique se o Java está instalado:
   ```powershell
   java -version
   ```

2. Verifique se o Maven está instalado:
   ```powershell
   mvn -version
   ```

3. Verifique os logs em: `logs/sistema-inventario.log`

4. Certifique-se de que o banco de dados está acessível

### Smartphone não conecta

1. Verifique se o servidor está rodando (status verde)
2. Certifique-se de que smartphone e computador estão na mesma rede
3. Verifique se o firewall não está bloqueando a porta 8081
4. Teste a URL no navegador do smartphone:
   ```
   http://[IP_DO_SERVIDOR]:8081/inventario/api/mobile/health
   ```

## 🔒 Segurança

- O gerenciamento do servidor requer privilégios de administrador
- Apenas usuários autenticados como ADMIN podem acessar
- Todas as ações são registradas nos logs do sistema
- Recomenda-se usar HTTPS em produção

## 📝 Notas Importantes

1. **Backup antes de reiniciar**: Sempre faça backup antes de reiniciar o servidor em produção
2. **Horário de manutenção**: Prefira reiniciar o servidor fora do horário de uso intenso
3. **Notificar usuários**: Avise os usuários mobile antes de parar o servidor
4. **Monitoramento**: Acompanhe os logs durante e após operações no servidor

## 🆘 Suporte

Em caso de problemas:
1. Verifique os logs do sistema
2. Consulte a documentação técnica
3. Entre em contato com o suporte técnico

---

**Versão:** 1.2.0  
**Última atualização:** 2025  
**Sistema de Inventário - IFMT**
