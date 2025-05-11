package com.lzy.mall.common.api;

/**
 * API返回码封装类
 */
//ResultCode由enum关键字定义，是枚举类
public enum ResultCode implements IErrorCode {
    /**
     * SUCCESS(200, "操作成功") 这种写法的含义就是： 定义一个名为 SUCCESS 的枚举常量，它是 ResultCode 类型的一个实例，
     * 创建这个实例时，使用参数 200 和 "操作成功" 来调用 ResultCode 的构造方法进行初始化。其他枚举常量也是如此
     */
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限");
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
