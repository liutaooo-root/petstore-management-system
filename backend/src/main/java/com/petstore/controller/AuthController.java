package com.petstore.controller;

import com.petstore.common.Result;
import com.petstore.dto.LoginDTO;
import com.petstore.dto.PasswordDTO;
import com.petstore.service.AuthService;
import com.petstore.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.ok(authService.login(dto));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.ok();
    }
}
