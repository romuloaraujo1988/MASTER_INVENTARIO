# Teste Rápido - Autorização de Coleta

## ⚡ Teste em 5 Minutos

### 1. Verificar no Banco de Dados (1 min)

Execute este SQL para verificar se o usuário é participante:

```sql
-- Substitua 'Nome do Usuario' pelo nome real
SELECT 
    u.ID as ID_USUARIO,
    u.NOME_COMPLETO,
    u.PERFIL,
    pi.ID as ID_PARTICIPANTE,
    pi.PAPEL,
    pi.ATIVO,
    i.NOME as INVENTARIO
FROM TABELA_USUARIO u
LEFT JOIN TABELA_PARTICIPANTE_INVENTARIO pi ON u.ID = pi.ID_USUARIO
LEFT JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
WHERE u.NOME_COMPLETO LIKE '%Nome do Usuario%'
AND (i.STATUS_INVENTARIO = 'EM_ANDAMENTO' OR i.ID IS NULL);
```

**Resultado esperado:**
- Se `ID_PARTICIPANTE` for NULL → Usuário NÃO é participante (precisa adicionar)
- Se `ATIVO` for FALSE → Usuário está inativo (precisa reativar)
- Se `ATIVO` for TRUE → Usuário está OK ✅

### 2. Adicionar Usuário como Participante (se necessário) (1 min)

```sql
-- Substitua os IDs pelos valores reais
INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO, OBSERVACOES)
VALUES 
    (1, 2, 'COLETOR', TRUE, 'Adicionado para realizar coletas')
ON CONFLICT (ID_INVENTARIO, ID_USUARIO) 
DO UPDATE SET 
    ATIVO = TRUE,
    DATA_REMOCAO = NULL,
    DATA_ULTIMA_ATUALIZACAO = CURRENT_TIMESTAMP;
```

### 3. Recompilar o Projeto (1 min)

No terminal:

```bash
# Se tiver Maven no PATH
mvn clean compile

# Ou use o Maven Wrapper
./mvnw clean compile

# Ou recompile via IDE (IntelliJ/Eclipse)
```

### 4. Reiniciar o Aplicativo Desktop (30 seg)

1. Feche o aplicativo se estiver aberto
2. Execute novamente
3. Faça login com o usuário

### 5. Testar Coleta (1 min 30 seg)

1. Abra **Coleta de Patrimônios**
2. Selecione uma sala
3. Digite um número de patrimônio ou use leitor de código de barras
4. Clique em **Buscar**
5. Clique em **Coletar**

**Resultado esperado:**
- ✅ Coleta realizada com sucesso
- ✅ Item aparece na tabela de histórico
- ❌ Se ainda der erro, veja troubleshooting abaixo

---

## 🔍 Troubleshooting Rápido

### Erro persiste após correção?

**1. Verificar logs do aplicativo:**

Procure por estas mensagens no console:

```
[DEBUG ParticipanteInventarioDAO] Buscando participante: idInventario=X, idUsuario=Y
[DEBUG ParticipanteInventarioDAO] Participante encontrado: ID=Z
```

Se aparecer "Participante NÃO encontrado", o usuário não está cadastrado.

**2. Verificar se o código foi recompilado:**

```bash
# Verificar data de modificação do .class
ls -la target/classes/com/inventario/dao/ParticipanteInventarioDAO.class
```

A data deve ser recente (após a correção).

**3. Verificar conexão com banco:**

```sql
-- Testar se a query funciona diretamente
SELECT ID FROM TABELA_PARTICIPANTE_INVENTARIO 
WHERE ID_INVENTARIO = 1 AND ID_USUARIO = 2 AND ATIVO = TRUE;
```

Deve retornar um ID. Se não retornar, o usuário não está cadastrado.

**4. Verificar inventário ativo:**

```sql
SELECT * FROM TABELA_INVENTARIO 
WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO';
```

Deve retornar exatamente 1 registro. Se não retornar, não há inventário ativo.

---

## 📋 Checklist Rápido

- [ ] Código corrigido em `ParticipanteInventarioDAO.java`
- [ ] Projeto recompilado
- [ ] Usuário cadastrado em `TABELA_PARTICIPANTE_INVENTARIO`
- [ ] Usuário com `ATIVO = TRUE`
- [ ] Inventário com `STATUS_INVENTARIO = 'EM_ANDAMENTO'`
- [ ] Aplicativo reiniciado
- [ ] Login realizado
- [ ] Teste de coleta executado
- [ ] Coleta funcionou ✅

---

## 🚀 Comandos Rápidos

### Adicionar usuário como coletor (copiar e colar)

```sql
-- ATENÇÃO: Ajuste os IDs antes de executar!

-- 1. Buscar IDs necessários
SELECT 
    (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1) as ID_INVENTARIO,
    (SELECT ID FROM TABELA_USUARIO WHERE NOME_COMPLETO LIKE '%NOME_AQUI%' LIMIT 1) as ID_USUARIO;

-- 2. Adicionar como participante
INSERT INTO TABELA_PARTICIPANTE_INVENTARIO 
    (ID_INVENTARIO, ID_USUARIO, PAPEL, ATIVO)
SELECT 
    (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1),
    (SELECT ID FROM TABELA_USUARIO WHERE NOME_COMPLETO LIKE '%NOME_AQUI%' LIMIT 1),
    'COLETOR',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM TABELA_PARTICIPANTE_INVENTARIO
    WHERE ID_INVENTARIO = (SELECT ID FROM TABELA_INVENTARIO WHERE STATUS_INVENTARIO = 'EM_ANDAMENTO' LIMIT 1)
    AND ID_USUARIO = (SELECT ID FROM TABELA_USUARIO WHERE NOME_COMPLETO LIKE '%NOME_AQUI%' LIMIT 1)
);
```

### Verificar se funcionou

```sql
SELECT 
    u.NOME_COMPLETO,
    pi.PAPEL,
    pi.ATIVO,
    i.NOME as INVENTARIO
FROM TABELA_PARTICIPANTE_INVENTARIO pi
JOIN TABELA_USUARIO u ON pi.ID_USUARIO = u.ID
JOIN TABELA_INVENTARIO i ON pi.ID_INVENTARIO = i.ID
WHERE u.NOME_COMPLETO LIKE '%NOME_AQUI%'
AND i.STATUS_INVENTARIO = 'EM_ANDAMENTO';
```

---

## ✅ Validação Final

Se tudo estiver correto, você deve ver:

1. **No banco de dados:**
   - Usuário aparece em `TABELA_PARTICIPANTE_INVENTARIO`
   - `ATIVO = TRUE`
   - `PAPEL = 'COLETOR'`

2. **No aplicativo:**
   - Tela de coleta abre normalmente
   - Busca de patrimônio funciona
   - Botão "Coletar" está habilitado
   - Coleta é registrada com sucesso

3. **Nos logs:**
   ```
   [DEBUG ParticipanteInventarioDAO] Participante encontrado: ID=X
   ```

---

**Tempo total estimado:** 5 minutos
**Dificuldade:** Baixa
**Requer reinicialização:** Sim (aplicativo)
