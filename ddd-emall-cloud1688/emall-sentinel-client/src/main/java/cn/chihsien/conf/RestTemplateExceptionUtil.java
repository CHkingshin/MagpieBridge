package cn.chihsien.conf;

import cn.chihsien.vo.JwtToken;
import com.alibaba.cloud.sentinel.rest.SentinelClientHttpResponse;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

/**
 * <h1>RestTemplate 在限流或异常时的兜底方法</h1>
 *
 * @author KingShin
 */
@Slf4j
public class RestTemplateExceptionUtil {

    /**
     * <h2>限流后的处理方法 必须是静态类型的</h2>
     */
    public static SentinelClientHttpResponse handleBlock(HttpRequest request,
                                                         byte[] body,
                                                         ClientHttpRequestExecution execution,
                                                         BlockException ex) {
        log.error("Handle RestTemplate Block Exception: [{}], [{}]",
                request.getURI().getPath(), ex.getClass().getCanonicalName());
        return new SentinelClientHttpResponse(
                JSON.toJSONString(new JwtToken("cn-chihsien-block"))
        );
    }

    /**
     * <h2>异常降级之后的处理方法</h2>
     */
    public static SentinelClientHttpResponse handleFallback(HttpRequest request,
                                                            byte[] body,
                                                            ClientHttpRequestExecution execution,
                                                            BlockException ex) {
        log.error("Handle RestTemplate Fallback Exception: [{}], [{}]",
                request.getURI().getPath(), ex.getClass().getCanonicalName());
        return new SentinelClientHttpResponse(
                JSON.toJSONString(new JwtToken("cn-chihsien-block"))
        );
    }
}
