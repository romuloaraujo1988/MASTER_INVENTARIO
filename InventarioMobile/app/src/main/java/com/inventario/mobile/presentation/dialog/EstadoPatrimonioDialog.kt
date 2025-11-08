package com.inventario.mobile.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.inventario.mobile.R
import com.inventario.mobile.data.model.EstadoPatrimonio

/**
 * Dialog para seleção do estado do patrimônio durante a coleta.
 */
class EstadoPatrimonioDialog : DialogFragment() {

    private var onEstadoSelected: ((EstadoPatrimonio) -> Unit)? = null
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    companion object {
        fun newInstance(onEstadoSelected: (EstadoPatrimonio) -> Unit): EstadoPatrimonioDialog {
            return EstadoPatrimonioDialog().apply {
                this.onEstadoSelected = onEstadoSelected
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_estado_patrimonio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroup = view.findViewById(R.id.radioGroupEstado)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        // Criar radio buttons dinamicamente
        EstadoPatrimonio.values().forEach { estado ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = estado.descricao
                tag = estado
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            radioGroup.addView(radioButton)
        }

        btnConfirmar.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                val estado = selectedRadioButton.tag as EstadoPatrimonio
                onEstadoSelected?.invoke(estado)
                dismiss()
            }
        }

        btnCancelar.setOnClickListener {
            dismiss()
        }
    }
}
