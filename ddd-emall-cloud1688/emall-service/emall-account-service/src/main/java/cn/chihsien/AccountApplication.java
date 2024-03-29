package cn.chihsien;

import cn.chihsien.conf.DataSourceProxyAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * <h1>用户账户微服务启动入口</h1>
 * 127.0.0.1:18003/emall-account-service/swagger-ui.html  原生的
 * 127.0.0.1:18003/emall-account-service/doc.html   带美化的
 *
 * @author KingShin
 */
@EnableJpaAuditing
@SpringBootApplication
@EnableDiscoveryClient
@Import(DataSourceProxyAutoConfiguration.class)
public class AccountApplication {

    public static void main(String[] args) {

        SpringApplication.run(AccountApplication.class, args);
    }
}
