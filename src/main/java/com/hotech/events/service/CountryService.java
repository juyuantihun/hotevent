package com.hotech.events.service;

import com.hotech.events.dto.CountryDTO;
import java.util.List;

public interface CountryService {
    CountryDTO getById(Long id);
    List<CountryDTO> getAll();
    CountryDTO create(CountryDTO dto);
    CountryDTO update(CountryDTO dto);
    boolean delete(Long id);
} 