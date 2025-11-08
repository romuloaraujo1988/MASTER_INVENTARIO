# Instruções de Instalação - SIHCP Mobile (Debug)

## Problema Resolvido

O erro ao acessar a coleta rápida com QR Code foi corrigido através da recompilação completa do aplicativo.

## Arquivos Gerados

- ✅ `SIHCP-Mobile-Debug.apk` (9.8 MB) - Aplicativo recompilado
- ✅ `instalar-app-debug.bat` - Script de instalação (Windows CMD)
- ✅ `instalar-app-debug.ps1` - Script de instalação (PowerShell)
- ✅ `SOLUCAO_ERRO_SCANNER_QRCODE.md` - Documentação detalhada

## Opções de Instalação

### Opção 1: Instalação Automática via USB (Recomendado)

**Pré-requisitos:**
- Smartphone conectado via USB
- Depuração USB ativada no smartphone
- ADB (Android Debug Bridge) instalado

**Passos:**

1. Conecte o smartphone ao computador via USB
2. No smartphone, ative a **Depuração USB**:
   - Configurações > Sobre o telefone
   - Toque 7 vezes em "Número da versão"
   - Volte e entre em "Opções do desenvolvedor"
   - Ative "Depuração USB"
3. Execute um dos scripts:
   - **CMD:** Clique duas vezes em `instalar-app-debug.bat`
   - **PowerShell:** Clique com botão direito em `instalar-app-debug.ps1` > Executar com PowerShell

### Opção 2: Instalação Manual

**Passos:**

1. Copie o arquivo `SIHCP-Mobile-Debug.apk` para o smartphone
   - Via cabo USB
   - Via Google Drive, Dropbox, etc.
   - Via WhatsApp (envie para você mesmo)

2. No smartphone:
   - Abra o gerenciador de arquivos
   - Localize o arquivo `SIHCP-Mobile-Debug.apk`
   - Toque no arquivo
   - Se solicitado, permita instalação de fontes desconhecidas
   - Toque em **Instalar**

### Opção 3: Via Android Studio

Se você tem o Android Studio instalado:

```bash
cd InventarioMobile
.\gradlew.bat installDebug
```

## Após a Instalação

### 1. Primeiro Acesso

1. Abra o aplicativo **SIHCP Mobile**
2. Faça login com suas credenciais
3. Aguarde o carregamento do dashboard

### 2. Testar Coleta Rápida com QR Code

1. No dashboard, toque em **Coleta Rápida (QR Code)**
2. Selecione uma sala da lista
3. **IMPORTANTE:** Quando solicitado, conceda permissão de câmera
4. A câmera deve abrir automaticamente
5. Posicione um QR Code de patrimônio na área de captura
6. O sistema deve:
   - Ler o QR Code
   - Buscar o patrimônio
   - Exibir informações do item
   - Permitir coletar (se ainda não coletado)

### 3. Verificar Permissões

Se a câmera não abrir:

1. Vá em **Configurações** do Android
2. **Aplicativos** > **SIHCP Mobile**
3. **Permissões**
4. Certifique-se que **Câmera** está **Permitida**

## Funcionalidades do Scanner

### O que o scanner faz:

- ✅ Solicita permissão de câmera automaticamente
- ✅ Abre a câmera para escanear QR Code
- ✅ Lê QR Codes de patrimônio
- ✅ Busca informações do patrimônio no banco de dados
- ✅ Verifica se o item já foi coletado
- ✅ Permite coletar o item diretamente
- ✅ Mostra contador de itens coletados
- ✅ Exibe informações detalhadas do patrimônio

### Informações Exibidas:

Após escanear um QR Code:

- Número do patrimônio
- Descrição do item
- Sala atual
- Status (coletado ou não)
- Botões de ação:
  - **Coletar** - Registra a coleta do item
  - **Tentar Novamente** - Escaneia outro QR Code
  - **Cancelar** - Volta para o dashboard

## Troubleshooting

### Erro: "Permissão de câmera negada"

**Solução:**
1. Configurações > Aplicativos > SIHCP Mobile > Permissões
2. Ative a permissão de **Câmera**
3. Volte ao app e tente novamente

### Erro: "Patrimônio não encontrado"

**Possíveis causas:**
- QR Code inválido ou corrompido
- Patrimônio não existe no banco de dados
- Problema de sincronização

**Solução:**
1. Verifique se o QR Code está legível
2. Tente sincronizar os dados (botão de sincronização no dashboard)
3. Verifique se o patrimônio existe no sistema desktop

### Erro: "Sala não selecionada"

**Solução:**
- Sempre selecione uma sala antes de escanear
- O fluxo correto é: Dashboard > Coleta Rápida > Selecionar Sala > Scanner

### Câmera não abre

**Soluções:**
1. Verifique permissões (veja acima)
2. Reinicie o aplicativo
3. Reinicie o smartphone
4. Desinstale e reinstale o app

### App trava ao abrir o scanner

**Solução:**
1. Desinstale completamente o app antigo
2. Instale o novo APK (`SIHCP-Mobile-Debug.apk`)
3. Limpe o cache: Configurações > Aplicativos > SIHCP Mobile > Armazenamento > Limpar cache

## Logs e Diagnóstico

Para desenvolvedores, verificar logs:

```bash
# Ver logs do scanner
adb logcat | findstr "ScannerActivity"

# Ver todos os logs do app
adb logcat | findstr "com.inventario.mobile"

# Limpar logs e ver novos
adb logcat -c
adb logcat
```

## Informações Técnicas

### Versão do App
- **Nome:** SIHCP Mobile
- **Package:** com.inventario.mobile.debug
- **Versão:** 1.1 (versionCode 2)
- **Build Type:** Debug
- **Tamanho:** 9.8 MB

### Dependências Principais
- ZXing 3.5.2 (leitura de QR Code)
- CameraX / ZXing Android Embedded 4.3.0
- Kotlin Coroutines
- Room Database
- Retrofit (API)

### Permissões Necessárias
- ✅ CAMERA - Para escanear QR Code
- ✅ INTERNET - Para comunicação com servidor
- ✅ ACCESS_NETWORK_STATE - Para verificar conectividade

## Suporte

Se o problema persistir após seguir todas as instruções:

1. Consulte `SOLUCAO_ERRO_SCANNER_QRCODE.md` para detalhes técnicos
2. Verifique os logs do aplicativo
3. Recompile o app com logs detalhados:
   ```bash
   cd InventarioMobile
   .\gradlew.bat clean assembleDebug --info
   ```

## Próximas Versões

Melhorias planejadas para o scanner:

- [ ] Suporte para múltiplos formatos de QR Code
- [ ] Scanner contínuo (sem fechar após cada leitura)
- [ ] Histórico de itens escaneados
- [ ] Modo offline completo
- [ ] Feedback visual melhorado
- [ ] Som de confirmação ao escanear

---

**Data:** 27/10/2025
**Autor:** Sistema de Inventário IFMT
**Versão do Documento:** 1.0
