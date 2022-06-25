package cn.chihsien;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author KingShin
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NacosDiscoveryApplication {
        //https://blog.csdn.net/zhuocailing3390/article/details/123058356
    public static void main(String[] args) {
        SpringApplication.run(NacosDiscoveryApplication.class, args);
    }

}