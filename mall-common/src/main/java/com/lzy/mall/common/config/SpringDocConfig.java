package com.lzy.mall.common.config;

import com.lzy.mall.common.domain.SwaggerProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDocConfig 是一个 Spring Boot 配置类，用于自定义 SpringDoc (OpenAPI 3) 的行为，从而生成和定制化 API 文档
 * 这个类使得开发者可以通过外部配置文件轻松定制化其项目的 API 文档，而不需要硬编码这些信息
 */
@Configuration // 声明这是一个Spring配置类
@EnableConfigurationProperties(SwaggerProperties.class) // 启用SwaggerProperties类，使其能够从application.yml或.properties文件中加载配置
public class SpringDocConfig {

    private final SwaggerProperties swaggerProperties; // 用于存储从配置文件加载的Swagger属性

    /**
     * 构造函数，通过依赖注入获取SwaggerProperties的实例。
     * @param swaggerProperties 从配置文件中读取的Swagger相关配置
     */
    @Autowired // Spring会自动注入SwaggerProperties的Bean
    public SpringDocConfig(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    /**
     * 创建并配置OpenAPI Bean，这是SpringDoc的核心对象，代表了整个API文档。
     * @return 配置好的OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenApi() {
        OpenAPI openApi = new OpenAPI()
                .info(apiInfo()); // 设置API的基本信息，如标题、描述、版本等

        // 如果在配置文件中启用了安全认证
        if (swaggerProperties.isEnableSecurity()) {
            // 配置安全组件，添加一个名为"Authorization"的安全方案
            openApi.components(new Components()
                    .addSecuritySchemes("Authorization", securityScheme())); // "Authorization"是安全方案的名称，需要与下面addSecurityItem中的名称一致

            // 添加全局的安全需求，意味着所有API默认都需要这个"Authorization"安全方案
            // 这会在Swagger UI的每个接口右上角显示一个小锁图标，并提供全局的Authorize按钮
            openApi.addSecurityItem(new SecurityRequirement().addList("Authorization"));
        }

        return openApi;
    }

    /**
     * 创建API的基本信息对象 (Info)。
     * 这些信息会显示在Swagger UI的顶部。
     * @return 包含API元数据的Info对象
     */
    private Info apiInfo() {
        return new Info()
                .title(swaggerProperties.getTitle()) // API标题，从配置文件读取
                .description(swaggerProperties.getDescription()) // API描述，从配置文件读取
                .version(swaggerProperties.getVersion()) // API版本，从配置文件读取
                .contact(new Contact() // 配置联系人信息
                        .name(swaggerProperties.getContactName()) // 联系人姓名，从配置文件读取
                        .url(swaggerProperties.getContactUrl())   // 联系人URL，从配置文件读取
                        .email(swaggerProperties.getContactEmail())); // 联系人邮箱，从配置文件读取
    }

    /**
     * 创建安全方案 (SecurityScheme) 对象。
     * 定义了API如何进行认证。
     * @return 配置好的SecurityScheme对象
     */
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // 安全方案类型：API Key
                .name("Authorization"); // API Key的名称，指定密钥将通过名为 "Authorization" 的请求头传递
        // 也可以更明确地指定 .in(SecurityScheme.In.HEADER)
    }

    /**
     * 创建并配置API分组 (GroupedOpenApi) Bean。
     * 用于指定SpringDoc应扫描哪些包来生成API文档。
     * @return 配置好的GroupedOpenApi对象
     */
    @Bean
    public GroupedOpenApi apiGroup() {
        // 从配置文件获取要扫描的API基础包路径
        String basePackage = swaggerProperties.getApiBasePackage();

        // 开始构建API分组，默认组名为 "default"
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group("default"); // 定义组名

        // 如果配置文件中指定了基础包路径
        if (basePackage != null && !basePackage.isEmpty()) {
            builder.packagesToScan(basePackage); // 设置要扫描的包路径，SpringDoc将在此路径下查找API控制器
        } else {
            // 如果未指定基础包路径，打印警告信息。
            // 实际项目中建议使用日志框架 (如SLF4J + Logback) 输出警告。
            // LOGGER.warn("Warning: 'swagger.apiBasePackage' is not set. SpringDoc will use its default package scanning.");
            System.out.println("Warning: 'swagger.apiBasePackage' is not set. SpringDoc will use its default package scanning.");
        }

        return builder.build(); // 构建并返回GroupedOpenApi对象
    }
}