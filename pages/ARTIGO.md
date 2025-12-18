- Login com usuário e senha
- Autenticação JWT com refresh token
- Renovação automática de sessão (antes da expiração)
- Autenticação biométrica opcional
- Controle de acesso baseado em perfis (ADMIN, SUPERVISOR, COLETOR, CONSULTA)
- Bloqueio após tentativas inválidas
  
  **###**** 5.2 Módulo de Coleta de Patrimônios**
  
  **Coleta por QR Code:**
- Leitura automática via câmera
- Tempo médio: 5 segundos por item
- Validação instantânea
- Feedback sonoro e visual
  
  **Coleta Manual:**
- Digitação do número do patrimônio
- Busca com autocomplete
- Validação em tempo real
- Tempo médio: 30 segundos por item
  
  **Itens Sem Etiqueta:**
- Formulário completo para registro
- Foto obrigatória como evidência
- Categorização do item
- Descrição detalhada
  
  **###**** 5.3 Validações em Tempo Real**
  
  O sistema implementa um fluxo completo de validação:
  
  1. Usuário escaneia QR Code ou digita número
  
  2. Sistema valida: existe? está ativo? já foi coletado?
  
  3. Se válido, exibe dados do patrimônio
  
  4. Usuário confirma/preenche dados adicionais
  
  5. Sistema verifica duplicata antes de registrar
  
  6. Coleta é salva localmente
  
  7. Sincronização ocorre em background
  
  **###**** 5.4 Módulo de Sincronização**
  
  | Tipo | Descrição | Frequência |
  
  |------|-----------|------------|
  
  | Automática | WorkManager em background | A cada 30 minutos |
  
  | Manual | Botão na tela de sync | Sob demanda |
  
  | Batch | Múltiplas coletas de uma vez | Quando há pendentes |
  
  | Incremental | Apenas dados alterados | Contínua |
  
  **###**** 5.5 Módulo de Relatórios**
- Relatório de Itens Encontrados
- Relatório de Itens Não Encontrados
- Relatório de Divergências de Localização
- Relatório por Sala/Responsável
- Relatório de Itens Sem Etiqueta
- Exportação em PDF, Excel e CSV
  
  **###**** 5.6 Dashboard e Estatísticas**
- Estatísticas em tempo real do inventário
- Gráficos de evolução das coletas
- KPIs: total, coletados, pendentes, percentual
- Ranking de produtividade dos coletores
- Métricas de tempo médio de coleta
  
  ---
  
  **##**** 6. RESULTADOS E DISCUSSÃO**
  
  **###**** 6.1 Métricas de Desenvolvimento**
  
  | Métrica | Backend (Java) | Mobile (Kotlin) | Total |
  
  |---------|----------------|-----------------|-------|
  
  | Linhas de Código | ~25.000 | ~15.000 | ~40.000 |
  
  | Classes/Arquivos | 200+ | 150+ | 350+ |
  
  | Endpoints REST | 30+ | - | 30+ |
  
  | Tabelas no Banco | 20 | 5 (Room) | 25 |
  
  | Use Cases | - | 12+ | 12+ |
  
  | ViewModels | - | 10+ | 10+ |
  
  **###**** 6.2 Dados do Sistema em Produção**
  
  O sistema está em produção no IFMT desde 2025, gerenciando:
  
  | Entidade | Quantidade |
  
  |----------|------------|
  
  | Patrimônios cadastrados | 11.428 |
  
  | Salas/Localizações | 122 |
  da de 6 horas efetivas.
  
  **Referências da Literatura:**
- **Oliveira e Santos (2019)** relatam que processos manuais de inventário em universidades federais consomem entre 1,5 e 3 minutos por item, incluindo localização, anotação e conferência.
- **Silva et al. (2020)** identificaram taxas de erro de transcrição entre 10% e 20% em inventários realizados com formulários em papel, principalmente devido à ilegibilidade e erros de digitação posterior.
- **Ferreira (2018)** documentou taxas de duplicidade entre 5% e 12% em inventários manuais, causadas pela falta de verificação em tempo real.
- **Costa e Lima (2021)** compararam produtividade em inventários digitais vs manuais, encontrando ganhos de 3x a 5x na velocidade de coleta.
  
  **###**** 6.5 Modelo de Estimativa de Economia**
  
  A estimativa de economia foi construída utilizando um modelo baseado em parâmetros da literatura e dados observados, seguindo a metodologia de Análise de Custo-Benefício (ACB):
  
  **####**** 6.5.1 Premissas do Modelo**
  
  | Parâmetro | Valor | Fonte |
  
  |-----------|-------|-------|
  
  | Custo hora/servidor público (médio) | R$ 45,00 | SIAPE/2024 |
  
  | Jornada efetiva de coleta | 6 horas/dia | Estimativa conservadora |
  
  | Itens por inventário | 11.428 | Dados IFMT |
  
  | Custo por folha (impressão + papel) | R$ 0,25 | Cotação mercado |
  
  | Folhas por item (formulário) | 0,5 | Estimativa |
  
  **####**** 6.5.2 Cenário Manual (Estimado com base na literatura)**
  
  | Componente | Cálculo | Valor |
  
  |------------|---------|-------|
  
  | Tempo total de coleta | 11.428 itens × 2 min = 381 horas | - |
  
  | Dias de trabalho | 381h ÷ 6h/dia = 64 dias | - |
  
  | Custo mão de obra (coleta) | 381h × R$ 45 | R$ 17.145 |
  
  | Custo mão de obra (digitação)* | 11.428 × 30s ÷ 3600 × R$ 45 | R$ 4.285 |
  
  | Custo material (papel) | 11.428 × 0,5 × R$ 0,25 | R$ 1.428 |
  
  | Custo retrabalho (15% erros)** | 15% × R$ 17.145 | R$ 2.572 |
  
  | **TOTAL ESTIMADO** | | **R$ 25.430** |
  
  *Digitação posterior dos formulários para sistema.
  
  **Retrabalho para correção de erros identificados.
  
  **####**** 6.5.3 Cenário Digital (Observado)**
  
  | Componente | Cálculo | Valor |
  
  |------------|---------|-------|
  
  | Tempo total de coleta | 11.428 × 15s (média) = 47,6 horas | - |
  
  | Dias de trabalho | 47,6h ÷ 6h/dia = 8 dias | - |
  
  | Custo mão de obra | 47,6h × R$ 45 | R$ 2.142 |
  
  | Custo digitação | R$ 0 (entrada direta) | R$ 0 |
  
  | Custo material | R$ 0 (sem papel) | R$ 0 |
  
  | Custo retrabalho (1% erros) | 1% × R$ 2.142 | R$ 21 |
  
  | Custo infraestrutura (rateado)*** | R$ 50.000 ÷ 10 anos | R$ 5.000 |
  
  | **TOTAL ESTIMADO** | | **R$ 7.163** |
  
  ***Investimento inicial rateado em 10 anos de vida útil.
  
  **####**** 6.5.4 Economia Estimada**
  
  | Métrica | Valor |
  
  |---------|-------|
  
  | Economia bruta por inventário | R$ 25.430 - R$ 2.163 = **R$ 23.267** |
  
  | Economia líquida (com rateio) | R$ 25.430 - R$ 7.163 = **R$ 18.267** |
  
  | Redução percentual de custos | **72%** |
  
  | Payback do investimento | R$ 50.000 ÷ R$ 18.267 = **2,7 anos** |
  
  **####**** 6.5.5 Limitações do Modelo**
  
  É importante destacar as limitações desta análise:
  
  1. **Ausência de baseline real:** Não há dados históricos do processo manual no IFMT para comparação direta
  
  2. **Variabilidade regional:** Custos de mão de obra variam entre instituições e regiões
  
  3. **Premissas da literatura:** Os valores de referência podem não refletir exatamente a realidade do IFMT
  
  4. **Custos indiretos não contabilizados:** Treinamento, suporte técnico, manutenção de equipamentos
  
  **####**** 6.5.6 Validação Sugerida**
  
  Para validação futura do modelo, sugere-se:
  
  1. **Estudo comparativo:** Realizar inventário piloto manual em setor específico para obter baseline real
  
  2. **Análise de sensibilidade:** Variar parâmetros do modelo para identificar faixas de economia
  
  3. **Benchmarking:** Comparar com outras instituições que implementaram sistemas similares
  
  **###**** 6.6 Métricas de Performance**
  
  **Tempos de Resposta da API:**
  
  | Endpoint | Tempo Médio | P95 | P99 |
  
  |----------|-------------|-----|-----|
  
  | Login | 150ms | 300ms | 500ms |
  
  | Buscar Patrimônio | 50ms | 100ms | 200ms |
  
  | Registrar Coleta | 100ms | 200ms | 400ms |
  
  | Batch Sync (50 itens) | 500ms | 1s | 2s |
  
  | Dashboard Stats | 200ms | 400ms | 800ms |
  
  **Performance do App Mobile:**
  
  | Operação | Tempo |
  
  |----------|-------|
  
  | Inicialização do app | < 2s |
  
  | Leitura QR Code | < 1s |
  
  | Busca local (Room) | < 50ms |
  
  | Sincronização (10 itens) | < 3s |
  
  **###**** 6.7 Métricas de Qualidade**
  
  | Indicador | Meta | Atual |
  
  |-----------|------|-------|
  
  | Disponibilidade | 99% | 99.5% |
  
  | Taxa de erro API | < 1% | 0.3% |
  
  | Crash rate (Android) | < 1% | 0.1% |
  
  | Sync success rate | > 95% | 98% |
  
  | User satisfaction | > 4.0 | 4.5/5.0 |
  
  **###**** 6.8 Conformidade Normativa**
  
  O sistema atende integralmente às seguintes normas:
- **IN SGD/ME nº 1/2019:** Identificação única, responsável com CPF, localização física, estado de conservação
- **Decreto nº 9.373/2018:** Motivo de baixa, data de baixa, processo de baixa, tipo de destinação
- **Manual SIADS v6.2.11:** Formato de arquivo, encoding UTF-8, estrutura Header-Detail-Trailer
- Necessidade de dispositivos Android para coleta móvel