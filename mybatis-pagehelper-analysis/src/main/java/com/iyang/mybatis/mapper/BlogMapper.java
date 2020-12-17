package com.iyang.mybatis.mapper;

import com.github.pagehelper.Page;
import com.iyang.mybatis.pojo.TbBlog;

import java.util.List;

/**
 * @author Yang
 * 当前服务 : mybatis-work-flow
 * @date 2020/12/3 / 17:20
 */
public interface BlogMapper {

    /**
     * Query By ID.
     * @param id
     * @return
     */
    TbBlog selectBlog(Integer id);

    /**
     *
     * @return
     */
    List<TbBlog> selectAll();
}
