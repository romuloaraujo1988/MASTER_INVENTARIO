# Scripts SQL - Sistema de Inventário IFMT

Este diretório contém todos os scripts SQL necessários para criar as tabelas do Sistema de Inventário do IFMT.

## Arquivos Criados

### Scripts Principais

1. **`executar_criacao_tabelas.sql`** - Script principal que executa todos os outros na ordem correta
2. **`criar_tabelas_sistema_inventario.sql`** - Script completo com todas as tabelas em um único arquivo

### Scripts Individuais por Tabela

3. **`script_tabela_setor.sql`** - Criação da tabela SETOR
4. **`script_tabela_responsavel.sql`** - Criação da tabela RESPONSAVEL
5. **`script_tabela_patrimonio.sql`** - Criação da tabela PATRIMONIO
6. **`script_tabela_inventario.sql`** - Criação da tabela INVENTARIO
7. **`script_tabela_coleta.sql`** - Criação da tabela COLETA

## Estrutura das Tabelas

### TABELA_SETOR
- **Função**: Armazena os setores da instituição
- **Campos principais**: ID, NOME, DESCRICAO, RESPONSAVEL_SETOR
- **Dados iniciais**: Administração, TI, Ensino, Biblioteca, Laboratórios

### TABELA_RESPONSAVEL
- **Função**: Cadastro de responsáveis pelos patrimônios
- **Campos principais**: ID, NOME, CPF, EMAIL, CARGO, ID_SETOR
- **Relacionamento**: Vinculado ao SETOR

### TABELA_SALA
- **Função**: Locais onde ficam os patrimônios
- **Campos principais**: ID_SALA, DESCRICAO, NUMERO_SALA, ANDAR, BLOCO
- **Relacionamento**: Vinculado ao SETOR

### TABELA_INVENTARIO
- **Função**: Controle dos inventários realizados
- **Campos principais**: ID, NOME, ANO, DATA_INICIO, DATA_FIM, STATUS_INVENTARIO
- **Recursos**: Controle de progresso e timestamps automáticos

### TABELA_COLETOR
- **Função**: Cadastro de pessoas que realizam as coletas
- **Campos principais**: ID, NOME_COLETOR, CPF, EMAIL

### TABELA_PATRIMONIO
- **Função**: Tabela principal com todos os patrimônios
- **Campos principais**: ID, NUMERO, DESCRICAO, VALOR_AQUISICAO, STATUS
- **Relacionamentos**: RESPONSAVEL e SALA
- **Recursos**: Controle de estado, valores e localização

### TABELA_COLETA
- **Função**: Registro das coletas realizadas durante inventários
- **Campos principais**: ID_INVENTARIO, ID_PATRIMONIO, ID_COLETOR, DATA_COLETA
- **Recursos**: Controle de divergências, coordenadas GPS, fotos

## Como Executar

### Opção 1: Script Completo (Recomendado)
```sql
-- Execute no PostgreSQL
\i executar_criacao_tabelas.sql
```

### Opção 2: Script Único
```sql
-- Execute no PostgreSQL
\i criar_tabelas_sistema_inventario.sql
```

### Opção 3: Scripts Individuais (Para manutenção)
```sql
-- Execute na ordem:
\i script_tabela_setor.sql
\i script_tabela_responsavel.sql
-- (continuar com os demais)
```

## Ordem de Dependências

1. **SETOR** (sem dependências)
2. **RESPONSAVEL** (depende de SETOR)
3. **SALA** (depende de SETOR)
4. **INVENTARIO** (sem dependências)
5. **COLETOR** (sem dependências)
6. **PATRIMONIO** (depende de RESPONSAVEL e SALA)
7. **COLETA** (depende de INVENTARIO, PATRIMONIO e COLETOR)

## Views Criadas

### vw_patrimonios_completo
View que une informações de patrimônio com responsável, sala e setor.

### vw_coletas_completo
View que une informações de coleta com inventário, patrimônio e coletor.

## Recursos Implementados

- **Chaves estrangeiras** para integridade referencial
- **Índices** para melhor performance
- **Comentários** em todas as tabelas e campos importantes
- **Dados iniciais** para começar a usar o sistema
- **Triggers** para atualização automática de timestamps
- **Constraints** para validação de dados
- **Views** para facilitar consultas complexas

## Configuração do Banco

Antes de executar os scripts, certifique-se de que:

1. PostgreSQL está instalado e rodando
2. Banco de dados foi criado
3. Usuário tem permissões adequadas
4. Conexão está configurada corretamente

## Próximos Passos

Após executar os scripts:

1. Verificar se todas as tabelas foram criadas
2. Inserir dados adicionais conforme necessário
3. Configurar o sistema de inventário para usar as tabelas
4. Testar as funcionalidades básicas

## Suporte

Para dúvidas ou problemas:
- Verificar logs de erro do PostgreSQL
- Confirmar ordem de execução dos scripts
- Validar permissões do usuário do banco