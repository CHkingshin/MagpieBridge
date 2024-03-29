package cn.chihsien;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <h1>监控中心服务器启动入口</h1>
 *
 * @author KingShin
 */
@EnableAdminServer
@SpringBootApplication
public class AdminApplication {

    public static void main(String[] args) {

        SpringApplication.run(AdminApplication.class, args);
    }
}
