# Guia de Teste - Visualizar Coletas

## ✅ APK Instalado com Sucesso!

O app foi instalado no emulador: **Medium_Phone_API_36.1**

## Passos para Testar

### 1. Iniciar o Servidor Backend (se não estiver rodando)

```powershell
.\mvnw.cmd spring-boot:run
```

Aguarde até ver:
```
Started InventarioApplication in X.XXX seconds
```

### 2. Abrir o App no Emulador

1. No emulador, localize o app "Inventário"
2. Toque para abrir

### 3. Fazer Login

- **Usuário:** admin
- **Senha:** admin123

### 4. Testar Visualização de Coletas

1. Na tela do Dashboard, você verá:
   - Card "Total de Patrimônios"
   - Card "Coletas Pendentes"
   - Botões de ação

2. Clique no botão **"Visualizar Coletas"**

3. A tela deve carregar e mostrar:
   - Lista de coletas realizadas
   - Informações de cada coleta:
     - Número do patrimônio
     - Descrição
     - Data da coleta
     - Status
     - Nome do coletor
     - Localização

### 5. Testar Filtros (se houver coletas)

- **Filtro por Usuário:**
  - "Todas as Coletas" - mostra todas
  - "Minhas Coletas" - mostra apenas suas coletas

- **Filtro por Sala:**
  - Use o dropdown para filtrar por sala específica

### 6. Se Não Houver Coletas

Se a lista aparecer vazia, você pode criar uma coleta de teste:

1. Volte ao Dashboard
2. Clique em **"Scan Rápido"** ou **"Coleta Manual"**
3. Selecione uma sala
4. Escaneie ou digite um número de patrimônio
5. Confirme a coleta
6. Volte para "Visualizar Coletas"

## Verificação de Logs

### Logs do App (Logcat)

Para ver os logs do app em tempo real:

```powershell
# Encontre o caminho do adb (geralmente em):
# C:\Users\[SEU_USUARIO]\AppData\Local\Android\Sdk\platform-tools\adb.exe

# Execute:
& "C:\Users\[SEU_USUARIO]\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat | Select-String "CollectionView"
```

Ou use o Logcat do Android Studio.

### Logs do Servidor

No terminal onde o servidor está rodando, procure por:

```
Username extraído do token JWT: admin
Buscando todas as coletas para usuário: admin
Encontradas X coletas para o usuário admin
```

## Resultado Esperado

### Se Houver Coletas:
- ✅ Lista de coletas aparece
- ✅ Cada item mostra informações completas
- ✅ Filtros funcionam corretamente
- ✅ Contador mostra total correto

### Se Não Houver Coletas:
- ✅ Mensagem "Nenhuma coleta encontrada" aparece
- ✅ Ícone de estado vazio é exibido
- ✅ Não há erros no log

## Problemas Comuns

### 1. Erro de Conexão
**Sintoma:** "Erro ao carregar coletas" ou timeout

**Solução:**
- Verifique se o servidor está rodando
- Verifique se o emulador consegue acessar localhost:8081
- No emulador, use `10.0.2.2:8081` em vez de `localhost:8081`

### 2. Lista Vazia (mas há coletas no banco)
**Sintoma:** Lista aparece vazia, mas você sabe que há coletas

**Solução:**
1. Verifique os logs do servidor
2. Verifique se as coletas pertencem ao usuário logado
3. Execute: `.\test-coletas-endpoint.ps1` para testar o endpoint

### 3. Erro 401 (Unauthorized)
**Sintoma:** Mensagem de erro de autenticação

**Solução:**
- Faça logout e login novamente
- Limpe os dados do app: Settings → Apps → Inventário → Clear Data

### 4. App Trava ou Fecha
**Sintoma:** App fecha ao clicar em "Visualizar Coletas"

**Solução:**
1. Verifique o Logcat para ver o erro
2. Reinstale o app:
   ```powershell
   cd InventarioMobile
   .\gradlew clean installDebug
   ```

## Teste do Endpoint (Opcional)

Para confirmar que o backend está funcionando:

```powershell
.\test-coletas-endpoint.ps1
```

Deve retornar:
```
✓ Login realizado com sucesso!
✓ Coletas obtidas com sucesso!
Total de coletas: X
```

## Verificar Coletas no Banco (Opcional)

Para ver quantas coletas existem no banco:

```powershell
# Ajuste o caminho do psql conforme sua instalação
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -d inventario -c "SELECT COUNT(*) as TOTAL FROM COLETA;"
```

## Criar Coleta de Teste (se necessário)

Se não houver coletas no banco, você pode criar uma via SQL:

```sql
-- Verificar IDs necessários
SELECT ID, NOME FROM INVENTARIO LIMIT 1;
SELECT ID, LOGIN FROM TABELA_USUARIO WHERE LOGIN = 'admin';
SELECT ID, NUMERO FROM PATRIMONIO LIMIT 1;

-- Inserir coleta de teste (ajuste os IDs conforme necessário)
INSERT INTO COLETA (
    ID_INVENTARIO,
    ID_COLETOR,
    ID_PATRIMONIO,
    DATA_COLETA,
    STATUS_COLETA,
    LOCALIZACAO_ENCONTRADA,
    OBSERVACAO_COLETA
) VALUES (
    1,  -- ID do inventário
    1,  -- ID do usuário admin
    1,  -- ID do patrimônio
    NOW(),
    'COLETADO',
    'Sala 101',
    'Coleta de teste'
);
```

## Checklist de Teste

- [ ] Servidor backend está rodando
- [ ] App instalado no emulador
- [ ] Login realizado com sucesso
- [ ] Dashboard carrega corretamente
- [ ] Botão "Visualizar Coletas" clicável
- [ ] Tela de coletas abre sem erros
- [ ] Lista de coletas aparece (ou mensagem de vazio)
- [ ] Filtros funcionam corretamente
- [ ] Informações das coletas estão completas
- [ ] Não há erros no Logcat

## Próximos Testes

Após confirmar que a visualização funciona:

1. **Criar Nova Coleta:**
   - Testar "Scan Rápido"
   - Testar "Coleta Manual"
   - Testar "Coleta por Descrição"

2. **Sincronização:**
   - Criar coleta offline
   - Sincronizar com servidor
   - Verificar se aparece na lista

3. **Filtros Avançados:**
   - Filtrar por sala
   - Filtrar por usuário
   - Combinar filtros

## Sucesso! 🎉

Se tudo funcionar:
- ✅ Endpoint implementado corretamente
- ✅ Extração de username do token funcionando
- ✅ App consegue listar coletas
- ✅ Problema resolvido!

---

**Dica:** Mantenha o Logcat aberto durante os testes para identificar rapidamente qualquer problema.
