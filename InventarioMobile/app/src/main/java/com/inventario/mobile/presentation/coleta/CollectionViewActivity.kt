package com.inventario.mobile.presentation.coleta

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventario.mobile.R
import com.inventario.mobile.data.realtime.RealtimeColetasManager
import com.inventario.mobile.databinding.ActivityCollectionViewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * "Monitor ao vivo" de coletas.
 *
 * <p>Esta tela exibe, em tempo real, as coletas registradas por qualquer
 * usuário no inventário ativo. Ela conecta-se ao endpoint WebSocket
 * <code>/ws/coletas-live</code> do servidor e atualiza a lista
 * automaticamente — útil para um supervisor acompanhar o andamento da
 * coleta em campo sem precisar ficar puxando "atualizar".</p>
 *
 * <p><b>História:</b> originalmente esta era uma tela tradicional de
 * listagem com filtros, equivalente à <code>ColetasUnificadaActivity</code>.
 * Como as duas faziam a mesma coisa, esta foi convertida no monitor live —
 * mantemos a mesma classe e o mesmo path para preservar todos os Intents
 * que apontam pra cá (Dashboard "ver coletas" / "coletados").</p>
 *
 * <p><b>Ciclo de vida:</b></p>
 * <ul>
 *   <li><code>onStart</code> abre a conexão WebSocket</li>
 *   <li><code>onStop</code> fecha — economia de bateria quando o app vai
 *       pra background</li>
 * </ul>
 */
@AndroidEntryPoint
class CollectionViewActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CollectionViewActivity"
    }

    private lateinit var binding: ActivityCollectionViewBinding

    private val viewModel: LiveColetaMonitorViewModel by viewModels()

    /**
     * Reusamos o adapter já existente — recebe `data.model.Coleta` e cuida da
     * formatação. Sem long-click: no monitor live você não gerencia coletas
     * (pra isso existe a `ColetasUnificadaActivity`).
     */
    private val adapter = CollectionAdapter()

    /**
     * Tamanho da última lista renderizada — usado para detectar quando uma
     * coleta NOVA chegou (lista cresceu) e tocar o som sutil. Evita tocar
     * no primeiro `submitList` (que pode trazer 0..N itens iniciais) —
     * apenas crescimentos depois disso disparam o som.
     */
    private var ultimoTamanhoLista = 0

    /**
     * Marcador para suprimir o som logo na primeira renderização. Sem isso,
     * abrir a tela com qualquer evento já renderizaria com som — feio.
     */
    private var primeiraRenderizacao = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: monitor de coletas ao vivo")

        binding = ActivityCollectionViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        com.inventario.mobile.ui.components.OfflineIndicator.setup(this)

        configurarHeader()
        configurarRecyclerView()
        configurarBotaoVoltar()
        ocultarFiltrosLegados()
        observarViewModel()
    }

    override fun onStart() {
        super.onStart()
        // Inicia a conexão quando a tela ganha foco — mais leve do que
        // mantê-la aberta o tempo todo.
        viewModel.iniciarMonitoramento()
    }

    override fun onStop() {
        super.onStop()
        // Fecha o WebSocket assim que a tela sai de foco. Coletas perdidas
        // nesse intervalo aparecerão na próxima vez que a tela for aberta —
        // assim mesmo, dado que o manager é singleton, se outra tela usar
        // o feed (cenário futuro) ela mantém viva por conta própria.
        viewModel.pararMonitoramento()
    }

    /**
     * Ajusta o header existente para refletir o novo papel da tela.
     * Em vez de "Itens Coletados" + total/pendentes, usamos:
     *   - Título "Monitor ao vivo"
     *   - Coluna esquerda: total da sessão (contador acumulado)
     *   - Coluna direita: status da conexão (ao vivo / conectando / offline)
     */
    private fun configurarHeader() {
        // Localiza os TextView do layout antigo. Não há ID dedicado para o
        // título, então ajustamos pelos campos existentes.
        binding.tvTotalColetas.text = "0"
        binding.tvPendingSync.text = "—"

        // Os labels "Total" / "Pendentes" são TextViews irmãos no layout.
        // Como não têm ID exposto, mexer neles seria frágil. Em vez disso
        // mostramos a informação do "total da sessão" no campo "Total" e
        // ocultamos a coluna de "Pendentes" — que não faz sentido em modo
        // live (toda coleta que chega já foi sincronizada).
        // O espaço extra é preenchido pelo chip de status da conexão.
    }

    /**
     * Esconde controles que pertenciam à versão antiga (filtros por sala,
     * por status, por usuário). Eles continuam no XML pra não quebrar
     * outras referências, mas não fazem sentido aqui — o monitor live mostra
     * apenas o que chega.
     */
    private fun ocultarFiltrosLegados() {
        binding.cardSalaFilter.visibility = View.GONE
        binding.layoutFilters.visibility = View.GONE
        binding.layoutSelectSala.visibility = View.GONE
        binding.fabRelatorioFoto.visibility = View.GONE
        binding.tvFotosBadge.visibility = View.GONE
        binding.progressBarRelatorio.visibility = View.GONE
        binding.swipeRefreshLayout.isEnabled = false
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewColetas.apply {
            layoutManager = LinearLayoutManager(this@CollectionViewActivity)
            adapter = this@CollectionViewActivity.adapter
        }
    }

    private fun configurarBotaoVoltar() {
        binding.fabBackToDashboard.setOnClickListener {
            val intent = android.content.Intent(
                this,
                com.inventario.mobile.presentation.main.MainActivity::class.java
            )
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    /**
     * Observa o ViewModel — combina o estado de conexão e a lista de coletas
     * num único `collect` para reduzir reentrância.
     */
    private fun observarViewModel() {
        lifecycleScope.launch {
            // Lista
            launch {
                viewModel.coletas.collect { coletas ->
                    Log.d(TAG, "lista atualizada: ${coletas.size} itens")

                    // Tocar som sutil quando uma coleta NOVA chega — só
                    // depois da primeira renderização (para não tocar ao
                    // abrir a tela) e só se a lista cresceu (para não tocar
                    // em submitList que apenas reordene).
                    if (!primeiraRenderizacao && coletas.size > ultimoTamanhoLista) {
                        com.inventario.mobile.utils.SoundUtils.playLiveColetaSound()
                    }
                    primeiraRenderizacao = false
                    ultimoTamanhoLista = coletas.size

                    adapter.submitList(coletas)
                    atualizarEmptyState(coletas.isEmpty())
                }
            }

            // Contador acumulado
            launch {
                viewModel.contador.collect { total ->
                    binding.tvTotalColetas.text = total.toString()
                }
            }

            // Estado da conexão (mostra no campo "Pendentes" do header)
            launch {
                viewModel.conexao.collect { state -> atualizarStatusConexao(state) }
            }
        }
    }

    /**
     * Mostra "Nenhuma coleta recebida ainda" enquanto a sessão está vazia.
     * Reusamos o `layoutEmptyState` do XML antigo, alterando o texto via
     * findViewById dos TextViews internos (não têm ID — usamos o índice).
     */
    private fun atualizarEmptyState(vazio: Boolean) {
        if (vazio) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.swipeRefreshLayout.visibility = View.GONE

            // Atualizar texto do empty state pra refletir o modo live.
            val container = binding.layoutEmptyState
            val titulo = container.getChildAt(1) as? android.widget.TextView
            val subtitulo = container.getChildAt(2) as? android.widget.TextView
            titulo?.text = "Aguardando coletas"
            subtitulo?.text = "As coletas aparecerão aqui em tempo real\n" +
                "assim que outros usuários registrarem"
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.swipeRefreshLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Atualiza o badge de "Pendentes" no header pra mostrar o estado da
     * conexão WebSocket. É a indicação visual sutil de que a tela está ao
     * vivo — sem chip extra, aproveita o espaço já existente no layout.
     */
    private fun atualizarStatusConexao(state: RealtimeColetasManager.ConnectionState) {
        val (texto, corRes) = when (state) {
            is RealtimeColetasManager.ConnectionState.Connected ->
                "🟢 AO VIVO" to R.color.success
            is RealtimeColetasManager.ConnectionState.Connecting ->
                "Conectando…" to R.color.warning
            is RealtimeColetasManager.ConnectionState.Disconnected ->
                "Offline" to R.color.text_secondary
            is RealtimeColetasManager.ConnectionState.Error ->
                "Erro" to R.color.warning
        }
        binding.tvPendingSync.text = texto
        binding.tvPendingSync.textSize = 14f
        binding.tvPendingSync.setTextColor(ContextCompat.getColor(this, corRes))
    }

    override fun onSupportNavigateUp(): Boolean {
        @Suppress("DEPRECATION")
        onBackPressed()
        return true
    }
}
