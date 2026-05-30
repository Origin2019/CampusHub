package com.campushub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class CampusHubApplication {

    private static final Logger log = LoggerFactory.getLogger(CampusHubApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CampusHubApplication.class, args);
    }

    @Bean
    CommandLineRunner startupBanner(Environment env) {
        return args -> {
            String port = env.getProperty("server.port", "8080");
            String ctx = env.getProperty("server.servlet.context-path", "");
            String base = "http://localhost:" + port + ctx;

            log.info("");
            log.info("============================================================");
            log.info("  CampusHub 启动成功");
            log.info("============================================================");
            log.info("  本地地址 : {}", base);
            log.info("------------------------------------------------------------");
            log.info("  已注册模块:");
            log.info("    /v1/auth/**         用户认证 (注册/登录)");
            log.info("    /v1/tasks/**        需求发布 (列表/详情/发布)");
            log.info("    /v1/categories      分类列表");
            log.info("    /v1/notifications   消息通知");
            log.info("    /v1/verification    短信验证码");
            log.info("    /v1/admin/**        管理后台");
            log.info("------------------------------------------------------------");
            log.info("  测试账号 (密码均为 Test123456):");
            log.info("    学号 20240001  张三(需求方)");
            log.info("    学号 20240002  李四(服务方)");
            log.info("    学号 admin001  管理员");
            log.info("============================================================");
            log.info("");
        };
    }
}
