package com.devops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("deploy_deployment")
public class Deployment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long applicationId;
    
    private String applicationName;
    
    private String environment;
    
    private String version;
    
    private String image;
    
    private Integer status;
    
    private String reason;
    
    private String deployedBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @TableLogic
    private Integer deleted;
}
