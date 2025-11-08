# Sistema de Inventario - Aplicacao Desktop

## Versao: 1.2.0
## Data: 05/11/2025 21:27:35,74

## Requisitos

- Java 21 ou superior
- PostgreSQL 12 ou superior
- Arquivo de configuracao: configuracao_banco.json

## Instalacao

1. Extrair todos os arquivos para uma pasta
2. Configurar o banco de dados em configuracao_banco.json
3. Executar o script de inicializacao:
   - Windows: iniciar-desktop.bat
   - Linux/Mac: ./iniciar-desktop.sh

## Estrutura de Arquivos

```
sistema-inventario-desktop/
├── sistema-inventario-desktop.jar    # Aplicacao principal
├── lib/                               # Dependencias
├── iniciar-desktop.bat               # Script Windows
├── iniciar-desktop.sh                # Script Linux/Mac
└── README.md                          # Este arquivo
```

## Configuracao do Banco de Dados

Criar o arquivo `configuracao_banco.json` no diretorio home do usuario:

- Windows: C:\Users\[usuario]\configuracao_banco.json
- Linux/Mac: ~/configuracao_banco.json

Conteudo do arquivo:

```json
{
  "host": "localhost",
  "porta": "5432",
  "database": "sispatrimonio",
  "usuario": "inventario",
  "senha": "sua_senha_aqui"
}
```

## Suporte

Para suporte, entre em contato com a equipe de TI do IFMT.

## Notas

- Esta e a versao DESKTOP do sistema
- NAO inclui a API mobile
- Para a API mobile, use o build separado
