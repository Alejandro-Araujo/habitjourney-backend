package com.alejandro.habitjourney.backend.common.util;

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
        assertEquals("La contraseña no puede ser nula", ValidationUtils.validatePassword(null));
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
        assertEquals("La contraseña debe contener al menos una mayúscula", ValidationUtils.validatePassword("valid1password!"));
    }

    @Test
    void validatePassword_WhenMissingLowercase_ShouldReturnErrorMessage() {
        assertEquals("La contraseña debe contener al menos una minúscula", ValidationUtils.validatePassword("VALID1PASSWORD!"));
    }

    @Test
    void validatePassword_WhenMissingDigit_ShouldReturnErrorMessage() {
        assertEquals("La contraseña debe contener al menos un dígito", ValidationUtils.validatePassword("ValidPassword!"));
    }

    @Test
    void validatePassword_WhenMissingSpecialChar_ShouldReturnErrorMessage() {
        assertEquals("La contraseña debe contener al menos un carácter especial", ValidationUtils.validatePassword("Valid123Password"));
    }

    // ---- EMAIL ----
    @Test
    void validateEmail_WhenValid_ShouldReturnNull() {
        assertNull(ValidationUtils.validateEmail("user@example.com"));
    }

    @Test
    void validateEmail_WhenNull_ShouldReturnErrorMessage() {
        assertEquals("El email no puede estar vacío", ValidationUtils.validateEmail(null));
    }

    @Test
    void validateEmail_WhenEmpty_ShouldReturnErrorMessage() {
        assertEquals("El email no puede estar vacío", ValidationUtils.validateEmail(""));
    }

    @Test
    void validateEmail_WhenInvalidFormat_ShouldReturnErrorMessage() {
        assertEquals("El formato del email no es válido", ValidationUtils.validateEmail("invalid-email"));
    }

    // ---- NAME ----
    @Test
    void validateName_WhenValid_ShouldReturnNull() {
        assertNull(ValidationUtils.validateName("John"));
    }

    @Test
    void validateName_WhenNull_ShouldReturnErrorMessage() {
        assertEquals("El nombre no puede estar vacío", ValidationUtils.validateName(null));
    }

    @Test
    void validateName_WhenEmpty_ShouldReturnErrorMessage() {
        assertEquals("El nombre no puede estar vacío", ValidationUtils.validateName("   "));
    }

    @Test
    void validateName_WhenTooShort_ShouldReturnErrorMessage() {
        assertEquals("El nombre debe tener al menos 2 caracteres", ValidationUtils.validateName("A"));
    }
}