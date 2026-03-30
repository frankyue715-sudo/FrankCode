-- DevOps Platform Database Initialization Script

-- Create database
CREATE DATABASE IF NOT EXISTS devops_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE devops_platform;

-- User table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status INT DEFAULT 1 COMMENT '1:正常 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- CI/CD Pipeline table
CREATE TABLE IF NOT EXISTS ci_pipeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    repository_url VARCHAR(500),
    branch VARCHAR(100) DEFAULT 'main',
    build_script TEXT,
    dockerfile TEXT,
    registry_url VARCHAR(500),
    image_name VARCHAR(200),
    status INT DEFAULT 1 COMMENT '1:启用 0:禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CI/CD流水线';

-- Pipeline Run table
CREATE TABLE IF NOT EXISTS ci_pipeline_run (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pipeline_id BIGINT NOT NULL,
    pipeline_name VARCHAR(100),
    commit_id VARCHAR(50),
    commit_message VARCHAR(500),
    branch VARCHAR(100),
    status INT DEFAULT 0 COMMENT '0:等待 1:运行中 2:成功 3:失败 4:取消',
    duration INT COMMENT '执行时长(秒)',
    logs TEXT,
    trigger_type VARCHAR(20) COMMENT 'manual:手动 webhook:钩子 trigger:触发器',
    trigger_user VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    deleted INT DEFAULT 0,
    INDEX idx_pipeline_id (pipeline_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线执行记录';

-- Application table
CREATE TABLE IF NOT EXISTS deploy_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image VARCHAR(200),
    namespace VARCHAR(100) DEFAULT 'default',
    replicas INT DEFAULT 1,
    cpu_request VARCHAR(50) DEFAULT '100m',
    memory_request VARCHAR(50) DEFAULT '256Mi',
    cpu_limit VARCHAR(50) DEFAULT '500m',
    memory_limit VARCHAR(50) DEFAULT '1Gi',
    port VARCHAR(50) DEFAULT '8080',
    env_vars TEXT,
    status INT DEFAULT 1 COMMENT '1:运行中 0:已停止',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用表';

-- Deployment table
CREATE TABLE IF NOT EXISTS deploy_deployment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    application_name VARCHAR(100),
    environment VARCHAR(50) COMMENT 'dev:开发 test:测试 prod:生产',
    version VARCHAR(50),
    image VARCHAR(200),
    status INT DEFAULT 0 COMMENT '0:等待 1:部署中 2:成功 3:失败 4:回滚中',
    reason VARCHAR(500),
    deployed_by VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    deleted INT DEFAULT 0,
    INDEX idx_application_id (application_id),
    INDEX idx_environment (environment),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署记录';

-- Monitor Data table
CREATE TABLE IF NOT EXISTS monitor_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) COMMENT 'server:服务器 container:容器 app:应用',
    cpu_usage DOUBLE,
    memory_usage DOUBLE,
    disk_usage DOUBLE,
    network_in DOUBLE,
    network_out DOUBLE,
    process_count INT,
    response_time DOUBLE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_target (target),
    INDEX idx_target_type (target_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控数据';

-- Alert table
CREATE TABLE IF NOT EXISTS monitor_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    target VARCHAR(100),
    metric VARCHAR(50) COMMENT 'cpu:CPU memory:内存 disk:磁盘',
    condition VARCHAR(20) COMMENT 'gt:大于 lt:小于 eq:等于',
    threshold DOUBLE,
    severity INT DEFAULT 1 COMMENT '1:info 2:warning 3:critical',
    status INT DEFAULT 1 COMMENT '1:启用 0:禁用',
    message VARCHAR(500),
    notify_type VARCHAR(50) COMMENT 'email:邮件 sms:短信 webhook:钩子',
    notify_users VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_target (target),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则';

-- Insert default admin user (password: admin123)
INSERT INTO sys_user (username, password, nickname, email, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@devops.com', 1);

-- Insert sample pipelines
INSERT INTO ci_pipeline (name, description, repository_url, branch, build_script, status) VALUES
('frontend-app-pipeline', '前端应用构建流水线', 'https://github.com/example/frontend-app', 'main', 'npm install && npm run build', 1),
('backend-api-pipeline', '后端API构建流水线', 'https://github.com/example/backend-api', 'main', 'mvn clean package', 1),
('microservice-pipeline', '微服务构建流水线', 'https://github.com/example/microservices', 'develop', 'docker build -t app .', 1);

-- Insert sample applications
INSERT INTO deploy_application (name, description, image, replicas, port, status) VALUES
('user-service', '用户服务', 'nginx:latest', 3, '8080', 1),
('order-service', '订单服务', 'nginx:latest', 2, '8081', 1),
('payment-service', '支付服务', 'nginx:latest', 2, '8082', 1),
('frontend-web', '前端Web应用', 'nginx:latest', 2, '80', 1);

-- Insert sample alerts
INSERT INTO monitor_alert (name, target, metric, condition, threshold, severity, status, message, notify_type) VALUES
('CPU使用率告警', 'server-1', 'cpu', 'gt', 80, 2, 1, '服务器CPU使用率超过80%', 'email'),
('内存使用率告警', 'server-1', 'memory', 'gt', 85, 3, 1, '服务器内存使用率超过85%', 'email'),
('磁盘使用率告警', 'server-2', 'disk', 'gt', 90, 2, 1, '服务器磁盘使用率超过90%', 'webhook');
