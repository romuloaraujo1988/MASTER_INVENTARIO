# Guia para Habilitar o Modo Offline

## Status Atual

O sistema possui **toda a implementação do modo offline completa**, mas não está operacional devido à falta de uma dependência essencial.

## ❌ Problema Principal

**Driver SQLite JDBC ausente** - O sistema não consegue inicializar o banco SQLite local.

### Erro Identificado:
```
java.lang.ClassNotFoundException: org.sqlite.JDBC
```

## 🔧 Passos para Habilitar o Modo Offline

### 1. **Adicionar Dependência SQLite JDBC**

#### Opção A: Download Manual
```bash
# Baixar sqlite-jdbc-3.44.1.0.jar (ou versão mais recente)
wget https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar

# Mover para a pasta lib
mv sqlite-jdbc-3.44.1.0.jar ./lib/
```

#### Opção B: Maven (se usando)
```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

### 2. **Verificar Configurações**

O arquivo `src/main/resources/offline.properties` já está configurado:

```properties
# Modo offline habilitado
offline.enabled=true

# Caminho do banco SQLite
offline.database.path=./data/inventario_offline.db

# Sincronização automática
sync.auto.enabled=true
sync.interval.minutes=5
```

### 3. **Testar Inicialização**

Após adicionar o driver SQLite:

```bash
# Compilar e executar
javac -cp "lib/*;src/main/java" src/main/java/com/inventario/SistemaInventarioApplication.java
java -cp "lib/*;src/main/java" com.inventario.SistemaInventarioApplication
```

### 4. **Verificar Criação do Banco**

O sistema criará automaticamente:
- `./data/inventario_offline.db` - Banco SQLite local
- Tabelas espelho das entidades principais
- Tabelas de controle de sincronização

## ✅ Funcionalidades que Serão Ativadas

### Interface do Usuário
- **Indicador de Status**: Mostra se está online/offline
- **Botão de Sincronização**: Permite sincronização manual
- **Tela de Sincronização**: Gerenciamento completo via `SyncFrame`

### Operações Offline
- **Criação de Inventários**: Funciona sem conexão
- **Coleta de Patrimônios**: Armazenamento local
- **Consultas**: Dados locais disponíveis
- **Relatórios**: Baseados em dados locais

### Sincronização Automática
- **Detecção de Conectividade**: Monitora conexão automaticamente
- **Sincronização Bidirecional**: SQLite ↔ PostgreSQL
- **Resolução de Conflitos**: Estratégia configurável
- **Logs de Sincronização**: Histórico completo

## 🔍 Verificação de Funcionamento

### 1. **Logs de Inicialização**
Procure por estas mensagens no console:
```
INFO: Inicializando banco de dados SQLite
INFO: Banco de dados SQLite inicializado com sucesso
INFO: OfflineManager inicializado
INFO: Modo offline habilitado
```

### 2. **Arquivos Criados**
Verifique se foram criados:
- `./data/inventario_offline.db`
- Logs em `./logs/offline.log` (se configurado)

### 3. **Interface Visual**
Na tela principal (`InventarioFrame`):
- Indicador de status: "● Online" (verde) ou "● Offline" (vermelho)
- Botão de sincronização (⟳) ativo

## 📋 Checklist de Ativação

- [ ] **Driver SQLite adicionado** à pasta `lib/`
- [ ] **Aplicação reiniciada** após adicionar o driver
- [ ] **Banco SQLite criado** em `./data/inventario_offline.db`
- [ ] **Indicador de status** visível na interface
- [ ] **Sincronização manual** funcionando
- [ ] **Operações offline** testadas

## 🚨 Troubleshooting

### Problema: ClassNotFoundException
**Solução**: Verificar se `sqlite-jdbc-*.jar` está na pasta `lib/`

### Problema: Banco não criado
**Solução**: Verificar permissões da pasta `./data/`

### Problema: Sincronização não funciona
**Solução**: Verificar configurações de rede e PostgreSQL

## 📞 Suporte

Para problemas adicionais, verificar:
1. Logs do sistema
2. Configurações em `offline.properties`
3. Conectividade com PostgreSQL
4. Permissões de arquivo

---

**Nota**: Após seguir estes passos, o sistema estará **100% operacional** em modo offline.