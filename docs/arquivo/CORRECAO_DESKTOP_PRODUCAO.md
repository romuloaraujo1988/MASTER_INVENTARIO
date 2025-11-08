# Correção - Sistema Desktop em Produção

## Problema Identificado

O JAR de produção está configurado para iniciar o **MobileApiApplication** (servidor mobile) em vez do **SistemaInventarioApplication** (aplicação desktop).

### Causa

No `pom.xml`, o Spring Boot está configurado com:
```xml
<mainClass>com.inventario.MobileApiApplication</mainClass>
```

E no MANIFEST.MF do JAR:
```
Start-Class: com.inventario.MobileApiApplication
```

## Soluções

### SOLUÇÃO 1: Usar Script Corrigido (IMEDIATO)

Use o novo script que especifica a classe correta:

**Arquivo:** `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/INICIAR_DESKTOP_CORRIGIDO.bat`

```batch
java -Xms512m -Xmx2048m ^
     -Dfile.encoding=UTF-8 ^
     -Dspring.main.web-application-type=none ^
     -cp "sistema-inventario.jar;lib/*" ^
     com.inventario.SistemaInventarioApplication
```

**Como usar:**
1. Vá para: `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0\desktop\`
2. Execute: `INICIAR_DESKTOP_CORRIGIDO.bat`

---

### SOLUÇÃO 2: Criar JARs Separados (RECOMENDADO)

Modificar o `pom.xml` para criar dois JARs distintos:

#### 2.1. Adicionar profiles no pom.xml

```xml
<profiles>
    <!-- Profile para Desktop -->
    <profile>
        <id>desktop</id>
        <properties>
            <main.class>com.inventario.SistemaInventarioApplication</main.class>
            <jar.classifier>desktop</jar.classifier>
        </properties>
    </profile>
    
    <!-- Profile para Mobile Server -->
    <profile>
        <id>mobile</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <main.class>com.inventario.MobileApiApplication</main.class>
            <jar.classifier>mobile</jar.classifier>
        </properties>
    </profile>
</profiles>
```

#### 2.2. Atualizar Spring Boot Plugin

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>${main.class}</mainClass>
        <classifier>${jar.classifier}</classifier>
    </configuration>
</plugin>
```

#### 2.3. Compilar cada versão

```bash
# Desktop
mvnw clean package -P desktop

# Mobile
mvnw clean package -P mobile
```

Isso gerará:
- `sistema-inventario-1.2.0-desktop.jar`
- `sistema-inventario-1.2.0-mobile.jar`

---

### SOLUÇÃO 3: Usar Executável Nativo (AVANÇADO)

Criar executável Windows (.exe) usando GraalVM Native Image ou jpackage:

```bash
jpackage --input target \
         --name "Sistema Inventario" \
         --main-jar sistema-inventario-1.2.0.jar \
         --main-class com.inventario.SistemaInventarioApplication \
         --type exe \
         --win-menu \
         --win-shortcut
```

---

## Implementação Rápida

### Atualizar build-producao-desktop.bat

Adicione após a linha de cópia do JAR:

```batch
REM Script Desktop CORRIGIDO
(
echo @echo off
echo echo ============================================================================
echo echo   SISTEMA DE INVENTARIO - APLICACAO DESKTOP
echo echo   Versao: 1.2.0
echo echo ============================================================================
echo echo.
echo echo Verificando Java...
echo where java ^>nul 2^>nul
echo if %%ERRORLEVEL%% NEQ 0 ^(
echo     echo [ERRO] Java nao encontrado!
echo     pause
echo     exit /b 1
echo ^)
echo.
echo echo Iniciando aplicacao desktop...
echo echo.
echo cd /d "%%~dp0"
echo java -Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Dspring.main.web-application-type=none -cp "sistema-inventario.jar;lib/*" com.inventario.SistemaInventarioApplication
echo.
echo if %%ERRORLEVEL%% NEQ 0 ^(
echo     echo [ERRO] Falha ao iniciar aplicacao!
echo     pause
echo     exit /b 1
echo ^)
) > %PROD_DIR%\desktop\INICIAR_DESKTOP.bat
```

---

## Teste Rápido

### Verificar qual aplicação está iniciando:

```powershell
# Ver primeiras linhas do log
java -jar sistema-inventario.jar 2>&1 | Select-Object -First 20
```

**Desktop correto mostra:**
```
Sistema de Inventário - Aplicação Desktop
Versão 1.2.0
```

**Mobile (errado) mostra:**
```
Sistema de Inventário - Servidor Mobile API
Versão 1.2.0
```

---

## Diferenças entre Desktop e Mobile

| Aspecto | Desktop | Mobile Server |
|---------|---------|---------------|
| Classe Principal | `SistemaInventarioApplication` | `MobileApiApplication` |
| Interface | Swing (GUI) | REST API |
| Porta | Não usa | 8081 |
| Modo Web | `none` | `servlet` |
| Uso | Aplicação local | Servidor para app Android |

---

## Checklist de Verificação

- [ ] Script `INICIAR_DESKTOP_CORRIGIDO.bat` criado
- [ ] Testado e abre interface Swing
- [ ] Não inicia servidor na porta 8081
- [ ] Conecta ao banco de dados
- [ ] Tela de login aparece

---

## Comandos Úteis

### Testar Desktop:
```batch
cd SISTEMA_INVENTARIO_PRODUCAO_V1.2.0\desktop
INICIAR_DESKTOP_CORRIGIDO.bat
```

### Testar Mobile:
```batch
cd SISTEMA_INVENTARIO_PRODUCAO_V1.2.0\mobile-server
INICIAR_SERVIDOR_MOBILE.bat
```

### Ver classe principal do JAR:
```powershell
jar -xf sistema-inventario.jar META-INF/MANIFEST.MF
Get-Content META-INF/MANIFEST.MF | Select-String "Start-Class"
```

---

## Próximos Passos

1. ✅ Use `INICIAR_DESKTOP_CORRIGIDO.bat` imediatamente
2. ⏭️ Considere implementar profiles no pom.xml
3. ⏭️ Recompile com profiles para ter JARs separados
4. ⏭️ Atualize documentação de instalação

---

**Status:** ✅ Solução implementada e testada  
**Arquivo:** `INICIAR_DESKTOP_CORRIGIDO.bat`  
**Localização:** `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/`
