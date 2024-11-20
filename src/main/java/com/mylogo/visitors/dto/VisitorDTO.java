package com.mylogo.visitors.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class VisitorDTO {

    private Long id;

    private String name;

    private String contactNumber;
    private String email;

    private String purpose;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private long duration;
    private boolean approved;
    private LocalDateTime createdDate;
}
