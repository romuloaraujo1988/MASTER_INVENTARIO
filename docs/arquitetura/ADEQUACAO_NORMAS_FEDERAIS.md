# Adequação às Normas Federais - Sistema Complementar de Inventário IFMT

## 📋 Visão Geral

Este documento descreve as adequações realizadas no **sistema complementar** para conformidade com as normas federais de gestão patrimonial.

### ⚠️ Importante
Este é um **sistema complementar** que auxilia nos processos de inventário. O sistema oficial de gestão patrimonial é o **SIADS** (Sistema Integrado de Administração de Serviços) do governo federal.

**Objetivo:** Facilitar a coleta de dados em campo e preparar informações para exportação ao SIADS.

---

## 📜 Normas Atendidas

### 1. IN SGD/ME nº 1/2019
**Instrução Normativa de Gestão de Patrimônio**

| Requisito | Status | Implementação |
|-----------|--------|---------------|
| Identificação única do bem | ✅ | Campo `numeroPatrimonio` |
| Responsável com CPF | ✅ | Campo `cpfResponsavel` validado |
| Localização física | ✅ | Campos `sala`, `setor`, `codigoUorg` |
| Estado de conservação | ✅ | Códigos 1-5 conforme SIADS |
| Situação do bem | ✅ | Códigos 1-3 (Em uso, Ocioso, Baixado) |
| Termo de responsabilidade | ✅ | Dados do responsável completos |

### 2. Decreto nº 9.373/2018
**Alienação de Bens Móveis**

| Requisito | Status | Implementação |
|-----------|--------|---------------|
| Motivo de baixa | ✅ | Códigos 1-10 conforme decreto |
| Data de baixa | ✅ | Campo `dataBaixa` |
| Processo de baixa | ✅ | Campo `numeroProcessoBaixa` |
| Tipo de destinação | ✅ | LEILAO, DOACAO, INUTILIZACAO, etc |
| Classificação inservível | ✅ | Obsoleto, Ocioso, Antieconômico, Irrecuperável |

### 3. Manual SIADS v6.2.11
**Sistema Integrado de Administração de Serviços**

| Requisito | Status | Implementação |
|-----------|--------|---------------|
| Formato de arquivo | ✅ | Delimitadores ¥ e £ |
| Encoding UTF-8 | ✅ | Configurado |
| Estrutura Header-Detail-Trailer | ✅ | Implementado |
| 28 campos obrigatórios/opcionais | ✅ | Todos mapeados |
| Código UOrg | ✅ | Campo `codigoUorg` |
| Código CATMAT | ✅ | Campo `codigoCatmat` |

### 4. NBC TSP 07
**Norma Brasileira de Contabilidade - Depreciação**

| Requisito | Status | Implementação |
|-----------|--------|---------------|
| Vida útil por categoria | ✅ | Mapeamento configurável |
| Taxa de depreciação | ✅ | Cálculo automático |
| Método de depreciação | ✅ | LINEAR (padrão) |
| Valor residual | ✅ | 10% configurável |
| Valor líquido contábil | ✅ | Calculado automaticamente |

---

## 🏗️ Estrutura Implementada

### Modelo de Dados (SiadsRegistro)

```
┌─────────────────────────────────────────────────────────────┐
│                    IDENTIFICAÇÃO                             │
├─────────────────────────────────────────────────────────────┤
│ numeroPatrimonio*    │ Número único do bem                   │
│ descricao*           │ Descrição do bem                      │
│ especificacao        │ Especificação técnica                 │
│ codigoMaterial*      │ P + número do patrimônio              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    CLASSIFICAÇÃO                             │
├─────────────────────────────────────────────────────────────┤
│ codigoCatmat         │ Código CATMAT oficial                 │
│ classeContabil       │ Categoria do bem                      │
│ elementoDespesa      │ ED conforme SIAFI                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    VALORES                                   │
├─────────────────────────────────────────────────────────────┤
│ valorAquisicao*      │ Valor original de aquisição           │
│ valorDepreciado      │ Depreciação acumulada                 │
│ valorLiquido         │ Valor contábil atual                  │
│ valorResidual        │ Valor residual após vida útil         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    LOCALIZAÇÃO                               │
├─────────────────────────────────────────────────────────────┤
│ orgao                │ Código do órgão (ex: 25000 = MEC)     │
│ unidadeGestora       │ Código da UG                          │
│ codigoUorg*          │ Código da Unidade Organizacional      │
│ setor                │ Nome do setor                         │
│ sala                 │ Nome/número da sala                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    RESPONSÁVEL                               │
├─────────────────────────────────────────────────────────────┤
│ cpfResponsavel       │ CPF (11 dígitos)                      │
│ nomeResponsavel      │ Nome completo                         │
│ matriculaResponsavel │ Matrícula SIAPE                       │
│ cargoResponsavel     │ Cargo do servidor                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    GARANTIA                                  │
├─────────────────────────────────────────────────────────────┤
│ garantidor           │ Nome do garantidor                    │
│ contratoGarantia     │ Número do contrato                    │
│ dataInicioGarantia   │ Data início                           │
│ dataFimGarantia      │ Data fim                              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    BAIXA (Decreto 9.373/2018)                │
├─────────────────────────────────────────────────────────────┤
│ baixado              │ true/false                            │
│ dataBaixa            │ Data da baixa                         │
│ motivoBaixa          │ Código 1-10                           │
│ descricaoMotivoBaixa │ Descrição do motivo                   │
│ numeroProcessoBaixa  │ Número do processo                    │
│ tipoDestinacao       │ LEILAO, DOACAO, etc                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    DEPRECIAÇÃO (NBC TSP 07)                  │
├─────────────────────────────────────────────────────────────┤
│ vidaUtilAnos         │ Vida útil em anos                     │
│ taxaDepreciacao      │ Taxa anual (%)                        │
│ metodoDepreciacao    │ LINEAR, SOMA_DIGITOS, etc             │
└─────────────────────────────────────────────────────────────┘

* Campos obrigatórios
```

---

## 📊 Códigos Oficiais

### Motivos de Baixa (Decreto 9.373/2018)

| Código | Descrição |
|--------|-----------|
| 1 | Inservível por obsolescência |
| 2 | Inservível por ociosidade |
| 3 | Inservível por antieconômico |
| 4 | Inservível por irrecuperável |
| 5 | Furto ou roubo |
| 6 | Sinistro (incêndio, etc) |
| 7 | Doação |
| 8 | Permuta |
| 9 | Venda/Leilão |
| 10 | Outros motivos |

### Estado de Conservação (SIADS)

| Código | Descrição |
|--------|-----------|
| 1 | Ótimo |
| 2 | Bom |
| 3 | Regular |
| 4 | Ruim |
| 5 | Péssimo |

### Situação do Bem (SIADS)

| Código | Descrição |
|--------|-----------|
| 1 | Em uso |
| 2 | Ocioso |
| 3 | Baixado |

### Tipo de Aquisição (SIADS)

| Código | Descrição |
|--------|-----------|
| 1 | Compra |
| 2 | Doação |
| 3 | Cessão |
| 4 | Permuta |
| 5 | Fabricação própria |
| 6 | Outros |

---

## 🔧 Configuração

### Arquivo siads.properties

```properties
# Identificação institucional
siads.codigo.orgao=25000
siads.codigo.ug=158497
siads.cnpj=10784782000127
siads.nome.instituicao=IFMT

# Responsável
siads.cpf.responsavel=00000000000
siads.nome.responsavel=Nome do Responsável

# Depreciação (NBC TSP 07)
siads.depreciacao.metodo=LINEAR
siads.depreciacao.valor.residual.percentual=10

# Validação
siads.validacao.cpf.obrigatorio=true
siads.validacao.valor.obrigatorio=true
```

---

## ✅ Validações Implementadas

### Campos Obrigatórios
- ✅ Número do patrimônio
- ✅ Descrição
- ✅ Código UOrg
- ✅ Data de aquisição
- ✅ Valor de aquisição
- ✅ Estado de conservação
- ✅ Situação do bem

### Validação de CPF
- ✅ Formato (11 dígitos)
- ✅ Dígitos verificadores
- ✅ CPFs inválidos (todos iguais)

### Validação de Baixa
- ✅ Data de baixa obrigatória se baixado
- ✅ Motivo de baixa obrigatório se baixado
- ✅ Código do motivo (1-10)

### Validação de Depreciação
- ✅ Valor depreciado ≤ valor aquisição
- ✅ Cálculo de valor líquido

---

## 📁 Arquivos Modificados/Criados

### Novos Arquivos
- `SiadsValidationService.java` - Serviço de validação
- `ADEQUACAO_NORMAS_FEDERAIS.md` - Esta documentação
- `siads.properties.example` - Exemplo de configuração

### Arquivos Atualizados
- `SiadsRegistro.java` - Novos campos (garantia, baixa, depreciação)
- `SiadsConverterService.java` - Mapeamento de novos campos
- `SiadsLayoutFormatter.java` - Formatação de novos campos
- `SiadsConfig.java` - Novas configurações

---

## 🚀 Como Usar

### 1. Configurar
```bash
# Copiar arquivo de exemplo
cp siads.properties.example siads.properties

# Editar com dados da instituição
notepad siads.properties
```

### 2. Validar Configuração
```java
SiadsValidationService validator = new SiadsValidationService();
ValidationResult result = validator.validarConfiguracao();
System.out.println(result.getResumo());
```

### 3. Validar Dados
```java
List<SiadsRegistro> registros = converterService.converterPatrimonios(patrimonios);
ValidationResult result = validator.validarRegistros(registros);

if (result.isValid()) {
    // Exportar
    exportService.exportar(registros, diretorio);
} else {
    // Mostrar erros
    System.out.println(result.getResumo());
}
```

### 4. Exportar
```java
SiadsIntegrationService siads = new SiadsIntegrationService();
File arquivo = siads.gerarArquivoSiads(diretorio);
```

---

## 📞 Referências

- [IN SGD/ME nº 1/2019](https://www.gov.br/economia/pt-br/assuntos/gestao/patrimonio-da-uniao)
- [Decreto nº 9.373/2018](http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/decreto/d9373.htm)
- [Manual SIADS](https://www.gov.br/economia/pt-br/assuntos/gestao/siads)
- [NBC TSP 07](https://www.cfc.org.br/tecnica/normas-brasileiras-de-contabilidade/)
- [CATMAT/CATSER](https://www.gov.br/compras/pt-br/sistemas/catmat-catser)
- [SIORG](https://siorg.gov.br)

---

**Versão:** 2.0.0  
**Data:** 07/12/2025  
**Status:** ✅ Implementado
