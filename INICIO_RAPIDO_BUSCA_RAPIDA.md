# 🚀 Início Rápido - Implementação Busca Rápida

## 📋 Pré-requisitos

- ✅ Clean Architecture estruturada
- ✅ Hilt configurado
- ✅ Room Database funcionando
- ✅ PatrimonioDao existente

---

## 🎯 Passo a Passo

### 1️⃣ Criar Models (5 min)

```bash
# Criar arquivos
touch domain/model/SearchFilter.kt
touch domain/model/SearchCriteria.kt
```

### 2️⃣ Criar Repository Interface (5 min)

```bash
touch domain/repository/SearchRepository.kt
```

### 3️⃣ Criar Use Case (10 min)

```bash
touch domain/usecase/BuscarPatrimoniosUseCase.kt
```

### 4️⃣ Adicionar Query no DAO (10 min)

Editar: `data/local/dao/PatrimonioDao.kt`

### 5️⃣ Implementar Repository (15 min)

```bash
touch data/repository/SearchRepositoryImpl.kt
```

### 6️⃣ Criar State e ViewModel (20 min)

```bash
touch presentation/search/SearchState.kt
touch presentation/search/QuickSearchViewModel.kt
```

### 7️⃣ Refatorar Activity (30 min)

Editar: `presentation/search/QuickSearchActivity.kt`

### 8️⃣ Testar (15 min)

```bash
# Compilar e instalar
cd InventarioMobile
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

---

## ⏱️ Tempo Total Estimado

**1h 50min** para implementação completa

---

## 🎯 Ordem de Implementação

```
1. Models (SearchFilter, SearchCriteria)
   ↓
2. Repository Interface
   ↓
3. Use Case
   ↓
4. DAO Query
   ↓
5. Repository Implementation
   ↓
6. State + ViewModel
   ↓
7. Activity (UI)
   ↓
8. Testes
```

---

## 📝 Comandos Úteis

```bash
# Compilar
.\gradlew.bat assembleDebug

# Instalar
.\gradlew.bat installDebug

# Ver logs
adb logcat -s QuickSearchViewModel:D

# Limpar build
.\gradlew.bat clean
```

---

## ✅ Checklist Rápido

- [ ] Models criados
- [ ] Repository interface criada
- [ ] Use Case criado
- [ ] Query adicionada no DAO
- [ ] Repository implementado
- [ ] State criado
- [ ] ViewModel criado
- [ ] Activity refatorada
- [ ] Hilt configurado
- [ ] Testado no emulador

---

**Pronto para começar!** 🚀
