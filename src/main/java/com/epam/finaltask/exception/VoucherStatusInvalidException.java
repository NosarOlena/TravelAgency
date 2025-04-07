package com.epam.finaltask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VoucherStatusInvalidException extends RuntimeException{

    public VoucherStatusInvalidException(String message){
        super(message);
    }

    public VoucherStatusInvalidException(String message, Throwable cause){
        super(message, cause);
    }

}
