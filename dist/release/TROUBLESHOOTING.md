# Troubleshooting - Erro "Pacote Inválido"

## 🔴 Problema

Ao tentar instalar o APK, o Android exibe o erro:
```
Pacote inválido
```

## 🔍 Causas Possíveis

1. **APK Corrompido** - Arquivo danificado durante download
2. **Assinatura Inválida** - Certificado expirado ou inválido
3. **Incompatibilidade de Versão** - Android muito antigo
4. **Espaço em Disco** - Memória insuficiente no dispositivo
5. **Permissões** - Instalação de fontes desconhecidas desabilitada

## ✅ Soluções

### Solução 1: Verificar Compatibilidade

**Requisitos Mínimos:**
- Android 6.0 ou superior (API 23+)
- 50MB de espaço livre
- 2GB de RAM

**Como verificar:**
1. Abra Configurações
2. Vá para Sobre o Telefone
3. Procure por "Versão do Android"

### Solução 2: Habilitar Instalação de Fontes Desconhecidas

**Android 6.0 - 7.x:**
1. Configurações → Segurança
2. Ative "Fontes Desconhecidas"

**Android 8.0+:**
1. Configurações → Aplicativos e Notificações
2. Avançado → Acesso Especial
3. Instalar Aplicativos Desconhecidos
4. Selecione o navegador/gerenciador de arquivos
5. Ative "Permitir desta fonte"

### Solução 3: Limpar Cache e Dados

```bash
# Via ADB
adb shell pm clear com.android.packageinstaller
adb shell pm clear com.android.vending
```

### Solução 4: Reinstalar o APK

1. Desinstale a versão anterior (se existir)
2. Limpe o cache do gerenciador de pacotes
3. Baixe novamente o APK
4. Instale a nova versão

### Solução 5: Usar ADB para Instalar

```bash
# Conectar dispositivo via USB
adb devices

# Instalar APK
adb install SIHCP-Mobile-2.7.0-FIXED.apk

# Verificar instalação
adb shell pm list packages | grep inventario
```

### Solução 6: Verificar Integridade do APK

```bash
# Verificar assinatura
jarsigner -verify -verbose SIHCP-Mobile-2.7.0-FIXED.apk

# Verificar tamanho
ls -lh SIHCP-Mobile-2.7.0-FIXED.apk
```

## 📋 Checklist de Instalação

- [ ] Android 6.0 ou superior
- [ ] 50MB de espaço livre
- [ ] Fontes desconhecidas habilitadas
- [ ] APK baixado completamente
- [ ] APK não está corrompido
- [ ] Versão anterior desinstalada

## 🔧 Versões Disponíveis

| Arquivo | Tamanho | Status | Notas |
|---------|---------|--------|-------|
| SIHCP-Mobile-2.7.0.apk | 15.27 MB | ⚠️ Pode ter problema | Versão anterior |
| SIHCP-Mobile-2.7.0-FIXED.apk | 15.27 MB | ✅ Recomendado | Versão corrigida |

**Recomendação:** Use a versão **SIHCP-Mobile-2.7.0-FIXED.apk**

## 📞 Se o Problema Persistir

1. **Coletar Logs:**
   ```bash
   adb logcat > logcat.txt
   ```

2. **Verificar Espaço:**
   ```bash
   adb shell df -h
   ```

3. **Testar em Outro Dispositivo:**
   - Tente instalar em outro Android
   - Confirme se o problema é do dispositivo ou do APK

4. **Contatar Suporte:**
   - Email: suporte@inventario.com
   - Anexe os logs e informações do dispositivo

## 🔐 Verificação de Segurança

O APK foi verificado e está seguro:

```
✅ Assinado com certificado de produção
✅ Algoritmo SHA256withRSA (2048-bit)
✅ Certificado válido até 2053
✅ Sem malware detectado
✅ Compatível com Android 6.0+
```

## 📝 Notas Técnicas

### Assinatura do APK

```
Certificado: CN=IFMT Inventario, OU=TI, O=IFMT, L=Cuiaba, ST=MT, C=BR
Algoritmo: SHA256withRSA
Tamanho da Chave: 2048-bit
Válido até: 2053-04-15
```

### Otimizações Aplicadas

- ✅ Zipalign (4-byte alignment)
- ✅ ProGuard (ofuscação de código)
- ✅ Minificação de recursos
- ✅ Compressão de imagens

---

**Última atualização:** 12/12/2025  
**Versão:** 2.7.0  
**Status:** ✅ Testado e Validado
