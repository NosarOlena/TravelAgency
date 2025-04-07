package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VoucherPriceInvalidException extends RuntimeException{

    public VoucherPriceInvalidException(String message){
        super(message);
    }

    public VoucherPriceInvalidException(String message, Throwable cause){
        super(message, cause);
    }

}
