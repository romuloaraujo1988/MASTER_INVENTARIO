# Plano de Importação de Dados - PATRIMONIO IFMT 26.06.2025.csv

## Análise da Estrutura dos Dados

### Estrutura do Arquivo CSV
O arquivo CSV contém **11.379 registros** com as seguintes colunas:

1. **#** - Número sequencial
2. **NUMERO** - Número do patrimônio (ex: 3240, 3241)
3. **STATUS** - Status atual ("Ativo")
4. **ED** - Código de classificação (ex: 12311.0101)
5. **DESCRICAO** - Descrição completa do item
6. **RÓTULOS** - Tags/categorias (ex: "CBA-DAE", "ROO-ALM")
7. **CARGA ATUAL** - Responsável atual com setor
8. **SETOR DO RESPONSÁVEL** - Setor do responsável
9. **CAMPUS DA CARGA** - Campus (PDL)
10. **VALOR AQUISIÇÃO** - Valor de aquisição
11. **VALOR DEPRECIADO** - Valor depreciado
12. **NUMERO NOTA FISCAL** - Número da nota fiscal
13. **NÚMERO DE SÉRIE** - Número de série do equipamento
14. **DATA DA ENTRADA** - Data de entrada no patrimônio
15. **DATA DA CARGA** - Data da carga
16. **FORNECEDOR** - Fornecedor do item
17. **SALA** - Localização atual
18. **ESTADO DE CONSERVAÇÃO** - Estado físico do item

### Correlação com a Estrutura do Banco de Dados

#### Mapeamento Direto (1:1)
- **CSV.NUMERO** → **BD.NUMERO**
- **CSV.STATUS** → **BD.STATUS** ("Ativo" → "ATIVO")
- **CSV.DESCRICAO** → **BD.DESCRICAO**
- **CSV.RÓTULOS** → **BD.ROTULOS**
- **CSV.VALOR AQUISIÇÃO** → **BD.VALOR_AQUISICAO**
- **CSV.VALOR DEPRECIADO** → **BD.VALOR_DEPRECIADO**
- **CSV.NUMERO NOTA FISCAL** → **BD.NUMERO_NOTA_FISCAL**
- **CSV.NÚMERO DE SÉRIE** → **BD.NUMERO_SERIE**
- **CSV.DATA DA ENTRADA** → **BD.DATA_ENTRADA**
- **CSV.DATA DA CARGA** → **BD.DATA_CARGA**
- **CSV.FORNECEDOR** → **BD.FORNECEDOR**
- **CSV.OBSERVACOES** → **BD.OBSERVACOES** (se existir)

#### Mapeamento com Transformação
- **CSV.ESTADO DE CONSERVAÇÃO** → **BD.ESTADO_CONSERVACAO**
  - "Não aplicar" → "BOM" (padrão)
  - "Bom" → "BOM"
  - "Regular" → "REGULAR"
  - "Ruim" → "RUIM"
  - "Ocioso" → "BOM" (interpretação)
  - "Irreversível" → "PESSIMO"
  - "Recuperável" → "REGULAR"

#### Mapeamento com Relacionamento (Chaves Estrangeiras)
- **CSV.CARGA ATUAL** → **BD.ID_RESPONSAVEL**
  - Extrair nome do responsável
  - Buscar/criar na TABELA_RESPONSAVEL
  - Usar ID como referência

- **CSV.SALA** → **BD.ID_SALA**
  - Extrair nome da sala
  - Buscar/criar na TABELA_SALA
  - Usar ID_SALA como referência

#### Campos Adicionais do Sistema
- **BD.SITUACAO** → "ATIVO" (padrão para todos)
- **BD.MARCA** → Extrair da DESCRICAO (ex: "MARCA: MINIPA")
- **BD.MODELO** → Extrair da DESCRICAO (ex: "MODELO: MO-1225")

## Estratégia de Importação

### Fase 1: Preparação das Tabelas de Referência
1. **TABELA_RESPONSAVEL**: Extrair e inserir responsáveis únicos
2. **TABELA_SALA**: Extrair e inserir salas únicas
3. **TABELA_SETOR**: Verificar setores existentes

### Fase 2: Processamento dos Dados
1. **Limpeza**: Tratar valores nulos e inconsistências
2. **Extração**: Separar MARCA e MODELO da DESCRICAO
3. **Validação**: Verificar formatos de data e valores numéricos
4. **Transformação**: Converter estados de conservação

### Fase 3: Importação Principal
1. **Inserção sequencial** na TABELA_PATRIMONIO
2. **Tratamento de duplicatas** por NUMERO
3. **Log de erros** para registros problemáticos
4. **Validação pós-importação**

### Fase 4: Verificação e Auditoria
1. **Contagem de registros** importados vs. arquivo
2. **Verificação de integridade** referencial
3. **Relatório de inconsistências**
4. **Backup pós-importação**

## Considerações Técnicas

### Tratamento de Dados Especiais
- **Datas**: Converter formato DD/MM/YYYY para DATE
- **Valores monetários**: Tratar vírgulas como separador decimal
- **Campos vazios**: Definir valores padrão apropriados
- **Caracteres especiais**: Normalizar encoding UTF-8

### Regras de Negócio Identificadas
- Todos os patrimônios no CSV estão "Ativo"
- Campus predominante é "PDL" (Primavera do Leste)
- Responsáveis seguem padrão: "Nome(Setor Código-Sigla)"
- Salas incluem localização detalhada entre parênteses

### Campos Calculados/Derivados
- **MARCA**: Regex para extrair "MARCA: ([^\s]+)"
- **MODELO**: Regex para extrair "MODELO: ([^\s]+)"
- **SITUACAO**: Sempre "ATIVO" para importação inicial

## Exemplos de Transformação

### Exemplo 1: Responsável
```
CSV: "Rosane Alves de Abreu(PDL PDL-ENS)"
Extração: Nome = "Rosane Alves de Abreu", Setor = "PDL-ENS"
BD: ID_RESPONSAVEL = (buscar/criar na TABELA_RESPONSAVEL)
```

### Exemplo 2: Sala
```
CSV: "LAB-ETEC(IFMT - PDL)"
Extração: Sala = "LAB-ETEC", Local = "IFMT - PDL"
BD: ID_SALA = (buscar/criar na TABELA_SALA)
```

### Exemplo 3: Marca e Modelo
```
CSV: "OSCILOSCOPIO ANALOGICO MARCA: MINIPA MODELO: MO-1225 SERIE: MO122501083"
Extração: MARCA = "MINIPA", MODELO = "MO-1225"
BD: MARCA = "MINIPA", MODELO = "MO-1225"
```

## Script de Importação Sugerido

### Estrutura do Script
1. **Conexão com banco de dados**
2. **Leitura do arquivo CSV**
3. **Processamento linha por linha**
4. **Inserção com tratamento de erros**
5. **Relatório final**

### Validações Necessárias
- Verificar se NUMERO já existe
- Validar formato de datas
- Verificar valores numéricos
- Confirmar integridade referencial

## Cronograma de Execução

1. **Preparação** (1 dia)
   - Backup do banco
   - Criação de tabelas temporárias
   - Desenvolvimento do script

2. **Teste** (1 dia)
   - Importação de amostra (100 registros)
   - Validação dos resultados
   - Ajustes necessários

3. **Execução** (1 dia)
   - Importação completa
   - Monitoramento do processo
   - Validação final

4. **Verificação** (1 dia)
   - Auditoria dos dados
   - Relatórios de consistência
   - Documentação do processo

## Riscos e Mitigações

### Riscos Identificados
- **Duplicação de dados**: Verificar NUMERO único
- **Inconsistência de referências**: Validar IDs de responsáveis e salas
- **Perda de dados**: Backup antes da importação
- **Problemas de encoding**: Normalizar caracteres especiais

### Mitigações
- **Transações**: Usar transações para rollback em caso de erro
- **Logs detalhados**: Registrar todas as operações
- **Validação prévia**: Verificar dados antes da inserção
- **Teste incremental**: Importar em lotes pequenos

---

**Data de Criação**: $(date)
**Responsável**: Sistema de Inventário IFMT
**Versão**: 1.0