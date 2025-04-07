package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserUsernameUnchangedException extends RuntimeException {

    public UserUsernameUnchangedException(String message) {
        super(message);
    }

    public UserUsernameUnchangedException(String message, Throwable cause){
        super(message, cause);
    }

}
