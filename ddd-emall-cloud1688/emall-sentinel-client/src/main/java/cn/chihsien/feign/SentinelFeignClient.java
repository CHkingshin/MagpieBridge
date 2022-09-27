package cn.chihsien.feign;


import cn.chihsien.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <h1>通过 Sentinel 对 OpenFeign 实现熔断降级</h1>
 *
 * @author KingShin
 */
@FeignClient(
        value = "ddd-emall",//模拟一个不存在的  会报错
        fallback = SentinelFeignClientFallback.class
)
public interface SentinelFeignClient {

    @RequestMapping(value = "chihsien", method = RequestMethod.GET)
    CommonResponse<String> getResultByFeign(@RequestParam Integer code);
}
