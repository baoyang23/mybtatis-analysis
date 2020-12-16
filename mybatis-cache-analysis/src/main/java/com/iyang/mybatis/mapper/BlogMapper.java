package com.iyang.mybatis.mapper;

import com.iyang.mybatis.pojo.TbBlog;
import org.apache.ibatis.annotations.Param;

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
     * 添加数据
     * @param name
     * @return
     */
    int addBlog(@Param("name") String name);

    /**
     * Hash Code to update.
     * @return
     */
    int updateHashCode(String name);

}
