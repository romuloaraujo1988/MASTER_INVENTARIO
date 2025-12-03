SISTEMA DE INVENTARIO - PRODUCAO
================================

Este diretorio contem o sistema completo com JARs separados (thin JARs).

ESTRUTURA:
- desktop.jar: Aplicacao desktop Swing (~1.6 MB)
- mobile-server.jar: Servidor mobile API (~1.6 MB)
- lib/: Dependencias compartilhadas (~134 MB)
- logs/: Diretorio de logs

EXECUCAO:

1. APENAS DESKTOP:
   .\iniciar-desktop.ps1
   ou iniciar-desktop.bat

2. APENAS SERVIDOR MOBILE:
   .\iniciar-servidor-mobile.ps1
   ou iniciar-servidor-mobile.bat

3. AMBOS (Desktop + Servidor):
   iniciar-tudo.bat

PORTAS:
- Desktop: Aplicacao local (sem porta)
- Servidor Mobile: http://localhost:8081/inventario

MEMORIA:
- Desktop: 512MB - 2GB
- Servidor Mobile: 256MB - 1GB (processo separado)

VANTAGENS DOS THIN JARS:
- JARs pequenos (~1.6 MB cada)
- Dependencias compartilhadas
- Atualizacoes rapidas
- Processos independentes
- Melhor controle de memoria

REQUISITOS:
- Java 21 ou superior
- PostgreSQL configurado
