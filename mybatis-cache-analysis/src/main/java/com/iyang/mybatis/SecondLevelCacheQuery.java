package com.iyang.mybatis;

import com.iyang.mybatis.mapper.BlogMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * 二级缓存的知识.
 *
 * @author Yang
 * 当前服务 : mybatis-cache-analysis
 * @date 2020/12/17 / 11:00
 */


public class SecondLevelCacheQuery {

    public static void main(String[] args) throws Exception {

        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);
        SqlSession sqlSession1 = sqlSessionFactory.openSession(true);
        SqlSession sqlSession2 = sqlSessionFactory.openSession(true);

        BlogMapper blogMapper1 = sqlSession1.getMapper(BlogMapper.class);
        BlogMapper blogMapper2 = sqlSession2.getMapper(BlogMapper.class);

        System.out.println("blogMapper1 获取数据" + blogMapper1.selectBlog(1));
        sqlSession1.commit();

        System.out.println("blogMapper2 获取数据" + blogMapper2.selectBlog(1));

    }

}
