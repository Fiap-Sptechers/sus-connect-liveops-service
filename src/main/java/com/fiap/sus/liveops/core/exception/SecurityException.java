package com.fiap.sus.liveops.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Access denied")
public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }
}
