# Como Executar o Servidor Mobile Spring Boot no NetBeans

Este documento fornece instruções detalhadas para executar o servidor mobile Spring Boot no NetBeans IDE.

## Problema Identificado

O NetBeans tem dificuldades para executar diretamente aplicações Spring Boot com perfis específicos, especialmente quando a classe principal difere da aplicação desktop.

## Soluções Implementadas

### 1. Configurações do NetBeans Atualizadas

Foram criados/atualizados os seguintes arquivos de configuração:

- `nbactions.xml` - Ações customizadas para executar o servidor mobile
- `nb-configuration.xml` - Configurações específicas do Spring Boot
- `.netbeans/project.properties` - Propriedades do projeto

### 2. Scripts Alternativos

Foram criados scripts para executar o servidor fora do NetBeans:

- `start-mobile-server-netbeans.bat` - Script batch para Windows
- `start-mobile-server-netbeans.ps1` - Script PowerShell com recursos avançados

## Como Executar no NetBeans

### Opção 1: Usando Ações Customizadas

1. **Abra o projeto no NetBeans**
2. **Clique com o botão direito no projeto**
3. **Selecione "Run Maven" > "Goals..."**
4. **Digite o seguinte comando:**
   ```
   clean compile spring-boot:run -Dspring-boot.run.profiles=mobile -Dspring-boot.run.main-class=com.inventario.MobileApiApplication
   ```
5. **Clique em "Run"**

### Opção 2: Usando o Menu de Ações Customizadas

1. **Abra o projeto no NetBeans**
2. **Clique com o botão direito no projeto**
3. **Procure por "CUSTOM-mobile-server" no menu**
4. **Clique na ação para executar**

### Opção 3: Configurando uma Nova Ação

1. **Vá em Project Properties > Actions**
2. **Clique em "Add Custom..."**
3. **Configure:**
   - **Action Name:** `Run Mobile Server`
   - **Goals:** `clean compile spring-boot:run`
   - **Properties:**
     ```
     spring-boot.run.profiles=mobile
     spring-boot.run.main-class=com.inventario.MobileApiApplication
     server.port=8081
     ```

## Como Executar Usando Scripts

### Script Batch (Windows)

```cmd
cd D:\MASTER_INVENTARIO
start-mobile-server-netbeans.bat
```

### Script PowerShell (Recomendado)

```powershell
cd D:\MASTER_INVENTARIO
.\start-mobile-server-netbeans.ps1
```

O script PowerShell oferece recursos adicionais:
- Verificação de porta disponível
- Logs detalhados
- Opção de parar processos na porta 8081
- Verificação de dependências

## Configurações Importantes

### Classe Principal
- **Desktop:** `com.inventario.SistemaInventarioApplication`
- **Mobile:** `com.inventario.MobileApiApplication`

### Perfis Spring
- **Desktop:** `default`
- **Mobile:** `mobile`

### Portas
- **Desktop:** `8080` (se aplicável)
- **Mobile:** `8081`

### Arquivos de Configuração
- **Desktop:** `application.properties`
- **Mobile:** `application-mobile.properties`

## Verificação do Servidor

Após iniciar o servidor mobile, verifique se está funcionando:

1. **Abra o navegador**
2. **Acesse:** `http://localhost:8081/inventario`
3. **Ou teste a API:** `http://localhost:8081/inventario/api/health`

## Troubleshooting

### Problema: Porta 8081 já está em uso
**Solução:** Execute o script PowerShell que oferece opção de parar processos na porta.

### Problema: Classe principal não encontrada
**Solução:** Verifique se `com.inventario.MobileApiApplication` existe no projeto.

### Problema: Perfil mobile não ativado
**Solução:** Certifique-se de que `-Dspring.profiles.active=mobile` está sendo passado.

### Problema: Dependências não encontradas
**Solução:** Execute `mvn clean install` antes de iniciar o servidor.

## Logs e Debug

Para executar em modo debug:

1. **Use a ação "CUSTOM-mobile-server-debug"**
2. **Ou adicione ao comando Maven:**
   ```
   -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```

## Contato

Se você continuar enfrentando problemas, verifique:
1. Se o Java 21 está configurado corretamente
2. Se o Maven está funcionando (`mvn --version`)
3. Se as dependências estão atualizadas (`mvn dependency:resolve`)
4. Se não há conflitos de porta

---

**Última atualização:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")