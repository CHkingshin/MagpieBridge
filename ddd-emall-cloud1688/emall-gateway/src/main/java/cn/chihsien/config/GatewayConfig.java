package cn.chihsien.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>配置类, 读取 Nacos 相关的配置项, 用于配置监听器</h1>
 *
 * @author KingShin
 */
@Configuration
public class GatewayConfig {

    /**
     * 读取配置的超时时间
     */
    public static final long DEFAULT_TIMEOUT = 30000;

    /**
     * Nacos 服务器地址
     */
    public static String NACOS_SERVER_ADDR;

    /**
     * 命名空间
     */
    public static String NACOS_NAMESPACE;

    /**
     * data-id
     */
    public static String NACOS_ROUTE_DATA_ID;

    /**
     * 分组 id
     */
    public static String NACOS_ROUTE_GROUP;

    public static String NACOS_USERNAME;

    public static String NACOS_PASSWORD;


    @Value("${spring.cloud.nacos.discovery.server-addr}")
    public void setNacosServerAddr(String nacosServerAddr) {
        NACOS_SERVER_ADDR = nacosServerAddr;
    }

    @Value("${spring.cloud.nacos.discovery.namespace}")
    public void setNacosNamespace(String nacosNamespace) {
        NACOS_NAMESPACE = nacosNamespace;
    }

    @Value("${nacos.gateway.route.config.data-id}")
    public void setNacosRouteDataId(String nacosRouteDataId) {
        NACOS_ROUTE_DATA_ID = nacosRouteDataId;
    }

    @Value("${nacos.gateway.route.config.group}")
    public void setNacosRouteGroup(String nacosRouteGroup) {
        NACOS_ROUTE_GROUP = nacosRouteGroup;
    }

//    @Value("${nacos.gateway.route.config.username}")
//    public void setNacosUsername(String nacosUsername){
//        NACOS_USERNAME = nacosUsername;
//    }
//
//    @Value("${nacos.gateway.route.config.username}")
//    public void setNacosPassword(String nacosPassword){
//        NACOS_PASSWORD = nacosPassword;
//    }
}
