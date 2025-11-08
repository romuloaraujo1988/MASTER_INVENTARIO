# Mecanismo de Instância Única

## Visão Geral

O sistema SIHCP implementa um mecanismo de instância única que garante que apenas uma cópia do sistema possa ser executada por vez no mesmo computador.

## Como Funciona

### Tecnologia Utilizada

O mecanismo utiliza **File Locking** do Java NIO (New I/O) para criar um lock exclusivo em um arquivo temporário:

1. **Arquivo de Lock**: Um arquivo chamado `.sihcp_instance.lock` é criado no diretório temporário do sistema
2. **Lock Exclusivo**: O sistema tenta adquirir um lock exclusivo usando `FileChannel.tryLock()`
3. **Verificação**: Se o lock não puder ser adquirido, significa que outra instância já está rodando
4. **Liberação Automática**: O lock é automaticamente liberado quando o sistema é fechado (via shutdown hook)

### Localização do Arquivo de Lock

O arquivo de lock é criado no diretório temporário do sistema operacional:

- **Windows**: `C:\Users\[usuario]\AppData\Local\Temp\.sihcp_instance.lock`
- **Linux**: `/tmp/.sihcp_instance.lock`
- **macOS**: `/var/folders/[random]/.sihcp_instance.lock`

## Implementação

### Classe Principal

A classe `SingleInstanceLock` (pacote `com.inventario.util`) gerencia todo o mecanismo:

```java
// Verificar se já existe uma instância rodando
if (!SingleInstanceLock.tryLock()) {
    SingleInstanceLock.showInstanceAlreadyRunningMessage();
    System.exit(0);
}
```

### Ponto de Entrada

A verificação é feita no método `main` da classe `JLogin`:

```java
public static void main(String args[]) {
    // Verificar instância única
    if (!SingleInstanceLock.tryLock()) {
        SingleInstanceLock.showInstanceAlreadyRunningMessage();
        System.exit(0);
        return;
    }
    
    // Continuar inicialização...
}
```

## Comportamento

### Primeira Instância

1. Sistema inicia normalmente
2. Arquivo de lock é criado
3. Lock exclusivo é adquirido
4. Sistema continua execução

### Segunda Instância (Tentativa)

1. Sistema tenta iniciar
2. Tenta adquirir lock no arquivo
3. Falha (arquivo já está locked)
4. Exibe mensagem de aviso
5. Sistema é encerrado

### Mensagem Exibida

Quando uma segunda instância tenta iniciar, a seguinte mensagem é exibida:

```
Sistema Já em Execução

O sistema SIHCP já está em execução!

Apenas uma instância do sistema pode ser executada por vez.
Por favor, feche a instância anterior antes de abrir uma nova.
```

## Vantagens

1. **Prevenção de Conflitos**: Evita problemas de concorrência no banco de dados
2. **Integridade de Dados**: Garante que apenas um usuário por máquina acesse o sistema
3. **Simplicidade**: Implementação simples e confiável
4. **Multiplataforma**: Funciona em Windows, Linux e macOS
5. **Limpeza Automática**: O lock é liberado automaticamente ao fechar o sistema

## Limitações

- O mecanismo funciona apenas **por máquina**
- Usuários diferentes na mesma máquina não podem executar o sistema simultaneamente
- Se o sistema travar sem liberar o lock, pode ser necessário deletar manualmente o arquivo de lock

## Solução de Problemas

### Sistema não inicia mesmo sem outra instância rodando

**Causa**: O arquivo de lock pode ter ficado "travado" após um encerramento anormal do sistema.

**Solução**:
1. Feche todas as instâncias do sistema
2. Navegue até o diretório temporário do sistema
3. Delete o arquivo `.sihcp_instance.lock`
4. Tente iniciar o sistema novamente

### Como encontrar o diretório temporário

**Windows**:
```
%TEMP%
```

**Linux/macOS**:
```bash
echo $TMPDIR  # macOS
echo /tmp     # Linux
```

## Código Fonte

- **Classe Principal**: `src/main/java/com/inventario/util/SingleInstanceLock.java`
- **Uso**: `src/main/java/com/inventario/view/JLogin.java` (método `main`)

## Testes

Para testar o mecanismo:

1. Inicie o sistema normalmente
2. Tente iniciar uma segunda instância
3. Verifique se a mensagem de aviso é exibida
4. Confirme que a segunda instância não inicia

## Manutenção

O mecanismo é totalmente automático e não requer manutenção. O arquivo de lock é:
- Criado automaticamente na primeira execução
- Deletado automaticamente ao fechar o sistema
- Recriado na próxima execução

---

**Última atualização**: 2025-01-19  
**Versão**: 1.0
