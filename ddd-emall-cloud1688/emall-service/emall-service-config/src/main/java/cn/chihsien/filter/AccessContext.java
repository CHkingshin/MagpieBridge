package cn.chihsien.filter;


import cn.chihsien.vo.LoginUserInfo;

/**
 * <h1>使用 ThreadLocal 去单独存储每一个线程携带的 LoginUserInfo 信息（每一个请求的上下文信息）</h1>
 * 要及时的清理我们保存到 ThreadLocal 中的用户信息:
 * 1. 保证没有资源泄露
 * 2. 保证线程在重用时, 不会出现数据混乱
 *
 * @author KingShin
 */
public class AccessContext {

    private static final ThreadLocal<LoginUserInfo> loginUserInfo = new ThreadLocal<>();

    public static LoginUserInfo getLoginUserInfo() {
        return loginUserInfo.get();
    }

    public static void setLoginUserInfo(LoginUserInfo loginUserInfo_) {
        loginUserInfo.set(loginUserInfo_);
    }
    //一定要清理
    public static void clearLoginUserInfo() {
        loginUserInfo.remove();
    }
}
