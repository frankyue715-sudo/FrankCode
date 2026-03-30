package com.devops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops.entity.MonitorData;
import com.devops.service.MonitorDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "系统监控")
@RestController
@RequestMapping("/monitor")
public class MonitorController {
    
    private final MonitorDataService monitorDataService;
    
    public MonitorController(MonitorDataService monitorDataService) {
        this.monitorDataService = monitorDataService;
    }
    
    @ApiOperation("监控数据列表")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String targetType) {
        
        Page<MonitorData> page = monitorDataService.page(current, size, target, targetType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取最新监控数据")
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> latest(@RequestParam String target) {
        List<MonitorData> data = monitorDataService.getLatestByTarget(target);
        
        Map<String, Object> result = new HashMap<>();
        result.put("target", target);
        result.put("data", data);
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取系统概览")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();
        
        String[] targets = {"server-1", "server-2", "app-prod-1"};
        for (String target : targets) {
            monitorDataService.collectMockData(target, "server");
        }
        
        result.put("totalServers", 3);
        result.put("totalApps", 12);
        result.put("cpuUsage", 45.5);
        result.put("memoryUsage", 62.3);
        result.put("diskUsage", 38.7);
        result.put("networkIn", 1250.5);
        result.put("networkOut", 890.2);
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("手动采集监控数据")
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collect(
            @RequestParam String target,
            @RequestParam String targetType) {
        
        monitorDataService.collectMockData(target, targetType);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "数据采集成功");
        
        return ResponseEntity.ok(result);
    }
}
