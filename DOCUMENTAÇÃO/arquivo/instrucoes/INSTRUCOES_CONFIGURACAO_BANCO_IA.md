# Instruções para Configuração do Banco de Dados - Treinamento de IA

## Problema Identificado

O erro ao treinar a IA pela interface web ocorre porque a conexão com o banco de dados PostgreSQL não está disponível. O sistema tenta conectar com:

- **Host**: localhost
- **Porta**: 5432
- **Banco**: sispatrimonio
- **Usuário**: postgres
- **Senha**: Romulo@2020

## Soluções Possíveis

### 1. Instalar e Configurar PostgreSQL

#### Instalação:
1. Baixe o PostgreSQL em: https://www.postgresql.org/download/
2. Execute o instalador
3. Durante a instalação, defina a senha do usuário `postgres` como `Romulo@2020`
4. Mantenha a porta padrão `5432`

#### Configuração:
1. Abra o pgAdmin ou psql
2. Conecte com o usuário `postgres` e senha `Romulo@2020`
3. Crie o banco de dados:
   ```sql
   CREATE DATABASE sispatrimonio;
   ```

### 2. Executar Scripts de Criação das Tabelas

Após criar o banco, execute os scripts SQL disponíveis no projeto:

```bash
# Execute os scripts na seguinte ordem:
1. criar_tabelas_sistema_inventario.sql
2. script_tabela_patrimonio.sql
3. script_tabela_inventario.sql
4. script_tabela_setor.sql
5. script_tabela_coleta.sql
6. script_tabela_responsavel.sql
```

### 3. Verificar Conexão

Para testar se a conexão está funcionando:

1. Abra o terminal/prompt de comando
2. Execute:
   ```bash
   psql -h localhost -p 5432 -U postgres -d sispatrimonio
   ```
3. Digite a senha: `Romulo@2020`
4. Se conectar com sucesso, o banco está configurado corretamente

### 4. Alternativa: Modificar Credenciais

Se você já tem PostgreSQL instalado com credenciais diferentes, modifique o arquivo `AITrainingService.java`:

```java
// Linhas 15-17 (atualmente configurado para:)
private static final String DB_URL = "jdbc:postgresql://localhost:5432/sispatrimonio";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "Romulo@2020";
```

Após modificar, recompile:
```bash
javac -cp "lib/*" *.java
```

## Testando o Treinamento

1. Certifique-se de que o PostgreSQL está rodando
2. Acesse: http://localhost:8080/chatbot.html
3. Clique no botão "Treinar IA"
4. Se configurado corretamente, você verá: "IA treinada com sucesso!"

## Mensagens de Erro Comuns

- **"Connection refused"**: PostgreSQL não está rodando
- **"Authentication failed"**: Usuário/senha incorretos
- **"Database does not exist"**: Banco 'sispatrimonio' não foi criado
- **"Connection timeout"**: Firewall bloqueando a porta 5432

## Logs do Sistema

Para diagnosticar problemas, verifique os logs no terminal onde o servidor está rodando. As mensagens incluem:

- ✅ "Conexão com banco de dados estabelecida com sucesso."
- ❌ "Erro ao conectar com banco de dados: [detalhes]"
- ❌ "Conexão com banco de dados não está disponível."

## Suporte

Se ainda houver problemas:

1. Verifique se o PostgreSQL está instalado e rodando
2. Confirme as credenciais de acesso
3. Teste a conexão manualmente com psql
4. Verifique os logs do servidor para erros específicos