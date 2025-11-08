# Criador de Tabelas do Sistema de Inventário

## Descrição
Este executável (`CriadorTabelasBD.exe`) foi desenvolvido para automatizar a criação das tabelas do banco de dados do Sistema de Inventário do IFMT.

## Características
- ✅ **Executável standalone**: Não requer instalação adicional
- ✅ **Compatível com SQL Server**: Funciona com qualquer versão do SQL Server
- ✅ **Interface interativa**: Solicita configurações do usuário
- ✅ **Tratamento de erros**: Valida conexões e operações
- ✅ **Transações seguras**: Rollback automático em caso de erro

## Pré-requisitos
- Windows com .NET Framework 4.0 ou superior (já instalado na maioria dos sistemas)
- SQL Server instalado e configurado
- Permissões para criar tabelas no banco de dados

## Como usar

### 1. Executar o programa
```bash
CriadorTabelasBD.exe
```

### 2. Configurar conexão
O programa solicitará:
- **Servidor**: Nome ou IP do servidor SQL Server (ex: `localhost`, `.\SQLEXPRESS`)
- **Nome do banco**: Nome do banco de dados (ex: `sispatrimonio`)
- **Usuário**: Usuário do SQL Server (deixe vazio para Windows Authentication)
- **Senha**: Senha do usuário (se aplicável)

### 3. Criação das tabelas
O programa criará automaticamente:

#### Tabelas Base:
- `TABELA_SETOR` - Setores da instituição
- `TABELA_RESPONSAVEL` - Responsáveis pelos setores
- `TABELA_SALA` - Salas/locais
- `TABELA_INVENTARIO` - Inventários realizados
- `TABELA_COLETOR` - Usuários coletores

#### Tabelas Principais:
- `TABELA_PATRIMONIO` - Patrimônios cadastrados
- `TABELA_COLETA` - Coletas realizadas

#### Views:
- `vw_patrimonios_completo` - Visão completa dos patrimônios
- `vw_coletas_completo` - Visão completa das coletas

### 4. Dados iniciais
O programa também insere dados iniciais:
- Setores padrão (Administração, TI, Almoxarifado)
- Responsáveis padrão (Administrador, Coordenador TI)
- Usuário coletor padrão (admin/admin123)
- Inventário do ano atual

## Estrutura criada

### Relacionamentos:
```
SETOR → RESPONSAVEL → SALA
       ↓
    INVENTARIO → COLETA ← PATRIMONIO
                  ↑
               COLETOR
```

### Índices criados:
- Índices de performance em chaves estrangeiras
- Índices únicos em campos críticos (CPF, email)
- Índices compostos para consultas otimizadas

## Arquivos relacionados

### Scripts SQL originais:
- `executar_criacao_tabelas.sql` - Script principal
- `criar_tabelas_sispatrimonio.sql` - Script completo
- `script_tabela_*.sql` - Scripts individuais por tabela
- `validar_scripts_bd.sql` - Script de validação

### Código fonte:
- `CriadorTabelasBD_Simples.cs` - Código fonte C#
- `compilar-exe-simples.bat` - Script de compilação

## Solução de problemas

### Erro de conexão:
- Verifique se o SQL Server está rodando
- Confirme o nome do servidor
- Teste as credenciais
- Verifique se o banco de dados existe

### Erro de permissão:
- Execute como administrador
- Verifique permissões do usuário no SQL Server
- Confirme se o usuário pode criar tabelas

### Tabelas já existem:
- O programa verifica se as tabelas já existem
- Não sobrescreve dados existentes
- Use scripts de validação para verificar integridade

## Suporte
Para problemas ou dúvidas, consulte:
- Scripts SQL originais na pasta do projeto
- Logs de erro exibidos pelo programa
- Documentação do SQL Server

---
**Desenvolvido para o IFMT - Instituto Federal de Mato Grosso**  
**Sistema de Inventário de Patrimônio - Versão 1.0**