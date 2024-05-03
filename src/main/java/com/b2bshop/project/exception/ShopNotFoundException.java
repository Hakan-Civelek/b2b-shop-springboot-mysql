package com.b2bshop.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShopNotFoundException extends RuntimeException{
    public ShopNotFoundException(String message) {
        super(message);
    }
}