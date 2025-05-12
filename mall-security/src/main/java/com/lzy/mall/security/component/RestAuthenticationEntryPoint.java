package com.lzy.mall.security.component;

import cn.hutool.json.JSONUtil;
import com.lzy.mall.common.api.CommonResult;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 自定义未认证处理器，当用户未认证（未登录或token过期/无效）访问受保护资源时，
 * 由此处理器处理认证异常，返回统一格式的JSON响应。
 * 它实现了Spring Security的AuthenticationEntryPoint接口。
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 当用户尝试访问受保护资源但未提供任何凭证或凭证无效时，此方法被调用。
     * 它负责设置HTTP响应的状态码、头部信息，并返回一个表示“未认证”的统一格式错误响应（通常是JSON）。
     *
     * @param request       当前HTTP请求对象
     * @param response      当前HTTP响应对象
     * @param authException 认证过程中发生的异常，包含失败原因
     * @throws IOException      如果写入响应时发生IO错误
     * @throws ServletException 如果处理请求发生Servlet相关错误
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 设置响应头，允许所有来源的跨域请求
        response.setHeader("Access-Control-Allow-Origin", "*");
        // 设置响应头，指示客户端和代理服务器不缓存响应
        response.setHeader("Cache-Control","no-cache");
        // 设置响应的字符编码为UTF-8，以支持中文等字符
        response.setCharacterEncoding("UTF-8");
        // 设置响应的内容类型为application/json，告诉客户端响应体是JSON格式
        response.setContentType("application/json");
        // 获取响应的PrintWriter，准备向响应体写入内容
        // 使用自定义的CommonResult构建一个表示未认证的统一结果对象，其中包含认证失败的错误信息
        // 使用Hutool的JSONUtil将CommonResult对象转换为JSON字符串
        // 将JSON字符串写入响应体
        response.getWriter().println(JSONUtil.parse(CommonResult.unauthorized(authException.getMessage())));
        // 刷新PrintWriter，确保所有缓冲的输出都被发送到客户端
        response.getWriter().flush();
    }
}
