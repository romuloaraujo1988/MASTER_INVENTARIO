# Guia de Teste Rápido - Aplicativo Android

## Pré-requisitos

1. Servidor backend rodando na porta 8081
2. Android Studio instalado
3. Dispositivo Android ou emulador configurado

## Passo 1: Testar o Servidor

Execute o script de teste para verificar se o servidor está respondendo:

```powershell
cd InventarioMobile
.\test-server-connection.ps1 -ServerIP "SEU_IP_AQUI" -Username "admin" -Password "admin"
```

**Exemplo**:
```powershell
.\test-server-connection.ps1 -ServerIP "192.168.1.100" -Username "admin" -Password "admin"
```

Se o teste passar, você verá:
- ✓ Servidor está online
- ✓ Login bem-sucedido
- ✓ Token recebido
- ✓ Dados do usuário recebidos

## Passo 2: Compilar o APK

### Opção A: Via Android Studio

1. Abra o projeto `InventarioMobile` no Android Studio
2. Aguarde a sincronização do Gradle
3. Vá em `Build > Build Bundle(s) / APK(s) > Build APK(s)`
4. Aguarde a compilação
5. O APK estará em `app/build/outputs/apk/debug/app-debug.apk`

### Opção B: Via Linha de Comando

```powershell
cd InventarioMobile
.\gradlew.bat assembleDebug
```

O APK será gerado em: `app\build\outputs\apk\debug\app-debug.apk`

## Passo 3: Instalar no Dispositivo

### Via ADB (Android Debug Bridge)

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Via Android Studio

1. Conecte o dispositivo via USB (com depuração USB ativada)
2. Clique em `Run > Run 'app'`
3. Selecione o dispositivo

## Passo 4: Configurar e Testar

1. **Abra o aplicativo** no dispositivo

2. **Configure o IP do servidor**:
   - Na tela de login, insira o IP do servidor no campo "IP do Servidor"
   - Exemplo: `192.168.1.100`
   - O aplicativo construirá automaticamente: `http://192.168.1.100:8081/inventario/api/mobile/`

3. **Faça login**:
   - Username: `admin` (ou o usuário que você criou)
   - Senha: `admin` (ou a senha correspondente)
   - Clique em "Entrar"

4. **Verificar logs** (opcional):
   ```powershell
   adb logcat | Select-String "InventarioApp|Retrofit|OkHttp"
   ```

## Problemas Comuns e Soluções

### Erro: "Servidor não encontrado"

**Causa**: O dispositivo não consegue acessar o IP do servidor

**Soluções**:
1. Verifique se o dispositivo está na mesma rede que o servidor
2. Teste o ping do dispositivo para o servidor
3. Verifique o firewall do servidor
4. Se estiver usando emulador, use `10.0.2.2` em vez de `localhost`

### Erro: "Conexão recusada"

**Causa**: O servidor não está rodando ou a porta está bloqueada

**Soluções**:
1. Verifique se o servidor está rodando: `http://SEU_IP:8081/inventario`
2. Verifique o firewall do Windows:
   ```powershell
   netsh advfirewall firewall add rule name="Java 8081" dir=in action=allow protocol=TCP localport=8081
   ```

### Erro: "Credenciais inválidas"

**Causa**: Username ou senha incorretos

**Soluções**:
1. Verifique as credenciais no banco de dados
2. Crie um usuário admin se necessário:
   ```sql
   INSERT INTO usuario (login, senha, nome, email, ativo, perfil) 
   VALUES ('admin', 'admin', 'Administrador', 'admin@example.com', true, 'ADMINISTRADOR');
   ```

### Erro: "Endpoint não encontrado" (404)

**Causa**: A API mobile não está implementada no backend

**Soluções**:
1. Verifique se o `MobileAuthController` existe em `src/main/java/com/inventario/mobile/server/controller/`
2. Verifique se o servidor foi compilado com as classes mobile
3. Reinicie o servidor

## Verificar Logs Detalhados

### No Android (via ADB)

```powershell
# Logs gerais do app
adb logcat -s InventarioApp:V

# Logs de rede (Retrofit/OkHttp)
adb logcat -s OkHttp:V

# Todos os logs do app
adb logcat | Select-String "com.inventario.mobile"
```

### No Servidor (backend)

Verifique os logs do Spring Boot para ver as requisições recebidas:
```
logs/sistema-inventario.log
```

Procure por linhas como:
```
INFO  MobileAuthController - Tentativa de login mobile para usuário: admin
```

## Teste de Conectividade Manual

Use o PowerShell para testar manualmente:

```powershell
$ip = "192.168.1.100"
$body = @{
    username = "admin"
    password = "admin"
    deviceId = "TEST_001"
    appVersion = "1.0.0"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://${ip}:8081/inventario/api/mobile/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

Se funcionar, você verá uma resposta JSON com o token.

## Checklist de Verificação

- [ ] Servidor backend está rodando
- [ ] Porta 8081 está acessível
- [ ] Firewall permite conexões na porta 8081
- [ ] Dispositivo Android está na mesma rede
- [ ] APK foi compilado com as correções mais recentes
- [ ] IP do servidor está correto no app
- [ ] Credenciais de login estão corretas
- [ ] Logs não mostram erros de rede

## Próximos Testes

Após o login bem-sucedido, teste:

1. **Sincronização de dados**:
   - Verifique se os setores são carregados
   - Verifique se as salas são carregadas
   - Verifique se os patrimônios são carregados

2. **Scanner de QR Code**:
   - Teste a câmera
   - Escaneie um QR Code de patrimônio
   - Verifique se os dados são carregados

3. **Coleta de dados**:
   - Registre uma coleta
   - Verifique se é salva localmente
   - Sincronize com o servidor

## Suporte

Se os problemas persistirem:

1. Capture os logs completos:
   ```powershell
   adb logcat > logs_android.txt
   ```

2. Verifique os logs do servidor em `logs/sistema-inventario.log`

3. Teste a API manualmente com Postman ou curl

4. Verifique a documentação em `CORRECOES_CONECTIVIDADE.md`
