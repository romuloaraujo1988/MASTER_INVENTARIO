# Integração de Validação no App Android - Guia Completo

## ✅ Implementação Concluída

### **Componentes Criados:**

1. ✅ **APIs Kotlin**
   - `PatrimonioApi.kt` - 2 novos endpoints
   - `ColetaApi.kt` - 1 novo endpoint + DTO

2. ✅ **Use Cases**
   - `ValidarPatrimonioUseCase.kt`
   - `VerificarDuplicataColetaUseCase.kt`
   - `VerificarSePatrimonioFoiColetadoUseCase.kt`

3. ✅ **ViewModel**
   - `ValidationViewModel.kt` - Gerencia todos os estados

4. ✅ **Exemplo de UI**
   - `ValidationExampleFragment.kt` - Template de implementação

---

## 🚀 Como Integrar nas Telas Existentes

### Opção 1: Integrar no ColetaActivity

```kotlin
@AndroidEntryPoint
class ColetaActivity : AppCompatActivity() {
    
    private val validationViewModel: ValidationViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupValidationObserver()
    }
    
    private fun setupValidationObserver() {
        lifecycleScope.launch {
            validationViewModel.validationState.collect { state ->
                when (state) {
                    is ValidationState.Valid -> {
                        // Patrimônio válido, permitir coleta
                        enableCollectionButton()
                        if (state.jaColetado) {
                            showWarning("Já foi coletado anteriormente")
                        }
                    }
                    is ValidationState.Invalid -> {
                        // Patrimônio inválido
                        disableCollectionButton()
                        showError(state.mensagem)
                    }
                    // ... outros estados
                }
            }
        }
    }
    
    private fun onQRCodeScanned(code: String) {
        // Validar antes de mostrar dados
        validationViewModel.validarPatrimonio(code)
    }
}
```


### Opção 2: Integrar no ManualCollectionActivity

```kotlin
@AndroidEntryPoint
class ManualCollectionActivity : AppCompatActivity() {
    
    private val validationViewModel: ValidationViewModel by viewModels()
    
    private fun onRegisterButtonClick() {
        val numeroPatrimonio = edtNumero.text.toString()
        
        // Verificar duplicata antes de registrar
        validationViewModel.verificarDuplicata(numeroPatrimonio)
    }
    
    private fun setupObserver() {
        lifecycleScope.launch {
            validationViewModel.validationState.collect { state ->
                when (state) {
                    is ValidationState.PodeRegistrar -> {
                        // Não é duplicata, registrar
                        registrarColeta()
                    }
                    is ValidationState.Duplicado -> {
                        // Mostrar dialog de confirmação
                        showDuplicateDialog(state.coletaExistente)
                    }
                }
            }
        }
    }
}
```

---

## 📱 Fluxos de Uso Recomendados

### Fluxo 1: Scanner QR Code
```
1. Usuário escaneia QR Code
2. App chama: validationViewModel.validarPatrimonio(code)
3. Observer recebe ValidationState.Valid ou Invalid
4. Se válido: mostrar dados e habilitar botão
5. Se inválido: mostrar erro e desabilitar botão
```

### Fluxo 2: Coleta Manual
```
1. Usuário digita número
2. App chama: validationViewModel.validarPatrimonio(numero)
3. Se válido: mostrar preview dos dados
4. Usuário clica "Registrar"
5. App chama: validationViewModel.verificarDuplicata(numero)
6. Se não duplicado: registrar
7. Se duplicado: mostrar dialog de confirmação
```

### Fluxo 3: Consulta Rápida
```
1. Usuário quer verificar status
2. App chama: validationViewModel.verificarSeJaFoiColetado(numero)
3. Observer recebe JaColetado ou AindaNaoColetado
4. Mostrar informações detalhadas
```


---

## 🎨 Exemplos de UI

### Mostrar Aviso de Duplicata

```kotlin
private fun showDuplicateDialog(coletaExistente: Map<String, Any>?) {
    val coletadoPor = coletaExistente?.get("coletadoPor") as? String
    val dataColeta = coletaExistente?.get("dataColeta") as? String
    
    AlertDialog.Builder(this)
        .setTitle("⚠️ Coleta Duplicada")
        .setMessage("""
            Este patrimônio já foi coletado:
            
            Por: $coletadoPor
            Em: $dataColeta
            
            Deseja registrar novamente?
        """.trimIndent())
        .setPositiveButton("Sim, Registrar") { _, _ ->
            registrarColeta(forceDuplicate = true)
        }
        .setNegativeButton("Cancelar", null)
        .show()
}
```

### Mostrar Status de Coleta

```kotlin
private fun showColetaStatus(info: ColetaInfo.Coletado) {
    binding.cardStatus.visibility = View.VISIBLE
    binding.tvStatus.text = "✅ JÁ COLETADO"
    binding.tvColetadoPor.text = "Por: ${info.coletadoPor}"
    binding.tvDataColeta.text = "Em: ${info.dataColeta}"
    binding.tvLocalizacao.text = "Local: ${info.localizacaoEncontrada}"
    binding.tvEstado.text = "Estado: ${info.estadoEncontrado}"
}
```

### Validação em Tempo Real

```kotlin
private fun setupRealtimeValidation() {
    edtNumero.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val numero = s.toString()
            if (numero.length >= 5) { // Validar após 5 caracteres
                validationViewModel.validarPatrimonio(numero)
            }
        }
    })
}
```


---

## 🧪 Testes Recomendados

### Teste 1: Validação de Patrimônio Válido
```kotlin
@Test
fun `validar patrimonio valido deve retornar Valid`() = runTest {
    // Given
    val numeroPatrimonio = "12345"
    
    // When
    viewModel.validarPatrimonio(numeroPatrimonio)
    
    // Then
    val state = viewModel.validationState.value
    assertTrue(state is ValidationState.Valid)
}
```

### Teste 2: Detecção de Duplicata
```kotlin
@Test
fun `verificar duplicata deve retornar Duplicado quando ja coletado`() = runTest {
    // Given
    val numeroPatrimonio = "12345"
    
    // When
    viewModel.verificarDuplicata(numeroPatrimonio)
    
    // Then
    val state = viewModel.validationState.value
    assertTrue(state is ValidationState.Duplicado)
}
```

---

## 📊 Métricas de Sucesso

- ✅ Redução de 95%+ em coletas duplicadas
- ✅ Tempo de validação < 200ms
- ✅ Taxa de erro < 1%
- ✅ Feedback positivo dos usuários

---

## 🔧 Troubleshooting

### Problema: Endpoint não encontrado (404)
**Solução:** Verificar se o servidor está rodando e a URL base está correta

### Problema: Timeout na validação
**Solução:** Aumentar timeout do Retrofit ou verificar conexão

### Problema: Estado não atualiza na UI
**Solução:** Verificar se está usando lifecycleScope.launch e collectAsState

---

**Implementado em:** 15/11/2025
**Versão:** 1.0.0
**Status:** ✅ Pronto para integração
