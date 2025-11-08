# 📦 SIHCP Mobile - Distribuição

Esta pasta contém os builds de release do aplicativo SIHCP Mobile.

---

## 📱 Arquivos Disponíveis

### APK (Android Package)
```
SIHCP-Mobile-v1.2.0-release.apk (7.88 MB)
```
- **Uso**: Instalação direta em dispositivos Android
- **Ideal para**: Testes internos, distribuição corporativa
- **Instalação**: Habilitar "Fontes Desconhecidas" e instalar

### AAB (Android App Bundle)
```
SIHCP-Mobile-v1.2.0-release.aab (7.19 MB)
```
- **Uso**: Publicação na Google Play Store
- **Ideal para**: Distribuição pública
- **Vantagem**: APKs otimizados por dispositivo

---

## 🚀 Como Instalar

### Método 1: APK Direto (Recomendado para Testes)

1. **Transferir APK para o dispositivo**
   - Via USB
   - Via email
   - Via Google Drive/Dropbox
   - Via ADB: `adb install SIHCP-Mobile-v1.2.0-release.apk`

2. **Habilitar Fontes Desconhecidas**
   - Configurações → Segurança
   - Ativar "Fontes Desconhecidas" ou "Instalar apps desconhecidos"

3. **Instalar**
   - Abrir o arquivo APK
   - Tocar em "Instalar"
   - Aguardar conclusão

### Método 2: Google Play Store (Produção)

1. **Upload no Google Play Console**
   - Fazer login em https://play.google.com/console
   - Selecionar o app ou criar novo
   - Ir em "Versões" → "Produção"
   - Fazer upload do arquivo AAB

2. **Configurar Listagem**
   - Título: SIHCP Mobile
   - Descrição curta e completa
   - Screenshots (mínimo 2)
   - Ícone (512x512px)
   - Banner (1024x500px)

3. **Revisar e Publicar**
   - Preencher questionário de conteúdo
   - Definir classificação etária
   - Configurar preços (gratuito)
   - Enviar para revisão

---

## 🔐 Assinatura Digital

### Status Atual
⚠️ **APK NÃO ASSINADO** - Apenas para desenvolvimento/testes

### Para Produção

É **OBRIGATÓRIO** assinar o APK/AAB antes de distribuir publicamente.

#### Passo 1: Criar Keystore

```bash
keytool -genkey -v -keystore sihcp-release.keystore \
  -alias sihcp \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Informações necessárias:**
- Nome completo: Instituto Federal de Mato Grosso
- Unidade organizacional: TI
- Organização: IFMT
- Cidade: Cuiabá
- Estado: MT
- Código do país: BR

⚠️ **IMPORTANTE**: Guarde a keystore e senhas em local seguro! Se perder, não poderá atualizar o app na Play Store.

#### Passo 2: Configurar build.gradle

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../sihcp-release.keystore")
            storePassword "SUA_SENHA_AQUI"
            keyAlias "sihcp"
            keyPassword "SUA_SENHA_AQUI"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

#### Passo 3: Gerar Build Assinado

```bash
cd InventarioMobile
.\gradlew.bat assembleRelease
.\gradlew.bat bundleRelease
```

---

## 📊 Informações Técnicas

### Versão
- **Código**: 1.2.0
- **Build**: 120
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

### Tamanhos
- **APK**: ~7.88 MB
- **AAB**: ~7.19 MB
- **Download (Play Store)**: ~6-7 MB (otimizado por dispositivo)

### Permissões
- Câmera (scanner QR)
- Armazenamento (dados offline)
- Internet (sincronização)
- Localização (opcional)
- Microfone (busca por voz)
- Vibração (feedback)

---

## 🧪 Testes

### Checklist Pré-Distribuição

- [ ] Testar instalação em dispositivo físico
- [ ] Testar todas funcionalidades principais
- [ ] Verificar permissões
- [ ] Testar modo offline
- [ ] Testar sincronização
- [ ] Verificar performance
- [ ] Testar em diferentes versões do Android
- [ ] Verificar consumo de bateria
- [ ] Testar rotação de tela
- [ ] Verificar acessibilidade

### Dispositivos Recomendados para Teste

- **Mínimo**: Android 8.0, 2GB RAM
- **Recomendado**: Android 10+, 4GB RAM
- **Ideal**: Android 12+, 6GB+ RAM

---

## 📝 Changelog

### v1.2.0 (06/11/2025)
- ✨ Busca por voz com 8 comandos
- ✨ Navigation Drawer completo
- ✨ Pull-to-refresh no Dashboard
- ✨ Animações de entrada
- ✨ Cards clicáveis
- 🐛 Correções de bugs
- ⚡ Melhorias de performance

### v1.1.0 (Anterior)
- Dashboard com KPIs
- Scanner QR Code
- Modo offline
- Sincronização

---

## 🆘 Troubleshooting

### Erro: "App não instalado"
- Desinstalar versão anterior
- Limpar cache do instalador
- Verificar espaço disponível

### Erro: "Arquivo corrompido"
- Baixar novamente o APK
- Verificar integridade do arquivo
- Usar outro método de transferência

### Erro: "Permissões negadas"
- Ir em Configurações → Apps → SIHCP
- Conceder todas permissões necessárias

---

## 📞 Suporte

**Email**: suporte@ifmt.edu.br  
**Documentação**: `../DOCUMENTAÇÃO/`  
**Issues**: Reportar bugs e sugestões

---

## 📄 Licença

© 2025 Instituto Federal de Mato Grosso (IFMT)  
Todos os direitos reservados.

---

**Última atualização**: 06/11/2025
