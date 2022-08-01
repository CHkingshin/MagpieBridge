package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.entity.UserInfo;
import cn.kingshin.rediscache.mapper.UserInfoMapper;
import cn.kingshin.rediscache.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
