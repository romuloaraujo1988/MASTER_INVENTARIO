SISTEMA DE INVENTARIO - PRODUCAO
================================

Este diretorio contem o sistema completo com JARs separados (thin JARs).

ESTRUTURA:
  * sihcp-desktop.jar: Aplicacao desktop Swing (~1.6 MB)
  * mobile-server.jar: Servidor mobile API (~1.6 MB)
  * lib/: Dependencias compartilhadas (~134 MB)
  * logs/: Diretorio de logs

EXECUCAO:

SERVIDOR MOBILE:
   .\iniciar-servidor-mobile.ps1
   ou iniciar-servidor-mobile.bat

PORTAS:
  * Servidor Mobile: http://localhost:8081/inventario

MEMORIA:
  * Servidor Mobile: 256MB - 1GB (processo separado)

VANTAGENS DOS THIN JARS:
  * JARs pequenos (~1.6 MB cada)
  * Dependencias compartilhadas
  * Atualizacoes rapidas
  * Processos independentes
  * Melhor controle de memoria

REQUISITOS:
  * Java 21 ou superior
  * PostgreSQL configurado
