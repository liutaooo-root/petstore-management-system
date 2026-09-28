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
import com.petstore.vo.PetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 宠物档案管理 */
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetMapper petMapper;
    private final CustomerMapper customerMapper;
    private final BillMapper billMapper;
    private final AppointmentMapper appointmentMapper;

    public PageResult<PetVO> page(long current, long size, String keyword, Long customerId) {
        LambdaQueryWrapper<Pet> w = new LambdaQueryWrapper<>();
        w.and(StringUtils.hasText(keyword),
                q -> q.like(Pet::getName, keyword).or().like(Pet::getBreed, keyword));
        w.eq(customerId != null, Pet::getCustomerId, customerId);
        w.orderByDesc(Pet::getId);
        Page<Pet> p = petMapper.selectPage(new Page<>(current, size), w);

        Map<Long, Customer> customers = batchCustomers(p.getRecords());
        return PageResult.of(p, pet -> {
            PetVO vo = new PetVO();
            BeanUtils.copyProperties(pet, vo);
            Customer owner = customers.get(pet.getCustomerId());
            if (owner != null) {
                vo.setCustomerName(owner.getName());
                vo.setCustomerPhone(owner.getPhone());
            }
            return vo;
        });
    }

    /** 按客户查宠物（开单/预约级联选择用） */
    public List<Pet> listByCustomer(Long customerId) {
        return petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCustomerId, customerId).orderByDesc(Pet::getId));
    }

    public void create(Pet pet) {
        requireCustomer(pet.getCustomerId());
        pet.setId(null);
        petMapper.insert(pet);
    }

    public void update(Pet pet) {
        if (petMapper.selectById(pet.getId()) == null) {
            throw BusinessException.error("宠物不存在");
        }
        requireCustomer(pet.getCustomerId());
        petMapper.updateById(pet);
    }

    public void delete(Long id) {
        if (petMapper.selectById(id) == null) {
            throw BusinessException.error("宠物不存在");
        }
        if (billMapper.selectCount(new LambdaQueryWrapper<Bill>().eq(Bill::getPetId, id)) > 0) {
            throw BusinessException.error("该宠物存在账单记录，无法删除");
        }
        if (appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>().eq(Appointment::getPetId, id)) > 0) {
            throw BusinessException.error("该宠物存在预约记录，无法删除");
        }
        petMapper.deleteById(id);
    }

    private void requireCustomer(Long customerId) {
        if (customerId == null || customerMapper.selectById(customerId) == null) {
            throw BusinessException.error("所选客户不存在");
        }
    }

    private Map<Long, Customer> batchCustomers(List<Pet> pets) {
        List<Long> ids = pets.stream().map(Pet::getCustomerId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return customerMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));
    }
}
