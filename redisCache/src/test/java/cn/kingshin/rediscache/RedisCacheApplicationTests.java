package cn.kingshin.rediscache;

import cn.kingshin.rediscache.entity.Shop;
import cn.kingshin.rediscache.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class RedisCacheApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L,10L);
    }

    @Test
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

/***
 * @description
 *        HLL模拟UV统计
 * @return void
 * @author KingShin
 * @date 2022/8/25 14:57:59
 */
    @Test
    void testHLL(){
        String[] users = new String[1000];
        int j = 0;

        for (int i = 0; i <= 1000000 ; i++) {
            j = i % 1000;
            users[j] = "user_" +i;
            if(j == 999){
                stringRedisTemplate.opsForHyperLogLog().add("hll1",users);
            }
        }
        Long size = stringRedisTemplate.opsForHyperLogLog().size("hll1");
        System.out.println("size="+size);
    }
}
