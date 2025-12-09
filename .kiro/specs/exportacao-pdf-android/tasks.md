# Implementation Plan

## 1. Setup e Dependências

- [ ] 1.1 Adicionar dependência iTextPDF no build.gradle
  - Adicionar `implementation 'com.itextpdf:itext7-core:7.2.5'` nas dependências
  - Sincronizar projeto
  - _Requirements: 4.1, 9.1_

- [ ] 1.2 Configurar FileProvider para compartilhamento de PDFs
  - Criar arquivo `res/xml/file_paths.xml` com paths de exportação
  - Adicionar provider no AndroidManifest.xml
  - _Requirements: 7.1, 7.2, 7.3_

- [ ] 1.3 Adicionar logo da instituição nos assets
  - Criar pasta `assets/images/` se não existir
  - Adicionar arquivo de logo (PNG ou SVG)
  - _Requirements: 9.1_

## 2. Domain Layer - Models e Interfaces

- [ ] 2.1 Criar modelos de domínio para exportação
  - Criar `ExportFilter.kt` (enum: TODOS, COLETADOS, NAO_COLETADOS)
  - Criar `ExportConfig.kt` (data class com sala, filter, inventarioId, isOffline)
  - Criar `ExportResult.kt` (data class com filePath, fileName, totais)
  - Criar `PdfSummary.kt` (data class com totalItems, coletados, naoColetados, percentual)
  - _Requirements: 3.1, 6.1_

- [ ] 2.2 Criar interface PdfRepository
  - Definir método `generatePdf(patrimonios, sala, filter, isOffline): Result<ExportResult>`
  - Definir método `getExportDirectory(): File`
  - Definir método `sharePdf(filePath): Intent`
  - Definir método `openPdf(filePath): Intent`
  - _Requirements: 4.1, 7.1_

## 3. Domain Layer - Use Cases

- [ ] 3.1 Criar BuscarSalasParaExportacaoUseCase
  - Injetar SalaRepository
  - Implementar busca de todas as salas do banco local
  - Retornar Result<List<Sala>>
  - _Requirements: 1.2, 2.1_

- [ ] 3.2 Criar BuscarPatrimoniosPorSalaUseCase
  - Injetar PatrimonioRepository
  - Implementar busca com filtro (TODOS, COLETADOS, NAO_COLETADOS)
  - Retornar Result<List<Patrimonio>>
  - _Requirements: 3.2, 3.3, 3.4_

- [ ] 3.3 Write property test for export filter
  - **Property 2: Export filter correctness**
  - Testar que TODOS retorna todos os itens
  - Testar que COLETADOS retorna apenas coletado=true
  - Testar que NAO_COLETADOS retorna apenas coletado=false
  - **Validates: Requirements 3.2, 3.3, 3.4**

- [ ] 3.4 Criar GerarRelatorioPdfUseCase
  - Injetar PdfRepository e PatrimonioRepository
  - Buscar patrimônios com filtro aplicado
  - Chamar PdfRepository.generatePdf()
  - Retornar Result<ExportResult>
  - _Requirements: 4.1, 5.1, 6.1_

## 4. Data Layer - PDF Generator

- [ ] 4.1 Criar PdfGenerator class
  - Implementar geração de PDF com iTextPDF
  - Criar método `generate(patrimonios, sala, filter, isOffline, outputFile): PdfSummary`
  - _Requirements: 4.1, 5.1_

- [ ] 4.2 Implementar header do PDF
  - Adicionar logo da instituição
  - Adicionar título "RELATÓRIO DE INVENTÁRIO"
  - Adicionar nome da sala, filtro aplicado e data de geração
  - Adicionar watermark se offline
  - _Requirements: 5.1, 8.2, 9.1_

- [ ] 4.3 Implementar tabela de patrimônios
  - Criar tabela com colunas: Nº Patrimônio, Descrição, Estado, Status Coleta, Data Coleta
  - Configurar repetição do header em múltiplas páginas
  - Formatar células com fontes legíveis e espaçamento adequado
  - _Requirements: 5.2, 5.3, 5.4, 9.3_

- [ ] 4.4 Write property test for multi-page header repetition
  - **Property 7: Multi-page header repetition**
  - Gerar PDF com muitos itens (forçar múltiplas páginas)
  - Verificar que cada página contém o header da tabela
  - **Validates: Requirements 9.3**

- [ ] 4.5 Implementar seção de resumo (somatório)
  - Calcular total de itens, coletados e não coletados
  - Calcular percentual de coleta
  - Formatar seção de resumo ao final do documento
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 4.6 Write property test for summary calculation
  - **Property 3: Summary calculation consistency**
  - Verificar que totalItems = coletados + naoColetados
  - Verificar que percentual = (coletados / totalItems) * 100
  - **Validates: Requirements 6.2, 6.3, 6.4, 6.5**

- [ ] 4.7 Write property test for item count consistency
  - **Property 4: PDF item count consistency**
  - Verificar que contagem no resumo = número de linhas na tabela
  - **Validates: Requirements 10.3**

- [ ] 4.8 Implementar footer com numeração de páginas
  - Adicionar "Página X de Y" em cada página
  - _Requirements: 9.2, 9.4_

- [ ] 4.9 Write property test for special character preservation
  - **Property 5: Special character preservation**
  - Gerar PDF com patrimônios contendo acentos e caracteres especiais
  - Verificar que caracteres são preservados no PDF
  - **Validates: Requirements 10.2**

- [ ] 4.10 Write property test for data integrity
  - **Property 8: Data integrity round-trip**
  - Verificar que todos os campos do patrimônio aparecem no PDF
  - **Validates: Requirements 10.1**

## 5. Data Layer - Repository Implementation

- [ ] 5.1 Criar PdfRepositoryImpl
  - Implementar generatePdf usando PdfGenerator
  - Implementar getExportDirectory (usar getExternalFilesDir)
  - Implementar sharePdf com FileProvider e Intent.ACTION_SEND
  - Implementar openPdf com FileProvider e Intent.ACTION_VIEW
  - _Requirements: 4.1, 7.1, 7.2, 7.3_

- [ ] 5.2 Adicionar queries no PatrimonioDao
  - Adicionar `buscarPorSala(salaId): List<PatrimonioEntity>`
  - Adicionar `buscarColetadosPorSala(salaId): List<PatrimonioEntity>`
  - Adicionar `buscarNaoColetadosPorSala(salaId): List<PatrimonioEntity>`
  - _Requirements: 3.2, 3.3, 3.4_

- [ ] 5.3 Atualizar PatrimonioRepository com métodos de busca por sala
  - Implementar buscarPorSala com filtro
  - Usar queries do DAO
  - _Requirements: 3.2, 3.3, 3.4_

## 6. Dependency Injection

- [ ] 6.1 Criar PdfModule para Hilt
  - Prover PdfGenerator
  - Bind PdfRepository para PdfRepositoryImpl
  - _Requirements: 4.1_

- [ ] 6.2 Atualizar UseCaseModule
  - Adicionar providers para os novos Use Cases
  - _Requirements: 3.1, 3.2, 4.1_

## 7. Presentation Layer - ViewModel

- [ ] 7.1 Criar ExportPdfState sealed class
  - Idle, LoadingSalas, SalasLoaded, Generating, Success, Error, NoData
  - _Requirements: 1.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 7.2 Criar ExportPdfViewModel
  - Injetar Use Cases e NetworkChecker
  - Implementar loadSalas()
  - Implementar selectSala(sala)
  - Implementar selectFilter(filter)
  - Implementar generatePdf()
  - Implementar openPdf(filePath)
  - Implementar sharePdf(filePath)
  - _Requirements: 1.1, 2.2, 3.1, 4.1, 7.1_

- [ ] 7.3 Write unit tests for ExportPdfViewModel
  - Testar estado inicial Idle
  - Testar loadSalas emite LoadingSalas e SalasLoaded
  - Testar generatePdf sem sala emite Error
  - Testar generatePdf com resultado vazio emite NoData
  - _Requirements: 1.3, 4.5_

## 8. Presentation Layer - UI

- [ ] 8.1 Criar layout activity_export_pdf.xml
  - AutoCompleteTextView para seleção de sala
  - RadioGroup com opções TODOS, COLETADOS, NÃO COLETADOS
  - Button para gerar PDF (inicialmente desabilitado)
  - ProgressBar e TextView para progresso
  - Botões de ação (Abrir, Compartilhar) inicialmente ocultos
  - _Requirements: 1.1, 2.1, 3.1, 4.2, 7.1_

- [ ] 8.2 Criar ExportPdfActivity
  - Adicionar @AndroidEntryPoint
  - Injetar ViewModel via by viewModels()
  - Configurar observers para state
  - Implementar seleção de sala com filtro de busca
  - Implementar seleção de filtro
  - Implementar ação de exportação
  - Implementar ações de abrir e compartilhar
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.1, 4.1, 7.1, 7.2, 7.3_

- [ ] 8.3 Write property test for sala search filtering
  - **Property 1: Sala search filtering**
  - Verificar que busca retorna apenas salas cujo nome contém a query
  - **Validates: Requirements 2.3**

- [ ] 8.4 Criar SalaAdapter para AutoCompleteTextView
  - Implementar filtro de busca por nome
  - Exibir nome da sala no dropdown
  - _Requirements: 2.1, 2.3, 2.4_

- [ ] 8.5 Implementar tratamento de erros na UI
  - Mostrar mensagens de erro apropriadas
  - Implementar retry para erros recuperáveis
  - Tratar caso de nenhum visualizador de PDF instalado
  - _Requirements: 1.4, 4.4, 7.4_

- [ ] 8.6 Implementar modo offline
  - Verificar conectividade antes de gerar
  - Desabilitar compartilhamento quando offline
  - Passar flag isOffline para geração do PDF
  - _Requirements: 8.1, 8.3_

- [ ] 8.7 Write property test for offline watermark
  - **Property 6: Offline watermark presence**
  - Gerar PDF em modo offline
  - Verificar presença do watermark de aviso
  - **Validates: Requirements 8.2**

## 9. Navigation e Integração

- [ ] 9.1 Adicionar entrada no menu principal
  - Adicionar item "Exportar PDF" no menu ou dashboard
  - Configurar navegação para ExportPdfActivity
  - _Requirements: 1.1_

- [ ] 9.2 Registrar Activity no AndroidManifest
  - Adicionar ExportPdfActivity
  - _Requirements: 1.1_

## 10. Checkpoint - Testes e Validação

- [ ] 10. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## 11. Final Checkpoint

- [ ] 11. Final Checkpoint - Make sure all tests are passing
  - Ensure all tests pass, ask the user if questions arise.
