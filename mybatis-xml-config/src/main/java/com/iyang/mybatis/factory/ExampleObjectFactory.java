package com.iyang.mybatis.factory;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Yang
 * 当前服务 : mybatis-xml-config
 * @date 2020/12/8 / 10:38
 */


public class ExampleObjectFactory extends DefaultObjectFactory {

    private Properties properties = new Properties();

    public ExampleObjectFactory(){

        System.out.println("ExampleObjectFactory 无参数构造函数");
    }

    /**
     *  如果在 xml 中配置了的话,那么在
     *  org.apache.ibatis.builder.xml.XMLConfigBuilder#objectFactoryElement(org.apache.ibatis.parsing.XNode)
     *  就会注入到这里来.
     *  可想而知,Mybatis也是利用了 set 方法来进行反射的.
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;

        System.out.println(properties);
    }

    // 处理无参数构造函数
    @Override
    public <T> T create(Class<T> type) {

        System.out.println("进入 create(Class<T> type) 方法中");
        System.out.println(type.toString());
        System.out.println("--------   分割线 --------");

        return super.create(type);
    }

    // 处理有参数构造函数
    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        System.out.println("进入 create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) 方法");
        return super.create(type, constructorArgTypes, constructorArgs);
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        //return super.isCollection(type);
        return Collection.class.isAssignableFrom(type);
    }
}
