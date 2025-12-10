package com.inventario.mobile.security

import android.content.Context
import android.content.SharedPreferences
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * Property-Based Tests para LoginAttemptManager
 * 
 * Feature: android-security-hardening
 * Validates: Requirements 9.2, 9.4, 9.5
 */
class LoginAttemptManagerPropertyTest : FunSpec({
    
    lateinit var context: Context
    lateinit var prefs: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    val prefsData = mutableMapOf<String, Any>()
    
    beforeTest {
        // Reset prefs data
        prefsData.clear()
        
        // Mock SharedPreferences
        editor = mockk(relaxed = true)
        prefs = mockk()
        context = mockk()
        
        // Capture puts
        val stringSlot = slot<String>()
        val intSlot = slot<Int>()
        val longSlot = slot<Long>()
        
        every { editor.putInt(capture(stringSlot), capture(intSlot)) } answers {
            prefsData[stringSlot.captured] = intSlot.captured
            editor
        }
        every { editor.putLong(capture(stringSlot), capture(longSlot)) } answers {
            prefsData[stringSlot.captured] = longSlot.captured
            editor
        }
        every { editor.apply() } answers { }
        
        every { prefs.edit() } returns editor
        every { prefs.getInt(any(), any()) } answers {
            (prefsData[firstArg()] as? Int) ?: secondArg()
        }
        every { prefs.getLong(any(), any()) } answers {
            (prefsData[firstArg()] as? Long) ?: secondArg()
        }
        
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { context.applicationContext } returns context
    }
    
    /**
     * Property 7: Login Lockout After Max Attempts
     * For any sequence of N failed login attempts where N >= 5,
     * the system should be in locked state.
     * 
     * Validates: Requirements 9.2
     */
    test("Property 7: account is locked after 5 or more failed attempts") {
        checkAll(100, Arb.int(5..20)) { attempts ->
            // Reset state
            prefsData.clear()
            
            val manager = LoginAttemptManager(context)
            
            // Record N failed attempts
            repeat(attempts) {
                manager.recordFailedAttempt()
            }
            
            // Should be locked
            manager.isLocked() shouldBe true
            manager.getFailedAttemptCount() shouldBe attempts
        }
    }
    
    /**
     * Property 7 (complementary): account is NOT locked with less than 5 attempts
     */
    test("Property 7: account is NOT locked with less than 5 failed attempts") {
        checkAll(100, Arb.int(1..4)) { attempts ->
            // Reset state
            prefsData.clear()
            
            val manager = LoginAttemptManager(context)
            
            // Record N failed attempts (less than 5)
            repeat(attempts) {
                manager.recordFailedAttempt()
            }
            
            // Should NOT be locked
            manager.isLocked() shouldBe false
            manager.getFailedAttemptCount() shouldBe attempts
        }
    }
    
    /**
     * Property 8: Successful Login Resets Attempt Counter
     * For any state with failed attempts, after a successful login,
     * the attempt counter should be zero.
     * 
     * Validates: Requirements 9.4
     */
    test("Property 8: successful login resets attempt counter") {
        checkAll(100, Arb.int(1..10)) { attempts ->
            // Reset state
            prefsData.clear()
            
            val manager = LoginAttemptManager(context)
            
            // Record some failed attempts
            repeat(attempts) {
                manager.recordFailedAttempt()
            }
            
            // Record successful login
            manager.recordSuccessfulLogin()
            
            // Counter should be reset
            manager.getFailedAttemptCount() shouldBe 0
            manager.isLocked() shouldBe false
        }
    }
    
    /**
     * Property 9: Lockout State Persists Across Restarts
     * For any active lockout state, after simulating restart
     * (recreating LoginAttemptManager), the lockout should persist
     * if the lockout time has not expired.
     * 
     * Validates: Requirements 9.5
     */
    test("Property 9: lockout state persists across manager recreation") {
        // Reset state
        prefsData.clear()
        
        val manager1 = LoginAttemptManager(context)
        
        // Record 5 failed attempts to trigger lockout
        repeat(5) {
            manager1.recordFailedAttempt()
        }
        
        // Verify locked
        manager1.isLocked() shouldBe true
        
        // Create new manager (simulating app restart)
        val manager2 = LoginAttemptManager(context)
        
        // Should still be locked (data persisted in SharedPreferences)
        manager2.isLocked() shouldBe true
        manager2.getFailedAttemptCount() shouldBe 5
    }
    
    /**
     * Property 9 (complementary): remaining attempts calculation is correct
     */
    test("Property 9: remaining attempts calculation is correct") {
        checkAll(100, Arb.int(0..10)) { attempts ->
            // Reset state
            prefsData.clear()
            
            val manager = LoginAttemptManager(context)
            
            repeat(attempts) {
                manager.recordFailedAttempt()
            }
            
            val expected = maxOf(0, LoginAttemptManager.MAX_ATTEMPTS - attempts)
            manager.getRemainingAttempts() shouldBe expected
        }
    }
})
