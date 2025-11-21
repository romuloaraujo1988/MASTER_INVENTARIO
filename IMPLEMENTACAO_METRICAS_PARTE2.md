# ✅ Implementação de Métricas - Parte 2 Concluída

**Data:** 16/11/2025  
**Versão:** 2.1  
**Status:** ✅ ViewModel Atualizado

---

## 🎯 O Que Foi Implementado

### ✅ ColetaViewModelClean Atualizado

**Arquivo:** `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/coleta/ColetaViewModelClean.kt`

**Novos Métodos de Tracking:**

```kotlin
// Tracking de tempo
viewModel.iniciarColeta()           // onCreate da Activity
viewModel.iniciarScan()             // Ao abrir scanner
viewModel.finalizarScan()           // Após scan bem-sucedido
viewModel.iniciarPreenchimento()    // Ao mostrar formulário

// Tracking de método
viewModel.setMetodoColeta("QR_CODE")  // Ao selecionar método
viewModel.setScanMetrics(metrics)     // Após scan (com métricas do tracker)

// Registrar com métricas
viewModel.registrarColetaComMetricas(...)  // Ao invés de registrarColeta()
```

**Métricas Coletadas Automaticamente:**
- ✅ Tempo total da coleta
- ✅ Tempo do scan
- ✅ Tempo de preenchimento
- ✅ Método de coleta
- ✅ Tipo de scan (QR ou Barcode)
- ✅ Tentativas e erros
- ✅ Qualidade da etiqueta
- ✅ Horário (hora, dia, período)

---

## 📱 Como Integrar nas Activities

### Exemplo 1: ScannerActivity (QR Code e Código de Barras)

```kotlin
@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {
    
    private val viewModel: ColetaViewModelClean by viewModels()
    private val scanTracker = ScanMetricsTracker()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Iniciar tracking de coleta
        viewModel.iniciarColeta()
        
        setupButtons()
    }
    
    private fun setupButtons() {
        // Botão QR Code
        btnScanQR.setOnClickListener {
            viewModel.setMetodoColeta(MetricsHelper.METODO_QR_CODE)
            viewModel.iniciarScan()
            scanTracker.iniciarScan(ScanMetricsTracker.TIPO_QR_CODE)
            
            // Abrir scanner configurado para QR Code
            startQRScanner()
        }
        
        // Botão Código de Barras
        btnScanBarcode.setOnClickListener {
            viewModel.setMetodoColeta(MetricsHelper.METODO_CODIGO_BARRAS)
            viewModel.iniciarScan()
            scanTracker.iniciarScan(ScanMetricsTracker.TIPO_CODIGO_BARRAS)
            
            // Abrir scanner configurado para Código de Barras
            startBarcodeScanner()
        }
    }
    
    // Callback do scanner
    private fun onScanAttempt() {
        scanTracker.registrarTentativa()
    }
    
    private fun onScanError() {
        scanTracker.registrarErro()
        Toast.makeText(this, "Tente novamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun onScanSuccess(code: String) {
        // Finalizar tracking
        viewModel.finalizarScan()
        val metrics = scanTracker.finalizarScan()
        viewModel.setScanMetrics(metrics)
        
        Log.d(TAG, "Scan bem-sucedido: ${metrics.getTipoScanDescricao()} em ${metrics.getTempoFormatado()}")
        
        // Iniciar preenchimento
        viewModel.iniciarPreenchimento()
        
        // Processar código
        processarCodigo(code)
    }
}
```

### Exemplo 2: ColetaActivity (Formulário de Coleta)

```kotlin
@AndroidEntryPoint
class ColetaActivity : AppCompatActivity() {
    
    private val viewModel: ColetaViewModelClean by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Se veio do scanner, métricas já foram iniciadas
        // Se é coleta manual, iniciar agora
        if (intent.getBooleanExtra("IS_MANUAL", false)) {
            viewModel.iniciarColeta()
            viewModel.setMetodoColeta(MetricsHelper.METODO_MANUAL)
            viewModel.iniciarPreenchimento()
        }
        
        setupUI()
    }
    
    private fun onRegistrarClick() {
        // Registrar com métricas
        viewModel.registrarColetaComMetricas(
            numeroPatrimonio = edtNumero.text.toString(),
            localizacaoAtual = edtLocalizacao.text.toString(),
            observacoes = edtObservacoes.text.toString(),
            latitude = currentLatitude,
            longitude = currentLongitude,
            idUsuario = getUserId()
        )
    }
}
```

### Exemplo 3: ManualCollectionActivity

```kotlin
@AndroidEntryPoint
class ManualCollectionActivity : AppCompatActivity() {
    
    private val viewModel: ColetaViewModelClean by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Iniciar tracking para coleta manual
        viewModel.iniciarColeta()
        viewModel.setMetodoColeta(MetricsHelper.METODO_MANUAL)
        viewModel.iniciarPreenchimento()
        
        setupUI()
    }
    
    private fun onBuscarClick() {
        // Se usar busca, atualizar método
        viewModel.setMetodoColeta(MetricsHelper.METODO_BUSCA)
        
        val numero = edtNumero.text.toString()
        viewModel.buscarPatrimonio(numero)
    }
}
```

---

## 🗄️ Sobre a Migração do Banco de Dados

### O Que É?

A migração é um script SQL que **adiciona novas colunas** na tabela `TABELA_COLETA` do PostgreSQL (backend) para armazenar as métricas coletadas pelo app.

### Arquivo Criado

`sql/migration_v2.1_metricas_coleta.sql`

### O Que Faz?

```sql
-- Adiciona 11 novas colunas:
ALTER TABLE TABELA_COLETA 
ADD COLUMN TEMPO_COLETA_SEGUNDOS INTEGER,
ADD COLUMN TEMPO_SCAN_SEGUNDOS INTEGER,
ADD COLUMN TEMPO_PREENCHIMENTO_SEGUNDOS INTEGER,
ADD COLUMN METODO_COLETA VARCHAR(20),
ADD COLUMN TIPO_SCAN VARCHAR(20),
ADD COLUMN TENTATIVAS_SCAN INTEGER,
ADD COLUMN ERROS_SCAN INTEGER,
ADD COLUMN HORA_COLETA INTEGER,
ADD COLUMN DIA_SEMANA INTEGER,
ADD COLUMN PERIODO_COLETA VARCHAR(10),
ADD COLUMN QUALIDADE_ETIQUETA VARCHAR(20);

-- Cria 6 índices para análise rápida
CREATE INDEX idx_coleta_tempo ON TABELA_COLETA(TEMPO_COLETA_SEGUNDOS);
-- ... etc
```

### Quando Executar?

**Antes de fazer deploy do app atualizado!**

O app vai enviar as métricas para o backend, então o banco precisa estar preparado para recebê-las.

### Como Executar?

```bash
# Opção 1: Via psql
psql -U inventario -d sispatrimonio -f sql/migration_v2.1_metricas_coleta.sql

# Opção 2: Via pgAdmin
# 1. Abrir pgAdmin
# 2. Conectar ao banco sispatrimonio
# 3. Abrir Query Tool
# 4. Copiar e colar o conteúdo do arquivo
# 5. Executar (F5)

# Opção 3: Via DBeaver
# 1. Conectar ao banco
# 2. Abrir SQL Editor
# 3. Carregar arquivo migration_v2.1_metricas_coleta.sql
# 4. Executar
```

### Verificar Se Funcionou

```sql
-- Ver as novas colunas
\d tabela_coleta

-- Ou
SELECT column_name, data_type 
FROM information_schema.columns
WHERE table_name = 'tabela_coleta'
  AND column_name LIKE '%tempo%' 
   OR column_name LIKE '%metodo%'
   OR column_name LIKE '%scan%';
```

### É Seguro?

✅ **SIM!** A migração:
- Não altera dados existentes
- Não remove colunas
- Apenas ADICIONA novas colunas (nullable)
- Cria índices para melhorar performance
- Inclui script de verificação

### E Se Já Tiver Coletas?

As coletas antigas continuam funcionando normalmente. As novas colunas ficam NULL para coletas antigas, e serão preenchidas apenas nas novas coletas.

O script até preenche alguns campos automaticamente:
```sql
-- Preenche horário das coletas antigas baseado na DATA_COLETA
UPDATE TABELA_COLETA 
SET HORA_COLETA = EXTRACT(HOUR FROM DATA_COLETA),
    DIA_SEMANA = EXTRACT(DOW FROM DATA_COLETA) + 1,
    PERIODO_COLETA = CASE 
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 6 AND 11 THEN 'MANHA'
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 12 AND 17 THEN 'TARDE'
        ELSE 'NOITE'
    END
WHERE HORA_COLETA IS NULL;
```

---

## 📋 Checklist de Integração

### Backend
- [ ] Executar migração do banco de dados
- [ ] Verificar colunas criadas
- [ ] Atualizar Model Coleta.java (próxima parte)
- [ ] Atualizar ColetaDAO.java (próxima parte)
- [ ] Atualizar MobileColetaDTO.java (próxima parte)

### Android - ViewModel
- [x] ColetaViewModelClean atualizado
- [x] Métodos de tracking adicionados
- [x] Cálculo de métricas implementado
- [x] Logs detalhados

### Android - Activities
- [ ] Atualizar ScannerActivity
- [ ] Atualizar ColetaActivity
- [ ] Atualizar ManualCollectionActivity
- [ ] Adicionar tracking de scan
- [ ] Testar fluxo completo

---

## 🧪 Como Testar

### 1. Testar ViewModel

```kotlin
// Em um teste ou Activity
val viewModel = ColetaViewModelClean(...)

// Simular coleta
viewModel.iniciarColeta()
Thread.sleep(1000)
viewModel.iniciarScan()
Thread.sleep(500)
viewModel.finalizarScan()
viewModel.iniciarPreenchimento()
Thread.sleep(2000)

// Ver resumo
val resumo = viewModel.getMetricasResumo()
Log.d("TEST", resumo)

// Output esperado:
// Métricas da Coleta:
// - Tempo Total: 3s
// - Tempo Scan: 0s (500ms)
// - Tempo Preenchimento: 2s
// - Método: QR Code 📱
// - Período: Manhã 🌅
```

### 2. Testar Integração Completa

1. Abrir app
2. Clicar em "Coletar Patrimônio"
3. Clicar em "Scan QR Code" ou "Scan Código de Barras"
4. Escanear código
5. Preencher formulário
6. Registrar coleta
7. Verificar logs:

```
D/ColetaViewModelClean: Coleta iniciada: timestamp=...
D/ColetaViewModelClean: Método de coleta definido: QR_CODE
D/ColetaViewModelClean: Scan iniciado: timestamp=...
D/ScanMetricsTracker: Tentativa #1 registrada
D/ScanMetricsTracker: Scan finalizado: tipo=QR_CODE, tempo=3s, ...
D/ColetaViewModelClean: Métricas de scan definidas: ScanMetrics(...)
D/ColetaViewModelClean: Preenchimento iniciado: timestamp=...
D/ColetaViewModelClean: Métricas da Coleta:
  - Tempo Total: 18s
  - Tempo Scan: 3s
  - Tempo Preenchimento: 15s
  - Método: QR Code 📱
  - Período: Manhã 🌅
```

---

## 🎯 Próximos Passos

### Parte 3: Atualizar Backend
- [ ] Atualizar Model Coleta.java
- [ ] Atualizar ColetaDAO.java
- [ ] Atualizar MobileColetaDTO.java
- [ ] Testar sincronização

### Parte 4: Integrar Activities
- [ ] Atualizar ScannerActivity
- [ ] Atualizar ColetaActivity
- [ ] Atualizar ManualCollectionActivity
- [ ] Testar fluxo completo

### Parte 5: Criar Relatórios
- [ ] Criar queries de análise
- [ ] Criar RelatorioMetricasFrame (desktop)
- [ ] Gráficos comparativos QR vs Barcode
- [ ] Dashboard de métricas

---

## ✅ Resumo

**Implementado:**
- ✅ ColetaViewModelClean com tracking completo
- ✅ Métodos automáticos de coleta de métricas
- ✅ Cálculo de tempo, horário e contexto
- ✅ Integração com ScanMetricsTracker
- ✅ Logs detalhados para debug

**Pendente:**
- ⏳ Executar migração do banco
- ⏳ Atualizar backend (Model, DAO, DTO)
- ⏳ Integrar tracking nas Activities
- ⏳ Testar fluxo completo

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** ✅ Parte 2 Concluída

**🚀 Próximo: Executar migração do banco e atualizar backend!**
