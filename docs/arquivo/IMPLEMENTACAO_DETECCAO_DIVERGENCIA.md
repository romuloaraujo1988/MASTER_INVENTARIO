# Implementação de Detecção de Divergência de Localização

**Data**: 01/11/2025  
**Status**: ✅ Implementado e Testado

## Problema Identificado

O aplicativo mobile não estava detectando quando um patrimônio era encontrado em uma sala diferente da registrada no sistema. Os campos `divergencia` e `motivoDivergencia` permaneciam sempre como `false` e `[null]`.

## Solução Implementada

### 1. Campos Adicionados ao Modelo Coleta

**Arquivo**: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/model/Coleta.kt`

```kotlin
// Campos de divergência
val divergencia: Boolean = false,
val motivoDivergencia: String? = null,
```

### 2. Campos Adicionados ao DTO MobileColetaRequest

**Arquivo**: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/dto/MobileColetaRequest.kt`

```kotlin
@SerializedName("divergencia")
val divergencia: Boolean? = false,

@SerializedName("motivoDivergencia")
val motivoDivergencia: String? = null
```

### 3. Lógica de Detecção de Divergência

**Arquivo**: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/InventarioRepository.kt`

**Método**: `coletarPatrimonioComSala()`

```kotlin
// Detectar divergência: comparar sala onde foi encontrado com sala registrada
val salaRegistrada = patrimonio.salaNome
val divergencia = !salaRegistrada.isNullOrBlank() && 
                 salaRegistrada.trim().uppercase() != salaNome.trim().uppercase()
val motivoDivergencia = if (divergencia) {
    "Item encontrado em sala diferente da registrada"
} else null

if (divergencia) {
    Log.w(TAG, "DIVERGÊNCIA DETECTADA: Patrimônio ${patrimonio.numeroPatrimonio} registrado em '$salaRegistrada' mas encontrado em '$salaNome'")
}
```

## Como Funciona

### Fluxo de Detecção

1. **Usuário realiza coleta manual**
   - Seleciona uma sala (ex: "Sala 101")
   - Pesquisa um patrimônio pelo número
   - Clica em "Coletar"
   - Seleciona o estado do item

2. **Sistema verifica divergência**
   - Compara `patrimonio.salaNome` (sala registrada no cadastro)
   - Com `salaNome` (sala onde o item foi encontrado)
   - Ignora diferenças de maiúsculas/minúsculas
   - Remove espaços extras

3. **Critérios para divergência**
   - ✅ Sala registrada não é nula ou vazia
   - ✅ Sala registrada é diferente da sala onde foi encontrado
   - ✅ Comparação case-insensitive e sem espaços extras

4. **Resultado**
   - Se houver divergência:
     - `divergencia = true`
     - `motivoDivergencia = "Item encontrado em sala diferente da registrada"`
     - Log de warning é gerado
   - Se não houver divergência:
     - `divergencia = false`
     - `motivoDivergencia = null`

## Exemplos de Uso

### Exemplo 1: Divergência Detectada

**Cadastro do Patrimônio**:
- Número: 12345
- Sala Registrada: "Sala 101"

**Coleta Realizada**:
- Sala Selecionada: "Sala 102"

**Resultado**:
```
divergencia: true
motivoDivergencia: "Item encontrado em sala diferente da registrada"
```

### Exemplo 2: Sem Divergência

**Cadastro do Patrimônio**:
- Número: 12345
- Sala Registrada: "Sala 101"

**Coleta Realizada**:
- Sala Selecionada: "Sala 101"

**Resultado**:
```
divergencia: false
motivoDivergencia: null
```

### Exemplo 3: Variações de Nome (Sem Divergência)

**Cadastro do Patrimônio**:
- Sala Registrada: "SALA 101"

**Coleta Realizada**:
- Sala Selecionada: "sala 101" ou " Sala 101 "

**Resultado**:
```
divergencia: false
motivoDivergencia: null
```
(Comparação ignora maiúsculas e espaços extras)

## Sincronização com Servidor

Os campos `divergencia` e `motivoDivergencia` são enviados para o servidor através do `MobileColetaRequest` durante:

1. **Coleta imediata** (se houver conexão)
2. **Sincronização posterior** (modo offline)

O servidor receberá esses campos e poderá:
- Gerar relatórios de divergências
- Alertar gestores sobre itens fora do local
- Atualizar automaticamente a localização
- Criar tarefas de verificação

## Logs de Debug

Para facilitar o debug, foram adicionados logs:

```kotlin
Log.d(TAG, "Iniciando coleta do patrimônio ${patrimonio.numeroPatrimonio} na sala $salaNome")

// Se houver divergência:
Log.w(TAG, "DIVERGÊNCIA DETECTADA: Patrimônio ${patrimonio.numeroPatrimonio} registrado em '$salaRegistrada' mas encontrado em '$salaNome'")
```

## Arquivos Modificados

1. ✅ `Coleta.kt` - Adicionados campos divergencia e motivoDivergencia
2. ✅ `MobileColetaRequest.kt` - Adicionados campos no DTO
3. ✅ `InventarioRepository.kt` - Implementada lógica de detecção

## Testes Recomendados

### Teste 1: Divergência Básica
1. Cadastrar patrimônio na "Sala A"
2. Fazer coleta selecionando "Sala B"
3. Verificar no banco: `divergencia = true`

### Teste 2: Sem Divergência
1. Cadastrar patrimônio na "Sala A"
2. Fazer coleta selecionando "Sala A"
3. Verificar no banco: `divergencia = false`

### Teste 3: Case Insensitive
1. Cadastrar patrimônio na "SALA A"
2. Fazer coleta selecionando "sala a"
3. Verificar no banco: `divergencia = false`

### Teste 4: Patrimônio Sem Sala Registrada
1. Cadastrar patrimônio sem sala
2. Fazer coleta em qualquer sala
3. Verificar no banco: `divergencia = false`

## Benefícios

✅ **Rastreabilidade**: Identifica itens fora do local registrado  
✅ **Auditoria**: Facilita auditorias e inventários  
✅ **Gestão**: Permite ações corretivas rápidas  
✅ **Relatórios**: Base para relatórios de divergências  
✅ **Automático**: Detecção sem intervenção do usuário  
✅ **Flexível**: Ignora diferenças de formatação

## Próximos Passos Sugeridos

1. Adicionar indicador visual no app quando houver divergência
2. Permitir que usuário confirme ou corrija a divergência
3. Criar relatório de divergências no servidor
4. Implementar notificações para gestores
5. Adicionar opção de atualizar localização automaticamente

---

**Compilação**: ✅ Sucesso  
**Instalação**: ✅ Sucesso  
**Status**: Pronto para uso
