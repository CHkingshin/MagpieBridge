package cn.kingshin.rediscache.service;

import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {


    Result StrqueryTypeList();

    Result getTypeList();
}
