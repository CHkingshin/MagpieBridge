package cn.chihsien.order.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Configuration;

/**
 * @author KingShin
 * @describe
 *          Ribbon的负载均衡算法：
 *          rest接口的第几次请求数 % 服务器集群总数 = 实际调用服务器位置下标
 */
@Configuration
public class MySelfRule {

    public IRule myRule(){
        return new RandomRule();//定义为随机
    }
}
