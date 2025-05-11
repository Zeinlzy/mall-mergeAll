package com.lzy.mall.common.exception;

import cn.hutool.core.util.StrUtil;
import com.lzy.mall.common.api.CommonResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLSyntaxErrorException;

/**
 * 全局异常处理类
 * 使用 @ControllerAdvice 注解，此类会应用于所有的 Controller。
 * 当 Controller 中抛出未被捕获的异常时，如果匹配到 @ExceptionHandler 中定义的异常类型，
 * 相应的方法就会被调用来处理这个异常，并返回一个统一格式的响应。
 */
@ControllerAdvice // 声明这是一个控制器增强器，用于全局处理控制器发生的特定事件，如此处的异常
public class GlobalExceptionHandler {

    /**
     * 处理自定义的API异常 (ApiException)。
     * @param e 捕获到的 ApiException 实例。
     * @return 返回一个封装了错误信息的 CommonResult 对象。
     */
    @ResponseBody // 表示此方法的返回值将直接作为HTTP响应体的内容（通常序列化为JSON）
    @ExceptionHandler(value = ApiException.class) // 指定此方法处理 ApiException 类型的异常
    public CommonResult handle(ApiException e) {
        // 检查 ApiException 实例中是否包含 errorCode
        if (e.getErrorCode() != null) {
            // 如果有 errorCode，则使用 errorCode 来构造失败的 CommonResult
            // CommonResult.failed(IErrorCode) 通常会同时设置错误码和错误消息
            return CommonResult.failed(e.getErrorCode());
        }
        // 如果没有 errorCode，则直接使用异常的 message 来构造失败的 CommonResult
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理请求体参数校验异常 (MethodArgumentNotValidException)。
     * 这种异常通常在 Spring MVC 中使用 @RequestBody 和 @Valid 注解校验请求体时，校验失败后抛出。
     * @param e 捕获到的 MethodArgumentNotValidException 实例。
     * @return 返回一个表示参数校验失败的 CommonResult 对象。
     */
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class) // 指定处理 MethodArgumentNotValidException
    public CommonResult handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult(); // 获取绑定结果，其中包含了校验错误信息
        String message = null;
        if (bindingResult.hasErrors()) { // 判断是否存在校验错误
            FieldError fieldError = bindingResult.getFieldError(); // 获取第一个字段校验错误
            if (fieldError != null) {
                // 将错误字段名和默认的错误消息拼接起来作为最终的错误提示
                // 例如："username不能为空"
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        // 使用 CommonResult 的 validateFailed 方法构造参数校验失败的响应
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理表单参数绑定或查询参数校验异常 (BindException)。
     * 这种异常通常在 Spring MVC 中不使用 @RequestBody (例如，表单提交、查询参数绑定到对象)
     * 且使用 @Valid 注解校验时，校验失败后抛出。
     * @param e 捕获到的 BindException 实例。
     * @return 返回一个表示参数校验失败的 CommonResult 对象。
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class) // 指定处理 BindException
    public CommonResult handleValidException(BindException e) {
        BindingResult bindingResult = e.getBindingResult(); // 获取绑定结果
        String message = null;
        if (bindingResult.hasErrors()) { // 判断是否存在校验错误
            FieldError fieldError = bindingResult.getFieldError(); // 获取第一个字段校验错误
            if (fieldError != null) {
                // 拼接错误字段名和错误消息
                message = fieldError.getField() + fieldError.getDefaultMessage();
            }
        }
        // 使用 CommonResult 的 validateFailed 方法构造参数校验失败的响应
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理SQL语法错误异常 (SQLSyntaxErrorException)。
     * @param e 捕获到的 SQLSyntaxErrorException 实例。
     * @return 返回一个表示操作失败的 CommonResult 对象，可能包含特殊处理后的错误消息。
     */
    @ResponseBody
    @ExceptionHandler(value = SQLSyntaxErrorException.class) // 指定处理 SQLSyntaxErrorException
    public CommonResult handleSQLSyntaxErrorException(SQLSyntaxErrorException e) {
        String message = e.getMessage(); // 获取原始的SQL错误消息
        // 特殊处理：如果错误消息非空且包含 "denied" 字符串
        // 这通常用于演示环境，防止用户进行敏感操作或提示用户权限不足
        if (StrUtil.isNotEmpty(message) && message.contains("denied")) {
            message = "演示环境暂无修改权限，如需修改数据可本地搭建后台服务！"; // 替换为更友好的提示信息
        }
        // 使用处理后的（或原始的）消息构造失败的 CommonResult
        return CommonResult.failed(message);
    }
}