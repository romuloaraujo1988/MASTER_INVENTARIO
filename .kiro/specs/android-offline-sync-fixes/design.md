# Android Offline Sync Fixes - Bugfix Design

## Overview

Este documento formaliza o design das correções para 5 bugs no fluxo de sincronizacao offline do app Android do SIHCP. Os bugs afetam a confiabilidade da sincronizacao de coletas com o servidor Spring Boot, causando coletas presas indefinidamente, perda silenciosa de dados no servidor, e envio de coletas para inventarios incorretos.

A estrategia de correcao e cirurgica: alterar apenas os metodos afetados em `ColetaRepositoryImpl.kt` e `EnviarColetasPendentesUseCase.kt`, sem modificar `ColetaEntity`, sem alterar URLs de endpoints, e sem quebrar o fluxo de coleta existente.

## Glossary

- **Bug_Condition (C)**: A condicao que identifica entradas que disparam o bug
- **Property (P)**: O comportamento correto esperado quando C(X) e verdadeiro
- **Preservation**: Comportamentos existentes que nao devem ser alterados pela correcao
- **isBugCondition**: Funcao pseudocodigo que identifica se uma entrada e bugada
- **ColetaRepositoryImpl**: Implementacao do repositorio em `InventarioMobile/app/src/main/java/com/inventario/mobile/data/repository/ColetaRepositoryImpl.kt` que gerencia persistencia e sincronizacao de coletas
- **EnviarColetasPendentesUseCase**: Use case em `InventarioMobile/app/src/main/java/com/inventario/mobile/domain/usecase/EnviarColetasPendentesUseCase.kt` que deveria enviar coletas pendentes ao servidor mas atualmente apenas as marca como sincronizadas sem envio real
- **ColetaDao**: Interface Room com metodos de acesso ao banco local de coletas
- **ColetaApi**: Interface Retrofit com endpoints `POST api/mobile/coletas` e `POST api/mobile/coletas/batch`
- **idInventario**: Campo `Int` na `ColetaEntity` que armazena o ID do inventario no momento da coleta; valor 0 indica inventario nao configurado
- **tentativasSincronizacao**: Contador de tentativas de sincronizacao na `ColetaEntity`; incrementado por `registrarErroSincronizacao()`
- **erroSincronizacao**: Campo `String?` na `ColetaEntity`; quando preenchido, indica que a coleta esta presa com erro permanente
- **sincronizarEmLote**: Metodo privado de `ColetaRepositoryImpl` que envia multiplas coletas via `coletaApi.registrarColetasEmLote()`
- **sincronizarIndividualmente**: Metodo privado de `ColetaRepositoryImpl` que envia coletas uma a uma via `coletaApi.registrarColeta()`
- **sincronizarColetasPendentes**: Metodo publico de `ColetaRepositoryImpl` que orquestra o ciclo de sincronizacao

## Bug Details

### Bug 1 - Coletas presas quando idInventario = 0

O bug se manifesta quando o usuario realiza uma coleta logo apos o login, antes de o inventario ativo ser carregado do servidor. Nesse momento, `preferencesManager.getInventarioAtivoId()` retorna `null` ou 0, e o bloco de sincronizacao em background de `registrarColeta()` registra imediatamente um erro permanente via `coletaDao.registrarErroSincronizacao(id, erro)`, deixando a coleta presa com `erroSincronizacao` preenchido e sem possibilidade de retry automatico.
