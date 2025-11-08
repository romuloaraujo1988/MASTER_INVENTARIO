# Solução: Som no Desktop ao Coletar pelo Mobile

**Data**: 02/11/2025  
**Problema**: Desktop emite som quando coleta é feita pelo app Android

## Análise

Após investigação, encontrei:

1. **Classe `SoundNotification.java`** - Responsável por emitir sons no desktop
2. **Importada em `ColetaFrame_v2.java`** - Mas não está sendo usada diretamente
3. **Não há polling ou auto-refresh** ativo que detecte novas coletas

## Possíveis Causas

1. **WebSocket ou notificação em tempo real** (não encontrado no código)
2. **Trigger no banco de dados** que dispara evento
3. **Listener de mudanças no banco**
4. **Atualização manual** que toca som

## Solução Recomendada

Como não consegui identificar exatamente onde o som está sendo tocado, recomendo:

### Opção 1: Desabilitar Sons Globalmente (Temporário)

Adicionar uma flag de configuração para desabilitar sons:

```java
// Em SoundNotification.java
private static boolean soundsEnabled = true;

public static void setSoundsEnabled(boolean enabled) {
    soundsEnabled = enabled;
}

public static void playSound(SoundType soundType) {
    if (!soundsEnabled) {
        return; // Som desabilitado
    }
    // ... resto do código
}
```

### Opção 2: Verificar Origem da Coleta

Modificar o método `inserirColeta` para aceitar um parâmetro indicando a origem:

```java
public void inserirColeta(Coleta coleta, boolean fromMobile) throws SQLException {
    // ... código de inserção ...
    
    // Só tocar som se não for do mobile
    if (!fromMobile) {
        SoundNotification.playColetaSalvaSound();
    }
}
```

### Opção 3: Desabilitar Sons no Servidor Mobile

No `MobileColetaService`, garantir que não há chamada para tocar som:

```java
// Remover qualquer chamada a SoundNotification no código do servidor mobile
```

## Investigação Adicional Necessária

Para identificar exatamente onde o som está sendo tocado, você pode:

1. **Adicionar logs em `SoundNotification.playSound()`**:
   ```java
   public static void playSound(SoundType soundType) {
       // Log para debug
       StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
       System.out.println("=== SOM SENDO TOCADO ===");
       System.out.println("Tipo: " + soundType);
       System.out.println("Chamado de:");
       for (int i = 2; i < Math.min(stackTrace.length, 10); i++) {
           System.out.println("  " + stackTrace[i]);
       }
       System.out.println("========================");
       
       // ... resto do código
   }
   ```

2. **Monitorar o desktop** quando fizer uma coleta pelo mobile e verificar os logs

3. **Verificar se há triggers no banco de dados**:
   ```sql
   SELECT * FROM information_schema.triggers 
   WHERE event_object_table = 'tabela_coleta';
   ```

## Solução Imediata

Enquanto investiga, você pode simplesmente comentar o código que toca o som:

```java
// Em SoundNotification.java
public static void playSound(SoundType soundType) {
    // TEMPORARIAMENTE DESABILITADO
    return;
    
    // ... resto do código comentado
}
```

Ou adicionar uma condição no início do método:

```java
public static void playSound(SoundType soundType) {
    // Desabilitar sons temporariamente
    if (true) return;
    
    // ... resto do código
}
```

## Recomendação Final

1. Adicione os logs sugeridos em `SoundNotification.playSound()`
2. Faça uma coleta pelo mobile
3. Verifique os logs do desktop para ver de onde o som está sendo chamado
4. Com essa informação, podemos fazer uma correção precisa

---

**Status**: Investigação em andamento  
**Próximo passo**: Adicionar logs para identificar origem da chamada
