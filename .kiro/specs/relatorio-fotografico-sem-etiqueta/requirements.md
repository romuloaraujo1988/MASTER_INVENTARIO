# Requirements Document

## Introduction

Esta feature integra os endpoints de relatório fotográfico já existentes no servidor ao app Android. O servidor expõe dois endpoints:

- `GET /api/mobile/relatorios/fotos/{inventarioId}?tipo=sem_etiqueta` — gera e retorna um PDF com as fotos dos itens sem etiqueta (requer role SUPERVISOR+)
- `GET /api/mobile/relatorios/fotos/{inventarioId}/info` — retorna contagens de fotos por tipo (requer role CONSULTA+)

O app Android atualmente não consome nenhum desses endpoints. O objetivo é permitir que o usuário, a partir da tela de visualização de coletas com o filtro "Sem Etiqueta" ativo, veja quantos itens têm foto e, se tiver permissão de SUPERVISOR, baixe e visualize o relatório em PDF diretamente no dispositivo.

## Glossary

- **RelatorioFotoApi**: Interface Retrofit responsável por consumir os dois endpoints de relatório fotográfico.
- **RelatorioFotoInfo**: Modelo de domínio com as contagens de fotos retornadas pelo endpoint `/info`.
- **BuscarInfoRelatorioFotoUseCase**: Use Case que chama o endpoint `/info` e retorna um `RelatorioFotoInfo`.
- **BaixarRelatorioFotoPdfUseCase**: Use Case que chama o endpoint de geração de PDF e salva o arquivo localmente.
- **CollectionViewViewModelClean**: ViewModel existente da tela de visualização de coletas, que será estendido com as novas funcionalidades.
- **RelatorioFotoState**: Sealed class que representa os estados da operação de relatório fotográfico na UI.
- **FileProvider**: Mecanismo Android para compartilhar arquivos entre apps de forma segura via `content://` URI.
- **Inventario_Ativo**: Inventário em andamento cujo ID é obtido via `PreferencesManager.getInventarioAtivoId()`.
- **SUPERVISOR**: Role de usuário com acesso a relatórios gerenciais (inclui ADMIN).
- **CONSULTA**: Role mínima para acesso a dados de leitura (inclui COLETOR, SUPERVISOR e ADMIN).

## Requirements

### Requirement 1: Consulta de Informações de Fotos

**User Story:** Como um usuário autenticado com qualquer role, quero ver quantos itens sem etiqueta possuem fotos registradas, para saber se há material fotográfico disponível antes de solicitar o relatório.

#### Acceptance Criteria

1. WHEN o usuário ativa o filtro "Sem Etiqueta" na tela de visualização de coletas, THE App SHALL chamar o endpoint `GET /api/mobile/relatorios/fotos/{inventarioId}/info` usando o ID do inventário ativo.
2. WHEN a resposta do endpoint `/info` é recebida com sucesso, THE App SHALL exibir o número de itens sem etiqueta que possuem foto (campo `semEtiqueta` da resposta).
3. WHEN o campo `semEtiqueta` da resposta é zero, THE App SHALL exibir a mensagem "Nenhum item sem etiqueta possui foto registrada".
4. IF a chamada ao endpoint `/info` falhar por erro de rede, THEN THE App SHALL exibir a mensagem "Não foi possível carregar informações de fotos" e ocultar o indicador de contagem.
5. WHEN não há inventário ativo salvo localmente, THE App SHALL omitir a chamada ao endpoint `/info` e não exibir o indicador de contagem de fotos.

---

### Requirement 2: Exibição do Botão de Relatório PDF

**User Story:** Como um usuário com role SUPERVISOR ou ADMIN, quero ver um botão para baixar o relatório fotográfico dos itens sem etiqueta, para acessar as evidências fotográficas diretamente no app.

#### Acceptance Criteria

1. WHEN o filtro "Sem Etiqueta" está ativo E o perfil do usuário é SUPERVISOR ou ADMIN, THE App SHALL exibir um botão de ação flutuante (FAB) ou botão de menu para "Baixar Relatório Fotográfico".
2. WHEN o filtro "Sem Etiqueta" está ativo E o perfil do usuário é COLETOR ou CONSULTA, THE App SHALL ocultar o botão de download do relatório PDF.
3. WHEN o filtro "Sem Etiqueta" não está ativo, THE App SHALL ocultar o botão de download do relatório PDF independentemente do perfil do usuário.
4. THE App SHALL determinar o perfil do usuário lendo o valor retornado por `PreferencesManager.getUserProfile()`.

---

### Requirement 3: Download do Relatório PDF

**User Story:** Como um usuário com role SUPERVISOR ou ADMIN, quero baixar o relatório fotográfico dos itens sem etiqueta, para visualizá-lo no dispositivo.

#### Acceptance Criteria

1. WHEN o usuário aciona o botão de download do relatório, THE App SHALL exibir um indicador de progresso e chamar o endpoint `GET /api/mobile/relatorios/fotos/{inventarioId}?tipo=sem_etiqueta`.
2. WHEN o PDF é recebido com sucesso (HTTP 200 com `Content-Type: application/pdf`), THE App SHALL salvar o arquivo no diretório de cache do app (`context.cacheDir`) com nome no formato `relatorio_sem_etiqueta_{inventarioId}_{timestamp}.pdf`.
3. WHEN o arquivo PDF é salvo com sucesso, THE App SHALL abrir o visualizador de PDF nativo do Android usando um `Intent` com `ACTION_VIEW`, URI gerada via `FileProvider` e tipo MIME `application/pdf`.
4. IF o servidor retornar HTTP 204 (sem conteúdo), THEN THE App SHALL exibir a mensagem "Nenhuma foto disponível para gerar o relatório".
5. IF o servidor retornar HTTP 403, THEN THE App SHALL exibir a mensagem "Você não tem permissão para acessar este relatório".
6. IF a chamada ao endpoint falhar por erro de rede ou timeout, THEN THE App SHALL exibir a mensagem "Erro de conexão. Verifique a rede e tente novamente".
7. IF nenhum aplicativo no dispositivo for capaz de abrir arquivos PDF, THEN THE App SHALL exibir a mensagem "Nenhum visualizador de PDF encontrado. Instale um app para abrir PDFs".
8. WHEN o download está em andamento, THE App SHALL desabilitar o botão de download para evitar requisições duplicadas.

---

### Requirement 4: Interface Retrofit para os Endpoints

**User Story:** Como desenvolvedor, quero uma interface Retrofit tipada para os dois endpoints de relatório fotográfico, para que as chamadas de rede sigam os padrões já estabelecidos no projeto.

#### Acceptance Criteria

1. THE RelatorioFotoApi SHALL declarar um método `buscarInfo` anotado com `@GET("api/mobile/relatorios/fotos/{inventarioId}/info")` que retorna `ApiResponse<Map<String, Any>>`.
2. THE RelatorioFotoApi SHALL declarar um método `baixarPdf` anotado com `@GET("api/mobile/relatorios/fotos/{inventarioId}")` com `@Query("tipo")` fixo em `"sem_etiqueta"` e `@Streaming` para suportar respostas binárias grandes, retornando `retrofit2.Response<okhttp3.ResponseBody>`.
3. THE ApiModule SHALL prover uma instância singleton de `RelatorioFotoApi` usando o `Retrofit` já configurado, seguindo o padrão dos demais providers do módulo.
4. THE RelatorioFotoApi SHALL usar o prefixo de URL `api/mobile/` em todos os endpoints, conforme a regra `endpoints-nao-alterar.md`.

---

### Requirement 5: Use Cases de Domínio

**User Story:** Como desenvolvedor, quero Use Cases isolados para buscar informações e baixar o PDF, para manter a lógica de negócio separada da camada de apresentação.

#### Acceptance Criteria

1. THE BuscarInfoRelatorioFotoUseCase SHALL receber o `inventarioId` como parâmetro, chamar `RelatorioFotoApi.buscarInfo` e retornar `Result<RelatorioFotoInfo>`.
2. THE BaixarRelatorioFotoPdfUseCase SHALL receber o `inventarioId` como parâmetro, chamar `RelatorioFotoApi.baixarPdf`, gravar os bytes no arquivo de destino e retornar `Result<File>`.
3. IF `RelatorioFotoApi.baixarPdf` retornar HTTP 204, THEN THE BaixarRelatorioFotoPdfUseCase SHALL retornar `Result.failure` com uma exceção do tipo `SemFotosException`.
4. IF `RelatorioFotoApi.baixarPdf` retornar HTTP 403, THEN THE BaixarRelatorioFotoPdfUseCase SHALL retornar `Result.failure` com uma exceção do tipo `PermissaoNegadaException`.
5. THE BaixarRelatorioFotoPdfUseCase SHALL gravar o arquivo usando `OutputStream` com buffer de 8 KB para evitar `OutOfMemoryError` em PDFs grandes.

---

### Requirement 6: Integração com o ViewModel Existente

**User Story:** Como desenvolvedor, quero que as novas funcionalidades sejam integradas ao `CollectionViewViewModelClean` existente, para evitar duplicação de ViewModels e manter a coesão da tela.

#### Acceptance Criteria

1. THE CollectionViewViewModelClean SHALL expor um `StateFlow<RelatorioFotoState>` chamado `relatorioFotoState` com valor inicial `RelatorioFotoState.Idle`.
2. WHEN o método `carregarInfoRelatorioFoto(inventarioId: Int)` é chamado, THE CollectionViewViewModelClean SHALL emitir `RelatorioFotoState.Loading` e então chamar `BuscarInfoRelatorioFotoUseCase`.
3. WHEN o método `baixarRelatorioFoto(inventarioId: Int)` é chamado, THE CollectionViewViewModelClean SHALL emitir `RelatorioFotoState.Downloading` e então chamar `BaixarRelatorioFotoPdfUseCase`.
4. WHEN `BuscarInfoRelatorioFotoUseCase` retorna sucesso, THE CollectionViewViewModelClean SHALL emitir `RelatorioFotoState.InfoCarregada` com o objeto `RelatorioFotoInfo`.
5. WHEN `BaixarRelatorioFotoPdfUseCase` retorna sucesso, THE CollectionViewViewModelClean SHALL emitir `RelatorioFotoState.PdfPronto` com o `File` do PDF salvo.
6. WHEN qualquer Use Case retorna falha, THE CollectionViewViewModelClean SHALL emitir `RelatorioFotoState.Erro` com a mensagem de erro apropriada.

---

### Requirement 7: Abertura do PDF no Visualizador Nativo

**User Story:** Como usuário, quero que o PDF seja aberto automaticamente no visualizador de PDF do meu dispositivo após o download, para não precisar navegar manualmente até o arquivo.

#### Acceptance Criteria

1. WHEN `RelatorioFotoState.PdfPronto` é emitido, THE CollectionViewActivity SHALL construir um `Intent` com `ACTION_VIEW`, URI via `FileProvider.getUriForFile` e tipo MIME `application/pdf`.
2. THE CollectionViewActivity SHALL adicionar a flag `Intent.FLAG_GRANT_READ_URI_PERMISSION` ao `Intent` antes de iniciá-lo.
3. THE CollectionViewActivity SHALL envolver a chamada a `startActivity` em um bloco `try/catch` para `ActivityNotFoundException` e exibir a mensagem de erro do Requirement 3.7 caso nenhum app suporte o Intent.
4. THE FileProvider SHALL estar declarado no `AndroidManifest.xml` com `android:authorities` igual a `${applicationId}.provider` e apontar para o diretório `cache` do app.

---

### Requirement 8: Limpeza de Arquivos Temporários

**User Story:** Como desenvolvedor, quero que os PDFs baixados sejam removidos do cache quando não forem mais necessários, para evitar acúmulo de arquivos temporários no dispositivo.

#### Acceptance Criteria

1. WHEN `CollectionViewActivity` é destruída (`onDestroy`), THE CollectionViewActivity SHALL deletar todos os arquivos com prefixo `relatorio_sem_etiqueta_` no diretório `context.cacheDir`.
2. WHEN `BaixarRelatorioFotoPdfUseCase` é chamado e já existe um arquivo com o mesmo nome no cache, THE BaixarRelatorioFotoPdfUseCase SHALL sobrescrever o arquivo existente.
