# -*- coding: utf-8 -*-
"""
宠物店后台管理系统 - 测试数据生成器
生成 sql/data.sql，特点：
1. 固定随机种子，结果可复现
2. 客户积分 = 该客户所有已支付账单实收金额取整之和，等级按积分规则计算（与后端业务逻辑严格一致）
3. 150 笔账单铺满近 60 天，看板图表开箱即有真实感
运行：python gen_data.py（在 tools 目录下执行，输出到 ../sql/data.sql）
"""
import random
from datetime import datetime, timedelta

random.seed(42)
NOW = datetime(2026, 9, 25, 12, 0, 0)


def level_of(points):
    """会员等级规则（与后端 BillService.levelOf 保持一致）"""
    if points >= 8000:
        return "白金"
    if points >= 3000:
        return "黄金"
    if points >= 1000:
        return "白银"
    return "普通"


def esc(s):
    return s.replace("\\", "\\\\").replace("'", "\\'")


def dt(v):
    return v.strftime("%Y-%m-%d %H:%M:%S")


def sql(v):
    """统一 SQL 值格式化：None→NULL，str→加引号转义，数字→原样"""
    if v is None:
        return "NULL"
    if isinstance(v, datetime):
        return "'" + dt(v) + "'"
    if isinstance(v, str):
        return "'" + esc(v) + "'"
    return str(v)


# ============ 基础数据 ============
CATEGORIES = ["主粮", "零食", "玩具", "清洁护理", "生活用品", "医疗保健"]

# (名称, 规格, 单位, 售价, 成本, 分类, 初始库存)
PRODUCTS = [
    ("皇家 金渐层猫粮", "2kg", "袋", 189.00, 142.00, "主粮", 60),
    ("皇家 幼猫奶糕粮", "400g", "袋", 68.00, 49.00, "主粮", 45),
    ("渴望 鸡肉味成猫粮", "1.5kg", "袋", 265.00, 205.00, "主粮", 38),
    ("爱肯拿 鸭肉梨狗粮", "2kg", "袋", 238.00, 186.00, "主粮", 30),
    ("麦富迪 冻干双拼粮", "1.8kg", "袋", 129.00, 92.00, "主粮", 52),
    ("伯纳天纯 小型犬粮", "2kg", "袋", 118.00, 85.00, "主粮", 40),
    ("卫仕 猫条零食", "12支", "盒", 25.00, 14.00, "零食", 120),
    ("麦富迪 鸡肉绕", "100g", "袋", 15.00, 8.50, "零食", 100),
    ("冻干鸡肉粒", "80g", "袋", 22.00, 12.00, "零食", 90),
    ("洁齿骨", "6支", "盒", 32.00, 18.00, "零食", 70),
    ("营养膏", "120g", "支", 45.00, 28.00, "零食", 55),
    ("漏食球", "中号", "个", 28.00, 15.00, "玩具", 40),
    ("猫抓板", "波浪形", "个", 35.00, 19.00, "玩具", 36),
    ("逗猫棒", "羽毛款", "个", 12.00, 6.00, "玩具", 80),
    ("橡胶咬咬球", "小号", "个", 18.00, 9.00, "玩具", 50),
    ("激光笔玩具", "USB充电", "个", 26.00, 13.00, "玩具", 30),
    ("宠物香波", "500ml", "瓶", 48.00, 27.00, "清洁护理", 42),
    ("宠物免洗泡沫", "200ml", "瓶", 39.00, 22.00, "清洁护理", 35),
    ("猫砂（豆腐砂）", "2.5kg", "袋", 32.00, 20.00, "清洁护理", 150),
    ("宠物湿巾", "80抽", "包", 16.00, 8.00, "清洁护理", 88),
    ("体外驱虫喷剂", "100ml", "瓶", 78.00, 52.00, "医疗保健", 26),
    ("体内驱虫片", "2片", "盒", 58.00, 36.00, "医疗保健", 30),
    ("复合维生素片", "60片", "瓶", 68.00, 42.00, "医疗保健", 28),
    ("宠物益生菌", "5袋", "盒", 55.00, 33.00, "医疗保健", 32),
    ("航空箱", "中号", "个", 158.00, 105.00, "生活用品", 18),
    ("猫窝（半封闭）", "灰", "个", 89.00, 56.00, "生活用品", 22),
    ("狗窝垫", "L码", "个", 75.00, 45.00, "生活用品", 20),
    ("牵引绳", "2m可调", "条", 42.00, 24.00, "生活用品", 46),
    ("自动喂食器", "4L", "台", 199.00, 135.00, "生活用品", 12),
    ("陶瓷猫碗", "双层", "个", 36.00, 20.00, "生活用品", 38),
    ("宠物饮水机滤芯", "3只", "包", 29.00, 15.00, "生活用品", 60),
    ("伊丽莎白圈", "M码", "个", 20.00, 9.00, "生活用品", 44),
]

# (名称, 价格, 时长分钟, 说明)
SERVICES = [
    ("基础洗护（小型犬）", 88, 60, "含洗澡、吹干、基础梳理、剪指甲、清耳朵"),
    ("精致美容（小型犬）", 168, 90, "洗护基础上含造型修剪"),
    ("基础洗护（中大型犬）", 128, 75, "含洗澡、吹干、基础梳理、剪指甲、清耳朵"),
    ("精致美容（中大型犬）", 228, 120, "洗护基础上含造型修剪"),
    ("猫咪洗护", 98, 60, "猫专用温和香波，含基础梳理"),
    ("药浴护理", 148, 60, "针对皮肤问题的药浴，需遵医嘱"),
    ("SPA 护理", 198, 90, "香薰洗护+按摩，深层滋养毛发"),
    ("宠物寄养（单日）", 68, 0, "含喂食、遛放、清洁，按天计费"),
    ("驱虫服务", 99, 15, "体内外驱虫（药品另计）"),
    ("健康体检", 158, 30, "基础体检：体重、体温、耳道、口腔、皮肤检查"),
]

# (姓名, 手机号, 性别, 生日)
CUSTOMERS = [
    ("王雅婷", "13728641539", 2, "1992-05-18"), ("李国豪", "13615584962", 1, "1988-11-03"),
    ("张倩雯", "15822763419", 2, "1995-08-21"), ("陈志远", "13927451860", 1, "1986-02-14"),
    ("刘雨萌", "13519674082", 2, "1997-03-26"), ("杨浩宇", "18817263549", 1, "1990-07-09"),
    ("赵思佳", "13653908427", 2, "1993-12-05"), ("黄俊贤", "15840627193", 1, "1989-09-17"),
    ("周诗涵", "13784092615", 2, "1996-04-28"), ("吴凯文", "13968541720", 1, "1991-06-30"),
    ("徐静怡", "15890374621", 2, "1994-10-11"), ("孙宇轩", "13624187590", 1, "1987-01-23"),
    ("马丽娟", "13576219804", 2, "1992-07-15"), ("朱嘉豪", "15811762093", 1, "1998-05-06"),
    ("胡欣然", "13732098456", 2, "1995-09-22"), ("郭子昂", "13948576210", 1, "1990-03-08"),
    ("林佳慧", "15862794013", 2, "1993-11-27"), ("何俊杰", "13679842501", 1, "1988-08-19"),
    ("高梦洁", "13541986270", 2, "1996-02-03"), ("罗天翔", "15803274196", 1, "1991-12-12"),
    ("郑晓芸", "13765428910", 2, "1994-06-25"), ("梁家辉", "13987150428", 1, "1989-04-16"),
]

# (主人序号, 宠物名, 品种类型, 品种, 性别, 出生日期, 体重, 绝育)
PETS = [
    (0, "豆豆", "狗", "比熊", 1, "2023-03-15", 5.2, 1),
    (0, "咪咪", "猫", "布偶", 2, "2022-08-20", 4.1, 1),
    (1, "旺财", "狗", "金毛", 1, "2021-11-02", 28.5, 1),
    (2, "雪球", "猫", "英国短毛猫", 2, "2023-01-18", 3.8, 1),
    (3, "大黄", "狗", "拉布拉多", 1, "2020-06-10", 30.2, 0),
    (4, "妞妞", "狗", "柯基", 2, "2023-07-25", 11.3, 1),
    (5, "皮皮", "猫", "橘猫", 1, "2022-04-30", 5.6, 0),
    (6, "露露", "狗", "泰迪", 2, "2023-10-05", 4.5, 1),
    (7, "小黑", "猫", "美国短毛猫", 1, "2023-02-14", 4.3, 1),
    (8, "果果", "狗", "博美", 2, "2022-12-08", 3.2, 1),
    (9, "奥利奥", "猫", "暹罗", 1, "2021-09-17", 4.8, 1),
    (10, "多多", "狗", "萨摩耶", 2, "2020-08-23", 22.0, 1),
    (11, "球球", "猫", "缅因", 1, "2022-06-12", 7.5, 0),
    (12, "贝贝", "狗", "柴犬", 1, "2023-05-20", 9.8, 1),
    (13, "可乐", "猫", "英国短毛猫", 2, "2023-09-01", 3.5, 1),
    (14, "毛毛", "仓鼠", "金丝熊", 2, "2024-01-10", 0.2, 0),
    (15, "闪电", "狗", "边境牧羊犬", 1, "2021-03-28", 17.6, 1),
    (16, "妹妹", "猫", "三花猫", 2, "2022-10-15", 3.9, 1),
    (17, "闹闹", "狗", "阿拉斯加", 1, "2020-12-05", 38.0, 0),
    (18, "安安", "猫", "俄罗斯蓝猫", 2, "2023-04-22", 4.0, 1),
    (19, "糖糖", "狗", "马尔济斯", 2, "2023-08-30", 2.8, 1),
    (20, "花卷", "猫", "金渐层", 1, "2022-02-19", 5.4, 0),
    (21, "汉堡", "狗", "柯基", 1, "2023-11-11", 10.5, 0),
    (4, "布丁", "猫", "布偶", 2, "2023-12-25", 3.6, 1),
    (9, "年糕", "兔", "垂耳兔", 2, "2023-06-08", 1.9, 0),
    (2, "七喜", "鸟", "玄凤鹦鹉", 1, "2023-03-03", 0.1, 0),
    (7, "汤圆", "狗", "比熊", 2, "2022-09-09", 5.0, 1),
    (16, "翠花", "猫", "狸花猫", 2, "2021-07-21", 4.6, 1),
    (1, "小七", "狗", "边境牧羊犬", 2, "2023-01-30", 16.8, 0),
    (13, "柚子", "猫", "布偶", 2, "2024-02-14", 3.2, 1),
]

PAY_METHODS = ["WECHAT", "WECHAT", "WECHAT", "CASH", "CASH", "ALIPAY", "CARD"]
REMARKS = [None, None, None, "客户要求尽快安排", "老客户", "会员推荐", "团购活动单"]

pets_by_customer = {}
for pi, p in enumerate(PETS):
    pets_by_customer.setdefault(p[0], []).append(pi)

# ============ 账单：150 笔铺满近 60 天 ============
bills = []   # dict 结构，一次生成到位
for i in range(150):
    # 近两周权重高，看板趋势更自然
    days_ago = random.choices(range(60), weights=[1.6 if k < 14 else 1.0 for k in range(60)])[0]
    create = NOW - timedelta(days=days_ago)
    create = create.replace(hour=random.randint(9, 19),
                            minute=random.randint(0, 59),
                            second=random.randint(0, 59))
    if create > NOW:
        create = NOW - timedelta(minutes=random.randint(10, 600))

    cust_idx = random.randrange(len(CUSTOMERS))
    kind = random.choices(["product", "service", "mixed"], weights=[55, 30, 15])[0]

    items = []
    if kind in ("product", "mixed"):
        for _ in range(random.randint(1, 3)):
            p = random.choice(PRODUCTS)
            qty = random.choices([1, 2, 3], weights=[6, 3, 1])[0]
            items.append(("PRODUCT", PRODUCTS.index(p) + 1, p[0], p[3], qty, round(p[3] * qty, 2)))
    if kind in ("service", "mixed"):
        for _ in range(random.randint(1, 2)):
            s = random.choice(SERVICES)
            items.append(("SERVICE", SERVICES.index(s) + 1, s[0], s[1], 1, s[1]))

    total = round(sum(it[5] for it in items), 2)
    discount = round(random.choice([0, 0, 0, 0, 1, 2, 5]), 2)
    discount = min(discount, total)

    r = random.random()
    status = "PAID" if r < 0.82 else ("UNPAID" if r < 0.94 else "CANCELLED")
    pay_time = pay_amount = method = None
    if status == "PAID":
        pay_amount = round(total - discount, 2)
        method = random.choice(PAY_METHODS)
        pay_time = create + timedelta(minutes=random.randint(2, 35))

    has_p = any(it[0] == "PRODUCT" for it in items)
    has_s = any(it[0] == "SERVICE" for it in items)
    btype = "MIXED" if (has_p and has_s) else ("PRODUCT" if has_p else "SERVICE")
    pet_id = None
    if has_s:
        candidates = pets_by_customer.get(cust_idx)
        if candidates:
            pet_id = random.choice(candidates) + 1

    bills.append({
        "bill_no": "B" + create.strftime("%Y%m%d%H%M%S") + f"{i:03d}",
        "customer_id": cust_idx + 1,
        "pet_id": pet_id,
        "type": btype,
        "total": total,
        "discount": discount,
        "pay_amount": pay_amount,
        "method": method,
        "status": status,
        "operator": random.choices([1, 2], weights=[7, 3])[0],  # 1=admin 2=staff（与启动初始化ID一致）
        "remark": random.choice(REMARKS),
        "pay_time": pay_time,
        "create": create,
        "items": items,
    })

# 客户积分 = 已支付账单实收取整之和，等级按规则推导（与后端逻辑严格一致）
cust_points = {i + 1: 0 for i in range(len(CUSTOMERS))}
for b in bills:
    if b["status"] == "PAID":
        cust_points[b["customer_id"]] += int(b["pay_amount"])

# ============ 预约：近10天历史（18条）+ 未来7天（12条） ============
appointments = []
for _ in range(18):
    days_ago = random.randint(0, 10)
    at = NOW - timedelta(days=days_ago)
    at = at.replace(hour=random.randint(10, 18), minute=random.choice([0, 15, 30, 45]))
    cust_idx = random.randrange(len(CUSTOMERS))
    candidates = pets_by_customer.get(cust_idx)
    if not candidates:
        continue
    status = "COMPLETED" if random.random() < 0.8 else "CANCELLED"
    appointments.append((cust_idx + 1, random.choice(candidates) + 1,
                        random.randrange(len(SERVICES)) + 1, random.choice([1, 2]),
                        at, status, "首次到店服务" if random.random() < 0.3 else None,
                        NOW - timedelta(days=days_ago + random.randint(1, 5))))
for _ in range(12):
    days_ahead = random.randint(0, 7)
    at = NOW + timedelta(days=days_ahead)
    at = at.replace(hour=random.randint(9, 18), minute=random.choice([0, 15, 30, 45]))
    cust_idx = random.randrange(len(CUSTOMERS))
    candidates = pets_by_customer.get(cust_idx)
    if not candidates:
        continue
    status = "IN_PROGRESS" if days_ahead == 0 and random.random() < 0.5 else "BOOKED"
    appointments.append((cust_idx + 1, random.choice(candidates) + 1,
                        random.randrange(len(SERVICES)) + 1, random.choice([1, 2]),
                        at, status, None, NOW - timedelta(days=random.randint(0, 3))))

# ============ 输出 SQL ============
out = []
out.append("-- ============================================================")
out.append("-- 宠物店后台管理系统 测试数据（由 tools/gen_data.py 生成，勿手改）")
out.append("-- 执行前请先跑 schema.sql；sys_user 由后端首次启动自动初始化")
out.append("-- ============================================================")
out.append("SET NAMES utf8mb4;")
out.append("")

out.append("-- 商品分类")
out.append("INSERT INTO product_category (id, name, sort, create_time, update_time) VALUES")
out.append(",\n".join(
    f"({i + 1}, {sql(c)}, {i}, {sql(NOW - timedelta(days=200))}, {sql(NOW - timedelta(days=200))})"
    for i, c in enumerate(CATEGORIES)) + ";")
out.append("")

out.append("-- 商品")
out.append("INSERT INTO product (id, category_id, name, spec, unit, price, cost, stock, status, notes, create_time, update_time) VALUES")
rows = []
for i, (name, spec, unit, price, cost, cat, stock) in enumerate(PRODUCTS):
    on = 0 if random.random() < 0.06 else 1
    rows.append(f"({i + 1}, {CATEGORIES.index(cat) + 1}, {sql(name)}, {sql(spec)}, {sql(unit)}, "
                f"{price}, {cost}, {stock}, {on}, NULL, "
                f"{sql(NOW - timedelta(days=random.randint(150, 220)))}, {sql(NOW - timedelta(days=random.randint(0, 30)))})")
out.append(",\n".join(rows) + ";")
out.append("")

out.append("-- 服务项目")
out.append("INSERT INTO service_item (id, name, price, duration_min, description, status, create_time, update_time) VALUES")
out.append(",\n".join(
    f"({i + 1}, {sql(n)}, {p}, {du}, {sql(desc)}, 1, "
    f"{sql(NOW - timedelta(days=180))}, {sql(NOW - timedelta(days=10))})"
    for i, (n, p, du, desc) in enumerate(SERVICES)) + ";")
out.append("")

out.append("-- 客户（等级/积分由已支付账单推算）")
out.append("INSERT INTO customer (id, name, phone, gender, birthday, level, points, notes, create_time, update_time) VALUES")
rows = []
for i, (name, phone, gender, birthday) in enumerate(CUSTOMERS):
    pts = cust_points[i + 1]
    # 前2位客户注册在本月内，保证看板"本月新增客户"有数据
    if i < 2:
        create = NOW - timedelta(days=[5, 12][i], hours=random.randint(1, 8))
    else:
        create = NOW - timedelta(days=random.randint(30, 170))
    rows.append(f"({i + 1}, {sql(name)}, {sql(phone)}, {gender}, {sql(birthday)}, "
                f"{sql(level_of(pts))}, {pts}, NULL, {sql(create)}, {sql(create)})")
out.append(",\n".join(rows) + ";")
out.append("")

out.append("-- 宠物")
out.append("INSERT INTO pet (id, customer_id, name, species, breed, gender, birth_date, weight, sterilized, notes, create_time, update_time) VALUES")
rows = []
for i, (owner, name, species, breed, gender, birth, weight, ster) in enumerate(PETS):
    create = NOW - timedelta(days=random.randint(20, 160))
    rows.append(f"({i + 1}, {owner + 1}, {sql(name)}, {sql(species)}, {sql(breed)}, {gender}, "
                f"{sql(birth)}, {weight}, {ster}, NULL, {sql(create)}, {sql(create)})")
out.append(",\n".join(rows) + ";")
out.append("")

out.append("-- 账单")
out.append("INSERT INTO bill (id, bill_no, customer_id, pet_id, type, total_amount, discount_amount, "
           "pay_amount, pay_method, status, operator_id, remark, pay_time, create_time, update_time) VALUES")
rows = []
for i, b in enumerate(bills):
    rows.append(f"({i + 1}, {sql(b['bill_no'])}, {b['customer_id']}, {sql(b['pet_id'])}, {sql(b['type'])}, "
                f"{b['total']}, {b['discount']}, {sql(b['pay_amount'])}, {sql(b['method'])}, {sql(b['status'])}, "
                f"{b['operator']}, {sql(b['remark'])}, {sql(b['pay_time'])}, {sql(b['create'])}, {sql(b['create'])})")
out.append(",\n".join(rows) + ";")
out.append("")

out.append("-- 账单明细（名称/单价为开单时快照）")
out.append("INSERT INTO bill_item (bill_id, item_type, ref_id, item_name, unit_price, quantity, amount) VALUES")
rows = []
for i, b in enumerate(bills):
    for it in b["items"]:
        rows.append(f"({i + 1}, '{it[0]}', {it[1]}, {sql(it[2])}, {it[3]}, {it[4]}, {it[5]})")
out.append(",\n".join(rows) + ";")
out.append("")

out.append("-- 服务预约")
out.append("INSERT INTO appointment (customer_id, pet_id, service_id, staff_id, appointment_time, status, remark, create_time, update_time) VALUES")
out.append(",\n".join(
    f"({c}, {p}, {s}, {st}, {sql(at)}, {sql(status)}, {sql(rk)}, {sql(cr)}, {sql(cr)})"
    for c, p, s, st, at, status, rk, cr in appointments) + ";")

with open("../sql/data.sql", "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(out) + "\n")

paid = sum(1 for b in bills if b["status"] == "PAID")
print(f"生成完成：客户 {len(CUSTOMERS)}、宠物 {len(PETS)}、商品 {len(PRODUCTS)}、服务 {len(SERVICES)}、"
      f"账单 {len(bills)}（已支付 {paid}、待支付 {sum(1 for b in bills if b['status']=='UNPAID')}、"
      f"已取消 {sum(1 for b in bills if b['status']=='CANCELLED')}）、预约 {len(appointments)}")
