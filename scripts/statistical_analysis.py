import pandas as pd
import numpy as np
import scipy.stats as stats
import statsmodels.api as sm
from statsmodels.formula.api import logit
import os

def run_analysis():
    df = pd.read_csv('../data/dataset_coletas.csv')
    
    df['tempo_coleta_segundos'] = pd.to_numeric(df['tempo_coleta_segundos'], errors='coerce')
    df['estado_encontrado'] = df['estado_encontrado'].str.upper().replace({
        'IRRECUPERAVEL': 'IRRECUPERÁVEL',
        'RECUPERAVEL': 'RECUPERÁVEL'
    }).fillna('N/A')
    
    df['divergencia_int'] = df['divergencia'].astype(int)
    
    results = []
    
    results.append("## 📈 Resultados da Análise Estatística (Fase 3)")
    
    results.append("\n### 1. Testes de Hipóteses (Qui-Quadrado)")
    
    contingency_table = pd.crosstab(df['estado_encontrado'], df['divergencia'])
    chi2, p, dof, ex = stats.chi2_contingency(contingency_table)
    results.append(f"- **H0:** O Estado de Conservação e a ocorrência de Divergência são independentes.")
    results.append(f"- **Resultado:** Qui-quadrado = {chi2:.2f}, p-valor = {p:.4e}")
    if p < 0.05:
         results.append(f"  - **Conclusão:** Rejeitamos H0. Há uma relação estatisticamente significativa entre o estado do bem e a probabilidade de divergência.")
    else:
         results.append(f"  - **Conclusão:** Não rejeitamos H0. O estado não parece influenciar a divergência.")
         
    top_locs = df['localizacao_encontrada'].value_counts().nlargest(10).index
    df_top = df[df['localizacao_encontrada'].isin(top_locs)]
    contingency_loc = pd.crosstab(df_top['localizacao_encontrada'], df_top['divergencia'])
    chi2_loc, p_loc, dof_loc, ex_loc = stats.chi2_contingency(contingency_loc)
    results.append(f"\n- **H0:** A Sala e a ocorrência de Divergência são independentes.")
    results.append(f"- **Resultado:** Qui-quadrado = {chi2_loc:.2f}, p-valor = {p_loc:.4e}")
    if p_loc < 0.05:
         results.append(f"  - **Conclusão:** Rejeitamos H0. A localização do bem influencia significativamente a chance de haver divergência.")

    results.append("\n### 2. Correlações")
    num_cols = ['tempo_coleta_segundos', 'divergencia_int']
    if 'tentativas_scan' in df.columns:
        df['tentativas_scan'] = pd.to_numeric(df['tentativas_scan'], errors='coerce')
        num_cols.append('tentativas_scan')
        
    corr_matrix = df[num_cols].corr()
    
    tempo_div_corr = corr_matrix.loc['tempo_coleta_segundos', 'divergencia_int']
    results.append(f"- **Tempo de Coleta vs Divergência (Pearson):** {tempo_div_corr:.4f}")
    if abs(tempo_div_corr) > 0.05:
        results.append(f"  - Existe uma correlação {'positiva' if tempo_div_corr > 0 else 'negativa'} fraca a moderada. Divergências podem estar levemente associadas com uma mudança no tempo de coleta.")
    else:
        results.append(f"  - A correlação é muito próxima de zero, indicando que o tempo de coleta não é uma variável forte para prever divergências linearmente.")

    results.append("\n### 3. Regressões (Regressão Logística)")
    df['is_irrecuperavel'] = (df['estado_encontrado'] == 'IRRECUPERÁVEL').astype(int)
    df_reg = df.dropna(subset=['tempo_coleta_segundos', 'divergencia_int', 'is_irrecuperavel'])
    
    try:
        model = logit("divergencia_int ~ tempo_coleta_segundos + is_irrecuperavel", data=df_reg).fit(disp=0)
        results.append("- **Modelo:** `Divergência ~ Tempo de Coleta + É_Irrecuperável`")
        
        coef_tempo = model.params['tempo_coleta_segundos']
        p_tempo = model.pvalues['tempo_coleta_segundos']
        
        coef_irr = model.params['is_irrecuperavel']
        p_irr = model.pvalues['is_irrecuperavel']
        
        results.append(f"  - **Tempo de Coleta:** Coef = {coef_tempo:.4f} (p-valor = {p_tempo:.4f})")
        if p_tempo < 0.05:
            results.append("    - Significativo como preditor.")
            
        results.append(f"  - **Estado Irrecuperável:** Coef = {coef_irr:.4f} (p-valor = {p_irr:.4f})")
        if p_irr < 0.05:
             results.append("    - Significativo. Ser irrecuperável afeta significativamente a probabilidade de divergência.")
             
        results.append(f"  - **Pseudo R-squared (McFadden):** {model.prsquared:.4f}")
        
    except Exception as e:
        results.append(f"- Erro ao rodar regressão: {e}")

    with open('../data/analise_estatistica.md', 'w', encoding='utf-8') as f:
        f.write("\n".join(results))
        
    print("Análise Estatística Concluída. Resultados salvos em data/analise_estatistica.md")

if __name__ == '__main__':
    run_analysis()
