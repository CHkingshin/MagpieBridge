package cn.chihsien.feign;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Feign 调用时, 把 Header 也传递到服务提供方
 *  Feign必要的配置
 * @author KingShin
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * <h2>给 Feign 配置请求拦截器</h2>
     * RequestInterceptor 是我们提供给 open-feign 的请求拦截器, 把 Header 信息传递
     * */
    @Bean
    public RequestInterceptor headerInterceptor() {

        return template -> {
            //springmvc原生操作 获取属性
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (null != attributes) {
                //获取到请求信息
                HttpServletRequest request = attributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (null != headerNames) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String values = request.getHeader(name);
                        // 不能把当前请求的 content-length 传递到下游的服务提供方, 这明显是不对的
                        // 请求可能一直返回不了, 或者是请求响应数据被截断
                        if (!name.equalsIgnoreCase("content-length")) {
                            // 这里的 template 就是 RestTemplate
                            template.header(name, values);
                        }
                    }
                }
            }
        };
    }
}
