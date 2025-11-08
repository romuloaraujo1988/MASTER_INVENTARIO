# Criador de Tabelas - Sistema de Inventário (PostgreSQL)

## 🔧 PROBLEMA RESOLVIDO
**Versão corrigida que executa scripts SQL reais no PostgreSQL**

### ❌ Problema Anterior
- O executável anterior apenas simulava a criação das tabelas
- Não executava os scripts SQL realmente no banco de dados
- As tabelas não eram criadas de fato

### ✅ Solução Implementada
- **Execução real de scripts SQL** usando o comando `psql`
- **Verificação automática** da disponibilidade do PostgreSQL
- **Conexão real** com o banco de dados PostgreSQL
- **Criação efetiva** de todas as tabelas, índices e views

## 📋 Características da Versão Corrigida

- ✅ **Executável standalone** (não requer .NET SDK)
- ✅ **Compatível com PostgreSQL** (versão 9.6+)
- ✅ **Execução real de scripts SQL** via psql
- ✅ **Interface interativa** para configuração
- ✅ **Transações seguras** com rollback automático
- ✅ **Verificação de pré-requisitos** automática
- ✅ **Criação completa do sistema** de inventário

## 📁 Arquivos da Versão Corrigida

```
CriadorTabelasBD_PostgreSQL.exe     # Executável corrigido (26.624 bytes)
CriadorTabelasBD_PostgreSQL.cs      # Código fonte corrigido
compilar-exe-postgresql.bat         # Script de compilação
README_POSTGRESQL.md                # Esta documentação
```

## 🔧 Pré-requisitos

### 1. PostgreSQL Instalado
O executável requer que o PostgreSQL esteja instalado e o comando `psql` disponível no PATH.

**Para instalar o PostgreSQL:**

#### Windows:
1. Baixe o PostgreSQL em: https://www.postgresql.org/download/windows/
2. Execute o instalador
3. Durante a instalação, marque a opção "Command Line Tools"
4. Adicione o PostgreSQL ao PATH do sistema

#### Verificar instalação:
```bash
psql --version
```

### 2. Banco de Dados PostgreSQL
- Servidor PostgreSQL rodando
- Banco de dados criado
- Usuário com permissões de criação de tabelas

## 🚀 Como Usar

### 1. Executar o Programa
```bash
CriadorTabelasBD_PostgreSQL.exe
```

### 2. Configurar Conexão
O programa solicitará:
- **Servidor**: localhost (padrão) ou IP do servidor
- **Porta**: 5432 (padrão) ou porta customizada
- **Nome do banco**: nome do banco de dados existente
- **Usuário**: usuário PostgreSQL
- **Senha**: senha do usuário

### 3. Verificação Automática
- ✅ Verifica se `psql` está disponível
- ✅ Testa conexão com o banco
- ✅ Executa scripts SQL reais
- ✅ Confirma criação das tabelas

## 🗄️ Estrutura Criada

### Tabelas (7):
1. **TABELA_SETOR** - Setores da instituição
2. **TABELA_RESPONSAVEL** - Responsáveis pelos setores
3. **TABELA_SALA** - Salas/locais dos patrimônios
4. **TABELA_INVENTARIO** - Inventários realizados
5. **TABELA_COLETOR** - Usuários do sistema
6. **TABELA_PATRIMONIO** - Patrimônios cadastrados
7. **TABELA_COLETA** - Coletas durante inventário

### Views (2):
- **vw_patrimonios_completo** - Patrimônios com informações completas
- **vw_coletas_completo** - Coletas com informações detalhadas

### Recursos:
- **Relacionamentos** entre tabelas (Foreign Keys)
- **Índices** para performance
- **Dados iniciais** para teste
- **Comentários** nas tabelas

## 🔍 Dados Iniciais Inseridos

### Setores:
- Administração
- Tecnologia da Informação
- Almoxarifado
- Biblioteca
- Laboratórios

### Responsáveis:
- Administrador Geral
- Coordenador TI

### Salas:
- Sala de Administração (ADM-01)
- Sala de TI (TI-01)
- Biblioteca Principal (BIB-01)
- Laboratório de Informática 1 (LAB-01)
- Almoxarifado Central (ALM-01)

### Usuário Padrão:
- **Login**: admin
- **Senha**: admin123
- **Perfil**: ADMIN

## ⚠️ Pontos Importantes

### Diferenças da Versão Anterior:
- **EXECUÇÃO REAL**: Agora executa scripts SQL de verdade
- **VERIFICAÇÃO**: Confirma se PostgreSQL está instalado
- **CONEXÃO REAL**: Testa conexão antes de executar
- **FEEDBACK**: Mostra erros reais se houver problemas

### Segurança:
- Senha não é salva no arquivo de configuração
- Conexão via variável de ambiente PGPASSWORD
- Arquivos temporários são limpos automaticamente

## 🛠️ Solução de Problemas

### Erro: "PostgreSQL (psql) não encontrado!"
**Solução**: Instale o PostgreSQL e adicione ao PATH

### Erro: "Não foi possível conectar ao banco"
**Soluções**:
- Verifique se o PostgreSQL está rodando
- Confirme servidor, porta, banco e credenciais
- Teste conexão manual: `psql -h servidor -p porta -d banco -U usuario`

### Erro: "Falha na criação das tabelas"
**Soluções**:
- Verifique permissões do usuário
- Confirme se o banco existe
- Verifique logs de erro do PostgreSQL

## 🔨 Compilação (Opcional)

Para recompilar o código:
```bash
compilar-exe-postgresql.bat
```

**Requisitos**:
- .NET Framework 4.0+ instalado
- Compilador C# (csc.exe) disponível

## 📊 Diferenças SQL Server vs PostgreSQL

| Recurso | SQL Server | PostgreSQL |
|---------|------------|------------|
| Auto-incremento | `IDENTITY(1,1)` | `SERIAL` |
| Booleano | `BIT` | `BOOLEAN` |
| Texto longo | `NVARCHAR(MAX)` | `TEXT` |
| Data/hora | `DATETIME` | `TIMESTAMP` |
| Inserção segura | `MERGE` | `ON CONFLICT DO NOTHING` |
| String de conexão | SqlConnection | psql command line |

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique se o PostgreSQL está instalado corretamente
2. Teste a conexão manualmente com `psql`
3. Confirme permissões do usuário no banco
4. Verifique logs de erro do PostgreSQL

---

**Versão**: 1.0 (Corrigida)  
**Compatibilidade**: PostgreSQL 9.6+  
**Sistema**: Windows com .NET Framework 4.0+  
**Última atualização**: Janeiro 2025