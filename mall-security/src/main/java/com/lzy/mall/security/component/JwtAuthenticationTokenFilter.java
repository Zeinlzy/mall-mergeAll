package com.lzy.mall.security.component;

import com.lzy.mall.security.utils.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器，用于在每个请求到达时检查JWT令牌的有效性，并根据令牌信息设置Spring Security的认证上下文。
 * 它继承自OncePerRequestFilter，确保每个请求只经过一次该过滤器。
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader; // JWT存储在请求头中的名称，例如 "Authorization"
    @Value("${jwt.tokenHead}")
    private String tokenHead;   // JWT在请求头中的前缀，例如 "Bearer "

    /**
     * 在每次HTTP请求到达时执行的过滤方法。
     * 它从请求头中提取JWT，验证令牌，加载用户详情，并根据令牌信息设置Spring Security的认证上下文。
     *
     * @param request     当前HTTP请求
     * @param response    当前HTTP响应
     * @param chain       过滤器链，用于将请求传递给下一个过滤器或目标资源
     * @throws ServletException 如果处理请求发生Servlet相关错误
     * @throws IOException      如果处理请求发生I/O相关错误
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 从请求头中获取指定名称的认证信息
        String authHeader = request.getHeader(this.tokenHeader);
        // 检查认证信息是否存在且以指定前缀（如"Bearer "）开头
        if (authHeader != null && authHeader.startsWith(this.tokenHead)) {
            // 提取令牌字符串，移除前缀（如"Bearer "）
            String authToken = authHeader.substring(this.tokenHead.length());
            // 从令牌中解析出用户名
            String username = jwtTokenUtil.extractUsername(authToken);
            LOGGER.info("checking username:{}", username);

            // 检查用户名是否有效，并且当前SecurityContext中还没有认证信息（避免重复认证）
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 根据用户名加载用户详情（包括权限信息）
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                // 验证令牌的有效性（如签名、过期时间以及是否与加载的用户匹配）
                if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                    // 令牌有效，创建认证对象
                    // UsernamePasswordAuthenticationToken 是 Spring Security 默认的认证对象，
                    // 第一个参数是主体(principal)，即UserDetails；第二个参数是凭证(credentials)，对于基于Token的认证，凭证通常设为null；
                    // 第三个参数是用户的权限列表。
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    // 设置认证对象的额外详情，如IP地址、会话ID等
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    LOGGER.info("authenticated user:{}", username);
                    // 将认证对象设置到SecurityContext中，表示当前用户已通过认证
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 继续执行后续的过滤器链或到达目标Servlet
        chain.doFilter(request, response);
    }
}