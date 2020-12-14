package com.iyang.mybatis.springboot.hello.mapper;

import com.iyang.mybatis.springboot.hello.domain.Blog;

/**
 * @author Yang
 * 当前服务 : mybatis-spring-boot-hello
 * @date 2020/12/14 / 15:52
 */
public interface BlogMapper {

    /**
     * 
     * @param id
     * @return
     */
    Blog findBlogById(Integer id);

}
