package cn.chihsien.feign;


import cn.chihsien.account.AddressInfo;
import cn.chihsien.common.TableId;
import cn.chihsien.feign.hystrix.AddressClientHystrix;
import cn.chihsien.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用户账户服务 Feign 接口(安全的)
 *  带兜底方法
 * @author KingShin
 */
@FeignClient(
        contextId = "AddressClient",
        value = "emall-account-service",
        fallback = AddressClientHystrix.class
)
public interface AddressClient {

    /**
     * <h2>根据 id 查询地址信息</h2>
     */
    @RequestMapping(
            value = "/emall-account-service/address/address-info-by-table-id",
            method = RequestMethod.POST
    )
    CommonResponse<AddressInfo> getAddressInfoByTablesId(@RequestBody TableId tableId);
}
