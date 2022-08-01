package cn.kingshin.rediscache.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author KingShin
 */
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
       //判断是否要进行拦截 (treadlocal里有没有用户)
        if(UserHolder.getUser() == null){
            //没有 拦截
            response.setStatus(401);
            return false;
        }
        //有 放行
        return true;
    }

}
