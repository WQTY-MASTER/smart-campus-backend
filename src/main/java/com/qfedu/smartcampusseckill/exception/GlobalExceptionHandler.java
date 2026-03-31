package com.qfedu.smartcampusseckill.exception;

import com.qfedu.smartcampusseckill.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将常见业务异常统一转换为 Result 返回，避免前端收到 500 白屏。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}

