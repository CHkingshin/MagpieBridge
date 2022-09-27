package cn.chihsien.service.impl;

import cn.chihsien.account.AddressInfo;
import cn.chihsien.account.BalanceInfo;
import cn.chihsien.common.TableId;
import cn.chihsien.dao.EcommerceOrderDao;
import cn.chihsien.entity.EcommerceOrder;
import cn.chihsien.feign.AddressClient;
import cn.chihsien.feign.NotSecuredBalanceClient;
import cn.chihsien.feign.NotSecuredGoodsClient;
import cn.chihsien.feign.SecuredGoodsClient;
import cn.chihsien.filter.AccessContext;
import cn.chihsien.goods.DeductGoodsInventory;
import cn.chihsien.goods.SimpleGoodsInfo;
import cn.chihsien.order.LogisticsMessage;
import cn.chihsien.order.OrderInfo;
import cn.chihsien.service.IOrderService;
import cn.chihsien.source.LogisticsSource;
import cn.chihsien.vo.PageSimpleOrderDetail;
import com.alibaba.fastjson.JSON;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <h1>订单相关服务接口实现</h1>
 *
 * @author KingShin
 */
@Slf4j
@Service
@EnableBinding(LogisticsSource.class)
public class OrderServiceImpl implements IOrderService {

    /**
     * 表的 dao 接口
     */
    private final EcommerceOrderDao orderDao;

    /**
     * Feign 客户端
     */
    private final AddressClient addressClient;
    private final SecuredGoodsClient securedGoodsClient;
    private final NotSecuredGoodsClient notSecuredGoodsClient;
    private final NotSecuredBalanceClient notSecuredBalanceClient;

    /**
     * SpringCloud Stream 的发射器
     */
    private final LogisticsSource logisticsSource;

    public OrderServiceImpl(EcommerceOrderDao orderDao,
                            AddressClient addressClient,
                            SecuredGoodsClient securedGoodsClient,
                            NotSecuredGoodsClient notSecuredGoodsClient,
                            NotSecuredBalanceClient notSecuredBalanceClient,
                            LogisticsSource logisticsSource) {
        this.orderDao = orderDao;
        this.addressClient = addressClient;
        this.securedGoodsClient = securedGoodsClient;
        this.notSecuredGoodsClient = notSecuredGoodsClient;
        this.notSecuredBalanceClient = notSecuredBalanceClient;
        this.logisticsSource = logisticsSource;
    }

    /**
     * <h2>创建订单: 这里会涉及到分布式事务</h2>
     * 创建订单会涉及到多个步骤和校验, 当不满足情况时直接抛出异常;
     * 1. 校验请求对象是否合法
     * 2. 创建订单
     * 3. 扣减商品库存
     * 4. 扣减用户余额
     * 5. 发送订单物流消息 SpringCloud Stream + Kafka
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public TableId createOrder(OrderInfo orderInfo) {

        // 获取地址信息
        AddressInfo addressInfo = addressClient.getAddressInfoByTablesId(
                new TableId(Collections.singletonList(
                        //getAddressInfoByTablesId返回的是CommonResponse 响应的数据则是里面的Data
                        new TableId.Id(orderInfo.getUserAddress())))).getData();

        // 1. 校验请求对象是否合法(商品信息不需要校验, 扣减库存会做校验)
        if (CollectionUtils.isEmpty(addressInfo.getAddressItems())) {
            throw new RuntimeException("user address is not exist: "
                    + orderInfo.getUserAddress());
        }

        // 2. 创建订单
        EcommerceOrder newOrder = orderDao.save(
                new EcommerceOrder(
                        //三个包装起来的 用户id 用户地址id 订单详情 就可以完成订单记录的创建
                        AccessContext.getLoginUserInfo().getId(),
                        orderInfo.getUserAddress(),
                        JSON.toJSONString(orderInfo.getOrderItems())
                )
        );
        log.info("create order success: [{}], [{}]",
                //打印创建订单的用户id
                AccessContext.getLoginUserInfo().getId(), newOrder.getId());//创建的订单id
        // 3. 扣减商品库存
        if (
                !notSecuredGoodsClient.deductGoodsInventory(
                        orderInfo.getOrderItems()
                                .stream()
                                .map(OrderInfo.OrderItem::toDeductGoodsInventory)
                                .collect(Collectors.toList())
                //    CommonResponse<Boolean> deductGoodsInventory
                ).getData()

        ) {
            throw new RuntimeException("deduct goods inventory failure");
        }

        // 4. 扣减用户账户余额
        // 4.1 获取商品信息-->封装为MAP-->计算总价格
        List<SimpleGoodsInfo> goodsInfos = notSecuredGoodsClient.getSimpleGoodsInfoByTableId(
                new TableId(
                        orderInfo.getOrderItems()
                                .stream()
                                .map(o -> new TableId.Id(o.getGoodsId()))
                                .collect(Collectors.toList())
                )
        ).getData();
        Map<Long, SimpleGoodsInfo> goodsId2GoodsInfo = goodsInfos.stream()
                //K - V : 当前对象id - 返回的当前对象
                .collect(Collectors.toMap(SimpleGoodsInfo::getId, Function.identity()));
        long balance = 0;
        for (OrderInfo.OrderItem orderItem : orderInfo.getOrderItems()) {
            //获取单个价格*数量 +=：每次循环就是一个累加
            balance += (long) goodsId2GoodsInfo.get(orderItem.getGoodsId()).getPrice()
                    * orderItem.getCount();
        }
        //断言
        assert balance > 0;

        // 4.2 填写总价格, 扣减账户余额
        BalanceInfo balanceInfo = notSecuredBalanceClient.deductBalance(
                //扣减当前用户的余额
                new BalanceInfo(AccessContext.getLoginUserInfo().getId(), balance)
        ).getData();
        if (null == balanceInfo) {
            throw new RuntimeException("deduct user balance failure");
        }
        log.info("deduct user balance: [{}], [{}]", newOrder.getId(),
                JSON.toJSONString(balanceInfo));

        // 5. 发送订单物流消息 SpringCloud Stream + Kafka
        LogisticsMessage logisticsMessage = new LogisticsMessage(
                AccessContext.getLoginUserInfo().getId(),
                newOrder.getId(),
                orderInfo.getUserAddress(),
                null    // 没有备注信息
        );
        if (!logisticsSource.logisticsOutput().send(
                MessageBuilder.withPayload(JSON.toJSONString(logisticsMessage)).build()
        )) {
            throw new RuntimeException("send logistics message failure");
        }
        log.info("send create order message to kafka with stream: [{}]",
                JSON.toJSONString(logisticsMessage));

        // 返回订单 id
        return new TableId(Collections.singletonList(new TableId.Id(newOrder.getId())));
    }
    /**
     * @description
     * @param page
     * 分页简单订单详情
     */
    @Override
    public PageSimpleOrderDetail getSimpleOrderDetailByPage(int page) {

        if (page <= 0) {
            page = 1;   // 默认是第一页
        }

        // 这里分页的规则是: 1页10条数据, 按照 id 倒序排列
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by("id").descending());
        Page<EcommerceOrder> orderPage = orderDao.findAllByUserId(
                AccessContext.getLoginUserInfo().getId(), pageable
        );
        List<EcommerceOrder> orders = orderPage.getContent();

        // 如果是空, 直接返回空数组
        if (CollectionUtils.isEmpty(orders)) {
            return new PageSimpleOrderDetail(Collections.emptyList(), false);
        }

        // 获取当前订单中所有的 goodsId, 这个 set 不可能为空或者是 null, 否则, 代码一定有 bug
        Set<Long> goodsIdsInOrders = new HashSet<>();
        orders.forEach(o -> {
            List<DeductGoodsInventory> goodsAndCount = JSON.parseArray(
                    o.getOrderDetail(), DeductGoodsInventory.class
            );
            //获取所有商品id
            goodsIdsInOrders.addAll(goodsAndCount.stream()
                    .map(DeductGoodsInventory::getGoodsId)
                    .collect(Collectors.toSet()));
        });

        assert CollectionUtils.isNotEmpty(goodsIdsInOrders);

        // 是否还有更多页: 总页数是否大于当前给定的页
        boolean hasMore = orderPage.getTotalPages() > page;

        // 获取商品信息 这里只需要获取订单详情 没有做额外的操作 不需要抛出异常给seata做全局异常捕获的回滚操作
        List<SimpleGoodsInfo> goodsInfos = securedGoodsClient.getSimpleGoodsInfoByTableId(
                new TableId(goodsIdsInOrders.stream()
                        .map(TableId.Id::new).collect(Collectors.toList()))
        ).getData();

        // 获取地址信息
        AddressInfo addressInfo = addressClient.getAddressInfoByTablesId(
                new TableId(orders.stream()
                        .map(o -> new TableId.Id(o.getAddressId()))
                        //去重
                        .distinct().collect(Collectors.toList()))
        ).getData();

        // 组装订单中的商品, 地址信息 -> 订单信息
        return new PageSimpleOrderDetail(
                assembleSimpleOrderDetail(orders, goodsInfos, addressInfo),
                hasMore//是否还有更多页
        );
    }

    /**
     * <h2>组装订单详情</h2>
     */
    private List<PageSimpleOrderDetail.SingleOrderItem> assembleSimpleOrderDetail(
            List<EcommerceOrder> orders, List<SimpleGoodsInfo> goodsInfos,
            AddressInfo addressInfo
    ) {
        // goodsId -> SimpleGoodsInfo
        Map<Long, SimpleGoodsInfo> id2GoodsInfo = goodsInfos.stream()
                .collect(Collectors.toMap(SimpleGoodsInfo::getId, Function.identity()));
        // addressId -> AddressInfo.AddressItem
        Map<Long, AddressInfo.AddressItem> id2AddressItem = addressInfo.getAddressItems()
                .stream().collect(
                        Collectors.toMap(AddressInfo.AddressItem::getId, Function.identity())
                );

        List<PageSimpleOrderDetail.SingleOrderItem> result = new ArrayList<>(orders.size());
        //根据每个订单来构造SingleOrderItem
        orders.forEach(o -> {

            PageSimpleOrderDetail.SingleOrderItem orderItem =
                    new PageSimpleOrderDetail.SingleOrderItem();
            //对循环的每一个订单对象进行填充
            orderItem.setId(o.getId());
            orderItem.setUserAddress(id2AddressItem.getOrDefault(o.getAddressId(),
                    //如果获取不到 兜底返回一个 将AddressItem 转换成的UserAddress
                    new AddressInfo.AddressItem(-1L)).toUserAddress());
            orderItem.setGoodsItems(buildOrderGoodsItem(o, id2GoodsInfo));

            result.add(orderItem);
        });

        return result;
    }

    /**
     * <h2>构造单个订单中的商品信息</h2>
     */
    private List<PageSimpleOrderDetail.SingleOrderGoodsItem> buildOrderGoodsItem(
            EcommerceOrder order, Map<Long, SimpleGoodsInfo> id2GoodsInfo
    ) {

        List<PageSimpleOrderDetail.SingleOrderGoodsItem> goodsItems = new ArrayList<>();
        List<DeductGoodsInventory> goodsAndCount = JSON.parseArray(
                order.getOrderDetail(), DeductGoodsInventory.class
        );

        goodsAndCount.forEach(gc -> {
            //根据每一个goodsAndCount去构造单个订单中的单项商品信息
            PageSimpleOrderDetail.SingleOrderGoodsItem goodsItem =
                    new PageSimpleOrderDetail.SingleOrderGoodsItem();
            //填充
            goodsItem.setCount(gc.getCount());
            goodsItem.setSimpleGoodsInfo(id2GoodsInfo.getOrDefault(gc.getGoodsId(),
                    //兜底 返回一个无效的商品信息
                    new SimpleGoodsInfo(-1L)));

            goodsItems.add(goodsItem);
        });

        return goodsItems;
    }
}
