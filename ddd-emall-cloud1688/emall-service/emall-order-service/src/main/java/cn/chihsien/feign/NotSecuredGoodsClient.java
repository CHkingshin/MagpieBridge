package cn.chihsien.feign;


import cn.chihsien.common.TableId;
import cn.chihsien.goods.DeductGoodsInventory;
import cn.chihsien.goods.SimpleGoodsInfo;
import cn.chihsien.vo.CommonResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * <h1>不安全的商品服务 Feign 接口</h1>
 *
 * @author KingShin
 */
@FeignClient(
        contextId = "NotSecuredGoodsClient",
        value = "emall-goods-service"
)
public interface NotSecuredGoodsClient {

    /**
     * <h2>根据 ids 查询简单的商品信息</h2>
     */
    @RequestMapping(
            value = "/emall-goods-service/goods/deduct-goods-inventory",
            method = RequestMethod.PUT
    )
    CommonResponse<Boolean> deductGoodsInventory(
            @RequestBody List<DeductGoodsInventory> deductGoodsInventories);

    /**
     * <h2>根据 ids 查询简单的商品信息</h2>
     */
    @RequestMapping(
            value = "emall-goods-service/goods/simple-goods-info",
            method = RequestMethod.POST
    )
    CommonResponse<List<SimpleGoodsInfo>> getSimpleGoodsInfoByTableId(
            @RequestBody TableId tableId);
}
