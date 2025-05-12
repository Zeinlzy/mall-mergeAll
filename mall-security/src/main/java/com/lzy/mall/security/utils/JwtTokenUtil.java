package com.lzy.mall.security.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException; // 明确导入签名异常
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // 使用 Spring 的 StringUtils 检查空字符串

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit; // 用于时间单位转换，替代 Hutool

/**
 * JWT（JSON Web Token）工具类，用于生成、解析、验证和刷新JWT token。
 * 集成了现代JWT库API、详细错误处理和高级刷新策略。
 */
@Component
public class JwtTokenUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    // JWT 标准声明（Claims）的 Key
    private static final String CLAIM_KEY_USERNAME = "sub"; // Subject (主体，通常是用户标识)
    private static final String CLAIM_KEY_ISSUED_AT = "iat"; // Issued At (签发时间)

    @Value("${jwt.secret}")
    private String secret; // JWT 签名密钥
    @Value("${jwt.expiration}")
    private Long expiration; // JWT 过期时间，单位：秒
    @Value("${jwt.tokenHead:}") // Token 前缀，例如 "Bearer "，如果未配置则默认为空
    private String tokenHead;

    // --- 密钥生成 ---

    /**
     * 生成安全的 HMAC-SHA 密钥。
     * JWT库会根据提供的算法（HS512）自动处理密钥字节长度的要求。
     */
    private SecretKey generateKey() {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret is not configured!");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // --- Token 构建 ---

    /**
     * 根据 Claims 构建最终的 JWT token 字符串。
     *
     * @param claims 包含用户信息的声明
     * @return JWT token 字符串
     */
    private String buildToken(Map<String, Object> claims) {
        // 使用现代 JWT Builder API
        return Jwts.builder()
                .setClaims(claims) // 设置 Payload (载荷)，包含声明
                .setExpiration(generateExpirationDate()) // 设置过期时间
                .setIssuedAt(new Date()) // 设置签发时间（这里强制更新，以反映新的生成时间）
                .signWith(generateKey(), SignatureAlgorithm.HS512) // 使用指定算法和密钥签名
                .compact(); // 生成最终的压缩字符串
    }

    // --- Token 生成 ---

    /**
     * 根据 UserDetails 信息生成 JWT token。
     *
     * @param userDetails 用户详细信息
     * @return 新生成的 JWT token 字符串 (不含tokenHead)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        // buildToken方法中会设置iat，这里可以省略或保留用于构建初始Claims
        // claims.put(CLAIM_KEY_ISSUED_AT, new Date()); // buildToken会覆盖，可省略
        return buildToken(claims);
    }

    // --- Token 解析 ---

    /**
     * 解析并验证 JWT 令牌，提取 Claims (负载)。
     * 该方法会验证令牌的签名和有效期。
     * 注意：此方法预期接收一个不包含 tokenHead 的原始 token 字符串。
     *
     * @param token 要解析的原始 JWT 令牌字符串 (不含tokenHead)
     * @return 如果令牌有效并解析成功，返回 Claims 对象；否则返回 null
     */
    public Claims parseToken(String token) {
        // 检查 token 字符串是否有效
        if (!StringUtils.hasText(token)) {
            LOGGER.warn("Attempted to parse null or empty token string.");
            return null;
        }

        try {
            // 使用现代 JWT Parser Builder API 构建解析器并设置签名密钥
            return Jwts.parserBuilder()
                    .setSigningKey(generateKey()) // 设置用于签名验证的密钥
                    .build()                       // 构建解析器实例
                    .parseClaimsJws(token)         // 解析 JWS (带有签名的JWT)，并验证签名
                    .getBody();                    // 如果验证成功，获取 JWT 的 Claims (负载)
        } catch (ExpiredJwtException e) {
            // 捕获 Token 过期异常
            LOGGER.warn("Token 已过期: {}", e.getMessage()); // 降级为 warn 级别，过期是预期内的状态
        } catch (SignatureException e) {
            // 捕获 Token 签名验证失败异常
            LOGGER.warn("Token 签名验证失败: {}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            // 捕获 Token 非法（如格式错误）异常
            LOGGER.warn("Token 非法或格式错误: {}", e.getMessage());
        } catch (JwtException e) {
            // 捕获其他 JWT 解析相关的通用异常
            LOGGER.warn("Token 解析失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // 捕获非法参数异常，例如密钥无效
            LOGGER.error("JWT 密钥或参数无效: {}", e.getMessage()); // 这个错误可能更严重，用 error
        }
        // 如果解析或验证过程中发生任何异常，返回 null
        return null;
    }

    // --- Claims 提取助手方法 ---

    /**
     * 从 token 中提取登录用户名。
     *
     * @param token 原始 token 字符串 (不含tokenHead)
     * @return 用户名，如果 token 无效或无法提取，则返回 null
     */
    public String extractUsername(String token) {
        Claims claims = parseToken(token); // 使用健壮的解析方法
        // getSubject() 是标准方式获取 "sub" 声明
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 token 中获取过期时间。
     *
     * @param token 原始 token 字符串 (不含tokenHead)
     * @return 过期时间 Date 对象，如果 token 无效或无过期时间声明，则返回 null
     */
    private Date getExpirationDate(String token) {
        Claims claims = parseToken(token); // 使用健壮的解析方法
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 从 token 中获取签发时间 (issued at)。
     *
     * @param token 原始 token 字符串 (不含tokenHead)
     * @return 签发时间 Date 对象，如果 token 无效或无签发时间声明，则返回 null
     */
    private Date getIssuedAtDate(String token) {
        Claims claims = parseToken(token); // 使用健壮的解析方法
        // 使用 get(key, type) 安全地获取指定类型的声明
        return claims != null ? claims.get(CLAIM_KEY_ISSUED_AT, Date.class) : null;
    }

    // --- 验证助手方法 ---

    /**
     * 生成 token 的过期时间 Date 对象。
     *
     * @return 过期时间 Date 对象
     */
    private Date generateExpirationDate() {
        // expiration 是秒，转换为毫秒
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 判断 token 是否已经失效 (过期)。
     *
     * @param token 原始 token 字符串 (不含tokenHead)
     * @return 如果 token 已过期或无法获取过期时间，则返回 true；否则返回 false
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = getExpirationDate(token);
        // 如果无法获取过期时间（例如 parseToken 失败或 token 没有 exp 声明），为安全起见也视为过期
        return expirationDate == null || expirationDate.before(new Date());
    }

    /**
     * 判断 token 是否在指定时间阈值内刚刚签发或刷新过。
     * 用于防止客户端在短时间内频繁刷新 token。
     *
     * @param token 原始 token 字符串 (不含tokenHead)
     * @param secondsThreshold 时间阈值（秒）。如果 token 的签发时间距离当前时间小于此阈值，则认为是最近签发的。
     * @return 如果 token 的签发时间在当前时间的前 secondsThreshold 秒内，则返回 true；否则返回 false。
     *         如果无法获取签发时间，默认返回 false (即不认为是最近签发的，允许刷新)。
     */
    private boolean isRecentlyIssued(String token, int secondsThreshold) {
        // 如果阈值 <= 0，则此检查无效，始终返回 false
        if (secondsThreshold <= 0) {
            return false;
        }

        Date issuedAt = getIssuedAtDate(token);

        // 如果无法获取签发时间（例如 parseToken 失败或 token 没有 iat 声明），则认为不是最近签发的，允许刷新
        if (issuedAt == null) {
            LOGGER.warn("Token missing {} claim, cannot check if recently issued for refresh.", CLAIM_KEY_ISSUED_AT);
            return false;
        }

        Date now = new Date();
        // 计算当前时间距离签发时间过去了多少毫秒
        long timeSinceIssueMillis = now.getTime() - issuedAt.getTime();

        // 如果时间差小于0（客户端时钟超前于服务器或iat未来），或者时间差小于阈值，则认为是最近签发的
        // 正常情况下 timeSinceIssueMillis >= 0
        return timeSinceIssueMillis >= 0 && timeSinceIssueMillis < TimeUnit.SECONDS.toMillis(secondsThreshold);
    }


    /**
     * 验证 token 是否有效。
     * 检查用户名是否匹配，以及 token 是否过期且签名格式正确 (由 parseToken 内部完成)。
     * 注意：此方法预期接收一个不包含 tokenHead 的原始 token 字符串。
     *
     * @param token       要验证的原始 token 字符串 (不含tokenHead)
     * @param userDetails 用户的详细信息，用于比对用户名
     * @return 如果 token 对该用户有效，则返回 true；否则返回 false
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        // 确保 userDetails 不为 null
        if (userDetails == null) {
            LOGGER.warn("UserDetails is null during token validation.");
            return false;
        }

        String username = extractUsername(token); // 提取用户名（内部已包含解析和基本验证）

        // 验证用户名是否一致，并且 token 未过期
        // extractUsername 返回 null 表示解析或签名失败，此时 username.equals() 会抛异常，所以先检查 username != null
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // --- Token 刷新 ---

    /**
     * 从带有配置的 tokenHead 前缀的字符串中提取原始 token 字符串。
     * 如果 tokenHead 未配置或字符串不以 tokenHead 开头，则返回 null。
     *
     * @param tokenString 带有前缀的 token 字符串 (e.g., "Bearer xxx.yyy.zzz")
     * @return 原始 token 字符串 (不含 tokenHead)，如果格式不符或 tokenHead 未配置，则返回 null
     */
    public String extractRawTokenWithHead(String tokenString) {
        // 检查 token 字符串和 tokenHead 是否有效
        if (!StringUtils.hasText(tokenString) || !StringUtils.hasText(tokenHead)) {
            return null;
        }

        // 检查是否以配置的 tokenHead 开头
        if (tokenString.startsWith(tokenHead)) {
            // 检查字符串长度是否足够，以避免 IndexOutOfBoundsException
            if (tokenString.length() > tokenHead.length()) {
                return tokenString.substring(tokenHead.length());
            } else {
                // 字符串只包含 tokenHead，后面没有 token
                LOGGER.warn("Token string contains only token head: {}", tokenString);
                return null;
            }
        }

        // 不以配置的 tokenHead 开头
        LOGGER.warn("Token string does not start with configured token head: {}", tokenString);
        return null;
    }


    /**
     * 刷新 Token。
     * 检查原始 token 是否有效（未过期，签名正确），并判断是否在指定时间内刚刚签发/刷新过。
     * 如果 token 有效且未在防止重复刷新的阈值内，则生成一个新的 token 并更新签发时间。
     *
     * @param oldToken 原始 token 字符串 (不含 tokenHead)
     * @param refreshThresholdSeconds 防止重复刷新的时间阈值（秒）。如果原始 token 的签发时间距离当前时间小于此阈值，则认为最近刷新过，直接返回原 token。
     *                                设置为 <= 0 可以禁用此功能，只要未过期就刷新。
     * @return 刷新的 token 字符串 (不含 tokenHead)。如果原始 token 无效、已过期，或者在刷新阈值内，则返回 null（表示无需刷新或无法刷新）。
     *         注意：如果 token 在阈值内，此方法返回 null，调用者应使用原 token。
     *         一个更好的设计可能是返回原 token 而不是 null，但为了区分“刷新成功”和“无需刷新”，这里返回新 token 或 null。
     *         **建议调用此方法的代码判断返回值是否为 null，如果为 null，且原 token 是有效的，则继续使用原 token。**
     */
    public String refreshToken(String oldToken, int refreshThresholdSeconds) {
        // 使用健壮的解析方法获取 Claims
        // parseToken 内部已检查 token 的 null/empty，并记录错误
        Claims claims = parseToken(oldToken);

        // 检查是否解析失败 或 token 已过期
        if (claims == null || isTokenExpired(oldToken)) {
            // parseToken 内部已记录具体原因 (过期、非法等)
            LOGGER.info("Token is invalid or expired, cannot refresh.");
            return null; // Token 无效或已过期，无法刷新
        }

        // 检查 token 是否在指定的阈值内刚刚签发/刷新过，如果是，则无需刷新
        // 避免在短时间内重复生成 token
        if (isRecentlyIssued(oldToken, refreshThresholdSeconds)) {
            LOGGER.info("Token issued within the last {} seconds, returning null (indicates no refresh needed).", refreshThresholdSeconds);
            // 返回 null 表示不需要刷新，调用方应该继续使用 oldToken
            return null;
        }

        // 如果 token 有效且不在最近刷新阈值内，则进行刷新
        // 更新 "issued at" (iat) 声明为当前时间，表示 token 被“重新签发”或“刷新”
        claims.setIssuedAt(new Date());

        // 使用更新后的 Claims 构建并返回新的 token
        String newToken = buildToken(claims);
        LOGGER.info("Token refreshed successfully.");
        return newToken;
    }

    /**
     * 刷新 Token，使用默认的防止重复刷新时间阈值（例如 30 分钟）。
     *
     * @param oldToken 原始 token 字符串 (不含 tokenHead)
     * @return 刷新的 token 字符串 (不含 tokenHead)。如果原始 token 无效、已过期，或者在默认的刷新阈值内，则返回 null。
     *         注意：如果 token 在默认阈值内，此方法返回 null，调用者应使用原 token。
     */
    public String refreshToken(String oldToken) {
        // 使用一个默认的刷新阈值，例如 30 分钟 = 1800 秒
        int defaultRefreshThresholdSeconds = (int)TimeUnit.MINUTES.toSeconds(30);
        return refreshToken(oldToken, defaultRefreshThresholdSeconds);
    }

    // --- 考虑 tokenHead 的公共刷新方法 (可选，通常在 Service 或 Filter 层处理 tokenHead 更合适) ---
    /**
     * 刷新带有 tokenHead 前缀的 token。
     * 首先提取原始 token，然后尝试刷新，如果刷新成功生成了新 token，则重新加上 tokenHead 前缀。
     * 如果原始 token 无效、过期或在刷新阈值内，且原始 token 是有效的（虽然无需刷新），则返回带有 tokenHead 的原始 token。
     * 如果原始 token 格式错误（无 tokenHead 或为空），则返回 null。
     *
     * @param oldTokenWithHead 带有 tokenHead 前缀的 token 字符串
     * @param refreshThresholdSeconds 防止重复刷新的时间阈值（秒）
     * @return 刷新后带有 tokenHead 的 token 字符串，如果无需刷新则返回原始带有 tokenHead 的 token，如果无效则返回 null。
     */
    public String refreshHeadToken(String oldTokenWithHead, int refreshThresholdSeconds) {
        String rawToken = extractRawTokenWithHead(oldTokenWithHead);

        // 如果无法提取原始 token (格式错误或无 head)
        if (!StringUtils.hasText(rawToken)) {
            LOGGER.warn("Cannot extract raw token from string with head: {}", oldTokenWithHead);
            return null;
        }

        // 尝试刷新原始 token
        // refreshToken 方法会返回 null 如果无效、过期或在阈值内
        String newToken = refreshToken(rawToken, refreshThresholdSeconds);

        // 如果 refreshToken 返回了新的 token (说明刷新成功且不在阈值内)
        if (StringUtils.hasText(newToken)) {
            // 将新的原始 token 加上 tokenHead 前缀返回
            return tokenHead + newToken;
        } else {
            // refreshToken 返回 null。这可能意味着：
            // 1. oldToken 无效/已过期 (parseToken 失败或 isTokenExpired 为 true)
            // 2. oldToken 在刷新阈值内 (isRecentlyIssued 为 true)

            // 如果是情况 1，原始 token 本身就是无效的，无需返回任何 token
            Claims oldClaims = parseToken(rawToken); // 重新解析一次，确认原始 token 的状态
            if (oldClaims == null || isTokenExpired(rawToken)) {
                LOGGER.warn("Original raw token is invalid or expired, cannot return original token with head.");
                return null;
            }

            // 如果是情况 2，原始 token 是有效的，只是因为在阈值内所以 refreshToken 返回了 null
            // 此时应该返回原始的、带有 head 的 token，让客户端继续使用
            LOGGER.info("Original raw token is valid but recently issued, returning original token with head.");
            return oldTokenWithHead;
        }
    }

    /**
     * 刷新带有 tokenHead 前缀的 token，使用默认的防止重复刷新时间阈值（例如 30 分钟）。
     *
     * @param oldTokenWithHead 带有 tokenHead 前缀的 token 字符串
     * @return 刷新后带有 tokenHead 的 token 字符串，如果无需刷新则返回原始带有 tokenHead 的 token，如果无效则返回 null。
     */
    public String refreshHeadToken(String oldTokenWithHead) {
        int defaultRefreshThresholdSeconds = (int)TimeUnit.MINUTES.toSeconds(30);
        return refreshHeadToken(oldTokenWithHead, defaultRefreshThresholdSeconds);
    }
}