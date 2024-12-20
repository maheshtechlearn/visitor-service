package com.mylogo.visitors.validator;

import com.mylogo.visitors.entity.Visitor;
import org.springframework.stereotype.Component;

@Component
public class VisitorValidator {

    public void validate(Visitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Visitor cannot be null");
        }
        if (visitor.getName() == null || visitor.getName().isEmpty()) {
            throw new IllegalArgumentException("Visitor name cannot be empty");
        }
    }
}
