package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.kingshin.rediscache.dto.LoginFormDTO;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.dto.UserDTO;
import cn.kingshin.rediscache.entity.User;
import cn.kingshin.rediscache.mapper.UserMapper;
import cn.kingshin.rediscache.service.IUserService;
import cn.kingshin.rediscache.utils.RedisConstants;
import cn.kingshin.rediscache.utils.RegexUtils;
import cn.kingshin.rediscache.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.kingshin.rediscache.utils.RedisConstants.*;
import static cn.kingshin.rediscache.utils.SystemConstants.USER_NICK_NAME_PREFIX;


/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4.保存验证码到 redis hash
        //session.setAttribute("code",code);
        //给手机号加一个业务前缀 给code设置有效期
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code,CACHE_NULL_TTL, TimeUnit.MINUTES);
        // 5.发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
        // 3.校验验证码
        //Object cacheCode = session.getAttribute("code");
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if(cacheCode == null || !cacheCode.equals(code)){
            //3.不一致，报错
            return Result.fail("验证码错误");
        }
        //一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();
        //5.判断用户是否存在
        if(user == null){
            //不存在，则创建
            user =  createUserWithPhone(phone);
        }
        //保存进redis
        String token = UUID.randomUUID().toString(true);
        //生成随机token 作为登录令牌
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap  = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        //将对象转成string类型 避免对象转换异常
                        .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        //将user对象转为hash存储
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //设置过期时间30min
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);

        //7.保存用户信息到session中

        return Result.ok(token);
    }
    /**
     * @description
     *        统计用户登录签到信息
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/25 13:43:06
     */
    @Override
    public Result sgin() {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();//注意是1-31天 不是0-30 要-1
        //写入redis STEBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }
    /**
     * @description
     *        统计连续签到数
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/25 14:04:49
     */
    @Override
    public Result signCount() {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        //获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();//注意是1-31天 不是0-30 要-1
        //获取本月截至今天为止的签到记录 拿到十进制  BITFIELD sign:5:202208 GET u14 0 查询id为5的用户本月14天的签到次数 从第一天开始查
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );

        if (result == null || result.isEmpty()) {
            //没有任何签到结果
            return Result.ok(0);
        }
        //保证健壮性 进行全量判断
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        //循环遍历
        int count = 0;//计数器
        while (true){
            //让这个数字和1做与运算 得到数字的最后一个Bit位 || 判断这个bit位是否为0
            if((num & 1) == 0){
                //如果为0 未签到
                break;
            }else {
                count++; //如果不为0 已签到 计数器+1
            }
            //把数字右移一位 抛弃最后一个bit位 继续下一个bit位
            num >>>= 1;//先右移一位再赋值给Num 覆盖掉num
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;

    }
}
