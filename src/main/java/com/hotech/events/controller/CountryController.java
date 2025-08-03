package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.CountryDTO;
import com.hotech.events.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/country")
@Tag(name = "国家管理", description = "国家实体相关API接口")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping("/list")
    @Operation(summary = "获取国家列表")
    public ResponseEntity<ApiResponse<List<CountryDTO>>> list() {
        try {
            List<CountryDTO> list = countryService.getAll();
            return ResponseEntity.ok(ApiResponse.success("查询成功", list));
        } catch (Exception e) {
            log.error("获取国家列表失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取国家详情")
    public ResponseEntity<ApiResponse<CountryDTO>> detail(@PathVariable Long id) {
        try {
            CountryDTO dto = countryService.getById(id);
            return ResponseEntity.ok(ApiResponse.success("查询成功", dto));
        } catch (Exception e) {
            log.error("获取国家详情失败", e);
            return ResponseEntity.ok(ApiResponse.error("查询失败：" + e.getMessage()));
        }
    }
} 