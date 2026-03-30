package com.devops.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops.entity.Pipeline;
import com.devops.entity.PipelineRun;
import com.devops.service.PipelineRunService;
import com.devops.service.PipelineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "CI/CD流水线")
@RestController
@RequestMapping("/pipeline")
public class PipelineController {
    
    private final PipelineService pipelineService;
    private final PipelineRunService pipelineRunService;
    
    public PipelineController(PipelineService pipelineService, PipelineRunService pipelineRunService) {
        this.pipelineService = pipelineService;
        this.pipelineRunService = pipelineRunService;
    }
    
    @ApiOperation("流水线列表")
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        
        Page<Pipeline> page = pipelineService.page(current, size, name);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pages", page.getPages());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("创建流水线")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Pipeline pipeline) {
        pipeline.setStatus(1);
        boolean success = pipelineService.save(pipeline);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("message", "创建成功");
            result.put("data", pipeline);
        } else {
            result.put("message", "创建失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("更新流水线")
    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestBody Pipeline pipeline) {
        boolean success = pipelineService.updateById(pipeline);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "更新成功" : "更新失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("删除流水线")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean success = pipelineService.removeById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", success ? "删除成功" : "删除失败");
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取流水线详情")
    @GetMapping("/{id}")
    public ResponseEntity<Pipeline> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pipelineService.getById(id));
    }
    
    @ApiOperation("触发流水线")
    @PostMapping("/{id}/run")
    public ResponseEntity<Map<String, Object>> run(
            @PathVariable Long id,
            @RequestParam(defaultValue = "manual") String triggerType,
            @RequestParam(defaultValue = "admin") String triggerUser) {
        
        Pipeline pipeline = pipelineService.getById(id);
        if (pipeline == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "流水线不存在"));
        }
        
        PipelineRun run = pipelineService.startRun(pipeline, triggerType, triggerUser);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "流水线已触发");
        result.put("runId", run.getId());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("流水线执行记录")
    @GetMapping("/{id}/runs")
    public ResponseEntity<Map<String, Object>> runs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        
        Page<PipelineRun> page = pipelineRunService.page(current, size, id, status);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        
        return ResponseEntity.ok(result);
    }
    
    @ApiOperation("获取构建日志")
    @GetMapping("/run/{runId}/logs")
    public ResponseEntity<Map<String, Object>> getLogs(@PathVariable Long runId) {
        PipelineRun run = pipelineRunService.getById(runId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("logs", run != null ? run.getLogs() : "");
        result.put("status", run != null ? run.getStatus() : 0);
        
        return ResponseEntity.ok(result);
    }
}
