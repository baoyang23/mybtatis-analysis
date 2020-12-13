package com.iyang.sm;

import com.iyang.sm.mapper.BlogMapper;
import com.iyang.sm.po.Blog;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Yang on 2020/12/13 15:07
 */

public class MyBatisSpringFuncTest {

    private ApplicationContext applicationContext;

    @Before
    public void setUp(){
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-beans.xml");
    }

    @Test
    public void findById(){

        BlogMapper blogMapper = applicationContext.getBean(BlogMapper.class);
        Blog blog = blogMapper.findBlogById(1);
        System.out.println(blog);

    }

}
