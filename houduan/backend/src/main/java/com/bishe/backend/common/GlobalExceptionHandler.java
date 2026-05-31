package com.bishe.backend.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理模块。
 *
 * <p>Controller 或 Service 抛出的常见业务异常会在这里统一转换成 Result，
 * 这样前端始终可以按照 code/message/data 的结构处理响应。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验、登录失败等业务异常。
     *
     * @param exception 业务异常对象
     * @return 统一失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        return Result.error(exception.getMessage());
    }

    /**
     * 处理未预期的系统异常。
     *
     * @param exception 系统异常对象
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.error("系统异常，请稍后再试");
    }
}
