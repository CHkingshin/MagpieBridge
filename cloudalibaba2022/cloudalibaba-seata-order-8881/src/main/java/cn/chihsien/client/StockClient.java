package cn.chihsien.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @describe
 * @auther KingShin
 */
@FeignClient(value= "seata-stock")
public interface StockClient {
    @GetMapping("/stock/decr")
    String decrement();
}
