package com.iyang.mybatis.factory;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.type.Alias;

/**
 * @author Yang
 * 当前服务 : mybatis-xml-config
 * @date 2020/12/8 / 11:33
 */

@Alias("GavinFactory")
public class GavinObjectWrapperFactory implements ObjectWrapperFactory {

    public GavinObjectWrapperFactory(){
        System.out.println("GavinObjectWrapperFactory 构造函数");
    }

    public boolean hasWrapperFor(Object object) {
        return false;
    }

    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new ReflectionException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
