package cn.chihsien.feign;

import cn.chihsien.common.TableId;
import cn.chihsien.feign.hystrix.GoodsClientHystrix;
import cn.chihsien.goods.SimpleGoodsInfo;
import cn.chihsien.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 安全的商品服务 Feign 接口 带兜底方案
 *
 * @author KingShin
 */
@FeignClient(
        contextId = "SecuredGoodsClient",
        value = "emall-goods-service",
        fallback = GoodsClientHystrix.class
)
public interface SecuredGoodsClient {

    /**
     * <h2>根据 ids 查询简单的商品信息</h2>
     */
    @RequestMapping(
            value = "/emall-goods-service/goods/simple-goods-info",
            method = RequestMethod.POST
    )
    CommonResponse<List<SimpleGoodsInfo>> getSimpleGoodsInfoByTableId(
            @RequestBody TableId tableId);
}
