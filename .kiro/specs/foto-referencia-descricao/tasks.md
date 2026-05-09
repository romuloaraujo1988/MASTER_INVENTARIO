# Implementation Plan: Foto de Referência por Descrição

## Overview

Este plano implementa a feature de fotos de referência por descrição em 6 fases:
1. Banco de dados e modelo
2. Processamento de imagem (Desktop)
3. Interface de cadastro (Desktop)
4. API Mobile
5. Sincronização e cache (Android)
6. Exibição no app (Android)

## Tasks

- [x] 1. Criar estrutura de banco de dados
  - [x] 1.1 Criar tabela `tabela_foto_referencia` no PostgreSQL
    - Campos: id, descricao_normalizada, imagem_blob, hash_imagem, tamanho_bytes, data_cadastro, data_atualizacao, ativo, usuario_cadastro
    - Constraint de tamanho máximo 50KB
    - Índices para busca e sincronização
    - _Requirements: 2.1, 2.2_
    - ✅ Implementado: `sql/criar_tabela_foto_referencia.sql`

  - [x] 1.2 Criar modelo Java `FotoReferencia`
    - Entidade JPA com mapeamento para a tabela
    - Campos com validações
    - _Requirements: 2.1, 2.2_
    - ✅ Implementado: `src/main/java/com/inventario/model/FotoReferencia.java`

  - [x] 1.3 Criar DAO `FotoReferenciaDAO`
    - Métodos: salvar, buscarPorDescricao, buscarTodas, buscarAtualizadasDesde, excluir (soft delete)
    - _Requirements: 2.1, 2.5_
    - ✅ Implementado: `src/main/java/com/inventario/dao/FotoReferenciaDAO.java`

- [x] 2. Implementar processamento de imagem
  - [x] 2.1 Criar classe `ImageProcessor`
    - Método `isImagemValida(byte[])`: validar JPG/PNG
    - Método `redimensionar(BufferedImage, maxWidth, maxHeight)`: manter proporção
    - Método `comprimirJpeg(BufferedImage, qualidade)`: comprimir para JPEG 80%
    - Método `processarParaThumbnail(byte[])`: pipeline completo
    - Método `calcularHash(byte[])`: SHA-256 da imagem
    - _Requirements: 1.2, 1.3, 1.6, 2.3, 2.4_
    - ✅ Implementado: `src/main/java/com/inventario/util/ImageProcessor.java`

  - [x] 2.2 Escrever property test para processamento de thumbnail
    - **Property 2: Processamento de Thumbnail**
    - Gerar imagens aleatórias válidas
    - Verificar dimensões <= 200x200 e tamanho <= 50KB
    - **Validates: Requirements 1.3, 2.4**
    - ✅ Implementado: `src/test/java/com/inventario/util/ImageProcessorPropertyTest.java`

  - [x] 2.3 Escrever property test para validação de entrada
    - **Property 1: Validação de Entrada de Imagem**
    - Gerar arquivos aleatórios (válidos e inválidos)
    - Verificar rejeição de não-imagens e arquivos > 2MB
    - **Validates: Requirements 1.2, 1.6**
    - ✅ Implementado: `src/test/java/com/inventario/util/ImageProcessorPropertyTest.java`

- [x] 3. Implementar normalização de descrições
  - [x] 3.1 Criar classe `DescricaoNormalizador`
    - Remover padrões: "PATRIMÔNIO ANAC XXXXXXX", "- PATRIMÔNIO ANAC XXXXXXX"
    - Remover números de série após "N/S:" ou "SÉRIE:"
    - Normalizar espaços e capitalização
    - _Requirements: 3.2_
    - ✅ Implementado: `src/main/java/com/inventario/util/DescricaoNormalizador.java`

  - [x] 3.2 Escrever property test para normalização
    - **Property 4: Normalização de Descrições**
    - Gerar descrições com padrões conhecidos
    - Verificar remoção correta dos padrões
    - **Validates: Requirements 3.2**
    - ✅ Implementado: `src/test/java/com/inventario/util/DescricaoNormalizadorPropertyTest.java`

- [x] 4. Implementar service de foto de referência
  - [x] 4.1 Criar `FotoReferenciaService`
    - Método `buscarDescricoesUnicas()`: agrupar descrições normalizadas com contagem
    - Método `salvarFotoReferencia(descricao, imagem)`: processar e persistir
    - Método `buscarPorDescricao(descricao)`: buscar foto correspondente
    - Método `excluirFotoReferencia(id)`: soft delete
    - Método `obterEstatisticas()`: total, sem foto, uso de armazenamento
    - _Requirements: 1.4, 3.1, 3.3, 3.4, 7.4_
    - ✅ Implementado: `src/main/java/com/inventario/service/FotoReferenciaService.java`

  - [x] 4.2 Escrever property test para completude de metadados
    - **Property 3: Completude de Metadados**
    - Salvar fotos aleatórias
    - Verificar presença de todos os metadados obrigatórios
    - **Validates: Requirements 2.2, 4.4**
    - ✅ Implementado: `src/test/java/com/inventario/service/FotoReferenciaServicePropertyTest.java`

- [x] 5. Checkpoint - Validar camada de negócio
  - ✅ Executados testes unitários e de propriedade
  - ✅ Todos os testes passaram com sucesso
  - ✅ Processamento de imagem funcionando corretamente

- [x] 6. Criar interface de cadastro no Desktop
  - [x] 6.1 Criar `FotoReferenciaFrame`
    - Tabela com descrições únicas (descrição, qtd patrimônios, tem foto)
    - Campo de busca com filtro em tempo real
    - Preview da foto selecionada
    - Botões: Selecionar Imagem, Salvar, Excluir
    - _Requirements: 1.1, 1.5, 1.7, 7.1, 7.2_
    - ✅ Implementado: `src/main/java/com/inventario/view/FotoReferenciaFrame.java`

  - [x] 6.2 Implementar seleção e upload de imagem
    - JFileChooser com filtro JPG/PNG
    - Validação de tamanho (max 2MB)
    - Preview antes de salvar
    - _Requirements: 1.2, 1.6_
    - ✅ Implementado em `FotoReferenciaFrame.java`

  - [x] 6.3 Implementar filtro de busca
    - Filtrar tabela conforme usuário digita
    - Case-insensitive
    - _Requirements: 1.7_
    - ✅ Implementado em `FotoReferenciaFrame.java`

  - [x] 6.4 Escrever property test para filtro de busca
    - **Property 5: Filtro de Busca**
    - Gerar listas de descrições e textos de busca
    - Verificar que resultados contêm o texto buscado
    - **Validates: Requirements 1.7**
    - ✅ Implementado: `src/test/java/com/inventario/view/FotoReferenciaFrameFilterPropertyTest.java`

  - [x] 6.5 Adicionar menu de acesso à tela
    - Adicionar item no menu principal do sistema
    - Verificar permissões de acesso (admin)
    - _Requirements: 1.1_
    - ✅ Implementado em `MainFrame.java` (método `abrirFotosReferencia()`)

- [x] 7. Checkpoint - Validar interface Desktop
  - ✅ Interface implementada e integrada ao menu principal
  - ✅ Funcionalidades de cadastro, substituição e exclusão disponíveis
  - ✅ Filtro de busca funcionando

- [x] 8. Implementar API Mobile
  - [x] 8.1 Criar `FotoReferenciaDTO`
    - Campos: id, descricaoNormalizada, imagemBase64, hashImagem, dataAtualizacao, ativo
    - Conversão de/para entidade
    - _Requirements: 4.1, 4.4_
    - ✅ Implementado: `src/main/java/com/inventario/mobile/server/dto/MobileFotoReferenciaDTO.java`

  - [x] 8.2 Criar `MobileFotoReferenciaService`
    - Método `buscarFotosAtualizadas(timestamp, pagina, tamanho)`: delta sync paginado
    - Método `buscarPorDescricao(descricao)`: busca por descrição normalizada
    - _Requirements: 4.1, 4.2, 4.3_
    - ✅ Implementado: `src/main/java/com/inventario/mobile/server/service/MobileFotoReferenciaService.java`

  - [x] 8.3 Criar `MobileFotoReferenciaController`
    - GET `/api/mobile/fotos-referencia`: lista paginada com delta sync
    - GET `/api/mobile/fotos-referencia/{id}`: busca por ID
    - GET `/api/mobile/fotos-referencia/descricao`: busca por descrição
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
    - ✅ Implementado: `src/main/java/com/inventario/mobile/server/controller/MobileFotoReferenciaController.java`

  - [x] 8.4 Escrever property test para paginação
    - **Property 6: Paginação da API**
    - Gerar requisições com diferentes tamanhos de página
    - Verificar que resultado <= tamanho solicitado (max 50)
    - **Validates: Requirements 4.2**
    - ✅ Implementado: `src/test/java/com/inventario/mobile/server/service/MobileFotoReferenciaServicePaginationPropertyTest.java`

  - [x] 8.5 Escrever property test para delta sync
    - **Property 7: Delta Sync**
    - Gerar timestamps e fotos com diferentes datas
    - Verificar que apenas fotos mais recentes são retornadas
    - **Validates: Requirements 4.3**
    - ✅ Implementado: `src/test/java/com/inventario/mobile/server/service/MobileFotoReferenciaServiceDeltaSyncPropertyTest.java`

- [x] 9. Checkpoint - Validar API
  - ✅ Compilação bem-sucedida
  - ✅ Property tests de paginação implementados
  - ✅ Property tests de delta sync implementados
  - Endpoints implementados:
    - `GET /api/mobile/fotos-referencia` - Lista paginada com delta sync
    - `GET /api/mobile/fotos-referencia/{id}` - Busca por ID
    - `GET /api/mobile/fotos-referencia/descricao` - Busca por descrição
    - `POST /api/mobile/fotos-referencia/batch` - Busca em lote
    - `GET /api/mobile/fotos-referencia/stats` - Estatísticas

- [x] 10. Implementar camada de dados Android
  - [x] 10.1 Criar `FotoReferenciaEntity` (Room)
    - Campos: id, descricaoNormalizada, imagemBlob, hashImagem, dataAtualizacao, ativo
    - Índice para busca por descrição
    - _Requirements: 6.2_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/entity/FotoReferenciaEntity.kt`

  - [x] 10.2 Criar `FotoReferenciaDao`
    - Métodos: buscarTodas, buscarPorDescricao, inserirTodas, desativar, calcularTamanhoTotal
    - _Requirements: 6.2, 6.4_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/local/dao/FotoReferenciaDao.kt`

  - [x] 10.3 Atualizar `AppDatabase`
    - Adicionar entity e dao
    - Incrementar versão do banco (10 → 11)
    - Migration implementada
    - _Requirements: 6.2_
    - ✅ Implementado: `MIGRATION_10_11` em `AppDatabase.kt`

  - [x] 10.4 Criar `FotoReferenciaApi` (Retrofit)
    - Endpoint para buscar fotos com paginação e delta sync
    - _Requirements: 6.1_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/api/FotoReferenciaApi.kt`
    - ✅ DTO: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/remote/dto/FotoReferenciaDTO.kt`

- [x] 11. Implementar repositório e sincronização Android
  - [x] 11.1 Criar `FotoReferenciaRepository`
    - Método `sincronizar()`: baixar fotos novas/atualizadas
    - Método `buscarPorDescricao(descricao)`: buscar no cache local
    - Método `limparFotosAntigas()`: remover fotos não utilizadas se > 100MB
    - _Requirements: 6.1, 6.3, 6.4_
    - ✅ Implementado: 
      - Interface: `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/repository/FotoReferenciaRepository.kt`
      - Implementação: `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/FotoReferenciaRepositoryImpl.kt`

  - [x] 11.2 Criar `SincronizarFotosUseCase`
    - Orquestrar sincronização de fotos
    - Priorizar fotos do inventário ativo
    - _Requirements: 6.1, 6.5_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/SincronizarFotosReferenciaUseCase.kt`

  - [x] 11.3 Integrar sincronização de fotos no `SyncManager`
    - Adicionar sincronização de fotos ao fluxo existente
    - _Requirements: 6.1_
    - ✅ Implementado: `SyncWorker.kt` atualizado com sincronização de fotos

- [x] 12. Implementar exibição de fotos no Android
  - [x] 12.1 Criar `FotoReferenciaHelper`
    - Método `buscarFotoPorDescricao(descricao)`: retorna Bitmap ou null
    - Método `normalizarDescricao(descricao)`: normalização no cliente
    - _Requirements: 5.1, 5.2_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/utils/FotoReferenciaHelper.kt` com cache LRU em memória

  - [x] 12.2 Atualizar `PatrimonioAdapter`
    - Adicionar ImageView para thumbnail
    - Carregar foto de referência ou placeholder
    - _Requirements: 5.3, 5.2_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/inventario/PatrimonioAdapter.kt`
    - ✅ Layout: `InventarioMobile/app/src/main/res/layout/item_patrimonio.xml`

  - [x] 12.3 Atualizar tela de detalhes do patrimônio
    - Exibir foto de referência maior
    - Implementar zoom ao tocar na foto
    - _Requirements: 5.1, 5.5_
    - ✅ Implementado: `InventarioMobile/app/src/main/java/com/inventario/mobile/presentation/dialog/FotoReferenciaDialogFragment.kt` com pinch-to-zoom e double-tap
    - ✅ Layout: `InventarioMobile/app/src/main/res/layout/dialog_foto_referencia.xml`

- [x] 13. Checkpoint Final
  - ✅ Fluxo completo verificado: cadastro → API → sincronização → exibição
  - ✅ Comportamento offline validado
  - ✅ Todas as funcionalidades operacionais
  - ✅ Feature 100% completa

## Status de Implementação por Requisito

| Requisito | Descrição | Status |
|-----------|-----------|--------|
| 1.1 | Menu de cadastro de fotos | ✅ Implementado |
| 1.2 | Upload de imagem JPG/PNG até 2MB | ✅ Implementado |
| 1.3 | Redimensionar para thumbnail 200x200 | ✅ Implementado |
| 1.4 | Salvar foto vinculada à descrição | ✅ Implementado |
| 1.5 | Exibir foto atual e permitir substituição | ✅ Implementado |
| 1.6 | Validar arquivo de imagem | ✅ Implementado |
| 1.7 | Filtrar descrições por texto | ✅ Implementado |
| 2.1 | Armazenar como BLOB no PostgreSQL | ✅ Implementado |
| 2.2 | Armazenar metadados | ✅ Implementado |
| 2.3 | Comprimir para JPEG 80% | ✅ Implementado |
| 2.4 | Tamanho máximo 50KB | ✅ Implementado |
| 2.5 | Soft delete com histórico | ✅ Implementado |
| 3.1 | Agrupar descrições similares | ✅ Implementado |
| 3.2 | Normalizar descrições | ✅ Implementado |
| 3.3 | Aplicar foto a patrimônios similares | ✅ Implementado |
| 3.4 | Mostrar quantidade de patrimônios | ✅ Implementado |
| 4.1 | API retorna fotos em Base64 | ✅ Implementado |
| 4.2 | Paginação (max 50) | ✅ Implementado |
| 4.3 | Delta sync | ✅ Implementado |
| 4.4 | Retornar metadados | ✅ Implementado |
| 4.5 | Lista vazia com status 200 | ✅ Implementado |
| 5.1 | Exibir foto nos detalhes | ✅ Implementado |
| 5.2 | Placeholder quando sem foto | ✅ Implementado |
| 5.3 | Thumbnail na lista | ✅ Implementado |
| 5.4 | Cache local | ✅ Implementado |
| 5.5 | Ampliar foto ao tocar | ✅ Implementado |
| 6.1 | Baixar fotos na sincronização | ✅ Implementado |
| 6.2 | Armazenar no SQLite (Room) | ✅ Implementado |
| 6.3 | Exibir offline | ✅ Implementado |
| 6.4 | Limpar fotos antigas (>100MB) | ✅ Implementado |
| 6.5 | Priorizar fotos do inventário ativo | ✅ Implementado |
| 7.1 | Listar fotos com preview | ✅ Implementado |
| 7.2 | Excluir com confirmação | ✅ Implementado |
| 7.3 | Soft delete e notificar app | ✅ Implementado |
| 7.4 | Exibir estatísticas | ✅ Implementado |

## Notes

- **100% das funcionalidades foram implementadas** ✅
- Property tests usam JUnit 5 (Java) e estrutura Room (Kotlin)
- Mínimo de 100 iterações por property test
- Feature pronta para uso em produção
