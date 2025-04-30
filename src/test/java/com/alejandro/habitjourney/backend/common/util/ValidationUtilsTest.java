package com.alejandro.habitjourney.backend.common.util;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {


    // ---- PASSWORD ----
    @Test
    void validatePassword_WhenValid_ShouldReturnNull() {
        assertNull(ValidationUtils.validatePassword("Valid1Password!"));
    }

    @Test
    void validatePassword_WhenNull_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_PASSWORD_NULL, ValidationUtils.validatePassword(null));
    }

    @Test
    void validatePassword_WhenTooShort_ShouldReturnErrorMessage() {
        assertEquals("La contraseña debe tener al menos 6 caracteres", ValidationUtils.validatePassword("Ab1!"));
    }

    @Test
    void validatePassword_WhenTooLong_ShouldReturnErrorMessage() {
        String longPassword = "A1!" + "a".repeat(30);
        assertEquals("La contraseña no puede tener más de 32 caracteres", ValidationUtils.validatePassword(longPassword));
    }

    @Test
    void validatePassword_WhenMissingUppercase_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_PASSWORD_UPPERCASE, ValidationUtils.validatePassword("valid1password!"));
    }

    @Test
    void validatePassword_WhenMissingLowercase_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_PASSWORD_LOWERCASE, ValidationUtils.validatePassword("VALID1PASSWORD!"));
    }

    @Test
    void validatePassword_WhenMissingDigit_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_PASSWORD_DIGIT, ValidationUtils.validatePassword("ValidPassword!"));
    }

    @Test
    void validatePassword_WhenMissingSpecialChar_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_PASSWORD_SPECIAL_CHAR, ValidationUtils.validatePassword("Valid123Password"));
    }

    // ---- EMAIL ----
    @Test
    void validateEmail_WhenValid_ShouldReturnNull() {
        assertNull(ValidationUtils.validateEmail("user@example.com"));
    }

    @Test
    void validateEmail_WhenNull_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_EMAIL_EMPTY, ValidationUtils.validateEmail(null));
    }

    @Test
    void validateEmail_WhenEmpty_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_EMAIL_EMPTY, ValidationUtils.validateEmail(""));
    }

    @Test
    void validateEmail_WhenInvalidFormat_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_EMAIL_FORMAT, ValidationUtils.validateEmail("invalid-email"));
    }

    // ---- NAME ----
    @Test
    void validateName_WhenValid_ShouldReturnNull() {
        assertNull(ValidationUtils.validateName("John"));
    }

    @Test
    void validateName_WhenNull_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_NAME_EMPTY, ValidationUtils.validateName(null));
    }

    @Test
    void validateName_WhenEmpty_ShouldReturnErrorMessage() {
        assertEquals(ErrorMessages.VALIDATION_NAME_EMPTY, ValidationUtils.validateName("   "));
    }

    @Test
    void validateName_WhenTooShort_ShouldReturnErrorMessage() {
        assertEquals("El nombre debe tener al menos 2 caracteres", ValidationUtils.validateName("A"));
    }
}