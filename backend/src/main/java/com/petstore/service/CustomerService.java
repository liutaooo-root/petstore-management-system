package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.entity.Appointment;
import com.petstore.entity.Bill;
import com.petstore.entity.Customer;
import com.petstore.entity.Pet;
import com.petstore.mapper.AppointmentMapper;
import com.petstore.mapper.BillMapper;
import com.petstore.mapper.CustomerMapper;
import com.petstore.mapper.PetMapper;
import com.petstore.vo.CustomerDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/** 客户（会员）管理 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final PetMapper petMapper;
    private final BillMapper billMapper;
    private final AppointmentMapper appointmentMapper;
    private final BillService billService;

    public PageResult<Customer> page(long current, long size, String keyword, String level) {
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<>();
        w.and(StringUtils.hasText(keyword),
                q -> q.like(Customer::getName, keyword).or().like(Customer::getPhone, keyword));
        w.eq(StringUtils.hasText(level), Customer::getLevel, level);
        w.orderByDesc(Customer::getId);
        Page<Customer> p = customerMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p, c -> c);
    }

    /** 下拉选择用 */
    public List<Customer> list() {
        return customerMapper.selectList(new LambdaQueryWrapper<Customer>().orderByDesc(Customer::getId));
    }

    public Customer detail(Long id) {
        return requireExists(id);
    }

    public CustomerDetailVO detailWithRelations(Long id) {
        Customer customer = requireExists(id);
        CustomerDetailVO vo = new CustomerDetailVO();
        vo.setCustomer(customer);
        vo.setPets(petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getCustomerId, id).orderByDesc(Pet::getId)));
        List<Bill> bills = billMapper.selectPage(new Page<>(1, 5),
                        new LambdaQueryWrapper<Bill>().eq(Bill::getCustomerId, id).orderByDesc(Bill::getId))
                .getRecords();
        vo.setRecentBills(billService.assemble(bills));
        return vo;
    }

    public void create(Customer customer) {
        checkPhoneUnique(customer.getPhone(), null);
        customer.setId(null);
        if (!StringUtils.hasText(customer.getLevel())) {
            customer.setLevel("普通");
        }
        if (customer.getPoints() == null) {
            customer.setPoints(0);
        }
        customerMapper.insert(customer);
    }

    public void update(Customer customer) {
        requireExists(customer.getId());
        checkPhoneUnique(customer.getPhone(), customer.getId());
        customerMapper.updateById(customer);
    }

    /** 删除前校验：客户有宠物/账单/预约任一存在则拒绝 */
    public void delete(Long id) {
        requireExists(id);
        if (petMapper.selectCount(new LambdaQueryWrapper<Pet>().eq(Pet::getCustomerId, id)) > 0) {
            throw BusinessException.error("该客户名下存在宠物，无法删除");
        }
        if (billMapper.selectCount(new LambdaQueryWrapper<Bill>().eq(Bill::getCustomerId, id)) > 0) {
            throw BusinessException.error("该客户存在账单记录，无法删除");
        }
        if (appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>().eq(Appointment::getCustomerId, id)) > 0) {
            throw BusinessException.error("该客户存在预约记录，无法删除");
        }
        customerMapper.deleteById(id);
    }

    private Customer requireExists(Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw BusinessException.error("客户不存在");
        }
        return customer;
    }

    private void checkPhoneUnique(String phone, Long excludeId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        LambdaQueryWrapper<Customer> w = new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, phone);
        if (excludeId != null) {
            w.ne(Customer::getId, excludeId);
        }
        if (customerMapper.selectCount(w) > 0) {
            throw BusinessException.error("该手机号已存在客户记录");
        }
    }
}
