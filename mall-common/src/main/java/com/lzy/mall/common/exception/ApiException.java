package com.lzy.mall.common.exception;

import com.lzy.mall.common.api.IErrorCode;

/**
 * 作用：在项目中（特别是涉及 API 接口的后端服务中）提供一种标准化的方式来表示和处理业务逻辑相关的错误。
 * 当 API 调用过程中发生可预见的、需要明确告知调用方的错误时（例如：用户输入无效、资源未找到、权限不足等），程序可以抛出 ApiException。
 *
 * 自定义API异常类
 * 该类用于在API（应用程序编程接口）层面发生错误时抛出。
 * 它继承自RuntimeException，意味着这是一个非受检异常，
 * 通常用于表示程序在运行时发生的、调用方不一定需要强制捕获的错误。
 */
public class ApiException extends RuntimeException { // 定义公共类ApiException，继承自RuntimeException

    // 声明一个私有的IErrorCode类型的成员变量，用于存储具体的错误码和信息
    private IErrorCode errorCode;

    /**
     * 构造函数：使用一个IErrorCode对象来创建ApiException实例。
     * 这是推荐的构造方式，因为它能携带结构化的错误信息。
     * @param errorCode 实现了IErrorCode接口的对象，包含了错误码和对应的错误描述。
     */
    public ApiException(IErrorCode errorCode) {
        // 调用父类RuntimeException的构造函数，并将errorCode中的错误描述信息作为异常的message。
        // 这样做的好处是，即使这个异常被当作普通的RuntimeException捕获，
        // 也能通过getMessage()获取到有意义的错误描述。
        super(errorCode.getMessage());
        // 将传入的errorCode对象赋值给成员变量，以便后续通过getErrorCode()方法获取。
        this.errorCode = errorCode;
    }

    /**
     * 构造函数：仅使用一个错误消息字符串来创建ApiException实例。
     * 适用于不方便或不需要传递具体错误码的情况。
     * @param message 描述错误的详细信息字符串。
     */
    public ApiException(String message) {
        // 调用父类RuntimeException的构造函数，设置异常的详细信息。
        super(message);
    }

    /**
     * 构造函数：使用一个Throwable对象（通常是另一个异常）来创建ApiException实例。
     * 这用于包装另一个底层异常，形成异常链。
     * @param cause 导致此ApiException被抛出的根本原因（通常是另一个被捕获的异常）。
     */
    public ApiException(Throwable cause) {
        // 调用父类RuntimeException的构造函数，传入根本原因。
        super(cause);
    }

    /**
     * 构造函数：使用一个错误消息字符串和一个Throwable对象来创建ApiException实例。
     * @param message 描述错误的详细信息字符串。
     * @param cause 导致此ApiException被抛出的根本原因。
     */
    public ApiException(String message, Throwable cause) {
        // 调用父类RuntimeException的构造函数，传入错误信息和根本原因。
        super(message, cause);
    }

    /**
     * 获取此异常关联的IErrorCode对象。
     * 通过此方法，异常的捕获者可以获取到结构化的错误信息（错误码和错误描述）。
     * @return 返回内部存储的IErrorCode对象；如果异常不是通过IErrorCode构造的，可能返回null。
     */
    public IErrorCode getErrorCode() {
        return errorCode;
    }
}
