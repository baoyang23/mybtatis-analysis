# 				mybtatis-analysis



#### 题记

   这里记录的是我阅读MyBatis已经一些分析代码的记录.

   联系方式  &  技术交流:

​          微信公众号 :  深文笔记

​           vw :  l18776416225

​    请备注: 技术交流. 谢谢啦.



#### 目前已阅读  

1. 基本的helloWorld使用 :  https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-work-flow 

   这里不仅仅是一个基本的 hello world 的初始化, 并且还分析了大致的MyBatis 的执行流程.

2. MyBatis 的   mybatis-config.xml 配置文件解析阅读  :   https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-xml-config

   这里是对 mybatis 的 mybatis-config.xml 配置文件的解析代码阅读.

3.  MyBatis  的 mapper 文件解析 & 分析:   https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-mapper-xml

4.   MyBatis 的缓存阅读  & 分析 :   https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-cache-analysis

      MyBatis 是有一级缓存/二级缓存，每级缓存对应的概念以及在什么时候是没有命中缓存的等.

5. 与Spring进行整合的整理分析  :  https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-hello

    MyBatis 是怎么于 Spring 进行整合的？通过 xml 配置进入的 bean, 那么我们就从注入进来的 bean 进行分析.

6. 与SpringBoot进行整合分析流程   :  https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-boot-hello

    MyBatis  与 SpringBoot 进行整合, 首先要了解下SpringBoot 的自动配置，然后跟着一个一个debug的的往下读，就会发现 SpringBoot 也是如何将需要注入的 bean 给注入进来的.

7. MyBatis各种插件

    MyBatis 分页插件 :    https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-pagehelper-analysis

     其实该分页插件中的中文注释代码已经是很好的理解了的，就是我们需要去了解下 MyBatis 提供的 plugin , 实现其提供的接口并且重写其方法，重写的方法又是在什么时候被调用到的.

8. 基于MyBatis上封装的MyBatisPlus操作.  后续更新上.

   

----------



#### 总结

最后,要耐心的看完整个流程以及要有研究精神去深入了解各个方面流程等操作.

最后,愿我们学的每一样技术以及框架,都能够有钻研的精神能够阅读下去.

最后,若技术交流&沟通问题，欢迎关注微信公众号/加vw联系方法.

望我们能坚持不懈的coding下去.