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
 * @date 2020/12/17 / 11:18
 */
public class SecondLevelCacheUpdateQuery {

    public static void main(String[] args)  throws Exception {

        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);
        SqlSession sqlSession1 = sqlSessionFactory.openSession(false);
        SqlSession sqlSession2 = sqlSessionFactory.openSession(false);
        SqlSession sqlSession3 = sqlSessionFactory.openSession(false);

        BlogMapper blogMapper1 = sqlSession1.getMapper(BlogMapper.class);
        BlogMapper blogMapper2 = sqlSession2.getMapper(BlogMapper.class);
        BlogMapper blogMapper3 = sqlSession3.getMapper(BlogMapper.class);

        System.out.println(" blogMapper1 查询出来的数据 : " + blogMapper1.selectBlog(1));
        sqlSession1.commit();

        System.out.println(" blogMapper2 查询出来的结果 : " + blogMapper2.selectBlog(1));

        System.out.println(blogMapper3.updateHashCode("GavinYang"));
        sqlSession3.commit();

        System.out.println(" blogMapper2 查询出来的结果 : " + blogMapper2.selectBlog(1));
    }

}
