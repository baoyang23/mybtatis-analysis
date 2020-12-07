package com.iyang.mybatis.pojo;

import org.apache.ibatis.type.Alias;

/**
 * @author Yang
 * 当前服务 : mybatis-work-flow
 * @date 2020/12/3 / 17:19
 */

//@Alias("GavinYang")
public class TbBlog {

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
        return "TbBlog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
