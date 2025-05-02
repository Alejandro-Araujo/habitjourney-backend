package com.alejandro.habitjourney.backend.common.util;

import com.alejandro.habitjourney.backend.common.constant.ErrorMessages;
import org.apache.commons.validator.routines.EmailValidator;

public class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 32;
    private static final int MIN_NAME_LENGTH = 2;

    public static String validatePassword(String password) {
        if (password == null) return ErrorMessages.VALIDATION_PASSWORD_NULL;
        if (password.length() < MIN_PASSWORD_LENGTH) return String.format(ErrorMessages.VALIDATION_PASSWORD_MIN_LENGTH, MIN_PASSWORD_LENGTH);
        if (password.length() > MAX_PASSWORD_LENGTH) return String.format(ErrorMessages.VALIDATION_PASSWORD_MAX_LENGTH, MAX_PASSWORD_LENGTH);

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            else if (Character.isLowerCase(c)) hasLowerCase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecialChar = true;
        }

        if (!hasUpperCase) return ErrorMessages.VALIDATION_PASSWORD_UPPERCASE;
        if (!hasLowerCase) return ErrorMessages.VALIDATION_PASSWORD_LOWERCASE;
        if (!hasDigit) return ErrorMessages.VALIDATION_PASSWORD_DIGIT;
        if (!hasSpecialChar) return ErrorMessages.VALIDATION_PASSWORD_SPECIAL_CHAR;

        return null; // Contraseña válida
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ErrorMessages.VALIDATION_EMAIL_EMPTY;
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            return ErrorMessages.VALIDATION_EMAIL_FORMAT;
        }
        return null;
    }

    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ErrorMessages.VALIDATION_NAME_EMPTY;
        }
        if (name.length() < MIN_NAME_LENGTH) {
            return String.format(ErrorMessages.VALIDATION_NAME_MIN_LENGTH, MIN_NAME_LENGTH);
        }
        return null;
    }
}
