package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.ServiceItem;
import com.petstore.service.ServiceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-item")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @GetMapping("/page")
    public Result<PageResult<ServiceItem>> page(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
        return Result.ok(serviceItemService.page(current, size, keyword, status));
    }

    /** 开单用：在架服务 */
    @GetMapping("/list")
    public Result<List<ServiceItem>> list() {
        return Result.ok(serviceItemService.listOnShelf());
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid ServiceItem item) {
        serviceItemService.create(item);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid ServiceItem item) {
        serviceItemService.update(item);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceItemService.delete(id);
        return Result.ok();
    }
}
