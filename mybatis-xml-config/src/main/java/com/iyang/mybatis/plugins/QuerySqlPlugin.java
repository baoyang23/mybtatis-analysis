package com.iyang.mybatis.plugins;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

/**
 * @author Yang
 * 当前服务 : mybatis-xml-config
 * @date 2020/12/8 / 10:08
 */

@Intercepts(
        {
                @Signature(
                        type = Executor.class,
                        method = "query",
                        args = {
                                MappedStatement.class, Object.class, RowBounds.class,
                                ResultHandler.class, CacheKey.class, BoundSql.class
                        }
                )
        }
)
public class QuerySqlPlugin implements Interceptor {

    private Properties properties = new Properties();

    /**
     * Init
     */
    public QuerySqlPlugin(){

        System.out.println("QuerySqlPlugin中构造方法");
    }

    public Object intercept(Invocation invocation) throws Throwable {

        System.out.println("QuerySqlPlugin的intercept方法调用");
        Set<Object> objects = properties.keySet();

        System.out.println("集合长度");
        System.out.println(objects.size());

        Object proceed = invocation.proceed();
        return proceed;
    }

    public Object plugin(Object target) {


        // System.out.println("QuerySqlPlugin 中的 plugin 操作");
        // System.out.println(target.toString());
        return target;
    }

/*    public void setProperties(Properties properties) {

    }*/
}
