package com.mylogo.visitors.service;

import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.validator.VisitorValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VisitorValidatorTest {

    private final VisitorValidator validator = new VisitorValidator();

    @Test
    void testValidate_ThrowsException_WhenVisitorIsNull() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
    }

    @Test
    void testValidate_ThrowsException_WhenNameIsEmpty() {
        Visitor visitor = new Visitor();
        visitor.setName("");

        assertThrows(IllegalArgumentException.class, () -> validator.validate(visitor));
    }

    @Test
    void testValidate_Passes_WhenVisitorIsValid() {
        Visitor visitor = new Visitor();
        visitor.setName("John Doe");

        assertDoesNotThrow(() -> validator.validate(visitor));
    }
}
