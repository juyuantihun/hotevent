package com.hotech.events.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PersonDTO {
    private Long id;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private Long countryId;
    private Long organizationId;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
} 