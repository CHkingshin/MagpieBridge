package cn.kingshin.rediscache.utils;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author KingShin
 * 逻辑过期解决缓存穿透的redis数据类
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
