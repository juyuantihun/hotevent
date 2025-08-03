package com.hotech.events.service.impl;

import com.hotech.events.dto.CountryDTO;
import com.hotech.events.entity.Country;
import com.hotech.events.mapper.CountryMapper;
import com.hotech.events.service.CountryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CountryServiceImpl implements CountryService {

    @Autowired
    private CountryMapper countryMapper;

    @Override
    public CountryDTO getById(Long id) {
        Country country = countryMapper.selectById(id);
        if (country == null) return null;
        CountryDTO dto = new CountryDTO();
        BeanUtils.copyProperties(country, dto);
        return dto;
    }

    @Override
    public List<CountryDTO> getAll() {
        List<Country> list = countryMapper.selectList(null);
        return list.stream().map(country -> {
            CountryDTO dto = new CountryDTO();
            BeanUtils.copyProperties(country, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CountryDTO create(CountryDTO dto) {
        Country country = new Country();
        BeanUtils.copyProperties(dto, country);
        countryMapper.insert(country);
        dto.setId(country.getId());
        return dto;
    }

    @Override
    public CountryDTO update(CountryDTO dto) {
        Country country = new Country();
        BeanUtils.copyProperties(dto, country);
        countryMapper.updateById(country);
        return dto;
    }

    @Override
    public boolean delete(Long id) {
        return countryMapper.deleteById(id) > 0;
    }
} 