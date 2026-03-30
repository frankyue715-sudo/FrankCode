package com.devops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("deploy_application")
public class Application {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private String image;
    
    private String namespace;
    
    private Integer replicas;
    
    private String cpuRequest;
    
    private String memoryRequest;
    
    private String cpuLimit;
    
    private String memoryLimit;
    
    private String port;
    
    private String envVars;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
