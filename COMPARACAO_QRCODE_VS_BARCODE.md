# 📊 Comparação QR Code vs Código de Barras

**Data:** 16/11/2025  
**Versão:** 1.0  
**Status:** 🎯 Planejamento

---

## 🎯 Objetivo

Coletar métricas detalhadas para comparar a eficiência entre QR Code e Código de Barras, permitindo decisões baseadas em dados sobre qual tecnologia usar.

---

## 📊 Métricas Específicas para Comparação

### 1. Campos Adicionais no Banco de Dados

```sql
-- Adicionar ao script de migração
ALTER TABLE TABELA_COLETA 
ADD COLUMN TIPO_SCAN VARCHAR(20),           -- 'QR_CODE' ou 'CODIGO_BARRAS'
ADD COLUMN TENTATIVAS_SCAN INTEGER DEFAULT 1,
ADD COLUMN ERROS_SCAN INTEGER DEFAULT 0,
ADD COLUMN DISTANCIA_SCAN_CM INTEGER,       -- Distância do scan em cm
ADD COLUMN LUMINOSIDADE_SCAN INTEGER,       -- Luminosidade ambiente (0-100)
ADD COLUMN QUALIDADE_ETIQUETA VARCHAR(20);  -- 'OTIMA', 'BOA', 'REGULAR', 'RUIM'

-- Índices para análise
CREATE INDEX idx_coleta_tipo_scan ON TABELA_COLETA(TIPO_SCAN);
CREATE INDEX idx_coleta_tentativas ON TABELA_COLETA(TENTATIVAS_SCAN);

-- Comentários
COMMENT ON COLUMN TABELA_COLETA.TIPO_SCAN IS 'Tipo de scan: QR_CODE ou CODIGO_BARRAS';
COMMENT ON COLUMN TABELA_COLETA.TENTATIVAS_SCAN IS 'Número de tentativas até sucesso';
COMMENT ON COLUMN TABELA_COLETA.ERROS_SCAN IS 'Número de erros durante scan';
COMMENT ON COLUMN TABELA_COLETA.DISTANCIA_SCAN_CM IS 'Distância estimada do scan em cm';
COMMENT ON COLUMN TABELA_COLETA.LUMINOSIDADE_SCAN IS 'Luminosidade ambiente (0-100)';
COMMENT ON COLUMN TABELA_COLETA.QUALIDADE_ETIQUETA IS 'Qualidade da etiqueta: OTIMA, BOA, REGULAR, RUIM';
```

---

## 🔧 Implementação Android

### 1. Atualizar ColetaEntity

```kotlin
@Entity(tableName = "coleta")
data class ColetaEntity(
    // ... campos existentes ...
    
    val metodoColeta: String? = null,      // QR_CODE, CODIGO_BARRAS, MANUAL, etc
    val tipoScan: String? = null,          // QR_CODE ou CODIGO_BARRAS (específico)
    val tentativasScan: Int = 1,           // Quantas tentativas
    val errosScan: Int = 0,                // Quantos erros
    val distanciaScanCm: Int? = null,      // Distância estimada
    val luminosidadeScan: Int? = null,     // Luminosidade (0-100)
    val qualidadeEtiqueta: String? = null  // OTIMA, BOA, REGULAR, RUIM
)
```

### 2. Criar ScanMetricsTracker

```kotlin
// utils/ScanMetricsTracker.kt
class ScanMetricsTracker {
    
    private var tipoScan: String = ""
    private var tentativas: Int = 0
    private var erros: Int = 0
    private var inicioScan: Long = 0
    
    fun iniciarScan(tipo: String) {
        tipoScan = tipo
        tentativas = 0
        erros = 0
        inicioScan = System.currentTimeMillis()
        Log.d(TAG, "Scan iniciado: tipo=$tipo")
    }
    
    fun registrarTentativa() {
        tentativas++
        Log.d(TAG, "Tentativa #$tentativas")
    }
    
    fun registrarErro() {
        erros++
        Log.d(TAG, "Erro #$erros")
    }
    
    fun finalizarScan(): ScanMetrics {
        val tempoScan = ((System.currentTimeMillis() - inicioScan) / 1000).toInt()
        
        return ScanMetrics(
            tipoScan = tipoScan,
            tempoSegundos = tempoScan,
            tentativas = tentativas,
            erros = erros,
            luminosidade = estimarLuminosidade(),
            qualidadeEtiqueta = avaliarQualidade()
        )
    }
    
    private fun estimarLuminosidade(): Int {
        // Usar sensor de luz se disponível
        return 50 // placeholder
    }
    
    private fun avaliarQualidade(): String {
        return when {
            erros == 0 && tentativas == 1 -> "OTIMA"
            erros == 0 && tentativas <= 2 -> "BOA"
            erros <= 1 && tentativas <= 3 -> "REGULAR"
            else -> "RUIM"
        }
    }
}

data class ScanMetrics(
    val tipoScan: String,
    val tempoSegundos: Int,
    val tentativas: Int,
    val erros: Int,
    val luminosidade: Int,
    val qualidadeEtiqueta: String
)
```

### 3. Atualizar Scanner Activity

```kotlin
@AndroidEntryPoint
class ScannerActivity : AppCompatActivity() {
    
    private val scanTracker = ScanMetricsTracker()
    private var tipoScanAtual: String = "QR_CODE"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupScannerOptions()
    }
    
    private fun setupScannerOptions() {
        // Botão QR Code
        btnScanQR.setOnClickListener {
            tipoScanAtual = "QR_CODE"
            iniciarScanner()
        }
        
        // Botão Código de Barras
        btnScanBarcode.setOnClickListener {
            tipoScanAtual = "CODIGO_BARRAS"
            iniciarScanner()
        }
    }
    
    private fun iniciarScanner() {
        scanTracker.iniciarScan(tipoScanAtual)
        
        // Configurar scanner baseado no tipo
        val options = when (tipoScanAtual) {
            "QR_CODE" -> BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            
            "CODIGO_BARRAS" -> BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8
                )
                .build()
            
            else -> BarcodeScannerOptions.Builder().build()
        }
        
        startScanner(options)
    }
    
    private fun onScanAttempt() {
        scanTracker.registrarTentativa()
    }
    
    private fun onScanError() {
        scanTracker.registrarErro()
        
        // Mostrar feedback ao usuário
        Toast.makeText(this, "Tente novamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun onScanSuccess(code: String) {
        val metrics = scanTracker.finalizarScan()
        
        Log.d(TAG, """
            Scan bem-sucedido:
            Tipo: ${metrics.tipoScan}
            Tempo: ${metrics.tempoSegundos}s
            Tentativas: ${metrics.tentativas}
            Erros: ${metrics.erros}
            Qualidade: ${metrics.qualidadeEtiqueta}
        """.trimIndent())
        
        // Enviar métricas para ViewModel
        viewModel.setScanMetrics(metrics)
        
        // Processar código
        processarCodigo(code)
    }
}
```

---

## 📊 Queries SQL para Análise Comparativa

### 1. Comparação de Tempo Médio

```sql
-- Tempo médio de scan por tipo
SELECT 
    TIPO_SCAN,
    COUNT(*) as total_scans,
    ROUND(AVG(TEMPO_SCAN_SEGUNDOS), 2) as tempo_medio_segundos,
    ROUND(MIN(TEMPO_SCAN_SEGUNDOS), 2) as tempo_minimo,
    ROUND(MAX(TEMPO_SCAN_SEGUNDOS), 2) as tempo_maximo,
    ROUND(STDDEV(TEMPO_SCAN_SEGUNDOS), 2) as desvio_padrao
FROM TABELA_COLETA
WHERE TIPO_SCAN IS NOT NULL
GROUP BY TIPO_SCAN
ORDER BY tempo_medio_segundos;
```

### 2. Taxa de Sucesso

```sql
-- Taxa de sucesso por tipo (tentativas = 1)
SELECT 
    TIPO_SCAN,
    COUNT(*) as total,
    SUM(CASE WHEN TENTATIVAS_SCAN = 1 THEN 1 ELSE 0 END) as sucesso_primeira,
    ROUND(SUM(CASE WHEN TENTATIVAS_SCAN = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as taxa_sucesso_pct
FROM TABELA_COLETA
WHERE TIPO_SCAN IS NOT NULL
GROUP BY TIPO_SCAN;
```

### 3. Análise de Qualidade

```sql
-- Distribuição de qualidade por tipo
SELECT 
    TIPO_SCAN,
    QUALIDADE_ETIQUETA,
    COUNT(*) as quantidade,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY TIPO_SCAN), 2) as percentual
FROM TABELA_COLETA
WHERE TIPO_SCAN IS NOT NULL AND QUALIDADE_ETIQUETA IS NOT NULL
GROUP BY TIPO_SCAN, QUALIDADE_ETIQUETA
ORDER BY TIPO_SCAN, quantidade DESC;
```

### 4. Análise por Período

```sql
-- Comparação por período do dia
SELECT 
    TIPO_SCAN,
    PERIODO_COLETA,
    COUNT(*) as total,
    ROUND(AVG(TEMPO_SCAN_SEGUNDOS), 2) as tempo_medio,
    ROUND(AVG(TENTATIVAS_SCAN), 2) as tentativas_media
FROM TABELA_COLETA
WHERE TIPO_SCAN IS NOT NULL
GROUP BY TIPO_SCAN, PERIODO_COLETA
ORDER BY TIPO_SCAN, PERIODO_COLETA;
```

### 5. Análise de Erros

```sql
-- Taxa de erro por tipo
SELECT 
    TIPO_SCAN,
    COUNT(*) as total_scans,
    SUM(ERROS_SCAN) as total_erros,
    ROUND(AVG(ERROS_SCAN), 2) as erros_medio,
    ROUND(SUM(CASE WHEN ERROS_SCAN > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as pct_com_erro
FROM TABELA_COLETA
WHERE TIPO_SCAN IS NOT NULL
GROUP BY TIPO_SCAN;
```

---

## 📈 Relatórios Esperados

### Relatório 1: Comparativo Geral

```
╔════════════════════════════════════════════════════════════╗
║         COMPARATIVO: QR CODE vs CÓDIGO DE BARRAS          ║
╠════════════════════════════════════════════════════════════╣
║                                                            ║
║  TEMPO MÉDIO DE SCAN                                       ║
║  ├─ QR Code:          2.3 segundos  ⚡                     ║
║  └─ Código de Barras: 4.1 segundos                        ║
║                                                            ║
║  TAXA DE SUCESSO (1ª tentativa)                            ║
║  ├─ QR Code:          95%  ✅                              ║
║  └─ Código de Barras: 78%                                 ║
║                                                            ║
║  TENTATIVAS MÉDIAS                                         ║
║  ├─ QR Code:          1.1                                  ║
║  └─ Código de Barras: 1.5                                 ║
║                                                            ║
║  TAXA DE ERRO                                              ║
║  ├─ QR Code:          2%                                   ║
║  └─ Código de Barras: 8%                                  ║
║                                                            ║
║  QUALIDADE DAS ETIQUETAS                                   ║
║  QR Code:                                                  ║
║  ├─ Ótima:   85%  ████████████████████                    ║
║  ├─ Boa:     12%  ███                                      ║
║  └─ Regular:  3%  █                                        ║
║                                                            ║
║  Código de Barras:                                         ║
║  ├─ Ótima:   65%  ██████████████                          ║
║  ├─ Boa:     25%  ██████                                   ║
║  └─ Regular: 10%  ██                                       ║
║                                                            ║
║  RECOMENDAÇÃO: QR Code é 44% mais rápido e 22% mais       ║
║  confiável. Priorizar uso de QR Code.                      ║
╚════════════════════════════════════════════════════════════╝
```

### Relatório 2: Análise por Período

```
PRODUTIVIDADE POR PERÍODO

MANHÃ (6h-12h)
├─ QR Code:          2.1s | 96% sucesso | 450 scans
└─ Código de Barras: 3.8s | 82% sucesso | 320 scans

TARDE (12h-18h)
├─ QR Code:          2.4s | 94% sucesso | 380 scans
└─ Código de Barras: 4.3s | 75% sucesso | 280 scans

NOITE (18h-6h)
├─ QR Code:          2.6s | 92% sucesso | 120 scans
└─ Código de Barras: 4.8s | 68% sucesso |  85 scans

INSIGHT: QR Code mantém performance consistente.
Código de Barras degrada 26% à noite (luminosidade).
```

---

## 🎯 Insights Esperados

### 1. Performance
- QR Code: **2-3 segundos** por scan
- Código de Barras: **4-5 segundos** por scan
- **Ganho QR:** 40-50% mais rápido

### 2. Confiabilidade
- QR Code: **95%** sucesso na 1ª tentativa
- Código de Barras: **75-80%** sucesso na 1ª tentativa
- **Ganho QR:** 20% mais confiável

### 3. Condições Ambientais
- QR Code: Funciona bem em qualquer luminosidade
- Código de Barras: Sensível à luminosidade
- **Recomendação:** QR para ambientes variados

### 4. Qualidade das Etiquetas
- QR Code: Mais resistente a danos
- Código de Barras: Mais sensível a sujeira/desgaste
- **Recomendação:** QR para longo prazo

---

## 📋 Checklist de Implementação

### Backend
- [ ] Adicionar campos TIPO_SCAN, TENTATIVAS_SCAN, ERROS_SCAN
- [ ] Adicionar campos DISTANCIA_SCAN_CM, LUMINOSIDADE_SCAN
- [ ] Adicionar campo QUALIDADE_ETIQUETA
- [ ] Criar índices para análise
- [ ] Criar views para relatórios

### Android
- [ ] Atualizar ColetaEntity
- [ ] Criar ScanMetricsTracker
- [ ] Atualizar ScannerActivity
- [ ] Adicionar botões QR/Barcode
- [ ] Implementar tracking de tentativas
- [ ] Implementar tracking de erros
- [ ] Testar ambos os tipos

### Relatórios (Desktop)
- [ ] Criar RelatorioComparativoScanFrame
- [ ] Implementar queries de análise
- [ ] Criar gráficos comparativos
- [ ] Adicionar recomendações automáticas

---

## 🚀 Próximos Passos

1. **Implementar coleta de métricas** (esta fase)
2. **Coletar dados por 1-2 semanas**
3. **Analisar resultados**
4. **Criar relatórios no desktop**
5. **Tomar decisão baseada em dados**

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 🎯 Pronto para Implementação
