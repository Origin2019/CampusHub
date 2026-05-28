package com.campushub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 全局配置类.
 * <p>
 * 主要职责：配置 CORS（跨域资源共享）策略，
 *
 *
 * 允许前端开发服务器（如 Vite localhost:5173）跨域调用后端 API。
 * </p>
 * <p>
 * 原理：当浏览器发起跨域请求时，先发送一个 HTTP OPTIONS 预检请求，
 * 后端返回允许的来源、方法、Header 列表，
 * 浏览器验证通过后才发送真正的 GET/POST 请求。
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注册 CORS 映射规则.
     * <p>
     * 针对所有 /v1/** 路径（即全部业务 API）：
     * <ul>
     *   <li>{@code allowedOriginPatterns("*")} — 允许任意来源访问（开发阶段），
     *       生产环境应限制为前端部署的域名</li>
     *   <li>{@code allowedMethods} — 允许的 HTTP 方法包括 GET、POST、PUT、DELETE、OPTIONS</li>
     *   <li>{@code allowedHeaders("*")} — 允许携带任意请求头（包括 Authorization Token）</li>
     *   <li>{@code allowCredentials(true)} — 允许携带 Cookie / Authorization 等凭据</li>
     * </ul>
     * </p>
     *
     * @param registry Spring MVC 提供的 CORS 注册中心，通过链式调用添加规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
