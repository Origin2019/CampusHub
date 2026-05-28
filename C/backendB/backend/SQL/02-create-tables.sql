-- ============================================
-- CampusHub 建表 SQL
-- 来源：P3 详细设计文档 - 3.2 建表 SQL
-- 表创建顺序遵循外键依赖关系
-- ============================================

USE campushub;

-- --------------------------------------------
-- 1. 用户表 users
-- --------------------------------------------
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    name            VARCHAR(20)     NOT NULL                 COMMENT '真实姓名',
    email           VARCHAR(100)    NOT NULL                 COMMENT '学校邮箱',
    phone           VARCHAR(255)    NOT NULL                 COMMENT '手机号（AES加密存储）',
    password_hash   VARCHAR(255)    NOT NULL                 COMMENT 'bcrypt哈希密码',
    role            ENUM('requester','provider','admin')
                                    NOT NULL                 COMMENT '用户角色',
    credit_score    DECIMAL(3,1)    NOT NULL DEFAULT 5.0     COMMENT '信用评分',
    avatar          VARCHAR(500)    DEFAULT NULL             COMMENT '头像URL',
    status          TINYINT(1)      NOT NULL DEFAULT 1       COMMENT '状态：1=正常, 0=禁用',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- --------------------------------------------
-- 2. 需求分类表 categories
-- --------------------------------------------
CREATE TABLE categories (
    id              INT             NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    name            VARCHAR(20)     NOT NULL                 COMMENT '分类名称',
    icon            VARCHAR(255)    DEFAULT NULL             COMMENT '分类图标URL',
    has_review      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否支持评价：1=支持, 0=不支持',
    sort_order      INT             NOT NULL DEFAULT 0       COMMENT '排序权重',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求分类表';

-- --------------------------------------------
-- 3. 需求表 tasks
-- --------------------------------------------
CREATE TABLE tasks (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '需求ID',
    requester_id    BIGINT          NOT NULL                 COMMENT '发布者ID',
    category_id     INT             NOT NULL                 COMMENT '分类ID',
    title           VARCHAR(50)     NOT NULL                 COMMENT '需求标题',
    description     VARCHAR(500)    NOT NULL                 COMMENT '详细描述',
    reward          DECIMAL(10,2)   DEFAULT NULL             COMMENT '报酬金额',
    status          ENUM('published','in_progress','completed','cancelled')
                                    NOT NULL DEFAULT 'published' COMMENT '需求状态',
    location        VARCHAR(100)    DEFAULT NULL             COMMENT '地点',
    deadline        DATETIME        DEFAULT NULL             COMMENT '截止时间',
    published_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_requester_id (requester_id),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_published_at (published_at),
    CONSTRAINT fk_task_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_task_category  FOREIGN KEY (category_id)  REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- --------------------------------------------
-- 4. 订单表 orders
-- --------------------------------------------
CREATE TABLE orders (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
    task_id         BIGINT          NOT NULL                 COMMENT '关联需求ID',
    requester_id    BIGINT          NOT NULL                 COMMENT '需求方ID',
    provider_id     BIGINT          NOT NULL                 COMMENT '服务方/参与者ID',
    status          ENUM('accepted','in_progress','completed','cancelled')
                                    NOT NULL DEFAULT 'accepted' COMMENT '订单状态',
    accepted_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接单/参与时间',
    completed_at    DATETIME        DEFAULT NULL             COMMENT '完成时间',
    cancelled_at    DATETIME        DEFAULT NULL             COMMENT '取消时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_task_id (task_id),
    KEY idx_requester_id (requester_id),
    KEY idx_provider_id (provider_id),
    KEY idx_status (status),
    CONSTRAINT fk_order_task      FOREIGN KEY (task_id)      REFERENCES tasks(id),
    CONSTRAINT fk_order_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_order_provider  FOREIGN KEY (provider_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单/参与记录表';

-- --------------------------------------------
-- 5. 评价表 reviews
-- --------------------------------------------
CREATE TABLE reviews (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
    order_id        BIGINT          NOT NULL                 COMMENT '关联订单ID',
    score           TINYINT         NOT NULL                 COMMENT '评分，1-5',
    comment         VARCHAR(200)    DEFAULT NULL             COMMENT '评价内容',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_id (order_id),
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT chk_score CHECK (score >= 1 AND score <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- --------------------------------------------
-- 6. 通知表 notifications
-- --------------------------------------------
CREATE TABLE notifications (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    receiver_id     BIGINT          NOT NULL                 COMMENT '接收者ID',
    type            VARCHAR(30)     NOT NULL                 COMMENT '通知类型',
    title           VARCHAR(100)    NOT NULL                 COMMENT '通知标题',
    content         TEXT            NOT NULL                 COMMENT '通知内容',
    is_read         TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否已读',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_receiver_read (receiver_id, is_read),
    KEY idx_created_at (created_at),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
