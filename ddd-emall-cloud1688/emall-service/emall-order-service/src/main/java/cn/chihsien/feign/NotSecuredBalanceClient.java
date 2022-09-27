package cn.chihsien.feign;

import cn.chihsien.account.BalanceInfo;
import cn.chihsien.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用户账户服务 Feign 接口
 * 无兜底 不安全 如果给出兜底策略安全落地 则不抛出异常 分布式事务无法捕获异常以实现回滚
 * @author KingShin
 */
@FeignClient(
        contextId = "NotSecuredBalanceClient",
        value = "emall-account-service"
)
public interface NotSecuredBalanceClient {
    /**
     * RPC调用余额扣减方法
    */
    @RequestMapping(
            value = "/emall-account-service/balance/deduct-balance",
            method = RequestMethod.PUT
    )
    CommonResponse<BalanceInfo> deductBalance(@RequestBody BalanceInfo balanceInfo);
}
