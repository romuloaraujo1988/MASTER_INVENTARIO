# Instruções para Implementar Tabela de Usuários no PostgreSQL

## ⚠️ IMPORTANTE
A tabela de usuários (`TABELA_USUARIO`) ainda não foi criada no banco de dados PostgreSQL. Este documento fornece as instruções para implementá-la.

## Pré-requisitos
- PostgreSQL instalado e funcionando
- Banco de dados `sispatrimonio` criado
- Acesso ao pgAdmin ou cliente PostgreSQL
- Permissões de escrita no banco

## Opções para Implementação

### Opção 1: Executar Script Específico (RECOMENDADO)

1. **Abra o pgAdmin**
   - Conecte-se ao servidor PostgreSQL
   - Selecione o banco `sispatrimonio`
   - Abra o Query Tool (Tools > Query Tool)

2. **Execute o script da tabela de usuários**
   - Abra o arquivo `criar_tabela_usuario.sql`
   - Copie todo o conteúdo
   - Cole no Query Tool
   - Execute (F5 ou botão Execute)

### Opção 2: Executar Script Completo

1. **Execute o script completo do sistema**
   - Abra o arquivo `criar_tabelas_sispatrimonio.sql`
   - Copie todo o conteúdo
   - Cole no Query Tool do pgAdmin
   - Execute (F5 ou botão Execute)

### Opção 3: Via Linha de Comando (se psql estiver disponível)

```bash
# Navegar até o diretório do projeto
cd "g:\Meu Drive\PESSOAL\AULAS\PROFNIT\MASTER_INVENTÁRIO"

# Executar script específico
psql -h localhost -U postgres -d sispatrimonio -f criar_tabela_usuario.sql

# OU executar script completo
psql -h localhost -U postgres -d sispatrimonio -f criar_tabelas_sispatrimonio.sql
```

## Verificação da Implementação

Após executar o script, verifique se a tabela foi criada:

```sql
-- Verificar se a tabela existe
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name = 'tabela_usuario';

-- Verificar estrutura da tabela
\d tabela_usuario

-- Verificar usuário administrador
SELECT LOGIN, NOME_COMPLETO, EMAIL, PERFIL, ATIVO 
FROM TABELA_USUARIO 
WHERE LOGIN = 'admin';
```

## Usuário Administrador Padrão

Após a criação da tabela, será inserido automaticamente:

- **Login:** `admin`
- **Senha:** `admin123`
- **Nome:** `Administrador do Sistema`
- **Email:** `admin@sistema.com`
- **Perfil:** `ADMIN`
- **Status:** `Ativo`

## Estrutura da Tabela TABELA_USUARIO

| Campo | Tipo | Descrição |
|-------|------|----------|
| ID | SERIAL | Chave primária |
| LOGIN | VARCHAR(50) | Login único do usuário |
| SENHA_HASH | VARCHAR(255) | Hash da senha (bcrypt) |
| NOME_COMPLETO | VARCHAR(150) | Nome completo do usuário |
| EMAIL | VARCHAR(100) | Email único do usuário |
| CPF | VARCHAR(14) | CPF do usuário (opcional) |
| PERFIL | VARCHAR(20) | Perfil: ADMIN, SUPERVISOR, COLETOR, CONSULTA |
| ID_SETOR | INTEGER | Referência ao setor (opcional) |
| ATIVO | CHAR(1) | Status: S=Sim, N=Não |
| DATA_CRIACAO | TIMESTAMP | Data de criação |
| DATA_ULTIMO_ACESSO | TIMESTAMP | Último acesso |
| TENTATIVAS_LOGIN | INTEGER | Contador de tentativas |
| BLOQUEADO | CHAR(1) | Bloqueado: S=Sim, N=Não |
| DATA_BLOQUEIO | TIMESTAMP | Data do bloqueio |
| DATA_EXPIRACAO_SENHA | TIMESTAMP | Expiração da senha |
| PRIMEIRO_ACESSO | CHAR(1) | Primeiro acesso: S=Sim, N=Não |
| OBSERVACOES | TEXT | Observações |
| CREATED_AT | TIMESTAMP | Timestamp de criação |
| UPDATED_AT | TIMESTAMP | Timestamp de atualização |

## Funcionalidades Implementadas

✅ **Constraints e Validações**
- Login único
- Email único
- CPF único (quando informado)
- Validação de perfis
- Validação de status

✅ **Índices para Performance**
- Índice no campo LOGIN
- Índice no campo EMAIL
- Índice no campo PERFIL
- Índice no campo ATIVO

✅ **Triggers de Auditoria**
- Atualização automática do campo `updated_at`

✅ **Segurança**
- Senhas armazenadas com hash bcrypt
- Controle de tentativas de login
- Sistema de bloqueio de usuários
- Expiração de senhas (90 dias)

## Próximos Passos

1. ✅ Executar um dos scripts acima
2. ✅ Verificar se a tabela foi criada
3. ✅ Testar login com usuário administrador
4. 🔄 Alterar senha padrão no primeiro acesso
5. 🔄 Criar usuários adicionais conforme necessário

## Troubleshooting

**Erro: "relation already exists"**
- A tabela já foi criada. Verifique com: `SELECT * FROM TABELA_USUARIO;`

**Erro: "permission denied"**
- Verifique se o usuário tem permissões de escrita no banco

**Erro: "database does not exist"**
- Crie o banco `sispatrimonio` primeiro: `CREATE DATABASE sispatrimonio;`

---

**📝 Nota:** Após implementar a tabela, o sistema Java poderá autenticar usuários corretamente no PostgreSQL.