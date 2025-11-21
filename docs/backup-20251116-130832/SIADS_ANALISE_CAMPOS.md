# Análise de Campos SIADS - Material Permanente

## Campos Exigidos pelo SIADS (28 campos)

Baseado no exemplo oficial da documentação:

| # | Campo | Exemplo | Obrigatório | Status | Mapeamento Atual |
|---|-------|---------|-------------|--------|------------------|
| 1 | Tipo Registro | D | ✅ Sim | ✅ OK | Fixo "D" |
| 2 | Código Material | P101479014 | ✅ Sim | ✅ OK | "P" + numero |
| 3 | Descrição | MATERIAL PERMANENTE01 | ✅ Sim | ✅ OK | descricao |
| 4 | Código CATMAT | 254602152 | ❌ Não | ⚠️ PARCIAL | categoria |
| 5 | Endereço/Local | ENDERECO | ❌ Não | ✅ OK | nomeSala |
| 6 | Código UOrg | 1790001 | ✅ Sim | ⚠️ PARCIAL | idSala (deveria ser código UOrg) |
| 7 | Tipo Aquisição | 1 | ✅ Sim | ✅ OK | Fixo "1" (Compra) |
| 8 | Estado Conservação | 2 | ✅ Sim | ✅ OK | Mapeado (1-5) |
| 9 | Situação | 3 | ✅ Sim | ✅ OK | Mapeado (1-3) |
| 10 | Data Aquisição | 11052017 | ✅ Sim | ✅ OK | dataEntrada (ddMMyyyy) |
| 11 | Valor Aquisição | 10000 | ✅ Sim | ✅ OK | valorAquisicao (centavos) |
| 12 | Forma Aquisição | MERCADO XPTO | ❌ Não | ⚠️ PARCIAL | Fixo "COMPRA" |
| 13 | Especificação | ESPECIFICACAO | ❌ Não | ✅ OK | rotulos |
| 14 | Data Depreciação | 11052018 | ❌ Não | ✅ OK | dataIncorporacao |
| 15 | Valor Depreciado | 9000000 | ❌ Não | ✅ OK | valorDepreciado (centavos) |
| 16 | Número Patrimônio | 1234567891 | ✅ Sim | ✅ OK | numero |
| 17 | Marca | MARCA | ❌ Não | ✅ OK | marca |
| 18 | Modelo | MODELO | ❌ Não | ❌ FALTA | modelo (não mapeado) |
| 19 | Fabricante | FABRICANTE | ❌ Não | ❌ FALTA | Vazio |
| 20 | Garantidor | GARANTIDOR | ❌ Não | ❌ FALTA | Vazio |
| 21 | Contrato | CONTRATO | ❌ Não | ❌ FALTA | Vazio |
| 22 | Data Início Garantia | 11052017 | ❌ Não | ❌ FALTA | Vazio |
| 23 | Data Fim Garantia | 11052018 | ❌ Não | ❌ FALTA | Vazio |
| 24 | CPF Responsável | 02146445459 | ❌ Não | ✅ OK | cpfResponsavel |
| 25 | Nome Responsável | ANTONIO | ❌ Não | ✅ OK | nomeResponsavel |
| 26 | Baixado | FALSE | ❌ Não | ✅ OK | Fixo "FALSE" |
| 27 | Data Baixa | 01012019 | ❌ Não | ❌ FALTA | Vazio |
| 28 | Motivo Baixa | 999 | ❌ Não | ❌ FALTA | Vazio |
| 29 | Número Processo Baixa | 12 | ❌ Não | ❌ FALTA | Vazio |

## Resumo da Análise

### ✅ Campos Obrigatórios (9 campos)
- ✅ Tipo Registro
- ✅ Código Material
- ✅ Descrição
- ⚠️ Código UOrg (usando idSala, deveria ser código da unidade organizacional)
- ✅ Tipo Aquisição
- ✅ Estado Conservação
- ✅ Situação
- ✅ Data Aquisição
- ✅ Valor Aquisição
- ✅ Número Patrimônio

**Status**: 9/9 obrigatórios preenchidos (1 com ressalva)

### ⚠️ Campos Opcionais Importantes (não preenchidos)
- ❌ Modelo (campo 18) - Temos no banco mas não está mapeado
- ❌ Fabricante (campo 19)
- ❌ Garantidor (campo 20)
- ❌ Contrato (campo 21)
- ❌ Datas de Garantia (campos 22-23)
- ❌ Dados de Baixa (campos 27-29)

### ⚠️ Campos com Mapeamento Parcial
- **Código UOrg** (campo 6): Estamos usando `idSala`, mas deveria ser o código da Unidade Organizacional (UOrg) cadastrada no SIADS
- **Código CATMAT** (campo 4): Estamos usando `categoria`, mas deveria ser o código oficial do Catálogo de Materiais do governo
- **Forma Aquisição** (campo 12): Fixo "COMPRA", mas poderia ser mais específico

## Recomendações

### 🔴 Crítico
1. **Código UOrg**: Criar mapeamento entre salas/setores e códigos UOrg do SIADS
   - Solução: Adicionar campo `codigoUOrg` na tabela SALA ou SETOR
   - Alternativa: Criar tabela de mapeamento

### 🟡 Importante
2. **Modelo**: Adicionar mapeamento do campo `modelo` que já existe no banco
3. **Código CATMAT**: Permitir configuração do código CATMAT por categoria
4. **Forma Aquisição**: Mapear de forma mais específica (COMPRA, DOAÇÃO, CESSÃO, etc.)

### 🟢 Opcional (Melhorias Futuras)
5. Adicionar campos de garantia no banco de dados
6. Adicionar campos de fabricante/garantidor
7. Implementar controle de baixa de patrimônios com motivo e processo

## Ações Imediatas Sugeridas

### 1. Corrigir Modelo (Fácil)
```java
// Em SiadsConverterService.java
registro.setGrupoMaterial(patrimonio.getMarca());
// Adicionar:
// registro.setModelo(patrimonio.getModelo()); // Precisa adicionar campo no SiadsRegistro
```

### 2. Adicionar Campo Modelo no SiadsRegistro
```java
private String modelo;
```

### 3. Criar Configuração de UOrg
Adicionar arquivo de configuração para mapear setores → códigos UOrg:
```properties
siads.uorg.setor.1=1790001
siads.uorg.setor.2=1790002
```

## Conclusão

### Status Geral: ✅ FUNCIONAL mas ⚠️ INCOMPLETO

O sistema atende **TODOS os campos obrigatórios** e está funcional para importação básica no SIADS.

Porém, alguns campos opcionais importantes não estão sendo preenchidos, o que pode:
- ❌ Reduzir a qualidade dos dados no SIADS
- ❌ Dificultar relatórios e consultas
- ✅ Não impedir a importação (campos são opcionais)

**Recomendação**: Sistema pode ser usado imediatamente, mas sugere-se implementar as melhorias para dados mais completos.
