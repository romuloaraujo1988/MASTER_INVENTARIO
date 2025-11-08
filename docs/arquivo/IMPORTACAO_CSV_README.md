# Importação de Dados CSV do SUAP

## Visão Geral

Este sistema permite importar dados de patrimônio diretamente de arquivos CSV exportados do SUAP (Sistema Unificado de Administração Pública) para o banco de dados do sistema de inventário.

## Arquivos Criados

### 1. ImportacaoCSV.java
- **Localização**: `src/main/java/com/inventario/util/ImportacaoCSV.java`
- **Função**: Classe principal responsável pela importação de dados CSV
- **Características**:
  - Processa arquivos CSV com estrutura do SUAP
  - Cria automaticamente responsáveis, setores e salas quando necessário
  - Utiliza cache para otimizar performance
  - Gera relatório detalhado da importação
  - Trata erros e continua processamento

### 2. TesteImportacaoCSV.java
- **Localização**: `src/main/java/com/inventario/util/TesteImportacaoCSV.java`
- **Função**: Classe de teste para demonstrar o uso da ImportacaoCSV
- **Características**:
  - Exemplo de uso completo
  - Medição de tempo de execução
  - Exibição de relatório detalhado

## Modificações nos DAOs

Para suportar a importação, os seguintes métodos foram adicionados/modificados:

### ResponsavelDAO.java
- ✅ **Adicionado**: `buscarPorNome(String nome)` - busca responsável por nome específico
- ✅ **Modificado**: `inserirResponsavel()` - agora retorna o ID do responsável inserido

### SalaDAO.java
- ✅ **Adicionado**: `buscarPorFiltro()` - busca salas com múltiplos parâmetros
- ✅ **Modificado**: `inserirSala()` - agora retorna o ID da sala inserida

### SetorDAO.java
- ✅ **Adicionado**: `buscarSetoresPorFiltro()` - sobrecarga para múltiplos parâmetros
- ✅ **Modificado**: `inserirSetor()` - agora retorna o ID do setor inserido

## Estrutura do Arquivo CSV

O arquivo CSV deve conter as seguintes colunas (baseado no arquivo `patrimonio ifmt 26.06.2025.csv`):

| Coluna | Descrição | Obrigatório |
|--------|-----------|-------------|
| NUMERO | Número do patrimônio | ✅ Sim |
| DESCRICAO | Descrição do item | ✅ Sim |
| SETOR DO RESPONSÁVEL | Nome do setor | ✅ Sim |
| RESPONSÁVEL | Nome do responsável | ✅ Sim |
| SALA | Identificação da sala | ❌ Não |
| VALOR | Valor monetário | ❌ Não |
| DATA DE AQUISIÇÃO | Data de aquisição | ❌ Não |
| ESTADO DE CONSERVAÇÃO | Estado do item | ❌ Não |

## Como Usar

### 1. Preparação do Arquivo
1. Exporte os dados do SUAP em formato CSV
2. Coloque o arquivo na raiz do projeto
3. Certifique-se de que o arquivo está codificado em UTF-8

### 2. Execução da Importação

```java
// Exemplo básico
ImportacaoCSV importacao = new ImportacaoCSV();
ImportacaoCSV.RelatorioImportacao relatorio = importacao.importarPatrimonios("arquivo.csv");
System.out.println(relatorio.toString());
```

### 3. Executar Teste

```bash
# Compilar o projeto
mvn compile

# Executar o teste
mvn exec:java -Dexec.mainClass="com.inventario.util.TesteImportacaoCSV"
```

## Funcionalidades

### ✅ Criação Automática de Entidades
- **Responsáveis**: Criados automaticamente se não existirem
- **Setores**: Criados automaticamente se não existirem
- **Salas**: Criadas automaticamente se não existirem

### ✅ Cache de Performance
- Cache de responsáveis por nome
- Cache de setores por nome
- Cache de salas por identificação
- Reduz consultas ao banco de dados

### ✅ Tratamento de Erros
- Continua processamento mesmo com erros em linhas específicas
- Log detalhado de erros
- Relatório final com estatísticas

### ✅ Relatório Detalhado
- Total de linhas processadas
- Itens inseridos vs atualizados
- Número de erros
- Tempo de execução

## Exemplo de Saída

```
=== RELATÓRIO DE IMPORTAÇÃO ===
Linhas processadas: 1500
Itens inseridos: 1200
Itens atualizados: 250
Erros: 50
Tempo de execução: 45.2 segundos
✅ IMPORTAÇÃO CONCLUÍDA COM SUCESSO!
```

## Troubleshooting

### Problemas Comuns

1. **Erro de codificação**: Certifique-se de que o CSV está em UTF-8
2. **Colunas faltando**: Verifique se todas as colunas obrigatórias estão presentes
3. **Formato de data**: As datas devem estar no formato dd/MM/yyyy
4. **Valores monetários**: Use vírgula como separador decimal

### Logs de Erro

Os erros são registrados no console com detalhes:
- Número da linha com erro
- Descrição do problema
- Dados da linha que causou o erro

## Próximos Passos

1. Testar com o arquivo `patrimonio ifmt 26.06.2025.csv`
2. Ajustar mapeamento de colunas se necessário
3. Implementar validações adicionais
4. Criar interface web para upload de arquivos

---

**Nota**: Esta implementação substitui a classe `ImportacaoSUAP` original, oferecendo melhor performance e facilidade de uso com arquivos CSV.