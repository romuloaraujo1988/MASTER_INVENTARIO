# Instruções para Debug - Carregamento de Participantes

## ✅ Correção Aplicada

Adicionados logs detalhados no método `carregarUsuarios()` para identificar o problema.

## 🔍 Como Testar

### 1. Execute a Aplicação Desktop

```bash
# Compile (já feito)
./mvnw.cmd clean compile

# Execute a aplicação
java -cp "target/classes;lib/*" com.inventario.SistemaInventarioApplication
```

### 2. Abra a Tela de Inventário

1. Faça login no sistema
2. Vá para "Inventário" → "Gerenciar Inventários"
3. Clique em "Novo" ou "Editar" um inventário existente
4. Vá para a aba "Participantes"

### 3. Observe os Logs no Console

Você verá logs detalhados como:

```
[DEBUG] ========== CARREGANDO USUÁRIOS ==========
[DEBUG] Obtendo UsuarioService...
[DEBUG] UsuarioService obtido: OK
[DEBUG] Chamando listarTodos()...
[DEBUG] listarTodos() retornou: OK
[DEBUG] Quantidade de usuários encontrados: 5
[DEBUG] Limpando modelo da lista...
[DEBUG] Adicionando usuários ao modelo...
[DEBUG] Adicionado: João Silva (ID: 1)
[DEBUG] Adicionado: Maria Santos (ID: 2)
[DEBUG] Total de usuários no modelo: 5
[DEBUG] ========== USUÁRIOS CARREGADOS COM SUCESSO ==========
```

## 🔴 Possíveis Problemas e Soluções

### Problema 1: "Nenhum usuário encontrado"

**Log:**
```
[DEBUG] Quantidade de usuários encontrados: 0
[AVISO] Nenhum usuário encontrado no banco de dados!
```

**Causa:** Tabela `usuario` está vazia

**Solução:**
1. Cadastre usuários no sistema
2. Ou execute script SQL para inserir usuários de teste:

```sql
INSERT INTO usuario (login, senha, nome_completo, email, ativo) 
VALUES 
('admin', '$2a$10$...', 'Administrador', 'admin@ifmt.edu.br', true),
('joao', '$2a$10$...', 'João Silva', 'joao@ifmt.edu.br', true),
('maria', '$2a$10$...', 'Maria Santos', 'maria@ifmt.edu.br', true);
```

### Problema 2: "UsuarioService não foi inicializado"

**Log:**
```
[DEBUG] UsuarioService obtido: NULL
[ERRO] UsuarioService não foi inicializado
```

**Causa:** `ServiceFactory.getUsuarioService()` retornou null

**Solução:**
1. Verificar se `UsuarioService` está registrado no `ServiceFactory`
2. Verificar se há erros de inicialização no startup

### Problema 3: Erro de conexão com banco

**Log:**
```
[ERRO] Tipo: SQLException
[ERRO] Mensagem: Connection refused
```

**Causa:** Banco de dados não está acessível

**Solução:**
1. Verificar se PostgreSQL está rodando
2. Verificar configurações em `configuracao_banco.json`
3. Testar conexão manualmente

### Problema 4: Lista vazia mas usuários existem

**Log:**
```
[DEBUG] Quantidade de usuários encontrados: 5
[DEBUG] Total de usuários no modelo: 5
```

**Mas a lista na tela está vazia**

**Causa:** Problema de renderização da JList

**Solução:**
1. Verificar se o `DefaultListCellRenderer` está configurado
2. Verificar se `Usuario.getNomeCompleto()` retorna valor válido
3. Adicionar log no renderer:

```java
DefaultListCellRenderer rendererBusca = new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Usuario) {
            Usuario usuario = (Usuario) value;
            String texto = usuario.getNomeCompleto() + " (" + usuario.getLogin() + ")";
            System.out.println("[DEBUG] Renderizando: " + texto);
            setText(texto);
        }
        return this;
    }
};
```

## 📋 Checklist de Verificação

Execute este checklist e anote os resultados:

- [ ] Aplicação inicia sem erros
- [ ] Consegue fazer login
- [ ] Consegue abrir tela de inventário
- [ ] Consegue abrir aba "Participantes"
- [ ] Vê logs de carregamento no console
- [ ] Logs mostram usuários sendo carregados
- [ ] Lista de usuários aparece na tela
- [ ] Consegue buscar usuários
- [ ] Consegue adicionar participantes

## 🐛 Reportar Problema

Se o problema persistir, copie e cole:

1. **Todos os logs do console** (especialmente os que começam com `[DEBUG]` e `[ERRO]`)
2. **Screenshot da tela** mostrando a lista vazia
3. **Resultado do checklist** acima
4. **Versão do Java:** `java -version`
5. **Sistema Operacional:** Windows/Linux/Mac

## 📞 Próximos Passos

Após executar e verificar os logs:

1. Se aparecer "Nenhum usuário encontrado" → Cadastre usuários
2. Se aparecer erro de conexão → Verifique banco de dados
3. Se usuários carregam mas lista está vazia → Problema de renderização
4. Se tudo funcionar → Problema resolvido! ✅

---

**Atualizado em:** 17/11/2025 19:46  
**Versão:** 2.0.0  
**Status:** Aguardando teste do usuário
