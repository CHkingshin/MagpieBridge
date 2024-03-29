package cn.chihsien.service.impl;

import cn.chihsien.account.AddressInfo;
import cn.chihsien.common.TableId;
import cn.chihsien.dao.EcommerceAddressDao;
import cn.chihsien.entity.EcommerceAddress;
import cn.chihsien.filter.AccessContext;
import cn.chihsien.service.IAddressService;
import cn.chihsien.vo.LoginUserInfo;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>用户地址相关服务接口实现</h1>
 * */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AddressServiceImpl implements IAddressService {

    private final EcommerceAddressDao addressDao;

    public AddressServiceImpl(EcommerceAddressDao addressDao) {
        this.addressDao = addressDao;
    }

    /**
     * <h2>存储多个地址信息</h2>
     * */
    @Override
    public TableId createAddressInfo(AddressInfo addressInfo) {

        // 不能直接从参数中获取用户的 id 信息
        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        // 将传递的参数转换成实体对象
        List<EcommerceAddress> ecommerceAddresses = addressInfo.getAddressItems().stream()
                .map(a -> EcommerceAddress.to(loginUserInfo.getId(), a))
                .collect(Collectors.toList());

        // 保存到数据表并把返回记录的 id 给调用方
        List<EcommerceAddress> savedRecords = addressDao.saveAll(ecommerceAddresses);
        List<Long> ids = savedRecords.stream()
                .map(EcommerceAddress::getId).collect(Collectors.toList());
        log.info("create address info: [{}], [{}]", loginUserInfo.getId(),
                JSON.toJSONString(ids));

        return new TableId(
                ids.stream().map(TableId.Id::new).collect(Collectors.toList())
        );
    }
    /**
     * @description
     *        获取当前用户的地址信息
     * @return cn.chihsien.account.AddressInfo
     * @author KingShin
     * @date 2022/9/10 00:56:27
     */
    @Override
    public AddressInfo getCurrentAddressInfo() {

        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        // 根据 userId 查询到用户的地址信息, 再实现转换
        List<EcommerceAddress> ecommerceAddresses = addressDao.findAllByUserId(
                loginUserInfo.getId()
        );
        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses.stream()
                .map(EcommerceAddress::toAddressItem)
                .collect(Collectors.toList());

        return new AddressInfo(loginUserInfo.getId(), addressItems);
    }
    /**
     * @description 按 ID 获取地址信息
     * @param id
     * @return cn.chihsien.account.AddressInfo
     * @author KingShin
     * @date 2022/9/10 01:04:18
     */
    @Override
    public AddressInfo getAddressInfoById(Long id) {
        //findById返回的是Optional对象 需要做操作：orElse：存在则返回值 如果不存在值，则返回的值可以为 null
        EcommerceAddress ecommerceAddress = addressDao.findById(id).orElse(null);
        if (null == ecommerceAddress) {
            throw new RuntimeException("address is not exist");
        }

        return new AddressInfo(
                ecommerceAddress.getUserId(),
                Collections.singletonList(ecommerceAddress.toAddressItem())
        );
    }
    /**
     * @description 按表 ID 获取地址信息
     * @param tableId
     * @return cn.chihsien.account.AddressInfo
     * @author KingShin
     * @date 2022/9/10 01:04:31
     */
    @Override
    public AddressInfo getAddressInfoByTableId(TableId tableId) {

        List<Long> ids = tableId.getIds().stream()
                .map(TableId.Id::getId).collect(Collectors.toList());
        log.info("get address info by table id: [{}]", JSON.toJSONString(ids));

        List<EcommerceAddress> ecommerceAddresses = addressDao.findAllById(ids);
        if (CollectionUtils.isEmpty(ecommerceAddresses)) {
            return new AddressInfo(-1L, Collections.emptyList());
        }
        //实体类 --> 值对象
        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses.stream()
                .map(EcommerceAddress::toAddressItem)
                .collect(Collectors.toList());

        return new AddressInfo(
                //第一个参数userId 第二个参数addressItems
                ecommerceAddresses.get(0).getUserId(), addressItems
        );
    }
}
