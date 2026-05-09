# Criptografia de Senhas do Banco de Dados

## Visão Geral

O sistema implementa **criptografia AES-256-CBC** para proteger as senhas do banco de dados armazenadas em arquivos de configuração.

## Especificações Técnicas

### Algoritmo
- **Cipher**: AES/CBC/PKCS5Padding
- **Key Size**: 256 bits
- **IV Size**: 16 bytes (128 bits)
- **Key Derivation**: PBKDF2WithHmacSHA256
- **Iterations**: 65.536
- **Encoding**: Base64

### Fluxo de Criptografia

```
Senha em Texto Plano
    ↓
Gerar Chave (PBKDF2 + Machine ID)
    ↓
Gerar IV Aleatório (16 bytes)
    ↓
Criptografar com AES-256-CBC
    ↓
Combinar: [IV][CipherText]
    ↓
Codificar em Base64
    ↓
Senha Criptografada
```

### Fluxo de Descriptografia

```
Senha Criptografada (Base64)
    ↓
Decodificar Base64
    ↓
Separar: IV (16 bytes) + CipherText
    ↓
Gerar Chave (PBKDF2 + Machine ID)
    ↓
Descriptografar com AES-256-CBC
    ↓
Senha em Texto Plano
```

## Implementação

### Classe Principal: `PasswordEncryption`

**Localização**: `com.inventario.util.PasswordEncryption`

#### Métodos Públicos

```java
// Criptografar senha
String encrypted = PasswordEncryption.encrypt("minhaSenha123");

// Descriptografar senha
String decrypted = PasswordEncryption.decrypt(encrypted);

// Verificar se está criptografado
boolean isEncrypted = PasswordEncryption.isEncrypted(text);
```

### Geração da Chave

A chave é derivada de informações únicas da máquina:

```java
private static String getMachineIdentifier() {
    StringBuilder identifier = new StringBuilder();
    
    // Propriedades do sistema
    identifier.append(System.getProperty("user.name", ""));
    identifier.append(System.getProperty("os.name", ""));
    identifier.append(System.getProperty("os.arch", ""));
    identifier.append(System.getProperty("os.version", ""));
    identifier.append(System.getProperty("user.home", ""));
    
    // Hash SHA-256 do identificador
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(identifier.toString().getBytes());
    
    return Base64.getEncoder().encodeToString(hash);
}
```

### PBKDF2 Key Derivation

```java
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec = new PBEKeySpec(
    machineInfo.toCharArray(),
    SALT.getBytes(StandardCharsets.UTF_8),
    65536,  // Iterações
    256     // Key size
);

SecretKey tmp = factory.generateSecret(spec);
SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
```

## Segurança

### Pontos Fortes

1. **AES-256**: Algoritmo aprovado pelo NIST, usado por governos
2. **PBKDF2**: Derivação de chave resistente a ataques de força bruta
3. **65.536 Iterações**: Torna ataques de dicionário muito lentos
4. **IV Aleatório**: Mesma senha gera cifras diferentes
5. **Machine-Specific**: Chave única por máquina

### Limitações

1. **Machine-Specific**: Senha não funciona em outra máquina
2. **Salt Fixo**: Salt é fixo no código (não ideal, mas aceitável)
3. **Chave em Memória**: Chave é gerada em tempo de execução (não persistida)
4. **Sem HSM**: Não usa Hardware Security Module

### Comparação com Alternativas

| Método | Segurança | Portabilidade | Complexidade |
|--------|-----------|---------------|--------------|
| **Texto Plano** | ❌ Nenhuma | ✅ Total | ✅ Simples |
| **Base64** | ❌ Nenhuma | ✅ Total | ✅ Simples |
| **AES-256 (nossa impl.)** | ✅ Alta | ❌ Machine-only | ⚠️ Média |
| **Keystore Java** | ✅ Alta | ⚠️ Limitada | ❌ Complexa |
| **HSM** | ✅ Muito Alta | ❌ Nenhuma | ❌ Muito Complexa |

## Exemplos de Uso

### Exemplo 1: Criptografar e Salvar

```java
import com.inventario.util.PasswordEncryption;
import java.util.Properties;
import java.io.FileOutputStream;

public class SalvarSenha {
    public static void main(String[] args) throws Exception {
        String senha = "minhaSenhaSecreta";
        
        // Criptografar
        String senhaCriptografada = PasswordEncryption.encrypt(senha);
        
        // Salvar em arquivo
        Properties props = new Properties();
        props.setProperty("password", senhaCriptografada);
        
        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            props.store(fos, "Password is encrypted");
        }
        
        System.out.println("Senha salva criptografada!");
    }
}
```

### Exemplo 2: Carregar e Descriptografar

```java
import com.inventario.util.PasswordEncryption;
import java.util.Properties;
import java.io.FileInputStream;

public class CarregarSenha {
    public static void main(String[] args) throws Exception {
        // Carregar de arquivo
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        }
        
        String senhaCriptografada = props.getProperty("password");
        
        // Descriptografar
        String senha = PasswordEncryption.decrypt(senhaCriptografada);
        
        System.out.println("Senha descriptografada: " + senha);
    }
}
```

### Exemplo 3: Verificar se Está Criptografado

```java
import com.inventario.util.PasswordEncryption;

public class VerificarCriptografia {
    public static void main(String[] args) {
        String texto1 = "senhaPlana";
        String texto2 = "AQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGRo=";
        
        System.out.println("'" + texto1 + "' está criptografado? " + 
                          PasswordEncryption.isEncrypted(texto1));
        
        System.out.println("'" + texto2 + "' está criptografado? " + 
                          PasswordEncryption.isEncrypted(texto2));
    }
}
```

## Testes

### Executar Teste Unitário

```bash
mvn test -Dtest=PasswordEncryptionTest
```

### Executar Teste de Integração

```bash
mvn exec:java -Dexec.mainClass="com.inventario.config.DatabaseConfigManagerEncryptionTest"
```

### Executar Teste Manual

```bash
mvn exec:java -Dexec.mainClass="com.inventario.util.PasswordEncryption"
```

## Migração de Senhas em Texto Plano

O sistema detecta automaticamente senhas em texto plano e as migra:

```java
// DatabaseConfigManager.loadConfiguration()
String encryptedPassword = props.getProperty("password", "");
String password = "";

if (!encryptedPassword.isEmpty()) {
    try {
        // Tentar descriptografar
        password = PasswordEncryption.decrypt(encryptedPassword);
    } catch (Exception e) {
        // Se falhar, é texto plano (compatibilidade)
        password = encryptedPassword;
    }
}
```

### Processo de Migração

1. Sistema detecta senha em texto plano
2. Carrega senha normalmente
3. Na próxima vez que salvar, criptografa automaticamente
4. Arquivo é atualizado com senha criptografada

## Troubleshooting

### Erro: "Senha não descriptografa"

**Causa**: Arquivo foi copiado de outra máquina

**Solução**: Reconfigurar senha no dialog de configuração

### Erro: "Invalid key length"

**Causa**: JCE Unlimited Strength não instalado (Java < 8u151)

**Solução**: 
- Atualizar Java para versão mais recente
- Ou instalar JCE Unlimited Strength Policy Files

### Erro: "BadPaddingException"

**Causa**: Senha foi corrompida ou modificada manualmente

**Solução**: Limpar configurações e reconfigurar

## Boas Práticas

### ✅ Recomendado

- Usar criptografia em todos os ambientes
- Fazer backup das configurações (sem senha)
- Documentar processo de recuperação
- Testar descriptografia após salvar

### ❌ Não Recomendado

- Copiar arquivos de configuração entre máquinas
- Editar senha criptografada manualmente
- Compartilhar arquivos de configuração
- Usar mesma senha em múltiplos ambientes

## Melhorias Futuras

- [ ] Usar Keystore Java para armazenar chave
- [ ] Implementar rotação de chaves
- [ ] Adicionar suporte a HSM
- [ ] Permitir configuração de iterações PBKDF2
- [ ] Implementar salt por usuário
- [ ] Adicionar auditoria de acesso à senha
- [ ] Suporte a múltiplos perfis de configuração

## Referências

- [AES - Advanced Encryption Standard](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard)
- [PBKDF2 - Password-Based Key Derivation Function 2](https://en.wikipedia.org/wiki/PBKDF2)
- [Java Cryptography Architecture](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [NIST Special Publication 800-132](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-132.pdf)

---

**Versão**: 1.0.0  
**Data**: 12/11/2025  
**Autor**: Sistema de Inventário IFMT
