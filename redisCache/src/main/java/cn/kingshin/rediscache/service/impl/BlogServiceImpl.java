package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.dto.UserDTO;
import cn.kingshin.rediscache.entity.Blog;
import cn.kingshin.rediscache.entity.User;
import cn.kingshin.rediscache.mapper.BlogMapper;
import cn.kingshin.rediscache.service.IBlogService;
import cn.kingshin.rediscache.service.IFollowService;
import cn.kingshin.rediscache.service.IUserService;
import cn.kingshin.rediscache.utils.SystemConstants;
import cn.kingshin.rediscache.utils.UserHolder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.kingshin.rediscache.utils.RedisConstants.BLOG_LIKED_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author KingShin
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private IUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    /**
     * @param blog
     * @return void
     * @description 用户是否已点赞
     * @author KingShin
     * @date 2022/8/16 17:33:21
     */
    private void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();
        // 2.判断当前登录用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);//！=null > 已点赞
    }

    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("blog不存在！！！");
        }
        //查询相关用户
        queryBlogUser(blog);
        //查询用户是否已经点赞
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.判断当前登录用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        //score查询用户是否存在在ZSet里
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的set集合  zadd key value score
            if (isSuccess) {
                //以当前时间戳存储 方便以时间排序
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    /**
     * @param id
     * @return cn.kingshin.rediscache.dto.Result
     * @description 查询点赞列表 TOP5的点赞用户
     * @author KingShin
     * @date 2022/8/16 18:27:49
     */
    @Override
    public Result queryBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        //1.查询top5的点赞用户 zrange key 0 4
        Set<String> top5set = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5set == null || top5set.isEmpty()) {
            return Result.ok(Collections.emptyList());//空给个空集合避免空指针
        }
        //2.解析出其中的用户id
        List<Long> ids = top5set.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);//工具类将ids里的每个id拼接成字符串 下面使用就不用写死
        //3.根据用户id查用户  SELECT id,phone,password,nick_name,icon,create_time,update_time FROM tb_user WHERE id IN ( ? , ? ) 会导致DB从1>5排序 而不是5>1 要加上order by field(id,5,1)手动指定排序方式 MP手动写SQL
        List<UserDTO> userDTOS = userService
                .query().in("id", ids)
                .last("ORDER BY FIELD(id," + idStr + ") ")//自动在前面的sql语句前拼接
                .list()
                .stream()
                .map(
                        user -> BeanUtil.copyProperties(user, UserDTO.class)
                )
                .collect(Collectors.toList());
        //返回
        return Result.ok(userDTOS);
    }

    private void queryBlogUser(Blog blog) {
        //查询相关用户
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
