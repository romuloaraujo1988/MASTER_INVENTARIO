================================================================================
  SISTEMA DE INVENTÁRIO - VERSÃO ${project.version}
  GUIA DE INSTALAÇÃO E CONFIGURAÇÃO
================================================================================

REQUISITOS DO SISTEMA
---------------------
- Java 21 ou superior (JDK ou JRE)
- PostgreSQL 12 ou superior
- Windows, Linux ou macOS
- Mínimo 4GB RAM (recomendado 8GB)
- 500MB espaço em disco

ESTRUTURA DO PACOTE
--------------------
sistema-inventario-${project.version}/
├── sistema-inventario-${project.version}.jar  # Aplicação principal
├── lib/                                        # Dependências (JARs)
├── config/                                     # Arquivos de configuração
│   ├── application.properties                  # Config geral
│   ├── application-prod.properties             # Config produção
│   └── log4j2.xml                             # Config de logs
├── iniciar-desktop.bat                        # Script Windows (Desktop)
├── iniciar-desktop.sh                         # Script Linux/Mac (Desktop)
├── iniciar-mobile-server.bat                  # Script Windows (Mobile API)
├── iniciar-mobile-server.sh                   # Script Linux/Mac (Mobile API)
├── docs/                                      # Documentação
└── README-INSTALACAO.txt                      # Este arquivo

================================================================================
INSTALAÇÃO PASSO A PASSO
================================================================================

1. INSTALAR JAVA 21
-------------------
   Windows:
   - Download: https://adoptium.net/
   - Instalar e adicionar ao PATH
   - Verificar: java -version

   Linux (Ubuntu/Debian):
   sudo apt update
   sudo apt install openjdk-21-jdk
   java -version

   macOS:
   brew install openjdk@21
   java -version

2. CONFIGURAR BANCO DE DADOS
-----------------------------
   a) Instalar PostgreSQL 12+
   
   b) Criar banco de dados:
      psql -U postgres
      CREATE DATABASE sispatrimonio;
      CREATE USER inventario WITH PASSWORD 'sua_senha_aqui';
      GRANT ALL PRIVILEGES ON DATABASE sispatrimonio TO inventario;
      \q

   c) Executar scripts SQL (na pasta sql/ do projeto original):
      psql -U inventario -d sispatrimonio -f criar_tabelas_sispatrimonio.sql

3. CONFIGURAR APLICAÇÃO
-----------------------
   Editar: config/application-prod.properties

   # Banco de Dados
   spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
   spring.datasource.username=inventario
   spring.datasource.password=sua_senha_aqui

   # JWT (Mobile API)
   jwt.secret=sua_chave_secreta_aqui_minimo_256_bits
   jwt.expiration=86400
   jwt.refresh-expiration=604800

4. EXTRAIR PACOTE
-----------------
   Descompactar sistema-inventario-${project.version}.zip em:
   
   Windows: C:\Sistemas\inventario\
   Linux:   /opt/inventario/
   macOS:   /Applications/inventario/

================================================================================
EXECUÇÃO
================================================================================

MODO DESKTOP (Interface Swing)
-------------------------------
   Windows:
   - Duplo clique em: iniciar-desktop.bat
   - Ou via CMD: iniciar-desktop.bat

   Linux/Mac:
   chmod +x iniciar-desktop.sh
   ./iniciar-desktop.sh

   Acesso:
   - Interface gráfica abre automaticamente
   - Login padrão: admin / admin (alterar após primeiro acesso)

MODO MOBILE API SERVER
-----------------------
   Windows:
   - Duplo clique em: iniciar-mobile-server.bat
   - Ou via CMD: iniciar-mobile-server.bat

   Linux/Mac:
   chmod +x iniciar-mobile-server.sh
   ./iniciar-mobile-server.sh

   Acesso:
   - API: http://localhost:8080/inventario
   - Swagger: http://localhost:8080/inventario/swagger-ui.html
   - Health: http://localhost:8080/inventario/actuator/health

EXECUÇÃO MANUAL (Avançado)
---------------------------
   Desktop:
   java -Xms512m -Xmx2g -jar sistema-inventario-${project.version}.jar

   Mobile Server:
   java -Xms1g -Xmx4g -Dspring.profiles.active=mobile -jar sistema-inventario-${project.version}.jar

================================================================================
CONFIGURAÇÕES AVANÇADAS
================================================================================

MEMÓRIA JVM
-----------
   Editar scripts .bat ou .sh:
   
   Desktop (mínimo):
   -Xms512m -Xmx2g
   
   Mobile Server (recomendado):
   -Xms1g -Xmx4g
   
   Servidor com muitos usuários:
   -Xms2g -Xmx8g

PORTA DO SERVIDOR
-----------------
   Adicionar ao comando:
   -Dserver.port=8081

   Ou editar config/application-prod.properties:
   server.port=8081

LOGS
----
   Localização: logs/
   - sistema-inventario.log (Desktop)
   - mobile-server.log (Mobile API)
   
   Configurar em: config/log4j2.xml

PROFILES SPRING
---------------
   - default: Modo desktop (padrão)
   - mobile: Modo servidor mobile API
   - prod: Configurações de produção

   Usar: -Dspring.profiles.active=mobile,prod

================================================================================
SOLUÇÃO DE PROBLEMAS
================================================================================

ERRO: "Java não encontrado"
----------------------------
   Solução: Instalar Java 21 e adicionar ao PATH

ERRO: "Conexão com banco recusada"
----------------------------------
   Verificar:
   1. PostgreSQL está rodando?
      Windows: services.msc → PostgreSQL
      Linux: sudo systemctl status postgresql
   
   2. Credenciais corretas em application-prod.properties?
   
   3. Firewall bloqueando porta 5432?

ERRO: "Porta 8080 já em uso"
-----------------------------
   Solução 1: Parar processo na porta 8080
   Windows: netstat -ano | findstr :8080
            taskkill /PID <PID> /F
   
   Linux: lsof -i :8080
          kill -9 <PID>
   
   Solução 2: Usar outra porta
   -Dserver.port=8081

ERRO: "OutOfMemoryError"
------------------------
   Aumentar memória JVM nos scripts:
   -Xmx4g (ou mais)

APLICAÇÃO LENTA
---------------
   1. Verificar memória disponível
   2. Otimizar banco de dados:
      VACUUM ANALYZE;
      REINDEX DATABASE sispatrimonio;
   3. Aumentar pool de conexões em application-prod.properties:
      spring.datasource.hikari.maximum-pool-size=20

================================================================================
BACKUP E MANUTENÇÃO
================================================================================

BACKUP DO BANCO
---------------
   Diário (recomendado):
   pg_dump -U inventario sispatrimonio > backup_$(date +%Y%m%d).sql

   Restaurar:
   psql -U inventario -d sispatrimonio < backup_20251118.sql

ATUALIZAÇÃO DE VERSÃO
----------------------
   1. Fazer backup do banco
   2. Parar aplicação
   3. Substituir JAR e lib/
   4. Manter config/ (verificar novos parâmetros)
   5. Executar scripts SQL de migração (se houver)
   6. Reiniciar aplicação

MONITORAMENTO
-------------
   Logs: tail -f logs/sistema-inventario.log
   
   Métricas (Mobile API):
   http://localhost:8080/inventario/actuator/metrics
   http://localhost:8080/inventario/actuator/health

================================================================================
SEGURANÇA EM PRODUÇÃO
================================================================================

CHECKLIST OBRIGATÓRIO
----------------------
   [ ] Alterar senha padrão do admin
   [ ] Configurar JWT secret forte (256+ bits)
   [ ] Habilitar HTTPS/SSL
   [ ] Configurar firewall (apenas portas necessárias)
   [ ] Backup automático diário
   [ ] Logs de auditoria ativos
   [ ] Atualizar PostgreSQL regularmente
   [ ] Limitar acesso ao servidor

HTTPS/SSL (Recomendado)
-----------------------
   Adicionar em application-prod.properties:
   
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=senha_keystore
   server.ssl.key-store-type=PKCS12

================================================================================
SUPORTE E CONTATO
================================================================================

Documentação Completa: docs/
Logs: logs/
Issues: Contatar equipe de TI do IFMT

Desenvolvido por: IFMT
Versão: ${project.version}
Data: Novembro 2025

================================================================================
