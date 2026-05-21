package com.inventario.mobile.presentation.scanner

import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.inventario.mobile.R
import com.inventario.mobile.utils.PreferencesManager

/**
 * Activity de captura customizada que substitui a `CaptureActivity` padrão do
 * ZXing quando o [ScannerActivity] abre a câmera.
 *
 * ### Por que existe
 * O `ScanContract`/`ScanOptions` do ZXing abre por padrão uma activity própria
 * em tela cheia que mostra apenas a câmera + um texto de prompt. Isso esconde
 * todos os elementos de feedback que o app queria exibir durante a coleta
 * (contador, último patrimônio lido, sala atual, botão fechar, flash).
 *
 * Esta subclasse sobe um layout customizado ([R.layout.custom_capture]) que
 * mantém o [DecoratedBarcodeView] do ZXing funcionando (mesmo id
 * `@id/zxing_barcode_scanner`) e adiciona a camada de feedback por cima.
 *
 * ### Como ativar
 * No `ScannerActivity`, ao construir o `ScanOptions`, basta chamar
 * `setCaptureActivity(CustomCaptureActivity::class.java)`.
 *
 * ### Ciclo de vida
 * Toda a lógica de captura de barcode é delegada ao [CaptureManager] do ZXing
 * — ele cuida de iniciar a câmera, decodificar e encerrar a activity com o
 * resultado. A activity precisa apenas encaminhar os callbacks de ciclo de
 * vida.
 */
class CustomCaptureActivity : AppCompatActivity() {

    private lateinit var captureManager: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var tvContadorColetas: TextView
    private lateinit var tvUltimoColetado: TextView
    private lateinit var tvSalaAtual: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnFechar: ImageButton
    private lateinit var btnFlash: ImageButton
    private lateinit var btnZoomIn: ImageButton
    private lateinit var btnZoomOut: ImageButton
    private lateinit var tvZoomLevel: TextView
    private lateinit var seekZoom: SeekBar

    private var flashLigado = false

    /**
     * Nível atual de zoom: 0 = mínimo (1x), maxZoomRaw = máximo.
     * Unidade depende do dispositivo (o ZXing usa a escala da Camera API 1).
     */
    private var zoomAtual = 0
    private var zoomMaximo = 0
    private var zoomSuportado = false

    /** Step usado pelos botões +/- — fatias de 10% do zoom máximo. */
    private val zoomStep: Int
        get() = (zoomMaximo / 10).coerceAtLeast(1)

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_capture)

        // ✅ v2.30: manter a tela ligada enquanto a câmera de coleta está aberta.
        // Sem essa flag, o sistema apaga a tela após o timeout normal (~30s a 2 min
        // dependendo do device), o que dispara `onPause` na activity, libera a
        // câmera e o usuário tem que reabrir a tela. Em coleta de campo isso
        // atrapalha a produtividade — o scan é uma sessão contínua, não uma
        // ação única.
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        preferencesManager = PreferencesManager(this)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        tvContadorColetas = findViewById(R.id.tvContadorColetas)
        tvUltimoColetado = findViewById(R.id.tvUltimoColetado)
        tvSalaAtual = findViewById(R.id.tvSalaAtual)
        tvStatus = findViewById(R.id.tvStatus)
        btnFechar = findViewById(R.id.btnFechar)
        btnFlash = findViewById(R.id.btnFlash)
        btnZoomIn = findViewById(R.id.btnZoomIn)
        btnZoomOut = findViewById(R.id.btnZoomOut)
        tvZoomLevel = findViewById(R.id.tvZoomLevel)
        seekZoom = findViewById(R.id.seekZoom)

        // Setup do CaptureManager — obrigatório para o ZXing decodificar e
        // retornar o resultado ao launcher que invocou a activity.
        captureManager = CaptureManager(this, barcodeScannerView)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        captureManager.decode()

        // Feedback adicional: mostra texto "Lendo..." quando o scanner detecta
        // algum candidato, útil para dar sinal visual de que a câmera está
        // trabalhando. Usar decodeContinuous em vez de decode faria o callback
        // disparar múltiplas vezes — aqui só observamos result points.
        barcodeScannerView.barcodeView.addStateListener(object : CameraPreview.StateListener {
            override fun previewSized() {}
            override fun previewStarted() {
                tvStatus.text = "Aponte para o QR Code ou código de barras"
                // Quando o preview começa, a câmera já está aberta e podemos
                // descobrir os parâmetros de zoom suportados pelo dispositivo.
                // Fazemos com um pequeno atraso porque em alguns aparelhos os
                // parâmetros ficam disponíveis alguns ms depois do callback.
                Handler(Looper.getMainLooper()).postDelayed({ inicializarZoom() }, 200)
                // Verifica disponibilidade do flash logo após o preview iniciar.
                // O hardware feature pode ser consultado antes, mas só conseguimos
                // ligar a torch com a câmera já aberta.
                Handler(Looper.getMainLooper()).postDelayed({ inicializarFlash() }, 200)
            }
            override fun previewStopped() {}
            override fun cameraError(error: Exception?) {
                tvStatus.text = "Erro na câmera: ${error?.message ?: "desconhecido"}"
            }
            override fun cameraClosed() {}
        })

        btnFechar.setOnClickListener { finish() }

        btnFlash.setOnClickListener { alternarFlash() }

        // Gesture de pinch-to-zoom — experiência familiar a qualquer usuário
        // que usa câmera do celular. Combinado com os botões +/- para usuários
        // que preferem controle discreto.
        scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    if (!zoomSuportado) return false
                    // `scaleFactor` > 1 → aproximando dedos, < 1 → afastando.
                    // Convertemos para delta proporcional ao alcance de zoom.
                    val delta = ((detector.scaleFactor - 1f) * zoomMaximo * 0.5f).toInt()
                    aplicarZoom(zoomAtual + delta)
                    return true
                }
            }
        )

        btnZoomIn.setOnClickListener { aplicarZoom(zoomAtual + zoomStep) }
        btnZoomOut.setOnClickListener { aplicarZoom(zoomAtual - zoomStep) }

        seekZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser || !zoomSuportado) return
                // Converte 0-100 da SeekBar para o alcance real do dispositivo.
                val novoZoom = (progress * zoomMaximo / 100f).toInt()
                aplicarZoom(novoZoom, atualizarSeekBar = false)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        atualizarFeedback()
    }

    /**
     * Lê os valores atuais do [PreferencesManager] e atualiza os elementos da UI.
     * Chamado em onCreate e onResume — assim se o usuário fizer uma coleta via
     * o bottom sheet do ScannerActivity e voltar para a câmera (fluxo padrão
     * da coleta rápida), o contador e o chip "Último coletado" refletem a
     * última leitura sem precisar reiniciar a activity.
     */
    private fun atualizarFeedback() {
        val count = preferencesManager.getCollectionCount()
        tvContadorColetas.text = count.toString()

        val ultimo = preferencesManager.getLastCollectedPatrimonio()
        if (!ultimo.isNullOrBlank()) {
            tvUltimoColetado.text = "Último: $ultimo"
            tvUltimoColetado.visibility = View.VISIBLE
        } else {
            tvUltimoColetado.visibility = View.GONE
        }

        val salaNome = preferencesManager.getCurrentSalaNome()
        if (salaNome.isNotBlank()) {
            tvSalaAtual.text = "Sala: $salaNome"
            tvSalaAtual.visibility = View.VISIBLE
        } else {
            tvSalaAtual.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
        atualizarFeedback()
        // Restaura a torch se o usuário a deixou ligada e voltou para a tela
        // (ex.: trocou de app rapidamente). O preview ainda não está ativo
        // aqui, então usamos um pequeno delay para dar tempo da câmera abrir.
        if (flashLigado && flashDisponivel) {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    barcodeScannerView.setTorchOn()
                } catch (e: Exception) {
                    android.util.Log.w("CustomCaptureActivity", "Falha ao restaurar torch: ${e.message}")
                }
            }, 500)
        }
    }

    override fun onPause() {
        super.onPause()
        // Desligar a lanterna ao sair da tela — evita deixar o LED aceso
        // gastando bateria caso o usuário só pressione o botão home/voltar.
        // Não tocamos no estado `flashLigado` para que o usuário ache a
        // lanterna no mesmo estado ao retornar (se voltar para a tela).
        if (flashLigado) {
            try {
                barcodeScannerView.setTorchOff()
            } catch (e: Exception) {
                // Silencioso — câmera pode já estar liberada.
            }
        }
        captureManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        captureManager.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    /**
     * Intercepta eventos de toque para o pinch-to-zoom. Precisa ser
     * `dispatchTouchEvent` para pegar os gestos ANTES do DecoratedBarcodeView
     * consumir — caso contrário o zoom nativo do preview vence.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (zoomSuportado) {
            scaleGestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    // ─────────────────────────────────────────────────────────────────
    // Controle de zoom — usa Camera API 1 via DecoratedBarcodeView
    // ─────────────────────────────────────────────────────────────────

    /**
     * Consulta os parâmetros da câmera para descobrir o alcance de zoom
     * suportado. Se o dispositivo não permitir zoom, os controles são
     * ocultados.
     *
     * O ZXing usa a Camera API 1 (legado). Acessamos a Camera via reflection
     * porque `BarcodeView.cameraInstance` expõe o `CameraInstance` que
     * internamente tem a Camera, mas não há um getter público para ela.
     * Em vez de mexer em reflection, aplicamos o zoom via
     * `BarcodeView.changeCameraParameters` que é um helper público da lib.
     */
    private fun inicializarZoom() {
        // ✅ CORREÇÃO: Envolver toda a inicialização de zoom em try-catch amplo.
        // O `changeCameraParameters` usa Camera API 1 (legada) e pode lançar
        // RuntimeException em dispositivos Android 14+ ou quando a câmera está
        // em estado de transição (ex.: logo após o preview iniciar). Qualquer
        // exceção aqui NÃO deve propagar para o CaptureManager — apenas
        // desabilitamos os controles de zoom silenciosamente.
        try {
            val cameraInstance = barcodeScannerView.barcodeView.cameraInstance
            if (cameraInstance == null) {
                android.util.Log.d("CustomCaptureActivity", "cameraInstance nulo — zoom desabilitado")
                zoomSuportado = false
                runOnUiThread { ocultarControlesZoom() }
                return
            }

            // Descobrir zoom máximo consultando os parâmetros da câmera.
            // Retornamos os parâmetros inalterados — apenas lemos os valores.
            barcodeScannerView.barcodeView.changeCameraParameters { parameters: Camera.Parameters ->
                try {
                    if (parameters.isZoomSupported) {
                        zoomMaximo = parameters.maxZoom
                        zoomSuportado = zoomMaximo > 0
                        runOnUiThread {
                            if (zoomSuportado) {
                                seekZoom.max = 100
                                seekZoom.progress = 0
                                atualizarLabelZoom(0)
                            } else {
                                ocultarControlesZoom()
                            }
                        }
                    } else {
                        zoomSuportado = false
                        runOnUiThread { ocultarControlesZoom() }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("CustomCaptureActivity", "Erro ao ler parâmetros de zoom: ${e.message}")
                    zoomSuportado = false
                    runOnUiThread { ocultarControlesZoom() }
                }
                parameters
            }
        } catch (e: RuntimeException) {
            // RuntimeException é o tipo mais comum lançado pela Camera API 1
            // quando a câmera está em estado inválido (ex.: sendo liberada).
            android.util.Log.w("CustomCaptureActivity", "RuntimeException ao inicializar zoom (Camera API 1): ${e.message}")
            zoomSuportado = false
            runOnUiThread { ocultarControlesZoom() }
        } catch (e: Exception) {
            android.util.Log.w("CustomCaptureActivity", "Exceção ao inicializar zoom: ${e.message}")
            zoomSuportado = false
            runOnUiThread { ocultarControlesZoom() }
        }
    }

    /**
     * Aplica um novo valor de zoom, normalizando para o alcance suportado.
     *
     * @param valor        valor absoluto (0..zoomMaximo)
     * @param atualizarSeekBar se true, sincroniza o SeekBar; deixamos false
     *                     quando o evento veio do próprio SeekBar para evitar
     *                     loop de callbacks.
     */
    private fun aplicarZoom(valor: Int, atualizarSeekBar: Boolean = true) {
        if (!zoomSuportado) return
        val novo = valor.coerceIn(0, zoomMaximo)
        if (novo == zoomAtual) return

        try {
            barcodeScannerView.barcodeView.changeCameraParameters { parameters: Camera.Parameters ->
                try {
                    if (parameters.isZoomSupported) {
                        parameters.zoom = novo
                    }
                } catch (e: Exception) {
                    // Ignorado — alguns dispositivos recusam zoom em certas
                    // condições (ex.: flash ligado + zoom em câmera frontal).
                }
                parameters
            }
            zoomAtual = novo
            atualizarLabelZoom(novo)
            if (atualizarSeekBar) {
                seekZoom.progress = (novo * 100 / zoomMaximo).coerceIn(0, 100)
            }
        } catch (e: RuntimeException) {
            // RuntimeException da Camera API 1 — desabilitar zoom para evitar
            // crashes futuros neste dispositivo.
            android.util.Log.w("CustomCaptureActivity", "RuntimeException ao aplicar zoom: ${e.message} — desabilitando zoom")
            zoomSuportado = false
            runOnUiThread { ocultarControlesZoom() }
        } catch (e: Exception) {
            // Silencioso — falha de zoom não deve interromper o scan.
        }
    }

    /**
     * Converte o valor cru do zoom (escala 0..zoomMaximo, específica do
     * dispositivo) para uma etiqueta legível tipo "1x", "1.5x", "3x".
     *
     * Considera que zoom=0 é 1x (sem zoom) e zoomMaximo geralmente
     * corresponde a aproximadamente 4x-8x dependendo do hardware — mas
     * não temos como saber o multiplicador exato sem a Camera2 API. Como
     * aproximação, usamos linear entre 1x e 4x.
     */
    private fun atualizarLabelZoom(valor: Int) {
        val multiplicador = if (zoomMaximo > 0) 1f + (valor * 3f / zoomMaximo) else 1f
        tvZoomLevel.text = if (multiplicador >= 10f)
            String.format("%.0fx", multiplicador)
        else
            String.format("%.1fx", multiplicador)
    }

    private fun ocultarControlesZoom() {
        findViewById<View>(R.id.layoutZoom).visibility = View.GONE
        seekZoom.visibility = View.GONE
    }

    // ─────────────────────────────────────────────────────────────────
    // Controle de flash/lanterna
    // ─────────────────────────────────────────────────────────────────

    /**
     * Estado de disponibilidade do flash. Atualizado uma única vez quando o
     * preview da câmera inicia. Se o dispositivo não tiver flash de hardware
     * (ex.: tablets baratos, alguns emuladores), o botão é ocultado.
     */
    private var flashDisponivel = false

    /**
     * Verifica se o dispositivo tem hardware de flash e habilita/oculta o
     * botão correspondentemente. Chamado em `previewStarted` porque alguns
     * dispositivos precisam que a câmera esteja aberta antes de reportar
     * corretamente as features.
     *
     * Importante: usar `FEATURE_CAMERA_FLASH` é o método oficial recomendado
     * pelo Android. Não tentamos ligar a torch só para testar — isso causa
     * piscadas indesejadas no preview.
     */
    private fun inicializarFlash() {
        flashDisponivel = packageManager.hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_CAMERA_FLASH
        )

        if (flashDisponivel) {
            btnFlash.visibility = View.VISIBLE
            // Garante o ícone correto no estado inicial (desligado).
            btnFlash.setImageResource(R.drawable.ic_flash_off)
            btnFlash.contentDescription = "Ligar lanterna"
        } else {
            android.util.Log.d("CustomCaptureActivity", "Dispositivo sem flash — botão ocultado")
            btnFlash.visibility = View.GONE
        }
    }

    /**
     * Alterna o estado da lanterna e atualiza o ícone do botão.
     *
     * Tratamento de erro: alguns dispositivos lançam `RuntimeException` ao
     * tentar ligar a torch enquanto a câmera está em transição (ex.: durante
     * autofocus). Capturamos para manter o scan funcionando — apenas revertemos
     * o estado interno e mantemos o ícone consistente.
     */
    private fun alternarFlash() {
        if (!flashDisponivel) return

        try {
            flashLigado = !flashLigado
            if (flashLigado) {
                barcodeScannerView.setTorchOn()
                btnFlash.setImageResource(R.drawable.ic_flash_on)
                btnFlash.contentDescription = "Desligar lanterna"
            } else {
                barcodeScannerView.setTorchOff()
                btnFlash.setImageResource(R.drawable.ic_flash_off)
                btnFlash.contentDescription = "Ligar lanterna"
            }
        } catch (e: RuntimeException) {
            android.util.Log.w("CustomCaptureActivity", "Erro ao alternar flash: ${e.message}")
            // Reverte o estado interno e o ícone para refletir a realidade
            // (a torch não foi ligada/desligada de fato).
            flashLigado = !flashLigado
            btnFlash.setImageResource(
                if (flashLigado) R.drawable.ic_flash_on else R.drawable.ic_flash_off
            )
        }
    }
}
