package com.lzy.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotEmpty;

/**
 * 修改管理员密码参数类
 * 用于封装管理员修改密码时提交的参数，包括用户名、旧密码和新密码。
 * 本类使用了参数校验注解（如@NotEmpty），用于在数据提交时自动校验字段的有效性，保证接口接收到的数据符合要求。
 */
@Getter
@Setter
public class UpdateAdminPasswordParam {
    /**
     * 用户名，不能为空
     */
    @NotEmpty
    @Schema(description = "用户名", required = true)
    private String username;

    /**
     * 旧密码，不能为空
     */
    @NotEmpty
    @Schema(description = "旧密码", required = true)
    private String oldPassword;

    /**
     * 新密码，不能为空
     */
    @NotEmpty
    @Schema(description = "新密码", required = true)
    private String newPassword;
}
