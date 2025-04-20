package com.alejandro.habitjourney.backend.common.util;

import org.apache.commons.validator.routines.EmailValidator;

public class ValidationUtils {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 32;
    private static final int MIN_NAME_LENGTH = 2;

    public static String validatePassword(String password) {
        if (password == null) return "La contraseña no puede ser nula";
        if (password.length() < MIN_PASSWORD_LENGTH) return "La contraseña debe tener al menos 6 caracteres";
        if (password.length() > MAX_PASSWORD_LENGTH) return "La contraseña no puede tener más de 32 caracteres";

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

        if (!hasUpperCase) return "La contraseña debe contener al menos una mayúscula";
        if (!hasLowerCase) return "La contraseña debe contener al menos una minúscula";
        if (!hasDigit) return "La contraseña debe contener al menos un dígito";
        if (!hasSpecialChar) return "La contraseña debe contener al menos un carácter especial";

        return null; // Contraseña válida
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "El email no puede estar vacío";
        }
        if (!EmailValidator.getInstance().isValid(email)) {
            return "El formato del email no es válido";
        }
        return null;
    }

    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "El nombre no puede estar vacío";
        }
        if (name.length() < MIN_NAME_LENGTH) {
            return "El nombre debe tener al menos 2 caracteres";
        }
        return null;
    }
}
