package com.mylogo.visitors.visitormgmt.handler;

public class VisitorRetrievalException extends RuntimeException {
    public VisitorRetrievalException(String message) {
        super(message);
    }

    public VisitorRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
