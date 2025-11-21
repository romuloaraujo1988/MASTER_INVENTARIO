# 🚀 Guia de Implementação Passo a Passo - Login Offline com PIN

## 📋 Pré-requisitos

- [ ] Android Studio instalado
- [ ] Projeto aberto
- [ ] Branch criada: `feature/pin-offline-login`
- [ ] Documentação lida

---

## 📅 Dia 1 - Core (PinAuthManager)

### Passo 1: Criar Estrutura de Pastas (5 min)

```bash
InventarioMobile/app/src/main/java/com/inventario/mobile/
└── security/
    ├── PinAuthManager.kt          # ← CRIAR
    ├── BiometricAuthManager.kt    # ← JÁ EXISTE
    └── BiometricCallback.kt       # ← JÁ EXISTE
```

### Passo 2: Criar PinAuthManager.kt (3h)

**Localização:** `app/src/main/java/com/inventario/mobile/security/PinAuthManager.kt`

**Conteúdo:** Copiar código completo do documento `PLANO_LOGIN_OFFLINE_SEM_BIOMETRIA.md`

**Checklist:**
- [ ] Arquivo criado
- [ ] Imports corretos
- [ ] Compilação sem erros
- [ ] Métodos principais:
  - [ ] `isPinEnabled()`
  - [ ] `createPin(pin: String)`
  - [ ] `validatePin(pin: String)`
  - [ ] `getRemainingAttempts()`
  - [ ] `isAccountLocked()`

### Passo 3: Adicionar Métodos no PreferencesManager.kt (30 min)

**Localização:** `app/src/main/java/com/inventario/mobile/utils/PreferencesManager.kt`

**Adicionar:**
```kotlin
// ===== MÉTODOS PARA PIN =====

fun getPinHash(): String = getString("pin_hash", "")
fun setPinHash(hash: String) = putString("pin_hash", hash)

fun getPinSalt(): String = getString("pin_salt", "")
fun setPinSalt(salt: String) = putString("pin_salt", salt)

fun isPinEnabled(): Boolean = getBoolean("pin_enabled", false)
fun setPinEnabled(enabled: Boolean) = putBoolean("pin_enabled", enabled)

fun getPinAttempts(): Int = getInt("pin_attempts", 3)
fun setPinAttempts(attempts: Int) = putInt("pin_attempts", attempts)

fun getPinLockUntil(): Long = getLong("pin_lock_until", 0L)
fun setPinLockUntil(timestamp: Long) = putLong("pin_lock_until", timestamp)

fun clearPinData() {
    remove("pin_hash")
    remove("pin_salt")
    remove("pin_enabled")
    remove("pin_attempts")
    remove("pin_lock_until")
}
```

**Checklist:**
- [ ] Métodos adicionados
- [ ] Compilação sem erros

### Passo 4: Testar PinAuthManager (1h)

**Criar:** `app/src/test/java/com/inventario/mobile/security/PinAuthManagerTest.kt`

```kotlin
class PinAuthManagerTest {
    
    @Test
    fun `criar PIN valido deve retornar true`() {
        val pinManager = PinAuthManager(context)
        val result = pinManager.createPin("1234")
        assertTrue(result)
    }
    
    @Test
    fun `validar PIN correto deve retornar true`() {
        val pinManager = PinAuthManager(context)
        pinManager.createPin("1234")
        val result = pinManager.validatePin("1234")
        assertTrue(result)
    }
    
    @Test
    fun `validar PIN incorreto deve decrementar tentativas`() {
        val pinManager = PinAuthManager(context)
        pinManager.createPin("1234")
        pinManager.validatePin("9999")
        assertEquals(2, pinManager.getRemainingAttempts())
    }
    
    @Test
    fun `3 tentativas incorretas devem bloquear conta`() {
        val pinManager = PinAuthManager(context)
        pinManager.createPin("1234")
        pinManager.validatePin("9999")
        pinManager.validatePin("8888")
        pinManager.validatePin("7777")
        assertTrue(pinManager.isAccountLocked())
    }
}
```

**Executar:**
```bash
./gradlew test
```

**Checklist:**
- [ ] Testes criados
- [ ] Todos os testes passando
- [ ] Cobertura > 80%

---

## 📅 Dia 2 - UI (Dialogs e Layouts)

### Passo 5: Criar Layouts XML (2h)

#### 5.1 - Criar pin_dot.xml

**Localização:** `app/src/main/res/drawable/pin_dot.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_activated="true">
        <shape android:shape="oval">
            <solid android:color="@color/primary"/>
            <size android:width="16dp" android:height="16dp"/>
        </shape>
    </item>
    <item>
        <shape android:shape="oval">
            <stroke android:width="2dp" android:color="@color/primary"/>
            <solid android:color="@android:color/transparent"/>
            <size android:width="16dp" android:height="16dp"/>
        </shape>
    </item>
</selector>
```

#### 5.2 - Criar dialog_pin_setup.xml

**Localização:** `app/src/main/res/layout/dialog_pin_setup.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Criar PIN Offline"
        android:textSize="20sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:id="@+id/tvMessage"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Crie um PIN de 4 dígitos:"
        android:textSize="14sp"
        android:gravity="center"
        android:layout_marginBottom="24dp"/>

    <!-- Display do PIN -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:orientation="horizontal"
        android:layout_marginBottom="24dp">

        <View
            android:id="@+id/pinDot1"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot2"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot3"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot4"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>
    </LinearLayout>

    <TextView
        android:id="@+id/tvError"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textColor="@color/error"
        android:textSize="12sp"
        android:gravity="center"
        android:visibility="gone"
        android:layout_marginBottom="16dp"/>

    <!-- Teclado numérico -->
    <include layout="@layout/numeric_keypad"/>

</LinearLayout>
```

#### 5.3 - Criar numeric_keypad.xml

**Localização:** `app/src/main/res/layout/numeric_keypad.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<GridLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:columnCount="3"
    android:rowCount="4">

    <!-- Linha 1: 1, 2, 3 -->
    <Button android:id="@+id/btn1" style="@style/PinKeypadButton" android:text="1"/>
    <Button android:id="@+id/btn2" style="@style/PinKeypadButton" android:text="2"/>
    <Button android:id="@+id/btn3" style="@style/PinKeypadButton" android:text="3"/>

    <!-- Linha 2: 4, 5, 6 -->
    <Button android:id="@+id/btn4" style="@style/PinKeypadButton" android:text="4"/>
    <Button android:id="@+id/btn5" style="@style/PinKeypadButton" android:text="5"/>
    <Button android:id="@+id/btn6" style="@style/PinKeypadButton" android:text="6"/>

    <!-- Linha 3: 7, 8, 9 -->
    <Button android:id="@+id/btn7" style="@style/PinKeypadButton" android:text="7"/>
    <Button android:id="@+id/btn8" style="@style/PinKeypadButton" android:text="8"/>
    <Button android:id="@+id/btn9" style="@style/PinKeypadButton" android:text="9"/>

    <!-- Linha 4: Cancelar, 0, Backspace -->
    <Button android:id="@+id/btnCancel" style="@style/PinKeypadButton" android:text="✕"/>
    <Button android:id="@+id/btn0" style="@style/PinKeypadButton" android:text="0"/>
    <Button android:id="@+id/btnBackspace" style="@style/PinKeypadButton" android:text="⌫"/>

</GridLayout>
```

#### 5.4 - Adicionar Estilo

**Localização:** `app/src/main/res/values/styles.xml`

```xml
<style name="PinKeypadButton">
    <item name="android:layout_width">64dp</item>
    <item name="android:layout_height">64dp</item>
    <item name="android:layout_margin">8dp</item>
    <item name="android:textSize">24sp</item>
    <item name="android:textStyle">bold</item>
    <item name="backgroundTint">@color/primary_light</item>
</style>
```

**Checklist:**
- [ ] pin_dot.xml criado
- [ ] dialog_pin_setup.xml criado
- [ ] numeric_keypad.xml criado
- [ ] Estilo adicionado
- [ ] Preview no Android Studio OK



### Passo 6: Criar PinSetupDialog.kt (2h)

**Localização:** `app/src/main/java/com/inventario/mobile/presentation/login/PinSetupDialog.kt`

**Conteúdo:** Copiar código completo do documento `PLANO_LOGIN_OFFLINE_SEM_BIOMETRIA.md`

**Estrutura:**
```kotlin
class PinSetupDialog : DialogFragment() {
    
    private var _binding: DialogPinSetupBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var pinAuthManager: PinAuthManager
    private var enteredPin: String = ""
    private var isConfirmation: Boolean = false
    
    override fun onCreateView(...)
    override fun onViewCreated(...)
    
    private fun setupUI()
    private fun setupListeners()
    private fun addDigit(digit: String)
    private fun removeDigit()
    private fun updatePinDisplay()
    private fun handlePinComplete()
    private fun showError(message: String)
    
    fun setOnPinCreatedListener(listener: (String) -> Unit)
}
```

**Checklist:**
- [ ] Arquivo criado
- [ ] ViewBinding configurado
- [ ] Teclado numérico funcionando
- [ ] Validação de PIN
- [ ] Confirmação de PIN
- [ ] Feedback visual (dots)
- [ ] Compilação sem erros

### Passo 7: Criar PinLoginDialog.kt (1h)

**Localização:** `app/src/main/java/com/inventario/mobile/presentation/login/PinLoginDialog.kt`

```kotlin
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
```

**Criar Layout:** `app/src/main/res/layout/dialog_pin_login.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login Offline"
        android:textSize="20sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:id="@+id/tvMessage"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Digite seu PIN:"
        android:textSize="14sp"
        android:gravity="center"
        android:layout_marginBottom="24dp"/>

    <!-- Display do PIN -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:orientation="horizontal"
        android:layout_marginBottom="16dp">

        <View
            android:id="@+id/pinDot1"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot2"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot3"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>

        <View
            android:id="@+id/pinDot4"
            android:layout_width="16dp"
            android:layout_height="16dp"
            android:layout_margin="8dp"
            android:background="@drawable/pin_dot"/>
    </LinearLayout>

    <TextView
        android:id="@+id/tvAttempts"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Tentativas restantes: 3"
        android:textSize="12sp"
        android:gravity="center"
        android:layout_marginBottom="8dp"/>

    <TextView
        android:id="@+id/tvError"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textColor="@color/error"
        android:textSize="12sp"
        android:gravity="center"
        android:visibility="gone"
        android:layout_marginBottom="16dp"/>

    <!-- Teclado numérico -->
    <include layout="@layout/numeric_keypad"/>

</LinearLayout>
```

**Checklist:**
- [ ] PinLoginDialog.kt criado
- [ ] dialog_pin_login.xml criado
- [ ] Validação de PIN funcionando
- [ ] Contador de tentativas funcionando
- [ ] Bloqueio após 3 tentativas
- [ ] Compilação sem erros

---

## 📅 Dia 3 - Integração (ViewModel e Activity)

### Passo 8: Atualizar LoginUiState (15 min)

**Localização:** `app/src/main/java/com/inventario/mobile/presentation/login/LoginViewModel.kt`

**Adicionar campos:**
```kotlin
data class LoginUiState(
    // ... campos existentes ...
    
    // Novos campos para PIN
    val shouldOfferPinSetup: Boolean = false,
    val hasPinEnabled: Boolean = false,
    val showPinLogin: Boolean = false,
    val pinAttemptsRemaining: Int = 3
)
```

**Checklist:**
- [ ] Campos adicionados
- [ ] Compilação sem erros

### Passo 9: Adicionar Métodos no LoginViewModel (1h)

**Adicionar:**
```kotlin
/**
 * Verifica se deve oferecer configuração de PIN
 */
fun checkPinSetuados

ogs adicion[ ] Lrros
-  eação sem] Compil- [ s
adicionado ] Métodos list:**
- [`

**Check    }
}
``     )
  ."
 etntern à isecte-idos. Coneocais invál"Dados lessage = rorM  er   ,
       g = falseisLoadin         (
   copyue.ate.val= _uiState.value    _uiSte {
      els  } )
  e
       = truode    offlineM
         l = true,ccessfu  isLoginSu      
    e,= fals  isLoading         copy(
  te.value. = _uiStae.value   _uiStat     
     
   ido")bem-sucedine via PIN fln of", "✅ LogiewModelginVid("Lo  Log.{
      = null)  ! tokenl &&me != nulullNa& fnull &sername != if (u
    
    sToken()getAccescesManager. preferen =al token vme()
   FullNaedUsertSaver.genageferencesMame = pral fullNa()
    vameetSavedUsernr.gesManageferenc prel username =
    vatials() {fflineCredenginWithOate fun los
 */
privvadenciais sal creine com offlLogin

/**
 * 
    }
})
        }       }"
     ${e.messagee = "Erro: rrorMessag           e  se,
   alading = f   isLo    (
         pycoState.value.= _uitate.value    _uiS         )
PIN", eo login com ", "Erro ndelinViewMo("Log       Log.e   ption) {
  ceh (e: Ex } catc }
             
            )
         remainingRemaining = Attempts     pin        
             },              "
minutos.eada por 30 Conta bloqu "                
          } else {               ing"
  as: $remainentativ To. incorret    "PIN                    {
ning > 0) (remai = if orMessage        err        e,
    = falsg sLoadin i              
     opy(e.value.c= _uiState.value   _uiStat              
      
          empts()ingAttinemaManager.getRAuthg = pininmain   val re      to
       corre/ PIN in  /       e {
        } els           ntials()
CredeithOfflineloginW            ne
     login offliN correto - // PI          )) {
     tePin(pinger.validauthManainA    if (p       r PIN
 lida    // Va         
   }
                  n@launch
     retur                    )
        nutos."
 s miuteem $min Tente da.queaonta bloe = "CrrorMessag    e               alse,
 oading = fisL                    e.copy(
iState.valuvalue = _ute. _uiSta        )
       ng(RemainiTimer.getLocknage= pinAuthMas ute     val min         ked()) {
  untLoc.isAccogerananAuthM      if (pi   ueio
   ar bloqrific      // Ve
           t)
       ger(contexMana PinAuthger =MananAuthval pi             
       )
    sage = null errorMestrue,g = isLoadinalue.copy(= _uiState.vvalue iState.          _u
     try {    {
 aunch elScope.l  viewModg) {
  inin: Str(ploginWithPin
fun */
  com PIN * Login

/**

    }
}   } e)
     ar PIN",erific "Erro ao vModel","LoginView.e(  Log
          {ption) : Exceh (e      } catc )
      
       nevalue.isOnli& !_uiState.d &inEnablesPn = hanLogiowPi        sh  
      nabled,inEsPed = haPinEnabl has               OfferPin,
uld shoetup =fferPinS  shouldO              e.copy(
te.valuSta.value = _uiate _uiSt                     
serSaved
  bled && hasUPinEnac && !hastrihasBiomeerPin = !ouldOff  val sh  N
         tem PIria e nãom biometo tenãPIN se // Oferecer      
               ()
    avedLocallysUserSsManager.haencefer preed =hasUserSav     val      
  Enabled()ger.isPinnainAuthMaabled = pPinEn val has        
   e()vailablable().isAcAvail.isBiometritricManageromeometric = bihasBi  val        
      )
         contexthManager(BiometricAutanager = l biometricM  va  )
        xtcontehManager( = PinAututhManager pinA     val
         try {
      e.launch {ewModelScop
    vip() {