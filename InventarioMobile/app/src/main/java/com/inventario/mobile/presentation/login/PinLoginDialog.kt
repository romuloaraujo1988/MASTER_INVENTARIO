package com.inventario.mobile.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.inventario.mobile.databinding.DialogPinLoginBinding
import com.inventario.mobile.security.PinAuthManager

class PinLoginDialog : DialogFragment() {
    
    private var _binding: DialogPinLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var pinAuthManager: PinAuthManager
    private var enteredPin: String = ""
    
    private var onPinValidatedListener: (() -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPinLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        pinAuthManager = PinAuthManager(requireContext())
        
        setupUI()
        setupListeners()
        checkIfLocked()
    }
    
    private fun setupUI() {
        binding.tvTitle.text = "Login Offline"
        binding.tvMessage.text = "Digite seu PIN:"
        updateAttemptsDisplay()
    }
    
    private fun setupListeners() {
        // Teclado numérico
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
    
    private fun checkIfLocked() {
        if (pinAuthManager.isAccountLocked()) {
            val minutes = pinAuthManager.getLockTimeRemaining()
            showError("Conta bloqueada. Tente em $minutes minutos.")
            disableKeypad()
        }
    }
    
    private fun addDigit(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin += digit
            updatePinDisplay()
            
            if (enteredPin.length == 4) {
                validatePin()
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
    
    private fun updateAttemptsDisplay() {
        val attempts = pinAuthManager.getRemainingAttempts()
        binding.tvAttempts.text = "Tentativas restantes: $attempts"
    }
    
    private fun validatePin() {
        if (pinAuthManager.validatePin(enteredPin)) {
            // PIN correto
            onPinValidatedListener?.invoke()
            dismiss()
        } else {
            // PIN incorreto
            val remaining = pinAuthManager.getRemainingAttempts()
            
            if (remaining > 0) {
                showError("PIN incorreto. Tentativas: $remaining")
                updateAttemptsDisplay()
            } else {
                showError("Conta bloqueada por 30 minutos.")
                disableKeypad()
            }
            
            // Limpar PIN
            enteredPin = ""
            updatePinDisplay()
        }
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    private fun disableKeypad() {
        binding.btn0.isEnabled = false
        binding.btn1.isEnabled = false
        binding.btn2.isEnabled = false
        binding.btn3.isEnabled = false
        binding.btn4.isEnabled = false
        binding.btn5.isEnabled = false
        binding.btn6.isEnabled = false
        binding.btn7.isEnabled = false
        binding.btn8.isEnabled = false
        binding.btn9.isEnabled = false
        binding.btnBackspace.isEnabled = false
    }
    
    fun setOnPinValidatedListener(listener: () -> Unit) {
        onPinValidatedListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(): PinLoginDialog {
            return PinLoginDialog()
        }
    }
}
