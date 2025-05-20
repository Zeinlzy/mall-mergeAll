package com.lzy.mall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * UmsAdminParam类
 * 用于封装后台用户注册时提交的参数，包括用户名、密码、头像、邮箱、昵称和备注等信息。
 * 结合参数校验注解和Swagger注解，实现接口参数校验和文档生成。
 */
@Getter
@Setter
public class UmsAdminParam {
    /**
     * 用户名，不能为空
     */
    @NotEmpty
    @Schema(description  = "用户名")
    private String username;

    /**
     * 密码，不能为空
     */
    @NotEmpty
    @Schema(description  = "密码")
    private String password;

    /**
     * 用户头像
     */
    @Schema(description  = "用户头像")
    private String icon;

    /**
     * 邮箱，需为合法邮箱格式
     */
    @Email
    @Schema(description  = "邮箱")
    private String email;

    /**
     * 用户昵称
     */
    @Schema(description  = "用户昵称")
    private String nickName;

    /**
     * 备注
     */
    @Schema(description  = "备注")
    private String note;
}
