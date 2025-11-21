# ⚠️ IMPORTANTE: Porta do Servidor Mobile

## 🔌 Configuração de Porta

### Servidor Mobile API
- **Porta**: `8081`
- **URL Base**: `http://localhost:8081`
- **Profile**: `mobile`

### Servidor Desktop
- **Porta**: `8080`
- **URL Base**: `http://localhost:8080`
- **Profile**: `default`

---

## 📝 Endpoints Corretos

### Mobile API (Porta 8081)
```
✅ http://localhost:8081/api/mobile/auth/login
✅ http://localhost:8081/api/mobile/patrimonio
✅ http://localhost:8081/api/mobile/salas
✅ http://localhost:8081/api/mobile/coletas
✅ http://localhost:8081/actuator/health
```

### Desktop API (Porta 8080)
```
❌ NÃO usar para mobile
http://localhost:8080/...
```

---

## 🚀 Como Iniciar o Servidor Mobile

### Opção 1: Maven Wrapper
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mobile
```

### Opção 2: Script Dedicado
```bash
.\restart-mobile-server.bat
```

### Opção 3: JAR Compilado
```bash
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile --server.port=8081
```

---

## 🔍 Verificar se Está Rodando

### Teste de Porta
```powershell
Test-NetConnection -ComputerName localhost -Port 8081 -InformationLevel Quiet
# Deve retornar: True
```

### Teste de Health
```powershell
curl http://localhost:8081/actuator/health
# Deve retornar: {"status":"UP"}
```

### Verificar Processos Java
```powershell
Get-Process -Name "java" | Select-Object Id, @{Name='Memory(MB)';Expression={[math]::Round($_.WorkingSet64/1MB,2)}}
```

---

## 📱 Configuração no App Android

### ApiModule.kt
```kotlin
private const val BASE_URL = "http://10.0.2.2:8081/"  // Emulador
// ou
private const val BASE_URL = "http://192.168.x.x:8081/"  // Dispositivo físico
```

### Verificar URL no App
```
Menu → Configurações → URL do Servidor
Deve estar: http://[IP]:8081/
```

---

## ⚠️ Erros Comuns

### Erro: "Connection refused" na porta 8080
**Causa**: Tentando acessar porta errada  
**Solução**: Usar porta 8081

### Erro: "Timeout" ao sincronizar
**Causa**: Servidor não está rodando ou porta errada  
**Solução**: 
1. Verificar se servidor está na porta 8081
2. Verificar firewall
3. Verificar URL no app

### Erro: "404 Not Found"
**Causa**: Endpoint não existe ou porta errada  
**Solução**: Verificar se está usando `/api/mobile/...` na porta 8081

---

## 📋 Checklist de Configuração

- [ ] Servidor rodando na porta 8081
- [ ] Health endpoint respondendo
- [ ] App configurado com porta 8081
- [ ] Firewall liberado para porta 8081
- [ ] Scripts usando porta 8081

---

**SEMPRE USE PORTA 8081 PARA MOBILE API** ✅
