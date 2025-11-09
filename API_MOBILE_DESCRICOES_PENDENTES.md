# 📱 API Mobile - Descrições Pendentes

## Endpoint: Buscar Descrições Pendentes

### `GET /api/mobile/coletas/descricoes-pendentes`

Retorna apenas as descrições de patrimônios que ainda **não foram coletados** no inventário ativo.

---

## Parâmetros

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `termoBusca` | String | ✅ Sim | Termo para buscar na descrição (ex: "CADEIRA") |
| `idInventario` | Integer | ❌ Não | ID do inventário (se omitido, usa o ativo) |

---

## Headers

```http
Authorization: Bearer {token_jwt}
Content-Type: application/json
```

---

## Resposta de Sucesso

### Status: `200 OK`

```json
{
  "success": true,
  "message": "Descrições pendentes carregadas",
  "data": {
    "descricoes": [
      {
        "descricao": "CADEIRA GIRATÓRIA PRETA",
        "quantidadePendente": 15
      },
      {
        "descricao": "CADEIRA FIXA AZUL",
        "quantidadePendente": 12
      },
      {
        "descricao": "CADEIRA ESCRITÓRIO ERGONÔMICA",
        "quantidadePendente": 8
      }
    ],
    "total": 3,
    "totalPatrimoniosPendentes": 35,
    "estatisticas": {
      "total": 100,
      "pendentes": 35,
      "coletados": 65,
      "percentualColetado": 65.0,
      "percentualPendente": 35.0
    },
    "mensagem": "Encontradas 3 descrição(ões) com 35 patrimônio(s) pendente(s)"
  },
  "timestamp": "2025-11-08T23:30:00"
}
```

---

## Respostas de Erro

### 400 Bad Request - Termo Vazio

```json
{
  "success": false,
  "message": "Termo de busca não pode ser vazio",
  "error": "INVALID_PARAMS",
  "timestamp": "2025-11-08T23:30:00"
}
```

### 400 Bad Request - Sem Inventário Ativo

```json
{
  "success": false,
  "message": "Nenhum inventário ativo encontrado",
  "error": "INVALID_PARAMS",
  "timestamp": "2025-11-08T23:30:00"
}
```

### 401 Unauthorized - Token Inválido

```json
{
  "success": false,
  "message": "Token JWT inválido ou expirado",
  "error": "UNAUTHORIZED",
  "timestamp": "2025-11-08T23:30:00"
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "Erro ao buscar descrições pendentes: ...",
  "error": "FETCH_ERROR",
  "timestamp": "2025-11-08T23:30:00"
}
```

---

## Exemplos de Uso

### cURL

```bash
curl -X GET "http://localhost:8080/api/mobile/coletas/descricoes-pendentes?termoBusca=CADEIRA" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### JavaScript (Fetch API)

```javascript
const token = localStorage.getItem('token');

fetch('http://localhost:8080/api/mobile/coletas/descricoes-pendentes?termoBusca=CADEIRA', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Descrições pendentes:', data.data.descricoes);
    console.log('Total pendentes:', data.data.totalPatrimoniosPendentes);
    console.log('Estatísticas:', data.data.estatisticas);
  }
})
.catch(error => console.error('Erro:', error));
```

### Kotlin (Android - Retrofit)

```kotlin
// Interface do serviço
interface ColetaApiService {
    @GET("coletas/descricoes-pendentes")
    suspend fun buscarDescricoesPendentes(
        @Query("termoBusca") termoBusca: String,
        @Query("idInventario") idInventario: Int? = null
    ): ApiResponse<DescricoesPendentesResponse>
}

// Uso no ViewModel
viewModelScope.launch {
    try {
        val response = coletaService.buscarDescricoesPendentes("CADEIRA")
        if (response.success) {
            _descricoesPendentes.value = response.data.descricoes
            _estatisticas.value = response.data.estatisticas
        }
    } catch (e: Exception) {
        _erro.value = "Erro ao buscar descrições: ${e.message}"
    }
}
```

### Kotlin (Android - Coroutines)

```kotlin
suspend fun buscarDescricoesPendentes(termo: String): Result<DescricoesPendentesResponse> {
    return withContext(Dispatchers.IO) {
        try {
            val response = coletaService.buscarDescricoesPendentes(termo)
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Fluxo de Uso no App Android

### 1. Tela de Coleta Sem Etiqueta

```kotlin
// ColetaSemEtiquetaFragment.kt

private fun buscarDescricoes() {
    val termo = binding.edtBusca.text.toString()
    
    if (termo.isBlank()) {
        Toast.makeText(context, "Digite um termo para buscar", Toast.LENGTH_SHORT).show()
        return
    }
    
    viewModel.buscarDescricoesPendentes(termo)
}

// Observar resultado
viewModel.descricoesPendentes.observe(viewLifecycleOwner) { descricoes ->
    if (descricoes.isEmpty()) {
        binding.tvMensagem.text = "✅ Todos os itens já foram coletados!"
        binding.tvMensagem.visibility = View.VISIBLE
        binding.recyclerDescricoes.visibility = View.GONE
    } else {
        binding.tvMensagem.visibility = View.GONE
        binding.recyclerDescricoes.visibility = View.VISIBLE
        adapter.submitList(descricoes)
    }
}

// Observar estatísticas
viewModel.estatisticas.observe(viewLifecycleOwner) { stats ->
    binding.tvProgresso.text = String.format(
        "Progresso: %d/%d (%.1f%%)",
        stats.coletados,
        stats.total,
        stats.percentualColetado
    )
}
```

### 2. Adapter do RecyclerView

```kotlin
class DescricaoPendenteAdapter : ListAdapter<DescricaoPendente, ViewHolder>(DiffCallback()) {
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
    
    inner class ViewHolder(private val binding: ItemDescricaoPendenteBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(descricao: DescricaoPendente) {
            binding.tvDescricao.text = descricao.descricao
            binding.tvQuantidade.text = "${descricao.quantidadePendente} pendente(s)"
            
            binding.root.setOnClickListener {
                onItemClick(descricao)
            }
        }
    }
}
```

### 3. ViewModel

```kotlin
class ColetaSemEtiquetaViewModel(
    private val repository: ColetaRepository
) : ViewModel() {
    
    private val _descricoesPendentes = MutableLiveData<List<DescricaoPendente>>()
    val descricoesPendentes: LiveData<List<DescricaoPendente>> = _descricoesPendentes
    
    private val _estatisticas = MutableLiveData<EstatisticasColeta>()
    val estatisticas: LiveData<EstatisticasColeta> = _estatisticas
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    fun buscarDescricoesPendentes(termo: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.buscarDescricoesPendentes(termo)
                _descricoesPendentes.value = response.descricoes
                _estatisticas.value = response.estatisticas
            } catch (e: Exception) {
                // Tratar erro
                Log.e("ViewModel", "Erro ao buscar descrições", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
```

---

## Models (Data Classes)

```kotlin
data class DescricoesPendentesResponse(
    val descricoes: List<DescricaoPendente>,
    val total: Int,
    val totalPatrimoniosPendentes: Int,
    val estatisticas: EstatisticasColeta,
    val mensagem: String
)

data class DescricaoPendente(
    val descricao: String,
    val quantidadePendente: Int
)

data class EstatisticasColeta(
    val total: Int,
    val pendentes: Int,
    val coletados: Int,
    val percentualColetado: Double,
    val percentualPendente: Double
)
```

---

## Benefícios

### Para o Coletor Mobile
✅ **Foco**: Vê apenas o que precisa coletar  
✅ **Eficiência**: Não perde tempo com itens já coletados  
✅ **Progresso**: Acompanha estatísticas em tempo real  
✅ **Offline**: Pode cachear lista de pendentes  

### Para o Sistema
✅ **Consistência**: Mesma lógica do desktop  
✅ **Performance**: Filtro no servidor (não no app)  
✅ **Escalável**: Funciona com muitos patrimônios  
✅ **Manutenível**: Código reutilizável  

---

## Testes

### Teste 1: Busca com Resultados
```bash
GET /api/mobile/coletas/descricoes-pendentes?termoBusca=CADEIRA
# Deve retornar lista de descrições pendentes
```

### Teste 2: Busca Sem Resultados (Todos Coletados)
```bash
GET /api/mobile/coletas/descricoes-pendentes?termoBusca=MESA
# Deve retornar lista vazia com mensagem apropriada
```

### Teste 3: Termo Vazio
```bash
GET /api/mobile/coletas/descricoes-pendentes?termoBusca=
# Deve retornar 400 Bad Request
```

### Teste 4: Sem Inventário Ativo
```bash
GET /api/mobile/coletas/descricoes-pendentes?termoBusca=CADEIRA
# (quando não há inventário ativo)
# Deve retornar 400 Bad Request
```

---

**Versão**: 2.0.0  
**Data**: 08/11/2025  
**Endpoint**: `/api/mobile/coletas/descricoes-pendentes`
