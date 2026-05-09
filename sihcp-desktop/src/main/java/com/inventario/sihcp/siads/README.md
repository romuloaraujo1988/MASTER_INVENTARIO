# Package SIADS - Integração com Sistema Federal

Este package contém toda a funcionalidade necessária para integração com o SIADS (Sistema Integrado de Administração de Serviços) do governo federal.

## Estrutura

```
com.inventario.siads/
├── model/
│   └── SiadsRegistro.java          # Modelo de dados SIADS
├── service/
│   ├── SiadsConverterService.java  # Conversão de dados internos para SIADS
│   ├── SiadsExportService.java     # Exportação de arquivos
│   └── SiadsIntegrationService.java # Orquestração da integração
├── util/
│   └── SiadsLayoutFormatter.java   # Formatação do layout do arquivo
├── view/
│   └── SiadsExportDialog.java      # Interface gráfica para exportação
└── dao/
    └── SiadsPatrimonioDAO.java     # Consultas específicas para SIADS
```

## Funcionalidades

### 1. Conversão de Dados
O `SiadsConverterService` converte os dados do sistema interno (Patrimonio) para o formato SIADS (SiadsRegistro), mapeando todos os campos necessários.

### 2. Formatação de Arquivo
O `SiadsLayoutFormatter` formata os dados no layout exigido pelo SIADS:
- Formato delimitado por pipe (|)
- Cabeçalho com nomes dos campos
- Registros de detalhe (tipo D)
- Rodapé com totalizadores (tipo T)

### 3. Exportação
O `SiadsExportService` gera o arquivo físico no formato texto, com encoding UTF-8.

### 4. Validação
O sistema valida os dados antes da exportação, verificando:
- Campos obrigatórios preenchidos
- Formato de CPF
- Valores monetários válidos
- Datas válidas

### 5. Interface Gráfica
O `SiadsExportDialog` fornece uma interface amigável para:
- Visualizar estatísticas dos dados
- Validar dados antes da exportação
- Selecionar diretório de destino
- Acompanhar progresso da exportação

## Como Usar

### Integração no Menu Principal

Adicione no menu principal do sistema:

```java
JMenuItem menuSiads = new JMenuItem("Exportar SIADS");
menuSiads.addActionListener(e -> {
    SiadsExportDialog dialog = new SiadsExportDialog(this);
    dialog.setVisible(true);
});
menuRelatorios.add(menuSiads);
```

### Uso Programático

```java
// Criar serviço
SiadsIntegrationService siadsService = new SiadsIntegrationService();

// Validar dados
List<String> erros = siadsService.validarDadosParaExportacao();
if (!erros.isEmpty()) {
    // Tratar erros
}

// Exportar arquivo
File diretorio = new File("C:/exportacao");
File arquivo = siadsService.gerarArquivoSiads(diretorio);
System.out.println("Arquivo gerado: " + arquivo.getAbsolutePath());
```

### Exportação por Inventário

```java
// Exportar apenas patrimônios de um inventário específico
Long idInventario = 1L;
File arquivo = siadsService.gerarArquivoSiadsPorInventario(diretorio, idInventario);
```

## Formato do Arquivo

O formato segue o padrão oficial SIADS conforme Guia v6.2.11:

### Delimitadores
- **Campo**: ¥ (símbolo do iene japonês)
- **Linha**: £ (símbolo da libra esterlina)
- **Encoding**: UTF-8

### Cabeçalho (Header)
```
H¥PE¥1¥25000¥00001¥02146445459¥00001¥£
```
Campos: Tipo¥TipoArquivo¥Versão¥CódigoÓrgão¥CódigoUG¥CPFResponsável¥CódigoUGDestino¥£

### Registro de Detalhe (Detail)
```
D¥P101479014¥COMPUTADOR DESKTOP¥254602152¥SALA 101¥1790001¥1¥2¥1¥15052024¥550000¥COMPRA¥DELL OPTIPLEX 7090¥¥¥12345¥¥¥¥¥¥¥¥02146445459¥JOAO SILVA¥FALSE¥¥¥¥£
```

### Rodapé (Trailer)
```
T¥08112024143025¥1500¥75000000¥FIM¥£
```
Campos: Tipo¥DataHora¥QtdRegistros¥ValorTotal¥FIM¥£

## Campos do Registro SIADS

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| NUMERO_PATRIMONIO | String | Sim | Número do patrimônio |
| DESCRICAO | String | Sim | Descrição do bem |
| ESPECIFICACAO | String | Não | Especificação técnica |
| CODIGO_MATERIAL | String | Não | Código do material |
| GRUPO_MATERIAL | String | Não | Grupo de classificação |
| CLASSE_CONTABIL | String | Não | Classe contábil |
| VALOR_AQUISICAO | Decimal | Sim | Valor de aquisição |
| VALOR_DEPRECIADO | Decimal | Não | Valor depreciado |
| VALOR_RESIDUAL | Decimal | Não | Valor residual |
| DATA_AQUISICAO | Data | Sim | Data de aquisição |
| DATA_INCORPORACAO | Data | Não | Data de incorporação |
| DATA_INVENTARIO | Data | Não | Data do inventário |
| ORGAO | String | Não | Órgão responsável |
| UNIDADE_GESTORA | String | Não | Unidade gestora |
| SETOR | String | Não | Setor |
| SALA | String | Não | Sala/Local |
| CPF_RESPONSAVEL | String | Sim | CPF do responsável |
| NOME_RESPONSAVEL | String | Sim | Nome do responsável |
| MATRICULA_RESPONSAVEL | String | Não | Matrícula do responsável |
| SITUACAO_BEM | String | Sim | Situação do bem |
| ESTADO_CONSERVACAO | String | Não | Estado de conservação |
| FORMA_AQUISICAO | String | Não | Forma de aquisição |
| NUMERO_NOTA_FISCAL | String | Não | Número da nota fiscal |
| FORNECEDOR | String | Não | Fornecedor |

## Mapeamento de Dados

O sistema mapeia automaticamente os dados internos para o formato SIADS:

- `Patrimonio.numeroPatrimonio` → `SiadsRegistro.numeroPatrimonio`
- `Patrimonio.descricao` → `SiadsRegistro.descricao`
- `Patrimonio.valorAquisicao` → `SiadsRegistro.valorAquisicao`
- `Patrimonio.responsavel.cpf` → `SiadsRegistro.cpfResponsavel`
- `Patrimonio.sala.nome` → `SiadsRegistro.sala`
- `Patrimonio.sala.setor.nome` → `SiadsRegistro.setor`

## Validações Implementadas

1. **Campos Obrigatórios**
   - Número do patrimônio
   - Descrição
   - Valor de aquisição
   - Responsável

2. **Formato de Dados**
   - CPF: apenas números
   - Valores: formato decimal brasileiro (vírgula)
   - Datas: formato dd/MM/yyyy

3. **Integridade**
   - Relacionamentos válidos (sala, responsável)
   - Valores positivos

## Tratamento de Erros

O sistema trata os seguintes cenários:
- Nenhum patrimônio encontrado
- Diretório de destino inválido
- Erro de escrita no arquivo
- Dados incompletos ou inválidos

## Logs

Todos os erros são registrados no console e podem ser visualizados na interface gráfica.

## Extensibilidade

Para adicionar novos campos ao arquivo SIADS:

1. Adicione o campo em `SiadsRegistro.java`
2. Atualize o mapeamento em `SiadsConverterService.java`
3. Atualize a formatação em `SiadsLayoutFormatter.java`

## Notas Importantes

- O arquivo gerado usa encoding UTF-8
- O delimitador padrão é pipe (|)
- Caracteres especiais são removidos automaticamente
- O nome do arquivo segue o padrão: `SIADS_AAAAMMDD_HHMMSS.txt`

## Referências

- Guia de Orientações Gerais para Geração dos Arquivos de Implantação v6.2.11
- Documentação SIADS (disponível em `docs/`)
