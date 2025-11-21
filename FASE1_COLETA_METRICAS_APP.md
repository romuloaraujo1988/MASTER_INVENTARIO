# 📱 Fase 1 - Coleta de Métricas no App Android

**Data:** 16/11/2025  
**Versão:** 1.0  
**Status:** 🎯 Pronto para Implementação

---

## 🎯 Objetivo

Fazer o app Android **coletar e armazenar** métricas importantes durante o processo de coleta, sem alterar a UX. Os dados serão enviados ao backend e posteriormente visualizados via relatórios no desktop.

---

## 📋 Princípios

1. **Transparente:** Usuário não percebe a coleta de métricas
2. **Automático:** Tudo é coletado automaticamente
3. **Leve:** Não impacta performance
4. **Opcional:** Falhas na coleta de métricas não impedem a coleta do patrimônio
5. **Incremental:** Implementar em fases pequenas

---

## 🚀 Fase 1.1 - Métricas de Tempo (1 semana)

### Objetivo
Coletar tempo de coleta para provar que digital é 10x mais rápido que papel.

### Backend - Alterações no Banco de Dados

```sql
-- Adicionar colunas na TABELA_COLETA
ALTER TABLE TABELA_COLETA 
ADD COLUMN TEMPO_COLETA_SEGUNDOS INTEGER,
ADD COLUMN TEMPO_SCAN_SEGUNDOS INTEGER,
ADD COLUMN TEMPO_PREENCHIMENTO_SEGUNDOS INTEGER,
ADD COLUMN METODO_COLETA VARCHAR(20),
ADD COLUMN HORA_COLETA INTEGER,
ADD COLUMN DIA_SEMANA INTEGER,
ADD COLUMN PERIODO_COLETA VARCHAR(10);

-- Adicionar comentários
COMMENT ON COLUMN TABELA_COLETA.TEMPO_COLETA_SEGUNDOS IS 'Tempo total da coleta em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_SCAN_SEGUNDOS IS 'Tempo do scan QR em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_PREENCHIMENTO_SEGUNDOS IS 'Tempo de preenchimento em segundos';
COMMENT ON COLUMN TABELA_COLETA.METODO_COLETA IS 'QR_CODE, MANUAL, BUSCA, SEM_ETIQUETA';
COMMENT ON COLUMN TABELA_COLETA.HORA_COLETA IS 'Hora da coleta (0-23)';
COMMENT ON COLUMN TABELA_COLETA.DIA_SEMANA IS 'Dia da semana (1=Dom, 7=Sab)';
COMMENT ON COLUMN TABELA_COLETA.PERIODO_COLETA IS 'MANHA, TARDE, NOITE';

-- Criar índices para consultas rápidas
CREATE INDEX idx_coleta_tempo ON TABELA_COLETA(TEMPO_COLETA_SEGUNDOS);
CREATE INDEX idx_coleta_metodo ON TABELA_COLETA(METODO_COLETA);
CREATE INDEX idx_coleta_hora ON TABELA_COLETA(HORA_COLETA);
CREATE INDEX idx_coleta_periodo ON TABELA_COLETA(PERIODO_COLETA);
```

### Backend - Atualizar Model

```java
// src/main/java/com/inventario/model/Coleta.java
public class Coleta {
    // ... campos existentes ...
    
    // ADICIONAR:
    private Integer tempoColetaSegundos;
    private Integer tempoScanSegundos;
    private Integer tempoPreenchimentoSegundos;
    private String metodoColeta;
    private Integer horaColeta;
    private Integer diaSemana;
    private String periodoColeta;
    
    // Getters e Setters
    public Integer getTempoColetaSegundos() {
        return tempoColetaSegundos;
    }
    
    public void setTempoColetaSegundos(Integer tempoColetaSegundos) {
        this.tempoColetaSegundos = tempoColetaSegundos;
    }
    
    public Integer getTempoScanSegundos() {
        return tempoScanSegundos;
    }
    
    public void setTempoScanSegundos(Integer tempoScanSegundos) {
        this.tempoScanSegundos = tempoScanSegundos;
    }
    
    public Integer getTempoPreenchimentoSegundos() {
        return tempoPreenchimentoSegundos;
    }
    
    public void setTempoPreenchimentoSegundos(Integer tempoPreenchimentoSegundos) {
        this.tempoPreenchimentoSegundos = tempoPreenchimentoSegundos;
    }
    
    public String getMetodoColeta() {
        return metodoColeta;
    }
    
    public void setMetodoColeta(String metodoColeta) {
        this.metodoColeta = metodoColeta;
    }
    
    public Integer getHoraColeta() {
        return horaColeta;
    }
    
    public void setHoraColeta(Integer horaColeta) {
        this.horaColeta = horaColeta;
    }
    
    public Integer getDiaSemana() {
        return diaSemana;
    }
    
    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }
    
    public String getPeriodoColeta() {
        return periodoColeta;
    }
    
    public void setPeriodoColeta(String periodoColeta) {
        this.periodoColeta = periodoColeta;
    }
    
    // Métodos utilitários
    public String getTempoColetaFormatado() {
        if (tempoColetaSegundos == null) return "N/A";
        if (tempoColetaSegundos < 60) {
            return tempoColetaSegundos + "s";
        } else {
            int minutos = tempoColetaSegundos / 60;
            int segundos = tempoColetaSegundos % 60;
            return String.format("%dm %ds", minutos, segundos);
        }
    }
    
    public String getMetodoColetaDescricao() {
        if (metodoColeta == null) return "N/A";
        switch (metodoColeta) {
            case "QR_CODE": return "QR Code";
            case "MANUAL": return "Manual";
            case "BUSCA": return "Busca";
            case "SEM_ETIQUETA": return "Sem Etiqueta";
            default: return metodoColeta;
        }
    }
    
    public String getPeriodoColetaDescricao() {
        if (periodoColeta == null) return "N/A";
        switch (periodoColeta) {
            case "MANHA": return "Manhã";
            case "TARDE": return "Tarde";
            case "NOITE": return "Noite";
            default: return periodoColeta;
        }
    }
}
```

### Backend - Atualizar DAO

```java
// src/main/java/com/inventario/dao/ColetaDAO.java
public class ColetaDAO {
    
    // Atualizar método de inserção
    public int inserir(Coleta coleta) {
        String sql = """
            INSERT INTO TABELA_COLETA (
                ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, DATA_COLETA,
                STATUS_COLETA, OBSERVACAO_COLETA, LOCALIZACAO_ENCONTRADA,
                ESTADO_ENCONTRADO, DIVERGENCIA, LATITUDE, LONGITUDE,
                TEMPO_COLETA_SEGUNDOS, TEMPO_SCAN_SEGUNDOS, 
                TEMPO_PREENCHIMENTO_SEGUNDOS, METODO_COLETA,
                HORA_COLETA, DIA_SEMANA, PERIODO_COLETA
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // ... parâmetros existentes ...
            stmt.setObject(12, coleta.getTempoColetaSegundos());
            stmt.setObject(13, coleta.getTempoScanSegundos());
            stmt.setObject(14, coleta.getTempoPreenchimentoSegundos());
            stmt.setString(15, coleta.getMetodoColeta());
            stmt.setObject(16, coleta.getHoraColeta());
            stmt.setObject(17, coleta.getDiaSemana());
            stmt.setString(18, coleta.getPeriodoColeta());
            
            // ... resto do código ...
        }
    }
}
```

### Backend - Atualizar DTO Mobile

```java
// src/main/java/com/inventario/mobile/server/dto/MobileColetaDTO.java
public class MobileColetaDTO {
    // ... campos existentes ...
    
    // ADICIONAR:
    @JsonProperty("tempoColetaSegundos")
    private Integer tempoColetaSegundos;
    
    @JsonProperty("tempoScanSegundos")
    private Integer tempoScanSegundos;
    
    @JsonProperty("tempoPreenchimentoSegundos")
    private Integer tempoPreenchimentoSegundos;
    
    @JsonProperty("metodoColeta")
    private String metodoColeta;  // QR_CODE, CODIGO_BARRAS, MANUAL, BUSCA, SEM_ETIQUETA
    
    @JsonProperty("tipoScan")
    private String tipoScan;  // QR_CODE, CODIGO_BARRAS (para análise específica)
    
    @JsonProperty("tentativasScan")
    private Integer tentativasScan;  // Quantas tentativas até conseguir
    
    @JsonProperty("horaColeta")
    private Integer horaColeta;
    
    @JsonProperty("diaSemana")
    private Integer diaSemana;
    
    @JsonProperty("periodoColeta")
    private String periodoColeta;
    
    // Getters e Setters
    // ... (similar ao Model)
}
```

### Android - Atualizar Entity

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/entity/ColetaEntity.kt
@Entity(
    tableName = "coleta",
    indices = [
        Index(value = ["idPatrimonio"]),
        Index(value = ["idInventario"]),
        Index(value = ["sincronizado"]),
        Index(value = ["dataColeta"]),
        Index(value = ["metodoColeta"])  // NOVO
    ]
)
data class ColetaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // ... campos existentes ...
    
    // ADICIONAR:
    val tempoColetaSegundos: Int? = null,
    val tempoScanSegundos: Int? = null,
    val tempoPreenchimentoSegundos: Int? = null,
    val metodoColeta: String? = null,  // "QR_CODE", "MANUAL", "BUSCA", "SEM_ETIQUETA"
    val horaColeta: Int? = null,       // 0-23
    val diaSemana: Int? = null,        // 1-7 (Dom-Sab)
    val periodoColeta: String? = null  // "MANHA", "TARDE", "NOITE"
)
```

### Android - Criar Helper de Métricas

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/utils/MetricsHelper.kt
package com.inventario.mobile.utils

import android.util.Log
import java.util.Calendar

/**
 * Helper para coletar métricas de forma transparente
 */
object MetricsHelper {
    
    private const val TAG = "MetricsHelper"
    
    /**
     * Calcula horário da coleta
     */
    fun calcularHorario(): Triple<Int, Int, String> {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val dia = calendar.get(Calendar.DAY_OF_WEEK)
        val periodo = when (hora) {
            in 6..11 -> "MANHA"
            in 12..17 -> "TARDE"
            else -> "NOITE"
        }
        
        Log.d(TAG, "Horário calculado: hora=$hora, dia=$dia, periodo=$periodo")
        return Triple(hora, dia, periodo)
    }
    
    /**
     * Calcula tempo decorrido em segundos
     */
    fun calcularTempoSegundos(inicioMs: Long): Int {
        val fimMs = System.currentTimeMillis()
        val segundos = ((fimMs - inicioMs) / 1000).toInt()
        Log.d(TAG, "Tempo calculado: ${segundos}s")
        return segundos
    }
    
    /**
     * Formata tempo para exibição
     */
    fun formatarTempo(segundos: Int): String {
        return if (segundos < 60) {
            "${segundos}s"
        } else {
            val min = segundos / 60
            val seg = segundos % 60
            "${min}m ${seg}s"
        }
    }
}
```

### Android - Atualizar ViewModel de Coleta

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/coleta/ColetaViewModel.kt
@HiltViewModel
class ColetaViewModel @Inject constructor(
    private val registrarColetaUseCase: RegistrarColetaUseCase
) : ViewModel() {
    
    // Métricas de tempo
    private var inicioColeta: Long = 0
    private var inicioScan: Long = 0
    private var fimScan: Long = 0
    private var inicioPreenchimento: Long = 0
    
    // Método de coleta
    private var metodoColeta: String = "QR_CODE"
    
    /**
     * Inicia tracking de tempo da coleta
     */
    fun iniciarColeta() {
        inicioColeta = System.currentTimeMillis()
        Log.d(TAG, "Coleta iniciada: $inicioColeta")
    }
    
    /**
     * Inicia tracking de tempo do scan
     */
    fun iniciarScan() {
        inicioScan = System.currentTimeMillis()
        Log.d(TAG, "Scan iniciado: $inicioScan")
    }
    
    /**
     * Finaliza tracking de tempo do scan
     */
    fun finalizarScan() {
        fimScan = System.currentTimeMillis()
        Log.d(TAG, "Scan finalizado: $fimScan")
    }
    
    /**
     * Inicia tracking de tempo de preenchimento
     */
    fun iniciarPreenchimento() {
        inicioPreenchimento = System.currentTimeMillis()
        Log.d(TAG, "Preenchimento iniciado: $inicioPreenchimento")
    }
    
    /**
     * Define método de coleta
     */
    fun setMetodoColeta(metodo: String) {
        metodoColeta = metodo
        Log.d(TAG, "Método de coleta: $metodo")
    }
    
    /**
     * Registra coleta com métricas
     */
    fun registrarColeta(
        numeroPatrimonio: String,
        observacao: String?,
        // ... outros parâmetros ...
    ) {
        viewModelScope.launch {
            try {
                // Calcular métricas de tempo
                val tempoTotal = MetricsHelper.calcularTempoSegundos(inicioColeta)
                val tempoScan = if (inicioScan > 0 && fimScan > 0) {
                    ((fimScan - inicioScan) / 1000).toInt()
                } else null
                val tempoPreenchimento = if (inicioPreenchimento > 0) {
                    MetricsHelper.calcularTempoSegundos(inicioPreenchimento)
                } else null
                
                // Calcular horário
                val (hora, dia, periodo) = MetricsHelper.calcularHorario()
                
                // Criar coleta com métricas
                val coleta = Coleta(
                    numeroPatrimonio = numeroPatrimonio,
                    observacao = observacao,
                    // ... outros campos ...
                    
                    // Métricas
                    tempoColetaSegundos = tempoTotal,
                    tempoScanSegundos = tempoScan,
                    tempoPreenchimentoSegundos = tempoPreenchimento,
                    metodoColeta = metodoColeta,
                    horaColeta = hora,
                    diaSemana = dia,
                    periodoColeta = periodo
                )
                
                Log.d(TAG, "Coleta com métricas: tempo=${tempoTotal}s, metodo=$metodoColeta, periodo=$periodo")
                
                // Registrar coleta
                registrarColetaUseCase(coleta).fold(
                    onSuccess = { /* sucesso */ },
                    onFailure = { /* erro */ }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao registrar coleta com métricas", e)
                // Continuar mesmo se falhar a coleta de métricas
            }
        }
    }
}
```

### Android - Atualizar Activity de Coleta

```kotlin
// InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/coleta/ColetaActivity.kt
@AndroidEntryPoint
class ColetaActivity : AppCompatActivity() {
    
    private val viewModel: ColetaViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Iniciar tracking de tempo
        viewModel.iniciarColeta()
        
        setupUI()
    }
    
    private fun setupQRScanner() {
        btnScanQR.setOnClickListener {
            // Iniciar tracking de scan
            viewModel.iniciarScan()
            viewModel.setMetodoColeta("QR_CODE")
            
            // Abrir scanner
            startQRScanner()
        }
    }
    
    private fun onQRCodeScanned(code: String) {
        // Finalizar tracking de scan
        viewModel.finalizarScan()
        
        // Iniciar tracking de preenchimento
        viewModel.iniciarPreenchimento()
        
        // Processar código
        processarCodigoQR(code)
    }
    
    private fun setupManualInput() {
        btnColetaManual.setOnClickListener {
            viewModel.setMetodoColeta("MANUAL")
            viewModel.iniciarPreenchimento()
            
            // Mostrar formulário manual
            showManualForm()
        }
    }
    
    private fun onRegistrarColeta() {
        // Registrar com métricas
        viewModel.registrarColeta(
            numeroPatrimonio = edtNumero.text.toString(),
            observacao = edtObservacao.text.toString()
            // ... outros campos ...
        )
    }
}
```

---

## 📊 Migração do Banco de Dados

### Script de Migração

```sql
-- migration_v2.1_metricas.sql

-- 1. Adicionar colunas
ALTER TABLE TABELA_COLETA 
ADD COLUMN IF NOT EXISTS TEMPO_COLETA_SEGUNDOS INTEGER,
ADD COLUMN IF NOT EXISTS TEMPO_SCAN_SEGUNDOS INTEGER,
ADD COLUMN IF NOT EXISTS TEMPO_PREENCHIMENTO_SEGUNDOS INTEGER,
ADD COLUMN IF NOT EXISTS METODO_COLETA VARCHAR(20),
ADD COLUMN IF NOT EXISTS HORA_COLETA INTEGER,
ADD COLUMN IF NOT EXISTS DIA_SEMANA INTEGER,
ADD COLUMN IF NOT EXISTS PERIODO_COLETA VARCHAR(10);

-- 2. Adicionar comentários
COMMENT ON COLUMN TABELA_COLETA.TEMPO_COLETA_SEGUNDOS IS 'Tempo total da coleta em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_SCAN_SEGUNDOS IS 'Tempo do scan QR em segundos';
COMMENT ON COLUMN TABELA_COLETA.TEMPO_PREENCHIMENTO_SEGUNDOS IS 'Tempo de preenchimento em segundos';
COMMENT ON COLUMN TABELA_COLETA.METODO_COLETA IS 'QR_CODE, MANUAL, BUSCA, SEM_ETIQUETA';
COMMENT ON COLUMN TABELA_COLETA.HORA_COLETA IS 'Hora da coleta (0-23)';
COMMENT ON COLUMN TABELA_COLETA.DIA_SEMANA IS 'Dia da semana (1=Dom, 7=Sab)';
COMMENT ON COLUMN TABELA_COLETA.PERIODO_COLETA IS 'MANHA, TARDE, NOITE';

-- 3. Criar índices
CREATE INDEX IF NOT EXISTS idx_coleta_tempo ON TABELA_COLETA(TEMPO_COLETA_SEGUNDOS);
CREATE INDEX IF NOT EXISTS idx_coleta_metodo ON TABELA_COLETA(METODO_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_hora ON TABELA_COLETA(HORA_COLETA);
CREATE INDEX IF NOT EXISTS idx_coleta_periodo ON TABELA_COLETA(PERIODO_COLETA);

-- 4. Atualizar coletas existentes (opcional - preencher com valores padrão)
UPDATE TABELA_COLETA 
SET 
    METODO_COLETA = 'QR_CODE',
    HORA_COLETA = EXTRACT(HOUR FROM DATA_COLETA),
    DIA_SEMANA = EXTRACT(DOW FROM DATA_COLETA) + 1,
    PERIODO_COLETA = CASE 
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 6 AND 11 THEN 'MANHA'
        WHEN EXTRACT(HOUR FROM DATA_COLETA) BETWEEN 12 AND 17 THEN 'TARDE'
        ELSE 'NOITE'
    END
WHERE METODO_COLETA IS NULL;

-- 5. Verificar migração
SELECT 
    COUNT(*) as total_coletas,
    COUNT(TEMPO_COLETA_SEGUNDOS) as com_tempo,
    COUNT(METODO_COLETA) as com_metodo,
    COUNT(HORA_COLETA) as com_hora
FROM TABELA_COLETA;
```

---

## ✅ Checklist de Implementação

### Backend
- [ ] Executar script de migração do banco
- [ ] Atualizar model Coleta.java
- [ ] Atualizar ColetaDAO.java
- [ ] Atualizar MobileColetaDTO.java
- [ ] Testar inserção com novos campos
- [ ] Validar que campos nulos não quebram

### Android
- [ ] Atualizar ColetaEntity.kt
- [ ] Criar MetricsHelper.kt
- [ ] Atualizar ColetaViewModel.kt
- [ ] Atualizar ColetaActivity.kt
- [ ] Atualizar ManualCollectionActivity.kt
- [ ] Atualizar ItemSemEtiquetaActivity.kt
- [ ] Testar coleta completa
- [ ] Validar sincronização

### Testes
- [ ] Testar coleta via QR Code
- [ ] Testar coleta manual
- [ ] Testar item sem etiqueta
- [ ] Verificar métricas no banco
- [ ] Validar que métricas não impedem coleta
- [ ] Testar com falha de GPS
- [ ] Testar offline

---

## 🎯 Resultado Esperado

Após implementação, cada coleta terá:

```json
{
  "numeroPatrimonio": "12345",
  "observacao": "Item em bom estado",
  "tempoColetaSegundos": 18,
  "tempoScanSegundos": 3,
  "tempoPreenchimentoSegundos": 15,
  "metodoColeta": "QR_CODE",
  "horaColeta": 10,
  "diaSemana": 2,
  "periodoColeta": "MANHA"
}
```

**Benefícios:**
- ✅ Dados coletados automaticamente
- ✅ Usuário não percebe
- ✅ Não impacta UX
- ✅ Pronto para relatórios no desktop
- ✅ Base para provar ROI

---

## 📅 Próximos Passos

### Após Fase 1.1
1. **Fase 1.2:** Adicionar métricas de dispositivo
2. **Fase 1.3:** Adicionar métricas de GPS
3. **Fase 2:** Criar relatórios no desktop
4. **Fase 3:** Dashboard de métricas

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 🎯 Pronto para Implementação

**🚀 Vamos começar pela migração do banco de dados?**
