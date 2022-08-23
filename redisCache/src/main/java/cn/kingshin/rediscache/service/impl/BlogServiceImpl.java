package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.dto.ScrollResult;
import cn.kingshin.rediscache.dto.UserDTO;
import cn.kingshin.rediscache.entity.Blog;
import cn.kingshin.rediscache.entity.Follow;
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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.kingshin.rediscache.utils.RedisConstants.BLOG_LIKED_KEY;
import static cn.kingshin.rediscache.utils.RedisConstants.FEED_KEY;

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
    @Resource
    private IBlogService blogService;
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

    @Override
    @Transactional
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("新增笔记失败");
        }
        //查询作者所有的粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        //推送笔记id给所有粉丝
        follows.forEach(follow -> {
            Long userId = follow.getUserId();
            String key = FEED_KEY + userId;
            //推送  以时间戳为标记进行排序判定
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        });
        // 返回id
        return Result.ok(blog.getId()+"发布成功！");
    }
    /**
     * @description 滚动分页推送
     * @param max
     * @param offset
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/23 16:00:14
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //获取当前用户
        Long userid = UserHolder.getUser().getId();
        //查询收件箱
        String key = FEED_KEY + userid;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);

        //判空
        if(typedTuples ==null||typedTuples.isEmpty()){
            return Result.ok();
        }
        //取出blog的id 时间戳score offset
        ArrayList<Object> ids = new ArrayList<>(typedTuples.size());//定义和收件箱一样长度的数组装id
        long minTime = 0;//给定初始化时间为0 循环遍历之后minTime里一定是最后一个元素的时间
        int os = 1;//offset初始化为1 集合里起码会有一个和minTime一样的 那就是他本身
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            //获取id
            ids.add(Long.valueOf(tuple.getValue()));
            //获取分数（时间戳
            // minTime.set(tuple.getScore().longValue());
            long time = tuple.getScore().longValue();
            //如果取出来的时间戳和最小时间戳一样 则表示有重复的时间戳
            if(time == minTime){
                os++;
            }else {
                //不一样说明当前时间不是最小时间 后取的时间肯定比当前时间小
                minTime = time;
                //重置 解决漏读重复的blog
                os = 1;
            }
        }
        //根据id查询blog 注意 这里的ids数组是有序的 不能直接在Mysql里用in去查 加orderby
        String idStr = StrUtil.join(",", ids);  //拼接一下
        List<Blog> blogs = query()
                .in("id", ids)
                .last("ORDER BY FIELD(id," + idStr + ") ")//自动在前面的sql语句前拼接
                .list();
        //查询是否有关注的用户和blog是否被点赞
        blogs.forEach(blog -> {
                queryBlogUser(blog);
                isBlogLiked(blog);
                }
        );
        // 封装返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogs);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(minTime);
        return Result.ok(scrollResult);
    }

    private void queryBlogUser(Blog blog) {
        //查询相关用户
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
