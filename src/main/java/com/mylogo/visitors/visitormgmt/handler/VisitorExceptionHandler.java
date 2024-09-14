package com.mylogo.visitors.visitormgmt.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VisitorExceptionHandler {

    @ExceptionHandler(VisitorRetrievalException.class)
    public ResponseEntity<String> handleVisitorRetrievalException(VisitorRetrievalException e) {
        JSONObject response = new JSONObject();
        response.put("error", true);
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(VisitorNotFoundException.class)
    public ResponseEntity<String> visitorNotFoundException(VisitorNotFoundException e) {
        JSONObject response = new JSONObject();
        response.put("error", true);
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response.toString(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<String> visitorRetrievalException(DatabaseOperationException e) {
        JSONObject response = new JSONObject();
        response.put("error", true);
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
