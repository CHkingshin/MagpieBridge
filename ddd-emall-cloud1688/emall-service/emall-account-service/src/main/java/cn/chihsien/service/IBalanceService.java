package cn.chihsien.service;


import cn.chihsien.account.BalanceInfo;

/**
 * <h2>用于余额相关的服务接口定义</h2>
 * */
public interface IBalanceService {

    /**
     * <h2>获取当前用户余额信息</h2>
     * ThreadLocal头信息里拿token -->  解析拿到用户信息 --> 去登录的用户对应表里查
     */
    BalanceInfo getCurrentUserBalanceInfo();

    /**
     * <h2>扣减用户余额</h2>
     * @param balanceInfo 代表想要扣减的余额 而不是真实余额 注意实现逻辑
     *                    主要是去拿balance
     */
    BalanceInfo deductBalance(BalanceInfo balanceInfo);
}
