## 				MyBatis with Spring



####  题记

​    MyBatis 与 Spring 整合操作.   在我们入门学习 SSM 等东西的时候，就发现了任何东西，最后都是逃不过与Spring整合起来的道路.   然后这里看完 MyBatis 整合完 Spring 之后，那么之后一些其他的第三方，比如axon/redis/apollo/shiro 等这些东西，如果要整合 Spring 的时候，是不是也是相似的整合方式呢？

​    这个需要我们看完 MyBatis 与 Spring 之后，探究其整合的操作.



#### 入门

​     分几个步骤，操作一把即可,带你回到哪个  SM 时代，不过这回是没有了 tomcat 的.

​     先放上一个完成的整合地址 :    https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-hello    如果不要看下面流程的,一步跳过即可.

1.    先创建一个 maven 项目，引入依赖.  依赖参考地址 :    https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-hello/pom.xml    
2.    db配置 : https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-hello/src/main/resources/db.properties  
3.    MyBatis配置:  https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-hello/src/main/resources/mybatis
4.    Spring 配置:  https://github.com/baoyang23/mybtatis-analysis/blob/master/mybatis-spring-hello/src/main/resources/spring-beans.xml
5.   最后,来份我们熟悉的 https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-hello/src/main/resources/sql  mapper.xml 文件.
6.    不忘记再来一份代码 :   https://github.com/baoyang23/mybtatis-analysis/tree/master/mybatis-spring-hello/src     这些直接跑测试类即可.

跟着这上面的几个步骤，就可以搭建完一个项目. 然后喊上我们的 永哥， 打上传说中的 debug , 疯狂的调试看每步干了什么事.

这个的时候，可以跑下测试类，是ok的.



####  分析

​       这里我们首先想到的是我们引入的依赖,是不是有个 mybatis-spring 的依赖. 从这个依赖，可以很明显的看出来，就是通过这个依赖，将 MyBatis 和 Spring 整合起来的。 

​       然后再想想，我们除了这个依赖的话，还再哪里有使用到一些 Spring 和 MyBatis 的东西呢？  然后看到 spring-beans.xml 这个xml配置, 可以看到 org.mybatis.spring.SqlSessionFactoryBean 给注入到 bean 里面来了.org.mybatis.spring.mapper.MapperScannerConfigurer也是给注入到 bean 里面来了. 并且二者都有通过<property>来进行属性设置值操作.  

​        那么,我们就基于这二个类的源码开始阅读.



​	**SqlSessionFactoryBean (org.mybatis.spring.SqlSessionFactoryBean)**	

这里 SqlSessionFactoryBean 是实现了很多接口,这些接口都是Spring的.

FactoryBean 工厂bean,点进去可以看到,其有方法getObject()/getObjectType等方法获取bean的,然后加上泛型,也就是这里获取的 getObject就是泛型.

InitializingBean:  afterPropertiesSet 初始化 bean 的时候，会调用该方法.

ApplicationEvent:   Spring的事件传播机制，就是使用的这种方式.

```java
/**  可以看到这个类实现了 Spring 这个多接口,那么就有个问题,实现了这么多接口的方法,到底是哪个方法先执行的呢？ 如果你对Spring源码很熟悉的话,是有可能清楚的,但是还是会有点绕的. 
这里我们给 getObject/afterPropertiesSet/onApplicationEvent这三个方法打上断点来进行debug,
debug每走的一步,就是执行的先后顺序。如果不是特别熟悉源码的执行顺序,这种笨方法其实也是可以的.
*
* 所以这里debug的执行顺序是 : afterPropertiesSet --> getObject  ---> onApplicationEvent
* 于是我们就跟着这个顺序来阅读.
* 注意在调用这些方法之前,<property>标签的值都是已经赋值进来了的,是通过反射走的set 方法进来的.
*/
public class SqlSessionFactoryBean
    implements FactoryBean<SqlSessionFactory>, InitializingBean, ApplicationListener<ApplicationEvent> {}

// 实现FactoryBean 方法,这里是实现了该接口的三个方法. 其实这里的 isSingle是可以不用实现的
// 因为接口是用 default 来修饰的.
  /**
   * 该方法是判断并且再次确认 SqlSessionFactory是不是有了. 如果没有的话,就会调用afterProperties来初始化.
   * {@inheritDoc}
   */
  @Override
  public SqlSessionFactory getObject() throws Exception {
    if (this.sqlSessionFactory == null) {
      afterPropertiesSet();
    }

    return this.sqlSessionFactory;
  }

  @Override
  public Class<? extends SqlSessionFactory> getObjectType() {
    return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
  }

  @Override
  public boolean isSingleton() {
    return true;
  }


// InitializingBean 实现的方法
  @Override
  public void afterPropertiesSet() throws Exception {
// 先对 dataSource/sqlSessionFactoryBuilder进行非null的判断.
    notNull(dataSource, "Property 'dataSource' is required");
    notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
    state((configuration == null && configLocation == null) || !(configuration != null && configLocation != null),
        "Property 'configuration' and 'configLocation' can not specified with together");
// 这里构建出 一个 sqlSessionFactory工厂来,想想我们最初再看单个MyBatis项目的时候,是不是也有一个获取SqlSessionFactroy的方法,然后从sqlSessionFactory会话中获取出SqlSession来.
    this.sqlSessionFactory = buildSqlSessionFactory();
  }

// ApplicationListener实现方法
  /**
   * failFast 时ture 并且传过来的 event是 ContextRefreshedEvent的话,就会进来.
   *  这里目前都是调用get方法,没有很仔细看出其作用.
   * {@inheritDoc}
   */
  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (failFast && event instanceof ContextRefreshedEvent) {
      // fail-fast -> check all statements are completed
      this.sqlSessionFactory.getConfiguration().getMappedStatementNames();
    }
  }


```





**org.mybatis.spring.SqlSessionFactoryBean#buildSqlSessionFactory**

 该方法需要单独拿出来说下,因为内容还是比较多的.

```java
/**
 * Build a {@code SqlSessionFactory} instance.
 *
 * The default implementation uses the standard MyBatis {@code XMLConfigBuilder} API to build a
 * {@code SqlSessionFactory} instance based on a Reader. Since 1.3.0, it can be specified a {@link Configuration}
 * instance directly(without config file).
 *
 * @return SqlSessionFactory
 * @throws Exception
 *           if configuration is failed
 */
protected SqlSessionFactory buildSqlSessionFactory() throws Exception {

  final Configuration targetConfiguration;

  XMLConfigBuilder xmlConfigBuilder = null;
    
 // 这里分为configuration/ configLocation / 非前二者(可以理解为默认的).
 // 三种处理方式.   
  if (this.configuration != null) {
    targetConfiguration = this.configuration;
    if (targetConfiguration.getVariables() == null) {
      targetConfiguration.setVariables(this.configurationProperties);
    } else if (this.configurationProperties != null) {
      targetConfiguration.getVariables().putAll(this.configurationProperties);
    }
  } else if (this.configLocation != null) {
// 这里就是我们配置的情况 
// org.apache.ibatis.builder.xml.XMLConfigBuilder#XMLConfigBuilder(java.io.InputStream, java.lang.String, java.util.Properties),可以看到这个熟悉的操作,也就是我们单个解析 MyBatis的时候有进行分析过的.      
    xmlConfigBuilder = new XMLConfigBuilder(this.configLocation.getInputStream(), null, this.configurationProperties);
// 获取出 configuration 配置信息.      
    targetConfiguration = xmlConfigBuilder.getConfiguration();
  } else {
    LOGGER.debug(
        () -> "Property 'configuration' or 'configLocation' not specified, using default MyBatis Configuration");
    targetConfiguration = new Configuration();
    Optional.ofNullable(this.configurationProperties).ifPresent(targetConfiguration::setVariables);
  }


// 这里采用 Optional,如果objectFactory不是null的话,就会调用targetConfiguration的 setObjectFactory方法.下面这二个是同理.
  Optional.ofNullable(this.objectFactory).ifPresent(targetConfiguration::setObjectFactory);
  Optional.ofNullable(this.objectWrapperFactory).ifPresent(targetConfiguration::setObjectWrapperFactory);
  Optional.ofNullable(this.vfs).ifPresent(targetConfiguration::setVfsImpl);

// 这里如果有配置typeAliasesPackage这个参数的话,就会对该包下进行扫描,进行一系列的过滤,
// 如果都满足条件的话,targetConfiguration.getTypeAliasRegistry()::registerAlias就会注册到这里. 
  if (hasLength(this.typeAliasesPackage)) {
    scanClasses(this.typeAliasesPackage, this.typeAliasesSuperType).stream()
        .filter(clazz -> !clazz.isAnonymousClass()).filter(clazz -> !clazz.isInterface())
        .filter(clazz -> !clazz.isMemberClass()).forEach(targetConfiguration.getTypeAliasRegistry()::registerAlias);
  }
// 是否有typeAliases这个参数,如果有的话,也是可以看到是注册到上面哪一步的里面来.
  if (!isEmpty(this.typeAliases)) {
    Stream.of(this.typeAliases).forEach(typeAlias -> {
      targetConfiguration.getTypeAliasRegistry().registerAlias(typeAlias);
      LOGGER.debug(() -> "Registered type alias: '" + typeAlias + "'");
    });
  }

//判断是否有插件,如果有插件的话,也会添加到configuration中来.    
  if (!isEmpty(this.plugins)) {
    Stream.of(this.plugins).forEach(plugin -> {
      targetConfiguration.addInterceptor(plugin);
      LOGGER.debug(() -> "Registered plugin: '" + plugin + "'");
    });
  }

  if (hasLength(this.typeHandlersPackage)) {
    scanClasses(this.typeHandlersPackage, TypeHandler.class).stream().filter(clazz -> !clazz.isAnonymousClass())
        .filter(clazz -> !clazz.isInterface()).filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
        .forEach(targetConfiguration.getTypeHandlerRegistry()::register);
  }

  if (!isEmpty(this.typeHandlers)) {
    Stream.of(this.typeHandlers).forEach(typeHandler -> {
      targetConfiguration.getTypeHandlerRegistry().register(typeHandler);
      LOGGER.debug(() -> "Registered type handler: '" + typeHandler + "'");
    });
  }

  targetConfiguration.setDefaultEnumTypeHandler(defaultEnumTypeHandler);

  if (!isEmpty(this.scriptingLanguageDrivers)) {
    Stream.of(this.scriptingLanguageDrivers).forEach(languageDriver -> {
      targetConfiguration.getLanguageRegistry().register(languageDriver);
      LOGGER.debug(() -> "Registered scripting language driver: '" + languageDriver + "'");
    });
  }
  Optional.ofNullable(this.defaultScriptingLanguageDriver)
      .ifPresent(targetConfiguration::setDefaultScriptingLanguage);

  if (this.databaseIdProvider != null) {// fix #64 set databaseId before parse mapper xmls
    try {
      targetConfiguration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource));
    } catch (SQLException e) {
      throw new NestedIOException("Failed getting a databaseId", e);
    }
  }

  Optional.ofNullable(this.cache).ifPresent(targetConfiguration::addCache);
// 这这之前,都是对一些配置信息的读取,如果有的话,就会进行相应的赋值之类的操作.
    
  if (xmlConfigBuilder != null) {
    try {
// 最后这里的 parse 解析方法,是和单个 Mybatis的解读是一样的.        
      xmlConfigBuilder.parse();
      LOGGER.debug(() -> "Parsed configuration file: '" + this.configLocation + "'");
    } catch (Exception ex) {
      throw new NestedIOException("Failed to parse config resource: " + this.configLocation, ex);
    } finally {
      ErrorContext.instance().reset();
    }
  }

//这里可以看事务工厂,是使用了mybatis-spring包下的.
  targetConfiguration.setEnvironment(new Environment(this.environment,
      this.transactionFactory == null ? new SpringManagedTransactionFactory() : this.transactionFactory,
      this.dataSource));

// 这里是处理 mapper.xml 文件的配置,如果在这里是有配置的话,那么也是会被解析到的.    
  if (this.mapperLocations != null) {
    if (this.mapperLocations.length == 0) {
      LOGGER.warn(() -> "Property 'mapperLocations' was specified but matching resources are not found.");
    } else {
      for (Resource mapperLocation : this.mapperLocations) {
        if (mapperLocation == null) {
          continue;
        }
        try {
          XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
              targetConfiguration, mapperLocation.toString(), targetConfiguration.getSqlFragments());
          xmlMapperBuilder.parse();
        } catch (Exception e) {
          throw new NestedIOException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
        } finally {
          ErrorContext.instance().reset();
        }
        LOGGER.debug(() -> "Parsed mapper file: '" + mapperLocation + "'");
      }
    }
  } else {
    LOGGER.debug(() -> "Property 'mapperLocations' was not specified.");
  }
//org.apache.ibatis.session.defaults.DefaultSqlSessionFactory,最后到这里也是new了一个mybatis包下的默认SqlSessionFactory类.
  return this.sqlSessionFactoryBuilder.build(targetConfiguration);
}
```

  可以看到该方法给人感觉, 先是判断一些配置信息是不是有值，如果是有值的话，就会进行相应的处理。最后调用我们在看单个 mybatis 的 parse 解析方法,最后new了一个默认的sqlSessionFactory工厂类出来.



 

**MapperScannerConfigurer(org.mybatis.spring.mapper.MapperScannerConfigurer)**

  接着看,spring-beans.xml 里面的第二个配置.

  可以看到该类，也是实现了 spring 的很多接口.

  BeanDefinitionRegistryPostProcessor :  注册BeanDefinition到Spring容器中来.

  ApplicationContextAware :  获取 ApplicationContext

  BeanNameAware :   设置 beanName名字.

```java
public class MapperScannerConfigurer
    implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware { }
```









