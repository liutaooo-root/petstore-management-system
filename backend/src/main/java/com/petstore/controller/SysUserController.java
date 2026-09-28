package com.petstore.controller;

import com.petstore.auth.RequireRole;
import com.petstore.common.PageResult;
import com.petstore.common.Result;
import com.petstore.entity.SysUser;
import com.petstore.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 系统用户管理：仅管理员可见可操作（前端菜单 + 后端注解双重控制） */
@RestController
@RequestMapping("/api/sys-user")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long current,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword) {
        return Result.ok(sysUserService.page(current, size, keyword));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid SysUser user) {
        sysUserService.create(user);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Valid SysUser user) {
        sysUserService.update(user);
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id,
                                      @RequestParam(required = false) String newPassword) {
        sysUserService.resetPassword(id, newPassword);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        sysUserService.toggleStatus(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.ok();
    }
}
