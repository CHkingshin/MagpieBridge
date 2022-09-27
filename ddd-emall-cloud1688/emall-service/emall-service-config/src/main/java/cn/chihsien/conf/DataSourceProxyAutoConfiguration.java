package cn.chihsien.conf;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * <h1>Seata 所需要的数据源代理配置类</h1>
 *
 * @author KingShin
 */
@Configuration
public class DataSourceProxyAutoConfiguration {

    private final DataSourceProperties dataSourceProperties;

    public DataSourceProxyAutoConfiguration(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    /**
     * <h2>配置数据源代理, 用于 Seata 全局事务回滚</h2>
     *     @Primary 讲spring默认注入到容器的数据源替换为我们设置的这个数据源代理
     *  seata的AT模式执行：执行之前找到before image + 执行之后找到 after image ->  根据这俩image生成回滚日志写入undo_log
     *  如果全局事务不需要回滚 则删除undo_log里的sql回滚记录
     *  所以这里注意必须改为自己的数据源代理
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource() {
        //用默认自带的HikariCP
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        //seata自己的数据源代理
        return new DataSourceProxy(dataSource);
    }
}
