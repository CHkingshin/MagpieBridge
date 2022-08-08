package cn.kingshin.streamex.pojo;

import lombok.Data;

@Data
public class User {
    int id;
    String name;
    Role role = new Role();

    // standard getters, setters, and constructors
}
