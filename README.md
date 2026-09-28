# 宠物店管理系统

面向单店/连锁门店的宠物店业务管理系统，覆盖 **客户会员 → 宠物档案 → 商品服务 → 预约 → 开单收银 → 数据看板** 的完整经营闭环，解决门店在账单、服务商品、客人、宠物等事务管理上的不便，助力规范化经营。

- 后端：Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + JWT + MySQL 8.0（JDK 21）
- 前端：Vue 3 + Vite 5 + Element Plus + Pinia + ECharts
- 交付形态：**单端口一体化**（前端 `dist` 打进 Spring Boot `static`，启动一个 jar 即可访问）

---

## 一、快速启动

### 1. 环境要求

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21（实测 21.0.12） | `java -version` 确认 |
| Maven | 3.8+ | 首次构建需联网下载依赖 |
| MySQL | 8.0+ | 实测 8.0.46，端口 3306 |
| Node.js | 18+（仅前端开发态需要） | 若直接用已打包产物可跳过 |

### 2. 建库并导入数据

```bash
# 登录 MySQL（按实际账号调整）
mysql -uroot -proot

# 建库
CREATE DATABASE IF NOT EXISTS petstore DEFAULT CHARSET utf8mb4;

# 导入（二选一）
# 方式 A：命令行
mysql -uroot -proot petstore < sql/schema.sql
mysql -uroot -proot petstore < sql/data.sql

# 方式 B：Navicat 中选中 petstore 库，依次执行 sql/schema.sql、sql/data.sql
```

> `schema.sql` 为幂等脚本（DROP + CREATE），重复执行会重建全部表。
> `data.sql` 为测试数据：22 位客户 / 30 只宠物 / 32 件商品 / 10 项服务 / 150 张账单 / 296 条明细 / 30 条预约。

### 3. 修改数据库连接

`backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petstore?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### 4. 启动

> 如果拿到的包里附带了构建好的 `petstore-backend-1.0.0.jar`（前端已内嵌其中），
> 可跳过编译，直接执行下面的启动命令即可。

**方式 A（推荐，直接用已构建产物）：**

```bash
# jar 位于 backend/target/ 下（若已在包根目录则去掉 backend/target/ 前缀）
java -jar backend/target/petstore-backend-1.0.0.jar
```

**方式 B（重新编译打包）：**

```bash
# 1) 前端构建（前端有改动时执行；无改动可跳过，static 中已有产物）
cd frontend && npm install && npm run build

# 2) 拷贝产物到后端静态目录
cp -r frontend/dist/* backend/src/main/resources/static/

# 3) 打包并启动
cd backend && mvn -DskipTests package
java -jar target/petstore-backend-1.0.0.jar
```

启动后浏览器访问 **http://localhost:8080**（如需换端口：`java -jar xxx.jar --server.port=18080`）。

### 5. 默认账号

| 账号 | 密码 | 角色 | 权限 |
| --- | --- | --- | --- |
| `admin` | `123456` | 管理员 ADMIN | 全部功能，含员工账号管理 |
| `staff` | `123456` | 店员 STAFF | 业务功能，**不可**访问员工管理 |

> 首次启动由 `DataInitializer` 自动写入这两个账号（已存在则跳过）。

### 6. 前端开发态（可选）

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173，/api 已代理到 8080
```

---

## 二、功能模块

| 模块 | 能力 |
| --- | --- |
| 登录鉴权 | JWT 登录、修改密码、Token 过期踢回登录页 |
| 数据看板 | 今日/本月营收与订单量、客户数、宠物数、本月新增客户、待到店预约；近 7 日营收折线、商品/服务占比环形图、热销商品 TOP5、热门服务 TOP5 |
| 客户管理 | 分页/模糊查询、新增编辑、手机号唯一校验、会员等级与积分、详情抽屉（名下宠物 + 近 5 笔账单）、删除前校验（有宠物/账单/预约则拒绝） |
| 宠物管理 | 分页查询、建档与编辑、所属客户、品种/体重/绝育等档案字段 |
| 商品管理 | 商品分页/上下架/删除（被账单引用时建议下架而非删除）、**商品分类**独立维护 |
| 服务管理 | 服务项目 CRUD、价格与时长、上下架 |
| 预约管理 | 预约创建、状态流转（待到店 → 进行中 → 已完成 / 已取消），非法流转拒绝 |
| 账单收银 | 选客户/宠物 → 加商品与服务 → 改数量 → 自动算合计/优惠/应收 → 开单 → 收款（现金/微信/支付宝/银行卡）→ 自动扣库存、记积分、升等级；待支付单可取消 |
| 员工管理 | 账号 CRUD、重置密码、启停、禁止删除当前登录账号（仅 ADMIN） |

---

## 三、数据库设计（9 张表）

| 表名 | 说明 | 关键字段 |
| --- | --- | --- |
| `sys_user` | 系统用户 | username(唯一)、password(BCrypt)、role(ADMIN/STAFF)、status |
| `customer` | 客户会员 | phone、level(普通/白银/黄金/白金)、points(1元=1分) |
| `pet` | 宠物档案 | customer_id(FK)、species、breed、weight、sterilized |
| `product_category` | 商品分类 | name(唯一)、sort |
| `product` | 商品 | category_id(FK)、price、cost、stock、status(1上架/0下架) |
| `service_item` | 服务项目 | price、duration_min、status |
| `bill` | 账单 | bill_no(唯一)、type(PRODUCT/SERVICE/MIXED)、total_amount、discount_amount、pay_amount、pay_method、status(UNPAID/PAID/CANCELLED) |
| `bill_item` | 账单明细 | bill_id(FK)、item_type、ref_id、**item_name/unit_price 快照** |
| `appointment` | 服务预约 | customer_id/pet_id/service_id(FK)、appointment_time、status(BOOKED/IN_PROGRESS/COMPLETED/CANCELLED) |

---

## 四、接口清单

统一前缀 `/api`，统一响应体 `{code, msg, data}`（`200` 成功 / `400` 业务错误 / `401` 未登录 / `403` 无权限 / `500` 系统异常）。除登录外均需在请求头携带 `Authorization: Bearer <token>`。

### 鉴权 `/api/auth`
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/login` | 登录，返回 token |
| PUT | `/password` | 修改当前用户密码 |

### 客户 `/api/customer`
`GET /page`、`GET /list`、`GET /{id}`（含宠物与近期账单）、`POST`、`PUT`、`DELETE /{id}`

### 宠物 `/api/pet`
`GET /page`、`GET /list?customerId=`、`POST`、`PUT`、`DELETE /{id}`

### 商品 `/api/product`
`GET /page`、`GET /list`、`POST`、`PUT`、`DELETE /{id}`
分类：`GET /category/list`、`POST /category`、`PUT /category`、`DELETE /category/{id}`

### 服务 `/api/service-item`
`GET /page`、`GET /list`、`POST`、`PUT`、`DELETE /{id}`

### 账单 `/api/bill`
| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/` | 开单（明细只传 `itemType`/`refId`/`quantity`，价格服务端快照） |
| PUT | `/{id}/pay?method=` | 收款，method ∈ CASH/WECHAT/ALIPAY/CARD |
| PUT | `/{id}/cancel` | 取消（仅待支付） |
| GET | `/page` | 分页，支持单号/类型/状态/日期区间/客户筛选 |
| GET | `/{id}` | 账单详情（含明细） |

### 预约 `/api/appointment`
`GET /page`、`POST`、`PUT`、`PUT /{id}/status?status=`、`DELETE /{id}`

### 看板 `/api/dashboard`
`GET /summary`、`GET /revenue-trend`、`GET /revenue-ratio`、`GET /top-products`、`GET /top-services`

### 员工 `/api/sys-user`（**仅 ADMIN**）
`GET /page`、`POST`、`PUT`、`PUT /{id}/password`、`PUT /{id}/status`、`DELETE /{id}`

---

## 五、核心设计要点（答辩可讲）

1. **账单明细价格快照**：开单时服务端按现价读取商品/服务并写入 `bill_item.item_name`、`unit_price`，前端不传价。后期改价不影响历史账单，保证财务可追溯。
2. **收款单事务**：`@Transactional` 内一次完成「条件 UPDATE 原子扣库存 → 写实收金额与支付方式 → 累加客户积分 → 自动升级会员等级」。扣库存用 `UPDATE product SET stock=stock-#{q} WHERE id=#{id} AND status=1 AND stock>=#{q}`，靠返回行数判断是否足够，避免并发超卖。
3. **财务记录不可删**：账单只能取消不能删除（且仅 UNPAID 可取消）；商品/客户被引用时拒绝删除或引导下架，保证数据完整性。
4. **两级角色而非五表 RBAC**：仅 ADMIN/STAFF 两级，用自定义 `@RequireRole` 注解 + 拦截器实现，够用且不为冗余设计买单；员工管理接口类级注解，前端菜单同步按角色过滤。
5. **预约状态机**：`TRANSITIONS` 显式声明合法流转（BOOKED→IN_PROGRESS/CANCELLED，IN_PROGRESS→COMPLETED/CANCELLED），已完成不允许再取消，避免状态脏数据。
6. **会员等级规则双向一致**：`BillService.levelOf(long)` 与测试数据生成器的 `level_of` 使用同一阈值（普通 0 / 白银 1000 / 黄金 3000 / 白金 8000，1 元 = 1 分），保证演示数据积分与等级自洽。
7. **统一响应与全局异常**：`Result<T>` + `@RestControllerAdvice`，业务异常、参数校验异常、未知异常分三类处理，前端 axios 拦截器统一解包与提示，业务代码里不写 try-catch 噪音。
8. **趋势图零值填充**：近 7 日营收先按日期建好零值骨架再回填，无交易的日期不会出现折线断点。
9. **避免 N+1 查询**：列表页用 `nameMap` 批量映射客户/宠物/操作员姓名，不做逐条查询。
10. **单端口部署**：前端构建产物进 `static`，`baseURL` 用相对路径 `/api`，无跨域、无需 Nginx，双击 jar 级体验。

---

## 六、系统存在的不足与改进方向

> 本节如实列出当前实现的短板，供答辩「系统不足与展望」环节使用，也是后续迭代的待办清单。

### 6.1 安全与鉴权（优先级最高）

| 问题 | 现状 | 改进方向 |
| --- | --- | --- |
| Token 无法主动失效 | JWT 签发后 24h 内始终有效，改密码、停用账号后旧 Token 仍可访问 | 引入 Redis 白名单/黑名单，或改用「短期 AccessToken + 可刷新 RefreshToken」 |
| 密钥硬编码 | `jwt.secret` 写在 `application.yml` 中 | 改走环境变量或配置中心（Nacos / Spring Cloud Config） |
| 无防爆破 | 登录接口无失败次数限制、无验证码 | 增加登录失败计数锁定 + 图形验证码 |
| 密码策略弱 | 默认口令 `123456`，仅校验 6-20 位，无复杂度要求 | 强制首登改密 + 复杂度校验 |
| 无操作审计 | 谁在什么时候改了哪条数据，无从追溯 | 增加操作日志表 + 注解式埋点 |
| CORS 过宽 | `allowedOriginPatterns("*")` | 单端口部署下不触发，但应收敛为白名单 |

### 6.2 并发与数据一致性

| 问题 | 现状 | 改进方向 |
| --- | --- | --- |
| **收款未做状态抢占** | `pay()` 先 `SELECT` 判断 `status='UNPAID'` 再 `UPDATE`，并发下两个请求可能同时读到 UNPAID，导致重复扣库存、重复积分 | 改为 `UPDATE bill SET status='PAID' WHERE id=? AND status='UNPAID'`，以返回行数做 CAS 抢占；或对账单加分布式锁 |
| 库存无下限保护 | 库存为 0 时仅报错，无预警 | 增加库存预警阈值与补货提醒 |
| 无采购入库流程 | 库存只能手工改 | 增加供应商、采购单、入库单 |

### 6.3 业务功能缺口

- **无退款/退单**：账单一旦 PAID 不可撤销，实际门店必然遇到退货退款。
- **无会员储值**：支付方式只有现金/微信/支付宝/银行卡，缺少储值卡余额、充值、扣减。
- **无优惠体系**：只有整单手工填优惠金额，没有优惠券、会员折扣、节假日活动价。
- **无宠物健康档案**：疫苗、驱虫、病历、体检记录缺失——这是宠物店高频需求。
- **无图片上传**：商品图、宠物照片均无（需接对象存储或本地文件服务）。
- **预约无冲突校验**：同一时段同一员工可重复预约；也没有预约提醒（短信/微信订阅消息）。
- **员工价值未闭环**：`appointment.staff_id` 已存在但未用于排班、业绩提成统计。
- **单店模型**：无多门店/分店维度，连锁场景不可用。
- **无打印与导出**：无小票打印、无 Excel/PDF 报表导出。

### 6.4 技术与工程质量

- **数据库脚本危险**：`schema.sql` 是 `DROP + CREATE` 的幂等重建，重复执行会清空数据，**不能用于生产**；应改用 Flyway/Liquibase 做版本化迁移。
- **看板实时聚合**：统计走实时 SQL，数据量大后必然变慢，未做缓存或预聚合宽表。
- **首屏包体偏大**：`Dashboard` 分片 1.04 MB（ECharts 全量引入），未做按需引入与 CDN 化。
- **零测试代码**：引入了 `spring-boot-starter-test` 但未编写任何单元/集成测试。
- **无接口文档**：未引入 Swagger/OpenAPI，联调靠人工对接口清单。
- **无监控运维**：未引入 Actuator；日志仅输出控制台，无文件归档、无链路追踪 ID。
- **无容器化**：没有 Dockerfile 与部署脚本。
- **前后端强耦合**：单端口部署方便，但改一个前端文案也要重新打后端 jar；生产环境应前后端分离部署。

### 6.5 答辩时可能被追问的点

1. 没做压力测试，缺少并发量、响应时间等量化性能数据。
2. 权限模型只到角色级（ADMIN/STAFF），无法按菜单/按钮精细授权。
3. 未做安全测试（越权、注入、XSS）。条件构造器基本免疫 SQL 注入，但富文本类字段未做 XSS 过滤。
4. 测试数据由脚本生成，未经真实业务验证。

---

## 七、目录结构

```
petstore-admin/
├── backend/                                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/petstore/
│       ├── PetStoreApplication.java
│       ├── auth/        # JwtUtil / UserContext / RequireRole / AuthInterceptor
│       ├── common/      # Result / PageResult / BusinessException / GlobalExceptionHandler
│       ├── config/      # WebConfig / MybatisPlusConfig / MyMetaObjectHandler / JacksonConfig / DataInitializer
│       ├── controller/  # 9 个控制器
│       ├── service/     # 9 个业务服务
│       ├── mapper/      # 9 个 Mapper + DashboardMapper
│       ├── entity/      # 9 个实体
│       ├── dto/ vo/     # 入参与视图对象
│       └── resources/
│           ├── application.yml
│           └── static/  # 前端构建产物（单端口部署）
├── frontend/                                 # Vue 3 前端
│   ├── src/
│   │   ├── api/         # request.js（拦截器）+ index.js（接口集合）
│   │   ├── layout/      # Layout.vue 侧边栏 + 顶栏
│   │   ├── router/      # 路由与权限守卫
│   │   ├── stores/      # Pinia 用户态
│   │   ├── utils/dict.js# 字典与金额格式化
│   │   └── views/       # Login / Dashboard / Customer / Pet / Product / ServiceView / Appointment / Bill / User
│   └── dist/            # 构建产物
├── sql/
│   ├── schema.sql       # 9 张表 DDL（幂等）
│   └── data.sql         # 测试数据
├── tools/
│   └── gen_data.py      # 测试数据生成器（固定 seed=42，可重复生成）
└── README.md
```

代码规模：后端 64 个 Java 文件约 2777 行，前端 18 个源文件约 2204 行。

---

## 八、测试数据重建（可选）

```bash
cd tools
python gen_data.py          # 生成 ../sql/data.sql（固定随机种子，结果可复现）
mysql -uroot -proot petstore < ../sql/schema.sql
mysql -uroot -proot petstore < ../sql/data.sql
```

数据一致性校验（积分 = 该客户已支付账单实收之和）：

```sql
SELECT COUNT(*) AS inconsistent FROM customer c
LEFT JOIN (SELECT customer_id, SUM(pay_amount) s FROM bill WHERE status='PAID' GROUP BY customer_id) b
  ON c.id = b.customer_id
WHERE c.points <> FLOOR(IFNULL(b.s, 0));   -- 期望 0
```

---

## 九、常见问题

| 现象 | 处理 |
| --- | --- |
| 端口被占用 | `java -jar app.jar --server.port=18080`，或停掉占用 8080 的进程 |
| 启动报数据库连接失败 | 检查 MySQL 是否启动、`application.yml` 中库名/账号/密码 |
| 页面空白 / 404 | 确认 `backend/src/main/resources/static` 下有 `index.html` 与 `assets/`，并已重新打包 |
| 接口 401 | Token 过期（默认 24h），重新登录 |
| 中文乱码 | 建库与连接均使用 `utf8mb4`，导入时确认客户端字符集 |
