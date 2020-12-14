package com.iyang.mybatis.springboot.hello;

import com.iyang.mybatis.springboot.hello.domain.Blog;
import com.iyang.mybatis.springboot.hello.mapper.BlogMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 * @author BaoYang
 *
 */

@SpringBootApplication
@MapperScan(basePackages = {"com.iyang.mybatis.springboot.hello.mapper"})
public class MybatisSpringBootHelloApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisSpringBootHelloApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(MybatisSpringBootHelloApplication.class, args);

        BlogMapper blogMapper = context.getBean(BlogMapper.class);
        Blog blog = blogMapper.findBlogById(1);

        LOGGER.info("The blog value is ---> {} " , blog);
    }

}
