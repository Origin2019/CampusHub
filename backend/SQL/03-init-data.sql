-- ============================================
-- CampusHub 初始数据
-- 来源：P3 详细设计文档 - 3.2.3 分类表初始数据
-- ============================================

USE campushub;

-- 初始化需求分类数据
INSERT INTO categories (name, has_review, sort_order) VALUES
    ('快递代取', 1, 1),
    ('学习辅导', 1, 2),
    ('二手交易', 0, 3),
    ('组队匹配', 0, 4);

INSERT INTO users (name, email, phone, password_hash, role) VALUES
                                                                ('张三', 'zhangsan@campus.edu', 'encrypted_phone_1', '$2a$10$placeholder', 'requester'),
                                                                ('李四', 'lisi@campus.edu',     'encrypted_phone_2', '$2a$10$placeholder', 'provider');

