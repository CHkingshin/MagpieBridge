package cn.chihsien.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>代码配置登录请求转发规则</h1>
 *
 * @author KingShin
 */
@Configuration
public class RouteLocatorConfig {

    /**
     * <h2>使用代码定义路由规则, 在网关层面拦截下登录和注册接口</h2>
     */
    @Bean
    public RouteLocator loginRouteLocator(RouteLocatorBuilder builder) {

        // 手动定义 Gateway 路由规则 需要指定 id、path 和 uri
        return builder.routes()
                .route(
                        "emall_authority",//全局唯一即可
                        r -> r.path(
                                "/imooc/emall/login",//拦截登录请求
                                "/imooc/emall/register"//拦截注册请求
                        ).uri("http://localhost:19001/")//将上面设置的请求向网关进行转发
                ).build();
    }
}
