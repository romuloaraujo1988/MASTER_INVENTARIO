# Correção do Erro "Erro parsing time stamp" - ColetaFrame_v2

## 🔍 Problema Identificado

Erro ao carregar salas no `ColetaFrame_v2`:
```
Erro ao carregar salas: Erro parsing time stamp
```

**Causa Raiz**: O método `mapResultSetToEntity()` no `SalaDAO.java` estava tentando fazer parse do campo `DATA_CADASTRO` sem tratamento de exceção, causando falha quando havia timestamps inválidos ou nulos no banco de dados.

## ✅ Correções Aplicadas

### **CORREÇÃO PRINCIPAL - SalaDAO.java (Linha 119)**
   - Adicionado try-catch robusto ao redor de `rs.getTimestamp("DATA_CADASTRO")`
   - Verificação de null com `rs.wasNull()`
   - Logs de aviso ao invés de falha silenciosa
   - Usa data padrão do construtor em caso de erro

### 1. **Método `criarSalaMinimalFromResultSet()` - NOVO**
   - Cria objetos `Sala` usando APENAS campos essenciais
   - Evita completamente o campo `DATA_CADASTRO` que causa problemas
   - Campos carregados:
     - `ID_SALA`
     - `NUMERO_SALA`
     - `DESCRICAO`
     - `ID_SETOR`
     - `ATIVO`

### 2. **Atualização dos Métodos de Busca**
   - `buscarSalasAbertasParaColeta()`: Usa `criarSalaMinimalFromResultSet()`
   - `buscarTodasSalasAtivas()`: Usa `criarSalaMinimalFromResultSet()`
   - SELECT simplificado para apenas 5 colunas essenciais

### 3. **Tratamento de Exceções Melhorado**
   - Try-catch individual para cada sala processada
   - Logs detalhados com SQLState e ErrorCode
   - Métodos auxiliares `tryGetInt()` e `tryGetString()` para debugging seguro
   - Continua processando outras salas mesmo se uma falhar

### 4. **Logs de Debugging Aprimorados**
   - Exibe SQL executado
   - Mostra dados de cada sala processada
   - Identifica exatamente qual sala causa erro
   - Stack traces completos para análise

## 📋 Script SQL de Diagnóstico

Criado arquivo `diagnosticar-timestamps-sala.sql` para:
1. Verificar estrutura da tabela
2. Identificar timestamps inválidos ou nulos
3. Contar salas com problemas
4. Corrigir timestamps problemáticos (opcional)
5. Listar todas as colunas de timestamp

### Como Executar o Script

```bash
# No PostgreSQL
psql -h localhost -U inventario -d sispatrimonio -f diagnosticar-timestamps-sala.sql

# Ou via pgAdmin
# Abra o arquivo e execute no Query Tool
```

## 🧪 Como Testar

1. **Execute a aplicação desktop**
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

2. **Faça login** com suas credenciais

3. **Abra o ColetaFrame_v2**
   - Menu: Coleta → Coleta de Patrimônios v2

4. **Verifique os logs no console**
   - Procure por: `DEBUG SalaInventarioDAO: Buscando salas abertas`
   - Verifique se há erros detalhados

5. **Se o erro persistir**, os logs mostrarão:
   - SQL executado
   - Qual sala está causando o problema
   - Detalhes do erro (SQLState, ErrorCode)

## 🔧 Próximos Passos (se o erro persistir)

### Opção 1: Corrigir Dados no Banco
Execute o script SQL para corrigir timestamps inválidos:

```sql
-- Corrigir timestamps nulos ou inválidos
UPDATE tabela_sala 
SET data_cadastro = CURRENT_TIMESTAMP 
WHERE data_cadastro IS NULL 
   OR data_cadastro < '1900-01-01'::timestamp
   OR data_cadastro > '2100-01-01'::timestamp;
```

### Opção 2: Alterar Estrutura da Tabela
Se a coluna `DATA_CADASTRO` não for essencial:

```sql
-- Tornar a coluna nullable
ALTER TABLE tabela_sala 
ALTER COLUMN data_cadastro DROP NOT NULL;

-- Ou definir valor padrão
ALTER TABLE tabela_sala 
ALTER COLUMN data_cadastro SET DEFAULT CURRENT_TIMESTAMP;
```

### Opção 3: Investigar Tipo de Dados
Verificar se o tipo de dados está correto:

```sql
-- Ver tipo atual
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'tabela_sala' 
  AND column_name = 'data_cadastro';

-- Se necessário, alterar tipo
ALTER TABLE tabela_sala 
ALTER COLUMN data_cadastro TYPE timestamp USING data_cadastro::timestamp;
```

## 📊 Análise de Logs

Quando executar a aplicação, procure por estas mensagens:

### ✅ Sucesso
```
DEBUG SalaInventarioDAO: Buscando salas abertas para inventário ID: 1
DEBUG SalaInventarioDAO: SQL = SELECT DISTINCT s.ID_SALA, s.NUMERO_SALA...
DEBUG SalaInventarioDAO: Sala 1 - 101 - Sala de Aula
DEBUG SalaInventarioDAO: Sala 2 - 102 - Laboratório
DEBUG SalaInventarioDAO: Total de salas abertas encontradas: 2
```

### ❌ Erro Específico
```
ERRO ao processar sala individual: Erro parsing time stamp
  ID_SALA: 5
  NUMERO_SALA: 105
  Mensagem: Cannot parse "INVALID_DATE" as timestamp
```

### ❌ Erro Crítico
```
ERRO CRÍTICO ao buscar salas abertas para coleta:
  Mensagem: ERROR: column "data_cadastro" does not exist
  SQLState: 42703
  ErrorCode: 0
```

## 🎯 Resultado Esperado

Após as correções, o `ColetaFrame_v2` deve:
1. ✅ Carregar salas sem erro
2. ✅ Exibir combo de salas preenchido
3. ✅ Permitir seleção de sala
4. ✅ Habilitar componentes de coleta

## 📝 Notas Importantes

- **Campos Mínimos**: A abordagem atual usa apenas campos essenciais para garantir funcionamento
- **Campos Opcionais**: Campos como `ANDAR`, `BLOCO`, `CAPACIDADE` não são carregados nesta versão
- **Data de Cadastro**: Usa data padrão do construtor ao invés de buscar do banco
- **Compatibilidade**: Mantém compatibilidade com código existente

## 🔄 Reversão (se necessário)

Se precisar reverter as alterações:

```bash
git checkout HEAD -- src/main/java/com/inventario/dao/SalaInventarioDAO.java
```

---

**Última atualização**: 20/11/2025 23:03  
**Status**: ✅ Compilação bem-sucedida  
**Próximo passo**: Testar execução da aplicação
