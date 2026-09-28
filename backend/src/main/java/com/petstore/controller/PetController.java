package com.petstore.controller;

import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.Pet;
import com.petstore.service.PetService;
import com.petstore.vo.PetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/page")
    public Result<PageResult<PetVO>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long customerId) {
        return Result.ok(petService.page(current, size, keyword, customerId));
    }

    /** 按客户查宠物（开单/预约级联选择用） */
    @GetMapping("/list")
    public Result<List<Pet>> list(@RequestParam Long customerId) {
        return Result.ok(petService.listByCustomer(customerId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid Pet pet) {
        petService.create(pet);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid Pet pet) {
        petService.update(pet);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.ok();
    }
}
