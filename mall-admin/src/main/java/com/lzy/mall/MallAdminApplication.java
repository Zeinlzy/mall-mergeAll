package com.lzy.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@MapperScan(basePackages = {
        "com.lzy.mall.mapper",
        "com.lzy.mall.dao"
})
@ComponentScan(basePackages = {
        "com.lzy.mall", // 扫描当前模块下的所有组件（可选，SpringBootApplication默认会扫描）
        "com.lzy.mall.common" // 添加扫描 mall-common 模块的根包
})
public class MallAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAdminApplication.class, args);
    }

}
