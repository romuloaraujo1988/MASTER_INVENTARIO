# Configuração da Keystore para Build de Release

## 📋 Visão Geral

Este documento explica como configurar as credenciais da keystore para fazer builds de release do aplicativo Android.

**⚠️ IMPORTANTE:** As credenciais da keystore **NUNCA** devem ser commitadas no Git por questões de segurança.

---

## 🚀 Configuração Rápida

### Passo 1: Criar arquivo keystore.properties

Na raiz do projeto `InventarioMobile/`, crie um arquivo chamado `keystore.properties`:

```properties
storePassword=inventario2025
keyPassword=inventario2025
keyAlias=inventario
storeFile=inventario-release.keystore
```

**Nota:** Use o arquivo `keystore.properties.example` como template.

### Passo 2: Verificar .gitignore

Certifique-se de que o arquivo `.gitignore` na raiz do projeto contém:

```gitignore
# Keystore credentials (NUNCA versionar)
keystore.properties
*.keystore
*.jks
```

### Passo 3: Build de Release

Execute o build normalmente:

```bash
cd InventarioMobile
.\gradlew.bat assembleRelease
```

O APK será gerado em: `app/build/outputs/apk/release/app-release.apk`

---

## 🔐 Segurança

### ✅ FAÇA

- ✅ Mantenha suas credenciais em um gerenciador de senhas seguro (1Password, LastPass, etc.)
- ✅ Use senhas fortes e únicas para a keystore
- ✅ Faça backup da keystore em local seguro (nuvem criptografada, cofre físico)
- ✅ Rotacione as credenciais periodicamente
- ✅ Compartilhe credenciais apenas através de canais seguros (gerenciador de senhas compartilhado)

### ❌ NÃO FAÇA

- ❌ **NUNCA** commite `keystore.properties` no Git
- ❌ **NUNCA** compartilhe suas credenciais por email, chat ou mensagem
- ❌ **NUNCA** use senhas fracas ou padrão
- ❌ **NUNCA** perca a keystore ou as senhas (impossível recuperar!)
- ❌ **NUNCA** exponha credenciais em logs ou screenshots

---

## 🛠️ Troubleshooting

### Erro: "keystore.properties não encontrado"

**Problema:** O arquivo `keystore.properties` não existe ou está no local errado.

**Solução:**
1. Verifique se o arquivo está em `InventarioMobile/keystore.properties` (não em `InventarioMobile/app/`)
2. Use o template `keystore.properties.example` como base
3. Preencha com as credenciais corretas

### Erro: "Keystore file not found"

**Problema:** O caminho para a keystore no `keystore.properties` está incorreto.

**Solução:**
1. Verifique se a keystore existe em `InventarioMobile/inventario-release.keystore`
2. Ajuste o caminho no `keystore.properties`:
   ```properties
   storeFile=inventario-release.keystore
   ```

### Erro: "Incorrect password"

**Problema:** A senha no `keystore.properties` está incorreta.

**Solução:**
1. Verifique as credenciais no gerenciador de senhas
2. Atualize o `keystore.properties` com as senhas corretas
3. Certifique-se de que não há espaços extras nas senhas

### Build de Debug Funciona, mas Release Falha

**Problema:** O build de debug não requer keystore.properties, mas o release sim.

**Solução:**
1. Configure o `keystore.properties` conforme instruções acima
2. Verifique que todas as propriedades estão preenchidas
3. Execute `.\gradlew.bat clean` e tente novamente

---

## 📝 Estrutura do Arquivo keystore.properties

```properties
# Senha da keystore (obrigatório)
storePassword=SUA_SENHA_AQUI

# Senha da chave privada (obrigatório)
keyPassword=SUA_SENHA_AQUI

# Alias da chave na keystore (obrigatório)
keyAlias=SEU_ALIAS_AQUI

# Caminho para o arquivo da keystore (obrigatório)
# Relativo ao diretório InventarioMobile/
storeFile=inventario-release.keystore
```

---

## 🔄 Rotação de Credenciais

Se as credenciais foram expostas ou comprometidas:

### 1. Gerar Nova Keystore

```bash
keytool -genkey -v -keystore inventario-release-new.keystore \
  -alias inventario \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### 2. Atualizar keystore.properties

```properties
storePassword=NOVA_SENHA
keyPassword=NOVA_SENHA
keyAlias=inventario
storeFile=inventario-release-new.keystore
```

### 3. Testar Build

```bash
.\gradlew.bat assembleRelease
```

### 4. Atualizar na Play Store

**⚠️ ATENÇÃO:** Trocar a keystore requer processo especial na Google Play Store. Consulte a documentação oficial antes de fazer isso em produção.

---

## 📚 Referências

- [Android Developers - Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Gradle - Signing configurations](https://developer.android.com/studio/build/gradle-tips#sign-your-app)
- [OWASP - Hardcoded Credentials](https://owasp.org/www-community/vulnerabilities/Use_of_hard-coded_password)

---

## 📞 Suporte

Se você tiver problemas com a configuração da keystore:

1. Verifique este documento primeiro
2. Consulte o time de desenvolvimento
3. **NUNCA** compartilhe suas credenciais ao pedir ajuda

---

**Última atualização:** 28/03/2026  
**Versão:** 1.0.0
