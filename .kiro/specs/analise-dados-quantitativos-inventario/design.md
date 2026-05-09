# Análise de Dados Quantitativos do Inventário Patrimonial - Design

## 1. Arquitetura da Solução

### 1.1 Visão Geral
Sistema de análise de dados em 4 camadas:
1. **Extração**: Queries SQL otimizadas no PostgreSQL
2. **Processamento**: Scripts Python/Pandas para limpeza e transformação
3. **Análise**: Estatísticas descritivas e inferenciais
4. **Visualização**: Gráficos e relatórios formatados

### 1.2 Fluxo de Dados
```
PostgreSQL → CSV Bruto → Normalização → Dataset Limpo → Análises → Relatórios
```

---

## 2. Metodologias de Extração de Dados

### 2.1 Query Principal - Estado de Conservação
```sql
-- Query otimizada para análise de estado de conservação
CREATE TEMP VIEW vw_analise_estado AS
SELECT 
    c.id as coleta_id,
    c.id_patrimonio,
    p.numero as numero_patrimonio,
    p.descricao,
    p.categoria,
    p.valor_aquisicao,
    p.valor_depreciado,
    p.data_entrada,
    -- Normalização de estado
    CASE 
        WHEN c.estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
        WHEN c.estado_encontrado IN ('RECUPERAVEL', 'RECUPERÁVEL') THEN 'RECUPERÁVEL'
        WHEN c.estado_encontrado = 'BOM' THEN 'BOM'
        WHEN c.estado_encontrado = 'OCIOSO' THEN 'OCIOSO'
        ELSE 'OUTROS'
    END as estado_normalizado,
    c.estado_encontrado as estado_original,
    p.estado_conservacao as estado_cadastrado,
    -- Localização
    s.descricao as sala,
    s.tipo_sala,
    s.bloco,
    s.andar,
    s.area_m2,
    -- Métricas de coleta
    c.tempo_coleta_segundos,
    c.tempo_scan_segundos,
    c.tempo_preenchimento_segundos,
    c.metodo_coleta,
    c.qualidade_etiqueta,
    c.divergencia,
    c.sem_etiqueta,
    -- Temporal
    c.data_coleta,
    c.periodo_coleta,
    c.hora_coleta,
    c.dia_semana
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_sala s ON p.id_sala = s.id_sala
WHERE c.id_inventario = (SELECT id FROM tabela_inventario WHERE ano = 2024 AND nome LIKE '%2025%' LIMIT 1);
```


### 2.2 Query - Análise de Divergências
```sql
-- Análise detalhada de divergências
CREATE TEMP VIEW vw_analise_divergencias AS
SELECT 
    c.id,
    p.numero,
    p.descricao,
    c.localizacao_atual,
    c.localizacao_encontrada,
    c.divergencia,
    c.motivo_divergencia,
    s_cadastrada.descricao as sala_cadastrada,
    s_encontrada.descricao as sala_encontrada,
    CASE 
        WHEN c.localizacao_atual != c.localizacao_encontrada THEN 'DIVERGENCIA_LOCALIZACAO'
        WHEN c.estado_encontrado != p.estado_conservacao THEN 'DIVERGENCIA_ESTADO'
        ELSE 'SEM_DIVERGENCIA'
    END as tipo_divergencia,
    c.data_coleta
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
LEFT JOIN tabela_sala s_cadastrada ON p.id_sala = s_cadastrada.id_sala
LEFT JOIN tabela_sala s_encontrada ON c.localizacao_encontrada = s_encontrada.descricao
WHERE c.divergencia = true;
```

### 2.3 Query - Métricas de Eficiência
```sql
-- Análise de tempo e eficiência por método
SELECT 
    metodo_coleta,
    qualidade_etiqueta,
    COUNT(*) as total_coletas,
    ROUND(AVG(tempo_coleta_segundos), 2) as tempo_medio,
    ROUND(STDDEV(tempo_coleta_segundos), 2) as desvio_padrao,
    ROUND(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY tempo_coleta_segundos), 2) as mediana,
    ROUND(PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY tempo_coleta_segundos), 2) as q1,
    ROUND(PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY tempo_coleta_segundos), 2) as q3,
    MIN(tempo_coleta_segundos) as tempo_min,
    MAX(tempo_coleta_segundos) as tempo_max
FROM tabela_coleta
WHERE tempo_coleta_segundos IS NOT NULL
GROUP BY metodo_coleta, qualidade_etiqueta
ORDER BY metodo_coleta, qualidade_etiqueta;
```

### 2.4 Query - Análise Espacial
```sql
-- Densidade e distribuição espacial
SELECT 
    s.descricao as sala,
    s.tipo_sala,
    s.bloco,
    s.andar,
    s.area_m2,
    COUNT(c.id) as total_coletas,
    ROUND(COUNT(c.id)::numeric / NULLIF(s.area_m2, 0), 2) as densidade_itens_m2,
    COUNT(CASE WHEN c.estado_encontrado = 'BOM' THEN 1 END) as qtd_bom,
    COUNT(CASE WHEN c.estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 1 END) as qtd_irrecuperavel,
    COUNT(CASE WHEN c.divergencia = true THEN 1 END) as qtd_divergencias,
    ROUND(AVG(c.tempo_coleta_segundos), 2) as tempo_medio_coleta,
    SUM(p.valor_aquisicao) as valor_total_sala,
    SUM(p.valor_depreciado) as valor_depreciado_sala
FROM tabela_sala s
INNER JOIN tabela_patrimonio p ON s.id_sala = p.id_sala
INNER JOIN tabela_coleta c ON p.id = c.id_patrimonio
WHERE c.id_inventario = (SELECT id FROM tabela_inventario WHERE ano = 2024 AND nome LIKE '%2025%' LIMIT 1)
GROUP BY s.id_sala, s.descricao, s.tipo_sala, s.bloco, s.andar, s.area_m2
ORDER BY total_coletas DESC;
```


### 2.5 Query - Análise Financeira
```sql
-- Valor patrimonial por estado de conservação
SELECT 
    CASE 
        WHEN c.estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
        WHEN c.estado_encontrado IN ('RECUPERAVEL', 'RECUPERÁVEL') THEN 'RECUPERÁVEL'
        WHEN c.estado_encontrado = 'BOM' THEN 'BOM'
        WHEN c.estado_encontrado = 'OCIOSO' THEN 'OCIOSO'
        ELSE 'OUTROS'
    END as estado,
    COUNT(*) as quantidade,
    SUM(p.valor_aquisicao) as valor_aquisicao_total,
    SUM(p.valor_depreciado) as valor_depreciado_total,
    ROUND(AVG(p.valor_aquisicao), 2) as valor_medio_aquisicao,
    ROUND(AVG(p.valor_depreciado), 2) as valor_medio_depreciado,
    ROUND(AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, p.data_entrada))), 1) as idade_media_anos,
    ROUND((SUM(p.valor_depreciado) / NULLIF(SUM(p.valor_aquisicao), 0)) * 100, 2) as taxa_depreciacao_percentual
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
WHERE c.estado_encontrado IS NOT NULL
  AND p.valor_aquisicao IS NOT NULL
  AND p.valor_depreciado IS NOT NULL
GROUP BY estado
ORDER BY quantidade DESC;
```

---

## 3. Processamento e Normalização

### 3.1 Script Python - Normalização de Dados
```python
import pandas as pd
import numpy as np
from datetime import datetime

def normalizar_estado_conservacao(df):
    """Normaliza estados de conservação com inconsistências ortográficas"""
    mapeamento = {
        'IRRECUPERAVEL': 'IRRECUPERÁVEL',
        'IRRECUPERÁVEL': 'IRRECUPERÁVEL',
        'RECUPERAVEL': 'RECUPERÁVEL',
        'RECUPERÁVEL': 'RECUPERÁVEL',
        'BOM': 'BOM',
        'OCIOSO': 'OCIOSO',
        'PENDENTE': 'PENDENTE',
        'N/A': 'NÃO INFORMADO',
        'COLETADO': 'OUTROS'
    }
    
    df['estado_normalizado'] = df['estado_encontrado'].map(mapeamento).fillna('OUTROS')
    return df

def calcular_metricas_temporais(df):
    """Calcula métricas temporais derivadas"""
    df['data_coleta'] = pd.to_datetime(df['data_coleta'])
    df['ano_coleta'] = df['data_coleta'].dt.year
    df['mes_coleta'] = df['data_coleta'].dt.month
    df['dia_mes'] = df['data_coleta'].dt.day
    df['hora_coleta'] = df['data_coleta'].dt.hour
    df['dia_semana'] = df['data_coleta'].dt.dayofweek  # 0=Segunda
    
    # Classificar período do dia
    df['periodo_dia'] = pd.cut(df['hora_coleta'], 
                                bins=[0, 6, 12, 18, 24],
                                labels=['MADRUGADA', 'MANHÃ', 'TARDE', 'NOITE'])
    return df

def calcular_idade_patrimonio(df):
    """Calcula idade do patrimônio em anos"""
    df['data_entrada'] = pd.to_datetime(df['data_entrada'])
    df['idade_anos'] = (datetime.now() - df['data_entrada']).dt.days / 365.25
    return df

def identificar_outliers_tempo(df, coluna='tempo_coleta_segundos'):
    """Identifica outliers usando IQR"""
    Q1 = df[coluna].quantile(0.25)
    Q3 = df[coluna].quantile(0.75)
    IQR = Q3 - Q1
    limite_inferior = Q1 - 1.5 * IQR
    limite_superior = Q3 + 1.5 * IQR
    
    df['outlier_tempo'] = (df[coluna] < limite_inferior) | (df[coluna] > limite_superior)
    return df
```


### 3.2 Script Python - Análises Estatísticas
```python
import scipy.stats as stats
from scipy.stats import chi2_contingency, f_oneway

def analise_descritiva_estado(df):
    """Estatísticas descritivas por estado de conservação"""
    resultado = df.groupby('estado_normalizado').agg({
        'coleta_id': 'count',
        'valor_aquisicao': ['sum', 'mean', 'median', 'std'],
        'valor_depreciado': ['sum', 'mean', 'median', 'std'],
        'tempo_coleta_segundos': ['mean', 'median', 'std'],
        'divergencia': 'sum',
        'idade_anos': ['mean', 'median']
    }).round(2)
    
    # Calcular percentuais
    total = df.shape[0]
    resultado['percentual'] = (resultado[('coleta_id', 'count')] / total * 100).round(2)
    
    return resultado

def teste_qui_quadrado_estado_sala(df):
    """Teste qui-quadrado: independência entre estado e tipo de sala"""
    tabela_contingencia = pd.crosstab(df['estado_normalizado'], df['tipo_sala'])
    chi2, p_valor, gl, freq_esperada = chi2_contingency(tabela_contingencia)
    
    return {
        'chi2': chi2,
        'p_valor': p_valor,
        'graus_liberdade': gl,
        'significativo': p_valor < 0.05
    }

def anova_tempo_por_metodo(df):
    """ANOVA: diferença de tempo entre métodos de coleta"""
    grupos = [grupo['tempo_coleta_segundos'].dropna() 
              for nome, grupo in df.groupby('metodo_coleta')]
    
    f_stat, p_valor = f_oneway(*grupos)
    
    return {
        'f_statistic': f_stat,
        'p_valor': p_valor,
        'significativo': p_valor < 0.05
    }

def intervalo_confianca_95(serie):
    """Calcula intervalo de confiança de 95%"""
    media = serie.mean()
    erro_padrao = serie.sem()
    ic = stats.t.interval(0.95, len(serie)-1, loc=media, scale=erro_padrao)
    return {'media': media, 'ic_inferior': ic[0], 'ic_superior': ic[1]}
```

---

## 4. Visualizações e Gráficos

### 4.1 Gráfico - Distribuição de Estados
```python
import matplotlib.pyplot as plt
import seaborn as sns

def grafico_distribuicao_estados(df):
    """Gráfico de pizza com distribuição de estados"""
    contagem = df['estado_normalizado'].value_counts()
    
    fig, ax = plt.subplots(figsize=(10, 8))
    colors = ['#2ecc71', '#e74c3c', '#f39c12', '#95a5a6', '#3498db']
    
    wedges, texts, autotexts = ax.pie(contagem, 
                                        labels=contagem.index,
                                        autopct='%1.1f%%',
                                        colors=colors,
                                        startangle=90)
    
    ax.set_title('Distribuição de Estados de Conservação\nInventário 2024/2025', 
                 fontsize=16, fontweight='bold')
    
    plt.tight_layout()
    plt.savefig('distribuicao_estados.png', dpi=300, bbox_inches='tight')
    return fig

def grafico_tempo_por_metodo(df):
    """Boxplot de tempo por método de coleta"""
    fig, ax = plt.subplots(figsize=(12, 6))
    
    df_filtrado = df[df['tempo_coleta_segundos'].notna()]
    sns.boxplot(data=df_filtrado, x='metodo_coleta', y='tempo_coleta_segundos', ax=ax)
    
    ax.set_title('Tempo de Coleta por Método', fontsize=16, fontweight='bold')
    ax.set_xlabel('Método de Coleta', fontsize=12)
    ax.set_ylabel('Tempo (segundos)', fontsize=12)
    ax.grid(axis='y', alpha=0.3)
    
    plt.tight_layout()
    plt.savefig('tempo_por_metodo.png', dpi=300, bbox_inches='tight')
    return fig
```


### 4.2 Gráfico - Mapa de Calor de Divergências
```python
def mapa_calor_divergencias(df):
    """Mapa de calor: divergências por sala e estado"""
    pivot = df.pivot_table(values='divergencia', 
                           index='sala', 
                           columns='estado_normalizado',
                           aggfunc='sum',
                           fill_value=0)
    
    # Filtrar top 20 salas
    pivot = pivot.loc[pivot.sum(axis=1).nlargest(20).index]
    
    fig, ax = plt.subplots(figsize=(14, 10))
    sns.heatmap(pivot, annot=True, fmt='g', cmap='YlOrRd', ax=ax)
    
    ax.set_title('Mapa de Calor: Divergências por Sala e Estado', 
                 fontsize=16, fontweight='bold')
    ax.set_xlabel('Estado de Conservação', fontsize=12)
    ax.set_ylabel('Sala', fontsize=12)
    
    plt.tight_layout()
    plt.savefig('mapa_calor_divergencias.png', dpi=300, bbox_inches='tight')
    return fig

def grafico_densidade_espacial(df):
    """Gráfico de barras: densidade de itens por sala"""
    densidade = df.groupby('sala').agg({
        'coleta_id': 'count',
        'area_m2': 'first'
    })
    densidade['densidade'] = densidade['coleta_id'] / densidade['area_m2']
    densidade = densidade.nlargest(15, 'densidade')
    
    fig, ax = plt.subplots(figsize=(12, 8))
    densidade['densidade'].plot(kind='barh', ax=ax, color='steelblue')
    
    ax.set_title('Densidade de Patrimônios por Sala (itens/m²)', 
                 fontsize=16, fontweight='bold')
    ax.set_xlabel('Densidade (itens/m²)', fontsize=12)
    ax.set_ylabel('Sala', fontsize=12)
    ax.grid(axis='x', alpha=0.3)
    
    plt.tight_layout()
    plt.savefig('densidade_espacial.png', dpi=300, bbox_inches='tight')
    return fig
```

---

## 5. Geração de Relatórios

### 5.1 Tabela LaTeX - Estatísticas Descritivas
```python
def gerar_tabela_latex_estados(df):
    """Gera tabela LaTeX para artigo científico"""
    stats = df.groupby('estado_normalizado').agg({
        'coleta_id': 'count',
        'valor_aquisicao': ['sum', 'mean'],
        'tempo_coleta_segundos': ['mean', 'std']
    }).round(2)
    
    latex = r"""
\begin{table}[htbp]
\centering
\caption{Estatísticas Descritivas por Estado de Conservação}
\label{tab:estados_conservacao}
\begin{tabular}{lrrrr}
\toprule
\textbf{Estado} & \textbf{Quantidade} & \textbf{Valor Total (R\$)} & \textbf{Tempo Médio (s)} & \textbf{Desvio Padrão (s)} \\
\midrule
"""
    
    for estado, row in stats.iterrows():
        latex += f"{estado} & {row[('coleta_id', 'count')]:,.0f} & "
        latex += f"{row[('valor_aquisicao', 'sum')]:,.2f} & "
        latex += f"{row[('tempo_coleta_segundos', 'mean')]:.2f} & "
        latex += f"{row[('tempo_coleta_segundos', 'std')]:.2f} \\\\\n"
    
    latex += r"""
\bottomrule
\end{tabular}
\end{table}
"""
    
    return latex
```


### 5.2 Relatório Executivo - Template
```python
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, Image
from reportlab.lib.styles import getSampleStyleSheet

def gerar_relatorio_executivo(df, output_path='relatorio_executivo.pdf'):
    """Gera relatório executivo em PDF"""
    doc = SimpleDocTemplate(output_path, pagesize=A4)
    story = []
    styles = getSampleStyleSheet()
    
    # Título
    titulo = Paragraph("Relatório Executivo - Inventário Patrimonial 2024/2025", 
                       styles['Title'])
    story.append(titulo)
    story.append(Spacer(1, 20))
    
    # Resumo Executivo
    resumo = f"""
    <b>Resumo Executivo</b><br/>
    <br/>
    Total de Coletas: {len(df):,}<br/>
    Patrimônios Únicos: {df['id_patrimonio'].nunique():,}<br/>
    Taxa de Localização: {(df['id_patrimonio'].nunique() / 11570 * 100):.2f}%<br/>
    Divergências Encontradas: {df['divergencia'].sum():,} ({df['divergencia'].mean()*100:.2f}%)<br/>
    <br/>
    <b>Estado de Conservação:</b><br/>
    BOM: {(df['estado_normalizado']=='BOM').sum():,} ({(df['estado_normalizado']=='BOM').mean()*100:.2f}%)<br/>
    IRRECUPERÁVEL: {(df['estado_normalizado']=='IRRECUPERÁVEL').sum():,} ({(df['estado_normalizado']=='IRRECUPERÁVEL').mean()*100:.2f}%)<br/>
    RECUPERÁVEL: {(df['estado_normalizado']=='RECUPERÁVEL').sum():,} ({(df['estado_normalizado']=='RECUPERÁVEL').mean()*100:.2f}%)<br/>
    OCIOSO: {(df['estado_normalizado']=='OCIOSO').sum():,} ({(df['estado_normalizado']=='OCIOSO').mean()*100:.2f}%)<br/>
    """
    
    story.append(Paragraph(resumo, styles['Normal']))
    story.append(Spacer(1, 20))
    
    # Adicionar gráficos
    story.append(Image('distribuicao_estados.png', width=400, height=300))
    
    doc.build(story)
    return output_path
```

---

## 6. Metodologia de Cálculo - Detalhamento

### 6.1 Taxa de Deterioração
```
Taxa de Deterioração = (Qtd IRRECUPERÁVEL / Total Coletado) × 100

Exemplo:
Taxa = (1.130 / 8.639) × 100 = 13.08%
```

### 6.2 Índice de Eficiência de Coleta
```
IEC = (Coletas Realizadas / Tempo Total em Horas) × Fator de Qualidade

Fator de Qualidade = 1 - (Taxa de Divergência / 100)

Exemplo:
Tempo Total = 8.639 coletas × 6.54 seg = 56.500 seg = 15.69 horas
Taxa Divergência = 17.7%
Fator Qualidade = 1 - 0.177 = 0.823

IEC = (8.639 / 15.69) × 0.823 = 453 coletas/hora efetivas
```

### 6.3 Valor de Perda por Deterioração
```
Perda = Σ(Valor Depreciado dos Itens IRRECUPERÁVEIS)

Perda Percentual = (Perda / Valor Total Depreciado) × 100
```

### 6.4 Densidade Patrimonial Ajustada
```
DPA = (Qtd Itens / Área m²) × Fator de Utilização

Fator de Utilização = (Qtd BOM + Qtd RECUPERÁVEL) / Qtd Total
```

### 6.5 Índice de Divergência Espacial (IDE)
```
IDE = (Divergências na Sala / Total Coletas na Sala) × 100

Classificação:
- IDE < 5%: Baixa divergência
- 5% ≤ IDE < 15%: Média divergência
- IDE ≥ 15%: Alta divergência
```


---

## 7. Análises Específicas para Artigo Científico

### 7.1 Hipóteses de Pesquisa

**H1:** Existe correlação significativa entre tipo de sala e estado de conservação dos patrimônios.
- **Teste:** Qui-quadrado (χ²)
- **Nível de significância:** α = 0.05

**H2:** O tempo de coleta varia significativamente entre diferentes métodos (QR Code, Manual, Sem Etiqueta).
- **Teste:** ANOVA one-way
- **Nível de significância:** α = 0.05

**H3:** Salas com maior densidade patrimonial apresentam maior taxa de divergências.
- **Teste:** Correlação de Pearson
- **Nível de significância:** α = 0.05

**H4:** A idade do patrimônio está correlacionada com o estado de conservação.
- **Teste:** Regressão logística multinomial
- **Variável dependente:** Estado de conservação (categórica)
- **Variável independente:** Idade em anos (contínua)

### 7.2 Análise de Caso: Secretaria (Sala Crítica)

**Contexto:**
- 1.195 coletas (13.83% do total)
- 92.89% em estado IRRECUPERÁVEL
- 36.74% com divergências
- Tempo médio 2.5x superior à média geral

**Análise Detalhada:**
```sql
-- Query específica para análise da Secretaria
SELECT 
    p.categoria,
    COUNT(*) as quantidade,
    ROUND(AVG(EXTRACT(YEAR FROM AGE(CURRENT_DATE, p.data_entrada))), 1) as idade_media,
    SUM(p.valor_aquisicao) as valor_total,
    COUNT(CASE WHEN c.divergencia = true THEN 1 END) as divergencias,
    ROUND(AVG(c.tempo_coleta_segundos), 2) as tempo_medio
FROM tabela_coleta c
INNER JOIN tabela_patrimonio p ON c.id_patrimonio = p.id
INNER JOIN tabela_sala s ON p.id_sala = s.id_sala
WHERE s.descricao = 'Secretaria'
  AND c.estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL')
GROUP BY p.categoria
ORDER BY quantidade DESC;
```

**Possíveis Causas:**
1. Patrimônios antigos aguardando baixa formal
2. Concentração de equipamentos eletrônicos obsoletos
3. Falta de manutenção preventiva
4. Processo de substituição não acompanhado de baixa patrimonial

### 7.3 Análise Comparativa: Biblioteca vs. Secretaria

```python
def analise_comparativa_salas(df):
    """Compara estatísticas entre Biblioteca e Secretaria"""
    salas = ['BIBLIOTECA(IFMT - PDL)', 'Secretaria']
    comparacao = df[df['sala'].isin(salas)].groupby('sala').agg({
        'coleta_id': 'count',
        'estado_normalizado': lambda x: (x == 'BOM').sum() / len(x) * 100,
        'divergencia': lambda x: x.sum() / len(x) * 100,
        'tempo_coleta_segundos': 'mean',
        'valor_aquisicao': 'sum'
    }).round(2)
    
    comparacao.columns = ['Total Coletas', '% BOM', '% Divergências', 
                          'Tempo Médio (s)', 'Valor Total (R$)']
    
    return comparacao
```

**Resultado Esperado:**
| Sala | Total Coletas | % BOM | % Divergências | Tempo Médio | Valor Total |
|------|---------------|-------|----------------|-------------|-------------|
| Biblioteca | 4.151 | 100% | 1.64% | 0.00s | R$ X |
| Secretaria | 1.195 | 6.95% | 36.74% | 16.68s | R$ Y |


---

## 8. Estrutura do Dataset Final

### 8.1 Arquivo CSV Principal: `inventario_2024_processado.csv`

**Colunas:**
```
coleta_id, patrimonio_id, numero_patrimonio, descricao, categoria,
estado_normalizado, estado_original, estado_cadastrado,
sala, tipo_sala, bloco, andar, area_m2,
valor_aquisicao, valor_depreciado, idade_anos,
tempo_coleta_seg, tempo_scan_seg, tempo_preenchimento_seg,
metodo_coleta, qualidade_etiqueta,
divergencia, tipo_divergencia,
sem_etiqueta, data_coleta, periodo_dia, hora_coleta, dia_semana,
densidade_sala, ide_sala
```

### 8.2 Arquivo Agregado: `estatisticas_por_sala.csv`

**Colunas:**
```
sala, tipo_sala, bloco, andar, area_m2,
total_coletas, densidade_itens_m2,
qtd_bom, qtd_irrecuperavel, qtd_recuperavel, qtd_ocioso,
perc_bom, perc_irrecuperavel,
qtd_divergencias, taxa_divergencia,
tempo_medio_coleta, desvio_tempo,
valor_total_sala, valor_depreciado_sala
```

### 8.3 Arquivo Agregado: `estatisticas_por_estado.csv`

**Colunas:**
```
estado_normalizado, quantidade, percentual,
valor_aquisicao_total, valor_depreciado_total,
valor_medio_aquisicao, valor_medio_depreciado,
idade_media_anos, tempo_medio_coleta,
qtd_divergencias, taxa_divergencia
```

---

## 9. Cronograma de Execução

### Fase 1: Extração (1 dia)
- [ ] Executar queries SQL
- [ ] Exportar dados para CSV
- [ ] Validar integridade dos dados

### Fase 2: Processamento (2 dias)
- [ ] Normalizar estados de conservação
- [ ] Calcular métricas derivadas
- [ ] Identificar outliers
- [ ] Gerar datasets processados

### Fase 3: Análise Estatística (3 dias)
- [ ] Estatísticas descritivas
- [ ] Testes de hipóteses
- [ ] Análises de correlação
- [ ] Regressões

### Fase 4: Visualização (2 dias)
- [ ] Gerar gráficos principais
- [ ] Criar mapas de calor
- [ ] Produzir infográficos

### Fase 5: Relatórios (2 dias)
- [ ] Relatório executivo (PDF)
- [ ] Relatório técnico (PDF)
- [ ] Tabelas LaTeX para artigo
- [ ] Apresentação (PowerPoint)

**Total:** 10 dias úteis

---

## 10. Validação e Qualidade

### 10.1 Checklist de Validação

**Dados:**
- [ ] Sem valores NULL em campos críticos
- [ ] Estados normalizados corretamente
- [ ] Datas válidas e consistentes
- [ ] Valores numéricos dentro de ranges esperados

**Análises:**
- [ ] Testes estatísticos com p-valor < 0.05
- [ ] Intervalos de confiança calculados
- [ ] Outliers identificados e tratados
- [ ] Correlações verificadas

**Visualizações:**
- [ ] Gráficos em alta resolução (300 DPI)
- [ ] Legendas claras e descritivas
- [ ] Cores acessíveis (colorblind-friendly)
- [ ] Fontes legíveis

**Relatórios:**
- [ ] Formatação acadêmica (ABNT/APA)
- [ ] Referências bibliográficas completas
- [ ] Tabelas numeradas e legendadas
- [ ] Revisão ortográfica e gramatical

---

## 11. Ferramentas e Bibliotecas

### 11.1 Python
```
pandas==2.1.0
numpy==1.24.0
scipy==1.11.0
matplotlib==3.7.0
seaborn==0.12.0
reportlab==4.0.0
openpyxl==3.1.0
```

### 11.2 R (Opcional)
```
tidyverse
ggplot2
dplyr
readr
knitr
```

### 11.3 LaTeX
```
\usepackage{booktabs}
\usepackage{graphicx}
\usepackage{float}
\usepackage{caption}
```

---

**Versão:** 1.0.0  
**Data:** 07/02/2026  
**Status:** ✅ Design Completo
