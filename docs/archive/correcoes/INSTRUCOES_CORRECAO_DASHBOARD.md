# Instruções para Corrigir Dashboard

## Problema Identificado

O servidor está retornando estatísticas com todos os valores em 0, mesmo que existam patrimônios no banco de dados.

## Alterações Realizadas

1. **MobileDashboardController.java** - Adicionados logs detalhados e tratamento de erros
2. Corrigido o construtor do DashboardStatsDTO (estava passando parâmetro errado)

## Passos para Aplicar a Correção

### 1. Parar o Servidor Atual

```powershell
# Encontrar o processo na porta 8081
netstat -ano | findstr ":8081"

# Matar o processo (substitua PID pelo número encontrado)
taskkill /F /PID 10844
```

### 2. Recompilar o Projeto

```powershell
# No diretório raiz do projeto
mvn clean compile -DskipTests
```

### 3. Reiniciar o Servidor Mobile

```powershell
# Opção 1: Via Maven
mvn exec:java -Dexec.mainClass="com.inventario.MobileApiApplication"

# Opção 2: Via JAR (se já compilado)
java -jar target/sistema-inventario-1.2.0.jar --spring.profiles.active=mobile
```

### 4. Verificar os Logs

Após reiniciar, os logs devem mostrar:

```
=== INICIANDO BUSCA DE ESTATÍSTICAS ===
PatrimonioDAO injetado: SIM
ColetaDAO injetado: SIM
UsuarioService injetado: SIM
Buscando todos os patrimônios...
Total de patrimônios encontrados: X
```

### 5. Testar no App

1. Abra o app no emulador
2. Faça login
3. Navegue para a Dashboard
4. As estatísticas devem aparecer

### 6. Verificar Logs do App

```powershell
# Limpar logs
adb logcat -c

# Monitorar logs relevantes
adb logcat -s DashboardViewModel:D DashboardFragment:D
```

## Possíveis Problemas

### Se ainda retornar 0:

1. **Verificar se há dados no banco:**
   ```sql
   SELECT COUNT(*) FROM TABELA_PATRIMONIO;
   ```

2. **Verificar conexão do servidor com o banco:**
   - Checar logs do servidor ao iniciar
   - Procurar por erros de conexão com PostgreSQL

3. **Verificar se o PatrimonioDAO está funcionando:**
   - Os logs devem mostrar "PatrimonioDAO injetado: SIM"
   - Se mostrar "NÃO", há problema com Spring Dependency Injection

### Se o app não conectar:

1. **Verificar URL do servidor no app:**
   - Deve ser: `http://10.0.2.2:8081/inventario/`
   - 10.0.2.2 é o IP do host no emulador Android

2. **Verificar se o servidor está escutando em 0.0.0.0:**
   - Deve estar em `application-mobile.properties`:
   ```properties
   server.address=0.0.0.0
   ```

## Logs Esperados (Sucesso)

### Servidor:
```
INFO  MobileDashboardController - === INICIANDO BUSCA DE ESTATÍSTICAS ===
INFO  MobileDashboardController - PatrimonioDAO injetado: SIM
INFO  MobileDashboardController - Buscando todos os patrimônios...
INFO  MobileDashboardController - Total de patrimônios encontrados: 150
INFO  MobileDashboardController - Estatísticas: Total=150, Coletados=45, Pendentes=105, Valor=1500000.0, Coletores=3
```

### App:
```
D DashboardViewModel: ✓ Estatísticas carregadas com sucesso:
D DashboardViewModel:   - Total: 150
D DashboardViewModel:   - Coletados: 45
D DashboardViewModel:   - Pendentes: 105
D DashboardViewModel:   - Divergências: 0
D DashboardViewModel:   - Coletores: 3
```

## Contato

Se o problema persistir, verifique:
1. Logs completos do servidor
2. Logs completos do app
3. Resultado da query SQL no banco de dados
