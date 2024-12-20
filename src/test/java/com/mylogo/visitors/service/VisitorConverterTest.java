package com.mylogo.visitors.service;

import com.mylogo.visitors.convertor.VisitorConverter;
import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VisitorConverterTest {

    private final VisitorConverter converter = new VisitorConverter();

    @Test
    void testToDTO() {
        // Arrange
        Visitor visitor = new Visitor();
        visitor.setId(1L);
        visitor.setName("John Doe");

        // Act
        VisitorDTO dto = converter.toDTO(visitor);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
    }

    @Test
    void testToDTOList() {
        // Arrange
        Visitor visitor = new Visitor();
        visitor.setId(1L);

        // Act
        List<VisitorDTO> dtos = converter.toDTOList(List.of(visitor));

        // Assert
        assertNotNull(dtos);
        assertEquals(1, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
    }
}
