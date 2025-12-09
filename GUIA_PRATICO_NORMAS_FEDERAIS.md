# Guia Prático - Sistema Complementar de Inventário

## 🚀 Como Usar o Sistema Complementar

### ⚠️ Escopo
Este guia descreve como usar o **sistema complementar** para auxiliar nos inventários. Os dados coletados aqui serão exportados para o **SIADS** (sistema oficial).

---

## 1️⃣ Configuração Inicial

### Passo 1: Copiar arquivo de configuração
```bash
cp siads.properties.example siads.properties
```

### Passo 2: Editar configuração
```properties
# Identificação institucional
siads.codigo.orgao=25000          # MEC
siads.codigo.ug=158497            # IFMT
siads.cnpj=10784782000127         # CNPJ IFMT
siads.nome.instituicao=IFMT

# Responsável
siads.cpf.responsavel=00000000000 # CPF do responsável
siads.nome.responsavel=Nome       # Nome completo
siads.cargo.responsavel=Cargo     # Cargo
siads.matricula.responsavel=0000  # Matrícula SIAPE
```

### Passo 3: Validar configuração
```java
SiadsValidationService validator = new SiadsValidationService();
ValidationResult result = validator.validarConfiguracao();
System.out.println(result.getResumo());
```

---

## 2️⃣ Exportação SIADS

### Fluxo Completo

```java
// 1. Carregar patrimônios
List<Patrimonio> patrimonios = patrimonioDAO.buscarTodos();

// 2. Converter para SIADS
SiadsConverterService converter = new SiadsConverterService();
List<SiadsRegistro> registros = converter.converterPatrimonios(patrimonios);

// 3. Validar dados
SiadsValidationService validator = new SiadsValidationService();
ValidationResult result = validator.validarRegistros(registros);

if (!result.isValid()) {
    // Mostrar erros
    System.out.println(result.getResumo());
    return;
}

// 4. Exportar
SiadsIntegrationService siads = new SiadsIntegrationService();
File arquivo = siads.gerarArquivoSiads(new File("exportacao"));
System.out.println("Arquivo gerado: " + arquivo.getAbsolutePath());
```

---

## 3️⃣ Campos Obrigatórios

### Antes de Exportar, Verificar:

| Campo | Tipo | Exemplo | Validação |
|-------|------|---------|-----------|
| numeroPatrimonio | String | "12345" | Obrigatório |
| descricao | String | "Computador" | Obrigatório |
| codigoUorg | String | "1584970001" | Obrigatório |
| dataAquisicao | LocalDate | 2024-01-15 | Obrigatório |
| valorAquisicao | BigDecimal | 5000.00 | > 0 |
| estadoConservacao | String | "2" | 1-5 |
| situacaoBem | String | "1" | 1-3 |
| cpfResponsavel | String | "12345678901" | 11 dígitos |

---

## 4️⃣ Códigos Oficiais

### Estados de Conservação
```
1 = Ótimo
2 = Bom
3 = Regular
4 = Ruim
5 = Péssimo
```

### Situação do Bem
```
1 = Em uso
2 = Ocioso
3 = Baixado
```

### Motivos de Baixa (se aplicável)
```
1 = Inservível por obsolescência
2 = Inservível por ociosidade
3 = Inservível por antieconômico
4 = Inservível por irrecuperável
5 = Furto ou roubo
6 = Sinistro
7 = Doação
8 = Permuta
9 = Venda/Leilão
10 = Outros motivos
```

---

## 5️⃣ Depreciação (NBC TSP 07)

### Vida Útil Padrão por Categoria

| Categoria | Anos | Taxa Anual |
|-----------|------|-----------|
| Informática | 5 | 20% |
| Mobiliário | 10 | 10% |
| Eletrodoméstico | 10 | 10% |
| Veículo | 5 | 20% |
| Equipamento Lab | 10 | 10% |

### Cálculo Automático

```java
// Valor líquido = Valor aquisição - Depreciação acumulada
BigDecimal valorLiquido = valorAquisicao.subtract(valorDepreciado);

// Taxa anual = 100 / vida útil
BigDecimal taxaAnual = new BigDecimal(100).divide(
    new BigDecimal(vidaUtilAnos), 2, RoundingMode.HALF_UP
);
```

---

## 6️⃣ Validação de Dados

### Exemplo de Validação Completa

```java
SiadsValidationService validator = new SiadsValidationService();

// Validar um registro
SiadsRegistro registro = new SiadsRegistro();
registro.setNumeroPatrimonio("12345");
registro.setDescricao("Computador");
registro.setCodigoUorg("1584970001");
registro.setDataAquisicao(LocalDate.of(2024, 1, 15));
registro.setValorAquisicao(new BigDecimal("5000.00"));
registro.setEstadoConservacao("2");
registro.setSituacaoBem("1");
registro.setCpfResponsavel("12345678901");

ValidationResult result = validator.validarRegistro(registro);

if (result.isValid()) {
    System.out.println("✅ Registro válido");
} else {
    System.out.println("❌ Erros encontrados:");
    for (String error : result.getErrors()) {
        System.out.println("  - " + error);
    }
}

// Mostrar avisos
if (!result.getWarnings().isEmpty()) {
    System.out.println("⚠️ Avisos:");
    for (String warning : result.getWarnings()) {
        System.out.println("  - " + warning);
    }
}
```

---

## 7️⃣ Baixa Patrimonial (Decreto 9.373/2018)

### Registrar Baixa

```java
SiadsRegistro registro = new SiadsRegistro();
// ... preencher dados básicos ...

// Marcar como baixado
registro.setBaixado(true);
registro.setDataBaixa(LocalDate.now());
registro.setMotivoBaixa("1"); // Inservível por obsolescência
registro.setDescricaoMotivoBaixa("Equipamento obsoleto");
registro.setNumeroProcessoBaixa("2024/12345");
registro.setTipoDestinacao("INUTILIZACAO");

// Validar
ValidationResult result = validator.validarRegistro(registro);
if (result.isValid()) {
    // Exportar com dados de baixa
    siads.gerarArquivoSiads(diretorio);
}
```

---

## 8️⃣ Garantia

### Registrar Garantia

```java
SiadsRegistro registro = new SiadsRegistro();
// ... preencher dados básicos ...

// Adicionar informações de garantia
registro.setGarantidor("Fabricante XYZ");
registro.setContratoGarantia("CONTRATO-2024-001");
registro.setDataInicioGarantia(LocalDate.of(2024, 1, 15));
registro.setDataFimGarantia(LocalDate.of(2025, 1, 15));

// Validar e exportar
ValidationResult result = validator.validarRegistro(registro);
```

---

## 9️⃣ Mapeamento CATMAT

### Configurar Códigos CATMAT

```java
SiadsConverterService converter = new SiadsConverterService();

// Configurar CATMAT por categoria
converter.configurarCatmat("INFORMATICA", "254602152");
converter.configurarCatmat("MOBILIARIO", "234567890");
converter.configurarCatmat("VEICULO", "456789012");

// Configurar Elemento de Despesa (SIAFI)
converter.configurarElementoDespesa("INFORMATICA", "449052");

// Configurar vida útil
converter.configurarVidaUtil("INFORMATICA", 5);
converter.configurarVidaUtil("MOBILIARIO", 10);
```

---

## 🔟 Troubleshooting

### Problema: CPF Inválido
```
❌ Erro: CPF do responsável pode estar inválido (dígitos verificadores)
```
**Solução:** Verificar dígitos verificadores do CPF

### Problema: Valor Depreciado > Valor Aquisição
```
❌ Aviso: Valor depreciado é maior que valor de aquisição
```
**Solução:** Revisar cálculo de depreciação

### Problema: Código UOrg Não Encontrado
```
❌ Erro: Código da Unidade Organizacional (UOrg) é obrigatório
```
**Solução:** Consultar SIORG para código correto

### Problema: Arquivo Não Importa no SIADS
```
❌ Erro ao importar no SIADS
```
**Solução:** 
1. Verificar encoding UTF-8
2. Validar delimitadores (¥ e £)
3. Verificar formato de datas (ddMMyyyy)
4. Validar valores em centavos

---

## 📞 Referências Rápidas

### Órgãos Federais
- **MEC:** 25000
- **IFMT:** 158497 (exemplo)

### Consultar Códigos
- **CATMAT:** https://www.gov.br/compras/pt-br/sistemas/catmat-catser
- **SIORG:** https://siorg.gov.br
- **SIADS:** https://www.gov.br/economia/pt-br/assuntos/gestao/siads

### Normas
- **IN SGD/ME nº 1/2019:** Gestão de Patrimônio
- **Decreto nº 9.373/2018:** Alienação de Bens
- **NBC TSP 07:** Depreciação

---

## 🎯 Checklist de Exportação

Antes de exportar, verificar:

- [ ] Configuração SIADS preenchida
- [ ] Todos os patrimônios têm número
- [ ] Todos os patrimônios têm descrição
- [ ] Todos os patrimônios têm responsável com CPF
- [ ] Todos os patrimônios têm localização (UOrg)
- [ ] Valores de aquisição preenchidos
- [ ] Estados de conservação válidos (1-5)
- [ ] Situação do bem válida (1-3)
- [ ] Se baixado: motivo e data preenchidos
- [ ] Validação passou sem erros
- [ ] Arquivo gerado com sucesso

---

## 📊 Exemplo Completo

```java
// Importações
import com.inventario.siads.service.*;
import com.inventario.siads.config.SiadsConfig;
import com.inventario.siads.model.SiadsRegistro;
import java.io.File;
import java.util.List;

public class ExportacaoSIADS {
    
    public static void main(String[] args) throws Exception {
        // 1. Configurar
        SiadsConfig config = new SiadsConfig();
        config.setCodigoOrgao("25000");
        config.setCodigoUG("158497");
        config.setCpfResponsavel("12345678901");
        config.setNomeInstituicao("IFMT");
        config.salvarConfiguracao();
        
        // 2. Validar configuração
        SiadsValidationService validator = new SiadsValidationService(config);
        ValidationResult configResult = validator.validarConfiguracao();
        if (!configResult.isValid()) {
            System.out.println(configResult.getResumo());
            return;
        }
        
        // 3. Carregar patrimônios
        List<Patrimonio> patrimonios = carregarPatrimonios();
        
        // 4. Converter
        SiadsConverterService converter = new SiadsConverterService(config);
        List<SiadsRegistro> registros = converter.converterPatrimonios(patrimonios);
        
        // 5. Validar dados
        ValidationResult dataResult = validator.validarRegistros(registros);
        if (!dataResult.isValid()) {
            System.out.println(dataResult.getResumo());
            return;
        }
        
        // 6. Exportar
        SiadsIntegrationService siads = new SiadsIntegrationService();
        File arquivo = siads.gerarArquivoSiads(new File("exportacao"));
        
        System.out.println("✅ Exportação concluída!");
        System.out.println("Arquivo: " + arquivo.getAbsolutePath());
    }
    
    private static List<Patrimonio> carregarPatrimonios() {
        // Implementar carregamento de patrimônios
        return new ArrayList<>();
    }
}
```

---

**Versão:** 1.0.0  
**Data:** 07/12/2025  
**Status:** ✅ Pronto para Uso
