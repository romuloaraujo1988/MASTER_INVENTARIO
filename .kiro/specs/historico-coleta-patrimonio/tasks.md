# Implementation Plan: Histórico de Coletas de Patrimônio

## Overview

Implementação da funcionalidade de histórico de coletas de patrimônio no sistema desktop (Java Swing). O histórico permitirá visualizar, filtrar e exportar todas as coletas realizadas para um patrimônio específico ao longo de múltiplos inventários.

**Escopo**: Sistema Desktop (Java Swing)  
**Tecnologias**: Java 21, Swing, PostgreSQL, Apache POI (Excel), iText (PDF)

## Tasks

- [x] 1. Criar estrutura de dados e DTOs
  - Criar `HistoricoColetaDTO` com todos os campos necessários
  - Criar `FiltroHistoricoDTO` para filtros de busca
  - Criar `ComparacaoColetaDTO` para comparação entre coletas
  - Criar `EstatisticasHistoricoDTO` para estatísticas do histórico
  - Criar enum `FormatoExportacao` (PDF, EXCEL)
  - _Requirements: 1.3, 2.1-2.5, 4.1-4.3_

- [x] 2. Implementar HistoricoColetaDAO
  - [x] 2.1 Criar método `buscarColetasPorPatrimonio(patrimonioId, offset, limit)`
    - Query com JOIN para buscar dados completos (inventário, coletor, sala, setor)
    - Ordenar por data_coleta DESC
    - Implementar paginação com OFFSET e LIMIT
    - _Requirements: 1.2, 1.5_
  
  - [x] 2.2 Criar método `buscarColetasComFiltros(patrimonioId, filtros)`
    - Aplicar filtros opcionais: inventarioId, coletorId, dataInicio, dataFim
    - Usar prepared statements para segurança
    - Manter ordenação por data DESC
    - _Requirements: 4.1-4.4_
  
  - [x] 2.3 Criar método `contarColetas(patrimonioId, filtros)`
    - Contar total de coletas com filtros aplicados
    - Necessário para paginação
    - _Requirements: 8.1, 8.2_
  
  - [x] 2.4 Criar método `buscarColetaAnterior(patrimonioId, dataReferencia)`
    - Buscar coleta imediatamente anterior a uma data
    - Necessário para comparação de mudanças
    - _Requirements: 3.1, 3.2_
  
  - [x] 2.5 Criar índices no banco de dados
    - Executar script SQL para criar índices otimizados
    - Índices: patrimônio, inventário, data, composto
    - _Requirements: 8.3_

- [x] 3. Implementar HistoricoColetaService
  - [x] 3.1 Criar método `buscarHistorico(patrimonioId, filtros)`
    - Validar entrada (patrimonioId não nulo e > 0)
    - Verificar se patrimônio existe
    - Buscar coletas via DAO
    - Converter Map para DTO
    - Identificar mudanças entre coletas consecutivas
    - Tratar exceções e retornar mensagens claras
    - _Requirements: 1.1-1.5, 2.1-2.5_
  
  - [x] 3.2 Criar método `compararColetas(coletaId1, coletaId2)`
    - Buscar ambas as coletas
    - Comparar localização e estado
    - Retornar DTO com mudanças identificadas
    - _Requirements: 3.1-3.4_
  
  - [x] 3.3 Criar método `buscarEstatisticas(patrimonioId)`
    - Calcular total de coletas
    - Identificar primeira e última coleta
    - Contar mudanças de localização e estado
    - Contar inventários distintos
    - _Requirements: 7.1-7.3_
  
  - [x] 3.4 Criar método `exportarHistorico(patrimonioId, formato, filtros)`
    - Buscar histórico completo
    - Delegar para gerador apropriado (PDF ou Excel)
    - Retornar bytes do arquivo gerado
    - _Requirements: 5.1-5.4_

- [x] 4. Checkpoint - Testar camada de dados e serviço
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implementar geradores de exportação
  - [x] 5.1 Criar `HistoricoPDFGenerator`
    - Usar iText para gerar PDF
    - Incluir cabeçalho com dados do patrimônio
    - Criar tabela com todas as coletas
    - Aplicar cores para mudanças (localização, estado)
    - Incluir rodapé com data de geração
    - _Requirements: 5.1, 5.3, 5.4_
  
  - [x] 5.2 Criar `HistoricoExcelGenerator`
    - Usar Apache POI para gerar Excel
    - Criar planilha com cabeçalho
    - Adicionar linha para cada coleta
    - Aplicar formatação condicional para mudanças
    - Auto-ajustar largura das colunas
    - _Requirements: 5.2, 5.3, 5.4_

- [x] 5.3 Escrever testes unitários para geradores
  - Testar geração de PDF com histórico vazio
  - Testar geração de Excel com múltiplas coletas
  - Testar formatação de mudanças
  - _Requirements: 5.1-5.4_

- [x] 6. Implementar HistoricoTableModel
  - [x] 6.1 Criar modelo de tabela customizado
    - Definir colunas: Data, Inventário, Coletor, Localização, Estado, Observações
    - Implementar getColumnCount(), getRowCount(), getValueAt()
    - Implementar getColumnName() e getColumnClass()
    - Adicionar método setData(List<HistoricoColetaDTO>)
    - _Requirements: 1.3, 2.1-2.5_
  
  - [x] 6.2 Criar HistoricoTableCellRenderer
    - Aplicar cores para mudanças de localização (amarelo)
    - Aplicar cores para mudanças de estado (vermelho/verde)
    - Destacar coleta mais recente (negrito)
    - Adicionar ícones para tipos de mudança
    - _Requirements: 3.1-3.3, 7.1, 7.4, 7.5_

- [x] 7. Implementar HistoricoColetaPanel (UI principal)
  - [x] 7.1 Criar estrutura básica do painel
    - Layout BorderLayout
    - Painel de filtros no topo (NORTH)
    - Tabela com scroll no centro (CENTER)
    - Painel de ações no rodapé (SOUTH)
    - _Requirements: 1.1, 10.1_
  
  - [x] 7.2 Criar painel de filtros
    - ComboBox para inventário (carregar da base)
    - ComboBox para coletor (carregar da base)
    - JDateChooser para data início
    - JDateChooser para data fim
    - Botão "Aplicar Filtros"
    - Botão "Limpar Filtros"
    - _Requirements: 4.1-4.5_
  
  - [x] 7.3 Criar tabela de histórico
    - JTable com HistoricoTableModel
    - JScrollPane para scroll
    - Configurar largura das colunas
    - Aplicar HistoricoTableCellRenderer
    - Adicionar listener para seleção de linha
    - _Requirements: 1.2-1.5_
  
  - [x] 7.4 Criar painel de ações
    - Botão "Exportar PDF"
    - Botão "Exportar Excel"
    - Label com estatísticas (total de coletas, período)
    - Barra de progresso para operações longas
    - _Requirements: 5.1, 5.2, 10.5_
  
  - [x] 7.5 Implementar carregamento assíncrono
    - Usar SwingWorker para buscar histórico
    - Mostrar loading durante busca
    - Atualizar tabela quando dados chegarem
    - Tratar erros e mostrar mensagens
    - _Requirements: 1.1, 8.1, 8.2_
  
  - [x] 7.6 Implementar aplicação de filtros
    - Capturar valores dos filtros
    - Validar datas (início <= fim)
    - Recarregar histórico com filtros
    - Atualizar estatísticas
    - _Requirements: 4.4, 4.5_
  
  - [x] 7.7 Implementar exportação
    - Capturar formato selecionado
    - Chamar service para gerar arquivo
    - Mostrar diálogo para salvar arquivo
    - Exibir mensagem de sucesso/erro
    - _Requirements: 5.1-5.5_

- [ ] 8. Checkpoint - Testar UI completa
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Integrar histórico na tela de patrimônio
  - [ ] 9.1 Adicionar botão/aba "Histórico" em PatrimonioFrame
    - Adicionar botão na toolbar ou aba no JTabbedPane
    - Configurar ícone e tooltip
    - Adicionar badge se houver coletas recentes
    - _Requirements: 10.1, 10.5_
  
  - [ ] 9.2 Implementar abertura do histórico
    - Ao clicar, abrir HistoricoColetaDialog
    - Passar patrimonioId para o dialog
    - Manter contexto do patrimônio visível
    - _Requirements: 10.2, 10.3_
  
  - [ ] 9.3 Criar HistoricoColetaDialog
    - JDialog modal com HistoricoColetaPanel
    - Título com número e descrição do patrimônio
    - Tamanho adequado (800x600)
    - Botão "Fechar"
    - _Requirements: 10.1-10.4_

- [ ] 10. Implementar auditoria e logs
  - [ ] 10.1 Adicionar log de acesso ao histórico
    - Registrar timestamp, usuário, patrimonioId
    - Usar logger existente do sistema
    - _Requirements: 9.5_
  
  - [ ] 10.2 Adicionar log de exportação
    - Registrar formato, filtros aplicados
    - Registrar sucesso/falha
    - _Requirements: 9.5_
  
  - [ ] 10.3 Implementar validação de imutabilidade
    - Verificar que dados históricos não são modificados
    - Adicionar constraint no banco se necessário
    - _Requirements: 9.3, 9.4_

- [ ] 11. Escrever testes unitários
  - [ ] 11.1 Testes do HistoricoColetaDAO
    - Testar busca básica
    - Testar filtros individuais e combinados
    - Testar paginação
    - Testar ordenação
    - _Requirements: 1.2, 4.4, 8.1, 8.2_
  
  - [ ] 11.2 Testes do HistoricoColetaService
    - Testar validação de entrada
    - Testar comparação de coletas
    - Testar cálculo de estatísticas
    - Testar tratamento de erros
    - _Requirements: 3.1-3.4, 7.1-7.3_
  
  - [ ] 11.3 Testes do HistoricoTableModel
    - Testar getColumnCount, getRowCount
    - Testar getValueAt com dados válidos
    - Testar setData com lista vazia
    - _Requirements: 1.3_

- [ ] 12. Escrever testes de propriedade (Property-Based Tests)
  - [ ] 12.1 Property 1: Ordenação cronológica
    - **Property 1: Ordenação Cronológica do Histórico**
    - **Validates: Requirements 1.2**
    - Gerar patrimônios aleatórios com coletas
    - Verificar que histórico está sempre ordenado DESC
  
  - [ ] 12.2 Property 2: Completude dos dados
    - **Property 2: Completude dos Dados de Coleta**
    - **Validates: Requirements 1.3, 2.1-2.5**
    - Gerar coletas aleatórias
    - Verificar que todos os campos obrigatórios estão presentes
  
  - [ ] 12.3 Property 5: Filtragem correta
    - **Property 5: Filtragem Correta do Histórico**
    - **Validates: Requirements 4.4**
    - Gerar coletas com múltiplos inventários/coletores
    - Aplicar filtros aleatórios
    - Verificar que resultados atendem aos critérios
  
  - [ ] 12.4 Property 6: Round-trip de filtros
    - **Property 6: Round-Trip de Filtros**
    - **Validates: Requirements 4.5**
    - Buscar histórico sem filtros
    - Buscar com filtros vazios
    - Verificar que resultados são idênticos
  
  - [ ] 12.5 Property 8: Completude da exportação
    - **Property 8: Completude da Exportação**
    - **Validates: Requirements 5.3, 5.4**
    - Gerar histórico aleatório
    - Exportar para PDF e Excel
    - Verificar que arquivo contém todos os dados

- [ ] 13. Otimizações e melhorias
  - [ ] 13.1 Implementar cache em memória
    - Cache de históricos recentes (5 minutos)
    - Invalidar cache ao registrar nova coleta
    - _Requirements: 8.4_
  
  - [ ] 13.2 Implementar detecção de intervalos longos
    - Calcular intervalo entre coletas consecutivas
    - Marcar com alerta se > 90 dias
    - Adicionar tooltip explicativo
    - _Requirements: 7.3_
  
  - [ ] 13.3 Implementar limite para históricos grandes
    - Se > 1000 coletas, mostrar aviso
    - Sugerir uso de filtros
    - Limitar visualização inicial a 100 registros
    - _Requirements: 8.5_

- [ ] 14. Checkpoint final - Testes de integração
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 15. Documentação e entrega
  - [ ] 15.1 Atualizar documentação do sistema
    - Adicionar seção sobre histórico de coletas
    - Documentar filtros disponíveis
    - Documentar formatos de exportação
  
  - [ ] 15.2 Criar manual do usuário
    - Como acessar o histórico
    - Como usar filtros
    - Como exportar relatórios
    - Interpretação de cores e ícones
  
  - [ ] 15.3 Preparar para deploy
    - Executar scripts de índices no banco de produção
    - Verificar dependências (iText, Apache POI)
    - Testar em ambiente de homologação

## Notes

- Todos os testes são obrigatórios para garantir qualidade máxima
- Cada task referencia os requirements específicos para rastreabilidade
- Checkpoints garantem validação incremental
- Testes de propriedade usam jqwik com mínimo 100 iterações
- Foco total no sistema desktop (Java Swing)
- Mobile não faz parte deste plano de implementação

## Estimated Effort

- **Tasks 1-4**: Camada de dados e serviço (2-3 dias)
- **Tasks 5**: Geradores de exportação (1-2 dias)
- **Tasks 6-8**: Interface Swing (3-4 dias)
- **Tasks 9**: Integração (1 dia)
- **Tasks 10**: Auditoria (1 dia)
- **Tasks 11-12**: Testes obrigatórios (2-3 dias)
- **Tasks 13**: Otimizações (1-2 dias)
- **Tasks 14-15**: Finalização (1 dia)

**Total estimado**: 12-17 dias (com todos os testes obrigatórios)
