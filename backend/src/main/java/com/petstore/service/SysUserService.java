package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petstore.auth.UserContext;
import com.petstore.common.BusinessException;
import com.petstore.common.PageResult;
import com.petstore.entity.SysUser;
import com.petstore.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 系统用户管理（仅管理员可操作，见控制器角色注解） */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public PageResult<SysUser> page(long current, long size, String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.and(StringUtils.hasText(keyword),
                q -> q.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        w.orderByDesc(SysUser::getId);
        Page<SysUser> p = userMapper.selectPage(new Page<>(current, size), w);
        p.getRecords().forEach(u -> u.setPassword(null)); // 永不返回密码摘要
        return PageResult.of(p, u -> u);
    }

    /** 新增账号初始密码统一为 123456 */
    public void create(SysUser user) {
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw BusinessException.error("登录名已存在");
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode("123456"));
        if (user.getRole() == null) {
            user.setRole("STAFF");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userMapper.insert(user);
    }

    public void update(SysUser user) {
        requireExists(user.getId());
        user.setPassword(null); // 密码与角色安全由专门接口维护
        userMapper.updateById(user);
    }

    /** 重置密码：不传则恢复默认 123456 */
    public void resetPassword(Long id, String newPassword) {
        SysUser user = requireExists(id);
        String target = StringUtils.hasText(newPassword) ? newPassword : "123456";
        user.setPassword(passwordEncoder.encode(target));
        userMapper.updateById(user);
    }

    public void toggleStatus(Long id) {
        SysUser user = requireExists(id);
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        if (id.equals(UserContext.currentId())) {
            throw BusinessException.error("不能删除当前登录账号");
        }
        requireExists(id);
        userMapper.deleteById(id);
    }

    private SysUser requireExists(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.error("用户不存在");
        }
        return user;
    }
}
