package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Shop;
import cn.kingshin.rediscache.entity.ShopType;
import cn.kingshin.rediscache.mapper.ShopTypeMapper;
import cn.kingshin.rediscache.service.IShopTypeService;
import cn.kingshin.rediscache.utils.RedisConstants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ShopServiceImpl shopService;

    /**
     * @description
     * string方式实现
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/4 00:36:09
     */
    @Override
    public Result StrqueryTypeList() {
        //1.从redis中查询店铺类型缓存
        String shopType = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOPTYPE_KEY);
        //2.判断是否为空
        if (StrUtil.isNotBlank(shopType)) {
            //3.存在，直接返回
            List<ShopType> shopTypes = JSONUtil.toList(shopType, ShopType.class);
            return Result.ok(shopTypes);
        }
        //4.不存在，从数据库中查询写入redis
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //5.不存在，返回错误
        if (shopTypes == null) {
            return Result.fail("分类不存在");
        }
        //6.存在，写入redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOPTYPE_KEY,JSONUtil.toJsonStr(shopTypes));
        //7.返回
        return Result.ok(shopTypes);
    }
    /**
     * @description
     * list方法实现
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/4 00:38:05
     */
    @Override
    public Result getTypeList() {
        //1.查询redis里与shopType:id:* 一样的key
        Set<String> keys = stringRedisTemplate.keys("shopType:id:*");
        //2.如果keys不为空 命中 返回
        if (!keys.isEmpty()) {
            List<ShopType> shopTypes = keys.stream().map(shop -> {
                // 获取keys中每个shop的键值对。
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(shop);
                //将获取到的shop键值对封装进map
                return BeanUtil.fillBeanWithMap(entries, new ShopType(), false);
            }).collect(Collectors.toList());
            //返回
            return Result.ok(shopTypes);
        }
        //3.如果为空 未命中 去DB查
        List<ShopType> typeList = this.query().orderByAsc("sort").list();
        //4.DB也为空 返回错误
        if (typeList.isEmpty()) {
            return Result.fail("非法查询！不存在该分类！");
        }

        //流式太复杂
        typeList.forEach(shopType ->{
                        //将DB取回的元素封装
                        Map<String, Object> shopTypeMap = BeanUtil.beanToMap(shopType,
                        new HashMap<>(),
                        CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((key,value)-> value.toString()));
            //DB有 则遍历后封装写回缓存
            stringRedisTemplate.opsForHash().putAll("shopType:id:"+shopType.getId(),shopTypeMap);
        });

        return Result.ok(typeList);

    }




    /**
     * @description
     * 传统写法
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/4 00:46:13
     */
    private Result m1() {
        String key = "cache:typelist";
        //1.在redis中间查询
        List<String> shopTypeList;
        shopTypeList = stringRedisTemplate.opsForList().range(key,0,-1);
        //2.判断是否缓存中了
        //3.中了返回
        if (shopTypeList != null && !shopTypeList.isEmpty()) {
            List<ShopType> typeList = new ArrayList<>();
            for (String s : shopTypeList) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }
        List<ShopType> typeList = query().orderByAsc("sort").list();
        //5.不存在直接返回错误
        if(typeList.isEmpty()){
            return Result.fail("不存在分类");
        }
        for(ShopType shopType : typeList){
            String s = JSONUtil.toJsonStr(shopType);
            shopTypeList.add(s);
        }
        //6.存在直接添加进缓存
        stringRedisTemplate.opsForList().rightPushAll(key, shopTypeList);
        return Result.ok(typeList);
    }

    /*加载DB里的GEO数据进redis
    * 按key分类 给定标志位
    * */
    void loadShopData(){
        //查询店铺信息
        List<Shop> list = shopService.list();
        //把店铺分组 按照type分组 将typeId一致的放到一个集合
        Map<Long, List<Shop>> shopList = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        //分批完成写入redis
        for (Map.Entry<Long, List<Shop>> entry : shopList.entrySet()) {
            //获取完成写入redis
            Long typeId = entry.getKey();
            String key = "shop:geo:"+typeId;
            //获取同类型的店铺的集合
            List<Shop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(value.size());
            //写入redis GEOADD key 经纬度 member
            for (Shop shop : value) {
                //stringRedisTemplate.opsForGeo().add(key,new Point(shop.getX(),shop.getY()),shop.getId().toString());
                locations.add(new RedisGeoCommands.GeoLocation<>(
                                shop.getId().toString(),
                                new Point(shop.getX(),shop.getY())
                        )
                );

            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }
}
