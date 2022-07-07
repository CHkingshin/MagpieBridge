package cn.chihsien.mp_demo;

import cn.chihsien.mp_demo.dao.User;
import cn.chihsien.mp_demo.mapper.UserMapper;
import cn.chihsien.mp_demo.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class MpDemoApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

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
        user.setAge(22);
        //3 调用方法修改
        int result = userMapper.updateById(user);
        System.out.println(result);
    }


    @Test
    public void testSelectPage() {
        Page<User> page = new Page<>(1,5);
        userMapper.selectPage(page, null);
        page.getRecords().forEach(System.out::println);
        System.out.println(page.getCurrent());
        System.out.println(page.getPages());
        System.out.println(page.getSize());
        System.out.println(page.getTotal());
        System.out.println(page.hasNext());
        System.out.println(page.hasPrevious());
    }


    @Test
    public void testDeleteById(){
        int result = userMapper.deleteById(1543498617236316161L);
        System.out.println(result);
    }


    @Test
    public void testDeleteBatchIds() {
        int result = userMapper.deleteBatchIds(Arrays.asList(8, 9, 10));
        System.out.println(result);
    }

    /**
     * 测试 逻辑删除
     */
    @Test
    public void testLogicDelete() {
        int result = userMapper.deleteById(1L);
        System.out.println(result);
    }

 /** * 测试 逻辑删除后的查询： * 不包括被逻辑删除的记录 */
 @Test
 public void testLogicDeleteSelect() {
     User user = new User();
     List<User> users = userMapper.selectList(null);
     users.forEach(System.out::println);}

    @Test
    public void testSelect() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("age", 18);
        List<User> users = userMapper.selectList(queryWrapper);
        System.out.println(users);
    }


    @Test
    public void testSelectOne() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", "kinro1");
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);
    }
    @Test
    public void testLambdaQuery() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAge,18);
        queryWrapper.like(User::getName,"kinro");
        List<User> list = userMapper.selectList(queryWrapper);
        System.out.println(list);
    }

    //查询表所有数据
    @Test
    public void findAll() {
        List<User> userList = userService.list();
        for (User user:userList) {
            System.out.println(user);
        }
    }
}
