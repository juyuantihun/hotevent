package com.hotech.events.service;

import com.hotech.events.dto.OrganizationDTO;

public interface OrganizationService {
    /**
     * 根据ID获取组织详情
     * @param id 组织ID
     * @return OrganizationDTO
     */
    OrganizationDTO getDetail(Long id);
} 