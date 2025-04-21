package com.alejandro.habitjourney.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String title;
    private String detail;
    private LocalDateTime timestamp;
}
