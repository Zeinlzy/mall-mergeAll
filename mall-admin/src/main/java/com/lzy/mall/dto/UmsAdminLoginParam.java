package com.lzy.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;

/**
 * UmsAdminLoginParam类
 * 用于封装后台管理员登录请求参数，包括用户名和密码。
 * 该类配合参数校验注解和Swagger注解，实现接口参数校验和文档生成。
 */
@Data
@EqualsAndHashCode
public class UmsAdminLoginParam {
    /**
     * 用户名，不能为空
     */
    @NotEmpty
    @Schema(description  = "用户名",required = true)
    private String username;

    /**
     * 密码，不能为空
     */
    @NotEmpty
    @Schema(description  = "密码",required = true)
    private String password;
}
