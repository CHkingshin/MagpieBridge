package cn.kingshin.streamex.stream;

import cn.kingshin.streamex.pojo.Role;
import cn.kingshin.streamex.pojo.User;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Map;

/**
 * @author KingShin
 */
public class StreamexDemo {
    public static void main(String[] args) {

        User users = new User();
        users.setName("lee");
        users.setId(1);
        /*users.setRole("cn");

        users.stream()
                .map(User::getName)
                .collect(Collectors.toList());*/

        List<String > userName = StreamEx.of(users).map(User::getName).toList();
        System.out.println(userName);

        Map<Role, List<User>> roleListMap = StreamEx.of(users).groupingBy(User::getRole);
        System.out.println(roleListMap);

        String joining = StreamEx.of(1, 2, 3)
                .joining("; ");// "1; 2; 3"
        System.out.println(joining);
    }



}
