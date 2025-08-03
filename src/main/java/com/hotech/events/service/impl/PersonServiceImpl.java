package com.hotech.events.service.impl;

import com.hotech.events.dto.PersonDTO;
import com.hotech.events.entity.Person;
import com.hotech.events.mapper.PersonMapper;
import com.hotech.events.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    private PersonMapper personMapper;

    @Override
    public PersonDTO getDetail(Long id) {
        Person person = personMapper.selectById(id);
        if (person == null) {
            throw new RuntimeException("人物不存在");
        }
        PersonDTO dto = new PersonDTO();
        BeanUtils.copyProperties(person, dto);
        return dto;
    }
} 