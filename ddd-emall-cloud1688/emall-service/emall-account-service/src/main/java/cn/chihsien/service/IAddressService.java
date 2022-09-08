package cn.chihsien.service;


import cn.chihsien.account.AddressInfo;
import cn.chihsien.common.TableId;

/**
 * <h1>用户地址相关服务接口定义</h1>
 * */
public interface IAddressService {

    /**
     * <h2>创建用户地址信息</h2>
     * 前端请求到后端服务后创建用户地址 通常会返回数据表记录的id给用户 用户通过该id去查询所创建的用户地址信息
     * 这里返回一个包含ID的包裹类给用户 包裹类扩展性更强
     */
    TableId createAddressInfo(AddressInfo addressInfo);

    /**
     * <h2>获取当前登录的用户地址信息</h2>
     * 这里不用传递任何信息 在LoginUserInfoInterceptor对每一个请求进行拦截解析的时候会从header里读取用户token
     * 通过token解析拿到LoginUserInfo填充到该请求的ThreadLocal的上下文AccessContext里
     * 我们再通过AccessContext拿到当前登录的用户信息 --> 去表里查其登录的地址信息
     * 返回AddressInfo 允许一个用户有多个地址 是一个List
     */
    AddressInfo getCurrentAddressInfo();

    /**
     * <h2>通过 id 获取用户地址信息, id 是 EcommerceAddress 表的主键</h2>
     *
     */
    AddressInfo getAddressInfoById(Long id);

    /**
     * <h2>通过 TableId 获取用户地址信息</h2>
     */
    AddressInfo getAddressInfoByTableId(TableId tableId);
}
