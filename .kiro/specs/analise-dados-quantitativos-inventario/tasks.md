# Análise de Dados Quantitativos do Inventário Patrimonial - Tasks

## 1. Extração de Dados

- [ ] 1.1 Criar view temporária `vw_analise_estado` com dados normalizados
- [ ] 1.2 Criar view temporária `vw_analise_divergencias` para análise de inconsistências
- [ ] 1.3 Executar query de métricas de eficiência e exportar para CSV
- [ ] 1.4 Executar query de análise espacial e exportar para CSV
- [ ] 1.5 Executar query de análise financeira e exportar para CSV
- [ ] 1.6 Validar integridade dos dados exportados (contagem de registros, valores NULL)

## 2. Processamento e Normalização

- [ ] 2.1 Criar script Python `processar_dados.py` com funções de normalização
- [ ] 2.2 Implementar função `normalizar_estado_conservacao()` para padronizar estados
- [ ] 2.3 Implementar função `calcular_metricas_temporais()` para derivar campos de data/hora
- [ ] 2.4 Implementar função `calcular_idade_patrimonio()` para calcular idade em anos
- [ ] 2.5 Implementar função `identificar_outliers_tempo()` usando método IQR
- [ ] 2.6 Executar pipeline de processamento e gerar `inventario_2024_processado.csv`
- [ ] 2.7 Gerar datasets agregados: `estatisticas_por_sala.csv` e `estatisticas_por_estado.csv`

## 3. Análise Estatística

- [ ] 3.1 Criar script Python `analises_estatisticas.py`
- [ ] 3.2 Implementar função `analise_descritiva_estado()` com estatísticas por estado
- [ ] 3.3 Executar teste qui-quadrado: independência entre estado e tipo de sala
- [ ] 3.4 Executar ANOVA: diferença de tempo entre métodos de coleta
- [ ] 3.5 Calcular correlação de Pearson: densidade vs. taxa de divergências
- [ ] 3.6 Executar regressão logística: idade vs. estado de conservação
- [ ] 3.7 Calcular intervalos de confiança (95%) para todas as métricas principais
- [ ] 3.8 Gerar relatório de testes estatísticos em formato JSON

## 4. Análise de Caso: Secretaria

- [ ] 4.1 Executar query específica para patrimônios IRRECUPERÁVEIS na Secretaria
- [ ] 4.2 Analisar distribuição por categoria de patrimônio
- [ ] 4.3 Calcular idade média dos itens irrecuperáveis
- [ ] 4.4 Identificar padrões de divergência
- [ ] 4.5 Gerar relatório específico: `analise_secretaria.pdf`

## 5. Visualizações

- [ ] 5.1 Criar script Python `gerar_visualizacoes.py`
- [ ] 5.2 Gerar gráfico de pizza: distribuição de estados de conservação
- [ ] 5.3 Gerar boxplot: tempo de coleta por método
- [ ] 5.4 Gerar mapa de calor: divergências por sala e estado
- [ ] 5.5 Gerar gráfico de barras: densidade patrimonial por sala (top 15)
- [ ] 5.6 Gerar gráfico de linha: evolução temporal das coletas
- [ ] 5.7 Gerar gráfico de dispersão: idade vs. valor depreciado
- [ ] 5.8 Exportar todos os gráficos em PNG (300 DPI) e SVG

## 6. Relatórios para Artigo Científico

- [ ] 6.1 Criar script Python `gerar_relatorios.py`
- [ ] 6.2 Gerar tabela LaTeX: estatísticas descritivas por estado
- [ ] 6.3 Gerar tabela LaTeX: análise comparativa Biblioteca vs. Secretaria
- [ ] 6.4 Gerar tabela LaTeX: resultados dos testes de hipóteses
- [ ] 6.5 Gerar tabela LaTeX: top 10 salas por volume de coletas
- [ ] 6.6 Criar arquivo `tabelas_artigo.tex` com todas as tabelas formatadas

## 7. Relatório Executivo

- [ ] 7.1 Implementar função `gerar_relatorio_executivo()` usando ReportLab
- [ ] 7.2 Adicionar seção: Resumo Executivo com métricas principais
- [ ] 7.3 Adicionar seção: Análise de Estado de Conservação
- [ ] 7.4 Adicionar seção: Análise de Divergências
- [ ] 7.5 Adicionar seção: Análise Espacial
- [ ] 7.6 Adicionar seção: Recomendações
- [ ] 7.7 Incluir gráficos principais no relatório
- [ ] 7.8 Gerar `relatorio_executivo.pdf`

## 8. Relatório Técnico

- [ ] 8.1 Criar documento LaTeX `relatorio_tecnico.tex`
- [ ] 8.2 Escrever seção: Introdução e Objetivos
- [ ] 8.3 Escrever seção: Metodologia de Coleta de Dados
- [ ] 8.4 Escrever seção: Metodologia de Análise Estatística
- [ ] 8.5 Escrever seção: Resultados e Discussão
- [ ] 8.6 Escrever seção: Análise de Casos Críticos
- [ ] 8.7 Escrever seção: Conclusões e Recomendações
- [ ] 8.8 Incluir todas as tabelas e gráficos
- [ ] 8.9 Adicionar referências bibliográficas
- [ ] 8.10 Compilar `relatorio_tecnico.pdf`

## 9. Apresentação

- [ ] 9.1 Criar apresentação PowerPoint `apresentacao_resultados.pptx`
- [ ] 9.2 Slide 1: Título e Contexto
- [ ] 9.3 Slide 2: Metodologia de Coleta
- [ ] 9.4 Slide 3: Resumo Quantitativo
- [ ] 9.5 Slide 4: Distribuição de Estados de Conservação (gráfico)
- [ ] 9.6 Slide 5: Análise de Divergências
- [ ] 9.7 Slide 6: Caso Crítico - Secretaria
- [ ] 9.8 Slide 7: Análise Espacial
- [ ] 9.9 Slide 8: Análise Financeira
- [ ] 9.10 Slide 9: Conclusões e Recomendações
- [ ] 9.11 Exportar para PDF: `apresentacao_resultados.pdf`

## 10. Validação e Documentação

- [ ] 10.1 Executar checklist de validação de dados
- [ ] 10.2 Executar checklist de validação de análises
- [ ] 10.3 Executar checklist de validação de visualizações
- [ ] 10.4 Executar checklist de validação de relatórios
- [ ] 10.5 Criar arquivo `README.md` com instruções de uso
- [ ] 10.6 Criar arquivo `METODOLOGIA.md` com detalhamento técnico
- [ ] 10.7 Criar arquivo `requirements.txt` com dependências Python
- [ ] 10.8 Criar arquivo `CHANGELOG.md` com histórico de versões

## 11. Entrega Final

- [ ] 11.1 Organizar estrutura de pastas do projeto
- [ ] 11.2 Compactar datasets processados em `datasets.zip`
- [ ] 11.3 Compactar visualizações em `graficos.zip`
- [ ] 11.4 Compactar relatórios em `relatorios.zip`
- [ ] 11.5 Criar arquivo `RESUMO_EXECUTIVO.md` com principais achados
- [ ] 11.6 Revisar todos os documentos
- [ ] 11.7 Entregar pacote completo

---

**Total de Tasks:** 77  
**Estimativa:** 10 dias úteis  
**Prioridade:** Alta (Artigo Científico e TCC)
