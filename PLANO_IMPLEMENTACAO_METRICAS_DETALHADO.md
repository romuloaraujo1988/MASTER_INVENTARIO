# 📋 Plano Detalhado de Implementação de Métricas

**Data:** 16/11/2025  
**Versão:** 2.1  
**Status:** 🎯 Planejamento Cuidadoso

---

## 🎯 Objetivo

Implementar coleta de métricas de forma **segura, incremental e testável**, sem quebrar funcionalidades existentes.

---

## 📊 Status Atual

### ✅ Concluído
- [x] Script de migração SQL criado
- [x] ScanMetricsTracker criado
- [x] MetricsHelper criado
- [x] ColetaEntity atualizada
- [x] ColetaViewModelClean atualizado

### ⏳ Pendente
- [ ] Executar migração do banco (PostgreSQL)
- [ ] Atualizar backend (Java)
- [ ] Integrar nas Activities (Android)
- [ ] Testar fluxo completo

---

## 🚀 Plano de Execução (Passo a Passo)

### FASE 1: Preparação do Banco de Dados ⚠️ CRÍTICO

**Objetivo:** Adicionar colunas no PostgreSQL sem quebrar nada

**Passos:**

1. **Backup do Banco** (OBRIGATÓRIO)
   ```bash
   # Fazer backup antes de qualquer alteração
   pg_dump -U inventario -d sispatrimonio > backup_antes_metricas_$(date +%Y%m%d).sql
   ```

2. **Testar Migração em Ambiente de Desenvolvimento**
   ```bash
   # Conectar ao banco de DEV (não produção!)
   psql -U inventario -d sispatrimonio_dev
   
   # Executar migração
   \i sql/migration_v2.1_metricas_coleta.sql
   
   # Verificar se funcionou
   \d tabela_coleta
   ```

3. **Validar Que Não Quebrou Nada**
   ```sql
   -- Testar inserção antiga (sem métricas)
   INSERT INTO TABELA_COLETA (
       ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, DATA_COLETA
   ) VALUES (1, 1, 1, NOW());
   
   -- Deve funcionar normalmente
   
   -- Testar inserção nova (com métricas)
   INSERT INTO TABELA_COLETA (
       ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, DATA_COLETA,
       TEMPO_COLETA_SEGUNDOS, METODO_COLETA
   ) VALUES (1, 2, 1, NOW(), 18, 'QR_CODE');
   
   -- Deve funcionar também
   ```

4. **Aplicar em Produção** (quando validado)
   ```bash
   # Fazer backup de produção
   pg_dump -U inventario -d sispatrimonio > backup_producao_$(date +%Y%m%d).sql
   
   # Aplicar migração
   psql -U inventario -d sispatrimonio -f sql/migration_v2.1_metricas_coleta.sql
   ```

**Critério de Sucesso:**
- ✅ Backup criado
- ✅ Colunas adicionadas
- ✅ Índices criados
- ✅ Inserções antigas funcionam
- ✅ Inserções novas funcionam

---

### FASE 2: Atualizar Backend (Java) - Parte 1

**Objetivo:** Fazer backend aceitar métricas (mas não obrigar)

**Passo 2.1: Atualizar Model Coleta.java**

Adicionar campos opcionais (nullable):
```java
// Campos de métricas (v2.1)
private Integer tempoColetaSegundos;
private Integer tempoScanSegundos;
private Integer tempoPreenchimentoSegundos;
private String metodoColeta;
private String tipoScan;
private Integer tentativasScan;
private Integer errosScan;
private Integer horaColeta;
private Integer diaSemana;
private String periodoColeta;
private String qualidadeEtiqueta;

// Getters e Setters
```

**Critério de Sucesso:**
- ✅ Código compila
- ✅ Campos são opcionais (nullable)
- ✅ Não quebra código existente

**Passo 2.2: Atualizar ColetaDAO.java**

Modificar método `inserir()` para aceitar novos campos:
```java
public int inserir(Coleta coleta) {
    String sql = """
        INSERT INTO TABELA_COLETA (
            ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, DATA_COLETA,
            STATUS_COLETA, OBSERVACAO_COLETA, LOCALIZACAO_ENCONTRADA,
            ESTADO_ENCONTRADO, DIVERGENCIA, LATITUDE, LONGITUDE,
            TEMPO_COLETA_SEGUNDOS, TEMPO_SCAN_SEGUNDOS, 
            TEMPO_PREENCHIMENTO_SEGUNDOS, METODO_COLETA,
            TIPO_SCAN, TENTATIVAS_SCAN, ERROS_SCAN,
            HORA_COLETA, DIA_SEMANA, PERIODO_COLETA,
            QUALIDADE_ETIQUETA
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    // ... setters com setObject() para aceitar null
}
```

**Critério de Sucesso:**
- ✅ Código compila
- ✅ Inserção com métricas funciona
- ✅ Inserção sem métricas funciona (null)

**Passo 2.3: Atualizar MobileColetaDTO.java**

Adicionar campos opcionais:
```java
@JsonProperty("tempoColetaSegundos")
private Integer tempoColetaSegundos;

@JsonProperty("metodoColeta")
private String metodoColeta;

// ... etc (todos opcionais)
```

**Critério de Sucesso:**
- ✅ Código compila
- ✅ JSON com métricas funciona
- ✅ JSON sem métricas funciona

---

### FASE 3: Testar Backend Isoladamente

**Objetivo:** Garantir que backend funciona antes de mexer no app

**Testes:**

1. **Teste 1: Inserção Antiga (sem métricas)**
   ```bash
   curl -X POST http://localhost:8080/api/mobile/coletas \
     -H "Content-Type: application/json" \
     -d '{
       "numeroPatrimonio": "12345",
       "idInventario": 1,
       "observacao": "Teste sem métricas"
     }'
   ```
   **Esperado:** ✅ Sucesso (200 OK)

2. **Teste 2: Inserção Nova (com métricas)**
   ```bash
   curl -X POST http://localhost:8080/api/mobile/coletas \
     -H "Content-Type: application/json" \
     -d '{
       "numeroPatrimonio": "12346",
       "idInventario": 1,
       "observacao": "Teste com métricas",
       "tempoColetaSegundos": 18,
       "metodoColeta": "QR_CODE",
       "tipoScan": "QR_CODE",
       "tentativasScan": 1,
       "errosScan": 0,
       "horaColeta": 10,
       "diaSemana": 2,
       "periodoColeta": "MANHA",
       "qualidadeEtiqueta": "OTIMA"
     }'
   ```
   **Esperado:** ✅ Sucesso (200 OK)

3. **Teste 3: Verificar no Banco**
   ```sql
   SELECT 
       NUMERO_PATRIMONIO,
       TEMPO_COLETA_SEGUNDOS,
       METODO_COLETA,
       TIPO_SCAN,
       QUALIDADE_ETIQUETA
   FROM TABELA_COLETA
   ORDER BY ID DESC
   LIMIT 2;
   ```
   **Esperado:** 
   - Linha 1: métricas NULL (teste 1)
   - Linha 2: métricas preenchidas (teste 2)

**Critério de Sucesso:**
- ✅ Todos os 3 testes passam
- ✅ Backend não quebrou
- ✅ Métricas são salvas corretamente

---

### FASE 4: Atualizar Android - Parte 1 (Preparação)

**Objetivo:** Preparar app para enviar métricas (mas não obrigar ainda)

**Passo 4.1: Atualizar Room Database**

Criar migração do Room:
```kotlin
// AppDatabase.kt
@Database(
    entities = [ColetaEntity::class, ...],
    version = 3, // Incrementar versão
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar colunas de métricas
                database.execSQL("ALTER TABLE coleta ADD COLUMN tempoColetaSegundos INTEGER")
                database.execSQL("ALTER TABLE coleta ADD COLUMN tempoScanSegundos INTEGER")
                // ... etc
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(...)
                .addMigrations(MIGRATION_2_3)
                .build()
        }
    }
}
```

**Critério de Sucesso:**
- ✅ App compila
- ✅ Migração do Room funciona
- ✅ Dados antigos preservados

**Passo 4.2: Atualizar Mapper**

Atualizar mapper para incluir métricas:
```kotlin
fun ColetaEntity.toDomain(): Coleta {
    return Coleta(
        // ... campos existentes ...
        tempoColetaSegundos = tempoColetaSegundos,
        metodoColeta = metodoColeta,
        // ... etc
    )
}
```

**Critério de Sucesso:**
- ✅ Código compila
- ✅ Mapper funciona com e sem métricas

---

### FASE 5: Testar App Isoladamente

**Objetivo:** Testar app antes de integrar tudo

**Testes:**

1. **Teste 1: Coleta Antiga (sem métricas)**
   - Abrir app
   - Fazer coleta normal
   - Verificar que funciona
   - Verificar logs: métricas devem ser NULL

2. **Teste 2: Coleta Nova (com métricas)**
   - Abrir app
   - Fazer coleta usando métodos do ViewModel
   - Verificar logs: métricas devem estar preenchidas
   - Verificar banco local: métricas salvas

3. **Teste 3: Sincronização**
   - Fazer coleta com métricas
   - Sincronizar
   - Verificar backend: métricas chegaram

**Critério de Sucesso:**
- ✅ Coleta antiga funciona
- ✅ Coleta nova funciona
- ✅ Sincronização funciona
- ✅ Métricas chegam no backend

---

### FASE 6: Integração Final (Opcional)

**Objetivo:** Integrar tracking nas Activities existentes

**Atividades a Atualizar:**
1. ScannerActivity
2. ColetaActivity
3. ManualCollectionActivity

**Abordagem:**
- Adicionar chamadas aos métodos do ViewModel
- Testar uma Activity por vez
- Validar que não quebrou nada

---

## ⚠️ Pontos de Atenção

### 1. Compatibilidade Retroativa
- ✅ Campos devem ser opcionais (nullable)
- ✅ App antigo deve funcionar com backend novo
- ✅ Backend novo deve aceitar requisições antigas

### 2. Migração de Dados
- ✅ Dados antigos não devem ser perdidos
- ✅ Colunas novas devem aceitar NULL
- ✅ Índices não devem travar o banco

### 3. Performance
- ✅ Índices criados para não degradar queries
- ✅ Tracking não deve impactar UX
- ✅ Logs devem ser controláveis

### 4. Testes
- ✅ Testar cada fase isoladamente
- ✅ Não pular para próxima fase sem validar
- ✅ Ter plano de rollback

---

## 🔄 Plano de Rollback

### Se Algo Der Errado no Banco:
```bash
# Restaurar backup
psql -U inventario -d sispatrimonio < backup_antes_metricas_YYYYMMDD.sql
```

### Se Algo Der Errado no Backend:
```bash
# Reverter para versão anterior
git checkout HEAD~1 src/main/java/com/inventario/model/Coleta.java
git checkout HEAD~1 src/main/java/com/inventario/dao/ColetaDAO.java
mvn clean package
```

### Se Algo Der Errado no App:
```bash
# Reverter para versão anterior
git checkout HEAD~1 InventarioMobile/app/src/main/java/...
./gradlew clean assembleDebug
```

---

## 📋 Checklist Geral

### Antes de Começar
- [ ] Fazer backup do banco de dados
- [ ] Criar branch no Git: `feature/metricas-coleta`
- [ ] Documentar estado atual
- [ ] Ter ambiente de testes

### Durante Implementação
- [ ] Testar cada fase isoladamente
- [ ] Validar compatibilidade retroativa
- [ ] Manter logs detalhados
- [ ] Documentar problemas encontrados

### Antes de Deploy
- [ ] Todos os testes passando
- [ ] Backend aceita requisições antigas e novas
- [ ] App funciona com e sem métricas
- [ ] Documentação atualizada
- [ ] Plano de rollback testado

---

## 🎯 Próximo Passo Recomendado

**FASE 1: Preparação do Banco de Dados**

1. Fazer backup do banco
2. Testar migração em DEV
3. Validar que não quebrou nada

**Quer que eu prepare os comandos específicos para o seu ambiente?**

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 📋 Plano Detalhado Pronto

**🎯 Aguardando aprovação para prosseguir com FASE 1**
