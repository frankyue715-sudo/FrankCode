package com.devops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops.entity.Application;
import com.devops.entity.Deployment;
import com.devops.service.ApplicationService;
import com.devops.service.DeploymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "部署管理")
@RestController
@RequestMapping("/deployment")
public class DeploymentController {
    
    private final DeploymentService deploymentService;
    private final ApplicationService applicationService;
    
    public DeploymentController(DeploymentService deploymentService, ApplicationService applicationService) {
        this.deploymentService = deploymentService;
        this.applicationService = applicationService;
    }
    
    @ApiOperation("部署记录列表")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) String environment) {
        
        Page<Deployment> page = deploymentService.page(current, size, applicationId, environment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("创建部署")
    @PostMapping
    public ResponseEntity<Map<String, Object>> deploy(@RequestBody Map<String, String> params) {
        Long applicationId = Long.parseLong(params.get("applicationId"));
        String environment = params.get("environment");
        String version = params.get("version");
        String deployedBy = params.get("deployedBy");
        
        Application app = applicationService.getById(applicationId);
        if (app == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "应用不存在"));
        }
        
        Deployment deployment = deploymentService.startDeployment(app, environment, version, deployedBy);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "部署成功");
        result.put("data", deployment);
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("回滚部署")
    @PostMapping("/{id}/rollback")
    public ResponseEntity<Map<String, Object>> rollback(@PathVariable Long id) {
        boolean success = deploymentService.rollback(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "回滚成功" : "回滚失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取部署详情")
    @GetMapping("/{id}")
    public ResponseEntity<Deployment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deploymentService.getById(id));
    }
}
