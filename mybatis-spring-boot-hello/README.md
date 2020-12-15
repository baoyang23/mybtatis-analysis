## 			   MyBatis With SpringBoot



#### 前提

​    MyBatis 与 SpringBoot 整合操作.  在这次整合的过程中,再次明白自己毫无疑问的是一个比较手残的同学了.

​    这里我们是基于 sql 语句写在 xml 里面进行整合的操作. 

   

#### 入门 

​      这里说下创建一个 入门 项目的大致流程.

​       先创建一个 SpringBoot 项目 ,  引入依赖 :   https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-boot-hello/pom.xml

​        创建 MyBatis 的配置文件信息 :     https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-boot-hello/src/main/resources/mybatis-config.xml

​        创建查询的 sql 语句，也就是我们的 mapper 文件 : https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-boot-hello/src/main/resources/mapper

​       application.properties :  https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-boot-hello/src/main/resources/application.properties

​      扫描 mapper 接口 :  @MapperScan(basePackages = {"com.iyang.mybatis.springboot.hello.mapper"})  https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-boot-hello/src/main/java/com/iyang/mybatis/springboot/hello/MybatisSpringBootHelloApplication.java



这里是没有引入 web 依赖的 , 直接启动 main 方法 ,  然后就可以看到我们查询出来的结果了.  

如果你熟悉 SpringBoot 源码的话，就会晓得有一个自动装配的操作.

如果不熟悉的话，那么就只能通过 @MapperScan(basePackages = {"com.iyang.mybatis.springboot.hello.mapper"}) 去看 , 这样有些是依赖自动装配（spring.factories） 中的配置加载的,  所以这里建议在看之前，如果是有一点 SpringBoot 扩展的知识了解是很好的。如果没有怎么办呢？没有就来看我接下来的内容。 

其实这个地方你仔细想下，在 MyBatis 与 Spring 整合的时候，通过 xml 的方式给 MyBatis 的bean 已经 mybatis-spring 中自己写的扫描类，最后将扫描出来的 bd 在还没初始化之前，将bd 的beanClass 替换为我们的代理类.

那么，SpringBoot 与 MyBatis 整合的时候，最后要做的事情是不是也是将 MyBatis 的信息注入到 SpringBoot 来呢？只不过，SpringBoot 就不像 Spring 一样了，还将 bean 的信息配置到 xml 文件中. 

于是，接下来跟我的阅读&分析来一步一步的往下看.



#### 方法分析

​     **关注点一** :   这里我们点入到   org.mybatis.spring.annotation.MapperScan 注解里面来，可以看到有一个  @Import(MapperScannerRegistrar.class) , 于是我们顺手跟进来 :  org.mybatis.spring.annotation.MapperScannerRegistrar , 从名字上来，这个类就做了一个扫描mapper并且将mapper注入到Spring容器中来的事情. 

​    **关注点二** :   我们从引入进来的依赖来看,   mybatis-spring-boot-starter-2.1.2.jar  跟进到 这个包来，可以看到这个包也是引入一些进来.  mybatis/mybatis-spring/spring-boot-starter-jdbc 这三个依赖我们应该不是很陌生的，mybatis-spring-boot-autoconfigure主要来看这个。   spring.factories 的作用大家可以去了解下，SpringBoot很多 EnableAutoConfiguration 的配置都是放入在这个里面的，在启动的时候，会去一层一层的去读取 spring.factories 文件的内容。  这里我们主要来看 :  org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration 这个类皆可.

​     MyBatis 在 properties 中的配置文件读取 :  org.mybatis.spring.boot.autoconfigure.MybatisProperties

可以看到该类上是有: @ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)



于是我们一下子就多了二个关注点, 这里我们可以采用之前的  笨方法， 当你对整合流程执行不是很熟悉的话，可以在这二个关注点的重写方法上都打算断点，看下其执行顺序是怎么执行的.   弄清楚了执行流程,就可以跟着流程来一步一步的分析. 从我们打上 debug 开始，往下的执行流程就是一步一步来的，那么就跟着我们debug 的方法来一步一步的分析.

org.mybatis.spring.annotation.MapperScannerRegistrar#registerBeanDefinitions() --->    org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration#MybatisAutoConfiguration  ---> org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration#afterPropertiesSet  ---->   org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration#sqlSessionFactory  --->  org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration#sqlSessionTemplate() ----> 



**org.mybatis.spring.annotation.MapperScannerRegistrar#registerBeanDefinitions() 方法** : 

```java
/**
 * {@inheritDoc}
 */
@Override
public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
/***
*  这里是获取出了注解里面属性的值. 
*/   
  AnnotationAttributes mapperScanAttrs = 
  AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
    
  if (mapperScanAttrs != null) {
    registerBeanDefinitions(importingClassMetadata, mapperScanAttrs, registry,
        generateBaseBeanName(importingClassMetadata, 0));
  }
}
```



