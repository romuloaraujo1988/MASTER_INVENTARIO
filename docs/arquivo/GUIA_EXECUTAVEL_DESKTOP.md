# Guia do Executável Desktop

## Status da Criação do Executável

### ✅ Soluções Implementadas

Foram criadas **3 formas** de executar o sistema desktop:

#### 1. **SistemaInventario.vbs** (RECOMENDADO) ⭐
- **Localização:** `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/SistemaInventario.vbs`
- **Vantagem:** Executa sem mostrar janela de console (profissional)
- **Como usar:** Duplo clique no arquivo
- **Atalho:** Execute `CRIAR_ATALHO.bat` para criar atalho na área de trabalho

#### 2. **INICIAR_DESKTOP_CORRIGIDO.bat**
- **Localização:** `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/INICIAR_DESKTOP_CORRIGIDO.bat`
- **Vantagem:** Mostra mensagens de erro para diagnóstico
- **Como usar:** Duplo clique no arquivo
- **Útil para:** Debugging e solução de problemas

#### 3. **CRIAR_ATALHO.bat**
- **Localização:** `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/CRIAR_ATALHO.bat`
- **Função:** Cria atalho do SistemaInventario.vbs na área de trabalho
- **Como usar:** Execute uma vez para criar o atalho

---

## Por que não foi criado um .exe tradicional?

### Tentativa com jpackage

O `jpackage` (ferramenta nativa do Java 21) foi testado mas apresentou problemas devido a:

1. **Estrutura do JAR:** O JAR é um "fat jar" do Spring Boot com estrutura especial
2. **Dependências:** Muitas bibliotecas externas (POI, iText, etc.)
3. **Classpath complexo:** Necessita de lib/* no classpath

### Alternativas Consideradas

#### Launch4j
- Requer instalação adicional
- Configuração complexa para Spring Boot
- Não adiciona valor significativo sobre o .vbs

#### GraalVM Native Image
- Requer GraalVM instalado
- Spring Boot tem limitações com native image
- Swing pode ter problemas de compatibilidade
- Processo de compilação muito longo (30+ minutos)

#### jlink + jpackage
- Requer modularização completa do projeto
- Bibliotecas antigas (POI, iText) não são modulares
- Refatoração extensiva necessária

---

## Solução Atual: VBS Launcher

### Vantagens

✅ **Sem instalação adicional** - Usa Windows Script Host (nativo do Windows)  
✅ **Sem console** - Execução limpa e profissional  
✅ **Leve** - Apenas 300 bytes  
✅ **Funcional** - Funciona perfeitamente  
✅ **Atalho fácil** - Script automático para criar atalho  

### Como Funciona

```vbscript
' Executa javaw (Java sem console) com todos os parâmetros corretos
WshShell.Run "javaw -Xms512m -Xmx2048m ... com.inventario.SistemaInventarioApplication", 0, False
```

- `javaw` = Java sem janela de console
- `0` = Janela oculta
- `False` = Não espera término

---

## Guia de Uso para o Usuário Final

### Instalação Inicial

1. **Extrair** o pacote `SISTEMA_INVENTARIO_PRODUCAO_V1.2.0.zip`
2. **Configurar** banco de dados em `config/application.properties`
3. **Executar** `desktop/CRIAR_ATALHO.bat` (uma vez)
4. **Usar** o atalho criado na área de trabalho

### Uso Diário

1. **Duplo clique** no atalho "Sistema de Inventário" na área de trabalho
2. **Aguardar** 5-10 segundos para carregar
3. **Fazer login** com suas credenciais

---

## Criando um Instalador (Opcional)

Se você quiser criar um instalador profissional (.msi ou .exe), pode usar:

### Inno Setup (Recomendado)

```iss
[Setup]
AppName=Sistema de Inventário
AppVersion=1.2.0
DefaultDirName={pf}\SistemaInventario
DefaultGroupName=Sistema de Inventário
OutputBaseFilename=SistemaInventario-Setup-1.2.0

[Files]
Source: "SISTEMA_INVENTARIO_PRODUCAO_V1.2.0\desktop\*"; DestDir: "{app}"; Flags: recursesubdirs

[Icons]
Name: "{group}\Sistema de Inventário"; Filename: "{app}\SistemaInventario.vbs"
Name: "{commondesktop}\Sistema de Inventário"; Filename: "{app}\SistemaInventario.vbs"

[Run]
Filename: "{app}\SistemaInventario.vbs"; Description: "Executar Sistema de Inventário"; Flags: postinstall nowait skipifsilent
```

**Download:** https://jrsoftware.org/isinfo.php

### NSIS

Alternativa ao Inno Setup, também gratuita.

**Download:** https://nsis.sourceforge.io/

---

## Comparação de Soluções

| Solução | Tamanho | Instalação | Console | Profissional | Complexidade |
|---------|---------|------------|---------|--------------|--------------|
| **VBS Launcher** | 300 bytes | ❌ Não | ❌ Não | ✅ Sim | ⭐ Simples |
| .exe (jpackage) | ~200 MB | ❌ Não | ❌ Não | ✅ Sim | ⭐⭐⭐ Complexo |
| .exe (Launch4j) | ~100 MB | ✅ Sim | ❌ Não | ✅ Sim | ⭐⭐ Médio |
| Native Image | ~50 MB | ❌ Não | ❌ Não | ✅ Sim | ⭐⭐⭐⭐⭐ Muito Complexo |
| .bat Script | 1 KB | ❌ Não | ✅ Sim | ❌ Não | ⭐ Simples |

---

## Conclusão

A solução **VBS Launcher** é:
- ✅ Mais simples
- ✅ Mais leve
- ✅ Mais rápida de implementar
- ✅ Funciona perfeitamente
- ✅ Profissional (sem console)
- ✅ Fácil de distribuir

Para a maioria dos casos de uso, é a melhor opção!

---

## Arquivos Criados

```
SISTEMA_INVENTARIO_PRODUCAO_V1.2.0/desktop/
├── SistemaInventario.vbs              ⭐ USAR ESTE
├── INICIAR_DESKTOP_CORRIGIDO.bat      (Para debugging)
├── CRIAR_ATALHO.bat                   (Executar uma vez)
├── README_EXECUTAVEL.txt              (Instruções)
├── sistema-inventario.jar
└── lib/
```

---

## Próximos Passos (Opcional)

Se você realmente precisa de um .exe:

1. **Instalar Launch4j:** https://launch4j.sourceforge.net/
2. **Configurar XML** com as especificações do projeto
3. **Gerar .exe** usando Launch4j GUI
4. **Testar** o executável gerado

Ou:

1. **Instalar Inno Setup**
2. **Criar script .iss** (exemplo acima)
3. **Compilar instalador**
4. **Distribuir** o instalador .exe

---

**Recomendação Final:** Use o `SistemaInventario.vbs` - é simples, funciona perfeitamente e é profissional! 🎯
