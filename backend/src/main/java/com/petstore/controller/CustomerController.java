package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.Customer;
import com.petstore.service.CustomerService;
import com.petstore.vo.CustomerDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/page")
    public Result<PageResult<Customer>> page(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String level) {
        return Result.ok(customerService.page(current, size, keyword, level));
    }

    /** 下拉选择用（开单/预约/宠物归属） */
    @GetMapping("/list")
    public Result<List<Customer>> list() {
        return Result.ok(customerService.list());
    }

    @GetMapping("/{id}")
    public Result<CustomerDetailVO> detail(@PathVariable Long id) {
        return Result.ok(customerService.detailWithRelations(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid Customer customer) {
        customerService.create(customer);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid Customer customer) {
        customerService.update(customer);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.ok();
    }
}
