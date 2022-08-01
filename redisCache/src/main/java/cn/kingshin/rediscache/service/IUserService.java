package cn.kingshin.rediscache.service;

import cn.kingshin.rediscache.dto.LoginFormDTO;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;


import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
