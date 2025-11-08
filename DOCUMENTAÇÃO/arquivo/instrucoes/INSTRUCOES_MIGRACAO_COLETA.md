# Instruções para Migração da Tabela de Coleta

## Problema Identificado
A `TABELA_COLETA` possui uma chave estrangeira `ID_COLETOR` que referencia a `TABELA_COLETOR`, mas o conceito de "coletor" foi substituído por "usuário" e "participante do inventário" no sistema.

## Solução Implementada
Foi escolhida a abordagem de adicionar um novo campo `ID_PARTICIPANTE_INVENTARIO` que referencia a `TABELA_PARTICIPANTE_INVENTARIO`, mantendo compatibilidade durante a transição.

## Arquivos Modificados

### 1. Coleta.java
- ✅ Adicionado campo `idParticipanteInventario`
- ✅ Adicionados métodos getter e setter
- ✅ Adicionado novo construtor que aceita `idParticipanteInventario`
- ✅ Mantido campo `idColetor` para compatibilidade

### 2. ColetaDAO.java
- ✅ Atualizados métodos `inserirColeta` e `atualizarColeta` para suportar `ID_PARTICIPANTE_INVENTARIO`
- ✅ Atualizado método `criarColetaFromResultSet` para ler ambos os campos
- ✅ Atualizadas todas as queries SQL para incluir JOIN com `TABELA_PARTICIPANTE_INVENTARIO`
- ✅ Corrigidas referências de `u.NOME` para `u.NOME_COMPLETO`
- ✅ Corrigidas referências de `p.NUMERO` para `p.NUMERO_PATRIMONIO`

## Scripts SQL Criados

### 1. migrar_coleta_para_participante_inventario.sql
- Script completo para migração de dados existentes
- Cria usuários e participantes se necessário
- Migra dados da `TABELA_COLETOR` para o novo modelo

### 2. atualizar_tabela_coleta.sql
- Adiciona coluna `ID_PARTICIPANTE_INVENTARIO`
- Cria índices para performance
- Prepara para adição de chave estrangeira

## Passos para Implementação

### Fase 1: Preparação do Banco de Dados
1. Execute o script `atualizar_tabela_coleta.sql`
2. Execute o script `migrar_coleta_para_participante_inventario.sql`
3. Verifique se a migração foi bem-sucedida

### Fase 2: Teste da Aplicação
1. Compile e teste a aplicação
2. Verifique se as coletas existentes são exibidas corretamente
3. Teste a criação de novas coletas
4. Verifique se os relatórios e dashboards funcionam

### Fase 3: Limpeza (Opcional)
Após confirmar que tudo funciona:
1. Remover campo `idColetor` da classe `Coleta.java`
2. Remover referências a `ID_USUARIO`/`ID_COLETOR` nas queries
3. Remover coluna `ID_COLETOR` da `TABELA_COLETA`
4. Remover `TABELA_COLETOR` se não for mais utilizada

## Vantagens da Solução

1. **Compatibilidade**: Mantém o sistema funcionando durante a transição
2. **Flexibilidade**: Permite migração gradual
3. **Integridade**: Usa a estrutura correta de participantes do inventário
4. **Performance**: Adiciona índices apropriados
5. **Rastreabilidade**: Mantém histórico de quem fez cada coleta

## Considerações Importantes

1. **Backup**: Sempre faça backup antes de executar os scripts
2. **Teste**: Teste em ambiente de desenvolvimento primeiro
3. **Monitoramento**: Monitore a performance após a migração
4. **Documentação**: Atualize a documentação do sistema

## Estrutura Final

Após a migração completa:
- `TABELA_COLETA.ID_PARTICIPANTE_INVENTARIO` → `TABELA_PARTICIPANTE_INVENTARIO.ID`
- `TABELA_PARTICIPANTE_INVENTARIO.ID_USUARIO` → `TABELA_USUARIO.ID`
- `TABELA_PARTICIPANTE_INVENTARIO.ID_INVENTARIO` → `TABELA_INVENTARIO.ID`

Isso garante que cada coleta está associada a um usuário específico participando de um inventário específico, com papel definido (COORDENADOR, COLETOR, OBSERVADOR).