package com.petstore.config;

import com.petstore.entity.SysUser;
import com.petstore.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 首次启动初始化默认账号（数据脚本不包含用户数据，密码以 BCrypt 落库） */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("店长");
        admin.setRole("ADMIN");
        admin.setPhone("13800000001");
        admin.setStatus(1);
        userMapper.insert(admin);

        SysUser staff = new SysUser();
        staff.setUsername("staff");
        staff.setPassword(passwordEncoder.encode("123456"));
        staff.setRealName("小李");
        staff.setRole("STAFF");
        staff.setPhone("13800000002");
        staff.setStatus(1);
        userMapper.insert(staff);

        log.info("已初始化默认账号：admin/123456（管理员）、staff/123456（店员）");
    }
}
