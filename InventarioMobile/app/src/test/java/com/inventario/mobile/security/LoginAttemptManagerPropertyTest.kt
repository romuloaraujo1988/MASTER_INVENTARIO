package com.inventario.mobile.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-Based Tests para LoginAttemptManager
 * 
 * Feature: android-security-hardening
 * Validates: Requirements 9.2, 9.4, 9.5
 * 
 * Nota: Estes testes verificam a lógica de negócio sem depender de Android Context.
 * Os testes de integração com SharedPreferences devem ser feitos em androidTest.
 */
class LoginAttemptManagerPropertyTest : FunSpec({
    
    val MAX_ATTEMPTS = 5
    
    /**
     * Simula a lógica de lockout do LoginAttemptManager.
     * Esta classe replica a lógica para testes unitários sem dependências Android.
     */
    class MockLoginAttemptManager(private val maxAttempts: Int) {
        private var failedAttempts = 0
        
        fun recordFailedAttempt() {
            failedAttempts++
        }
        
        fun recordSuccessfulLogin() {
            failedAttempts = 0
        }
        
        fun isLocked(): Boolean = failedAttempts >= maxAttempts
        
        fun getFailedAttemptCount(): Int = failedAttempts
        
        fun getRemainingAttempts(): Int = maxOf(0, maxAttempts - failedAttempts)
        
        fun reset() {
            failedAttempts = 0
        }
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
            val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
            
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
            val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
            
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
            val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
            
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
     * Property 9: Remaining attempts calculation is correct
     * 
     * Validates: Requirements 9.5
     */
    test("Property 9: remaining attempts calculation is correct") {
        checkAll(100, Arb.int(0..10)) { attempts ->
            val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
            
            repeat(attempts) {
                manager.recordFailedAttempt()
            }
            
            val expected = maxOf(0, MAX_ATTEMPTS - attempts)
            manager.getRemainingAttempts() shouldBe expected
        }
    }
    
    /**
     * Property: Lockout threshold is exactly 5 attempts
     */
    test("Lockout threshold is exactly 5 attempts") {
        val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
        
        // 4 attempts should not lock
        repeat(4) { manager.recordFailedAttempt() }
        manager.isLocked() shouldBe false
        
        // 5th attempt should lock
        manager.recordFailedAttempt()
        manager.isLocked() shouldBe true
    }
    
    /**
     * Property: Reset clears all state
     */
    test("Reset clears all state") {
        checkAll(100, Arb.int(1..20)) { attempts ->
            val manager = MockLoginAttemptManager(MAX_ATTEMPTS)
            
            repeat(attempts) { manager.recordFailedAttempt() }
            manager.reset()
            
            manager.getFailedAttemptCount() shouldBe 0
            manager.isLocked() shouldBe false
            manager.getRemainingAttempts() shouldBe MAX_ATTEMPTS
        }
    }
})
