package com.devops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops.entity.Alert;
import com.devops.service.AlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "告警管理")
@RestController
@RequestMapping("/alert")
public class AlertController {
    
    private final AlertService alertService;
    
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }
    
    @ApiOperation("告警列表")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer severity) {
        
        Page<Alert> page = alertService.page(current, size, target, status, severity);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("创建告警规则")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Alert alert) {
        alert.setStatus(1);
        boolean success = alertService.save(alert);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("message", "创建成功");
            result.put("data", alert);
        } else {
            result.put("message", "创建失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("更新告警规则")
    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Alert alert) {
        boolean success = alertService.updateById(alert);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "更新成功" : "更新失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("删除告警规则")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean success = alertService.removeById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "删除成功" : "删除失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("处理告警")
    @PostMapping("/{id}/handle")
    public ResponseEntity<Map<String, Object>> handle(@PathVariable Long id) {
        boolean success = alertService.changeStatus(id, 2);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "处理成功" : "处理失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取活跃告警数量")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> count() {
        int count = alertService.getActiveAlertCount();
        
        Map<String, Object> result = new HashMap<>();
        result.put("activeCount", count);
        
        return ResponseEntity.ok(result);
    }
}
