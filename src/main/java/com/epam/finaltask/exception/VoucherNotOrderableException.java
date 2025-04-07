package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VoucherNotOrderableException extends RuntimeException{

    public VoucherNotOrderableException(String message){
        super(message);
    }

    public VoucherNotOrderableException(String message, Throwable cause){
        super(message, cause);
    }

}
