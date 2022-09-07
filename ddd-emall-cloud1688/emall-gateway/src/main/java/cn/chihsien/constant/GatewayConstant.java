package cn.chihsien.constant;

/**
 * <h1>网关常量定义</h1>
 *
 * @author KingShin
 */
public class GatewayConstant {

    /** 登录的 uri */
    public static final String LOGIN_URI = "/emall/login";

    /** 注册的 uri */
    public static final String REGISTER_URI = "/emall/register";

    /** 去授权中心拿到登录 token 的 uri 格式化接口 */
    public static final String AUTHORITY_CENTER_TOKEN_URL_FORMAT =
            //协议://主机名:端口号/权限管理微服务id/controller接口定义的路径/获取token的路径
            "http://%s:%s/emall-authority-center/authority/token";

    /** 去授权中心注册并拿到 token 的 uri 格式化接口 */
    public static final String AUTHORITY_CENTER_REGISTER_URL_FORMAT =
            "http://%s:%s/emall-authority-center/authority/register";
}
