package com.mylogo.visitors.handler;

public class VisitorNotFoundException extends RuntimeException {
    public VisitorNotFoundException(String message) {
        super(message);
    }
}