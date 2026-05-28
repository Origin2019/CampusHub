package com.campushub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CampusHub 校园互助服务平台 — Spring Boot 启动类.
 * <p>
 * {@code @SpringBootApplication} 是一个组合注解，等价于同时标注
 * {@code @Configuration @EnableAutoConfiguration @ComponentScan}，
 * 启用 Spring Boot 的自动配置、组件扫描和 Java Config 支持。
 * </p>
 * <p>
 * 启动方式：运行本类的 {@code main} 方法，Spring Boot 内嵌的 Tomcat
 * 服务器会在 8080 端口启动（端口配置见 application.yml）。
 * </p>
 */
@SpringBootApplication
public class CampusHubApplication {

    /**
     * 应用程序主入口.
     * 调用 {@link SpringApplication#run(Class, String...)} 完成：
     * <ol>
     *   <li>创建 Spring 应用上下文</li>
     *   <li>自动配置所有 Bean（JPA、Web MVC、校验框架等）</li>
     *   <li>启动内嵌 Tomcat 并部署应用</li>
     * </ol>
     *
     * @param args JVM 启动参数，可通过命令行传入覆盖 application.yml 中的配置
     */
    public static void main(String[] args) {
        SpringApplication.run(CampusHubApplication.class, args);
    }
}
