package cn.chihsien.service.impl;


import cn.chihsien.account.BalanceInfo;
import cn.chihsien.dao.EcommerceBalanceDao;
import cn.chihsien.entity.EcommerceBalance;
import cn.chihsien.filter.AccessContext;
import cn.chihsien.service.IBalanceService;
import cn.chihsien.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <h1>用于余额相关服务接口实现</h1>
 *
 * @author KingShin
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceServiceImpl implements IBalanceService {

    private final EcommerceBalanceDao balanceDao;

    public BalanceServiceImpl(EcommerceBalanceDao balanceDao) {
        this.balanceDao = balanceDao;
    }
    /**
     * @description
     *        获取当前用户余额信息 如果没有 则创建一个新的余额记录进表 余额数为0
     * @return cn.chihsien.account.BalanceInfo
     * @author KingShin
     */
    @Override
    public BalanceInfo getCurrentUserBalanceInfo() {

        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();
        BalanceInfo balanceInfo = new BalanceInfo(
                //第一次登录
                loginUserInfo.getId(), 0L
        );

        EcommerceBalance ecommerceBalance =
                balanceDao.findByUserId(loginUserInfo.getId());

        if (null != ecommerceBalance) {
            balanceInfo.setBalance(ecommerceBalance.getBalance());
        } else {
            // 如果还没有用户余额记录, 这里创建出来，余额设定为0即可
            EcommerceBalance newBalance = new EcommerceBalance();
            newBalance.setUserId(loginUserInfo.getId());
            newBalance.setBalance(0L);
            log.info("init user balance record: [{}]",
                    balanceDao.save(newBalance).getId());
        }

        return balanceInfo;
    }
    /**
     * @description  扣除当前用户余额
     * @param balanceInfo
     * @return cn.chihsien.account.BalanceInfo
     * @author KingShin
     * @date 2022/9/10
     */
    @Override
    public BalanceInfo deductBalance(BalanceInfo balanceInfo) {

        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        // 扣减用户余额的一个基本原则: 扣减额 <= 当前用户余额 实际业务中应该判断是否是同一个用户
        EcommerceBalance ecommerceBalance =
                balanceDao.findByUserId(loginUserInfo.getId());
        //当前表余额不为空 且表里的余额-需要扣减的钱 < 0 则抛错
        if (null == ecommerceBalance
                || ecommerceBalance.getBalance() - balanceInfo.getBalance() < 0
        ) {
            throw new RuntimeException("user balance is not enough!");
        }

        Long sourceBalance = ecommerceBalance.getBalance();
        ecommerceBalance.setBalance(ecommerceBalance.getBalance() - balanceInfo.getBalance());
        log.info("deduct balance: [{}], [{}], [{}]",
                //ecommerceBalance  原来的余额   扣减的余额
                balanceDao.save(ecommerceBalance).getId(), sourceBalance,
                balanceInfo.getBalance());

        return new BalanceInfo(
                ecommerceBalance.getUserId(),
                ecommerceBalance.getBalance()
        );
    }
}
