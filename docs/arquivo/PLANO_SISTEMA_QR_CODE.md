# Plano de Implementação - Sistema de QR Codes para Patrimônio

## 📋 Análise e Requisitos

### Dados para QR Code (APENAS IMUTÁVEIS)
- **Número do Patrimônio** - Identificador único
- **Data de Aquisição** - Data de entrada no sistema
- **Descrição Resumida** - Descrição básica do item
- **ID do Patrimônio** - Chave primária para consultas

### Dados EXCLUÍDOS (Mutáveis)
- ❌ Responsável (pode mudar)
- ❌ Sala/Localização (pode mudar)
- ❌ Sistema/Instituição (pode mudar)
- ❌ Estado de conservação (pode mudar)

## 🎯 Estrutura do QR Code

### Formato JSON Simplificado
```json
{
  "id": 123,
  "numero": "123456",
  "descricao": "Notebook Dell Inspiron",
  "dataAquisicao": "2023-03-15"
}
```

### Formato Alternativo (String Simples)
```
ID:123|NUM:123456|DESC:Notebook Dell|DATA:2023-03-15
```

## 🗄️ Estrutura do Banco de Dados

### Tabela QR_CODE
```sql
CREATE TABLE TABELA_QR_CODE (
    ID SERIAL PRIMARY KEY,
    ID_PATRIMONIO INTEGER NOT NULL,
    CODIGO_QR TEXT NOT NULL,
    HASH_DADOS VARCHAR(64), -- SHA-256 dos dados para validação
    DATA_GERACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FORMATO VARCHAR(10) DEFAULT 'PNG',
    TAMANHO INTEGER DEFAULT 200,
    ATIVO BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (ID_PATRIMONIO) REFERENCES TABELA_PATRIMONIO(ID)
);

CREATE UNIQUE INDEX idx_qr_patrimonio ON TABELA_QR_CODE(ID_PATRIMONIO) WHERE ATIVO = TRUE;
CREATE INDEX idx_qr_hash ON TABELA_QR_CODE(HASH_DADOS);
```

## 📦 Dependências Maven

```xml
<!-- ZXing para geração e leitura de QR Codes -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

## 🏗️ Arquitetura de Classes

### 1. Model - QRCode.java
```java
public class QRCode {
    private int id;
    private int idPatrimonio;
    private String codigoQR;
    private String hashDados;
    private Timestamp dataGeracao;
    private String formato;
    private int tamanho;
    private boolean ativo;
    // getters, setters, construtores
}
```

### 2. Service - QRCodeService.java
- `gerarQRCode(Patrimonio patrimonio)` - Gera QR com dados imutáveis
- `gerarImagemQR(String dados, int tamanho)` - Cria BufferedImage
- `lerQRCode(BufferedImage imagem)` - Decodifica QR
- `validarQRCode(String dados, String hash)` - Valida integridade
- `formatarDadosPatrimonio(Patrimonio p)` - Formata JSON/String

### 3. DAO - QRCodeDAO.java
- `salvar(QRCode qrCode)`
- `buscarPorPatrimonio(int idPatrimonio)`
- `buscarPorHash(String hash)`
- `desativarQRCode(int id)`
- `listarAtivos()`

### 4. View - QRCodeFrame.java
- Lista patrimônios com status QR
- Geração individual/em lote
- Visualização e impressão
- Exportação para PDF/Excel

## 📅 Cronograma de Implementação

### Fase 1: Base (Semana 1)
- [x] Adicionar dependências ZXing
- [x] Criar script SQL da tabela
- [x] Implementar classe QRCode (model)
- [x] Implementar QRCodeDAO

### Fase 2: Serviços (Semana 2)
- [ ] Implementar QRCodeService
- [ ] Testes de geração/leitura
- [ ] Validação de dados
- [ ] Tratamento de erros

### Fase 3: Interface (Semana 3)
- [ ] QRCodeFrame - tela principal
- [ ] QRCodeVisualizadorDialog
- [ ] Integração com InventarioFrame
- [ ] Botões no PatrimonioFrame

### Fase 4: Funcionalidades Avançadas (Semana 4)
- [ ] Geração em lote
- [ ] Templates de etiquetas
- [ ] Impressão
- [ ] Exportação PDF/Excel

### Fase 5: Integração e Testes (Semana 5)
- [ ] Integração com ColetaFrame
- [ ] Leitura de QR na coleta
- [ ] Testes completos
- [ ] Documentação

## 🔧 Funcionalidades Principais

### Geração de QR Code
1. Selecionar patrimônio(s)
2. Gerar dados imutáveis
3. Criar QR Code
4. Salvar no banco
5. Exibir/imprimir

### Leitura de QR Code
1. Capturar/carregar imagem
2. Decodificar dados
3. Validar hash
4. Buscar patrimônio
5. Exibir informações atuais

### Impressão de Etiquetas
1. Template configurável
2. QR Code + dados básicos
3. Múltiplos formatos
4. Impressão em lote

## 🛡️ Segurança e Validação

### Integridade dos Dados
- Hash SHA-256 dos dados originais
- Validação na leitura
- Detecção de alterações

### Controle de Versão
- Apenas um QR ativo por patrimônio
- Histórico de gerações
- Desativação de QRs antigos

## 📊 Relatórios e Estatísticas

### Dashboard QR Codes
- Total de QRs gerados
- QRs por período
- QRs mais utilizados
- Patrimônios sem QR

### Relatórios
- Lista de patrimônios com QR
- Patrimônios sem QR
- Histórico de gerações
- Estatísticas de uso

## 🔄 Integração com Sistema Existente

### InventarioFrame
- Menu "Gerenciar QR Codes"
- Acesso ao QRCodeFrame

### PatrimonioFrame
- Botão "Gerar QR Code"
- Status do QR Code
- Visualização rápida

### ColetaFrame
- Campo de busca por QR
- Leitura automática
- Validação de dados

## 📝 Observações Importantes

1. **Dados Imutáveis**: QR Code contém apenas informações que não mudam
2. **Consulta Dinâmica**: Dados atuais (responsável, sala) obtidos via consulta
3. **Versionamento**: Controle de versões para QRs regenerados
4. **Performance**: Cache de imagens geradas
5. **Backup**: Exportação completa dos QRs

## 🚀 Próximos Passos

1. Adicionar dependências ZXing ao pom.xml
2. Executar script de criação da tabela
3. Implementar classes base (Model, DAO)
4. Criar serviço de geração
5. Desenvolver interface básica
6. Testes e integração

Este plano garante que os QR Codes sejam estáveis e não precisem ser regenerados quando dados mutáveis do patrimônio forem alterados.