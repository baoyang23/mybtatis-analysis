package com.iyang.mybatis.springboot.hello.domain;

/**
 * @author Yang
 * 当前服务 : mybatis-spring-boot-hello
 * @date 2020/12/14 / 15:50
 */
public class Blog {

    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
