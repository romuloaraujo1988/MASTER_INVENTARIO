# Plano: Modo Offline Automático - Detecção e Fallback Inteligente

**Data:** 22/11/2025  
**Objetivo:** App deve detectar automaticamente falta de conexão e usar dados locais sem intervenção do usuário

---

## 🎯 Visão Geral

### Problema Atual
- ✅ App já tem dados locais (Room Database)
- ✅ App já tem estratégias offline-first
- ❌ Usuário precisa saber quando está offline
- ❌ Algumas telas ainda tentam servidor mesmo offline
- ❌ Não há indicador visual de modo offline
- ❌ Erros de rede aparecem como erros genéricos

### Solução Proposta
- ✅ Detecção automática de conectividade
- ✅ Fallback transparente para dados locais
- ✅ Indicador visual de modo offline
- ✅ Mensagens contextuais sobre limitações offline
- ✅ Sincronização automática quando conexão voltar

---

## 📊 Análise de Impacto

### Telas Afetadas (Prioridade Alta)

#### 1. **SalaSelectionActivity** ⚠️ CRÍTICO
**Impacto:** ALTO - Primeira tela após login
**Status Atual:**
- Tenta carregar salas do servidor
- Falha se servidor offline
- Não usa dados locais automaticamente

**Mudanças Necessárias:**
- ✅ Detectar conectividade antes de carregar
- ✅ Usar dados locais se offline
- ✅ Mostrar indicador "Modo Offline"
- ✅ Avisar sobre limitações (dados podem estar desatualizados)

#### 2. **DashboardFragment** ⚠️ CRÍTICO
**Impacto:** ALTO - Tela principal de navegação
**Status Atual:**
- Carrega estatísticas do servidor
- Busca por voz pode falhar offline

**Mudanças Necessárias:**
- ✅ Estatísticas baseadas em dados locais
- ✅ Busca por voz funciona com dados locais
- ✅ Indicador de modo offline
- ✅ Botão "Sincronizar" quando online

#### 3. **ColetaActivity** ⚠️ CRÍTICO
**Impacto:** ALTO - Tela de coleta principal
**Status Atual:**
- ✅ Já salva coletas localmente
- ⚠️ Busca de patrimônio já tem fallback (implementado hoje)
- ⚠️ Validações podem tentar servidor

**Mudanças Necessárias:**
- ✅ Garantir que todas validações funcionem offline
- ✅ Indicador de "Coleta Offline"
- ✅ Avisar que sincronização será feita depois

#### 4. **ManualCollectionActivity** ⚠️ CRÍTICO
**Impacto:** ALTO - Coleta manual
**Status Atual:**
- ✅ Busca de patrimônio já tem fallback
- ⚠️ Pode tentar validações no servidor

**Mudanças Necessárias:**
- ✅ Validações offline
- ✅ Indicador de modo offline

#### 5. **CollectionViewActivity** ⚠️ MÉDIO
**Impacto:** MÉDIO - Visualização de coletas
**Status Atual:**
- Carrega coletas do servidor
- Não mostra coletas locais pendentes

**Mudanças Necessárias:**
- ✅ Mostrar coletas locais
- ✅ Indicar quais estão sincronizadas
- ✅ Indicar quais estão pendentes

#### 6. **SyncActivity** ⚠️ MÉDIO
**Impacto:** MÉDIO - Sincronização
**Status Atual:**
- ✅ Já implementada com Clean Architecture
- ✅ Já tem WorkManager para background sync

**Mudanças Necessárias:**
- ✅ Desabilitar sync quando offline
- ✅ Mostrar status de conectividade
- ✅ Agendar sync automático quando conexão voltar

---

## 🏗️ Arquitetura da Solução

### 1. **ConnectivityManager** (Novo Componente)

**Responsabilidade:** Detectar e monitorar conectividade

```kotlin
// util/ConnectivityManager.kt
class ConnectivityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as android.net.ConnectivityManager
    
    init {
        registerNetworkCallback()
    }
    
    /**
     * Verifica conectividade atual
     */
    fun checkConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }
    
    /**
     * Verifica se servidor está acessível
     */
    suspend fun isServerReachable(): Boolean {
        return try {
            withTimeout(3000) {
                // Ping no servidor
                val url = URL("${BuildConfig.BASE_URL}/api/mobile/health")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.requestMethod = "GET"
                
                val responseCode = connection.responseCode
                connection.disconnect()
                
                responseCode == 200
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Registra callback para mudanças de rede
     */
    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }
                
                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            }
        )
    }
}
```

---

### 2. **OfflineIndicatorView** (Novo Componente UI)

**Responsabilidade:** Indicador visual de modo offline

```kotlin
// presentation/components/OfflineIndicatorView.kt
class OfflineIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    
    private val binding: ViewOfflineIndicatorBinding
    
    init {
        binding = ViewOfflineIndicatorBinding.inflate(
            LayoutInflater.from(context), this, true
        )
        
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.parseColor("#FFA500")) // Laranja
        setPadding(16, 8, 16, 8)
        visibility = GONE
    }
    
    fun show(message: String = "Modo Offline - Usando dados locais") {
        binding.tvMessage.text = message
        visibility = VISIBLE
    }
    
    fun hide() {
        visibility = GONE
    }
}
```

**Layout XML:**
```xml
<!-- res/layout/view_offline_indicator.xml -->
<LinearLayout>
    <ImageView
        android:src="@drawable/ic_offline"
        android:tint="@color/white" />
    
    <TextView
        android:id="@+id/tvMessage"
        android:text="Modo Offline"
        android:textColor="@color/white" />
</LinearLayout>
```

---

### 3. **BaseOfflineActivity** (Novo Componente)

**Responsabilidade:** Activity base com suporte offline

```kotlin
// presentation/base/BaseOfflineActivity.kt
@AndroidEntryPoint
abstract class BaseOfflineActivity : AppCompatActivity() {
    
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    
    protected lateinit var offlineIndicator: OfflineIndicatorView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOfflineIndicator()
        observeConnectivity()
    }
    
    private fun setupOfflineIndicator() {
        // Adicionar indicador no topo da tela
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        offlineIndicator = OfflineIndicatorView(this)
        rootView.addView(offlineIndicator, 0)
    }
    
    private fun observeConnectivity() {
        lifecycleScope.launch {
            connectivityManager.isOnline.collect { isOnline ->
                if (isOnline) {
                    onConnectivityRestored()
                    offlineIndicator.hide()
                } else {
                    onConnectivityLost()
                    offlineIndicator.show()
                }
            }
        }
    }
    
    /**
     * Chamado quando conexão é restaurada
     */
    protected open fun onConnectivityRestored() {
        // Subclasses podem sobrescrever
        // Ex: iniciar sincronização automática
    }
    
    /**
     * Chamado quando conexão é perdida
     */
    protected open fun onConnectivityLost() {
        // Subclasses podem sobrescrever
        // Ex: cancelar operações de rede
    }
    
    /**
     * Verifica se está online
     */
    protected fun isOnline(): Boolean {
        return connectivityManager.isOnline.value
    }
}
```

---

### 4. **Atualização das Estratégias** (Modificação)

**Responsabilidade:** Estratégias devem respeitar modo offline

```kotlin
// data/strategy/DataSourceStrategyFactory.kt
class DataSourceStrategyFactory @Inject constructor(
    private val localStrategy: LocalDataSourceStrategy,
    private val remoteStrategy: RemoteDataSourceStrategy,
    private val connectivityManager: ConnectivityManager
) {
    
    /**
     * Retorna estratégia baseada em conectividade
     */
    fun getStrategy(): DataSourceStrategy {
        return if (connectivityManager.isOnline.value) {
            // Online: tenta remoto, fallback para local
            remoteStrategy
        } else {
            // Offline: usa apenas local
            localStrategy
        }
    }
    
    /**
     * Força uso da estratégia local
     */
    fun getLocalStrategy(): DataSourceStrategy {
        return localStrategy
    }
    
    /**
     * Força uso da estratégia remota (pode falhar)
     */
    fun getRemoteStrategy(): DataSourceStrategy {
        return remoteStrategy
    }
}
```

---

## 📱 Mudanças por Tela

### 1. SalaSelectionActivity

**Antes:**
```kotlin
class SalaSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        carregarSalas() // Sempre tenta servidor
    }
}
```

**Depois:**
```kotlin
@AndroidEntryPoint
class SalaSelectionActivity : BaseOfflineActivity() {
    
    private val viewModel: SalaSelectionViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        observeState()
        carregarSalas()
    }
    
    private fun carregarSalas() {
        if (isOnline()) {
            viewModel.carregarSalasDoServidor()
        } else {
            viewModel.carregarSalasLocais()
            mostrarAvisoOffline()
        }
    }
    
    override fun onConnectivityRestored() {
        // Recarregar salas do servidor
        viewModel.carregarSalasDoServidor()
    }
    
    private fun mostrarAvisoOffline() {
        Snackbar.make(
            binding.root,
            "Usando dados locais. Conecte para atualizar.",
            Snackbar.LENGTH_LONG
        ).show()
    }
}
```

---

### 2. DashboardFragment

**Antes:**
```kotlin
class DashboardFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarEstatisticas() // Sempre tenta servidor
    }
}
```

**Depois:**
```kotlin
@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    
    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupOfflineIndicator()
        observeConnectivity()
        carregarEstatisticas()
    }
    
    private fun setupOfflineIndicator() {
        lifecycleScope.launch {
            connectivityManager.isOnline.collect { isOnline ->
                binding.offlineIndicator.visibility = if (isOnline) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }
    
    private fun carregarEstatisticas() {
        if (connectivityManager.isOnline.value) {
            viewModel.carregarEstatisticasDoServidor()
        } else {
            viewModel.carregarEstatisticasLocais()
        }
    }
}
```

---

### 3. ColetaActivity

**Antes:**
```kotlin
class ColetaActivity : AppCompatActivity() {
    private fun registrarColeta() {
        viewModel.registrarColeta(dados)
    }
}
```

**Depois:**
```kotlin
@AndroidEntryPoint
class ColetaActivity : BaseOfflineActivity() {
    
    private fun registrarColeta() {
        if (isOnline()) {
            // Registrar e sincronizar imediatamente
            viewModel.registrarColetaComSyncImediato(dados)
        } else {
            // Registrar localmente, sincronizar depois
            viewModel.registrarColetaLocal(dados)
            mostrarAvisoSincronizacaoPendente()
        }
    }
    
    private fun mostrarAvisoSincronizacaoPendente() {
        Snackbar.make(
            binding.root,
            "Coleta salva. Será sincronizada quando conectar.",
            Snackbar.LENGTH_LONG
        ).setAction("Ver Pendentes") {
            // Abrir tela de coletas pendentes
        }.show()
    }
}
```

---

## 🔄 Fluxos de Funcionamento

### Fluxo 1: App Inicia Offline

```
1. App inicia
2. ConnectivityManager detecta: OFFLINE
3. Todas Activities herdam BaseOfflineActivity
4. Indicador "Modo Offline" aparece no topo
5. SalaSelectionActivity carrega salas do banco local
6. DashboardFragment mostra estatísticas locais
7. Usuário pode fazer coletas normalmente
8. Coletas são salvas localmente
9. Badge mostra "X coletas pendentes"
```

### Fluxo 2: Conexão Volta Durante Uso

```
1. Usuário está usando app offline
2. WiFi/4G é ativado
3. ConnectivityManager detecta: ONLINE
4. Indicador "Modo Offline" desaparece
5. onConnectivityRestored() é chamado em todas Activities
6. SyncWorker inicia sincronização automática
7. Notificação: "Sincronizando X coletas..."
8. Após sync: "X coletas sincronizadas com sucesso"
9. Badge de pendentes é atualizado
```

### Fluxo 3: Conexão Cai Durante Uso

```
1. Usuário está usando app online
2. Conexão é perdida
3. ConnectivityManager detecta: OFFLINE
4. Indicador "Modo Offline" aparece
5. onConnectivityLost() é chamado
6. Operações de rede são canceladas
7. App continua funcionando com dados locais
8. Usuário é avisado sobre limitações
```

---

## 🎨 Elementos Visuais

### 1. Indicador de Modo Offline

**Posição:** Topo da tela (abaixo da ActionBar)  
**Cor:** Laranja (#FFA500)  
**Ícone:** WiFi com X  
**Texto:** "Modo Offline - Usando dados locais"

### 2. Badge de Coletas Pendentes

**Posição:** Ícone de sincronização no menu  
**Cor:** Vermelho (#FF0000)  
**Texto:** Número de coletas pendentes  
**Ação:** Abre tela de sincronização

### 3. Status de Sincronização

**Ícones:**
- ✅ Verde: Sincronizado
- ⏳ Amarelo: Pendente
- ❌ Vermelho: Erro

### 4. Snackbar Contextual

**Quando aparece:**
- Coleta salva offline
- Conexão restaurada
- Sincronização concluída

**Ações:**
- "Ver Pendentes"
- "Sincronizar Agora"
- "Desfazer"

---

## 📋 Checklist de Implementação

### Fase 1: Infraestrutura (Prioridade ALTA)
- [ ] Criar `ConnectivityManager`
- [ ] Criar `BaseOfflineActivity`
- [ ] Criar `OfflineIndicatorView`
- [ ] Atualizar `DataSourceStrategyFactory`
- [ ] Adicionar permissão `ACCESS_NETWORK_STATE`
- [ ] Testar detecção de conectividade

### Fase 2: Telas Críticas (Prioridade ALTA)
- [ ] Migrar `SalaSelectionActivity` para `BaseOfflineActivity`
- [ ] Migrar `DashboardFragment` para usar `ConnectivityManager`
- [ ] Migrar `ColetaActivity` para `BaseOfflineActivity`
- [ ] Migrar `ManualCollectionActivity` para `BaseOfflineActivity`
- [ ] Testar fluxos offline em cada tela

### Fase 3: Sincronização Inteligente (Prioridade MÉDIA)
- [ ] Atualizar `SyncWorker` para detectar conexão
- [ ] Implementar sincronização automática ao conectar
- [ ] Adicionar notificações de sincronização
- [ ] Implementar badge de coletas pendentes
- [ ] Testar sincronização automática

### Fase 4: UX e Feedback (Prioridade MÉDIA)
- [ ] Adicionar indicadores visuais em todas telas
- [ ] Implementar Snackbars contextuais
- [ ] Adicionar animações de transição online/offline
- [ ] Implementar tela de status de sincronização
- [ ] Testar experiência do usuário

### Fase 5: Otimizações (Prioridade BAIXA)
- [ ] Cache inteligente de dados
- [ ] Pré-carregamento de dados críticos
- [ ] Compressão de dados para sync
- [ ] Métricas de uso offline
- [ ] Testes de performance

---

## 🧪 Plano de Testes

### Teste 1: Detecção de Conectividade
```
1. Iniciar app com WiFi ligado
2. ✅ Verificar que indicador offline NÃO aparece
3. Desligar WiFi
4. ✅ Verificar que indicador offline aparece
5. Ligar WiFi
6. ✅ Verificar que indicador desaparece
```

### Teste 2: Carregamento de Salas Offline
```
1. Importar salas com conexão
2. Desligar conexão
3. Reiniciar app
4. ✅ Verificar que salas são carregadas do banco local
5. ✅ Verificar indicador "Modo Offline"
```

### Teste 3: Coleta Offline
```
1. Desligar conexão
2. Fazer coleta de patrimônio
3. ✅ Verificar que coleta é salva localmente
4. ✅ Verificar Snackbar "Será sincronizada quando conectar"
5. ✅ Verificar badge de pendentes
```

### Teste 4: Sincronização Automática
```
1. Fazer 5 coletas offline
2. Ligar conexão
3. ✅ Verificar que sincronização inicia automaticamente
4. ✅ Verificar notificação de progresso
5. ✅ Verificar que badge é atualizado
```

### Teste 5: Transição Online → Offline → Online
```
1. Usar app online
2. Desligar conexão durante uso
3. ✅ Verificar transição suave
4. Continuar usando offline
5. Ligar conexão
6. ✅ Verificar sincronização automática
```

---

## 📊 Métricas de Sucesso

### Funcionalidade
- ✅ 100% das telas críticas funcionam offline
- ✅ Detecção de conectividade < 1 segundo
- ✅ Sincronização automática em < 5 segundos após conexão
- ✅ 0% de perda de dados offline

### Experiência do Usuário
- ✅ Indicador visual claro de modo offline
- ✅ Feedback contextual em todas ações
- ✅ Transições suaves entre modos
- ✅ Usuário sempre sabe o status

### Performance
- ✅ Carregamento de dados locais < 500ms
- ✅ Detecção de conectividade não impacta performance
- ✅ Sincronização em background não trava UI
- ✅ Consumo de bateria otimizado

---

## ⚠️ Riscos e Mitigações

### Risco 1: Dados Locais Desatualizados
**Mitigação:**
- Mostrar timestamp de última sincronização
- Avisar usuário sobre idade dos dados
- Botão "Atualizar" sempre visível quando online

### Risco 2: Conflitos de Sincronização
**Mitigação:**
- Coletas locais sempre têm prioridade
- Servidor nunca sobrescreve coletas locais
- Logs detalhados de conflitos

### Risco 3: Espaço em Disco
**Mitigação:**
- Limpar dados antigos automaticamente
- Compressão de dados
- Limite de coletas pendentes (ex: 1000)

### Risco 4: Bateria
**Mitigação:**
- Sincronização apenas com bateria > 20%
- WorkManager com constraints de bateria
- Sincronização em lote (batch)

---

## 🎯 Resultado Esperado

### Antes (Situação Atual)
```
❌ App tenta servidor mesmo offline
❌ Erros genéricos confundem usuário
❌ Usuário não sabe se está offline
❌ Algumas telas não funcionam sem conexão
❌ Sincronização é manual
```

### Depois (Com Implementação)
```
✅ App detecta automaticamente modo offline
✅ Usa dados locais transparentemente
✅ Indicador visual claro de status
✅ Todas telas críticas funcionam offline
✅ Sincronização automática ao conectar
✅ Feedback contextual em todas ações
✅ Experiência fluida e profissional
```

---

## 📅 Cronograma Estimado

### Fase 1: Infraestrutura (2-3 horas)
- Criar componentes base
- Configurar permissões
- Testes iniciais

### Fase 2: Telas Críticas (3-4 horas)
- Migrar 4 telas principais
- Testar cada tela
- Ajustes de UX

### Fase 3: Sincronização (2-3 horas)
- Atualizar SyncWorker
- Implementar notificações
- Testes de sincronização

### Fase 4: UX e Feedback (2-3 horas)
- Indicadores visuais
- Snackbars e animações
- Testes de usabilidade

### Fase 5: Otimizações (2-3 horas)
- Performance
- Métricas
- Testes finais

**Total Estimado:** 11-16 horas de desenvolvimento

---

## ✅ Aprovação para Implementação

**Este plano deve ser revisado e aprovado antes de iniciar a implementação.**

**Perguntas para o usuário:**
1. ✅ O plano cobre todos os cenários necessários?
2. ✅ As prioridades estão corretas?
3. ✅ Há alguma tela adicional que precisa suporte offline?
4. ✅ O cronograma está adequado?
5. ✅ Podemos começar pela Fase 1?

---

**Criado por:** Kiro AI Assistant  
**Data:** 22/11/2025  
**Status:** 📋 Aguardando Aprovação  
**Versão:** 1.0.0
