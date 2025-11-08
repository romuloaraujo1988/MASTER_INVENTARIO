# Comandos Úteis - Desenvolvimento Android

## Compilação

### Compilar APK Debug
```powershell
.\gradlew.bat assembleDebug
```

### Compilar APK Release
```powershell
.\gradlew.bat assembleRelease
```

### Limpar e Recompilar
```powershell
.\gradlew.bat clean assembleDebug
```

### Verificar Dependências
```powershell
.\gradlew.bat dependencies
```

## Instalação

### Instalar APK no Dispositivo
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Desinstalar App
```powershell
adb uninstall com.inventario.mobile
```

### Reinstalar (desinstala e instala)
```powershell
adb uninstall com.inventario.mobile
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Logs e Debug

### Ver Logs do App
```powershell
adb logcat -s InventarioApp:V
```

### Ver Logs de Rede (OkHttp)
```powershell
adb logcat -s OkHttp:V
```

### Ver Todos os Logs do App
```powershell
adb logcat | Select-String "com.inventario.mobile"
```

### Salvar Logs em Arquivo
```powershell
adb logcat > logs_android.txt
```

### Limpar Logs
```powershell
adb logcat -c
```

### Ver Logs em Tempo Real com Filtro
```powershell
adb logcat | Select-String "InventarioApp|Retrofit|OkHttp|NetworkModule"
```

## Dispositivos

### Listar Dispositivos Conectados
```powershell
adb devices
```

### Conectar via WiFi (após conectar USB uma vez)
```powershell
# 1. Conecte via USB primeiro
adb tcpip 5555

# 2. Desconecte o USB e conecte via WiFi
adb connect 192.168.1.XXX:5555

# 3. Verifique a conexão
adb devices
```

### Reiniciar ADB
```powershell
adb kill-server
adb start-server
```

## Dados do App

### Limpar Dados do App
```powershell
adb shell pm clear com.inventario.mobile
```

### Ver SharedPreferences
```powershell
adb shell run-as com.inventario.mobile cat /data/data/com.inventario.mobile/shared_prefs/inventario_prefs.xml
```

### Ver Banco de Dados
```powershell
adb shell run-as com.inventario.mobile ls /data/data/com.inventario.mobile/databases/
```

### Exportar Banco de Dados
```powershell
adb shell run-as com.inventario.mobile cp /data/data/com.inventario.mobile/databases/inventario_database ./
adb pull /data/data/com.inventario.mobile/inventario_database .
```

## Testes

### Executar Testes Unitários
```powershell
.\gradlew.bat test
```

### Executar Testes Instrumentados
```powershell
.\gradlew.bat connectedAndroidTest
```

### Ver Relatório de Testes
```powershell
start app\build\reports\tests\testDebugUnitTest\index.html
```

## Rede e Conectividade

### Testar Conectividade com Servidor
```powershell
.\test-server-connection.ps1 -ServerIP "192.168.1.100"
```

### Verificar IP do Dispositivo
```powershell
adb shell ip addr show wlan0
```

### Testar Ping do Dispositivo para Servidor
```powershell
adb shell ping -c 4 192.168.1.100
```

### Verificar Portas Abertas no Servidor
```powershell
Test-NetConnection -ComputerName 192.168.1.100 -Port 8081
```

## Gradle

### Atualizar Dependências
```powershell
.\gradlew.bat --refresh-dependencies
```

### Ver Tarefas Disponíveis
```powershell
.\gradlew.bat tasks
```

### Build com Logs Detalhados
```powershell
.\gradlew.bat assembleDebug --info
```

### Build com Stack Trace
```powershell
.\gradlew.bat assembleDebug --stacktrace
```

## APK

### Ver Informações do APK
```powershell
aapt dump badging app\build\outputs\apk\debug\app-debug.apk
```

### Ver Tamanho do APK
```powershell
Get-Item app\build\outputs\apk\debug\app-debug.apk | Select-Object Name, @{Name="Size(MB)";Expression={[math]::Round($_.Length/1MB,2)}}
```

### Analisar APK
```powershell
# No Android Studio: Build > Analyze APK
# Ou use o APK Analyzer online
```

## Emulador

### Listar Emuladores Disponíveis
```powershell
emulator -list-avds
```

### Iniciar Emulador
```powershell
emulator -avd Pixel_5_API_30
```

### Iniciar Emulador sem Áudio
```powershell
emulator -avd Pixel_5_API_30 -no-audio
```

## Permissões

### Conceder Permissão de Câmera
```powershell
adb shell pm grant com.inventario.mobile android.permission.CAMERA
```

### Conceder Permissão de Localização
```powershell
adb shell pm grant com.inventario.mobile android.permission.ACCESS_FINE_LOCATION
```

### Ver Permissões do App
```powershell
adb shell dumpsys package com.inventario.mobile | Select-String "permission"
```

## Performance

### Ver Uso de Memória
```powershell
adb shell dumpsys meminfo com.inventario.mobile
```

### Ver Uso de CPU
```powershell
adb shell top -n 1 | Select-String "com.inventario.mobile"
```

### Capturar Trace de Performance
```powershell
adb shell am profile start com.inventario.mobile /sdcard/profile.trace
# Use o app
adb shell am profile stop com.inventario.mobile
adb pull /sdcard/profile.trace .
```

## Screenshots e Gravação

### Capturar Screenshot
```powershell
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png .
```

### Gravar Tela
```powershell
# Iniciar gravação
adb shell screenrecord /sdcard/demo.mp4

# Parar com Ctrl+C após alguns segundos

# Baixar vídeo
adb pull /sdcard/demo.mp4 .
```

## Troubleshooting

### App Não Instala
```powershell
# Verificar se há versão antiga
adb shell pm list packages | Select-String "inventario"

# Desinstalar completamente
adb uninstall com.inventario.mobile

# Reinstalar
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Erro de Assinatura
```powershell
# Limpar build
.\gradlew.bat clean

# Recompilar
.\gradlew.bat assembleDebug
```

### Gradle Lento
```powershell
# Adicionar ao gradle.properties:
# org.gradle.daemon=true
# org.gradle.parallel=true
# org.gradle.caching=true
```

## Atalhos Úteis

### Build, Instalar e Executar
```powershell
.\gradlew.bat assembleDebug ; adb install -r app\build\outputs\apk\debug\app-debug.apk ; adb shell am start -n com.inventario.mobile/.ui.splash.SplashActivity
```

### Limpar, Build e Ver Logs
```powershell
.\gradlew.bat clean assembleDebug ; adb install -r app\build\outputs\apk\debug\app-debug.apk ; adb logcat -c ; adb logcat -s InventarioApp:V
```

### Teste Completo
```powershell
# 1. Testar servidor
.\test-server-connection.ps1 -ServerIP "192.168.1.100"

# 2. Compilar
.\gradlew.bat clean assembleDebug

# 3. Instalar
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 4. Ver logs
adb logcat -c ; adb logcat | Select-String "InventarioApp|OkHttp"
```

## Variáveis de Ambiente Úteis

```powershell
# Adicionar ao PATH (se necessário)
$env:ANDROID_HOME = "C:\Users\SEU_USUARIO\AppData\Local\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
$env:PATH += ";$env:ANDROID_HOME\tools"
```

## Dicas

1. **Use `-r` no `adb install`** para reinstalar sem desinstalar (mantém dados)
2. **Use `Select-String` no PowerShell** em vez de `grep`
3. **Limpe o logcat antes de testar** com `adb logcat -c`
4. **Use tags específicas** para filtrar logs: `-s TAG:LEVEL`
5. **Salve logs importantes** em arquivos para análise posterior
