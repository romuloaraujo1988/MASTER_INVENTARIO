# Requirements Document

## Introduction

Este documento especifica os requisitos para o tratamento de coletas duplicadas no servidor mobile. Atualmente, quando o aplicativo Android tenta sincronizar uma coleta que já foi sincronizada anteriormente, o servidor lança uma exceção que derruba a conexão do smartphone. O objetivo é tratar essa situação de forma graceful, retornando um status de "duplicada" para que o usuário saiba que deve excluir a coleta local, pois o servidor não irá aceitá-la novamente.

## Glossary

- **Coleta**: Registro de um patrimônio inventariado, contendo informações como número do patrimônio, data, localização encontrada e estado de conservação.
- **Coleta Duplicada**: Uma coleta que já existe no banco de dados do servidor para o mesmo patrimônio no mesmo inventário.
- **Sincronização**: Processo de envio de coletas do aplicativo Android para o servidor.
- **MobileColetaService**: Serviço Java responsável por processar as requisições de coleta do aplicativo mobile.
- **MobileColetaResponse**: DTO de resposta que contém os dados da coleta processada.
- **Status de Sincronização**: Campo que indica se a coleta foi aceita, rejeitada ou é duplicada.

## Requirements

### Requirement 1

**User Story:** As a mobile app user, I want the server to gracefully handle duplicate collection attempts, so that my app doesn't crash and I know which local collections to delete.

#### Acceptance Criteria

1. WHEN the server receives a collection request for a patrimony that was already collected in the same inventory THEN the MobileColetaService SHALL return a response with status "DUPLICADA" instead of throwing an exception
2. WHEN a duplicate collection is detected THEN the MobileColetaService SHALL include the existing collection ID in the response for reference
3. WHEN a duplicate collection is detected THEN the MobileColetaService SHALL return HTTP status 200 (OK) with a success flag indicating the duplicate status
4. WHEN processing a batch of collections containing duplicates THEN the MobileColetaService SHALL continue processing remaining collections and report duplicates in the result summary

### Requirement 2

**User Story:** As a mobile app user, I want to receive clear information about duplicate collections, so that I can take appropriate action to clean up my local data.

#### Acceptance Criteria

1. WHEN a duplicate collection response is returned THEN the MobileColetaResponse SHALL include a "duplicada" boolean field set to true
2. WHEN a duplicate collection response is returned THEN the MobileColetaResponse SHALL include a "mensagemDuplicada" field explaining that the collection already exists
3. WHEN a duplicate collection response is returned THEN the MobileColetaResponse SHALL include the "dataColetaOriginal" field with the timestamp of the original collection
4. WHEN a duplicate collection response is returned THEN the MobileColetaResponse SHALL include the "coletorOriginal" field with the name of the user who made the original collection

### Requirement 3

**User Story:** As a system administrator, I want duplicate collection attempts to be logged, so that I can monitor synchronization issues and identify problematic devices.

#### Acceptance Criteria

1. WHEN a duplicate collection is detected THEN the MobileColetaService SHALL log a warning message with the patrimony number, inventory ID, and requesting user
2. WHEN multiple duplicate attempts occur from the same user in a short period THEN the MobileColetaService SHALL log an informational message suggesting the user clear local cache
