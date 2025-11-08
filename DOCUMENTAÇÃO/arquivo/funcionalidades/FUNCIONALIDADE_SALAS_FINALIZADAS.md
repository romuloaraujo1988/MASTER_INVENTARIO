# Funcionalidade: Filtrar Salas Finalizadas no App Mobile

## Data: 05/11/2025

## Objetivo

Impedir que salas com coleta finalizada apareçam para seleção no aplicativo Android, evitando coletas duplicadas e melhorando a organização do processo de inventário.

## Problema

Atualmente, todas as salas ativas aparecem no app mobile, mesmo aquelas que já tiveram sua coleta finalizada no sistema desktop. Isso pode causar:
- Confusão para os coletores
- Coletas duplicadas
- Dificuldade em saber quais salas ainda precisam ser coletadas

## Solução Implementada

### 1. Nova Tabela: TABELA_SALA_INVENTARIO

Criada uma tabela para controlar o status de coleta de cada sala em cada inventário.

**Estrutura:**
```sql
CREATE TABLE TABELA_SALA_INVENTARIO (
    ID SERIAL PRIMARY KEY,
    ID_INVENTARIO INTEGER NOT NULL,
    ID_SALA INTEGER NOT NULL,
    STATUS_COLETA VARCHAR(50) DEFAULT 'PENDENTE',
    DATA_INICIO TIMESTAMP,
    DATA_FINALIZACAO TIMESTAMP,
    TOTAL_PATRIMONIOS INTEGER DEFAULT 0,
    PATRIMONIOS_COLETADOS INTEGER DEFAULT 0,
    PERCENTUAL_CONCLUSAO DECIMAL(5,2) DEFAULT 0.00,
    OBSERVACOES TEXT,
    FINALIZADO_POR INTEGER,
    ...
)
```

**Status Possíveis:**
- `PENDENTE` - Sala ainda não iniciada
- `EM_ANDAMENTO` - Coleta em andamento
- `FINALIZADA` - Coleta concluída (não aparece no app)
- `CANCELADA` - Coleta cancelada

### 2. Funções SQL Criadas

#### inicializar_salas_inventario(id_inventario)
Inicializa todas as salas ativas para um inventário.

```sql
SELECT inicializar_salas_inventario(1);
```

#### finalizar_coleta_sala(id_inventario, id_sala, id_usuario, observacoes)
Finaliza a coleta de uma sala específica.

```sql
SELECT finalizar_coleta_sala(1, 10, 5, 'Coleta concluída com sucesso');
```

#### reabrir_coleta_sala(id_inventario, id_sala)
Reabre uma sala que foi finalizada (caso necessário).

```sql
SELECT reabrir_coleta_sala(1, 10);
```

### 3. View: vw_salas_inventario_status

View para facilitar consultas sobre o status das salas.

```sql
SELECT * FROM vw_salas_inventario_status 
WHERE ID_INVENTARIO = 1 
AND STATUS_COLETA != 'FINALIZADA';
```

### 4. Modificação no Backend

**Arquivo:** `MobileSalaService.java`

**Método modificado:** `listarSalas()`

**Lógica:**
1. Busca todas as salas ativas
2. Obtém o inventário ativo
3. Para cada sala, verifica se está finalizada
4. Retorna apenas salas não finalizadas

```java
public List<MobileSalaDTO> listarSalas() throws SQLException {
    // Buscar salas ativas
    List<Sala> salas = salaDAO.listarSalas();
    
    // Obter inventário ativo
    Integer idInventarioAtivo = obterIdInventarioAtivo();
    
    // Filtrar salas não finalizadas
    for (Sala sala : salas) {
        if (sala.isAtiva() && !isSalaFinalizada(idInventarioAtivo, sala.getIdSala())) {
            dtos.add(converterParaDTO(sala));
        }
    }
    
    return dtos;
}
```

## Fluxo de Uso

### 1. Iniciar Inventário (Desktop)

```sql
-- Criar inventário
INSERT INTO TABELA_INVENTARIO (NOME, ANO, DATA_INICIO, STATUS_INVENTARIO)
VALUES ('Inventário 2025', 2025, CURRENT_DATE, 'EM_ANDAMENTO');

-- Inicializar salas
SELECT inicializar_salas_inventario(1);
```

### 2. Coletar no App Mobile

- Usuário abre app
- Seleciona sala (apenas salas não finalizadas aparecem)
- Realiza coletas
- Salas finalizadas não aparecem na lista

### 3. Finalizar Sala (Desktop)

```sql
-- Finalizar sala 10 do inventário 1
SELECT finalizar_coleta_sala(1, 10, 5, 'Todos os patrimônios coletados');
```

### 4. Verificar Status

```sql
-- Ver todas as salas do inventário
SELECT * FROM vw_salas_inventario_status WHERE ID_INVENTARIO = 1;

-- Ver apenas salas pendentes
SELECT * FROM vw_salas_inventario_status 
WHERE ID_INVENTARIO = 1 AND STATUS_COLETA = 'PENDENTE';

-- Ver apenas salas finalizadas
SELECT * FROM vw_salas_inventario_status 
WHERE ID_INVENTARIO = 1 AND STATUS_COLETA = 'FINALIZADA';
```

## Instalação

### 1. Executar Script SQL

```bash
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabela_sala_inventario.sql
```

### 2. Recompilar Backend

```bash
.\mvnw.cmd compile
```

### 3. Reiniciar Servidor

```bash
# Parar servidor atual
# Iniciar novamente
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile
```

### 4. Recompilar App Android

```bash
cd InventarioMobile
.\gradlew.bat assembleDebug installDebug
```

## Integração com Sistema Desktop

### Adicionar Botão "Finalizar Coleta" na Interface

No frame de gerenciamento de salas ou inventário, adicionar:

```java
JButton btnFinalizarSala = new JButton("Finalizar Coleta");
btnFinalizarSala.addActionListener(e -> {
    int idInventario = getInventarioAtivo();
    int idSala = getSalaSelecionada();
    int idUsuario = getUsuarioLogado();
    
    String sql = "SELECT finalizar_coleta_sala(?, ?, ?, ?)";
    // Executar SQL
    
    JOptionPane.showMessageDialog(this, 
        "Coleta da sala finalizada com sucesso!\n" +
        "Esta sala não aparecerá mais no app mobile.");
});
```

### Adicionar Indicador Visual

Mostrar status da sala na lista:

```java
// Na tabela de salas
String status = getSalaStatus(idInventario, idSala);
if ("FINALIZADA".equals(status)) {
    // Mostrar em verde ou com ícone de check
    cell.setBackground(Color.GREEN);
    cell.setIcon(iconCheck);
}
```

## Benefícios

### Para Coletores
✅ Veem apenas salas que precisam ser coletadas  
✅ Não há confusão sobre quais salas já foram finalizadas  
✅ Interface mais limpa e focada

### Para Gestores
✅ Controle preciso do progresso por sala  
✅ Estatísticas detalhadas de conclusão  
✅ Rastreabilidade de quem finalizou cada sala

### Para o Sistema
✅ Evita coletas duplicadas  
✅ Melhora organização do processo  
✅ Facilita relatórios de progresso

## Estatísticas e Relatórios

### Progresso Geral do Inventário

```sql
SELECT 
    i.NOME AS INVENTARIO,
    COUNT(*) AS TOTAL_SALAS,
    SUM(CASE WHEN si.STATUS_COLETA = 'FINALIZADA' THEN 1 ELSE 0 END) AS SALAS_FINALIZADAS,
    SUM(CASE WHEN si.STATUS_COLETA = 'EM_ANDAMENTO' THEN 1 ELSE 0 END) AS SALAS_EM_ANDAMENTO,
    SUM(CASE WHEN si.STATUS_COLETA = 'PENDENTE' THEN 1 ELSE 0 END) AS SALAS_PENDENTES,
    ROUND(AVG(si.PERCENTUAL_CONCLUSAO), 2) AS PERCENTUAL_MEDIO
FROM TABELA_INVENTARIO i
JOIN TABELA_SALA_INVENTARIO si ON i.ID = si.ID_INVENTARIO
WHERE i.ID = 1
GROUP BY i.NOME;
```

### Salas com Maior Progresso

```sql
SELECT 
    s.NUMERO_SALA,
    s.DESCRICAO,
    si.PATRIMONIOS_COLETADOS,
    si.TOTAL_PATRIMONIOS,
    si.PERCENTUAL_CONCLUSAO,
    si.STATUS_COLETA
FROM TABELA_SALA_INVENTARIO si
JOIN TABELA_SALA s ON si.ID_SALA = s.ID_SALA
WHERE si.ID_INVENTARIO = 1
ORDER BY si.PERCENTUAL_CONCLUSAO DESC;
```

### Histórico de Finalizações

```sql
SELECT 
    s.NUMERO_SALA,
    s.DESCRICAO,
    si.DATA_FINALIZACAO,
    u.NOME_COMPLETO AS FINALIZADO_POR,
    si.PATRIMONIOS_COLETADOS,
    si.OBSERVACOES
FROM TABELA_SALA_INVENTARIO si
JOIN TABELA_SALA s ON si.ID_SALA = s.ID_SALA
LEFT JOIN TABELA_USUARIO u ON si.FINALIZADO_POR = u.ID
WHERE si.ID_INVENTARIO = 1
AND si.STATUS_COLETA = 'FINALIZADA'
ORDER BY si.DATA_FINALIZACAO DESC;
```

## Triggers Automáticos

### Atualização de Estatísticas

Quando uma coleta é registrada, as estatísticas da sala são atualizadas automaticamente:

```sql
CREATE TRIGGER trigger_update_sala_inventario_stats
    AFTER INSERT OR UPDATE ON TABELA_COLETA
    FOR EACH ROW
    EXECUTE FUNCTION update_sala_inventario_stats();
```

Isso mantém sempre atualizados:
- `PATRIMONIOS_COLETADOS`
- `PERCENTUAL_CONCLUSAO`

## Casos de Uso Especiais

### Reabrir Sala Finalizada

Se for necessário reabrir uma sala:

```sql
SELECT reabrir_coleta_sala(1, 10);
```

A sala voltará a aparecer no app mobile.

### Cancelar Coleta de Sala

```sql
UPDATE TABELA_SALA_INVENTARIO
SET STATUS_COLETA = 'CANCELADA',
    OBSERVACOES = 'Sala não será coletada neste inventário'
WHERE ID_INVENTARIO = 1 AND ID_SALA = 15;
```

### Verificar Salas Sem Patrimônios

```sql
SELECT * FROM vw_salas_inventario_status
WHERE ID_INVENTARIO = 1
AND TOTAL_PATRIMONIOS = 0;
```

## Troubleshooting

### Sala não aparece no app mas não está finalizada

**Verificar:**
```sql
SELECT * FROM TABELA_SALA_INVENTARIO 
WHERE ID_SALA = 10 AND ID_INVENTARIO = 1;
```

**Solução:**
```sql
-- Se não existir registro, inicializar
SELECT inicializar_salas_inventario(1);

-- Se status estiver errado, corrigir
UPDATE TABELA_SALA_INVENTARIO
SET STATUS_COLETA = 'PENDENTE'
WHERE ID_SALA = 10 AND ID_INVENTARIO = 1;
```

### Todas as salas sumiram do app

**Verificar inventário ativo:**
```sql
SELECT * FROM TABELA_INVENTARIO 
WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';
```

**Verificar se salas foram inicializadas:**
```sql
SELECT COUNT(*) FROM TABELA_SALA_INVENTARIO 
WHERE ID_INVENTARIO = 1;
```

## Arquivos Criados/Modificados

```
sql/
└── criar_tabela_sala_inventario.sql (NOVO)

src/main/java/com/inventario/mobile/server/service/
└── MobileSalaService.java (MODIFICADO)

DOCUMENTAÇÃO/
└── FUNCIONALIDADE_SALAS_FINALIZADAS.md (NOVO)
```

## Próximos Passos

1. ✅ Criar tabela e funções SQL
2. ✅ Modificar backend para filtrar salas
3. ⏳ Executar script SQL no banco
4. ⏳ Recompilar e reiniciar backend
5. ⏳ Testar no app Android
6. ⏳ Adicionar interface no desktop para finalizar salas
7. ⏳ Adicionar relatórios de progresso por sala

## Conclusão

Esta funcionalidade traz controle granular sobre o processo de coleta, permitindo que gestores finalizem salas individualmente e que coletores vejam apenas o que ainda precisa ser feito.

**Status**: ✅ IMPLEMENTADO (Aguardando execução do script SQL e reinicialização do servidor)
