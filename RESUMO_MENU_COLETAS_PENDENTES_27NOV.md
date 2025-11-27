# Melhorias no App Android - 27/11/2025

## ✅ 1. Menu de Contexto para Coletas Pendentes (v2.8)

Na tela de **Itens Coletados** (`CollectionViewActivity`), ao pressionar e segurar uma coleta **pendente** (não sincronizada), aparece um menu com opções:

1. **🔄 Reenviar Coleta** - Tenta sincronizar a coleta novamente
2. **🗑️ Excluir Coleta** - Remove a coleta pendente do dispositivo

### Arquivos Modificados
- `CollectionViewActivity.kt` - Menu de contexto com dialogs de confirmação
- `menu_coleta_pendente.xml` - Novo arquivo de menu
- `ColetaRepositoryImpl.kt` - Corrigido `getColetaById()`

---

## ✅ 2. Coleta por Descrição - Múltiplos Itens (v2.9)

### Problema
A coleta por descrição estava bloqueada pela validação de duplicata.

### Solução
Ajustada a validação para **ignorar duplicatas** quando é coleta por descrição:

| Tipo de Coleta | Permite Duplicata? |
|----------------|-------------------|
| Por número de patrimônio | ❌ NÃO |
| Por QR Code | ❌ NÃO |
| Por descrição | ✅ SIM |

### Arquivo Modificado
- `ColetaRepositoryImpl.kt` - Fase 2 (Verificar Duplicata)

---

## ✅ 3. Sala Fixada - Coleta Rápida (v2.9)

### Funcionalidade
Permite **fixar uma sala** para não precisar procurá-la toda vez que voltar à tela de seleção.

### Como Usar
1. Na tela de seleção de salas, **pressione e segure** uma sala
2. Selecione **"📌 Fixar esta sala"**
3. Um banner aparece no topo com a sala fixada
4. Clique em **"Usar"** para ir direto para coleta
5. Para remover, clique no **X** ou pressione a sala novamente

### Arquivos Modificados/Criados
- `PreferencesManager.kt` - Métodos `setSalaFixada()`, `getSalaFixadaId()`, `clearSalaFixada()`
- `SalaSelectionActivity.kt` - Lógica de sala fixada e menu de contexto
- `SalaAdapter.kt` - Suporte a long click
- `activity_sala_selection.xml` - Banner de sala fixada

### Fluxo
```
1. Usuário pressiona e segura uma sala
2. Menu aparece: "📌 Fixar esta sala" ou "📌 Remover fixação"
3. Se fixar: banner aparece no topo
4. Próxima vez que abrir: sala fixada já está disponível
5. Clique em "Usar" → vai direto para coleta
```

---

## Build

```bash
cd InventarioMobile
.\gradlew.bat assembleDebug
```

**Status:** ✅ BUILD SUCCESSFUL

**Data:** 27/11/2025
**Versão:** 2.9
