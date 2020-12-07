package com.iyang.mybatis;

import com.iyang.mybatis.mapper.BlogMapper;
import com.iyang.mybatis.pojo.TbBlog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Yang on 2020/12/7 23:57
 */

public class UpdateMyBatisInit {

    public static void main(String[] args) throws IOException {

        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);

        SqlSession session = sqlSessionFactory.openSession();

        BlogMapper blogMapper = session.getMapper(BlogMapper.class);
        Integer updateById = blogMapper.updateById(1);
        System.out.println("更新影响的行数" + updateById);

    }

}
