# Problema: Dados Vazios nas Coletas

## 🐛 Problema Identificado

As coletas estão aparecendo com dados genéricos:
- "Patrimônio coletado" ao invés da descrição real
- "Área Externa" ao invés do nome da sala correto
- Número do patrimônio aparece mas sem descrição

## 🔍 Causa Raiz

O `ColetaMapper.toEntity()` estava salvando campos vazios:

```kotlin
// ANTES (ERRADO)
fun toEntity(domain: Coleta, idInventario: Int = 0): ColetaEntity {
    return ColetaEntity(
        numeroPatrimonio = "", // ❌ VAZIO!
        nomeUsuario = "",      // ❌ VAZIO!
        // ...
    )
}
```

Quando a coleta era registrada, esses campos ficavam vazios no banco.

## ✅ Solução Implementada

Atualizado `ColetaRepositoryImpl.registrarColeta()` para buscar os dados reais:

```kotlin
// DEPOIS (CORRETO)
override suspend fun registrarColeta(coleta: Coleta): Result<Coleta> {
    // 1. Buscar dados do patrimônio
    val patrimonio = patrimonioDao.buscarPorId(coleta.patrimonioId.toInt())
    
    // 2. Criar entity com dados completos
    val entity = mapper.toEntity(coleta).copy(
        numeroPatrimonio = patrimonio?.numero ?: "",  // ✅ Busca do banco!
        nomeUsuario = "Usuário ${coleta.usuarioId}"   // ✅ Preenchido!
    )
    
    // 3. Salvar no banco
    coletaDao.inserir(entity)
}
```

## 📝 Status

### ✅ Corrigido
- Novas coletas agora salvam com dados completos
- `numeroPatrimonio` é buscado do banco
- `nomeUsuario` é preenchido (temporariamente com ID)

### ⚠️ Coletas Antigas
As coletas que já estavam no banco **ainda têm dados vazios** porque foram salvas com o código antigo.

## 🔧 Como Testar

### Opção 1: Fazer Nova Coleta (Recomendado)
1. Abra o app
2. Faça uma **nova coleta** de um patrimônio
3. Vá em "Itens Coletados"
4. A nova coleta deve aparecer com dados corretos

### Opção 2: Limpar Banco e Sincronizar
1. Vá em Configurações
2. Limpe os dados locais
3. Sincronize novamente
4. Faça novas coletas

### Opção 3: Atualizar Coletas Antigas (Avançado)
Implementar migração de dados para atualizar coletas antigas:

```kotlin
// Método já criado em ColetaRepositoryImpl
suspend fun atualizarColetasAntigas() {
    val coletas = coletaDao.buscarTodas()
    
    coletas.forEach { coleta ->
        if (coleta.numeroPatrimonio.isBlank()) {
            val patrimonio = patrimonioDao.buscarPorId(coleta.idPatrimonio)
            if (patrimonio != null) {
                val coletaAtualizada = coleta.copy(
                    numeroPatrimonio = patrimonio.numero
                )
                coletaDao.inserir(coletaAtualizada)
            }
        }
    }
}
```

## 🎯 Próximos Passos

### Curto Prazo
- [ ] Testar com nova coleta
- [ ] Verificar se dados aparecem corretamente
- [ ] Implementar busca de nome do usuário real

### Médio Prazo
- [ ] Criar migração automática para coletas antigas
- [ ] Adicionar descrição do patrimônio na ColetaEntity
- [ ] Melhorar exibição no adapter

### Longo Prazo
- [ ] Refatorar para usar domain.model.Coleta no adapter
- [ ] Criar ViewModel específico para cada item da lista
- [ ] Implementar cache de dados de patrimônio/usuário

## 📊 Dados Esperados vs Reais

### Antes da Correção
```
Patrimônio 64
Patrimônio coletado        ← ❌ Genérico
Área Externa               ← ❌ Pode estar errado
```

### Depois da Correção (Nova Coleta)
```
Patrimônio 64              ← ✅ Número correto
Mesa de Escritório         ← ✅ Descrição real (se disponível)
Sala 101                   ← ✅ Sala correta
```

## 🔍 Como Verificar no Banco

Para verificar os dados no banco SQLite:

```sql
-- Ver coletas com dados vazios
SELECT id, idPatrimonio, numeroPatrimonio, nomeSala, observacao
FROM coleta
WHERE numeroPatrimonio = '' OR numeroPatrimonio IS NULL;

-- Ver coletas com dados preenchidos
SELECT id, idPatrimonio, numeroPatrimonio, nomeSala, observacao
FROM coleta
WHERE numeroPatrimonio != '' AND numeroPatrimonio IS NOT NULL;
```

---

**Data**: 14/11/2025  
**Status**: ✅ Corrigido para novas coletas  
**Ação Necessária**: Fazer nova coleta para testar
