package cn.chihsien.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * <h1>通过 nacos 下发动态路由配置, 监听 Nacos 中路由配置变更</h1>
 *  连接到nacos server 读取路由配置信息 ->解析成路由信息需要的对象->通过nacos service增加路由定义 ->配置监听器监听是否有修改
 * @author KingShin
 */
@Slf4j
@Component
@DependsOn({"gatewayConfig"})//因为存在依赖关系 所以先初始化gatewayConfig再初始化该类
public class DynamicRouteServiceImplByNacos {

    /**
     * Nacos 配置服务
     */
    private ConfigService configService;
    private final DynamicRouteServiceImpl dynamicRouteService;

    public DynamicRouteServiceImplByNacos(DynamicRouteServiceImpl dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }

    /**
     * <h2>@PostConstruct保证 Bean 在容器中构造完成之后会执行 init 方法</h2>
     */
    @PostConstruct
    public void init() {

        log.info("gateway route init....");

        try {
            // 初始化 Nacos 配置客户端
            configService = initConfigService();
            if (null == configService) {
                log.error("init config service fail");
                return;//初始化失败意味着连接不上nacos 直接返回
            }

            // 通过 Nacos Config 并指定路由配置路径去获取路由配置
            String configInfo = configService.getConfig(
                    GatewayConfig.NACOS_ROUTE_DATA_ID,//找到指定的dataid
                    GatewayConfig.NACOS_ROUTE_GROUP,//找到指定的组
                    GatewayConfig.DEFAULT_TIMEOUT//超时时间
            );

            log.info("get current gateway config: [{}]", configInfo);
            List<RouteDefinition> definitionList =
                    JSON.parseArray(configInfo, RouteDefinition.class);

            if (CollectionUtils.isNotEmpty(definitionList)) {
                for (RouteDefinition definition : definitionList) {
                    log.info("init gateway config: [{}]", definition.toString());
                    dynamicRouteService.addRouteDefinition(definition);
                }
            }

        } catch (Exception ex) {
            log.error("gateway route init has some error: [{}]", ex.getMessage(), ex);
        }
        // 设置监听器
        dynamicRouteByNacosListener(GatewayConfig.NACOS_ROUTE_DATA_ID,
                GatewayConfig.NACOS_ROUTE_GROUP);
    }

    /**
     * <h2>初始化 Nacos Config</h2>
     */
    private ConfigService initConfigService() {

        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", GatewayConfig.NACOS_SERVER_ADDR);//nacos地址
            properties.setProperty("namespace", GatewayConfig.NACOS_NAMESPACE);//nacos namespace
            return configService = NacosFactory.createConfigService(properties);
        } catch (Exception ex) {
            log.error("init gateway nacos config error: [{}]", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * <h2>监听 Nacos 下发的动态路由配置</h2>
     */
    private void dynamicRouteByNacosListener(String dataId, String group) {

        try {
            // 给 Nacos Config 客户端增加一个监听器
            configService.addListener(dataId, group, new Listener() {

                /**
                 * <h2>自己提供线程池执行操作
                 *  这里不做任何线程池操作 返回Null就行
                 * </h2>
                 * */
                @Override
                public Executor getExecutor() {
                    return null;
                }

                /**
                 * <h2>监听器收到配置更新</h2>
                 * @param configInfo Nacos 中最新的配置定义 也就是每一个JSON
                 * */
                @Override
                public void receiveConfigInfo(String configInfo) {

                    log.info("start to update config: [{}]", configInfo);
                    List<RouteDefinition> definitionList =
                            JSON.parseArray(configInfo, RouteDefinition.class);//获取需要更新的definition
                    log.info("update route: [{}]", definitionList.toString());
                    dynamicRouteService.updateList(definitionList);
                }
            });
        } catch (NacosException ex) {
            log.error("dynamic update gateway config error: [{}]", ex.getMessage(), ex);
        }
    }
}
