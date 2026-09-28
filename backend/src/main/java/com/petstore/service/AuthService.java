package com.petstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petstore.auth.JwtUtil;
import com.petstore.auth.UserContext;
import com.petstore.common.BusinessException;
import com.petstore.dto.LoginDTO;
import com.petstore.dto.PasswordDTO;
import com.petstore.entity.SysUser;
import com.petstore.mapper.SysUserMapper;
import com.petstore.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** 登录鉴权与个人密码维护 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.error("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw BusinessException.error("账号已被停用");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.create(user.getId(), user.getUsername(), user.getRole()));
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    public void changePassword(PasswordDTO dto) {
        SysUser user = userMapper.selectById(UserContext.currentId());
        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw BusinessException.error("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
