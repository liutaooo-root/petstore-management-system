package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.Appointment;
import com.petstore.service.AppointmentService;
import com.petstore.vo.AppointmentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/page")
    public Result<PageResult<AppointmentVO>> page(@RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String date,
                                                  @RequestParam(required = false) Long customerId) {
        return Result.ok(appointmentService.page(current, size, status, date, customerId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid Appointment appointment) {
        appointmentService.create(appointment);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid Appointment appointment) {
        appointmentService.update(appointment);
        return Result.ok();
    }

    /** 状态流转：到店(BOOKED→IN_PROGRESS) / 完成(→COMPLETED) / 取消(→CANCELLED) */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam String status) {
        appointmentService.changeStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return Result.ok();
    }
}
