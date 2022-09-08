package cn.chihsien.filter;


import cn.chihsien.constant.CommonConstant;
import cn.chihsien.util.TokenParseUtil;
import cn.chihsien.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>用户身份统一登录拦截</h1>
 * 实现拦截器接口
 * 1.请求进入service之前解析header中的token信息 并填充用户信息到ThreadLocal里 方便功能微服务代码中处理用户行为
 * 2.请求结束之后清理context中的用户信息
 * 3.一些特定的HTTP请求不拦截
 *
 * @author KingShin
 */
@Slf4j
@Component
public class LoginUserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 部分请求不需要带有身份信息, 即白名单
        if (checkWhiteListUrl(request.getRequestURI())) {
            return true;
        }

        // 先尝试从 http header 里面拿到 token
        String token = request.getHeader(CommonConstant.JWT_USER_INFO_KEY);

        LoginUserInfo loginUserInfo = null;
        try {
            loginUserInfo = TokenParseUtil.parseUserInfoFromToken(token);
        } catch (Exception ex) {
            log.error("parse login user info error: [{}]", ex.getMessage(), ex);
        }

        // 如果程序走到这里, 说明 header 中没有 token 信息 实际上不可能走到这一步 网关已经对header中没有token的信息进行了拦截
        if (null == loginUserInfo) {
            throw new RuntimeException("can not parse current login user");
        }

        log.info("set login user info: [{}]", request.getRequestURI());
        // 设置当前请求上下文, 把用户信息填充进去
        AccessContext.setLoginUserInfo(loginUserInfo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        //请求执行之后 返回之前 要对返回结果进行修改的时候 才会用到postHandle 这里对返回结果没有修改需求
    }

    /**
     * <h2>在请求完全结束后调用, 常用于清理资源等工作</h2>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

        if (null != AccessContext.getLoginUserInfo()) {
            //有用户信息则进行清理
            AccessContext.clearLoginUserInfo();
        }
    }

    /**
     * <h2>校验是否是白名单接口</h2>
     * swagger2 接口
     */
    private boolean checkWhiteListUrl(String url) {

        return StringUtils.containsAny(
                url,
                //将swagger2里会用到的关键字加入白名单
                "springfox", "swagger", "v2",
                "webjars", "doc.html"
        );
    }
}
