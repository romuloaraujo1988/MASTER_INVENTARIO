# Teste de Exportação SIADS com Código UOrg

## Objetivo
Validar que o código UOrg do campus está sendo corretamente exportado no arquivo SIADS.

## Pré-requisitos

### 1. Banco de Dados Atualizado
- ✅ Coluna `codigo_uorg` adicionada na tabela `TABELA_CAMPUS`
- ✅ Script SQL executado com sucesso

### 2. Campus Configurados
Cada campus deve ter um código UOrg cadastrado:

```sql
-- Verificar campus sem código UOrg
SELECT id, nome, codigo_uorg 
FROM TABELA_CAMPUS 
WHERE codigo_uorg IS NULL OR codigo_uorg = '';

-- Atualizar campus (exemplo)
UPDATE TABELA_CAMPUS 
SET codigo_uorg = '1790001' 
WHERE id = 1;
```

### 3. Dados de Teste
- Pelo menos 1 campus com código UOrg configurado
- Pelo menos 1 setor vinculado ao campus
- Pelo menos 1 patrimônio vinculado ao setor
- Pelo menos 1 coleta de inventário realizada

## Procedimento de Teste

### Passo 1: Configurar Campus
1. Abrir a aplicação desktop
2. Acessar menu: **Cadastros → Campus** (ou executar `CampusManagementFrame`)
3. Editar um campus existente
4. Preencher o campo **"Código UOrg (SIADS)"** com valor válido
   - Exemplo: `1790001`
   - Formato: 7 dígitos numéricos
5. Salvar

### Passo 2: Verificar Hierarquia
Confirmar que a hierarquia está correta:
```
Campus (com código UOrg)
  └── Setor
       └── Sala
            └── Patrimônio
```

### Passo 3: Realizar Coleta (se necessário)
1. Criar um novo inventário
2. Realizar coleta dos patrimônios
3. Finalizar o inventário

### Passo 4: Exportar para SIADS
1. Abrir a tela de exportação SIADS
2. Selecionar o inventário
3. Configurar parâmetros:
   - Código do Órgão: `25000` (IFMT)
   - Código UG: `00001`
   - CPF Responsável: (CPF válido)
4. Clicar em **"Exportar"**
5. Salvar o arquivo

### Passo 5: Validar Arquivo Gerado

#### 5.1 Estrutura do Arquivo
O arquivo deve ter 3 seções:
```
H¥PE¥1¥25000¥00001¥00000000000¥00001¥£
D¥...¥1790001¥...£
T¥...¥FIM¥£
```

#### 5.2 Verificar Código UOrg
Abrir o arquivo em um editor de texto e localizar o campo do código UOrg:

**Formato da linha de detalhe (D):**
```
D¥codigoMaterial¥descricao¥catmat¥endereco¥CODIGO_UORG¥...
                                            ^^^^^^^^^^^
                                            Campo 5
```

**Exemplo esperado:**
```
D¥P12345¥COMPUTADOR DESKTOP¥123456¥SALA 101¥1790001¥1¥2¥1¥15052024¥150000¥...£
                                              ^^^^^^^
                                              Código UOrg do campus
```

#### 5.3 Validação Automática
Executar script de validação:

```powershell
# Extrair códigos UOrg do arquivo
$arquivo = "SIADS_20251108_143000.txt"
$linhas = Get-Content $arquivo -Encoding UTF8

foreach ($linha in $linhas) {
    if ($linha.StartsWith("D¥")) {
        $campos = $linha -split "¥"
        $codigoUorg = $campos[5]
        Write-Host "Patrimonio: $($campos[1]) - Codigo UOrg: $codigoUorg"
    }
}
```

## Casos de Teste

### Caso 1: Campus com Código UOrg Configurado
**Entrada:**
- Campus: "Campus Cuiabá"
- Código UOrg: "1790001"
- Patrimônio: "12345"

**Resultado Esperado:**
```
D¥P12345¥...¥1790001¥...£
```

**Status:** [ ] Passou [ ] Falhou

---

### Caso 2: Campus sem Código UOrg (Fallback)
**Entrada:**
- Campus: "Campus Teste"
- Código UOrg: (vazio)
- Patrimônio: "67890"
- ID Sala: 42

**Resultado Esperado:**
```
D¥P67890¥...¥0000042¥...£
```
(Usa ID da sala formatado com 7 dígitos)

**Status:** [ ] Passou [ ] Falhou

---

### Caso 3: Múltiplos Campus
**Entrada:**
- Campus A: código "1790001"
- Campus B: código "1790002"
- Patrimônios de ambos os campus

**Resultado Esperado:**
- Patrimônios do Campus A: código "1790001"
- Patrimônios do Campus B: código "1790002"

**Status:** [ ] Passou [ ] Falhou

---

## Validação no SIADS

### Importação no Sistema
1. Acessar o SIADS
2. Ir para: **Importação → Material Permanente**
3. Fazer upload do arquivo gerado
4. Verificar mensagens de validação

### Erros Comuns

#### Erro: "Código UOrg inválido"
**Causa:** Código não existe no cadastro do SIADS
**Solução:** 
- Verificar código no SIADS
- Atualizar no sistema de inventário
- Gerar novo arquivo

#### Erro: "Formato de código incorreto"
**Causa:** Código não tem 7 dígitos
**Solução:**
- Verificar formato no banco de dados
- Deve ser exatamente 7 dígitos numéricos

#### Erro: "Patrimônio sem localização"
**Causa:** Código UOrg vazio ou nulo
**Solução:**
- Cadastrar código UOrg no campus
- Verificar hierarquia (Campus → Setor → Sala → Patrimônio)

## Checklist de Validação

- [ ] Banco de dados atualizado com coluna `codigo_uorg`
- [ ] Todos os campus têm código UOrg cadastrado
- [ ] Código UOrg tem formato correto (7 dígitos)
- [ ] Arquivo SIADS gerado com sucesso
- [ ] Código UOrg aparece no campo correto (campo 5)
- [ ] Arquivo importado no SIADS sem erros
- [ ] Patrimônios aparecem na localização correta no SIADS

## Logs e Evidências

### Consulta SQL para Verificação
```sql
-- Verificar patrimônios com código UOrg
SELECT 
    p.NUMERO as patrimonio,
    p.DESCRICAO,
    s.DESCRICAO as sala,
    st.NOME as setor,
    c.nome as campus,
    c.codigo_uorg
FROM TABELA_PATRIMONIO p
LEFT JOIN TABELA_SALA s ON p.ID_SALA = s.ID_SALA
LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID_SETOR
LEFT JOIN TABELA_CAMPUS c ON st.ID_CAMPUS = c.id
WHERE p.STATUS = 'ATIVO'
ORDER BY c.nome, p.NUMERO;
```

### Exemplo de Saída Esperada
```
patrimonio | descricao          | sala      | setor      | campus         | codigo_uorg
-----------+--------------------+-----------+------------+----------------+-------------
12345      | COMPUTADOR DESKTOP | SALA 101  | TI         | Campus Cuiabá  | 1790001
12346      | IMPRESSORA LASER   | SALA 102  | TI         | Campus Cuiabá  | 1790001
67890      | PROJETOR MULTIM.   | SALA 201  | Pedagogico | Campus Várzea  | 1790002
```

## Relatório de Teste

**Data do Teste:** ___/___/______
**Testador:** _______________________
**Versão do Sistema:** 1.2.0

### Resultados
- Total de casos testados: ___
- Casos aprovados: ___
- Casos reprovados: ___
- Observações:
  ```
  
  
  
  ```

### Aprovação
- [ ] Todos os testes passaram
- [ ] Sistema pronto para produção
- [ ] Documentação atualizada

**Assinatura:** _______________________

---

## Referências
- Manual SIADS: Guia de Orientações Gerais para Geração dos Arquivos de Implantação v6.2.11
- Documentação: `DOCUMENTAÇÃO/CODIGO_UORG_SIADS.md`
- Script SQL: `sql/adicionar_codigo_uorg_campus.sql`
