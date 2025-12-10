package com.inventario.mobile.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para LogSanitizer
 * 
 * Feature: android-security-hardening, Property 10: Log Sanitization Masks Sensitive Data
 * Validates: Requirements 3.2, 3.3
 */
class LogSanitizerPropertyTest : FunSpec({
    
    /**
     * Property 10: Log Sanitization Masks Sensitive Data
     * For any string containing JWT tokens, the sanitized output
     * should not contain the original token.
     * 
     * Validates: Requirements 3.2, 3.3
     */
    test("Property 10: JWT tokens are masked in sanitized output") {
        checkAll(100, jwtTokenArb()) { jwt ->
            val message = "User logged in with token: $jwt"
            val sanitized = LogSanitizer.sanitize(message)
            
            sanitized shouldNotContain jwt
            sanitized.contains("[JWT_TOKEN]") shouldBe true
        }
    }
    
    /**
     * Property 10 (complementary): emails are partially masked
     */
    test("Property 10: emails are masked in sanitized output") {
        checkAll(100, emailArb()) { email ->
            val message = "User email: $email"
            val sanitized = LogSanitizer.sanitize(message)
            
            // Email should be partially masked (first 2 chars + *** + @domain)
            sanitized shouldNotContain email
        }
    }
    
    /**
     * Property 10 (complementary): passwords are masked
     */
    test("Property 10: passwords are masked in sanitized output") {
        checkAll(100, alphanumericPasswordArb()) { password ->
            val message = "Login attempt with password=$password end"
            val sanitized = LogSanitizer.sanitize(message)
            
            // Password should be masked
            sanitized.contains("password=***") shouldBe true
        }
    }
    
    /**
     * Property 10 (complementary): CPF numbers are masked
     */
    test("Property 10: CPF numbers are masked in sanitized output") {
        checkAll(100, cpfArb()) { cpf ->
            val message = "User CPF: $cpf"
            val sanitized = LogSanitizer.sanitize(message)
            
            sanitized shouldNotContain cpf
            sanitized.contains("[CPF]") shouldBe true
        }
    }
    
    /**
     * Property 10 (complementary): sensitive map keys are masked
     */
    test("Property 10: sensitive map keys are masked") {
        checkAll(100, Arb.string(5..20), Arb.string(5..20)) { password, token ->
            val data = mapOf(
                "username" to "john",
                "password" to password,
                "token" to token,
                "action" to "login"
            )
            
            val sanitized = LogSanitizer.sanitizeMap(data)
            
            sanitized["password"] shouldBe "***"
            sanitized["token"] shouldBe "***"
            sanitized["username"] shouldBe "john"
            sanitized["action"] shouldBe "login"
        }
    }
    
    /**
     * Property 10 (complementary): non-sensitive data is preserved
     */
    test("Property 10: non-sensitive data is preserved") {
        checkAll(100, Arb.string(5..50)) { normalText ->
            // Text without sensitive patterns should be preserved
            val cleanText = normalText
                .replace(Regex("[<>@]"), "")
                .replace(Regex("password|token|email", RegexOption.IGNORE_CASE), "")
            
            if (cleanText.isNotEmpty() && !cleanText.contains(".") && !cleanText.any { it.isDigit() }) {
                val sanitized = LogSanitizer.sanitize(cleanText)
                sanitized shouldBe cleanText
            }
        }
    }
})

/**
 * Generator for JWT-like tokens
 */
fun jwtTokenArb(): Arb<String> = arbitrary {
    val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
    val payloadChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
    val payload = buildString {
        repeat(Arb.int(20..50).bind()) {
            append(payloadChars.random())
        }
    }
    val signature = buildString {
        repeat(Arb.int(20..40).bind()) {
            append(payloadChars.random())
        }
    }
    "$header.$payload.$signature"
}

/**
 * Generator for email addresses
 */
fun emailArb(): Arb<String> = arbitrary {
    val localPart = buildString {
        repeat(Arb.int(5..15).bind()) {
            append(('a'..'z').random())
        }
    }
    val domains = listOf("gmail.com", "hotmail.com", "yahoo.com", "outlook.com", "empresa.com.br")
    "$localPart@${domains.random()}"
}

/**
 * Generator for CPF numbers
 */
fun cpfArb(): Arb<String> = arbitrary {
    val digits = buildString {
        repeat(11) {
            append(Arb.int(0..9).bind())
        }
    }
    // Format as xxx.xxx.xxx-xx
    "${digits.substring(0,3)}.${digits.substring(3,6)}.${digits.substring(6,9)}-${digits.substring(9,11)}"
}

/**
 * Generator for alphanumeric passwords (no special chars that could break regex)
 */
fun alphanumericPasswordArb(): Arb<String> = arbitrary {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    buildString {
        repeat(Arb.int(8..16).bind()) {
            append(chars.random())
        }
    }
}
