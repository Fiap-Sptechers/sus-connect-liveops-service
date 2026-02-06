package com.fiap.sus.liveops.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAttendanceStatusException extends RuntimeException {

    public InvalidAttendanceStatusException(String message) {
        super(message);
    }

}
