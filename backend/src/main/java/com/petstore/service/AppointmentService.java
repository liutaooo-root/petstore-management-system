package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.entity.*;
import com.petstore.mapper.AppointmentMapper;
import com.petstore.mapper.CustomerMapper;
import com.petstore.mapper.PetMapper;
import com.petstore.mapper.ServiceItemMapper;
import com.petstore.mapper.SysUserMapper;
import com.petstore.vo.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 服务预约管理：状态流转 BOOKED → IN_PROGRESS → COMPLETED（或 → CANCELLED） */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    /** 合法状态流转表 */
    private static final Map<String, List<String>> TRANSITIONS = Map.of(
            "BOOKED", List.of("IN_PROGRESS", "CANCELLED"),
            "IN_PROGRESS", List.of("COMPLETED", "CANCELLED"));

    private final AppointmentMapper appointmentMapper;
    private final CustomerMapper customerMapper;
    private final PetMapper petMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final SysUserMapper userMapper;

    public PageResult<AppointmentVO> page(long current, long size, String status,
                                          String date, Long customerId) {
        LambdaQueryWrapper<Appointment> w = new LambdaQueryWrapper<>();
        w.eq(StringUtils.hasText(status), Appointment::getStatus, status);
        w.eq(customerId != null, Appointment::getCustomerId, customerId);
        if (StringUtils.hasText(date)) {
            w.apply("DATE(appointment_time) = {0}", date);
        }
        w.orderByDesc(Appointment::getAppointmentTime);
        Page<Appointment> p = appointmentMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(assemble(p.getRecords()), p);
    }

    public void create(Appointment appointment) {
        requireCustomer(appointment.getCustomerId());
        Pet pet = petMapper.selectById(appointment.getPetId());
        if (pet == null || !pet.getCustomerId().equals(appointment.getCustomerId())) {
            throw BusinessException.error("所选宠物不属于该客户");
        }
        if (serviceItemMapper.selectById(appointment.getServiceId()) == null) {
            throw BusinessException.error("所选服务不存在");
        }
        if (appointment.getAppointmentTime() == null
                || appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw BusinessException.error("预约时间不能早于当前时间");
        }
        appointment.setId(null);
        appointment.setStatus("BOOKED");
        appointmentMapper.insert(appointment);
    }

    /** 修改：仅待到店状态可改 */
    public void update(Appointment appointment) {
        Appointment old = requireExists(appointment.getId());
        if (!"BOOKED".equals(old.getStatus())) {
            throw BusinessException.error("仅待到店预约可修改");
        }
        requireCustomer(appointment.getCustomerId());
        Pet pet = petMapper.selectById(appointment.getPetId());
        if (pet == null || !pet.getCustomerId().equals(appointment.getCustomerId())) {
            throw BusinessException.error("所选宠物不属于该客户");
        }
        if (serviceItemMapper.selectById(appointment.getServiceId()) == null) {
            throw BusinessException.error("所选服务不存在");
        }
        appointment.setStatus(null); // 状态不走编辑接口
        appointmentMapper.updateById(appointment);
    }

    public void changeStatus(Long id, String target) {
        Appointment appointment = requireExists(id);
        List<String> allowed = TRANSITIONS.get(appointment.getStatus());
        if (allowed == null || !allowed.contains(target)) {
            throw BusinessException.error("当前状态不允许变更为「" + target + "」");
        }
        appointment.setStatus(target);
        appointmentMapper.updateById(appointment);
    }

    public void delete(Long id) {
        Appointment appointment = requireExists(id);
        if ("IN_PROGRESS".equals(appointment.getStatus())) {
            throw BusinessException.error("进行中的预约无法删除");
        }
        appointmentMapper.deleteById(id);
    }

    public List<AppointmentVO> assemble(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Long, String> customerNames = map(appointments, Appointment::getCustomerId,
                customerMapper::selectBatchIds, Customer::getId, Customer::getName);
        Map<Long, String> petNames = map(appointments, Appointment::getPetId,
                petMapper::selectBatchIds, Pet::getId, Pet::getName);
        Map<Long, String> serviceNames = map(appointments, Appointment::getServiceId,
                serviceItemMapper::selectBatchIds, ServiceItem::getId, ServiceItem::getName);
        Map<Long, String> staffNames = map(appointments, Appointment::getStaffId,
                userMapper::selectBatchIds, SysUser::getId, SysUser::getRealName);

        return appointments.stream().map(a -> {
            AppointmentVO vo = new AppointmentVO();
            BeanUtils.copyProperties(a, vo);
            vo.setCustomerName(customerNames.get(a.getCustomerId()));
            vo.setPetName(a.getPetId() == null ? null : petNames.get(a.getPetId()));
            vo.setServiceName(serviceNames.get(a.getServiceId()));
            vo.setStaffName(a.getStaffId() == null ? null : staffNames.get(a.getStaffId()));
            return vo;
        }).toList();
    }

    private <T, E> Map<Long, String> map(List<T> records, Function<T, Long> idGetter,
                                         Function<List<Long>, List<E>> query,
                                         Function<E, Long> keyGetter, Function<E, String> valueGetter) {
        List<Long> ids = records.stream().map(idGetter).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return query.apply(ids).stream().collect(Collectors.toMap(keyGetter, valueGetter));
    }

    private Appointment requireExists(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw BusinessException.error("预约不存在");
        }
        return appointment;
    }

    private void requireCustomer(Long customerId) {
        if (customerId == null || customerMapper.selectById(customerId) == null) {
            throw BusinessException.error("所选客户不存在");
        }
    }
}
