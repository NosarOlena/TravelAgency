package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UserBalanceInvalid extends RuntimeException {

    public UserBalanceInvalid(String message) {
        super(message);
    }

    public UserBalanceInvalid(String message, Throwable cause){
        super(message, cause);
    }

}
