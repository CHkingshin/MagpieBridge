package cn.chihsien.filter;


import cn.chihsien.constant.GatewayConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <h1>缓存请求 body 的全局过滤器</h1>
 * 基于Spring WebFlux实现 spring5.0响应式web框架 与springmvc不同并不需要servlet api 而且是异步的
 *
 * @author KingShin
 */
@Slf4j
@Component
public class GlobalCacheRequestBodyFilter implements GlobalFilter, Ordered {
    /**
     * @description 基于Spring WebFlux对请求数据进行缓存 方便之后的filter拿取
     * @param exchange
     * @param chain
     * @return reactor.core.publisher.Mono<java.lang.Void>
     * @author KingShin
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //是否是登录或者注册 是则缓存 不是则不管
        boolean isloginOrRegister =
                exchange.getRequest().getURI().getPath().contains(GatewayConstant.LOGIN_URI)
                || exchange.getRequest().getURI().getPath().contains(GatewayConstant.REGISTER_URI);
        //没有请求头  不用管
        if (null == exchange.getRequest().getHeaders().getContentType()
                || !isloginOrRegister) {
            return chain.filter(exchange);
        }
        //开始缓存逻辑
        // DataBufferUtils.join 拿到请求中的数据 --> DataBuffer  flatMap方法将DataBuffer进行展开
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(dataBuffer -> {

            // 确保数据缓冲区不被释放, 必须要 DataBufferUtils.retain
            DataBufferUtils.retain(dataBuffer);
            // defer、just 都是去创建数据源, 得到当前数据的副本
            Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                    //从0开始获取 直到字节的总字节数结束
                    Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount())));
            // 重新包装 ServerHttpRequest, 重写 getBody 方法, 能够返回请求数据
            ServerHttpRequest mutatedRequest =
                    //装饰器模式
                    new ServerHttpRequestDecorator(exchange.getRequest()) {
                        //重写ServerHttpRequestDecorator的getbody方法
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };
            // 将包装之后的 ServerHttpRequest 向下继续传递
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        });
    }
    /**
     * @description
     *        设置优先级 保GlobalCacheRequestBodyFilter在拿到请求数据的过滤器之前执行
     *        +1 保证拿缓存的
     * @return int
     * @author KingShin
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1;
    }
}
