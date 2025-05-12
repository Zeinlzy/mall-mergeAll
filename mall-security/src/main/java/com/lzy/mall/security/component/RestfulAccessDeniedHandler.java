package com.lzy.mall.security.component;

import cn.hutool.json.JSONUtil;
import com.lzy.mall.common.api.CommonResult;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 自定义未授权（访问拒绝）处理器，用于处理已认证用户尝试访问没有足够权限的资源时的情况，
 * 返回统一格式的JSON错误响应。
 * 它实现了Spring Security的AccessDeniedHandler接口。
 */
public class RestfulAccessDeniedHandler implements AccessDeniedHandler{

    /**
     * 当已认证用户因权限不足而访问被拒绝时，此方法被调用。
     * 它负责设置HTTP响应，并向客户端返回一个表示“未授权”（访问拒绝）的统一格式错误响应（通常是JSON）。
     *
     * @param request       当前HTTP请求对象
     * @param response      当前HTTP响应对象
     * @param e             访问被拒绝时发生的异常，包含拒绝原因
     * @throws IOException      如果写入响应时发生IO错误
     * @throws ServletException 如果处理请求发生Servlet相关错误
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        // 设置响应头，允许所有来源的跨域请求
        response.setHeader("Access-Control-Allow-Origin", "*");
        // 设置响应头，指示客户端和代理服务器不缓存响应
        response.setHeader("Cache-Control","no-cache");
        // 设置响应的字符编码为UTF-8，以支持中文等字符
        response.setCharacterEncoding("UTF-8");
        // 设置响应的内容类型为application/json，告诉客户端响应体是JSON格式
        response.setContentType("application/json");
        // 获取响应的PrintWriter，准备向响应体写入内容
        // 使用自定义的CommonResult构建一个表示访问被拒绝（禁止访问）的统一结果对象，其中包含拒绝访问的错误信息
        // 使用Hutool的JSONUtil将CommonResult对象转换为JSON字符串
        // 将JSON字符串写入响应体
        response.getWriter().println(JSONUtil.parse(CommonResult.forbidden(e.getMessage())));
        // 刷新PrintWriter，确保所有缓冲的输出都被发送到客户端
        response.getWriter().flush();
    }
}
