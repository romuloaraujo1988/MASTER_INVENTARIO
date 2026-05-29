# Correção do Erro `unaccent(text) does not exist`

**Data:** 29/05/2026 12:22
**Versão:** 2.9.0
**Build:** sihcp-server.jar (97 MB)

---

## 🐛 Problema Identificado

O servidor lançava erro PostgreSQL ao buscar sugestões de descrição:

```
ERROR: MobileSugestaoDescricaoService - Erro ao buscar sugestões de
descrição (idInventario=6, page=0, size=100)
org.postgresql.util.PSQLException: ERRO: função unaccent(text) não existe
  Dica: Nenhuma função corresponde com o nome e os tipos de argumentos
        informados. Você precisa adicionar conversões de tipo explícitas.
```

**Origem:** `PatrimonioDAO.buscarSugestoesNaoColetadasPaginado()` (linha 1467)

### Causa Raiz

A query SQL usava a função `unaccent()` da extensão PostgreSQL `unaccent`, mas **essa extensão não está instalada** no banco de produção (10.14.150.14).

Instalar a extensão exigiria:
- Privilégio de superuser
- Pacote `postgresql-contrib`
- `CREATE EXTENSION unaccent;`

Nem sempre disponível em produção.

---

## ✅ Solução Aplicada

Substituí `unaccent()` por `translate()` — função **built-in** do PostgreSQL que não requer nenhuma extensão. A solução é **auto-contida no JAR**, sem necessidade de mexer no banco.

### Antes (dependia da extensão)
```sql
WHERE unaccent(lower(p.DESCRICAO)) LIKE '%' || unaccent(lower(?)) || '%'
ORDER BY unaccent(lower(p.DESCRICAO)) ASC, p.NUMERO ASC
```

### Depois (built-in, sem extensão)
```sql
WHERE translate(lower(p.DESCRICAO), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc')
      LIKE '%' || translate(lower(?), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc') || '%'
ORDER BY translate(lower(p.DESCRICAO), ...) ASC, p.NUMERO ASC
```

### Novo Método Helper

```java
private static String normalizarSemAcento(String coluna) {
    final String fromChars = "áàâãäéèêëíìîïóòôõöúùûüç";
    final String toChars   = "aaaaaeeeeiiiiooooouuuuc";
    return "translate(lower(" + coluna + "), '" + fromChars + "', '" + toChars + "')";
}
```

O `translate()` mapeia cada vogal acentuada do português (+ cedilha) para a forma sem acento. Cobre: á à â ã ä, é è ê ë, í ì î ï, ó ò ô õ ö, ú ù û ü, ç.

---

## 📦 Arquivos Modificados

1. `sihcp-core/src/main/java/com/inventario/sihcp/dao/PatrimonioDAO.java`
   - Método `buscarSugestoesNaoColetadasPaginado()`: SQL usa `translate()` ao invés de `unaccent()`
   - Novo método privado `normalizarSemAcento(String)`
   - Javadoc atualizado

---

## 🔒 Segurança

- O parâmetro `coluna` do helper é sempre um literal controlado por nós (`"p.DESCRICAO"` ou `"?"`), **nunca entrada do usuário** → sem risco de SQL injection
- Os valores de busca continuam passando por `PreparedStatement`

---

## ⚠️ Observação sobre o Índice

O índice funcional `idx_patrimonio_descricao_unaccent` (que usa `unaccent`) **não será usado** pela nova query. Isso **não quebra nada** — apenas a query não se beneficia desse índice específico.

Se quiser otimizar a performance com índice compatível, pode criar (opcional):

```sql
CREATE INDEX IF NOT EXISTS idx_patrimonio_descricao_translate
    ON tabela_patrimonio (translate(lower(descricao), 'áàâãäéèêëíìîïóòôõöúùûüç', 'aaaaaeeeeiiiiooooouuuuc'));
```

Mas **não é obrigatório** — o sistema funciona sem ele.

---

## 🚀 Build de Produção

- **Comando:** `.\mvnw.cmd clean package -DskipTests -P fat-jar`
- **Resultado:** ✅ BUILD SUCCESS
- **JAR:** `SISTEMA_INVENTARIO_PRODUCAO/sihcp-server.jar` (97 MB, 29/05 12:22)

---

## ✅ Checklist de Deploy

- [x] Código corrigido (translate ao invés de unaccent)
- [x] Verificado tamanho das strings de translate (23 = 23)
- [x] Compilação bem-sucedida
- [x] JAR gerado e copiado para produção
- [ ] Substituir JAR no servidor (10.14.150.14)
- [ ] Reiniciar servidor
- [ ] Testar busca de sugestões de descrição
- [ ] Validar monitor ao vivo (correção anterior do WebSocket)

---

## 🎯 Próximos Passos

1. **Substituir o servidor** em 10.14.150.14:8081 com o novo JAR
2. **Reiniciar** o servidor
3. **Testar:**
   - Buscar sugestões de descrição (não deve mais dar erro)
   - Buscar com acentos (ex: "cadeira giratória") deve funcionar
   - Monitor ao vivo deve mostrar 🟢 AO VIVO

---

**Status:** ✅ Correção aplicada e servidor recompilado
**Este JAR também inclui:** correção anterior do WebSocket (monitor ao vivo)
