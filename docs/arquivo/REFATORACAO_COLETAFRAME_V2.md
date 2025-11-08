# Refatoração da ColetaFrame_v2 - Documentação

## ⚠️ ATENÇÃO: View Crítica do Sistema

Esta é a view **mais importante** do sistema - responsável pela coleta de patrimônios.

## 📊 Análise Inicial

### DAOs Utilizados (6)
1. **SalaDAORefactored** - Buscar salas
2. **PatrimonioDAORefactored** - Buscar patrimônios
3. **ColetaDAO** - Registrar coletas
4. **InventarioDAO** - Buscar inventário ativo
5. **SalaInventarioDAO** - Gerenciar coleta por sala
6. **ParticipanteInventarioDAO** - Buscar participantes

### Métodos Críticos Identificados

#### ColetaDAO (15+ usos)
- `buscarColetasSemEtiquetaPorSala()`
- `inserirColeta()`
- `excluirColeta()`
- `buscarColetasPorSala()`
- `buscarPorPatrimonio()`
- `coletaExiste()`
- `buscarColetasComEtiquetaPorLocalizacaoEncontrada()`
- `agruparItensSemEtiquetaPorDescricao()`

#### InventarioDAO (10+ usos)
- `buscarInventarioPorStatus("EM_ANDAMENTO")` - usado em MUITOS lugares

#### SalaInventarioDAO (8+ usos)
- `isColetaFinalizada()`
- `iniciarColeta()`
- `atualizarEstatisticas()`
- `finalizarColeta()`

#### PatrimonioDAO (5+ usos)
- `buscarPorNumero()`
- `buscarPorDescricaoAbrangente()`

#### SalaDAO (2+ usos)
- `findAll()`

#### ParticipanteInventarioDAO (5+ usos)
- `buscarIdParticipantePorUsuario()`

## 🎯 Estratégia de Refatoração

### Etapa 1: Verificar Serviços Existentes
Antes de refatorar, verificar se todos os métodos necessários existem nos serviços.

### Etapa 2: Adicionar Métodos Faltantes
Expandir serviços com métodos que faltam.

### Etapa 3: Refatorar Incrementalmente
Substituir DAOs por serviços, um de cada vez.

### Etapa 4: Testar Cada Mudança
Verificar que nada quebrou após cada substituição.

## 📋 Checklist de Segurança

- [x] ✅ Backup criado (ColetaFrame_v2.java.backup)
- [ ] ⏳ Verificar métodos nos serviços
- [ ] ⏳ Adicionar métodos faltantes
- [ ] ⏳ Refatorar imports
- [ ] ⏳ Refatorar inicialização
- [ ] ⏳ Refatorar método por método
- [ ] ⏳ Testar funcionalidade completa
- [ ] ⏳ Validar com usuário

## 🚨 Pontos de Atenção

1. **Inventário Ativo** - Usado em MUITOS lugares
2. **Coleta de Itens Sem Etiqueta** - Lógica complexa
3. **Estatísticas de Sala** - Atualização após cada coleta
4. **Participantes** - Lógica de permissão
5. **Som de Notificação** - Não quebrar feedback do usuário

---

**Status**: 🔄 EM PROGRESSO  
**Backup**: ✅ Criado  
**Risco**: 🔴 ALTO (view crítica)
