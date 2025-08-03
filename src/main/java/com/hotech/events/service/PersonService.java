package com.hotech.events.service;

import com.hotech.events.dto.PersonDTO;

public interface PersonService {
    /**
     * 根据ID获取人物详情
     * @param id 人物ID
     * @return PersonDTO
     */
    PersonDTO getDetail(Long id);
} 