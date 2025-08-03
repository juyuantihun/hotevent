package com.hotech.events.controller;

import com.hotech.events.dto.ApiResponse;
import com.hotech.events.dto.PersonDTO;
import com.hotech.events.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/person")
public class PersonController {
    @Autowired
    private PersonService personService;

    /**
     * 获取人物详情
     * @param id 人物ID
     * @return ApiResponse<PersonDTO>
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<PersonDTO>> getDetail(@PathVariable Long id) {
        try {
            PersonDTO dto = personService.getDetail(id);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取人物详情失败: " + e.getMessage()));
        }
    }
} 