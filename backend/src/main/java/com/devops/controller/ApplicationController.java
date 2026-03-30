package com.devops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops.entity.Application;
import com.devops.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "应用管理")
@RestController
@RequestMapping("/application")
public class ApplicationController {
    
    private final ApplicationService applicationService;
    
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    
    @ApiOperation("应用列表")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        
        Page<Application> page = applicationService.page(current, size, name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("创建应用")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Application application) {
        application.setStatus(1);
        boolean success = applicationService.save(application);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("message", "创建成功");
            result.put("data", application);
        } else {
            result.put("message", "创建失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("更新应用")
    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Application application) {
        boolean success = applicationService.updateById(application);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "更新成功" : "更新失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("删除应用")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean success = applicationService.removeById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "删除成功" : "删除失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取应用详情")
    @GetMapping("/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }
}
