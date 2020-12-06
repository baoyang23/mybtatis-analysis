package com.iyang.mybatis;

import com.iyang.mybatis.mapper.BlogMapper;
import com.iyang.mybatis.pojo.TbBlog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import sun.misc.Launcher;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Yang
 * 当前服务 : mybatis-work-flow
 * @date 2020/12/1 / 18:04
 */
public class InitHelloMyBatis {


    /*
     *
     * 这里我们还是 copy 之前的代码,只是这里会专门操作 xml config 配置文件.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);

        SqlSession session = sqlSessionFactory.openSession();
        BlogMapper blogMapper = session.getMapper(BlogMapper.class);
        TbBlog tbBlog = blogMapper.selectBlog(1);
        System.out.println(tbBlog);

    }

}
