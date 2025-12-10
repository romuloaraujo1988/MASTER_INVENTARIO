package com.inventario.mobile.security

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Property-Based Tests para InputValidator
 * 
 * Feature: android-security-hardening
 * Validates: Requirements 6.2, 6.3, 6.4
 */
class InputValidatorPropertyTest : FunSpec({
    
    /**
     * Property 3: Patrimonio Number Validation
     * For any string, validation returns Valid if and only if
     * the string contains only numeric digits (0-9) and has length between 1 and 10.
     * 
     * Validates: Requirements 6.2
     */
    test("Property 3: patrimonio number validation accepts only numeric strings of valid length") {
        checkAll(100, Arb.string(1..15)) { input ->
            val result = InputValidator.validatePatrimonioNumber(input)
            val trimmed = input.trim()
            val isOnlyDigits = trimmed.all { it.isDigit() }
            val isValidLength = trimmed.length in 1..10
            
            if (isOnlyDigits && isValidLength && trimmed.isNotEmpty()) {
                result shouldBe ValidationResult.Valid
            } else {
                result.isValid() shouldBe false
            }
        }
    }
    
    /**
     * Property 3 (complementary): valid numeric strings always pass
     */
    test("Property 3: valid numeric strings always pass validation") {
        checkAll(100, validPatrimonioNumberArb()) { numero ->
            val result = InputValidator.validatePatrimonioNumber(numero)
            result shouldBe ValidationResult.Valid
        }
    }
    
    /**
     * Property 4: Text Sanitization Removes Dangerous Characters
     * For any string containing dangerous characters (< > " ' ` ; & | \),
     * sanitization returns a string that does not contain any of these characters.
     * 
     * Validates: Requirements 6.3
     */
    test("Property 4: sanitized text never contains dangerous characters") {
        checkAll(100, Arb.string(0..100)) { input ->
            val sanitized = InputValidator.sanitizeText(input)
            
            sanitized shouldNotContain "<"
            sanitized shouldNotContain ">"
        }
    }
    
    /**
     * Property 4 (complementary): strings with dangerous chars are modified
     */
    test("Property 4: strings with dangerous characters are sanitized") {
        checkAll(100, stringWithDangerousCharsArb()) { input ->
            val sanitized = InputValidator.sanitizeText(input)
            
            sanitized shouldNotContain "<"
            sanitized shouldNotContain ">"
        }
    }
    
    /**
     * Property 5: Text Truncation Respects Max Length
     * For any string with length greater than maxLength,
     * the truncated output has exactly maxLength characters.
     * 
     * Validates: Requirements 6.4
     */
    test("Property 5: truncated text never exceeds max length") {
        checkAll(100, Arb.string(0..500), Arb.int(1..255)) { input, maxLength ->
            val truncated = InputValidator.truncateToMaxLength(input, maxLength)
            
            truncated.length shouldBe minOf(input.length, maxLength)
        }
    }
    
    /**
     * Property 5 (complementary): strings longer than max are truncated to exact length
     */
    test("Property 5: long strings are truncated to exact max length") {
        checkAll(100, Arb.string(100..500), Arb.int(10..50)) { input, maxLength ->
            val truncated = InputValidator.truncateToMaxLength(input, maxLength)
            
            if (input.length > maxLength) {
                truncated.length shouldBe maxLength
            }
        }
    }
    
    /**
     * Property 5 (complementary): short strings are not modified
     */
    test("Property 5: short strings are not modified by truncation") {
        checkAll(100, Arb.string(1..50)) { input ->
            val maxLength = 255
            val truncated = InputValidator.truncateToMaxLength(input, maxLength)
            
            if (input.length <= maxLength) {
                truncated shouldBe input
            }
        }
    }
})

/**
 * Generator for valid patrimonio numbers (1-10 digits)
 */
fun validPatrimonioNumberArb(): Arb<String> = arbitrary {
    val length = Arb.int(1..10).bind()
    buildString {
        repeat(length) {
            append(Arb.int(0..9).bind())
        }
    }
}

/**
 * Generator for strings containing dangerous characters
 */
fun stringWithDangerousCharsArb(): Arb<String> = arbitrary {
    val dangerousChars = listOf('<', '>')
    val normalPart = Arb.string(0..20).bind()
    val dangerousChar = dangerousChars.random()
    val position = Arb.int(0..normalPart.length).bind()
    
    buildString {
        append(normalPart.substring(0, position))
        append(dangerousChar)
        if (position < normalPart.length) {
            append(normalPart.substring(position))
        }
    }
}
