package com.iyang.mybatis;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.iyang.mybatis.mapper.BlogMapper;
import com.iyang.mybatis.pojo.TbBlog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.List;

/**
 * @author Yang
 * 当前服务 : mybatis-pagehelper-analysis
 * @date 2020/12/17 / 12:20
 */
public class PageHelperInit {

    public static void main(String[] args) throws Exception {

        // PageHelper
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = factory.openSession();
        PageHelper.startPage(2, 5);
        BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);
        List<TbBlog> tbBlogs = blogMapper.selectAll();
        // PageInfo<List<TbBlog>> pageInfo = new PageInfo(tbBlogs);
        System.out.println(tbBlogs.toString());
        System.out.println(tbBlogs.get(0).toString());
        // System.out.println(pageInfo);

    }

}
