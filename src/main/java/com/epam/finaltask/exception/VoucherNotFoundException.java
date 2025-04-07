package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VoucherNotFoundException extends RuntimeException{

    public VoucherNotFoundException(String message){
        super(message);
    }

    public VoucherNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
