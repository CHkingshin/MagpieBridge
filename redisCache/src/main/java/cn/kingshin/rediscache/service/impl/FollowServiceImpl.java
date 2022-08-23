package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.dto.UserDTO;
import cn.kingshin.rediscache.entity.Follow;
import cn.kingshin.rediscache.mapper.FollowMapper;
import cn.kingshin.rediscache.service.IFollowService;
import cn.kingshin.rediscache.service.IUserService;
import cn.kingshin.rediscache.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author KingShin
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    /**
     * @description 关注 取关
     * @param followUserId
     * @param isFollw
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/16 19:19:32
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollw) {
        //获取当前登录的用户
        Long userId = UserHolder.getUser().getId();

        String key = "follows:"+userId;
        //1.判断到底是关注还是取关
        if(isFollw){
            //关注 新增当前用户与目标用户的关系
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                //把关注用户的id 放入redis的SET集合

                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else {
            //取关 删除当前用户与目标用户的关系
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (isSuccess) {
                //把关注用户的id从redis移除
                stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        //查询是否关注
        Integer count = query().eq("user_id", userId)
                .eq("follow_user_id", followUserId).count();

        return Result.ok(count > 0);
    }
    /*共同关注*/
    @Override
    public Result followCommons(Long id) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key1 = "follows:"+userId;
        //求交集
        String key2 = "follows:"+userId;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if(intersect == null || intersect.isEmpty()){
            //无交集 给个空集合不报错
            return Result.ok(Collections.emptyList());
        }
        //解析id集合 转为Long
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOS = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return Result.ok(userDTOS);
    }
}
