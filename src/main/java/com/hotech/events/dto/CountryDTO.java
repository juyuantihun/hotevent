package com.hotech.events.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CountryDTO {
    private Long id;
    private String name;
    private String shortName;
    private Long population;
    private Double area;
    private String capital;
    private String language;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 