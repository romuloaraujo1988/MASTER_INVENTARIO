# Configuração de Credenciais do Banco — Leia antes de rodar

A partir de 31/05/2026, **nenhuma credencial de banco é versionada no Git**. Os
arquivos de configuração reais foram removidos do versionamento (constam no
`.gitignore`) e substituídos por *templates* `.example`.

## Como configurar localmente

1. Copie os templates removendo o sufixo `.example`:

   ```powershell
   Copy-Item config\configuracao_banco.json.example config\configuracao_banco.json
   Copy-Item config\database-config.properties.example config\database-config.properties
   ```

2. Edite os arquivos copiados e substitua `COLOQUE_A_SENHA_AQUI` pela senha real
   do PostgreSQL do seu ambiente.

3. (Opcional, recomendado para servidor/CI) Em vez de arquivos, defina variáveis
   de ambiente — o código as prioriza:

   ```
   DATASOURCE_URL=jdbc:postgresql://localhost:5432/sispatrimonio
   DATASOURCE_USERNAME=postgres
   DATASOURCE_PASSWORD=<sua_senha>
   JWT_SECRET=<segredo_com_no_minimo_32_bytes>
   SPRING_SECURITY_PASSWORD=<senha_admin_actuator>
   ```

## Ordem de resolução da senha (código)

`HikariConnectionPool` e os demais pontos resolvem a senha nesta ordem:

1. `~/configuracao_banco.properties` (legado, se existir)
2. `config/configuracao_banco.json` (ou `configuracao_banco.json` na raiz)
3. Variável de ambiente `DATASOURCE_PASSWORD`
4. System property `db.password`

Se nenhuma fonte fornecer a senha, a aplicação registra um aviso e a conexão
falhará de forma explícita (preferível a embutir credencial no binário).

## Docker

O `infrastructure/docker/docker-compose-scalable.yml` agora exige as variáveis
`POSTGRES_PASSWORD` e `JWT_SECRET` (falha se ausentes). Crie um arquivo `.env`
ao lado do compose (o `.env` já está no `.gitignore`):

```
POSTGRES_PASSWORD=<sua_senha>
JWT_SECRET=<segredo_forte>
```

## ⚠️ Pendências operacionais (NÃO automatizáveis com segurança)

A senha `Romulo@1919` **já esteve versionada** e deve ser considerada
**comprometida**. Para conformidade completa, ainda é necessário (ação humana):

1. **Rotacionar a senha do PostgreSQL** em todos os ambientes:
   `ALTER USER postgres WITH PASSWORD '<nova_senha_forte>';`
2. **Limpar o histórico do Git** (a senha permanece em commits antigos) com
   `git filter-repo` ou BFG, seguido de `force push` coordenado com a equipe.
3. **Gerar novo `JWT_SECRET`** (mínimo 32 bytes) — força re-login de todos.

Consulte a spec `correcao-credenciais-hardcoded-build` para o checklist completo.
