package cn.chihsien.service;

import cn.chihsien.account.AddressInfo;
import cn.chihsien.common.TableId;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * <h1>用户地址相关服务功能测试</h1>
 * */
@Slf4j
public class AddressServiceTest extends BaseTest {

    @Autowired
    private IAddressService addressService;

    /**
     * <h2>测试创建用户地址信息</h2>
     * */
    @Test
    public void testCreateAddressInfo() {

        AddressInfo.AddressItem addressItem = new AddressInfo.AddressItem();
        addressItem.setUsername("刘德华");
        addressItem.setPhone("18888888888");
        addressItem.setProvince("上海市");
        addressItem.setCity("上海市");
        addressItem.setAddressDetail("陆家嘴");

        log.info("test create address info: [{}]", JSON.toJSONString(
                addressService.createAddressInfo(
                        new AddressInfo(loginUserInfo.getId(),
                                Collections.singletonList(addressItem))
                )
        ));
    }

    /**
     * <h2>测试获取当前登录用户地址信息</h2>
     * */
    @Test
    public void testGetCurrentAddressInfo() {

        log.info("test get current user info: [{}]", JSON.toJSONString(
                addressService.getCurrentAddressInfo()
        ));
    }

    /**
     * <h2>测试通过 id 获取用户地址信息</h2>
     * */
    @Test
    public void testGetAddressInfoById() {

        log.info("test get address info by id: [{}]", JSON.toJSONString(
                addressService.getAddressInfoById(10L)
        ));
    }

    /**
     * <h2>测试通过 TableId 获取用户地址信息</h2>
     * */
    @Test
    public void testGetAddressInfoByTableId() {

        log.info("test get address info by table id: [{}]", JSON.toJSONString(
                addressService.getAddressInfoByTableId(
                        new TableId(Collections.singletonList(new TableId.Id(10L)))
                )
        ));
    }
}
