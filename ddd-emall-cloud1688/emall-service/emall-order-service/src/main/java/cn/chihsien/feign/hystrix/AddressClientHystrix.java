package cn.chihsien.feign.hystrix;

import cn.chihsien.account.AddressInfo;
import cn.chihsien.common.TableId;
import cn.chihsien.feign.AddressClient;
import cn.chihsien.vo.CommonResponse;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 账户服务熔断降级兜底策略
 *
 * @author KingShin
 */

@Slf4j
@Component
public class AddressClientHystrix implements AddressClient {

    @Override
    public CommonResponse<AddressInfo> getAddressInfoByTablesId(TableId tableId) {

        log.error("[account client feign request error in order service] get address info" +
                "error: [{}]", JSON.toJSONString(tableId));//具体要打印什么异常可以用注解标注factory
        return new CommonResponse<>(
                -1,
                "[account client feign request error in order service]",
                new AddressInfo(-1L, Collections.emptyList())//返回一个无效的AddressInfo对象
        );
    }
}
