package com.mylogo.visitors.visitormgmt.handler;

public class VisitorNotFoundException extends RuntimeException {
    public VisitorNotFoundException(String message) {
        super(message);
    }
}