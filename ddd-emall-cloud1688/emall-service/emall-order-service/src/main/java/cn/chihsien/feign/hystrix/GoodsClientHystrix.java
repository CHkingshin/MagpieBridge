package cn.chihsien.feign.hystrix;

import cn.chihsien.common.TableId;
import cn.chihsien.feign.SecuredGoodsClient;
import cn.chihsien.goods.SimpleGoodsInfo;
import cn.chihsien.vo.CommonResponse;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 商品服务熔断降级兜底
 *
 * @author KingShin
 */
@Slf4j
@Component
public class GoodsClientHystrix implements SecuredGoodsClient {

    @Override
    public CommonResponse<List<SimpleGoodsInfo>> getSimpleGoodsInfoByTableId(
            TableId tableId) {

        log.error("[goods client feign request error in order service] get simple goods" +
                "error: [{}]", JSON.toJSONString(tableId));
        return new CommonResponse<>(
                -1,
                "[goods client feign request error in order service]",
                Collections.emptyList()//返回一个空list 防止空指针
        );
    }
}
