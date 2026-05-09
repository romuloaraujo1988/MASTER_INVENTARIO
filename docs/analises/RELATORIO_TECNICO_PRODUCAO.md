# Relatório Técnico - Análise de Produção

**Data:** 29/12/2025  
**Ambiente:** PostgreSQL sispatrimonio  
**Versão do Sistema:** 2.0.0 (estimado)

---

## 1. Diagnóstico de Sincronização

### Problema Identificado

A taxa de coleta é extremamente baixa (0,16%), sugerindo que:
1. Coletas estão sendo feitas no app mobile mas não sincronizadas
2. Banco SQLite do app tem dados não sincronizados
3. Sincronização automática pode estar falhando

### Investigação Necessária

```sql
-- 1. Verificar última sincronização
SELECT 
  MAX(data_coleta) as ultima_coleta_servidor,
  NOW() as data_atual,
  NOW() - MAX(data_coleta) as tempo_sem_sincronizar
FROM tabela_coleta;

-- 2. Verificar coletas por dia
SELECT 
  DATE(data_coleta) as data,
  COUNT(*) as total_coletas,
  COUNT(CASE WHEN divergencia = true THEN 1 END) as com_divergencia
FROM tabela_coleta
GROUP BY DATE(data_coleta)
ORDER BY data DESC;

-- 3. Verificar status de sincronização
SELECT 
  COUNT(*) as total_coletas,
  COUNT(CASE WHEN id_participante_inventario IS NOT NULL THEN 1 END) as com_participante,
  COUNT(CASE WHEN id_participante_inventario IS NULL THEN 1 END) as sem_participante
FROM tabela_coleta;
```

### Ações Recomendadas

1. **Verificar App Mobile**
   ```bash
   # Conectar ao dispositivo Android
   adb shell
   sqlite3 /data/data/com.ifmt.inventariomobile/databases/inventario.db
   SELECT COUNT(*) FROM tabela_coleta;
   ```

2. **Verificar Logs de Sincronização**
   - Procurar por erros em `logs/sync.log`
   - Verificar status do WorkManager
   - Confirmar conectividade de rede

3. **Implementar Monitoramento**
   ```sql
   -- Criar tabela de log de sincronização
   CREATE TABLE IF NOT EXISTS log_sincronizacao (
     id SERIAL PRIMARY KEY,
     data_sincronizacao TIMESTAMP DEFAULT NOW(),
     total_coletas_sincronizadas INTEGER,
     total_erros INTEGER,
     tempo_execucao_ms INTEGER,
     status VARCHAR(50)
   );
   ```

---

## 2. Análise de Divergências

### Distribuição de Divergências

```sql
-- Divergências por tipo
SELECT 
  motivo_divergencia,
  COUNT(*) as quantidade,
  ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM tabela_coleta WHERE divergencia = true), 2) as percentual
FROM tabela_coleta
WHERE divergencia = true
GROUP BY motivo_divergencia
ORDER BY quantidade DESC;

-- Divergências por sala
SELECT 
  s.nome as sala,
  COUNT(*) as total_divergencias,
  COUNT(DISTINCT c.id_coletor) as coletores_envolvidos
FROM tabela_coleta c
LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_sala s ON p.id_sala = s.id
WHERE c.divergencia = true
GROUP BY s.id, s.nome
ORDER BY total_divergencias DESC;

-- Divergências por responsável
SELECT 
  r.nome as responsavel,
  COUNT(*) as total_divergencias,
  COUNT(DISTINCT c.id_patrimonio) as patrimonios_afetados
FROM tabela_coleta c
LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
WHERE c.divergencia = true
GROUP BY r.id, r.nome
ORDER BY total_divergencias DESC;
```

### Fluxo de Reconciliação Recomendado

```
1. Gerar Relatório de Divergências
   ↓
2. Notificar Responsáveis de Salas
   ↓
3. Coletor Revisita Local
   ↓
4. Atualiza Localização/Estado
   ↓
5. Aprova Reconciliação
   ↓
6. Sincroniza com Servidor
```

### Implementação no App

```kotlin
// Criar fluxo de reconciliação
data class ReconciliationRequest(
    val coletaId: Int,
    val novaLocalizacao: String,
    val novoEstado: String,
    val observacoes: String,
    val foto: String? = null
)

// Endpoint para reconciliação
@POST("api/mobile/coletas/{id}/reconciliar")
suspend fun reconciliarColeta(
    @Path("id") coletaId: Int,
    @Body request: ReconciliationRequest
): ApiResponse<Coleta>
```

---

## 3. Gestão de Patrimônios Sem Etiqueta

### Identificação

```sql
-- Patrimônios coletados sem etiqueta
SELECT 
  p.numero,
  p.descricao,
  p.categoria,
  s.nome as sala,
  r.nome as responsavel,
  COUNT(c.id) as total_coletas_sem_etiqueta,
  MAX(c.data_coleta) as ultima_coleta
FROM tabela_coleta c
LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_responsavel r ON p.id_responsavel = r.id
WHERE c.sem_etiqueta = true
GROUP BY p.id, p.numero, p.descricao, p.categoria, s.id, s.nome, r.id, r.nome
ORDER BY total_coletas_sem_etiqueta DESC;

-- Patrimônios que nunca foram coletados com etiqueta
SELECT 
  p.numero,
  p.descricao,
  p.categoria,
  s.nome as sala,
  COUNT(c.id) as total_coletas,
  COUNT(CASE WHEN c.sem_etiqueta = true THEN 1 END) as coletas_sem_etiqueta
FROM tabela_patrimonio p
LEFT JOIN tabela_sala s ON p.id_sala = s.id
LEFT JOIN tabela_coleta c ON p.id = c.id_patrimonio
WHERE p.status = 'Ativo'
GROUP BY p.id, p.numero, p.descricao, p.categoria, s.id, s.nome
HAVING COUNT(CASE WHEN c.sem_etiqueta = false THEN 1 END) = 0
ORDER BY p.numero;
```

### Processo de Reemissão

1. **Gerar Relatório**
   - Exportar lista de patrimônios sem etiqueta
   - Agrupar por sala/responsável
   - Priorizar por valor

2. **Reemitir Etiquetas**
   - Imprimir novas etiquetas QR code
   - Distribuir por responsável
   - Registrar reemissão no sistema

3. **Validar no App**
   - Implementar validação de etiqueta
   - Alertar se etiqueta não encontrada
   - Permitir coleta manual com foto

### Implementação no App

```kotlin
// Validação de etiqueta
data class EtiquetaValidationResult(
    val valida: Boolean,
    val numeroPatrimonio: String,
    val descricao: String,
    val ultimaReemissao: LocalDateTime? = null
)

// Fluxo de coleta sem etiqueta
@POST("api/mobile/coletas/sem-etiqueta")
suspend fun registrarColetaSemEtiqueta(
    @Body request: ColetaSemEtiquetaRequest
): ApiResponse<Coleta>

data class ColetaSemEtiquetaRequest(
    val descricao: String,
    val categoria: String,
    val localizacao: String,
    val estado: String,
    val foto: String,
    val observacoes: String? = null
)
```

---

## 4. Cálculo de Progresso em Tempo Real

### Problema Atual

Os campos de progresso estão vazios:
- `total_patrimonios`: 0
- `patrimonios_coletados`: 0
- `percentual_conclusao`: 0%

### Solução: View Materializada

```sql
-- Criar view de progresso
CREATE OR REPLACE VIEW vw_progresso_inventario AS
SELECT 
  i.id,
  i.nome,
  i.ano,
  i.status_inventario,
  COUNT(DISTINCT p.id) as total_patrimonios,
  COUNT(DISTINCT c.id) as patrimonios_coletados,
  COUNT(DISTINCT CASE WHEN c.divergencia = true THEN p.id END) as patrimonios_com_divergencia,
  COUNT(DISTINCT CASE WHEN c.sem_etiqueta = true THEN p.id END) as patrimonios_sem_etiqueta,
  ROUND(100.0 * COUNT(DISTINCT c.id) / NULLIF(COUNT(DISTINCT p.id), 0), 2) as percentual_conclusao,
  ROUND(100.0 * COUNT(DISTINCT CASE WHEN c.divergencia = true THEN p.id END) / NULLIF(COUNT(DISTINCT c.id), 0), 2) as percentual_divergencia,
  MAX(c.data_coleta) as ultima_coleta,
  i.data_inicio,
  i.data_fim
FROM tabela_inventario i
LEFT JOIN tabela_patrimonio p ON i.id = i.id  -- Ajustar relação se necessário
LEFT JOIN tabela_coleta c ON i.id = c.id_inventario AND p.id = c.id_patrimonio
GROUP BY i.id, i.nome, i.ano, i.status_inventario, i.data_inicio, i.data_fim;

-- Criar índices para performance
CREATE INDEX idx_coleta_inventario ON tabela_coleta(id_inventario);
CREATE INDEX idx_coleta_patrimonio ON tabela_coleta(id_patrimonio);
CREATE INDEX idx_coleta_divergencia ON tabela_coleta(divergencia);
CREATE INDEX idx_coleta_sem_etiqueta ON tabela_coleta(sem_etiqueta);
```

### Atualizar Campos Automaticamente

```sql
-- Trigger para atualizar progresso
CREATE OR REPLACE FUNCTION atualizar_progresso_inventario()
RETURNS TRIGGER AS $$
BEGIN
  UPDATE tabela_inventario
  SET 
    total_patrimonios = (
      SELECT COUNT(DISTINCT id_patrimonio) 
      FROM tabela_coleta 
      WHERE id_inventario = NEW.id_inventario
    ),
    patrimonios_coletados = (
      SELECT COUNT(DISTINCT id_patrimonio) 
      FROM tabela_coleta 
      WHERE id_inventario = NEW.id_inventario 
      AND status_coleta = 'COLETADO'
    ),
    percentual_conclusao = (
      SELECT ROUND(100.0 * COUNT(DISTINCT id_patrimonio) / 
        NULLIF((SELECT COUNT(*) FROM tabela_patrimonio), 0), 2)
      FROM tabela_coleta 
      WHERE id_inventario = NEW.id_inventario
    ),
    data_ultima_atualizacao = NOW()
  WHERE id = NEW.id_inventario;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_atualizar_progresso
AFTER INSERT OR UPDATE ON tabela_coleta
FOR EACH ROW
EXECUTE FUNCTION atualizar_progresso_inventario();
```

### Endpoint de Progresso na API

```java
@GetMapping("/api/mobile/inventario/{id}/progresso")
@RequireConsulta
public ResponseEntity<ProgressoInventarioDTO> obterProgresso(
    @PathVariable Integer id
) {
    ProgressoInventarioDTO progresso = inventarioService.obterProgresso(id);
    return ResponseEntity.ok(progresso);
}

@Data
public class ProgressoInventarioDTO {
    private Integer id;
    private String nome;
    private Integer totalPatrimonios;
    private Integer patrimoniosColetados;
    private Integer patrimoniosComDivergencia;
    private Integer patrimoniosSemEtiqueta;
    private BigDecimal percentualConclusao;
    private BigDecimal percentualDivergencia;
    private LocalDateTime ultimaColeta;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
```

---

## 5. Otimizações de Performance

### Índices Recomendados

```sql
-- Índices para coletas
CREATE INDEX idx_coleta_inventario_data ON tabela_coleta(id_inventario, data_coleta DESC);
CREATE INDEX idx_coleta_patrimonio_data ON tabela_coleta(id_patrimonio, data_coleta DESC);
CREATE INDEX idx_coleta_coletor_data ON tabela_coleta(id_coletor, data_coleta DESC);
CREATE INDEX idx_coleta_divergencia_data ON tabela_coleta(divergencia, data_coleta DESC);

-- Índices para patrimônios
CREATE INDEX idx_patrimonio_status ON tabela_patrimonio(status);
CREATE INDEX idx_patrimonio_sala ON tabela_patrimonio(id_sala);
CREATE INDEX idx_patrimonio_responsavel ON tabela_patrimonio(id_responsavel);
CREATE INDEX idx_patrimonio_categoria ON tabela_patrimonio(categoria);

-- Índices para salas
CREATE INDEX idx_sala_nome ON tabela_sala(nome);

-- Índices para responsáveis
CREATE INDEX idx_responsavel_nome ON tabela_responsavel(nome);
```

### Análise de Queries

```sql
-- Verificar plano de execução
EXPLAIN ANALYZE
SELECT 
  s.nome,
  COUNT(*) as total_coletas
FROM tabela_coleta c
LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_sala s ON p.id_sala = s.id
WHERE c.data_coleta > NOW() - INTERVAL '7 days'
GROUP BY s.id, s.nome;

-- Verificar tamanho de tabelas
SELECT 
  schemaname,
  tablename,
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as tamanho
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## 6. Monitoramento Contínuo

### Dashboard de Métricas

```sql
-- Criar view de métricas diárias
CREATE OR REPLACE VIEW vw_metricas_diarias AS
SELECT 
  DATE(data_coleta) as data,
  COUNT(*) as total_coletas,
  COUNT(DISTINCT id_coletor) as coletores_ativos,
  COUNT(DISTINCT id_patrimonio) as patrimonios_coletados,
  COUNT(CASE WHEN divergencia = true THEN 1 END) as divergencias,
  COUNT(CASE WHEN sem_etiqueta = true THEN 1 END) as sem_etiqueta,
  ROUND(100.0 * COUNT(CASE WHEN divergencia = true THEN 1 END) / COUNT(*), 2) as taxa_divergencia,
  ROUND(100.0 * COUNT(CASE WHEN sem_etiqueta = true THEN 1 END) / COUNT(*), 2) as taxa_sem_etiqueta
FROM tabela_coleta
GROUP BY DATE(data_coleta)
ORDER BY data DESC;

-- Alertas automáticos
CREATE TABLE IF NOT EXISTS alertas_sistema (
  id SERIAL PRIMARY KEY,
  data_alerta TIMESTAMP DEFAULT NOW(),
  tipo_alerta VARCHAR(100),
  mensagem TEXT,
  severidade VARCHAR(20), -- CRITICA, ALTA, MEDIA, BAIXA
  resolvido BOOLEAN DEFAULT false
);

-- Trigger para alertar divergências altas
CREATE OR REPLACE FUNCTION verificar_taxa_divergencia()
RETURNS TRIGGER AS $$
DECLARE
  taxa_divergencia NUMERIC;
BEGIN
  SELECT ROUND(100.0 * COUNT(CASE WHEN divergencia = true THEN 1 END) / COUNT(*), 2)
  INTO taxa_divergencia
  FROM tabela_coleta
  WHERE DATE(data_coleta) = CURRENT_DATE;
  
  IF taxa_divergencia > 30 THEN
    INSERT INTO alertas_sistema (tipo_alerta, mensagem, severidade)
    VALUES ('TAXA_DIVERGENCIA_ALTA', 
            'Taxa de divergência em ' || taxa_divergencia || '% (limite: 30%)',
            'ALTA');
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### Queries de Monitoramento

```sql
-- Status geral do sistema
SELECT 
  'Coletas Hoje' as metrica,
  COUNT(*) as valor
FROM tabela_coleta
WHERE DATE(data_coleta) = CURRENT_DATE
UNION ALL
SELECT 'Divergências Hoje', COUNT(*) FROM tabela_coleta WHERE DATE(data_coleta) = CURRENT_DATE AND divergencia = true
UNION ALL
SELECT 'Sem Etiqueta Hoje', COUNT(*) FROM tabela_coleta WHERE DATE(data_coleta) = CURRENT_DATE AND sem_etiqueta = true
UNION ALL
SELECT 'Coletores Ativos', COUNT(DISTINCT id_coletor) FROM tabela_coleta WHERE DATE(data_coleta) = CURRENT_DATE;

-- Alertas pendentes
SELECT * FROM alertas_sistema WHERE resolvido = false ORDER BY data_alerta DESC;
```

---

## 7. Plano de Ação

### Semana 1 (29/12 - 04/01)

- [ ] Verificar sincronização de dados do app mobile
- [ ] Analisar banco SQLite para coletas não sincronizadas
- [ ] Implementar logs de sincronização
- [ ] Criar dashboard de status

### Semana 2 (05/01 - 11/01)

- [ ] Implementar fluxo de reconciliação
- [ ] Gerar relatório de divergências
- [ ] Notificar responsáveis
- [ ] Criar view de progresso

### Semana 3 (12/01 - 18/01)

- [ ] Reemitir etiquetas para patrimônios sem etiqueta
- [ ] Implementar validação de etiqueta no app
- [ ] Criar fluxo de coleta manual
- [ ] Testar sincronização

### Semana 4 (19/01 - 25/01)

- [ ] Otimizar queries e índices
- [ ] Implementar monitoramento contínuo
- [ ] Criar alertas automáticos
- [ ] Documentar processos

---

## 8. Conclusão

O sistema está bem estruturado, mas requer:

1. **Imediato:** Verificar sincronização de dados
2. **Curto Prazo:** Reconciliar divergências
3. **Médio Prazo:** Reemitir etiquetas
4. **Longo Prazo:** Otimizar e monitorar

Com essas ações, o sistema estará pronto para escala e produção em larga escala.

---

**Relatório Preparado:** 29/12/2025  
**Próxima Revisão:** 05/01/2026  
**Responsável:** Equipe Técnica
