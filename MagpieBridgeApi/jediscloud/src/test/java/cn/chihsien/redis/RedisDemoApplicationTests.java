package cn.chihsien.redis;

import cn.chihsien.redis.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @describe
 * @auther KingShin
 */
@SpringBootTest
class RedisDemoApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testString() {
        // 写入一条String数据
        redisTemplate.opsForValue().set("name", "虎哥");
        // 获取string数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);
    }

    @Test
    void testSaveUser(){
        //写入数据
        redisTemplate.opsForValue().set("user:100", new User("chihsien001",26));
        User o = (User) redisTemplate.opsForValue().get("user:100");
        //获取数据
        System.out.println(("o=" + o));

    }
}