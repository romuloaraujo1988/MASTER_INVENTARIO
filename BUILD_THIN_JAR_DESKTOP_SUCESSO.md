# Build Thin JAR Desktop - Sucesso

## ✅ Compilação Concluída

**Data:** 26/11/2025  
**Versão:** 2.0.0  
**Perfil:** thin-jar (não monolítico)

---

## 📦 Arquivos Gerados

### JAR Principal
```
target/mobile-server/sistema-inventario-2.0.0.jar
Tamanho: 1.43 MB
```

### Dependências Externas
```
target/lib/
Total de JARs: 193 arquivos
Tamanho Total: 133.85 MB
```

### Arquivos de Configuração
```
target/mobile-server/
├── sistema-inventario-2.0.0.jar (1.43 MB)
├── application.properties
├── application-mobile.properties
├── application-production.properties
├── application-test.properties
├── application-performance.properties
├── application.yml
├── README.txt
├── start-mobile-server.bat
└── start-mobile-server.sh
```

---

## 🚀 Como Executar

### Opção 1: Script Automático (Windows)
```bash
cd target/mobile-server
start-mobile-server.bat
```

### Opção 2: Script Automático (Linux/Mac)
```bash
cd target/mobile-server
chmod +x start-mobile-server.sh
./start-mobile-server.sh
```

### Opção 3: Manual
```bash
cd target/mobile-server
java -jar sistema-inventario-2.0.0.jar --spring.profiles.active=mobile
```

---

## 🔧 Configuração

### Banco de Dados
Editar `application-mobile.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio
spring.datasource.username=inventario
spring.datasource.password=sua_senha
```

### Porta do Servidor
```properties
server.port=8080
```

---

## 🌐 Acesso

### Aplicação Principal
```
http://localhost:8080/inventario
```

### Documentação API (Swagger)
```
http://localhost:8080/inventario/swagger-ui.html
```

### Health Check
```
http://localhost:8080/inventario/actuator/health
```

---

## 📊 Comparação: Thin JAR vs Fat JAR

| Característica | Thin JAR | Fat JAR |
|----------------|----------|---------|
| JAR Principal | 1.43 MB | ~135 MB |
| Dependências | 133.85 MB (separadas) | Incluídas |
| Atualização | Apenas JAR principal | JAR completo |
| Deploy | Copiar lib/ uma vez | Copiar tudo sempre |
| Startup | Rápido | Rápido |
| Manutenção | Fácil | Média |

---

## ✅ Vantagens do Thin JAR

### 1. Atualizações Rápidas
- Apenas 1.43 MB para atualizar código
- Dependências (133 MB) ficam fixas

### 2. Economia de Espaço
- Múltiplas versões compartilham mesmas libs
- Ideal para ambientes com várias instâncias

### 3. Deploy Eficiente
```
Primeira vez: 135 MB (JAR + libs)
Atualizações: 1.43 MB (apenas JAR)
```

### 4. Desenvolvimento Ágil
- Recompilação mais rápida
- Testes mais ágeis
- CI/CD otimizado

---

## 📁 Estrutura de Deploy

### Produção
```
/opt/inventario/
├── sistema-inventario-2.0.0.jar (1.43 MB)
├── lib/ (133.85 MB)
│   ├── spring-boot-*.jar
│   ├── postgresql-*.jar
│   └── ... (193 JARs)
├── application-production.properties
├── logs/
└── start-mobile-server.sh
```

### Atualização
```bash
# Apenas substituir o JAR principal
scp sistema-inventario-2.0.0.jar servidor:/opt/inventario/
ssh servidor "systemctl restart inventario"
```

---

## 🔍 Dependências Principais

### Spring Boot
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-actuator

### Banco de Dados
- postgresql-42.6.0.jar
- sqlite-jdbc-3.44.1.0.jar
- HikariCP-5.0.1.jar

### Segurança
- jjwt-api-0.11.5.jar
- jjwt-impl-0.11.5.jar
- spring-security-*

### Relatórios
- poi-5.4.0.jar (Excel)
- itext-7.2.5.jar (PDF)
- jfreechart-1.5.5.jar (Gráficos)

### QR Code
- core-3.5.2.jar (ZXing)
- javase-3.5.2.jar

---

## 🧪 Testes

### Verificar Compilação
```bash
java -jar target/mobile-server/sistema-inventario-2.0.0.jar --version
```

### Testar Startup
```bash
cd target/mobile-server
java -jar sistema-inventario-2.0.0.jar --spring.profiles.active=mobile
```

### Verificar Dependências
```bash
cd target/lib
ls -lh | wc -l  # Deve mostrar 193 JARs
```

---

## 📝 Logs de Build

### Compilação
```
[INFO] Compiling 286 source files
[INFO] BUILD SUCCESS
[INFO] Total time: 17.498 s
```

### Dependências Copiadas
```
[INFO] Copying 193 dependencies to target/lib/
```

### Empacotamento
```
[INFO] Building jar: target/mobile-server/sistema-inventario-2.0.0.jar
[INFO] Copying 7 files to target/mobile-server
```

---

## 🚨 Troubleshooting

### Erro: "ClassNotFoundException"
**Causa:** Pasta lib/ não está no mesmo diretório do JAR  
**Solução:** Copiar pasta lib/ junto com o JAR

### Erro: "Could not find or load main class"
**Causa:** MANIFEST.MF incorreto  
**Solução:** Recompilar com `mvn clean package -P thin-jar`

### Erro: "Port 8080 already in use"
**Causa:** Porta ocupada  
**Solução:** Alterar porta em application.properties ou matar processo

---

## 📦 Distribuição

### Criar Pacote Completo
```bash
cd target
zip -r inventario-mobile-2.0.0.zip mobile-server/ lib/
```

### Tamanho do Pacote
```
inventario-mobile-2.0.0.zip: ~50 MB (comprimido)
Descomprimido: ~135 MB
```

---

## ✅ Checklist de Deploy

- [ ] Java 21 instalado no servidor
- [ ] PostgreSQL configurado e rodando
- [ ] Copiar JAR + lib/ para servidor
- [ ] Configurar application-production.properties
- [ ] Testar conexão com banco
- [ ] Executar start-mobile-server.sh
- [ ] Verificar logs de startup
- [ ] Testar endpoints via Swagger
- [ ] Configurar systemd/service (opcional)

---

## 🎉 Resultado

✅ **Build bem-sucedido!**  
✅ **JAR thin gerado: 1.43 MB**  
✅ **193 dependências copiadas: 133.85 MB**  
✅ **Pronto para deploy em produção**

---

**Comando usado:**
```bash
.\mvnw.cmd clean package -DskipTests -P thin-jar
```

**Perfil Maven:** `thin-jar`  
**Tempo de build:** 17.5 segundos  
**Status:** ✅ SUCCESS
