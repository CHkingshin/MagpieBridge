package cn.chihsien.mp_demo.service.impl;

import cn.chihsien.mp_demo.dao.User;
import cn.chihsien.mp_demo.mapper.UserMapper;
import cn.chihsien.mp_demo.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @describe
 * @auther KingShin
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

}
