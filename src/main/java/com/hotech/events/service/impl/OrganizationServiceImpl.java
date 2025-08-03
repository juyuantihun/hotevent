package com.hotech.events.service.impl;

import com.hotech.events.dto.OrganizationDTO;
import com.hotech.events.entity.Organization;
import com.hotech.events.mapper.OrganizationMapper;
import com.hotech.events.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
@Service
public class OrganizationServiceImpl implements OrganizationService {
    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public OrganizationDTO getDetail(Long id) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new RuntimeException("组织不存在");
        }
        OrganizationDTO dto = new OrganizationDTO();
        BeanUtils.copyProperties(org, dto);
        return dto;
    }
} 