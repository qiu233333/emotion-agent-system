package com.bishe.backend.common;

import lombok.Data;

/**
 * 后端统一返回对象。
 *
 * <p>所有 Controller 接口统一返回该结构，前端就可以固定按照
 * {@code code/message/data} 三个字段判断请求是否成功并读取业务数据。</p>
 *
 * @param <T> data 字段的数据类型
 */
@Data
public class Result<T> {

    /**
     * 业务状态码，200 表示成功，其他值表示失败。
     */
    private Integer code;

    /**
     * 接口提示信息，用于前端提示用户或调试问题。
     */
    private String message;

    /**
     * 接口返回的业务数据。
     */
    private T data;

    /**
     * 创建一个成功返回对象。
     *
     * @param data 接口返回的数据
     * @param <T> data 字段的数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 创建一个不带数据的成功返回对象。
     *
     * @param <T> data 字段的数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建一个失败返回对象。
     *
     * @param message 失败提示信息
     * @param <T> data 字段的数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 创建一个可指定业务状态码的失败返回对象。
     *
     * @param code 业务状态码，例如 401 表示未登录或 token 无效
     * @param message 失败提示信息
     * @param <T> data 字段的数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}
