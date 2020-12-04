## 						MyBatis之HelloWorld



#### Init

  该项目是根据 MyBatis 官网提供的写的一个HelloWorld,也就是根据官网一步一步来的.

  这也说明,我们可以跟着官网提供的代码案列来一步一步的往下debug跟着源码来阅读.



   sql 放在项目的 resources 目录下的 sql 目录下, 找到合适db就create.



#### Function

 

 大致的执行流程代码, 这里是列举的 Query 的 HelloWorld案例.

 配置好数据库信息,然后走到 InitHelloMyBatis类中来,执行就可以看到查询出id是1的 Object了.

```java
public class InitHelloMyBatis {

    public static void main(String[] args) throws IOException {
        // 读取配置文件.
        InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");
        // 传入读取配置文件的流,使用SqlSessionFactoryBuilder来
        // 构建 SqlSessionFactory.
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream);
        // 从 SqlSessionFactory 中获取SqlSession会话.
        SqlSession session = sqlSessionFactory.openSession();

        // 从会话中获取 Mapper.
        BlogMapper blogMapper = session.getMapper(BlogMapper.class);
        
        // 调用查询方法.
        TbBlog tbBlog = blogMapper.selectBlog(1);
        System.out.println(tbBlog);
        
    }

}
```

大致流程:

-   使用 Resources 来读取 mybatis-config.xml配置文件, 如果该文件不存在或者读取出来 InputStream 是 null 的话,程序就会抛出 IOException 的错误来.
-   读取配置没有问题,来到 new SqlSessionFactoryBuilder().build(io) 来构建出一个 SqlSessionFactory 来, 这里构建出来的 SqlSessionFactory 肯定是有已经讲配置文件给全部加载进去了的.
-  SqlSessionFactory.openSession() 从 SqlSessionFactory 中获取一次会话, 然后可以从会话中获取出接口(BlogMapper)来,这里是不是有点好奇,明明这就是一个接口,也没有实现类,怎么就可以get出一个接口对象来?获取出接口来,然后就可以调用接口中的方法, 根据id查询出数据来.

可以看到,根据从官网写的一个列子,从表面来看,代码量并不是很多.   所以接下来点去源码,去跟进源码中的每个方法,到底做了些什么事情.



#### Source Function

​    	接下来解析下我们写的每行代码, MyBatis源码做了什么?

​        

​		**读取配置文件**

```java
 InputStream mybatisInputStream = Resources.getResourceAsStream("mybatis-config.xml");
```





org.apache.ibatis.io.Resources (Class).

可以看到MyBatis源码还写了一个 ClassLoader的包装类，通过ClassLoaderWrapper包装类来讲配置文件转化为InputSream.

如果返回的InputStream是null，就会抛出IOException来.

```java
/**
 * Returns a resource on the classpath as a Stream object
 *
 * @param loader   The classloader used to fetch the resource
 * @param resource The resource to find
 * @return The resource
 * @throws java.io.IOException If the resource cannot be found or read
 */
private static ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper();

public static InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
  // 利用 ClasssLoaderWrapper.  
  InputStream in = classLoaderWrapper.getResourceAsStream(resource, loader);
  if (in == null) {
    throw new IOException("Could not find resource " + resource);
  }
  return in;
}
```





于是我们接着看  ClassLoaderWrapper 是怎么 读取配置文件 & 转化为 InputStream 流的.

```java
// 这里返回的是 ClassLoader的数组,如果对ClassLoader不是很了解的话,可以先去百度了解下.
ClassLoader[] getClassLoaders(ClassLoader classLoader) {
  return new ClassLoader[]{
      // 传递进来的 
      classLoader,
      // 默认的 ClassLoader
      defaultClassLoader,
      // 根据当前线程获取出来的
      Thread.currentThread().getContextClassLoader(),
      // 根据当前 Class 获取出来的.
      getClass().getClassLoader(),
      // 系统的ClassLoader.
      systemClassLoader};
}


// 获取到了 classLoader的数组,然后对其进行迭代.
// 也就是使用 ClassLoader的  getResourceAsStream 方法,来讲 mybatis-config.xml
// 配置文件转化为 InputStream.
// 最后如果获取到InputStream都是null的话,那么返回的也就是null了.
// 根据上面的说法,返回的如果是null的话,就会出 IOException来.
InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
    for (ClassLoader cl : classLoader) {
      if (null != cl) {

        // try to find the resource as passed
        InputStream returnValue = cl.getResourceAsStream(resource);

        // now, some class loaders want this leading "/", so we'll add it and try again if we didn't find the resource
        if (null == returnValue) {
          returnValue = cl.getResourceAsStream("/" + resource);
        }

        if (null != returnValue) {
          return returnValue;
        }
      }
    }
    return null;
  }
```



 **至此,MyBatis读取 mybatis-config.xml 配置文件也就是解析完毕,可以看到采用了自己写的 ClassLoaderWrapper来操作的, 传递一种 ClassLoader进来,其默认的&系统&线程的,加一起也是有四种. 最后挨个进来迭代，满足条件的会读取文件转化为InputStream,如果都是null的话,也会返回null.**



------



  **获取SqlSessionFactory & 解析配置文件**



 new SqlSessionFactoryBuilder()  也是new了一个 SqlSessionFactoryBuild,个人理解  SqlSessionFactoryBuilder 就是专程用来构建出  SqlSessionFactory 来的,毕竟其后面有一个 build 方法.

Problem ?  这里有个问题,为什么不将 SqlSessionFactoryBuilder 的build 方法,修改为静态的 ? 如果修改为静态的话，那就不用new了,就可以直接 SqlSessionFactoryBuilder.build(mybatisInputStream);

```java
SqlSessionFactory sqlSessionFactory = new                     SqlSessionFactoryBuilder().build(mybatisInputStream);
```



​       **SqlSessionFactory** 

​       接着我们来到 SqlSessionFactory 的 build 方法.

​       这里在 finnaly 中, 可以看到 ErrorContext 利用了 ThreadLocal , 刚好这周出了 ThreadLocal 的视频.

​       视频地址 :  https://www.bilibili.com/video/BV1Ga4y1W72w

​       有兴趣&乐于学习&分享的,可以共同进步.

```java
public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
  try {
    // 利用传入进来的参数,new出来了一个 XMLConfigBuilder.
    XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
    return build(parser.parse());
  } catch (Exception e) {
    throw ExceptionFactory.wrapException("Error building SqlSession.", e);
  } finally {
    // 这里对 ThreadLocal 中进行 remove() 操作   
    ErrorContext.instance().reset();
    try {
      // 关闭流.  
      inputStream.close();
    } catch (IOException e) {
      // Intentionally ignore. Prefer previous error.
    }
  }
}
```



new XmlConfigBuilder() 方法:



```java

public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
  // 先new一个XMLMapperEntityResolver,再new一个XPathParser,然后就走到下面的构造函数.
  this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
}

// 最后还是走到这个构造方法中来.
private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
  super(new Configuration());
  ErrorContext.instance().resource("SQL Mapper Configuration");
  this.configuration.setVariables(props);
  this.parsed = false;
  this.environment = environment;
  this.parser = parser;
}


----------------------------------------
// new XPathParser代码:
    
  public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
    // 普通的构造方法.
    // 对 XPathParser的validation/entityResolver/variables/xpath
    // 的属性进行赋值操作.
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(inputStream));
  }    


// createDocument 方法
  private Document createDocument(InputSource inputSource) {
    // important: this must only be called AFTER common constructor
    try {
      // 这里通过debug看,返回的对象是DocumentBuilderFactoryImpl
      // 也就是其实现类.  
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     // 对 factory 的 features(HashMap) 添加值,   
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
     // 对 factory 的 validating 进行赋值  
      factory.setValidating(validation);
	 // 这下面都是对 factory的属性进行赋值操作.	
      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);
		
      // 可以看到 return new DocumentBuilderImpl
      // 最后返回的也是其实现类. 
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setEntityResolver(entityResolver);
      // 设置错误的handler,可以看到ErrorHandler是接口,这里是匿名实现的
      // 也就是直接new了接口,然后重写其方法.  
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          // NOP
        }
      });
      //   DocumentBuilderImpl 的 parse 解析方法
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

-------------
//   builder.parse(inputSource)

    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException(
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN,
                "jaxp-null-input-source", null));
        }
    // fSchemaValidator 是 null ,跳过.
        if (fSchemaValidator != null) {
            if (fSchemaValidationManager != null) {
                fSchemaValidationManager.reset();
                fUnparsedEntityHandler.reset();
            }
            resetSchemaValidator();
        }
  // 使用 xml 的相关类对 is 进行解析  
        domParser.parse(is);
 //  ?   
        Document doc = domParser.getDocument();
 // ? 这些解析 Document 的地方.....   
        domParser.dropDocumentReferences();
        return doc;
    }    


---------------
// 最后看到 this 构造函数.

  private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
    /** new Configuration() 中,TypeAliasRegistry typeAliasRegistry中的 typeAliases,
    *   在初始化这个对象的时候,就默认设置了一些别名配置.
    *   初始化的时候,还有对 LanguageDriverRegistry 的 LANGUAGE_DRIVER_MAP 赋值.
    *  父类 :  BaseBuilder抽象类.
    *  然后调用super方法,将configuration赋值父类的configuration
    *  同时将 configuration的typeAliasRegistry和typeHandlerRegistry也赋值
    *  给当前的这个对象.
    *   
    */
    super(new Configuration());
    // instance() 方法是往 ThreadLocal里面去set了一个ErrorContext
    // 最后会在finnaly中进行remove掉.
    ErrorContext.instance().resource("SQL Mapper Configuration");
    // 将 props 赋值到 configuration 的 variable 参数.
    this.configuration.setVariables(props);
    // 表示还没有被解析
    this.parsed = false;
    this.environment = environment;
    this.parser = parser;
  }    
    
```

 到这里,就可以看到 this构造方法以及其之前还有new对象的方法,都已经走完了.  这上面的方法,基本都是再为后面的解析xml文件做准备, 并且还有一些初始化数据的赋值操作.

 **Note** :   注意这里的 BaseBuilder是抽象类,其实现类是有好几个的. 这种写法,其实是将子类的一些common的方法,写入到 BaseBuilder父类中,然后不同的方法,需要子类自己去重写这个方法实现自己的业务逻辑.  当然一些参数也是可以放在抽象类中.



 **build(parser.parse())**  : 解析代码.  

​    parser.parse() 方法 : 

  

```java
public Configuration parse() {
  // 用 parsed 来控制是否解析过,如果已经解析过了,那就抛出异常.  
  if (parsed) {
    throw new BuilderException("Each XMLConfigBuilder can only be used once.");
  }
  parsed = true;
  //   
  parseConfiguration(parser.evalNode("/configuration"));
  return configuration;
}



---------------------------------
// parseConfiguration
// 这里 debug 可以看到 root 是    
  private void parseConfiguration(XNode root) {
    try {
      //issue #117 read properties first
      //   
      propertiesElement(root.evalNode("properties"));
      Properties settings = settingsAsProperties(root.evalNode("settings"));
      loadCustomVfs(settings);
      loadCustomLogImpl(settings);
      typeAliasesElement(root.evalNode("typeAliases"));
      pluginElement(root.evalNode("plugins"));
      objectFactoryElement(root.evalNode("objectFactory"));
      objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
      reflectorFactoryElement(root.evalNode("reflectorFactory"));
      settingsElement(settings);
      // read it after objectFactory and objectWrapperFactory issue #631
      environmentsElement(root.evalNode("environments"));
      databaseIdProviderElement(root.evalNode("databaseIdProvider"));
      typeHandlerElement(root.evalNode("typeHandlers"));
      mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
  }    
    
```

