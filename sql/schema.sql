-- ============================================================
-- 宠物店后台管理系统 数据库结构（MySQL 8.0，utf8mb4）
-- 幂等脚本：可重复执行，每次重建全部表
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS bill_item;
DROP TABLE IF EXISTS bill;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS service_item;
DROP TABLE IF EXISTS sys_user;

-- 系统用户（管理员/店员）
CREATE TABLE sys_user (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '登录名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    real_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
    role        VARCHAR(20)  NOT NULL COMMENT '角色：ADMIN/STAFF',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

-- 客户（会员）
CREATE TABLE customer (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone       VARCHAR(20)  NOT NULL COMMENT '手机号',
    gender      TINYINT      DEFAULT NULL COMMENT '1男 2女',
    birthday    DATE         DEFAULT NULL COMMENT '生日',
    level       VARCHAR(20)  NOT NULL DEFAULT '普通' COMMENT '会员等级：普通/白银/黄金/白金',
    points      INT          NOT NULL DEFAULT 0 COMMENT '累计积分（1元=1分）',
    notes       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME     NOT NULL COMMENT '注册时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    KEY idx_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='客户（会员）';

-- 宠物档案
CREATE TABLE pet (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT       NOT NULL COMMENT '主人客户ID',
    name        VARCHAR(50)  NOT NULL COMMENT '宠物名',
    species     VARCHAR(30)  DEFAULT NULL COMMENT '品种类型：狗/猫/兔/仓鼠/鸟类/其他',
    breed       VARCHAR(50)  DEFAULT NULL COMMENT '具体品种',
    gender      TINYINT      DEFAULT NULL COMMENT '1公 2母',
    birth_date  DATE         DEFAULT NULL COMMENT '出生日期',
    weight      DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
    sterilized  TINYINT      DEFAULT 0 COMMENT '0未绝育 1已绝育',
    notes       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME     NOT NULL COMMENT '建档时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    KEY idx_customer (customer_id),
    CONSTRAINT fk_pet_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='宠物档案';

-- 商品分类
CREATE TABLE product_category (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME     NOT NULL COMMENT '创建时间',
    update_time DATETIME     NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品分类';

-- 商品
CREATE TABLE product (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT        NOT NULL COMMENT '分类ID',
    name        VARCHAR(100)  NOT NULL COMMENT '商品名',
    spec        VARCHAR(50)   DEFAULT NULL COMMENT '规格',
    unit        VARCHAR(10)   DEFAULT NULL COMMENT '单位：袋/个/瓶/盒',
    price       DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost        DECIMAL(10,2) DEFAULT NULL COMMENT '进货成本',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    notes       VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL COMMENT '更新时间',
    KEY idx_category (category_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品';

-- 服务项目
CREATE TABLE service_item (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(100)  NOT NULL COMMENT '服务名',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    duration_min INT          DEFAULT NULL COMMENT '时长（分钟）',
    description VARCHAR(500)  DEFAULT NULL COMMENT '说明',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NOT NULL COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='服务项目';

-- 账单
CREATE TABLE bill (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    bill_no         VARCHAR(30)   NOT NULL COMMENT '单号',
    customer_id     BIGINT        NOT NULL COMMENT '客户ID',
    pet_id          BIGINT        DEFAULT NULL COMMENT '本次服务对象（可空）',
    type            VARCHAR(10)   NOT NULL COMMENT 'PRODUCT/SERVICE/MIXED',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '明细总额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '优惠金额',
    pay_amount      DECIMAL(10,2) DEFAULT NULL COMMENT '实收金额',
    pay_method      VARCHAR(20)   DEFAULT NULL COMMENT 'CASH/WECHAT/ALIPAY/CARD',
    status          VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/CANCELLED',
    operator_id     BIGINT        DEFAULT NULL COMMENT '操作员ID',
    remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    pay_time        DATETIME      DEFAULT NULL COMMENT '收款时间',
    create_time     DATETIME      NOT NULL COMMENT '开单时间',
    update_time     DATETIME      NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_customer (customer_id),
    KEY idx_status_time (status, create_time),
    CONSTRAINT fk_bill_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_bill_pet FOREIGN KEY (pet_id) REFERENCES pet (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='账单';

-- 账单明细：名称/单价为开单时快照；ref_id 多态引用商品或服务，不设外键
CREATE TABLE bill_item (
    id         BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    bill_id    BIGINT        NOT NULL COMMENT '账单ID',
    item_type  VARCHAR(10)   NOT NULL COMMENT 'PRODUCT/SERVICE',
    ref_id     BIGINT        NOT NULL COMMENT '商品或服务ID',
    item_name  VARCHAR(100)  NOT NULL COMMENT '名称快照',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价快照',
    quantity   INT           NOT NULL COMMENT '数量',
    amount     DECIMAL(10,2) NOT NULL COMMENT '小计',
    KEY idx_bill (bill_id),
    CONSTRAINT fk_item_bill FOREIGN KEY (bill_id) REFERENCES bill (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='账单明细';

-- 服务预约
CREATE TABLE appointment (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id      BIGINT       NOT NULL COMMENT '客户ID',
    pet_id           BIGINT       NOT NULL COMMENT '宠物ID',
    service_id       BIGINT       NOT NULL COMMENT '服务ID',
    staff_id         BIGINT       DEFAULT NULL COMMENT '负责员工ID',
    appointment_time DATETIME    NOT NULL COMMENT '预约到店时间',
    status           VARCHAR(20)  NOT NULL DEFAULT 'BOOKED' COMMENT 'BOOKED/IN_PROGRESS/COMPLETED/CANCELLED',
    remark           VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time      DATETIME     NOT NULL COMMENT '创建时间',
    update_time      DATETIME     NOT NULL COMMENT '更新时间',
    KEY idx_time (appointment_time),
    KEY idx_status (status),
    CONSTRAINT fk_appoint_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_appoint_pet FOREIGN KEY (pet_id) REFERENCES pet (id),
    CONSTRAINT fk_appoint_service FOREIGN KEY (service_id) REFERENCES service_item (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='服务预约';

SET FOREIGN_KEY_CHECKS = 1;
