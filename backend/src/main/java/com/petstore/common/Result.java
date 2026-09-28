package com.petstore.common;

import lombok.Data;

/**
 * 统一响应体：{code, msg, data}
 * code: 200 成功 / 400 业务拒绝 / 401 未登录 / 403 无权限 / 500 系统异常
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        return build(200, "success", data);
    }

    public static Result<Void> ok() {
        return build(200, "success", null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    private static <T> Result<T> build(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
