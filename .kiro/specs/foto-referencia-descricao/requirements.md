# Requirements Document

## Introduction

Este documento especifica os requisitos para a funcionalidade de **Foto de Referência por Descrição**, que permite cadastrar imagens representativas vinculadas a descrições de patrimônios (não a itens individuais). Essas fotos servirão como referência visual no app mobile para ajudar os coletores a identificar rapidamente os tipos de itens durante o inventário.

A foto de referência é uma imagem pequena e otimizada que representa uma categoria/tipo de patrimônio, facilitando a identificação visual sem a necessidade de consultar cada item individualmente.

## Glossary

- **Sistema_Desktop**: Aplicação Java Swing para gerenciamento de inventário patrimonial
- **App_Mobile**: Aplicação Android para coleta de patrimônios em campo
- **Foto_Referencia**: Imagem pequena (thumbnail) que representa visualmente um tipo de patrimônio
- **Descricao_Patrimonio**: Texto que descreve um tipo de patrimônio (ex: "CADEIRA GIRATÓRIA", "MONITOR LED 24 POLEGADAS")
- **Descricao_Normalizada**: Versão simplificada da descrição para agrupamento (sem números de patrimônio, marcas específicas, etc.)
- **Thumbnail**: Imagem redimensionada para exibição rápida (máximo 200x200 pixels)
- **API_Mobile**: Backend Spring Boot que serve dados para o app mobile

## Requirements

### Requirement 1: Cadastro de Foto de Referência no Desktop

**User Story:** Como administrador do sistema, quero cadastrar fotos de referência para descrições de patrimônios, para que os coletores possam identificar visualmente os itens no app mobile.

#### Acceptance Criteria

1. WHEN o usuário acessa o menu de cadastro de fotos de referência THEN THE Sistema_Desktop SHALL exibir uma tela com lista de descrições únicas de patrimônios
2. WHEN o usuário seleciona uma descrição THEN THE Sistema_Desktop SHALL permitir upload de uma imagem (JPG, PNG) de até 2MB
3. WHEN uma imagem é carregada THEN THE Sistema_Desktop SHALL redimensionar automaticamente para thumbnail (máximo 200x200 pixels)
4. WHEN o usuário confirma o cadastro THEN THE Sistema_Desktop SHALL salvar a Foto_Referencia vinculada à descrição
5. WHEN uma descrição já possui foto THEN THE Sistema_Desktop SHALL exibir a foto atual e permitir substituição
6. IF o arquivo não for uma imagem válida THEN THE Sistema_Desktop SHALL exibir mensagem de erro e rejeitar o upload
7. WHEN o usuário busca descrições THEN THE Sistema_Desktop SHALL filtrar a lista por texto digitado

### Requirement 2: Armazenamento de Fotos de Referência

**User Story:** Como sistema, quero armazenar as fotos de referência de forma eficiente, para que possam ser sincronizadas rapidamente com o app mobile.

#### Acceptance Criteria

1. THE Sistema_Desktop SHALL armazenar fotos como BLOB no banco de dados PostgreSQL
2. THE Sistema_Desktop SHALL armazenar metadados: descrição vinculada, data de cadastro, tamanho original, tamanho comprimido
3. WHEN uma foto é salva THEN THE Sistema_Desktop SHALL comprimir a imagem para JPEG com qualidade 80%
4. THE Sistema_Desktop SHALL garantir que o tamanho final do thumbnail não exceda 50KB
5. WHEN uma foto é atualizada THEN THE Sistema_Desktop SHALL manter histórico da versão anterior (soft delete)

### Requirement 3: Normalização de Descrições

**User Story:** Como administrador, quero que descrições similares sejam agrupadas, para que uma única foto sirva para múltiplos patrimônios do mesmo tipo.

#### Acceptance Criteria

1. WHEN exibindo descrições para cadastro THEN THE Sistema_Desktop SHALL agrupar descrições similares
2. THE Sistema_Desktop SHALL normalizar descrições removendo: números de patrimônio ANAC, números de série, marcas específicas quando seguidas de modelo
3. WHEN uma foto é vinculada a uma descrição normalizada THEN THE Sistema_Desktop SHALL aplicar a todos os patrimônios com descrição similar
4. WHEN o usuário visualiza uma descrição THEN THE Sistema_Desktop SHALL mostrar quantos patrimônios serão beneficiados pela foto

### Requirement 4: API para Sincronização de Fotos

**User Story:** Como app mobile, quero receber as fotos de referência do servidor, para exibir aos coletores durante a coleta.

#### Acceptance Criteria

1. WHEN o app solicita fotos de referência THEN THE API_Mobile SHALL retornar lista de fotos com descrição e imagem em Base64
2. WHEN o app solicita fotos THEN THE API_Mobile SHALL suportar paginação (máximo 50 fotos por requisição)
3. WHEN o app solicita fotos THEN THE API_Mobile SHALL retornar apenas fotos atualizadas desde última sincronização (delta sync)
4. THE API_Mobile SHALL retornar metadados: id, descrição, hash da imagem, data de atualização
5. IF não houver fotos novas THEN THE API_Mobile SHALL retornar lista vazia com status 200

### Requirement 5: Exibição de Foto no App Mobile

**User Story:** Como coletor, quero ver a foto de referência do patrimônio durante a coleta, para identificar visualmente o item correto.

#### Acceptance Criteria

1. WHEN o coletor visualiza detalhes de um patrimônio THEN THE App_Mobile SHALL exibir a foto de referência correspondente à descrição
2. WHEN não houver foto de referência THEN THE App_Mobile SHALL exibir ícone placeholder padrão
3. WHEN o coletor está na lista de patrimônios THEN THE App_Mobile SHALL exibir thumbnail da foto ao lado de cada item
4. THE App_Mobile SHALL cachear fotos localmente para uso offline
5. WHEN a foto é tocada THEN THE App_Mobile SHALL ampliar a imagem em modal para melhor visualização

### Requirement 6: Sincronização Offline de Fotos

**User Story:** Como coletor, quero que as fotos estejam disponíveis offline, para identificar patrimônios mesmo sem conexão.

#### Acceptance Criteria

1. WHEN o app sincroniza dados THEN THE App_Mobile SHALL baixar todas as fotos de referência novas ou atualizadas
2. THE App_Mobile SHALL armazenar fotos no banco SQLite local (Room)
3. WHEN offline THEN THE App_Mobile SHALL exibir fotos do cache local
4. WHEN o armazenamento local exceder 100MB de fotos THEN THE App_Mobile SHALL remover fotos mais antigas não utilizadas
5. THE App_Mobile SHALL priorizar download de fotos para patrimônios do inventário ativo

### Requirement 7: Gerenciamento de Fotos no Desktop

**User Story:** Como administrador, quero gerenciar as fotos cadastradas, para manter o catálogo atualizado e organizado.

#### Acceptance Criteria

1. WHEN o usuário acessa gerenciamento de fotos THEN THE Sistema_Desktop SHALL listar todas as fotos cadastradas com preview
2. WHEN o usuário seleciona uma foto THEN THE Sistema_Desktop SHALL permitir exclusão com confirmação
3. WHEN uma foto é excluída THEN THE Sistema_Desktop SHALL marcar como inativa (soft delete) e notificar app na próxima sincronização
4. THE Sistema_Desktop SHALL exibir estatísticas: total de fotos, descrições sem foto, uso de armazenamento
