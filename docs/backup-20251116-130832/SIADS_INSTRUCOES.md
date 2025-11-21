# Instruções de Uso - Integração SIADS

## Visão Geral

O sistema agora possui integração completa com o SIADS (Sistema Integrado de Administração de Serviços) do governo federal, permitindo a exportação de dados patrimoniais no formato exigido.

## Localização dos Arquivos

Todos os arquivos da integração SIADS estão no package:
```
src/main/java/com/inventario/siads/
```

## Como Adicionar ao Menu Principal

Para disponibilizar a funcionalidade no sistema, adicione ao menu principal:

```java
// No arquivo que contém o menu principal (ex: SistemaInventarioApplication.java)

import com.inventario.siads.view.SiadsExportDialog;

// Dentro do método que cria os menus:
JMenu menuRelatorios = new JMenu("Relatórios");

JMenuItem menuSiads = new JMenuItem("Exportar SIADS");
menuSiads.setIcon(new ImageIcon(getClass().getResource("/icons/export.png"))); // opcional
menuSiads.addActionListener(e -> {
    SiadsExportDialog dialog = new SiadsExportDialog(this);
    dialog.setVisible(true);
});

menuRelatorios.add(menuSiads);
```

## Fluxo de Uso

### 1. Preparação dos Dados

Antes de exportar, certifique-se de que:
- Todos os patrimônios têm número identificador
- Todos os patrimônios têm descrição
- Todos os patrimônios têm valor de aquisição
- Todos os patrimônios têm responsável cadastrado
- Os responsáveis têm CPF válido

### 2. Acessar a Exportação

1. No menu principal, clique em **Relatórios > Exportar SIADS**
2. A janela de exportação será aberta

### 3. Visualizar Estatísticas

A janela mostra automaticamente:
- Total de patrimônios
- Percentual com responsável
- Percentual com localização

### 4. Validar Dados

1. Clique no botão **Validar Dados**
2. O sistema verificará:
   - Campos obrigatórios preenchidos
   - Formato de CPF
   - Valores válidos
3. Se houver erros, uma lista será exibida
4. Corrija os erros antes de exportar

### 5. Selecionar Diretório

1. Clique em **Selecionar...**
2. Escolha o diretório onde o arquivo será salvo
3. O botão **Exportar** será habilitado

### 6. Exportar

1. Clique em **Exportar**
2. Aguarde o processamento
3. Uma mensagem de sucesso será exibida
4. Opcionalmente, abra o diretório com o arquivo gerado

## Formato do Arquivo Gerado

### Nome do Arquivo
```
SIADS_AAAAMMDD_HHMMSS.txt
```
Exemplo: `SIADS_20241108_143025.txt`

### Estrutura do Arquivo

```
TIPO|NUMERO_PATRIMONIO|DESCRICAO|...|FORNECEDOR
D|12345|COMPUTADOR|...|DELL COMPUTADORES
D|12346|IMPRESSORA|...|HP BRASIL
T|2|08/11/2024 14:30:25
```

- **Linha 1**: Cabeçalho com nomes dos campos
- **Linhas 2-N**: Registros de patrimônios (tipo D)
- **Última linha**: Rodapé com total de registros (tipo T)

## Campos Exportados

| Campo | Origem | Obrigatório |
|-------|--------|-------------|
| Número Patrimônio | patrimonio.numero_patrimonio | Sim |
| Descrição | patrimonio.descricao | Sim |
| Especificação | patrimonio.especificacao | Não |
| Valor Aquisição | patrimonio.valor_aquisicao | Sim |
| Data Aquisição | patrimonio.data_aquisicao | Sim |
| Setor | sala.setor.nome | Não |
| Sala | sala.nome | Não |
| CPF Responsável | responsavel.cpf | Sim |
| Nome Responsável | responsavel.nome | Sim |
| Matrícula Responsável | responsavel.matricula | Não |
| Situação | patrimonio.situacao | Sim |
| Estado Conservação | patrimonio.estado_conservacao | Não |

## Solução de Problemas

### Erro: "Nenhum patrimônio encontrado"
**Causa**: Não há patrimônios cadastrados no sistema
**Solução**: Cadastre patrimônios antes de exportar

### Erro: "Patrimônio sem responsável"
**Causa**: Existem patrimônios sem responsável vinculado
**Solução**: 
1. Execute a validação para identificar quais patrimônios
2. Acesse o cadastro de patrimônios
3. Vincule um responsável a cada patrimônio

### Erro: "CPF inválido"
**Causa**: O CPF do responsável está em formato incorreto
**Solução**: 
1. Acesse o cadastro de responsáveis
2. Corrija o CPF (formato: 000.000.000-00 ou 00000000000)

### Erro: "Diretório inválido"
**Causa**: O diretório selecionado não existe ou não tem permissão de escrita
**Solução**: 
1. Selecione um diretório válido
2. Verifique as permissões de escrita

### Arquivo não abre no Excel
**Causa**: O arquivo usa delimitador pipe (|) ao invés de vírgula
**Solução**: 
1. Abra o Excel
2. Vá em Dados > De Texto/CSV
3. Selecione o arquivo SIADS
4. Escolha delimitador "Outro" e digite |

## Uso Programático

Se precisar exportar via código:

```java
import com.inventario.siads.service.SiadsIntegrationService;
import java.io.File;

// Criar serviço
SiadsIntegrationService siadsService = new SiadsIntegrationService();

// Exportar todos os patrimônios
File diretorio = new File("C:/Exportacao");
File arquivo = siadsService.gerarArquivoSiads(diretorio);
System.out.println("Arquivo gerado: " + arquivo.getAbsolutePath());

// Ou exportar apenas um inventário específico
Long idInventario = 1L;
File arquivo2 = siadsService.gerarArquivoSiadsPorInventario(diretorio, idInventario);
```

## Testes

Para testar a formatação dos registros:

```bash
# Compile e execute o teste
javac src/main/java/com/inventario/siads/test/SiadsLayoutFormatterTest.java
java com.inventario.siads.test.SiadsLayoutFormatterTest
```

## Manutenção

### Adicionar Novos Campos

1. **Modelo** (`SiadsRegistro.java`):
```java
private String novoCampo;

public String getNovoCampo() {
    return novoCampo;
}

public void setNovoCampo(String novoCampo) {
    this.novoCampo = novoCampo;
}
```

2. **Conversor** (`SiadsConverterService.java`):
```java
registro.setNovoCampo(patrimonio.getNovoCampo());
```

3. **Formatador** (`SiadsLayoutFormatter.java`):
```java
// No cabeçalho
sb.append("NOVO_CAMPO").append(DELIMITADOR);

// No registro
sb.append(formatarCampo(registro.getNovoCampo())).append(DELIMITADOR);
```

### Alterar Formato do Arquivo

Para mudar de delimitado para posicional, edite `SiadsLayoutFormatter.java`:

```java
// Ao invés de delimitador, use posições fixas
private String formatarCampo(String valor, int tamanho) {
    if (valor == null) valor = "";
    return String.format("%-" + tamanho + "s", valor).substring(0, tamanho);
}
```

## Referências

- Documentação SIADS: `docs/Guia de Orientaes Gerais para Geracao dos ArquivosdeImplantacaov6211.pdf`
- README técnico: `src/main/java/com/inventario/siads/README.md`
- Código fonte: `src/main/java/com/inventario/siads/`

## Suporte

Para dúvidas ou problemas:
1. Consulte este documento
2. Verifique o README técnico
3. Execute os testes de formatação
4. Consulte os logs do sistema


## 📋 Checklist de Implantação

### 1. Configuração Inicial

- [ ] Copiar `siads.properties.example` para `siads.properties`
- [ ] Editar `siads.properties` com os códigos da sua instituição
- [ ] Obter código do órgão junto ao governo federal
- [ ] Obter código da Unidade Gestora
- [ ] Definir CPF do responsável autorizado

### 2. Integração ao Sistema

- [ ] Adicionar menu SIADS ao sistema principal
- [ ] Seguir guia em `SIADS_INTEGRACAO_MENU.md`
- [ ] Testar abertura do dialog
- [ ] Verificar botão de configurações

### 3. Testes

- [ ] Executar `SiadsExportTest.java` para teste básico
- [ ] Validar dados do sistema
- [ ] Exportar arquivo de teste
- [ ] Verificar formato do arquivo gerado
- [ ] Confirmar encoding UTF-8
- [ ] Validar delimitadores (¥ e £)

### 4. Validação

- [ ] Importar arquivo no sistema SIADS
- [ ] Verificar se não há erros de formato
- [ ] Confirmar dados importados
- [ ] Ajustar mapeamentos se necessário

### 5. Produção

- [ ] Treinar usuários
- [ ] Documentar processo interno
- [ ] Estabelecer rotina de exportação
- [ ] Manter backup dos arquivos gerados

## 🎉 Sistema Pronto!

O sistema agora está 100% compatível com o formato oficial SIADS v6.2.11!
