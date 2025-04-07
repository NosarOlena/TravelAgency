package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserStatusUnchangedException extends RuntimeException {

    public UserStatusUnchangedException(String message) {
        super(message);
    }

    public UserStatusUnchangedException(String message, Throwable cause){
        super(message, cause);
    }

}
