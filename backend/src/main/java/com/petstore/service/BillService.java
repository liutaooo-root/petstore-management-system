package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.auth.UserContext;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.dto.BillCreateDTO;
import com.petstore.entity.*;
import com.petstore.mapper.*;
import com.petstore.vo.BillDetailVO;
import com.petstore.vo.BillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 账单核心流程：开单（现价快照）→ 收款（事务：原子扣库存 + 记积分 + 会员升级）→ 取消。
 * 财务记录不可删除，仅待支付单可取消。
 */
@Service
@RequiredArgsConstructor
public class BillService {

    /** 会员等级规则：按累计积分自动升级（消费 1 元 = 1 分） */
    public static String levelOf(long points) {
        if (points >= 8000) return "白金";
        if (points >= 3000) return "黄金";
        if (points >= 1000) return "白银";
        return "普通";
    }

    private static final Set<String> PAY_METHODS = Set.of("CASH", "WECHAT", "ALIPAY", "CARD");
    private static final DateTimeFormatter BILL_NO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final BillMapper billMapper;
    private final BillItemMapper billItemMapper;
    private final CustomerMapper customerMapper;
    private final PetMapper petMapper;
    private final ProductMapper productMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final SysUserMapper userMapper;

    /** 开单：明细价格由服务端按现价读取并快照，防止前端传价 */
    @Transactional
    public BillDetailVO create(BillCreateDTO dto) {
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw BusinessException.error("客户不存在");
        }
        if (dto.getPetId() != null) {
            Pet pet = petMapper.selectById(dto.getPetId());
            if (pet == null || !pet.getCustomerId().equals(dto.getCustomerId())) {
                throw BusinessException.error("所选宠物不属于该客户");
            }
        }

        boolean hasProduct = false;
        boolean hasService = false;
        BigDecimal total = BigDecimal.ZERO;
        List<BillItem> items = new ArrayList<>();

        for (BillCreateDTO.Item i : dto.getItems()) {
            String name;
            BigDecimal price;
            if ("PRODUCT".equals(i.getItemType())) {
                Product p = productMapper.selectById(i.getRefId());
                if (p == null || p.getStatus() == null || p.getStatus() != 1) {
                    throw BusinessException.error("商品不存在或已下架");
                }
                name = p.getName();
                price = p.getPrice();
                hasProduct = true;
            } else if ("SERVICE".equals(i.getItemType())) {
                ServiceItem s = serviceItemMapper.selectById(i.getRefId());
                if (s == null || s.getStatus() == null || s.getStatus() != 1) {
                    throw BusinessException.error("服务项目不存在或已下架");
                }
                name = s.getName();
                price = s.getPrice();
                hasService = true;
            } else {
                throw BusinessException.error("明细类型不合法");
            }
            BigDecimal amount = price.multiply(BigDecimal.valueOf(i.getQuantity()));
            BillItem item = new BillItem();
            item.setItemType(i.getItemType());
            item.setRefId(i.getRefId());
            item.setItemName(name);
            item.setUnitPrice(price);
            item.setQuantity(i.getQuantity());
            item.setAmount(amount);
            items.add(item);
            total = total.add(amount);
        }

        BigDecimal discount = dto.getDiscount() == null ? BigDecimal.ZERO : dto.getDiscount();
        if (discount.compareTo(total) > 0) {
            throw BusinessException.error("优惠金额不能超过账单总额");
        }

        Bill bill = new Bill();
        bill.setBillNo(generateBillNo());
        bill.setCustomerId(dto.getCustomerId());
        bill.setPetId(dto.getPetId());
        bill.setType(hasProduct && hasService ? "MIXED" : hasProduct ? "PRODUCT" : "SERVICE");
        bill.setTotalAmount(total);
        bill.setDiscountAmount(discount);
        bill.setStatus("UNPAID");
        bill.setOperatorId(UserContext.currentId());
        bill.setRemark(dto.getRemark());
        billMapper.insert(bill);

        for (BillItem item : items) {
            item.setBillId(bill.getId());
            billItemMapper.insert(item);
        }
        return detail(bill.getId());
    }

    /**
     * 收款：一个事务内完成——
     * 1. 状态校验（仅待支付可收款）
     * 2. 商品明细逐项原子扣库存（SQL 条件更新，库存不足整体回滚）
     * 3. 计算实收、更新账单状态与支付时间
     * 4. 客户累计积分（1 元 = 1 分）并按积分自动升级会员等级
     */
    @Transactional
    public void pay(Long id, String payMethod) {
        if (payMethod == null || !PAY_METHODS.contains(payMethod)) {
            throw BusinessException.error("支付方式不合法");
        }
        Bill bill = requireBill(id);
        if (!"UNPAID".equals(bill.getStatus())) {
            throw BusinessException.error("账单状态不允许收款");
        }
        List<BillItem> items = billItemMapper.selectList(
                new LambdaQueryWrapper<BillItem>().eq(BillItem::getBillId, id));
        for (BillItem item : items) {
            if (!"PRODUCT".equals(item.getItemType())) {
                continue;
            }
            int rows = productMapper.deductStock(item.getRefId(), item.getQuantity());
            if (rows == 0) {
                throw BusinessException.error("商品【" + item.getItemName() + "】库存不足，收款失败");
            }
        }
        BigDecimal payAmount = bill.getTotalAmount().subtract(bill.getDiscountAmount());
        bill.setPayAmount(payAmount);
        bill.setPayMethod(payMethod);
        bill.setPayTime(LocalDateTime.now());
        bill.setStatus("PAID");
        billMapper.updateById(bill);

        Customer customer = customerMapper.selectById(bill.getCustomerId());
        long points = customer.getPoints() + payAmount.setScale(0, RoundingMode.DOWN).longValue();
        customer.setPoints((int) points);
        customer.setLevel(levelOf(points));
        customerMapper.updateById(customer);
    }

    /** 取消：仅待支付账单可取消，已收款账单走线下退款不删记录 */
    public void cancel(Long id) {
        Bill bill = requireBill(id);
        if (!"UNPAID".equals(bill.getStatus())) {
            throw BusinessException.error("仅待支付账单可取消");
        }
        bill.setStatus("CANCELLED");
        billMapper.updateById(bill);
    }

    public PageResult<BillVO> page(long current, long size, String billNo, String type,
                                   String status, String beginDate, String endDate, Long customerId) {
        LambdaQueryWrapper<Bill> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(billNo), Bill::getBillNo, billNo);
        w.eq(StringUtils.hasText(type), Bill::getType, type);
        w.eq(StringUtils.hasText(status), Bill::getStatus, status);
        w.eq(customerId != null, Bill::getCustomerId, customerId);
        if (StringUtils.hasText(beginDate)) {
            w.ge(Bill::getCreateTime, LocalDate.parse(beginDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            w.le(Bill::getCreateTime, LocalDate.parse(endDate).atTime(LocalTime.MAX));
        }
        w.orderByDesc(Bill::getId);
        Page<Bill> p = billMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(assemble(p.getRecords()), p);
    }

    public BillDetailVO detail(Long id) {
        Bill bill = requireBill(id);
        BillDetailVO vo = new BillDetailVO();
        vo.setBill(assemble(List.of(bill)).get(0));
        vo.setItems(billItemMapper.selectList(
                new LambdaQueryWrapper<BillItem>().eq(BillItem::getBillId, id).orderByAsc(BillItem::getId)));
        return vo;
    }

    /** 批量组装客户/宠物/操作员姓名 */
    public List<BillVO> assemble(List<Bill> bills) {
        if (bills.isEmpty()) {
            return List.of();
        }
        Map<Long, String> customerNames = nameMap(bills, Bill::getCustomerId,
                ids -> customerMapper.selectBatchIds(ids), Customer::getId, Customer::getName);
        Map<Long, String> petNames = nameMap(bills, Bill::getPetId,
                ids -> petMapper.selectBatchIds(ids), Pet::getId, Pet::getName);
        Map<Long, String> operatorNames = nameMap(bills, Bill::getOperatorId,
                ids -> userMapper.selectBatchIds(ids), SysUser::getId, SysUser::getRealName);

        return bills.stream().map(bill -> {
            BillVO vo = new BillVO();
            BeanUtils.copyProperties(bill, vo);
            vo.setCustomerName(customerNames.get(bill.getCustomerId()));
            vo.setPetName(bill.getPetId() == null ? null : petNames.get(bill.getPetId()));
            vo.setOperatorName(bill.getOperatorId() == null ? null : operatorNames.get(bill.getOperatorId()));
            return vo;
        }).toList();
    }

    /** 通用批量 id→name 映射，避免列表页 N+1 查询 */
    private <T, E> Map<Long, String> nameMap(List<T> records, Function<T, Long> idGetter,
                                             Function<List<Long>, List<E>> query,
                                             Function<E, Long> keyGetter, Function<E, String> valueGetter) {
        List<Long> ids = records.stream().map(idGetter).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return query.apply(ids).stream().collect(Collectors.toMap(keyGetter, valueGetter));
    }

    private Bill requireBill(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw BusinessException.error("账单不存在");
        }
        return bill;
    }

    private String generateBillNo() {
        return "B" + LocalDateTime.now().format(BILL_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
