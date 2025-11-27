# Implementation Plan - Itens Compostos

## Fase 1: Preparação do Banco de Dados

- [x] 1. Criar estrutura de banco de dados





- [ ] 1.1 Criar script SQL para TABELA_ITEM_COMPOSTO
  - Incluir constraints e foreign keys
  - Adicionar índices para performance

  - _Requirements: 1.1, 1.5_

- [ ] 1.2 Criar script SQL para TABELA_COMPONENTE
  - Incluir validações (descrição mínima 3 chars, quantidade > 0)

  - Adicionar índices por tipo e item_composto
  - _Requirements: 1.2, 1.3_

- [x] 1.3 Criar script SQL para TABELA_COMPONENTE_COLETA

  - Incluir constraint unique (componente + coleta)
  - Adicionar índices para queries de relatório
  - _Requirements: 2.3, 4.1_

- [ ] 1.4 Criar script SQL para TABELA_PADRAO_DETECCAO e TABELA_COMPONENTE_PADRAO
  - Incluir tipos de padrão (PALAVRAS_CHAVE, REGEX)
  - Adicionar índices por prioridade


  - _Requirements: 5.1, 6.1_

- [x] 1.5 Criar views para relatórios


  - VIEW_ITEM_COMPOSTO_RESUMO (join com patrimônio, sala, setor)

  - VIEW_COMPONENTE_STATUS_INVENTARIO (status por inventário)
  - _Requirements: 2.3, 4.1_

- [ ] 1.6 Inserir padrões de detecção padrão
  - "Mesa com Cadeiras" (MESA.*COM.*CADEIRA)
  - "Estação de Trabalho" (ESTACAO.*TRABALHO)
  - "Computador Completo" (COMPUTADOR.*(MONITOR|TECLADO))
  - _Requirements: 5.2, 6.1_

- [ ] 1.7 Executar scripts no banco de dados de desenvolvimento
  - Validar criação de todas as tabelas





  - Testar constraints e foreign keys
  - Verificar índices criados
  - _Requirements: 1.5_



- [ ]* 1.8 Criar testes de integridade do banco
  - Testar cascade delete (item_composto → componentes)


  - Testar constraints de validação
  - Testar unique constraints

  - _Requirements: 1.5_

## Fase 2: Camada de Modelo (Domain)


- [ ] 2. Criar classes de modelo
- [ ] 2.1 Criar classe ItemComposto.java
  - Atributos: id, idPatrimonio, numeroPatrimonio, descricaoPatrimonio, deteccaoAutomatica, dataCriacao, componentes

  - Métodos: getTotalComponentes(), isCompleto(idInventario), getTaxaIntegridade(idInventario)
  - _Requirements: 1.1, 2.5_

- [ ] 2.2 Criar classe Componente.java
  - Atributos: id, idItemComposto, tipo, descricao, quantidadeEsperada, ordem
  - Métodos: getQuantidadeEncontrada(idInventario), getQuantidadeFaltante(idInventario), isCompleto(idInventario)
  - _Requirements: 1.2, 4.2_


- [ ] 2.3 Criar classe ComponenteColeta.java
  - Atributos: id, idComponente, idColeta, idInventario, quantidadeEncontrada, observacoes, dataRegistro
  - _Requirements: 2.3_

- [ ] 2.4 Criar classe PadraoDeteccao.java
  - Atributos: id, nome, descricaoPadrao, tipoPadrao, ativo, prioridade, componentesPadrao
  - Métodos: matches(descricao), extrairComponentes(descricao)
  - _Requirements: 5.1, 6.1_

- [ ] 2.5 Criar classe ComponentePadrao.java
  - Atributos: id, idPadraoDeteccao, tipo, descricao, quantidadePadrao, ordem





  - _Requirements: 6.2_

- [ ] 2.6 Criar classes de DTO para relatórios
  - RelatorioItensCompostos.java

  - EstatisticasGerais.java
  - EstatisticaComponente.java
  - RelatorioComparativo.java
  - FiltroRelatorio.java
  - _Requirements: 2.2, 2.5, 4.1_



- [ ] 2.7 Criar classes de exceção
  - ItemCompostoValidationException.java
  - RelatorioException.java

  - ExportacaoException.java
  - _Requirements: 1.3, 3.4_

- [x]* 2.8 Escrever testes unitários para métodos de negócio dos models

  - Testar cálculo de taxa de integridade
  - Testar validações
  - Testar métodos de comparação
  - **Property 1: Validação de descrição de componente**

  - **Validates: Requirements 1.3**

## Fase 3: Camada de Acesso a Dados (DAO)


- [ ] 3. Implementar DAOs
- [ ] 3.1 Criar interface ItemCompostoDAO.java
  - Métodos CRUD: inserir, atualizar, excluir, buscarPorId, buscarPorPatrimonio

  - Métodos de consulta: listarTodos, listarPorInventario, listarIncompletos, listarComFiltros
  - Métodos de estatística: contarTotal, contarCompletos, calcularTaxaIntegridadeGeral
  - _Requirements: 1.1, 2.3, 2.5_

- [ ] 3.2 Implementar ItemCompostoDAOImpl.java
  - Implementar todos os métodos da interface
  - Usar PreparedStatements para prevenir SQL Injection
  - Implementar paginação para listarTodos
  - _Requirements: 1.5, 2.3_

- [x] 3.3 Criar interface ComponenteDAO.java





  - Métodos CRUD: inserir, atualizar, excluir
  - Métodos de consulta: listarPorItemComposto, listarTiposComponentes
  - Métodos de estatística: obterEstatisticasPorTipo
  - _Requirements: 1.2, 4.1_

- [ ] 3.4 Implementar ComponenteDAOImpl.java
  - Implementar batch insert para múltiplos componentes

  - Implementar query otimizada para estatísticas por tipo
  - _Requirements: 1.5, 4.2_

- [ ] 3.5 Criar interface ComponenteColetaDAO.java
  - Métodos: registrar, atualizar, listarPorInventario, listarPorComponente
  - Métodos de consulta: obterQuantidadeEncontrada, isComponenteColetado

  - _Requirements: 2.3_

- [ ] 3.6 Implementar ComponenteColetaDAOImpl.java
  - Implementar queries otimizadas com índices
  - _Requirements: 2.3_

- [x] 3.7 Criar interface PadraoDeteccaoDAO.java

  - Métodos CRUD: inserir, atualizar, excluir, buscarPorId
  - Métodos de consulta: listarAtivos, listarPorPrioridade, testarPadrao
  - _Requirements: 5.1, 6.1_

- [ ] 3.8 Implementar PadraoDeteccaoDAOImpl.java
  - Implementar ordenação por prioridade
  - Implementar contagem de patrimônios afetados
  - _Requirements: 6.4_


- [ ]* 3.9 Escrever testes unitários para DAOs
  - Testar CRUD completo
  - Testar queries complexas
  - Testar tratamento de erros
  - **Property 2: Persistência de componentes (Round Trip)**
  - **Validates: Requirements 1.5**


## Fase 4: Camada de Serviço (Business Logic)

- [ ] 4. Implementar Services
- [ ] 4.1 Criar ItemCompostoService.java
  - criarItemComposto(idPatrimonio, componentes)
  - adicionarComponente(idItemComposto, componente)
  - removerComponente(idComponente)

  - atualizarComponentes(idItemComposto, componentes)
  - validarItemComposto(item)
  - patrimonioJaEhComposto(idPatrimonio)
  - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [x] 4.2 Implementar validações no ItemCompostoService

  - Validar descrição de componente (mínimo 3 caracteres)
  - Validar quantidade esperada (> 0)
  - Validar que patrimônio existe
  - Validar que patrimônio não é já composto
  - _Requirements: 1.3_

- [ ] 4.3 Criar RelatorioItemCompostoService.java
  - gerarRelatorio(filtro)
  - calcularEstatisticasGerais(idInventario)
  - calcularEstatisticasPorTipo(idInventario)
  - compararInventarios(idsInventarios)
  - obterHistorico(idItemComposto)
  - _Requirements: 2.2, 2.3, 2.5, 4.1, 9.2_

- [-] 4.4 Implementar lógica de filtragem no RelatorioItemCompostoService



  - Filtro por inventário
  - Filtro por setor
  - Filtro por sala
  - Filtro por status de integridade
  - Filtro por tipo de componente faltante
  - Combinação de múltiplos filtros (AND lógico)
  - _Requirements: 2.2, 7.1, 7.2_

- [ ] 4.5 Implementar cálculos estatísticos no RelatorioItemCompostoService
  - Calcular total de conjuntos, completos, incompletos
  - Calcular taxa geral de integridade
  - Agrupar por tipo de componente
  - Calcular total esperado, encontrado, faltante por tipo
  - Ordenar por taxa de ausência
  - _Requirements: 2.5, 4.1, 4.2, 4.3_

- [ ] 4.6 Criar DeteccaoAutomaticaService.java
  - detectarItensCompostos(patrimonios)
  - analisarDescricao(descricao)
  - aplicarSugestao(sugestao)
  - criarPadrao(padrao)
  - testarPadrao(padrao)
  - contarPatrimoniosAfetados(padrao)
  - _Requirements: 5.1, 5.2, 5.3, 6.2, 6.4_

- [ ] 4.7 Implementar algoritmo de detecção de padrões
  - Buscar padrões ativos ordenados por prioridade
  - Aplicar regex ou palavras-chave
  - Extrair componentes sugeridos
  - Gerar SugestaoItemComposto
  - _Requirements: 5.2, 5.3_

- [ ] 4.8 Criar ExportacaoService.java
  - exportarExcel(relatorio, caminhoArquivo)
  - exportarPDF(relatorio, caminhoArquivo)
  - exportarCSV(relatorio, caminhoArquivo)
  - _Requirements: 3.1, 3.2, 3.3_

- [ ]* 4.9 Escrever testes unitários para Services
  - Testar validações
  - Testar cálculos estatísticos
  - Testar detecção de padrões
  - Testar filtragem
  - **Property 7: Invariante matemática de componentes**
  - **Validates: Requirements 4.2**
  - **Property 17: Filtragem com AND lógico**
  - **Validates: Requirements 7.2**

## Fase 5: Camada de Apresentação - Gestão de Componentes

- [ ] 5. Criar tela de gestão de itens compostos
- [ ] 5.1 Criar ItemCompostoFrame.java (JFrame)
  - Layout principal com busca de patrimônio
  - Checkbox "Marcar como Item Composto"
  - Painel de componentes (JTable)
  - Botões: Adicionar, Editar, Remover componente
  - Botões: Salvar, Cancelar
  - _Requirements: 1.1, 1.2_

- [ ] 5.2 Implementar busca de patrimônio no ItemCompostoFrame
  - Campo de texto para número do patrimônio
  - Botão "Buscar"
  - Carregar dados do patrimônio
  - Verificar se já é item composto
  - _Requirements: 1.1_

- [ ] 5.3 Implementar JTable de componentes
  - Colunas: Tipo, Descrição, Quantidade, Ações
  - Renderizador customizado para botões de ação
  - Ordenação por campo "ordem"
  - _Requirements: 1.2, 1.4_

- [ ] 5.4 Criar ComponenteDialog.java (JDialog)
  - Campo: Tipo (JComboBox com sugestões)
  - Campo: Descrição (JTextField)
  - Campo: Quantidade (JSpinner)
  - Validação em tempo real
  - Botões: Salvar, Cancelar
  - _Requirements: 1.2, 1.3_

- [ ] 5.5 Implementar validações no ComponenteDialog
  - Descrição mínima de 3 caracteres
  - Quantidade maior que zero
  - Exibir mensagens de erro amigáveis
  - _Requirements: 1.3_

- [ ] 5.6 Implementar ações de adicionar/editar/remover componente
  - Adicionar: abrir dialog, validar, adicionar à tabela
  - Editar: carregar dados, abrir dialog, atualizar tabela
  - Remover: confirmar, remover da tabela
  - _Requirements: 1.2, 1.4_

- [ ] 5.7 Implementar salvamento de item composto
  - Validar que há pelo menos um componente
  - Chamar ItemCompostoService.criarItemComposto ou atualizar
  - Exibir mensagem de sucesso
  - Limpar formulário
  - _Requirements: 1.5_

- [ ] 5.8 Implementar detecção automática no ItemCompostoFrame
  - Checkbox "Detecção Automática Ativada"
  - Botão "Detectar Padrões"
  - Exibir sugestões encontradas
  - Permitir aplicar sugestão
  - _Requirements: 5.1, 5.4_

- [ ] 5.9 Criar SugestaoDialog.java para exibir sugestões
  - Listar componentes sugeridos
  - Permitir editar antes de aplicar
  - Botões: Aplicar, Editar, Rejeitar
  - _Requirements: 5.4_

## Fase 6: Camada de Apresentação - Relatórios

- [ ] 6. Criar tela de relatórios de itens compostos
- [ ] 6.1 Criar RelatorioItemCompostoFrame.java (JFrame)
  - Painel de filtros no topo
  - Painel de estatísticas gerais
  - JTable com resultados
  - Botões de exportação e análises
  - _Requirements: 2.1, 2.2_

- [ ] 6.2 Implementar painel de filtros
  - ComboBox: Inventário
  - ComboBox: Setor
  - ComboBox: Sala
  - ComboBox: Status de Integridade (Todos, Completos, Incompletos)
  - Botões: Aplicar, Limpar
  - _Requirements: 2.2, 7.1_

- [ ] 6.3 Implementar configurações salvas de filtros
  - ComboBox: "Configurações Salvas"
  - Botão: "Salvar Como..."
  - Carregar e aplicar configuração selecionada
  - _Requirements: 7.5_

- [ ] 6.4 Implementar painel de estatísticas gerais
  - Labels: Total de Conjuntos, Completos, Incompletos
  - Label: Taxa Geral de Integridade
  - Atualizar ao aplicar filtros
  - _Requirements: 2.5_

- [ ] 6.5 Implementar JTable de resultados
  - Colunas: Nº Patrimônio, Descrição, Local, Esperado, Encontrado, Faltante
  - Renderizador customizado para destacar incompletos
  - Ícone de alerta para itens com componentes faltantes
  - Ordenação por colunas
  - _Requirements: 2.3, 2.4_

- [ ] 6.6 Implementar contador de registros
  - Label: "Mostrando X de Y registros"
  - Atualizar ao aplicar filtros
  - _Requirements: 7.4_

- [ ] 6.7 Implementar geração de relatório
  - Botão "Gerar Relatório"
  - Chamar RelatorioItemCompostoService.gerarRelatorio(filtro)
  - Preencher tabela com resultados
  - Atualizar estatísticas
  - _Requirements: 2.3, 2.5_

- [ ] 6.8 Criar EstatisticasPorTipoDialog.java
  - JTable: Tipo, Esperado, Encontrado, Faltante, Taxa Presença
  - Gráfico de barras (JFreeChart)
  - Clique em tipo para ver detalhes
  - Botão: Exportar
  - _Requirements: 4.1, 4.2, 4.3, 4.5_

- [ ] 6.9 Implementar detalhamento por tipo de componente
  - Ao clicar em tipo, abrir dialog com patrimônios
  - Listar patrimônios que possuem aquele componente faltante
  - _Requirements: 4.4_

- [ ] 6.10 Criar AnaliseComparativaDialog.java
  - Checkboxes para selecionar inventários
  - Botão "Comparar"
  - JTable comparativa com colunas por inventário
  - Indicadores visuais de tendência (⬆️⬇️)
  - Resumo de conjuntos que pioraram/melhoraram
  - _Requirements: 9.1, 9.2, 9.3_

- [ ] 6.11 Criar LinhaDoTempoDialog.java
  - Gráfico de linha mostrando evolução da integridade
  - Eixo X: Inventários (cronológico)
  - Eixo Y: Taxa de Integridade (%)
  - Detalhamento de componentes por inventário
  - _Requirements: 9.4_

## Fase 7: Exportação de Relatórios

- [ ] 7. Implementar exportação de relatórios
- [ ] 7.1 Implementar exportação Excel (Apache POI)
  - Criar workbook com 4 abas
  - Aba "Resumo Geral": estatísticas gerais
  - Aba "Conjuntos Completos": itens 100% completos
  - Aba "Conjuntos Incompletos": itens com componentes faltantes
  - Aba "Detalhamento por Componente": todos os componentes
  - _Requirements: 3.2_

- [ ] 7.2 Implementar formatação do Excel
  - Cabeçalhos em negrito
  - Cores alternadas nas linhas
  - Destacar incompletos em vermelho
  - Auto-ajustar largura das colunas
  - _Requirements: 3.2_

- [ ] 7.3 Implementar exportação PDF (iText)
  - Criar documento com cabeçalho e rodapé
  - Adicionar tabela de resultados
  - Adicionar gráfico de pizza (taxa de integridade)
  - Adicionar estatísticas resumidas
  - _Requirements: 3.3_

- [ ] 7.4 Implementar geração de gráficos para PDF
  - Gráfico de pizza: Completos vs Incompletos
  - Usar JFreeChart para gerar imagem
  - Inserir imagem no PDF
  - _Requirements: 3.3_

- [ ] 7.5 Implementar exportação CSV
  - Formato simples: cabeçalho + linhas
  - Separador: ponto-e-vírgula (;)
  - Encoding: UTF-8
  - _Requirements: 3.1_

- [ ] 7.6 Implementar dialog de salvamento
  - JFileChooser com filtros por tipo
  - Nome sugerido: "relatorio_itens_compostos_YYYYMMDD_HHMMSS"
  - Exibir mensagem de sucesso com caminho
  - Opção de abrir arquivo após salvar
  - _Requirements: 3.4, 3.5_

- [ ]* 7.7 Escrever testes de exportação
  - Testar geração de Excel com 4 abas
  - Testar geração de PDF com gráficos
  - Testar geração de CSV
  - Validar estrutura dos arquivos
  - **Property 5: Estrutura de exportação Excel**
  - **Validates: Requirements 3.2**

## Fase 8: Integração com Sistema Existente

- [ ] 8. Integrar com telas existentes
- [ ] 8.1 Adicionar indicadores visuais em PatrimonioFrame
  - Adicionar coluna "Componentes" na JTable
  - Renderizador customizado: ícone 🧩 + contagem
  - Ícone de alerta ⚠️ para componentes faltantes
  - _Requirements: 8.1, 8.4, 8.5_

- [ ] 8.2 Implementar tooltip em PatrimonioFrame
  - Ao passar mouse sobre indicador, exibir tooltip
  - Tooltip: lista de componentes + status
  - _Requirements: 8.2_

- [ ] 8.3 Adicionar filtro "Apenas Compostos" em PatrimonioFrame
  - Checkbox no painel de filtros
  - Filtrar apenas patrimônios com registro em TABELA_ITEM_COMPOSTO
  - _Requirements: 8.3_

- [ ] 8.4 Adicionar opções no menu principal (MainFrame)
  - Menu "Patrimônios" → "Gerenciar Itens Compostos"
  - Menu "Patrimônios" → "Detectar Itens Compostos"
  - Menu "Relatórios" → "Relatório de Itens Compostos"
  - Menu "Configurações" → "Padrões de Detecção"
  - _Requirements: 2.1, 5.1, 6.1_

- [ ] 8.5 Criar PadraoDeteccaoFrame.java
  - Listar padrões configurados
  - Adicionar novo padrão
  - Editar padrão existente
  - Testar padrão (mostrar patrimônios afetados)
  - Ativar/desativar padrão
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 8.6 Criar DeteccaoEmLoteService.java
  - agruparPorDescricao() - agrupa patrimônios por descrição única
  - analisarDescricao(descricao) - sugere componentes automaticamente
  - aplicarEmLote(descricao, componentes, callback) - aplica em todos os patrimônios
  - validarAplicacaoLote() - valida antes de aplicar
  - Usar transação para garantir atomicidade
  - _Requirements: 10.1, 10.2, 10.4, 10.7_

- [ ] 8.7 Criar DeteccaoEmLoteDialog.java (JDialog)
  - JTable: Descrição, Quantidade de Patrimônios, Botão "Configurar"
  - Ordenar por quantidade (maior para menor)
  - Filtro de busca por descrição
  - Label com total de patrimônios agrupados
  - Botão "Atualizar Lista"
  - _Requirements: 10.1_

- [ ] 8.8 Criar ConfigurarDescricaoDialog.java (JDialog)
  - Exibir descrição selecionada e quantidade de patrimônios
  - JTable de componentes sugeridos (editável)
  - Botões: Adicionar, Editar, Remover componente
  - Botão "Aplicar em X Itens" (destaque)
  - Aviso: "Esta ação irá configurar X patrimônios"
  - _Requirements: 10.2, 10.3_

- [ ] 8.9 Criar ProgressoAplicacaoDialog.java (JDialog)
  - JProgressBar com percentual
  - Labels: Itens configurados, Componentes criados, Tempo decorrido
  - Botão "Cancelar" (ativa flag de cancelamento)
  - Atualização em tempo real via ProgressCallback
  - _Requirements: 10.5_

- [ ] 8.10 Implementar aplicação em lote com transação
  - Iniciar transação antes do loop
  - Para cada patrimônio: criar item_composto + componentes
  - Atualizar progresso a cada 10 itens
  - Verificar flag de cancelamento
  - Commit se sucesso, Rollback se erro ou cancelamento
  - _Requirements: 10.4, 10.7_

- [ ] 8.11 Criar ResumoAplicacaoDialog.java (JDialog)
  - Exibir resultado: X patrimônios configurados, Y componentes criados
  - Tempo total de execução
  - Lista de erros (se houver)
  - Botão "Fechar"
  - _Requirements: 10.6_

- [ ]* 8.12 Escrever testes para aplicação em lote
  - Testar agrupamento por descrição
  - Testar aplicação em lote com sucesso
  - Testar rollback em caso de erro
  - Testar cancelamento durante execução
  - **Property 27: Agrupamento correto por descrição**
  - **Property 28: Aplicação em lote atômica**
  - **Property 29: Consistência de contagem em lote**
  - **Property 30: Progresso monotônico**
  - **Validates: Requirements 10.1, 10.4, 10.5, 10.6, 10.7**

## Fase 9: Testes e Validação

- [ ] 9. Executar testes completos
- [ ] 9.1 Checkpoint - Garantir que todos os testes passam
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 9.2 Executar testes de integração
  - Fluxo completo: criar item composto → registrar coleta → gerar relatório
  - Fluxo de detecção: configurar padrão → importar patrimônio → aplicar sugestão
  - Fluxo de análise: criar 2 inventários → comparar → exportar
  - _Requirements: 1.5, 2.3, 5.5, 9.2_

- [ ]* 9.3 Executar testes de performance
  - Relatório com 10.000 itens compostos (< 3 segundos)
  - Exportação Excel com 50.000 linhas (< 10 segundos)
  - Detecção em lote de 100.000 patrimônios (< 30 segundos)
  - _Requirements: 2.3, 3.2, 5.5_

- [ ]* 9.4 Executar testes de usabilidade
  - Coordenadora testa criação de item composto
  - Coordenadora testa geração de relatórios
  - Coordenadora testa exportação
  - Coordenadora testa análise comparativa
  - _Requirements: 1.1, 2.1, 3.1, 9.1_

- [ ]* 9.5 Validar correctness properties
  - Executar property-based tests
  - Validar todas as 26 propriedades
  - Corrigir falhas encontradas
  - _Requirements: Todos_

## Fase 10: Documentação e Treinamento

- [ ] 10. Finalizar documentação
- [ ] 10.1 Criar manual do usuário
  - Como criar item composto
  - Como gerar relatórios
  - Como exportar dados
  - Como configurar padrões de detecção
  - _Requirements: Todos_

- [ ] 10.2 Criar documentação técnica
  - Diagrama de classes
  - Diagrama de banco de dados
  - Fluxos de dados
  - APIs dos Services
  - _Requirements: Todos_

- [ ] 10.3 Preparar material de treinamento
  - Slides de apresentação
  - Vídeos tutoriais
  - FAQ
  - _Requirements: Todos_

- [ ] 10.4 Realizar treinamento com coordenadora
  - Sessão prática de 2 horas
  - Demonstrar todas as funcionalidades
  - Responder dúvidas
  - Coletar feedback
  - _Requirements: Todos_

- [ ] 10.5 Criar guia de troubleshooting
  - Erros comuns e soluções
  - Como interpretar mensagens de erro
  - Contatos de suporte
  - _Requirements: Todos_

## Fase 11: Deploy e Monitoramento

- [ ] 11. Preparar para produção
- [ ] 11.1 Executar scripts de migração em produção
  - Backup do banco de dados
  - Executar scripts SQL
  - Validar integridade
  - _Requirements: 1.5_

- [ ] 11.2 Compilar e empacotar aplicação
  - Build com Maven
  - Gerar JAR executável
  - Incluir dependências (Apache POI, iText, JFreeChart)
  - _Requirements: Todos_

- [ ] 11.3 Instalar em ambiente de produção
  - Copiar JAR para servidor
  - Configurar banco de dados
  - Testar conectividade
  - _Requirements: Todos_

- [ ] 11.4 Monitorar primeiros dias de uso
  - Verificar logs de erro
  - Coletar feedback dos usuários
  - Corrigir bugs críticos rapidamente
  - _Requirements: Todos_

- [ ] 11.5 Checkpoint Final - Validação em produção
  - Ensure all tests pass, ask the user if questions arise.

---

## Resumo de Esforço

| Fase | Descrição | Dias Estimados |
|------|-----------|----------------|
| 1 | Banco de Dados | 1 |
| 2 | Modelos | 1 |
| 3 | DAOs | 2 |
| 4 | Services | 2 |
| 5 | UI - Gestão | 2 |
| 6 | UI - Relatórios | 3 |
| 7 | Exportação | 2 |
| 8 | Integração + Aplicação em Lote | 3 |
| 9 | Testes | 2 |
| 10 | Documentação | 1 |
| 11 | Deploy | 1 |
| **Total** | | **20 dias** |

---

## Dependências entre Fases

- Fase 2 depende de Fase 1 (banco criado)
- Fase 3 depende de Fase 2 (models criados)
- Fase 4 depende de Fase 3 (DAOs criados)
- Fases 5, 6, 7 dependem de Fase 4 (services criados)
- Fase 8 depende de Fases 5, 6, 7 (UIs criadas)
- Fase 9 depende de Fase 8 (tudo implementado)
- Fase 10 pode ser paralela a Fase 9
- Fase 11 depende de Fases 9 e 10 (tudo testado e documentado)

---

## Notas Importantes

- Tarefas marcadas com * são opcionais (testes, documentação avançada)
- Checkpoints garantem que tudo está funcionando antes de prosseguir
- Property-based tests estão distribuídos nas fases relevantes
- Cada tarefa referencia os requirements que valida
- Estimativas são conservadoras, podem ser ajustadas conforme progresso
