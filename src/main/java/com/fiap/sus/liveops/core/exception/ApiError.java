package com.fiap.sus.liveops.core.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {}
