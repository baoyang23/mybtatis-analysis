package com.iyang.mybatis.factory;

import org.apache.ibatis.reflection.DefaultReflectorFactory;

/**
 * @author Yang
 * 当前服务 : mybatis-xml-config
 * @date 2020/12/8 / 11:39
 */
public class GavinReflectorFactory extends DefaultReflectorFactory {

    public GavinReflectorFactory(){

        System.out.println("GavinReflectorFactory 无参构造函数");
    }


}
