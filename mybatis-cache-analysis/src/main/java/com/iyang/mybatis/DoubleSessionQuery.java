package com.iyang.mybatis;

import com.iyang.mybatis.mapper.BlogMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * @author Yang
 * 当前服务 : mybatis-cache-analysis
 * @date 2020/12/16 / 16:31
 */
public class DoubleSessionQuery {

    public static void main(String[] args) throws Exception {

        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);

        SqlSession openSession1 = sqlSessionFactory.openSession();
        SqlSession openSession2 = sqlSessionFactory.openSession();
        BlogMapper blogMapper1 = openSession1.getMapper(BlogMapper.class);
        BlogMapper blogMapper2 = openSession2.getMapper(BlogMapper.class);

        System.out.println("blogMapper1 读取数据 " + blogMapper1.selectBlog(1));
        System.out.println("blogMapper2 读取数据" + blogMapper2.selectBlog(1));

        System.out.println(blogMapper1.updateHashCode("PeterWong"));

        System.out.println("blogMapper1 读取数据 " + blogMapper1.selectBlog(1));
        System.out.println("blogMapper2 读取数据" + blogMapper2.selectBlog(1));

    }

}
