package com.mylogo.visitors.visitormgmt.controller;

import com.mylogo.visitors.visitormgmt.config.VisitorDataSourceConfig;
import com.mylogo.visitors.visitormgmt.model.Visitor;
import com.mylogo.visitors.visitormgmt.service.VisitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class VisitorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VisitorService visitorService;

    @Mock
    private VisitorDataSourceConfig visitorDataSourceConfig;

    @InjectMocks
    private VisitorController visitorController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(VisitorControllerTest.class);
    }

    @Test
    public void testGetAllVisitors() throws Exception {
        List<Visitor> visitors = Arrays.asList(
                new Visitor(1L, "John Doe", "1234567890", "Meeting", LocalDateTime.now(), LocalDateTime.now(), 50L, true),
                new Visitor(2L, "Jane Doe", "0987654321", "Interview", LocalDateTime.now(), LocalDateTime.now(), 40L, false)
        );
        when(visitorService.getAllVisitors()).thenReturn(visitors);

        mockMvc.perform(get("/visitors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));
    }

    @Test
    public void testGetVisitorByIdFound() throws Exception {
        Optional<Visitor> visitor = Optional.of(new Visitor(1L, "John Doe", "1234567890", "Meeting", LocalDateTime.now(), LocalDateTime.now(), 90L, true));
        when(visitorService.getVisitorById(1L)).thenReturn(visitor);

        mockMvc.perform(get("/visitors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetVisitorByIdNotFound() throws Exception {
        when(visitorService.getVisitorById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/visitors/1"))
                .andExpect(status().isNotFound());
    }

}