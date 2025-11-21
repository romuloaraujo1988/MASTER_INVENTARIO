package com.inventario.mobile.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.inventario.mobile.databinding.DialogPinSetupBinding
import com.inventario.mobile.security.PinAuthManager

class PinSetupDialog : DialogFragment() {
    
    private var _binding: DialogPinSetupBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var pinAuthManager: PinAuthManager
    private var enteredPin: String = ""
    private var isConfirmation: Boolean = false
    private var firstPin: String = ""
    
    private var onPinCreatedListener: ((String) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPinSetupBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        pinAuthManager = PinAuthManager(requireContext())
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        if (isConfirmation) {
            binding.tvTitle.text = "Confirmar PIN"
            binding.tvMessage.text = "Digite o PIN novamente:"
        } else {
            binding.tvTitle.text = "Criar PIN Offline"
            binding.tvMessage.text = "Crie um PIN de 4 dígitos:"
        }
    }
    
    private fun setupListeners() {
        binding.btn0.setOnClickListener { addDigit("0") }
        binding.btn1.setOnClickListener { addDigit("1") }
        binding.btn2.setOnClickListener { addDigit("2") }
        binding.btn3.setOnClickListener { addDigit("3") }
        binding.btn4.setOnClickListener { addDigit("4") }
        binding.btn5.setOnClickListener { addDigit("5") }
        binding.btn6.setOnClickListener { addDigit("6") }
        binding.btn7.setOnClickListener { addDigit("7") }
        binding.btn8.setOnClickListener { addDigit("8") }
        binding.btn9.setOnClickListener { addDigit("9") }
        
        binding.btnBackspace.setOnClickListener { removeDigit() }
        binding.btnCancel.setOnClickListener { dismiss() }
    }
    
    private fun addDigit(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin += digit
            updatePinDisplay()
            
            if (enteredPin.length == 4) {
                handlePinComplete()
            }
        }
    }
    
    private fun removeDigit() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            updatePinDisplay()
        }
    }
    
    private fun updatePinDisplay() {
        binding.pinDot1.isActivated = enteredPin.length >= 1
        binding.pinDot2.isActivated = enteredPin.length >= 2
        binding.pinDot3.isActivated = enteredPin.length >= 3
        binding.pinDot4.isActivated = enteredPin.length >= 4
    }
    
    private fun handlePinComplete() {
        if (!isConfirmation) {
            firstPin = enteredPin
            enteredPin = ""
            isConfirmation = true
            setupUI()
            updatePinDisplay()
        } else {
            if (enteredPin == firstPin) {
                if (pinAuthManager.createPin(enteredPin)) {
                    onPinCreatedListener?.invoke(enteredPin)
                    dismiss()
                } else {
                    showError("Erro ao criar PIN")
                }
            } else {
                showError("PINs não coincidem. Tente novamente.")
                enteredPin = ""
                firstPin = ""
                isConfirmation = false
                setupUI()
                updatePinDisplay()
            }
        }
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        
        binding.root.postDelayed({
            binding.tvError.visibility = View.GONE
        }, 3000)
    }
    
    fun setOnPinCreatedListener(listener: (String) -> Unit) {
        onPinCreatedListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): PinSetupDialog {
            return PinSetupDialog()
        }
    }
}
