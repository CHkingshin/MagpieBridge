package cn.chihsien.partition;

import cn.chihsien.vo.QinyiMessage;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * <h1>自定义从 Message 中提取 partition key 的策略</h1>
 *
 * @author KingShin
 */
@Slf4j
@Component
public class QinyiPartitionKeyExtractorStrategy implements PartitionKeyExtractorStrategy {

    @Override
    public Object extractKey(Message<?> message) {
        //格式转化
        QinyiMessage qinyiMessage = JSON.parseObject(
                message.getPayload().toString(), QinyiMessage.class
        );
        // 自定义提取 key
        String key = qinyiMessage.getProjectName();
        log.info("SpringCloud Stream Qinyi Partition Key: [{}]", key);
        return key;
    }
}
