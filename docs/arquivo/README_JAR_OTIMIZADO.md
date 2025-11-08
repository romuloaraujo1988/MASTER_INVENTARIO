# JAR Otimizado - Sistema de Inventário

## Problema Resolvido

O JAR gerado pelo Spring Boot Maven Plugin estava com **131 MB** e não era executável diretamente pelo JRE. Agora temos uma solução otimizada similar ao que o NetBeans gera.

## Solução Implementada

### JAR Otimizado Gerado
- **Tamanho**: 0.87 MB (redução de 99.3%)
- **Localização**: `target/mobile-server/sistema-inventario-1.2.0.jar`
- **Dependências**: `target/mobile-server/lib/` (130+ JARs)

### Como Compilar

```bash
# Limpar e compilar com o perfil thin-jar
.\mvnw.cmd compile jar:jar -DskipTests -Pthin-jar

# Copiar dependências
.\mvnw.cmd dependency:copy-dependencies -DoutputDirectory=target\mobile-server\lib -DskipTests -Pthin-jar
```

### Como Executar

#### Opção 1: Script Batch (Recomendado)
```bash
cd target\mobile-server
.\executar.bat
```

#### Opção 2: Linha de Comando
```bash
cd target\mobile-server
java -cp "sistema-inventario-1.2.0.jar;lib\*" com.inventario.SistemaInventarioApplication
```

## Configuração do Perfil thin-jar

O perfil `thin-jar` foi adicionado ao `pom.xml` com:

1. **maven-jar-plugin**: Gera JAR com classpath no MANIFEST.MF
2. **maven-dependency-plugin**: Copia dependências para `lib/`
3. **spring-boot-maven-plugin**: Desabilitado para evitar reempacotamento

## Vantagens

✅ **JAR Pequeno**: 0.87 MB vs 131 MB  
✅ **Executável**: Funciona diretamente com `java -cp`  
✅ **Rápido**: Compilação mais rápida  
✅ **Compatível**: Mesmo comportamento do NetBeans  
✅ **Modular**: Dependências separadas em `lib/`  

## Estrutura Final

```
target/mobile-server/
├── sistema-inventario-1.2.0.jar    (0.87 MB)
├── executar.bat                     (Script de execução)
└── lib/                            (130+ dependências)
    ├── spring-boot-3.2.0.jar
    ├── postgresql-42.7.0.jar
    └── ... (outras dependências)
```

## Teste Realizado

✅ Compilação bem-sucedida  
✅ JAR executável confirmado  
✅ Aplicação inicia corretamente  
✅ Conexão com banco de dados funcional  
✅ Sistema de autenticação operacional  