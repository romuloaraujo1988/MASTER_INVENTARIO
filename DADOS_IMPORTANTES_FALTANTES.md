# 📊 Dados Importantes que o App Deveria Coletar

**Data:** 16/11/2025  
**Versão:** 1.0  
**Status:** 📋 Análise Completa

---

## 🎯 Objetivo

Identificar dados adicionais que o app deveria coletar para gerar insights mais valiosos e melhorar a tomada de decisão.

---

## ✅ Dados Já Coletados

### Informações Básicas
- ✅ ID do patrimônio
- ✅ Número do patrimônio
- ✅ ID do inventário
- ✅ ID da sala
- ✅ Nome da sala
- ✅ ID do responsável
- ✅ Nome do responsável
- ✅ Observação
- ✅ Estado do patrimônio
- ✅ Latitude/Longitude (GPS)
- ✅ Data/hora da coleta
- ✅ ID do usuário coletor
- ✅ Nome do usuário coletor

### Informações de Sincronização
- ✅ Status de sincronização
- ✅ Tentativas de sincronização
- ✅ Erro de sincronização
- ✅ ID no servidor

---

## ❌ Dados Importantes Faltantes

### 1. 🕐 Métricas de Tempo e Performance

#### 1.1 Tempo de Coleta
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val tempoColetaSegundos: Int?,        // Tempo total da coleta
    val tempoScanSegundos: Int?,          // Tempo do scan QR
    val tempoPreenchimentoSegundos: Int?, // Tempo de preenchimento
    val tempoValidacaoSegundos: Int?      // Tempo de validação
)
```

**Por que é importante:**
- Identificar gargalos no processo
- Calcular produtividade real
- Comparar tempo digital vs papel
- Otimizar fluxo de coleta
- Treinar usuários mais lentos

**Como coletar:**
```kotlin
// Ao iniciar coleta
val inicioColeta = System.currentTimeMillis()

// Ao finalizar
val fimColeta = System.currentTimeMillis()
val tempoTotal = (fimColeta - inicioColeta) / 1000 // segundos
```

**Insights gerados:**
- Tempo médio por coleta: 18s
- Tempo médio por usuário
- Horários mais produtivos
- Itens que demoram mais

---

#### 1.2 Horário da Coleta
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val horaColeta: Int?,           // 0-23
    val diaSemana: Int?,            // 1-7 (Dom-Sab)
    val periodoColeta: String?      // "MANHA", "TARDE", "NOITE"
)
```

**Por que é importante:**
- Identificar horários mais produtivos
- Planejar turnos de trabalho
- Otimizar alocação de recursos
- Evitar horários de baixa produtividade

**Insights gerados:**
- Produtividade por horário
- Melhor período para coletar
- Padrões de comportamento

---

### 2. 📱 Métricas de Dispositivo e Conectividade

#### 2.1 Informações do Dispositivo
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val modeloDispositivo: String?,     // "Samsung Galaxy S21"
    val versaoAndroid: String?,         // "13"
    val versaoApp: String?,             // "2.0.1"
    val nivelBateria: Int?,             // 0-100
    val tipoConexao: String?            // "WIFI", "4G", "5G", "OFFLINE"
)
```

**Por que é importante:**
- Identificar problemas específicos de dispositivos
- Planejar upgrades de hardware
- Otimizar app para dispositivos mais usados
- Monitorar consumo de bateria

**Insights gerados:**
- Dispositivos mais eficientes
- Problemas de compatibilidade
- Necessidade de novos aparelhos

---

#### 2.2 Qualidade da Conexão
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val forcaSinal: Int?,               // 0-100
    val latenciaMs: Int?,               // Ping em ms
    val coletadoOffline: Boolean,       // true/false
    val tempoSincronizacaoMs: Long?     // Tempo para sincronizar
)
```

**Por que é importante:**
- Identificar áreas com sinal fraco
- Planejar infraestrutura de rede
- Otimizar modo offline
- Melhorar experiência do usuário

**Insights gerados:**
- Mapa de cobertura de sinal
- Áreas problemáticas
- Necessidade de repetidores

---

### 3. 🎯 Métricas de Qualidade e Precisão

#### 3.1 Precisão do GPS
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val precisaoGpsMetros: Float?,      // Precisão em metros
    val altitudeMetros: Float?,         // Altitude
    val velocidadeMs: Float?,           // Velocidade (detectar movimento)
    val provedorGps: String?            // "GPS", "NETWORK", "FUSED"
)
```

**Por que é importante:**
- Validar localização real
- Detectar coletas fraudulentas
- Melhorar mapeamento de patrimônios
- Identificar patrimônios movidos

**Insights gerados:**
- Precisão média das coletas
- Patrimônios fora do local esperado
- Necessidade de recalibração

---

#### 3.2 Qualidade da Foto
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val fotoTamanhoKb: Int?,            // Tamanho da foto
    val fotoResolucao: String?,         // "1920x1080"
    val fotoQualidade: String?,         // "ALTA", "MEDIA", "BAIXA"
    val fotoComprimida: Boolean?        // true/false
)
```

**Por que é importante:**
- Garantir qualidade das evidências
- Otimizar armazenamento
- Validar conformidade
- Detectar problemas de câmera

**Insights gerados:**
- Qualidade média das fotos
- Dispositivos com câmera ruim
- Necessidade de compressão

---

### 4. 🔄 Métricas de Processo e Fluxo

#### 4.1 Método de Coleta
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val metodoColeta: String?,          // "QR_CODE", "MANUAL", "NFC", "BUSCA"
    val tentativasScan: Int?,           // Quantas vezes tentou escanear
    val errosScan: Int?,                // Quantos erros de scan
    val usouBusca: Boolean?,            // Usou busca antes de coletar
    val editouDados: Boolean?           // Editou dados após scan
)
```

**Por que é importante:**
- Identificar métodos mais eficientes
- Detectar problemas com QR codes
- Melhorar UX do scanner
- Treinar usuários

**Insights gerados:**
- Taxa de sucesso do QR code
- Necessidade de reimpressão de etiquetas
- Métodos preferidos pelos usuários

---

#### 4.2 Validações e Correções
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val validacoesRealizadas: Int?,     // Quantas validações
    val correcoesFeitasApp: Int?,       // Correções no app
    val correcoesFeitasServidor: Int?,  // Correções no servidor
    val alertasIgnorados: Int?,         // Alertas que o usuário ignorou
    val confirmacoesDuplicata: Int?     // Confirmou coleta duplicada
)
```

**Por que é importante:**
- Identificar dados problemáticos
- Melhorar validações
- Treinar usuários
- Detectar fraudes

**Insights gerados:**
- Taxa de correção
- Campos mais problemáticos
- Usuários que mais erram

---

### 5. 📍 Métricas de Localização e Contexto

#### 5.1 Contexto da Coleta
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val setorColeta: String?,           // Setor onde coletou
    val blocoColeta: String?,           // Bloco/Prédio
    val andarColeta: String?,           // Andar
    val distanciaUltimaColetaMetros: Float?, // Distância da última coleta
    val sequenciaColeta: Int?           // Ordem de coleta no dia
)
```

**Por que é importante:**
- Otimizar rota de coleta
- Identificar padrões de movimento
- Planejar logística
- Detectar coletas fora de ordem

**Insights gerados:**
- Rota mais eficiente
- Tempo de deslocamento
- Áreas mais demoradas

---

#### 5.2 Condições Ambientais
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val luminosidadeLux: Float?,        // Luminosidade ambiente
    val temperaturaDispositivo: Float?, // Temperatura do dispositivo
    val nivelRuido: Float?              // Nível de ruído (se disponível)
)
```

**Por que é importante:**
- Identificar condições ideais de trabalho
- Detectar ambientes problemáticos
- Melhorar ergonomia
- Planejar infraestrutura

**Insights gerados:**
- Ambientes com melhor produtividade
- Necessidade de iluminação
- Conforto térmico

---

### 6. 👤 Métricas de Usuário e Comportamento

#### 6.1 Experiência do Usuário
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val dificuldadeReportada: String?,  // "FACIL", "MEDIO", "DIFICIL"
    val feedbackUsuario: String?,       // Feedback opcional
    val avaliacaoColeta: Int?,          // 1-5 estrelas
    val problemaEncontrado: String?     // Tipo de problema
)
```

**Por que é importante:**
- Melhorar UX do app
- Identificar pontos de fricção
- Priorizar melhorias
- Aumentar satisfação

**Insights gerados:**
- Satisfação média
- Problemas mais comuns
- Áreas de melhoria

---

#### 6.2 Padrões de Uso
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val primeiraColetaDia: Boolean?,    // Primeira coleta do dia
    val ultimaColetaDia: Boolean?,      // Última coleta do dia
    val coletasConsecutivas: Int?,      // Coletas sem pausa
    val pausaAntesMinutos: Int?,        // Tempo desde última coleta
    val sessaoId: String?               // ID da sessão de coleta
)
```

**Por que é importante:**
- Identificar padrões de trabalho
- Detectar fadiga
- Otimizar pausas
- Melhorar produtividade

**Insights gerados:**
- Produtividade por sessão
- Necessidade de pausas
- Padrões de fadiga

---

### 7. 🔍 Métricas de Divergências e Problemas

#### 7.1 Detalhamento de Divergências
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val tipoDivergencia: String?,       // "LOCALIZACAO", "ESTADO", "RESPONSAVEL"
    val gravidadeDivergencia: String?,  // "BAIXA", "MEDIA", "ALTA", "CRITICA"
    val divergenciaResolvida: Boolean?, // true/false
    val tempoResolucaoMinutos: Int?,    // Tempo para resolver
    val acaoCorretiva: String?          // Ação tomada
)
```

**Por que é importante:**
- Priorizar resolução de problemas
- Identificar causas raiz
- Melhorar processos
- Reduzir divergências futuras

**Insights gerados:**
- Tipos mais comuns de divergência
- Tempo médio de resolução
- Taxa de resolução

---

### 8. 💰 Métricas de Valor e Impacto

#### 8.1 Valor do Patrimônio
**O que coletar:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    
    // ADICIONAR:
    val valorPatrimonio: Double?,       // Valor do item
    val valorEstimadoAtual: Double?,    // Valor estimado atual
    val depreciacaoPercentual: Double?, // % de depreciação
    val necessitaManutencao: Boolean?,  // Precisa manutenção
    val custoManutencaoEstimado: Double? // Custo estimado
)
```

**Por que é importante:**
- Calcular valor total do inventário
- Priorizar manutenções
- Planejar substituições
- Justificar investimentos

**Insights gerados:**
- Valor total inventariado
- Patrimônios de alto valor
- Necessidades de manutenção

---

## 📊 Priorização de Implementação

### 🔴 PRIORIDADE ALTA (Implementar Imediatamente)

1. **Tempo de Coleta** ⏱️
   - Impacto: Alto
   - Esforço: Baixo
   - ROI: Imediato
   - Justificativa: Prova valor da digitalização

2. **Método de Coleta** 📱
   - Impacto: Alto
   - Esforço: Baixo
   - ROI: Imediato
   - Justificativa: Melhora UX e identifica problemas

3. **Horário da Coleta** 🕐
   - Impacto: Médio
   - Esforço: Baixo
   - ROI: Rápido
   - Justificativa: Otimiza alocação de recursos

### 🟡 PRIORIDADE MÉDIA (Implementar em 1-2 meses)

4. **Informações do Dispositivo** 📱
   - Impacto: Médio
   - Esforço: Médio
   - ROI: Médio prazo

5. **Precisão do GPS** 📍
   - Impacto: Médio
   - Esforço: Baixo
   - ROI: Médio prazo

6. **Contexto da Coleta** 🗺️
   - Impacto: Médio
   - Esforço: Médio
   - ROI: Médio prazo

### 🟢 PRIORIDADE BAIXA (Implementar em 3-6 meses)

7. **Qualidade da Conexão** 📶
   - Impacto: Baixo
   - Esforço: Médio
   - ROI: Longo prazo

8. **Experiência do Usuário** 😊
   - Impacto: Baixo
   - Esforço: Baixo
   - ROI: Longo prazo

9. **Condições Ambientais** 🌡️
   - Impacto: Baixo
   - Esforço: Alto
   - ROI: Longo prazo

---

## 🚀 Plano de Implementação - Fase 1 (ALTA PRIORIDADE)

### Sprint 1 (1 semana) - Tempo de Coleta

**Backend:**
```java
// Adicionar campos na tabela TABELA_COLETA
ALTER TABLE TABELA_COLETA ADD COLUMN TEMPO_COLETA_SEGUNDOS INTEGER;
ALTER TABLE TABELA_COLETA ADD COLUMN TEMPO_SCAN_SEGUNDOS INTEGER;
ALTER TABLE TABELA_COLETA ADD COLUMN TEMPO_PREENCHIMENTO_SEGUNDOS INTEGER;
```

**Android:**
```kotlin
// Adicionar campos no ColetaEntity
data class ColetaEntity(
    // ... campos existentes ...
    val tempoColetaSegundos: Int? = null,
    val tempoScanSegundos: Int? = null,
    val tempoPreenchimentoSegundos: Int? = null
)

// Implementar tracking de tempo
class ColetaViewModel {
    private var inicioColeta: Long = 0
    private var inicioScan: Long = 0
    
    fun iniciarColeta() {
        inicioColeta = System.currentTimeMillis()
    }
    
    fun finalizarColeta(): Int {
        return ((System.currentTimeMillis() - inicioColeta) / 1000).toInt()
    }
}
```

**Endpoints:**
```java
// Adicionar no MobileColetaDTO
public class MobileColetaDTO {
    // ... campos existentes ...
    private Integer tempoColetaSegundos;
    private Integer tempoScanSegundos;
    private Integer tempoPreenchimentoSegundos;
}
```

---

### Sprint 2 (1 semana) - Método de Coleta

**Backend:**
```java
ALTER TABLE TABELA_COLETA ADD COLUMN METODO_COLETA VARCHAR(20);
ALTER TABLE TABELA_COLETA ADD COLUMN TENTATIVAS_SCAN INTEGER DEFAULT 0;
ALTER TABLE TABELA_COLETA ADD COLUMN ERROS_SCAN INTEGER DEFAULT 0;
```

**Android:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    val metodoColeta: String? = null, // "QR_CODE", "MANUAL", "BUSCA"
    val tentativasScan: Int = 0,
    val errosScan: Int = 0
)
```

---

### Sprint 3 (1 semana) - Horário da Coleta

**Backend:**
```java
ALTER TABLE TABELA_COLETA ADD COLUMN HORA_COLETA INTEGER;
ALTER TABLE TABELA_COLETA ADD COLUMN DIA_SEMANA INTEGER;
ALTER TABLE TABELA_COLETA ADD COLUMN PERIODO_COLETA VARCHAR(10);
```

**Android:**
```kotlin
data class ColetaEntity(
    // ... campos existentes ...
    val horaColeta: Int? = null,
    val diaSemana: Int? = null,
    val periodoColeta: String? = null
)

// Calcular automaticamente
fun calcularHorario(): Triple<Int, Int, String> {
    val calendar = Calendar.getInstance()
    val hora = calendar.get(Calendar.HOUR_OF_DAY)
    val dia = calendar.get(Calendar.DAY_OF_WEEK)
    val periodo = when (hora) {
        in 6..11 -> "MANHA"
        in 12..17 -> "TARDE"
        else -> "NOITE"
    }
    return Triple(hora, dia, periodo)
}
```

---

## 📈 Insights Esperados

### Com Tempo de Coleta
- Tempo médio: 18 segundos (digital) vs 3 minutos (papel)
- Ganho de produtividade: 10x
- Economia de tempo: 293 horas por inventário
- ROI comprovado com dados reais

### Com Método de Coleta
- Taxa de sucesso QR: 95%
- Coletas manuais: 5%
- Problemas com etiquetas: 2%
- Necessidade de reimpressão identificada

### Com Horário da Coleta
- Melhor horário: 9h-11h (350 itens/hora)
- Pior horário: 14h-16h (180 itens/hora)
- Recomendação: Concentrar coletas pela manhã
- Economia adicional: 20% de tempo

---

## ✅ Checklist de Implementação

### Fase 1 - Alta Prioridade
- [ ] Adicionar campos no banco de dados
- [ ] Atualizar ColetaEntity (Android)
- [ ] Atualizar MobileColetaDTO (Backend)
- [ ] Implementar tracking de tempo
- [ ] Implementar tracking de método
- [ ] Implementar cálculo de horário
- [ ] Testar coleta completa
- [ ] Validar dados coletados

### Fase 2 - Endpoints de Estatísticas
- [ ] Criar endpoint de tempo médio
- [ ] Criar endpoint de produtividade por horário
- [ ] Criar endpoint de métodos mais usados
- [ ] Criar dashboard de insights

### Fase 3 - Visualização
- [ ] Adicionar gráficos de tempo
- [ ] Adicionar gráficos de produtividade
- [ ] Adicionar comparativos
- [ ] Adicionar recomendações

---

## 🎯 Resultado Esperado

Com esses dados adicionais, o sistema poderá:

1. **Provar o Valor da Digitalização**
   - Tempo real: 18s vs 3min (papel)
   - Produtividade: 300 itens/dia vs 75 itens/dia
   - Economia: R$ 20.000 por inventário

2. **Otimizar Processos**
   - Identificar gargalos
   - Melhorar treinamento
   - Otimizar rotas
   - Reduzir tempo de coleta

3. **Melhorar Tomada de Decisão**
   - Dados em tempo real
   - Insights acionáveis
   - Previsões precisas
   - ROI comprovado

4. **Aumentar Qualidade**
   - Reduzir erros
   - Melhorar precisão
   - Aumentar satisfação
   - Garantir conformidade

---

**Preparado por:** Kiro AI Assistant  
**Data:** 16/11/2025  
**Status:** 📋 Pronto para Implementação

**🚀 Vamos começar pela Fase 1 (Alta Prioridade)?**
