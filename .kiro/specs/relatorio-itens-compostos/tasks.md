# Implementation Plan - Relatório de Integridade de Itens Compostos

## Fase 1: Camada de Modelo (Domain)

- [x] 1. Criar classes de modelo

- [x] 1.1 Criar enums StatusIntegridade e StatusComponente


  - Criar `src/main/java/com/inventario/model/StatusIntegridade.java`
  - Criar `src/main/java/com/inventario/model/StatusComponente.java`
  - StatusIntegridade: COMPLETO, INCOMPLETO, PARCIAL, TODOS
  - StatusComponente: ENCONTRADO, FALTANTE, PARCIAL, NAO_COLETADO
  - Incluir cores associadas para UI (java.awt.Color)
  - _Requirements: 3.2, 3.3_

- [x] 1.2 Criar classe ItemCompostoResumo.java


  - Criar `src/main/java/com/inventario/model/ItemCompostoResumo.java`
  - Atributos: idPatrimonio, numeroPatrimonio, descricaoPatrimonio, nomeSala, nomeResponsavel
  - Atributos de contagem: totalComponentes, componentesEsperados, componentesEncontrados, componentesFaltantes
  - Atributos calculados: taxaIntegridade, status, tiposFaltantes
  - Métodos: isCompleto(), getComponentesFaltantesFormatado()
  - _Requirements: 3.1, 3.4_


- [x] 1.3 Criar classe ComponenteDetalhe.java

  - Criar `src/main/java/com/inventario/model/ComponenteDetalhe.java`
  - Atributos: id, tipo, descricao, quantidadeEsperada, quantidadeEncontrada, status, observacao, dataColeta
  - Método: getQuantidadeFaltante()
  - _Requirements: 7.3_

- [x] 1.4 Criar classe EstatisticasIntegridade.java


  - Criar `src/main/java/com/inventario/model/EstatisticasIntegridade.java`
  - Atributos: totalConjuntos, conjuntosCompletos, conjuntosIncompletos, taxaIntegridadeGeral
  - Método: isAlerta() - retorna true se taxa < 80%
  - _Requirements: 4.1, 4.3, 4.4_

- [x] 1.5 Criar classe FiltroRelatorioItemComposto.java


  - Criar `src/main/java/com/inventario/model/FiltroRelatorioItemComposto.java`
  - Atributos: idInventario, idSetor, idSala, idResponsavel, statusIntegridade
  - Método: temFiltrosAtivos()
  - _Requirements: 2.1_

- [x]* 1.6 Escrever testes unitários para métodos de negócio dos models
  - ✅ 94 testes criados e passando
  - ✅ Testar isCompleto() com diferentes cenários
  - ✅ Testar cálculo de taxa de integridade
  - ✅ Testar isAlerta() com valores limite (79%, 80%, 81%)
  - **Property 7: Cor da taxa de integridade segue regra de 80%**
  - **Validates: Requirements 4.3, 4.4**

## Fase 2: Camada de Acesso a Dados (DAO)

- [x] 2. Implementar DAO de relatório


- [x] 2.1 Criar classe ItemCompostoRelatorioDAO.java


  - Criar `src/main/java/com/inventario/dao/ItemCompostoRelatorioDAO.java`
  - Método: buscarResumoComFiltros(FiltroRelatorioItemComposto filtro) → List<ItemCompostoResumo>
  - Método: calcularEstatisticas(FiltroRelatorioItemComposto filtro) → EstatisticasIntegridade
  - Método: buscarComponentesPorPatrimonio(Integer idPatrimonio, Integer idInventario) → List<ComponenteDetalhe>
  - Métodos para filtros: listarInventariosComItensCompostos(), listarSetoresComItensCompostos(), listarSalasComItensCompostos(Integer idSetor), listarResponsaveisComItensCompostos()
  - Usar PreparedStatements para prevenir SQL Injection
  - Implementar query principal com filtros dinâmicos (WHERE 1=1 pattern)
  - Otimizar com índices existentes (idx_item_composto_patrimonio, idx_coleta_componente_item)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 4.1_

- [ ]* 2.2 Escrever testes para DAO
  - Testar busca com filtro único
  - Testar busca com múltiplos filtros
  - Testar cálculo de estatísticas
  - **Property 3: Múltiplos filtros aplicam operação AND lógica**
  - **Validates: Requirements 2.4**

## Fase 3: Camada de Serviço (Business Logic)

- [x] 3. Implementar Services


- [x] 3.1 Criar RelatorioItemCompostoService.java


  - Criar `src/main/java/com/inventario/service/RelatorioItemCompostoService.java`
  - Injetar ItemCompostoRelatorioDAO
  - Método: gerarRelatorio(FiltroRelatorioItemComposto filtro) → List<ItemCompostoResumo>
  - Método: calcularEstatisticas(FiltroRelatorioItemComposto filtro) → EstatisticasIntegridade
  - Método: obterDetalhes(Integer idPatrimonio, Integer idInventario) → List<ComponenteDetalhe>
  - Método: ordenar(List<ItemCompostoResumo> itens, String coluna, boolean crescente) → List<ItemCompostoResumo>
  - Implementar lógica de ordenação com Comparator
  - Ordenação numérica para taxa de integridade (não alfabética)
  - _Requirements: 2.2, 3.1, 4.1, 6.1, 6.2, 6.3, 6.4_

- [x]* 3.2 Escrever testes para ordenação
  - ✅ 20 testes criados e passando (RelatorioItemCompostoServiceTest.java)
  - ✅ Testar ordenação crescente e decrescente
  - ✅ Testar ordenação numérica vs alfabética
  - ✅ Testar que filtros são mantidos
  - **Property 10: Ordenação mantém dados corretos**
  - **Property 11: Ordenação numérica para taxa de integridade**
  - **Validates: Requirements 6.1, 6.2, 6.3, 6.4**

- [x] 3.3 Criar ExportacaoItemCompostoService.java


  - Criar `src/main/java/com/inventario/service/ExportacaoItemCompostoService.java`
  - Método: exportarExcel(List<ItemCompostoResumo> itens, EstatisticasIntegridade stats, String caminhoArquivo) → File
  - Método: exportarPDF(List<ItemCompostoResumo> itens, EstatisticasIntegridade stats, String caminhoArquivo) → File
  - Método: exportarCSV(List<ItemCompostoResumo> itens, String caminhoArquivo) → File
  - _Requirements: 5.1, 5.2, 5.3, 5.4_


- [x] 3.4 Implementar exportação Excel (Apache POI)


  - Usar Apache POI (já disponível no projeto)
  - Criar workbook com 2 abas: "Resumo" e "Detalhamento"
  - Aba Resumo: estatísticas gerais formatadas (Total, Completos, Incompletos, Taxa)
  - Aba Detalhamento: todos os itens com colunas (Nº Patrimônio, Descrição, Sala, Responsável, Esperados, Encontrados, Faltantes, Taxa, Status)
  - Formatação: cabeçalhos em negrito, cores por status (verde/vermelho)
  - _Requirements: 5.2_



- [x] 3.5 Implementar exportação PDF (iText)
  - Usar iText 7.2.5 (já disponível no projeto)
  - Cabeçalho institucional (IFMT - Sistema de Inventário)
  - Estatísticas resumidas em tabela
  - Tabela de dados formatada com cores por status
  - _Requirements: 5.3_

- [x] 3.6 Implementar exportação CSV
  - Separador: ponto-e-vírgula (;)
  - Encoding: UTF-8 com BOM
  - Cabeçalho + linhas de dados
  - _Requirements: 5.4_

- [x]* 3.7 Escrever testes para exportação

  - ✅ 18 testes criados e passando (ExportacaoItemCompostoServiceTest.java)
  - ✅ Testar estrutura do Excel (2 abas)
  - Testar formato do CSV (separador, encoding)
  - Testar geração de PDF
  - **Property 8: Exportação Excel contém estrutura correta**
  - **Property 9: Exportação CSV usa formato correto**
  - **Validates: Requirements 5.2, 5.4**

## Fase 4: Camada de ViewModel (MVVM)

- [x] 4. Implementar ViewModel



- [x] 4.1 Criar RelatorioItemCompostoState.java

  - Criar `src/main/java/com/inventario/presentation/state/RelatorioItemCompostoState.java`
  - Usar sealed interface pattern (como RelatorioState.java existente)
  - Estados: Idle, Loading, Success(itens, estatisticas), DetalhesCarregados(detalhes), ExportacaoSucesso(caminhoArquivo), Error(mensagem)
  - _Requirements: 1.3, 3.1, 5.5_


- [x] 4.2 Criar RelatorioItemCompostoViewModel.java

  - Criar `src/main/java/com/inventario/presentation/viewmodel/RelatorioItemCompostoViewModel.java`
  - Seguir padrão do RelatorioViewModel.java existente
  - PropertyChangeSupport para observer pattern
  - Injetar RelatorioItemCompostoService e ExportacaoItemCompostoService
  - Métodos: carregarRelatorio(FiltroRelatorioItemComposto), ordenarPor(String coluna), exportar(FormatoExportacao, String caminho), carregarDetalhes(Integer idPatrimonio)
  - Métodos para filtros: carregarInventarios(), carregarSetores(), carregarSalas(Integer idSetor), carregarResponsaveis()
  - Gerenciamento de estado com RelatorioItemCompostoState
  - _Requirements: 1.3, 2.1, 3.5, 5.1_

- [ ]* 4.3 Escrever testes para ViewModel
  - Testar transições de estado
  - Testar carregamento de dados
  - Testar notificação de observers
  - **Property 6: Estatísticas são consistentes com dados filtrados**
  - **Validates: Requirements 4.2**

## Fase 5: Camada de Apresentação - Refatorar Janela Principal

- [x] 5. Refatorar tela principal do relatório para MVVM


- [x] 5.1 Refatorar RelatorioItensCompostosFrame.java para usar ViewModel


  - Arquivo existente: `src/main/java/com/inventario/view/RelatorioItensCompostosFrame.java`
  - Remover acesso direto ao ItemCompostoDAO
  - Injetar RelatorioItemCompostoViewModel
  - Implementar PropertyChangeListener para observar estado
  - Manter layout existente (BorderLayout com filtros, estatísticas, tabela, botões)
  - _Requirements: 1.2, 2.1, 3.1, 4.1_


- [x] 5.2 Refatorar painel de filtros para usar ViewModel
  - Adicionar ComboBox: Setor (carregado via ViewModel)
  - Adicionar ComboBox: Responsável (carregado via ViewModel)
  - Implementar filtro de Sala por Setor (cascata)
  - Conectar botões "Aplicar Filtros" e "Limpar Filtros" ao ViewModel
  - _Requirements: 2.1, 2.5_

- [x] 5.3 Refatorar painel de estatísticas
  - Usar EstatisticasIntegridade do ViewModel
  - Adicionar Label: Taxa Geral de Integridade
  - Implementar cor condicional (verde >= 80%, vermelho < 80%)
  - Atualização automática via observer pattern
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 5.4 Refatorar JTable de resultados
  - Usar List<ItemCompostoResumo> do ViewModel
  - Adicionar colunas: Responsável, Taxa de Integridade
  - Implementar ordenação por clique no cabeçalho (via ViewModel.ordenarPor())
  - Adicionar coluna "Componentes Faltantes" com tipos formatados
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 6.1_

- [x] 5.5 Melhorar destaque visual por status
  - Usar StatusIntegridade.getCor() para cores
  - Verde para COMPLETO
  - Vermelho para INCOMPLETO
  - Laranja para PARCIAL
  - Tooltip com taxa de integridade e componentes faltantes
  - _Requirements: 3.2, 3.3_

- [ ]* 5.6 Escrever testes de UI
  - Testar que cores correspondem ao status
  - Testar ordenação por clique
  - **Property 4: Destaque visual corresponde ao status de integridade**
  - **Validates: Requirements 3.2, 3.3**

## Fase 6: Camada de Apresentação - Diálogos

- [x] 6. Criar diálogos auxiliares

- [x] 6.1 Criar DetalheItemCompostoDialog.java (JDialog)


  - Criar `src/main/java/com/inventario/view/DetalheItemCompostoDialog.java`
  - Exibir: número patrimônio, descrição, sala, responsável
  - JTable com componentes: tipo, descrição, qtd esperada, qtd encontrada, status, data coleta
  - TableCellRenderer para destaque em vermelho de componentes faltantes
  - Botão "Fechar"
  - Tamanho: 800x500
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 6.2 Implementar abertura do diálogo de detalhes
  - Adicionar MouseListener para duplo clique na tabela principal
  - Carregar componentes via ViewModel.carregarDetalhes(idPatrimonio)
  - Observar estado DetalhesCarregados para abrir diálogo
  - Manter seleção na tabela principal ao fechar
  - _Requirements: 7.1, 7.5_

- [ ]* 6.3 Escrever testes para diálogo de detalhes
  - Testar que todos os componentes são exibidos
  - Testar destaque de componentes faltantes
  - **Property 12: Detalhamento exibe todos os componentes**
  - **Property 13: Destaque de componentes faltantes no detalhamento**
  - **Validates: Requirements 7.3, 7.4**

- [x] 6.4 Implementar fluxo de exportação
  - Substituir placeholders existentes em btnExportarExcel e btnExportarPDF
  - Usar JFileChooser para selecionar destino
  - Nome sugerido: "RelatorioItensCompostos_YYYYMMDD_HHmmss.xlsx/pdf/csv"
  - Chamar ViewModel.exportar(formato, caminho)
  - Observar estado ExportacaoSucesso para exibir mensagem
  - Opção de abrir arquivo após salvar (Desktop.open())
  - _Requirements: 5.1, 5.5, 5.6_

## Fase 7: Integração com Sistema

- [x] 7. Integrar com sistema existente
- [x] 7.1 Verificar item no menu principal (MainFrame)


  - Verificar se já existe menu "Relatórios" → "Relatório de Itens Compostos" em MainFrame.java
  - Se não existir, adicionar item de menu
  - Conectar ao RelatorioItensCompostosFrame refatorado
  - _Requirements: 1.1_

- [x] 7.2 Implementar carregamento inicial via ViewModel
  - Carregar inventário ativo automaticamente no construtor
  - Exibir mensagem se não houver inventário ativo (JOptionPane)
  - Permitir selecionar inventário manualmente via ComboBox
  - Carregar dados iniciais: inventários, setores, salas, responsáveis
  - _Requirements: 1.3, 1.4_

- [x] 7.3 Implementar observer do ViewModel completo
  - Adicionar PropertyChangeListener no construtor do Frame
  - Usar SwingUtilities.invokeLater() para atualizar UI
  - Exibir cursor de espera durante Loading
  - Tratar erros com JOptionPane e mensagens amigáveis
  - _Requirements: 1.3_

## Fase 8: Testes e Validação

- [-] 8. Executar testes completos
- [x] 8.1 Checkpoint - Garantir que o projeto compila



  - Executar `mvn clean compile` para verificar compilação
  - Corrigir erros de compilação se houver
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 8.2 Executar testes de integração manual
  - Fluxo completo: abrir → filtrar → ordenar → exportar
  - Testar com dados reais (1.588 itens compostos)
  - Testar detalhamento de item específico
  - Testar exportação Excel, PDF e CSV
  - _Requirements: Todos_

- [ ]* 8.3 Validar performance
  - Carregar relatório com todos os itens (< 3 segundos)
  - Aplicar filtros (< 1 segundo)
  - Exportar Excel com todos os dados (< 5 segundos)
  - _Requirements: 3.1, 5.2_

- [x] 8.4 Checkpoint Final - Validação completa
  - ✅ Projeto compila com sucesso (BUILD SUCCESS)
  - ✅ Todas as classes implementadas e integradas
  - ✅ Menu "Relatório de Itens Compostos" adicionado ao MainFrame
  - ✅ ViewModel com observer pattern funcionando
  - ✅ Exportação Excel, PDF e CSV implementada
  - ✅ Diálogo de detalhes implementado
  - ✅ Layout corrigido: botões movidos para painel de filtros (03/12/2025)
  - Testes manuais opcionais (Tasks 8.2*, 8.3*) podem ser executados pelo usuário

---

## Resumo de Esforço

| Fase | Descrição | Horas Estimadas |
|------|-----------|-----------------|
| 1 | Modelos (5 classes) | 2h |
| 2 | DAO (1 classe) | 2h |
| 3 | Services (2 classes + exportação) | 4h |
| 4 | ViewModel (2 classes) | 2h |
| 5 | UI Principal (refatoração) | 3h |
| 6 | Diálogos (1 classe + exportação) | 2h |
| 7 | Integração | 1h |
| 8 | Testes | 2h |
| **Total** | | **18 horas** |

---

## Dependências entre Fases

```
Fase 1 (Models) ──────────────────────────────────────┐
                                                       │
Fase 2 (DAO) ─────────────────────────────────────────┤
       ↓                                               │
Fase 3 (Services) ────────────────────────────────────┤
       ↓                                               │
Fase 4 (ViewModel) ───────────────────────────────────┤
       ↓                                               │
Fase 5 (UI Principal) ────────────────────────────────┤
       ↓                                               │
Fase 6 (Diálogos) ────────────────────────────────────┤
       ↓                                               │
Fase 7 (Integração) ──────────────────────────────────┤
       ↓                                               │
Fase 8 (Testes) ──────────────────────────────────────┘
```

---

## Arquivos a Criar/Modificar

### Novos Arquivos (10)
1. `src/main/java/com/inventario/model/StatusIntegridade.java`
2. `src/main/java/com/inventario/model/StatusComponente.java`
3. `src/main/java/com/inventario/model/ItemCompostoResumo.java`
4. `src/main/java/com/inventario/model/ComponenteDetalhe.java`
5. `src/main/java/com/inventario/model/EstatisticasIntegridade.java`
6. `src/main/java/com/inventario/model/FiltroRelatorioItemComposto.java`
7. `src/main/java/com/inventario/dao/ItemCompostoRelatorioDAO.java`
8. `src/main/java/com/inventario/service/RelatorioItemCompostoService.java`
9. `src/main/java/com/inventario/service/ExportacaoItemCompostoService.java`
10. `src/main/java/com/inventario/presentation/state/RelatorioItemCompostoState.java`
11. `src/main/java/com/inventario/presentation/viewmodel/RelatorioItemCompostoViewModel.java`
12. `src/main/java/com/inventario/view/DetalheItemCompostoDialog.java`

### Arquivos a Modificar (2)
1. `src/main/java/com/inventario/view/RelatorioItensCompostosFrame.java` - Refatorar para MVVM
2. `src/main/java/com/inventario/view/MainFrame.java` - Verificar/adicionar menu

---

## Notas Importantes

- Tarefas marcadas com * são opcionais (testes)
- Checkpoints garantem que tudo está funcionando antes de prosseguir
- Property-based tests estão distribuídos nas fases relevantes
- Cada tarefa referencia os requirements que valida
- Utilizar estrutura de banco existente (tabela_item_composto, tabela_coleta_componente)
- Seguir padrão MVVM do RelatorioFrame.java e RelatorioViewModel.java existentes
- Usar Apache POI 5.4.0 para Excel (já disponível)
- Usar iText 7.2.5 para PDF (já disponível)
- Índices já existem: idx_item_composto_patrimonio, idx_coleta_componente_item

