package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Shop;
import cn.kingshin.rediscache.mapper.ShopMapper;
import cn.kingshin.rediscache.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static cn.kingshin.rediscache.utils.RedisConstants.CACHE_SHOP_KEY;
import static cn.kingshin.rediscache.utils.RedisConstants.CACHE_SHOP_TTL;


/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryById(Long id) {

        String key = CACHE_SHOP_KEY + id;
        //先查redis
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //命中 取
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //未命中 去DB查
        Shop shop = getById(id);
        //DB无 返回错误
        if (shop == null) {
            return Result.fail("没有该店铺！");
        }
        //DB有 则回写redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null){
            return Result.fail("店铺id不存在！");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}
