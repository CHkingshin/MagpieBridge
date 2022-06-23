package cn.chihsien;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @describe
 * @auther KingShin
 */
@EnableDiscoveryClient
@SpringBootApplication
public class MainApp8401{
    public static void main(String[] args) {
        SpringApplication.run(MainApp8401.class, args);
    }
}
