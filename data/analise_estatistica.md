## 📈 Resultados da Análise Estatística (Fase 3)

### 1. Testes de Hipóteses (Qui-Quadrado)
- **H0:** O Estado de Conservação e a ocorrência de Divergência são independentes.
- **Resultado:** Qui-quadrado = 280.46, p-valor = 1.2506e-57
  - **Conclusão:** Rejeitamos H0. Há uma relação estatisticamente significativa entre o estado do bem e a probabilidade de divergência.

- **H0:** A Sala e a ocorrência de Divergência são independentes.
- **Resultado:** Qui-quadrado = 4674.31, p-valor = 0.0000e+00
  - **Conclusão:** Rejeitamos H0. A localização do bem influencia significativamente a chance de haver divergência.

### 2. Correlações
- **Tempo de Coleta vs Divergência (Pearson):** nan
  - A correlação é muito próxima de zero, indicando que o tempo de coleta não é uma variável forte para prever divergências linearmente.

### 3. Regressões (Regressão Logística)
- **Modelo:** `Divergência ~ Tempo de Coleta + É_Irrecuperável`
  - **Tempo de Coleta:** Coef = -0.0970 (p-valor = 0.9960)
  - **Estado Irrecuperável:** Coef = 20.4091 (p-valor = 1.0000)
  - **Pseudo R-squared (McFadden):** inf