package cn.chihsien.mp_demo;

import cn.chihsien.mp_demo.dao.User;
import cn.chihsien.mp_demo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MpDemoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectList() {
        System.out.println(("----- selectAll method test ------"));
        //UserMapper 中的 selectList() 方法的参数为 MP 内置的条件封装器 Wrapper
        //所以不填写就是无任何条件
        List<User> users = userMapper.selectList(null);
        users.forEach(System.out::println);
    }


    @Test
    public void testInsert(){
        User user = new User();
        user.setName("kinro");
        user.setAge(18);
        user.setEmail("chihsien@gmail.com");
        int result = userMapper.insert(user);
        System.out.println(result); //影响的行数
        System.out.println(user); //id自动回填
    }

    @Test
    public void testUpdateById(){
        //1 根据id查询记录
        User user = userMapper.selectById(1L);
        //2 设置修改的值
        user.setAge(50);
        //3 调用方法修改
        int result = userMapper.updateById(user);
        System.out.println(result);
    }
}
