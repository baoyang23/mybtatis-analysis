package com.iyang.mybatis.plugins;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

/**
 * Created by Yang on 2020/12/7 23:43
 */

@Intercepts({@Signature(
        type= Executor.class,
        method = "update",
        args = {MappedStatement.class,Object.class})})
public class ExamplePlugin implements Interceptor {

    public ExamplePlugin(){
        System.out.println("进入到插件的无参构造函数中来");
    }

    public Object intercept(Invocation invocation) throws Throwable {

        System.out.println("程序走进入到 com.iyang.mybatis.plugins.ExamplePlugin 来了");

        Object proceed = invocation.proceed();

        return proceed;
    }


}
