SERVIDOR MOBILE STANDALONE - SISTEMA DE INVENTARIO
===================================================

Este diretorio contem o servidor mobile como processo independente.

VANTAGENS DO PROCESSO SEPARADO:
- Gerenciamento de memoria isolado (GC independente)
- Pode ser reiniciado sem afetar o desktop
- Melhor controle de recursos
- Logs separados

ARQUIVOS:
- mobile-server.jar: Servidor executavel
- start-server.bat: Script Windows (CMD)
- start-server.ps1: Script Windows (PowerShell)
- application-mobile.properties: Configuracoes
- logs/: Diretorio de logs

EXECUCAO:
Windows CMD: start-server.bat
PowerShell: .\start-server.ps1

ACESSO:
URL Base: http://localhost:8081/inventario
Swagger: http://localhost:8081/inventario/swagger-ui.html
Health: http://localhost:8081/inventario/actuator/health

REQUISITOS:
- Java 21 ou superior
- PostgreSQL configurado e rodando
- Porta 8081 disponivel

MEMORIA:
- Inicial: 256MB
- Maxima: 1GB
- Metaspace: 192MB
