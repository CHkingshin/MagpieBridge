package cn.chihsien.service.impl;

import cn.chihsien.common.TableId;
import cn.chihsien.constant.GoodsConstant;
import cn.chihsien.dao.EcommerceGoodsDao;
import cn.chihsien.entity.EcommerceGoods;
import cn.chihsien.goods.DeductGoodsInventory;
import cn.chihsien.goods.GoodsInfo;
import cn.chihsien.goods.SimpleGoodsInfo;
import cn.chihsien.service.IGoodsService;
import cn.chihsien.vo.PageSimpleGoodsInfo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h1>商品微服务相关服务功能实现</h1>
 *
 * @author KingShin
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class GoodsServiceImpl implements IGoodsService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EcommerceGoodsDao ecommerceGoodsDao;

    /**
     * @description 按表ID获取商品信息
     * @param tableId
     * @return java.util.List<cn.chihsien.goods.GoodsInfo>
     * @author KingShin
     */
    @Override
    public List<GoodsInfo> getGoodsInfoByTableId(TableId tableId) {

        // 详细的商品信息, 不能从 redis cache 中去拿
        List<Long> ids = tableId.getIds().stream()
                .map(TableId.Id::getId)
                .collect(Collectors.toList());
        log.info("get goods info by ids: [{}]", JSON.toJSONString(ids));

        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(
                ecommerceGoodsDao.findAllById(ids)
        );

        return ecommerceGoods.stream()
                .map(EcommerceGoods::toGoodsInfo).collect(Collectors.toList());
    }
    /**
     * @description 分页获取简单商品信息
     * @param page
     * @return cn.chihsien.vo.PageSimpleGoodsInfo
     * @author KingShin
     */
    @Override
    public PageSimpleGoodsInfo getSimpleGoodsInfoByPage(int page) {

        // 分页不能从 redis cache 中去拿
        if (page <= 1) {
            // 默认是第一页
            page = 1;
        }

        // 这里分页的规则(你可以自由修改): 1页10条数据, 按照 id 倒序排列
        Pageable pageable = PageRequest.of(
                page - 1, 10, Sort.by("id").descending()
        );
        Page<EcommerceGoods> orderPage = ecommerceGoodsDao.findAll(pageable);

        // 是否还有更多页: 总页数是否大于当前给定的页
        boolean hasMore = orderPage.getTotalPages() > page;

        return new PageSimpleGoodsInfo(
                orderPage.getContent().stream()
                        .map(EcommerceGoods::toSimple).collect(Collectors.toList()),
                hasMore
        );
    }
    /**
     * @description  按表 ID 获取简单商品信息
     * @param tableId
     * @return java.util.List<cn.chihsien.goods.SimpleGoodsInfo>
     * @author KingShin
     */
    @Override
    public List<SimpleGoodsInfo> getSimpleGoodsInfoByTableId(TableId tableId) {

        // 获取商品的简单信息, 可以从 redis cache 中去拿, 拿不到需要从 DB 中获取并保存到 Redis 里面
        // Redis 中的 KV 都是字符串类型
        List<Object> goodIds = tableId.getIds().stream()
                //id的long类型转为str
                .map(i -> i.getId().toString()).collect(Collectors.toList());

        // FIXME 已修复 如果 cache 中查不到 goodsId 对应的数据, 返回的是 null, [null, null]
        List<Object> cachedSimpleGoodsInfos = stringRedisTemplate.opsForHash()
                .multiGet(GoodsConstant.ECOMMERCE_GOODS_DICT_KEY, goodIds)
                //做非空过滤
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 如果从 Redis 中查到了商品信息, 分两种情况去操作
        if (CollectionUtils.isNotEmpty(cachedSimpleGoodsInfos)) {
            // 1. 如果从缓存中查询出所有需要的 SimpleGoodsInfo 直接返回
            if (cachedSimpleGoodsInfos.size() == goodIds.size()) {
                log.info("get simple goods info by ids (from cache): [{}]",
                        JSON.toJSONString(goodIds));
                return parseCachedGoodsInfo(cachedSimpleGoodsInfos);
            } else {
                // 2. 一半从数据表中获取 (right), 一半从 redis cache 中获取 (left)
                List<SimpleGoodsInfo> left = parseCachedGoodsInfo(cachedSimpleGoodsInfos);
                // subtract ---> 取差集: 传递进来的参数 - 缓存中查到的 = 缓存中没有的
                Collection<Long> subtractIds = CollectionUtils.subtract(
                        //传进来的参数转化为long
                        goodIds.stream()
                                .map(g -> Long.valueOf(g.toString())).collect(Collectors.toList()),
                        //redis里已经查到的
                        left.stream()
                                .map(SimpleGoodsInfo::getId).collect(Collectors.toList())
                );
                // 缓存中没有的, 查询数据表并缓存
                List<SimpleGoodsInfo> right = queryGoodsFromDBAndCacheToRedis(
                        new TableId(subtractIds.stream().map(TableId.Id::new)
                                .collect(Collectors.toList()))
                );
                // 合并 left 和 right 并返回
                log.info("get simple goods info by ids (from db and cache): [{}]",
                        JSON.toJSONString(subtractIds));
                return new ArrayList<>(CollectionUtils.union(left, right));
            }
        } else {
            // 从 redis 里面什么都没有查到 --> 都去DB查然后缓存到redis
            return queryGoodsFromDBAndCacheToRedis(tableId);
        }
    }

    /**
     * <h2>将缓存中的数据反序列化成 Java Pojo(SimpleGoodsInfo) 对象</h2>
     */
    private List<SimpleGoodsInfo> parseCachedGoodsInfo(List<Object> cachedSimpleGoodsInfo) {

        return cachedSimpleGoodsInfo.stream()
                .map(s -> JSON.parseObject(s.toString(), SimpleGoodsInfo.class))
                .collect(Collectors.toList());
    }

    /**
     * <h2>从数据表中查询数据, 并缓存到 Redis 中</h2>
     */
    private List<SimpleGoodsInfo> queryGoodsFromDBAndCacheToRedis(TableId tableId) {

        // 从数据表中查询数据并做转换
        List<Long> ids = tableId.getIds().stream()
                .map(TableId.Id::getId).collect(Collectors.toList());
        log.info("get simple goods info by ids (from db): [{}]",
                JSON.toJSONString(ids));
        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(
                ecommerceGoodsDao.findAllById(ids)
        );
        List<SimpleGoodsInfo> result = ecommerceGoods.stream()
                .map(EcommerceGoods::toSimple).collect(Collectors.toList());
        // 将结果缓存, 下一次可以直接从 redis cache 中查询
        log.info("cache goods info: [{}]", JSON.toJSONString(ids));
        //将SimpleGoodsInfo转为Map对象保存进redis
        Map<String, String> id2JsonObject = new HashMap<>(result.size());
        result.forEach(g -> id2JsonObject.put(
                g.getId().toString(), JSON.toJSONString(g)
        ));
        // 保存到 Redis 中
        stringRedisTemplate.opsForHash().putAll(
                GoodsConstant.ECOMMERCE_GOODS_DICT_KEY, id2JsonObject);
        return result;
    }
    /**
     * @description 扣减商品库存
     * @param deductGoodsInventories
     * @return java.lang.Boolean
     * @author KingShin
     */
    @Override
    public Boolean deductGoodsInventory(List<DeductGoodsInventory> deductGoodsInventories) {

        // 检验下参数是否合法
        deductGoodsInventories.forEach(d -> {
            if (d.getCount() <= 0) {
                throw new RuntimeException("purchase goods count need > 0");
            }
        });

        List<EcommerceGoods> ecommerceGoods = IterableUtils.toList(
                ecommerceGoodsDao.findAllById(
                        deductGoodsInventories.stream()
                                .map(DeductGoodsInventory::getGoodsId)
                                .collect(Collectors.toList())
                )
        );
        // 根据传递的 goodsIds 查询不到商品对象, 抛异常
        if (CollectionUtils.isEmpty(ecommerceGoods)) {
            throw new RuntimeException("can not found any goods by request");
        }
        // 查询出来的商品数量与传递的不一致, 抛异常
        if (ecommerceGoods.size() != deductGoodsInventories.size()) {
            throw new RuntimeException("request is not valid");
        }
        // goodsId -> DeductGoodsInventory
        Map<Long, DeductGoodsInventory> goodsId2Inventory =
                deductGoodsInventories.stream().collect(
                        Collectors.toMap(DeductGoodsInventory::getGoodsId,//获取GoodsId
                                Function.identity())//获取当前对象本身
                );

        // 循环检查是不是可以扣减库存, 再去扣减库存
        ecommerceGoods.forEach(g -> {
            Long currentInventory = g.getInventory();
            //需要扣减的库存
            Integer needDeductInventory = goodsId2Inventory.get(g.getId()).getCount();
            if (currentInventory < needDeductInventory) {
                log.error("goods inventory is not enough: [{}], [{}]",
                        currentInventory, needDeductInventory);
                throw new RuntimeException("goods inventory is not enough: " + g.getId());
            }
            // 扣减库存
            g.setInventory(currentInventory - needDeductInventory);
            log.info("deduct goods inventory: [{}], [{}], [{}]", g.getId(),
                    currentInventory, g.getInventory());
        });

        ecommerceGoodsDao.saveAll(ecommerceGoods);
        log.info("deduct goods inventory done");

        return true;//执行错误抛异常 否则就返回成功
    }
}
