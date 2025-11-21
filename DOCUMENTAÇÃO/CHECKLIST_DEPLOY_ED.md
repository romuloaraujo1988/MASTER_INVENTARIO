# ✅ Checklist de Deploy - Campo ED

## 📋 Pré-Deploy

### Backup
- [ ] Backup completo do banco de dados criado
- [ ] Backup testado e validado
- [ ] Backup armazenado em local seguro
- [ ] Plano de rollback documentado

### Código
- [ ] Código compilado sem erros
- [ ] Testes unitários passando
- [ ] Testes de integração passando
- [ ] Code review realizado

### Documentação
- [ ] Documentação técnica completa
- [ ] Guia de uso criado
- [ ] Resumo executivo preparado
- [ ] Changelog atualizado

---

## 🚀 Deploy - Banco de Dados

### Passo 1: Backup
```bash
# Criar backup
pg_dump -h localhost -U postgres -d sispatrimonio -F c -b -v -f backup_pre_ed.backup

# Verificar backup
ls -lh backup_pre_ed.backup
```
- [ ] Backup criado com sucesso
- [ ] Tamanho do backup verificado
- [ ] Backup copiado para local seguro

### Passo 2: Executar Migração
```bash
# Executar script de migração
psql -h localhost -U postgres -d sispatrimonio -f sql/migration_add_ed_nf_fornecedor.sql
```
- [ ] Script executado sem erros
- [ ] Colunas criadas com sucesso
- [ ] Índices criados com sucesso
- [ ] Comentários adicionados

### Passo 3: Validar Migração
```sql
-- Verificar estrutura
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tabela_patrimonio'
AND column_name IN ('ed', 'numero_nota_fiscal', 'fornecedor');

-- Verificar índices
SELECT indexname FROM pg_indexes
WHERE tablename = 'tabela_patrimonio'
AND indexname LIKE 'idx_patrimonio_%';

-- Verificar dados
SELECT COUNT(*) as total FROM TABELA_PATRIMONIO;
```
- [ ] Colunas existem com tipos corretos
- [ ] Índices criados corretamente
- [ ] Dados preservados (11.428 registros)

---

## 🚀 Deploy - Backend

### Passo 1: Compilar
```bash
# Compilar projeto
mvn clean compile

# Verificar erros
mvn clean package -DskipTests
```
- [ ] Compilação sem erros
- [ ] JAR gerado com sucesso
- [ ] Tamanho do JAR verificado

### Passo 2: Testar Localmente
```bash
# Executar aplicação
java -jar target/sistema-inventario-1.2.0.jar

# Testar endpoints
curl http://localhost:8080/api/mobile/patrimonio/numero/3241
```
- [ ] Aplicação iniciou sem erros
- [ ] Endpoints respondendo
- [ ] Campo ED presente na resposta

### Passo 3: Deploy em Produção
```bash
# Parar aplicação
systemctl stop sistema-inventario

# Copiar novo JAR
cp target/sistema-inventario-1.2.0.jar /opt/sistema-inventario/

# Iniciar aplicação
systemctl start sistema-inventario

# Verificar logs
tail -f /var/log/sistema-inventario/server.log
```
- [ ] Aplicação parada com sucesso
- [ ] JAR copiado para produção
- [ ] Aplicação iniciada sem erros
- [ ] Logs sem erros críticos

---

## 🚀 Deploy - Importação

### Passo 1: Testar Importação CSV
```bash
# Preparar arquivo de teste
cat > teste_ed.csv << EOF
NUMERO,STATUS,ED,DESCRICAO,ROTULOS,CARGA_ATUAL,SETOR_RESPONSAVEL,CAMPUS,VALOR_AQUISICAO,VALOR_DEPRECIADO,NUMERO_NOTA_FISCAL,NUMERO_SERIE,DATA_ENTRADA,DATA_CARGA,FORNECEDOR,SALA,ESTADO_CONSERVACAO
TEST-001,ATIVO,12311.0101,Teste ED,TESTE,João Silva,TI,CUIABA,100.00,80.00,NF-TEST-001,SN-001,01/01/2024,01/01/2024 10:00:00,Fornecedor Teste,Sala 101,BOM
EOF

# Importar via sistema
# (executar via interface desktop)
```
- [ ] Arquivo de teste criado
- [ ] Importação executada
- [ ] Campo ED importado corretamente
- [ ] Dados de teste removidos

### Passo 2: Testar Importação Excel
```bash
# Criar arquivo Excel de teste
# (usar LibreOffice ou Excel)
```
- [ ] Arquivo Excel criado
- [ ] Cabeçalho "ED" reconhecido
- [ ] Importação executada
- [ ] Campo ED importado corretamente
- [ ] Dados de teste removidos

---

## 🧪 Testes Pós-Deploy

### Teste 1: Inserção Manual
```sql
INSERT INTO TABELA_PATRIMONIO (NUMERO, STATUS, ED, DESCRICAO, NUMERO_NOTA_FISCAL, FORNECEDOR)
VALUES ('TEST-MANUAL-001', 'ATIVO', '12311.0101', 'Teste Manual', 'NF-TEST-001', 'Fornecedor Teste');

SELECT NUMERO, ED, NUMERO_NOTA_FISCAL, FORNECEDOR
FROM TABELA_PATRIMONIO
WHERE NUMERO = 'TEST-MANUAL-001';

DELETE FROM TABELA_PATRIMONIO WHERE NUMERO = 'TEST-MANUAL-001';
```
- [ ] Inserção executada com sucesso
- [ ] Dados consultados corretamente
- [ ] Dados de teste removidos

### Teste 2: API Mobile
```bash
# Testar endpoint
curl http://localhost:8080/api/mobile/patrimonio/numero/3241

# Verificar resposta
# Deve conter: "ed": "12311.0101"
```
- [ ] Endpoint respondendo
- [ ] Campo ED presente na resposta
- [ ] Formato JSON correto

### Teste 3: Importação CSV
```bash
# Importar arquivo de teste
# Verificar logs de importação
# Verificar dados no banco
```
- [ ] Importação executada
- [ ] Campo ED importado
- [ ] Sem erros nos logs

### Teste 4: Importação Excel
```bash
# Importar arquivo de teste
# Verificar logs de importação
# Verificar dados no banco
```
- [ ] Importação executada
- [ ] Campo ED importado
- [ ] Sem erros nos logs

---

## 📊 Validação Final

### Banco de Dados
```sql
-- Verificar total de patrimônios
SELECT COUNT(*) FROM TABELA_PATRIMONIO;
-- Esperado: 11.428

-- Verificar patrimônios com ED
SELECT COUNT(*) FROM TABELA_PATRIMONIO WHERE ED IS NOT NULL;

-- Verificar índices
SELECT indexname FROM pg_indexes WHERE tablename = 'tabela_patrimonio';
```
- [ ] Total de patrimônios correto
- [ ] Índices criados
- [ ] Performance mantida

### API Mobile
```bash
# Testar endpoints principais
curl http://localhost:8080/api/mobile/patrimonio/numero/3241
curl http://localhost:8080/api/mobile/patrimonio/numero/3260
curl http://localhost:8080/api/mobile/patrimonio/numero/16371
```
- [ ] Todos os endpoints respondendo
- [ ] Campo ED presente nas respostas
- [ ] Tempo de resposta < 500ms

### Importação
```bash
# Testar importação de arquivo real
# (usar arquivo de produção)
```
- [ ] Importação executada
- [ ] Campo ED importado
- [ ] Sem erros nos logs
- [ ] Dados validados no banco

---

## 🚨 Plano de Rollback

### Se algo der errado:

#### Passo 1: Parar Aplicação
```bash
systemctl stop sistema-inventario
```

#### Passo 2: Restaurar Banco
```bash
# Restaurar backup
pg_restore -h localhost -U postgres -d sispatrimonio -c backup_pre_ed.backup
```

#### Passo 3: Restaurar JAR Anterior
```bash
# Copiar JAR anterior
cp /opt/sistema-inventario/backup/sistema-inventario-1.1.0.jar /opt/sistema-inventario/sistema-inventario-1.2.0.jar
```

#### Passo 4: Reiniciar Aplicação
```bash
systemctl start sistema-inventario
tail -f /var/log/sistema-inventario/server.log
```

#### Passo 5: Validar Rollback
```bash
# Testar endpoints
curl http://localhost:8080/api/mobile/patrimonio/numero/3241

# Verificar banco
psql -h localhost -U postgres -d sispatrimonio -c "SELECT COUNT(*) FROM TABELA_PATRIMONIO;"
```

---

## 📝 Pós-Deploy

### Documentação
- [ ] Changelog atualizado
- [ ] Versão incrementada (1.1.0 → 1.2.0)
- [ ] Release notes publicadas
- [ ] Documentação de API atualizada

### Comunicação
- [ ] Equipe técnica notificada
- [ ] Usuários informados sobre nova funcionalidade
- [ ] Treinamento agendado (se necessário)
- [ ] Suporte preparado para dúvidas

### Monitoramento
- [ ] Logs monitorados por 24h
- [ ] Performance monitorada
- [ ] Erros monitorados
- [ ] Feedback dos usuários coletado

---

## ✅ Aprovações

### Técnica
- [ ] Desenvolvedor: _________________ Data: ____/____/____
- [ ] Tech Lead: _________________ Data: ____/____/____
- [ ] DBA: _________________ Data: ____/____/____

### Negócio
- [ ] Product Owner: _________________ Data: ____/____/____
- [ ] Gestor: _________________ Data: ____/____/____

---

## 📊 Métricas de Sucesso

### Após 24h
- [ ] Zero erros críticos
- [ ] Performance mantida
- [ ] Importações funcionando
- [ ] API respondendo corretamente

### Após 1 semana
- [ ] Feedback positivo dos usuários
- [ ] Importações com ED realizadas
- [ ] Integração SIADS testada
- [ ] Sem necessidade de rollback

---

**Data de Deploy:** ____/____/____  
**Horário:** ____:____  
**Responsável:** _________________  
**Status:** ⬜ Pendente | ⬜ Em Andamento | ⬜ Concluído | ⬜ Rollback

---

**Versão:** 1.0.0  
**Data:** 16/11/2024  
**Status:** ✅ Checklist Completo
