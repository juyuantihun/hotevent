package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.OrganizationDTO;
import com.hotech.events.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {
    @Autowired
    private OrganizationService organizationService;

    /**
     * 获取组织详情
     * @param id 组织ID
     * @return ApiResponse<OrganizationDTO>
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<OrganizationDTO>> getDetail(@PathVariable Long id) {
        try {
            OrganizationDTO dto = organizationService.getDetail(id);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取组织详情失败: " + e.getMessage()));
        }
    }
} 