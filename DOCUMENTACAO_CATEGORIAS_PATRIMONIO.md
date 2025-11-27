# Documentação - Sistema de Categorias de Patrimônios

## 📋 Visão Geral

Sistema hierárquico de categorização de patrimônios baseado em análise de dados reais do banco PostgreSQL.

**Data de Criação:** 27/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Pronto para uso

---

## 🎯 Objetivo

Organizar os 11.428 patrimônios do sistema em categorias e subcategorias para:
- ✅ Facilitar busca e filtros
- ✅ Melhorar relatórios e estatísticas
- ✅ Agilizar coleta de itens sem etiqueta
- ✅ Reduzir itens em "OUTROS" (atualmente 63%)

---

## 📊 Estrutura de Dados

### Tabelas Criadas

#### 1. `tabela_categoria_patrimonio`
Categorias principais (9 categorias)

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | INTEGER/SERIAL | Chave primária |
| nome | VARCHAR(100) | Nome da categoria (único) |
| descricao | TEXT | Descrição detalhada |
| icone | VARCHAR(50) | Nome do ícone Material Design |
| cor | VARCHAR(20) | Cor hexadecimal para UI |
| ordem_exibicao | INTEGER | Ordem de exibição (menor = primeiro) |
| ativo | BOOLEAN/INTEGER | Se a categoria está ativa |
| data_criacao | TIMESTAMP/TEXT | Data de criação |
| data_atualizacao | TIMESTAMP/TEXT | Data da última atualização |

#### 2. `tabela_subcategoria_patrimonio`
Subcategorias (40+ subcategorias)

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | INTEGER/SERIAL | Chave primária |
| id_categoria | INTEGER | FK para categoria |
| nome | VARCHAR(100) | Nome da subcategoria |
| descricao | TEXT | Descrição detalhada |
| ordem_exibicao | INTEGER | Ordem de exibição |
| ativo | BOOLEAN/INTEGER | Se a subcategoria está ativa |
| data_criacao | TIMESTAMP/TEXT | Data de criação |
| data_atualizacao | TIMESTAMP/TEXT | Data da última atualização |

---

## 🗂️ Categorias e Subcategorias

### 1. MOBILIÁRIO (2.937 itens - R$ 3,5M)
**Ícone:** `chair` | **Cor:** `#795548` (Marrom)

- **Cadeira** (1.704 itens) - R$ 240 médio
- **Mesa** (511 itens) - R$ 1.210 médio
- **Conjunto Escolar** (160 itens) - R$ 113 médio
- **Armário/Estante** (309 itens) - R$ 1.264 médio
- **Poltrona** (115 itens) - R$ 728 médio
- **Banqueta** (138 itens) - R$ 174 médio
- **Outros**

### 2. INFORMÁTICA (1.672 itens - R$ 9,6M)
**Ícone:** `computer` | **Cor:** `#2196F3` (Azul)

- **Desktop** (372 itens) - R$ 2.642 médio
- **Notebook** (147 itens) - R$ 2.665 médio
- **Monitor** (988 itens) - R$ 123 médio
- **Equipamento de Rede** (79 itens) - R$ 3.282 médio
- **Impressora/Scanner** (31 itens) - R$ 2.382 médio
- **Webcam** (55 itens) - R$ 590 médio
- **Periféricos**
- **Outros**

### 3. AUDIOVISUAL (222 itens - R$ 700K)
**Ícone:** `videocam` | **Cor:** `#9C27B0` (Roxo)

- **Projetor** (55 itens) - R$ 2.885 médio
- **Tela de Projeção** (68 itens) - R$ 1.026 médio
- **Quadro/Lousa** (99 itens) - R$ 3.177 médio
- **Caixa de Som**
- **Outros**

### 4. LABORATÓRIO (47+ itens)
**Ícone:** `science` | **Cor:** `#4CAF50` (Verde)

- **Microscópio** (47 itens) - R$ 2.125 médio
- **Instrumento de Medição** (30+ itens)
- **Vidraria**
- **Equipamento Específico**
- **Outros**

### 5. CLIMATIZAÇÃO (113+ itens - R$ 966K)
**Ícone:** `ac_unit` | **Cor:** `#00BCD4` (Ciano)

- **Ar Condicionado** (113 itens) - R$ 8.473 médio
- **Ventilador**
- **Outros**

### 6. ACERVO (3.962 itens - R$ 346K)
**Ícone:** `book` | **Cor:** `#FF9800` (Laranja)

- **Livro** (3.962 itens) - R$ 87 médio
- **Dicionário**
- **Revista**
- **Outros**

### 7. VEÍCULO (155 itens - R$ 2M)
**Ícone:** `directions_car` | **Cor:** `#F44336` (Vermelho)

- **Veículo Leve** - R$ 13.182 médio
- **Veículo Pesado**
- **Outros**

### 8. ELETRODOMÉSTICO (121 itens - R$ 966K)
**Ícone:** `kitchen` | **Cor:** `#607D8B` (Cinza Azulado)

- **Refrigeração**
- **Aquecimento**
- **Outros**

### 9. OUTROS (2.456 itens - R$ 6,2M)
**Ícone:** `category` | **Cor:** `#9E9E9E` (Cinza)

- **Sem Subcategoria**

---

## 🚀 Como Usar

### 1. Criar Tabelas no PostgreSQL

```powershell
.\executar-categorias-postgresql.ps1
```

**Ou manualmente:**
```bash
psql -h localhost -U inventario -d sispatrimonio -f sql/criar_tabelas_categoria_patrimonio.sql
```

### 2. Criar Tabelas no SQLite (Offline)

```powershell
.\executar-categorias-sqlite.ps1
```

**Ou manualmente:**
```bash
sqlite3 data/inventario_offline.db < sql/criar_tabelas_categoria_patrimonio_sqlite.sql
```

---

## 📱 Integração no App Android

### 1. Criar Entities Room

```kotlin
@Entity(tableName = "tabela_categoria_patrimonio")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val descricao: String?,
    val icone: String?,
    val cor: String?,
    val ordem_exibicao: Int = 0,
    val ativo: Boolean = true,
    val data_criacao: String?,
    val data_atualizacao: String?
)

@Entity(
    tableName = "tabela_subcategoria_patrimonio",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_categoria"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("id_categoria")]
)
data class SubcategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val id_categoria: Int,
    val nome: String,
    val descricao: String?,
    val ordem_exibicao: Int = 0,
    val ativo: Boolean = true,
    val data_criacao: String?,
    val data_atualizacao: String?
)
```

### 2. Criar DAOs

```kotlin
@Dao
interface CategoriaDao {
    
    @Query("SELECT * FROM tabela_categoria_patrimonio WHERE ativo = 1 ORDER BY ordem_exibicao")
    suspend fun buscarTodasAtivas(): List<CategoriaEntity>
    
    @Query("SELECT * FROM tabela_categoria_patrimonio WHERE id = :id")
    suspend fun buscarPorId(id: Int): CategoriaEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(categoria: CategoriaEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(categorias: List<CategoriaEntity>)
}

@Dao
interface SubcategoriaDao {
    
    @Query("SELECT * FROM tabela_subcategoria_patrimonio WHERE id_categoria = :idCategoria AND ativo = 1 ORDER BY ordem_exibicao")
    suspend fun buscarPorCategoria(idCategoria: Int): List<SubcategoriaEntity>
    
    @Query("""
        SELECT s.* FROM tabela_subcategoria_patrimonio s
        INNER JOIN tabela_categoria_patrimonio c ON s.id_categoria = c.id
        WHERE c.ativo = 1 AND s.ativo = 1
        ORDER BY c.ordem_exibicao, s.ordem_exibicao
    """)
    suspend fun buscarTodasAtivas(): List<SubcategoriaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTodas(subcategorias: List<SubcategoriaEntity>)
}
```

### 3. Criar Domain Models

```kotlin
data class Categoria(
    val id: Int,
    val nome: String,
    val descricao: String?,
    val icone: String?,
    val cor: String?,
    val subcategorias: List<Subcategoria> = emptyList()
)

data class Subcategoria(
    val id: Int,
    val idCategoria: Int,
    val nome: String,
    val descricao: String?
)
```

### 4. Usar na UI

```kotlin
// Spinner de Categorias
@Composable
fun CategoriaSpinner(
    categorias: List<Categoria>,
    onCategoriaSelected: (Categoria) -> Unit
) {
    LazyColumn {
        items(categorias) { categoria ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoriaSelected(categoria) }
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.getIcon(categoria.icone),
                    contentDescription = null,
                    tint = Color(android.graphics.Color.parseColor(categoria.cor))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = categoria.nome)
            }
        }
    }
}
```

---

## 📊 Views Úteis

### 1. `view_categorias_com_contagem`
Lista categorias com total de subcategorias

```sql
SELECT * FROM view_categorias_com_contagem;
```

### 2. `view_categorias_subcategorias`
Lista completa hierárquica

```sql
SELECT * FROM view_categorias_subcategorias;
```

---

## 🔧 Manutenção

### Adicionar Nova Categoria

```sql
INSERT INTO tabela_categoria_patrimonio (nome, descricao, icone, cor, ordem_exibicao)
VALUES ('NOVA_CATEGORIA', 'Descrição', 'icone', '#HEXCOLOR', 10);
```

### Adicionar Nova Subcategoria

```sql
INSERT INTO tabela_subcategoria_patrimonio (id_categoria, nome, descricao, ordem_exibicao)
VALUES (1, 'Nova Subcategoria', 'Descrição', 10);
```

### Desativar Categoria

```sql
UPDATE tabela_categoria_patrimonio SET ativo = FALSE WHERE id = 1;
```

### Reordenar Categorias

```sql
UPDATE tabela_categoria_patrimonio SET ordem_exibicao = 5 WHERE id = 1;
```

---

## 📈 Benefícios Esperados

### Organização
- ✅ Redução de 63% para <10% de itens em "OUTROS"
- ✅ Hierarquia clara de 2 níveis
- ✅ Fácil expansão futura

### Performance
- ✅ Índices otimizados
- ✅ Queries rápidas
- ✅ Cache possível

### UX
- ✅ Filtros por categoria/subcategoria
- ✅ Ícones e cores visuais
- ✅ Busca mais precisa
- ✅ Coleta sem etiqueta facilitada

### Relatórios
- ✅ Estatísticas por categoria
- ✅ Valor total por tipo
- ✅ Análises detalhadas

---

## 🧪 Testes

### Verificar Instalação

```sql
-- PostgreSQL
SELECT COUNT(*) FROM tabela_categoria_patrimonio;
SELECT COUNT(*) FROM tabela_subcategoria_patrimonio;

-- SQLite
sqlite3 data/inventario_offline.db "SELECT COUNT(*) FROM tabela_categoria_patrimonio;"
```

### Consultar Hierarquia

```sql
SELECT 
    c.nome as categoria,
    s.nome as subcategoria,
    c.cor,
    c.icone
FROM tabela_categoria_patrimonio c
LEFT JOIN tabela_subcategoria_patrimonio s ON c.id = s.id_categoria
WHERE c.ativo = TRUE
ORDER BY c.ordem_exibicao, s.ordem_exibicao;
```

---

## 📝 Próximos Passos

1. ✅ Criar tabelas (CONCLUÍDO)
2. ⏳ Integrar no app Android
3. ⏳ Criar endpoints REST
4. ⏳ Adicionar filtros na UI
5. ⏳ Migrar patrimônios existentes
6. ⏳ Criar relatórios por categoria

---

## 🆘 Troubleshooting

### Erro: "relation already exists"
**Solução:** Tabelas já existem. Use `DROP TABLE` se quiser recriar.

### Erro: "psql: command not found"
**Solução:** Adicione PostgreSQL ao PATH do sistema.

### Erro: "sqlite3: command not found"
**Solução:** Instale SQLite ou adicione ao PATH.

### Erro: "permission denied"
**Solução:** Execute como administrador ou ajuste permissões.

---

**Documentação criada em:** 27/11/2025  
**Versão:** 1.0.0  
**Status:** ✅ Completo
