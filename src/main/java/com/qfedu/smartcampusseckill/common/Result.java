// com.qfedu.smartcampusseckill.common.Result (回顾)
package com.qfedu.smartcampusseckill.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200: 成功, 500: 失败
    private String msg;   // 提示信息
    private T data;       // 返回的数据

    // 私有构造函数，强制使用静态方法创建
    private Result() {}

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    // 重载 success 方法，允许自定义成功消息
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = msg;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}