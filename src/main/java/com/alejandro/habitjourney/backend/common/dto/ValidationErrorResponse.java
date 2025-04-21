package com.alejandro.habitjourney.backend.common.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse extends ErrorResponse {

    private final List<ValidationError> validationErrors = new ArrayList<>();

    public ValidationErrorResponse(int status, String title, String detail, LocalDateTime timestamp) {
        super(status, title, detail, timestamp);
    }

    public void addValidationError(String field, String message) {
        validationErrors.add(new ValidationError(field, message));
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
