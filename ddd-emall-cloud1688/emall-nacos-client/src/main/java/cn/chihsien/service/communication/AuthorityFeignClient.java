package cn.chihsien.service.communication;


import cn.chihsien.service.hystrix.AuthorityFeignClientFallbackFactory;
import cn.chihsien.vo.JwtToken;
import cn.chihsien.vo.UsernameAndPassword;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <h1>与 Authority 服务通信的 Feign Client 接口定义</h1>
 *
 * @author KingShin
 */
@FeignClient(
        contextId = "AuthorityFeignClient", value = "emall-authority-center",
//        fallback = AuthorityFeignClientFallback.class
        fallbackFactory = AuthorityFeignClientFallbackFactory.class
)
public interface AuthorityFeignClient {

    /**
     * <h2>通过 OpenFeign 访问 Authority 获取 Token</h2>
     */
    @RequestMapping(value = "/emall-authority-center/authority/token",
            method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    JwtToken getTokenByFeign(@RequestBody UsernameAndPassword usernameAndPassword);
}
