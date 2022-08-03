package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Shop;
import cn.kingshin.rediscache.mapper.ShopMapper;
import cn.kingshin.rediscache.service.IShopService;
import cn.kingshin.rediscache.utils.BloomFilterUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.kingshin.rediscache.utils.RedisConstants.*;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author KingShin
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    public static long size =  CACHE_BLOOMFILTER_SIZE;// 预期插入数量
    public static double fpp = CACHE_BLOOMFILTER_FPP; // 误判率



    private RBloomFilter<Long> bloomFilter = null;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private BloomFilterUtil bloomFilterUtil;//此处可以换成hutool的工具类 提供了封装成map的方法


    @PostConstruct // 项目启动的时候执行该方法，也可以理解为在spring容器初始化的时候执行该方法
    public void init() {
        // 启动项目时初始化bloomFilter
        List<Shop> shopList = this.list();
        bloomFilter = bloomFilterUtil.create("idWhiteList", size, fpp);
        shopList.forEach(shop ->
                bloomFilter.add(shop.getId())
        );
    }

    @Override
    public Result queryById(Long id) {

   /*     if (id == null) {
            return null;
        }

        String key = CACHE_SHOP_KEY + id;
        //布隆过滤器查，又再进redis查，没有进DB查，还没有就返回null
        if (bloomFilter.contains(id)) {
            //过滤器里有 再进redsi
            String shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                Shop shop = JSONUtil.toBean(shopJson, Shop.class);
                bloomFilter.add(shop.getId());
                return Result.ok(shop);

            }
            if (shopJson != null) {
                //返回一个错误信息
                return Result.fail("店铺信息不存在！");
            }

            //redis里没有 查数据库
            Shop shop = getById(id);
            //不存在 返回错误
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("没有该店铺！");
            }
            //DB存在  回写
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
            bloomFilter.add(shop.getId());
            //返回
            return Result.ok(shop);

        }*/

        String key = CACHE_SHOP_KEY + id;
        //先查redis
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //命中 取
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //命中空值
        if (shopJson != null){
            //返回一个错误信息
            return Result.fail("店铺不存在");
        }
        //未命中 去DB查
        Shop shop = getById(id);
        //DB无 返回错误
        if (shop == null) {
            //设置null的方法：将null值写入redis
            stringRedisTemplate.opsForValue().set(key, "",CACHE_NULL_TTL, TimeUnit.MINUTES);
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
