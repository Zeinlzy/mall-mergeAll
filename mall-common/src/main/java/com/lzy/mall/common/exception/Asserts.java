package com.lzy.mall.common.exception; // 包声明，与ApiException在同一个包下，表明它们紧密相关

import com.lzy.mall.common.api.IErrorCode;

// 注意：这个类通常会导入 IErrorCode 接口，即使在本段代码片段中没有直接在参数类型中显式使用（除了fail(IErrorCode)），
// 但其主要目的是与 ApiException 配合，而 ApiException 依赖 IErrorCode。

/**
 * 断言处理类，用于封装和简化抛出API异常的操作。
 * 当业务逻辑中断言（即预期的条件）失败时，可以使用此类中的方法来快速抛出 {@link ApiException}。
 * 这种方式使得错误处理更加统一和简洁。
 */
public class Asserts { // 定义公共类Asserts

    /**
     * 静态方法，用于当某个断言失败时，抛出一个包含指定错误消息的 {@link ApiException}。
     * @param message 描述错误的详细信息字符串。
     *                当这个方法被调用时，意味着一个预期的条件没有满足，
     *                并且应该以这个消息为内容抛出一个API异常。
     */
    public static void fail(String message) {
        // 创建一个新的ApiException实例，使用传入的message作为错误信息，并立即抛出它。
        // 这样，调用处的代码会因异常而中断执行。
        throw new ApiException(message);
    }

    /**
     * 静态方法，用于当某个断言失败时，抛出一个包含指定错误码（及其对应消息）的 {@link ApiException}。
     * 这是更推荐的方式，因为它使用了结构化的错误信息（通过IErrorCode接口实现）。
     * @param errorCode 实现了IErrorCode接口的对象，包含了错误码和对应的错误描述。
     *                  当这个方法被调用时，意味着一个预期的条件没有满足，
     *                  并且应该以这个错误码为核心抛出一个API异常。
     */
    public static void fail(IErrorCode errorCode) {
        // 创建一个新的ApiException实例，使用传入的errorCode对象，并立即抛出它。
        // ApiException的构造函数会从errorCode中提取错误消息，并保存错误码本身。
        throw new ApiException(errorCode);
    }
}
