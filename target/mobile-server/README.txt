SERVIDOR MOBILE - SISTEMA DE INVENTARIO
========================================

Este diretório contém o servidor mobile do Sistema de Inventário.

ESTRUTURA:
- sistema-inventario-2.0.0.jar: Aplicação principal
- lib/: Dependências externas
- application-mobile.properties: Configurações do servidor mobile
- start-mobile-server.bat: Script para Windows
- start-mobile-server.sh: Script para Linux/Mac

EXECUÇÃO:
Windows: Execute start-mobile-server.bat
Linux/Mac: Execute ./start-mobile-server.sh
Manual: java -jar sistema-inventario-2.0.0.jar --spring.profiles.active=mobile

ACESSO:
URL: http://localhost:8080/inventario
Swagger: http://localhost:8080/inventario/swagger-ui.html

REQUISITOS:
- Java 21 ou superior
- PostgreSQL configurado e rodando
- Configurações de banco em application-mobile.properties