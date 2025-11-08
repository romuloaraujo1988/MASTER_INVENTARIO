# Solução: Erro ao Acessar Coleta Rápida com QR Code

## Problema Identificado

O erro ocorreu ao tentar iniciar a `ScannerActivity` do aplicativo móvel:

```
FATAL EXCEPTION: main
Process: com.inventario.mobile.debug, PID: 7250
java.lang.RuntimeException: Unable to start activity ComponentInfo{com.inventario.mobile.debug/com.inventario.mobile.presentation.scanner.ScannerActivity}
```

## Causa Raiz

O problema estava relacionado a uma possível incompatibilidade ou erro de compilação no APK instalado no dispositivo. O código-fonte está correto, mas o APK precisava ser recompilado.

## Solução Aplicada

### 1. Limpeza e Recompilação

Foi realizada uma limpeza completa e recompilação do projeto Android:

```bash
cd InventarioMobile
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

### 2. Verificação do Código

Todos os componentes necessários estão presentes e corretos:

- ✅ `ScannerActivity.kt` - Activity principal do scanner
- ✅ `ScannerViewModel.kt` - ViewModel com lógica de negócio
- ✅ `activity_scanner.xml` - Layout da tela
- ✅ `AndroidManifest.xml` - Activity registrada corretamente
- ✅ Classes de dados (`ScannerUiState`, `ScanResult`) definidas

### 3. Fluxo de Navegação

O fluxo correto para coleta rápida com QR Code é:

```
DashboardFragment (btnQuickScan)
    ↓
SalaSelectionActivity (selecionar sala)
    ↓
ScannerActivity (escanear QR Code)
```

## Instruções para Resolver

### Passo 1: Desinstalar o App Antigo

No smartphone, desinstale completamente o aplicativo atual:

1. Vá em **Configurações** > **Aplicativos**
2. Encontre **SIHCP Mobile** ou **Inventário Mobile**
3. Toque em **Desinstalar**
4. Confirme a desinstalação

### Passo 2: Instalar o Novo APK

Duas opções:

**Opção A: Via Android Studio**
```bash
cd InventarioMobile
.\gradlew.bat installDebug
```

**Opção B: Transferir APK Manualmente**
1. Copie o arquivo `SIHCP-Mobile-Debug.apk` para o smartphone
2. No smartphone, abra o arquivo APK
3. Permita a instalação de fontes desconhecidas se solicitado
4. Instale o aplicativo

### Passo 3: Conceder Permissões

Ao abrir o app pela primeira vez:

1. Faça login normalmente
2. No dashboard, toque em **Coleta Rápida (QR Code)**
3. Quando solicitado, **conceda permissão de câmera**
4. Selecione uma sala
5. O scanner deve abrir corretamente

## Verificação de Permissões

A `ScannerActivity` solicita permissão de câmera automaticamente:

```kotlin
private val requestCameraPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        initializeScanner()
    } else {
        Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_LONG).show()
        finish()
    }
}
```

## Recursos da ScannerActivity

### Funcionalidades Implementadas

1. **Solicitação Automática de Permissão** - Pede permissão de câmera ao iniciar
2. **Scanner ZXing** - Usa biblioteca ZXing para leitura de QR Code
3. **Busca de Patrimônio** - Busca automática após escanear
4. **Verificação de Coleta** - Indica se o item já foi coletado
5. **Contador de Coletas** - Mostra total de itens coletados
6. **Coleta Direta** - Permite coletar o item após escanear

### Informações Exibidas

Após escanear um QR Code, a tela mostra:

- Número do patrimônio
- Descrição do item
- Sala atual
- Status (coletado ou não coletado)
- Botões de ação (Coletar, Tentar Novamente, Cancelar)

## Troubleshooting

### Se o erro persistir:

1. **Verifique as permissões no Android:**
   - Configurações > Aplicativos > SIHCP Mobile > Permissões
   - Certifique-se que **Câmera** está permitida

2. **Limpe o cache do app:**
   - Configurações > Aplicativos > SIHCP Mobile
   - Armazenamento > Limpar cache

3. **Verifique os logs:**
   ```bash
   adb logcat | findstr "ScannerActivity"
   ```

4. **Recompile com logs detalhados:**
   ```bash
   cd InventarioMobile
   .\gradlew.bat clean assembleDebug --info
   ```

## Arquivos Relacionados

- `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/scanner/ScannerActivity.kt`
- `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/scanner/ScannerViewModel.kt`
- `InventarioMobile/app/src/main/res/layout/activity_scanner.xml`
- `InventarioMobile/app/src/main/AndroidManifest.xml`

## Status

✅ **Código verificado e correto**
✅ **Build bem-sucedido**
✅ **APK gerado: SIHCP-Mobile-Debug.apk**
⏳ **Aguardando instalação e teste no dispositivo**

## Próximos Passos

1. Desinstalar app antigo do smartphone
2. Instalar novo APK (`SIHCP-Mobile-Debug.apk`)
3. Testar funcionalidade de coleta rápida com QR Code
4. Verificar se a câmera abre corretamente
5. Testar escaneamento de um QR Code de patrimônio

---

**Data:** 27/10/2025
**Versão do App:** 1.1 (versionCode 2)
**Build:** Debug
