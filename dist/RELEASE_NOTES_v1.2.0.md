# 📱 SIHCP Mobile - Release v1.2.0

**Data de Build**: 06/11/2025  
**Versão**: 1.2.0  
**Build Type**: Release

---

## 📦 Arquivos Gerados

### APK (Android Package)
- **Arquivo**: `SIHCP-Mobile-v1.2.0-release.apk`
- **Tamanho**: 7.88 MB
- **Uso**: Instalação direta em dispositivos Android
- **Compatibilidade**: Android 8.0+ (API 26+)

### AAB (Android App Bundle)
- **Arquivo**: `SIHCP-Mobile-v1.2.0-release.aab`
- **Tamanho**: 7.19 MB
- **Uso**: Publicação na Google Play Store
- **Vantagem**: Download otimizado por dispositivo

---

## ✨ Novidades desta Versão

### 🎤 **Busca por Voz**
- Reconhecimento de comandos em português
- 8 comandos disponíveis:
  - "Buscar patrimônio [número]"
  - "Mostrar sala [número]"
  - "Listar divergências"
  - "Listar pendentes"
  - "Listar coletados"
  - "Sincronizar"
  - "Abrir scanner"
  - "Voltar"
- Interface animada de escuta
- Feedback visual em tempo real

### 📱 **Navigation Drawer (Menu Lateral)**
- Menu organizado em 4 seções
- Header personalizado com dados do usuário
- 12+ funcionalidades acessíveis
- Ícones intuitivos
- Animação suave de abertura/fechamento

### 🎨 **Melhorias de UX/UI**
- Pull-to-refresh no Dashboard
- Animações de entrada dos cards
- Números animados (contagem progressiva)
- Barra de progresso animada
- Cards clicáveis com ripple effect
- FAB (Floating Action Button) para busca por voz

### 📊 **Dashboard Aprimorado**
- KPIs visuais e interativos
- Gráfico de evolução de coletas (preparado)
- Estatísticas em tempo real
- Feedback visual de loading

### 🔄 **Sincronização**
- Indicador visual de sincronização
- Feedback de sucesso/erro
- Contador de itens pendentes

---

## 🛠️ Requisitos Técnicos

### Dispositivo
- **Android**: 8.0 (Oreo) ou superior
- **RAM**: Mínimo 2GB recomendado
- **Armazenamento**: 50MB livres
- **Câmera**: Necessária para scanner QR Code
- **Microfone**: Necessário para busca por voz
- **Internet**: Necessária para sincronização

### Permissões Necessárias
- ✅ Câmera (scanner QR Code)
- ✅ Armazenamento (dados offline)
- ✅ Internet (sincronização)
- ✅ Localização (opcional)
- ✅ Microfone (busca por voz)
- ✅ Vibração (feedback tátil)

---

## 📥 Instalação

### APK (Instalação Direta)
1. Baixe o arquivo `SIHCP-Mobile-v1.2.0-release.apk`
2. Habilite "Fontes Desconhecidas" nas configurações do Android
3. Abra o arquivo APK
4. Toque em "Instalar"
5. Aguarde a instalação
6. Abra o app e faça login

### AAB (Google Play Store)
1. Faça upload do arquivo `SIHCP-Mobile-v1.2.0-release.aab` no Google Play Console
2. Preencha as informações da listagem
3. Configure preços e distribuição
4. Envie para revisão
5. Aguarde aprovação (1-3 dias)

---

## 🔐 Assinatura do APK

⚠️ **IMPORTANTE**: Este APK está **NÃO ASSINADO** para fins de desenvolvimento.

Para produção, é necessário:
1. Criar uma keystore
2. Assinar o APK/AAB
3. Configurar ProGuard/R8

### Como Assinar (Produção)

```bash
# 1. Criar keystore (primeira vez)
keytool -genkey -v -keystore sihcp-release.keystore -alias sihcp -keyalg RSA -keysize 2048 -validity 10000

# 2. Configurar no build.gradle
signingConfigs {
    release {
        storeFile file("sihcp-release.keystore")
        storePassword "sua_senha"
        keyAlias "sihcp"
        keyPassword "sua_senha"
    }
}

# 3. Gerar APK assinado
.\gradlew.bat assembleRelease
```

---

## 🧪 Testes Realizados

### ✅ Funcionalidades Testadas
- [x] Login e autenticação
- [x] Scanner QR Code
- [x] Coleta de patrimônios
- [x] Modo offline
- [x] Sincronização
- [x] Dashboard e KPIs
- [x] Navigation Drawer
- [x] Busca por voz
- [x] Pull-to-refresh
- [x] Animações

### 📱 Dispositivos Testados
- Emulador Android 14 (API 34)
- Emulador Android 13 (API 33)

---

## 🐛 Problemas Conhecidos

### Limitações Atuais
1. **Gráfico de Evolução**: Código preparado mas temporariamente desabilitado (aguardando biblioteca MPAndroidChart)
2. **Relatórios**: Em desenvolvimento
3. **Fotos de Patrimônio**: Planejado para v1.3.0
4. **Notificações Push**: Planejado para v1.3.0

### Workarounds
- Gráfico: Será ativado na próxima atualização
- Relatórios: Use o sistema desktop temporariamente

---

## 📞 Suporte

### Contato
- **Email**: suporte@ifmt.edu.br
- **Telefone**: (65) XXXX-XXXX
- **Site**: https://ifmt.edu.br

### Documentação
- Manual do Usuário: `DOCUMENTAÇÃO/MANUAL_DO_USUARIO.md`
- Guia de Instalação: `DOCUMENTAÇÃO/INSTRUCOES_INSTALACAO.md`

---

## 🚀 Próximas Versões

### v1.3.0 (Planejado)
- 📸 Captura de fotos do patrimônio
- 🔔 Notificações push
- 🔊 Feedback sonoro
- 📊 Gráficos interativos
- 🏆 Gamificação

### v1.4.0 (Planejado)
- 🤖 IA para detecção de anomalias
- 🗺️ Mapa de calor do campus
- 📱 Widget para tela inicial
- 🌙 Modo escuro

---

## 📄 Licença

© 2025 Instituto Federal de Mato Grosso (IFMT)  
Todos os direitos reservados.

---

## 🎉 Agradecimentos

Desenvolvido com ❤️ para o IFMT

**Equipe de Desenvolvimento**:
- Backend: Java + Spring Boot
- Mobile: Kotlin + Android
- Design: Material Design 3
- IA: Kiro Assistant

---

**Build ID**: `release-1.2.0-20251106`  
**Git Commit**: `[hash do commit]`  
**Build Date**: 06/11/2025 13:15:54
