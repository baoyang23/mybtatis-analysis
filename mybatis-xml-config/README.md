## 			MyBatis 解析  xml 配置文件



####  题记

​     对于配置文件的解析, 还是相对比较好理解的, 就是读取配置文件, 然后在代码需要的地方给使用到.

​     这里,可以扩展下, Spring / SpringBoot 等是怎么读取配置文件呢 ? 并且配置文件还是有 xml / properties/yaml 等格式的 ， 其读取代码是怎么写的 ?  然后基于 阿波罗(携程开源) 的配置中心 , 其实现配置又是怎么实现的呢 ?  然后这里，看了 Mybatis 读取配置文件, 后续再出 Spring 配置文件的时候，如果二者读取配置进行对比, 你个人更倾向使用代码呢 ?  

​      所以,这里就开启读取 Mybatis 是如何解析配置文件的操作.



#### 配置文件

​    这里的配置文件解读,是根据 MyBatis官网来一步一步的解析阅读. 如果有官网没有涉及到的,发现了也会在后续加上去的.  解析多行代码, 才能理解 何为优秀.  

   

​    **标签一  :   properties**

​	

​	org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration  --->   propertiesElement(root.evalNode("properties"))  方法中来.

​    

```java
// 这里传入进来的 XNode 的值,就是我们写的 properties 标签.
// 可以看到 XNode的属性,name标签的名字,attributes就是key/value属性
// 比如这里: key 就是 resource , value 就是 ./db.properties.
private void propertiesElement(XNode context) throws Exception {
  if (context != null) {
// 这里调用的node.getChildNodes(),如果有点话,会遍历挨个解析,最后封装成为key/value结构.      
    Properties defaults = context.getChildrenAsProperties();
// 获取 resource / url 二者的值.      
    String resource = context.getStringAttribute("resource");
    String url = context.getStringAttribute("url");
// 如果二者都是null,就会抛出异常来.      
    if (resource != null && url != null) {
      throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
    }
      
 // 这里先处理resource,再处理url,也就是有可能url会覆盖掉resource的内容.
 // 二者读取的方式不一样,前者是根据 resource开始读,url是根据绝对路径开始读.
 // 最后 defaults 里面放入的全部是 key/value 对应的键值对
 // 也就是db.properties中的 key / value 相对应i起来.     
    if (resource != null) {
      defaults.putAll(Resources.getResourceAsProperties(resource));
    } else if (url != null) {
      defaults.putAll(Resources.getUrlAsProperties(url));
    }
  
// 这里看的是 xml 里面是不是直接有 porperties 配置.     
// 如果有的话,就会putAll进去.      
    Properties vars = configuration.getVariables();
    if (vars != null) {
      defaults.putAll(vars);
    }
// 最后吧 defaults,也就是properties给放入到 BaseBuilder 和 Confifuration中去.      
    parser.setVariables(defaults);
    configuration.setVariables(defaults);
  }
}


-----------------------------
//  如何让 Properties vars = configuration.getVariables(); 有值呢 ?
//  如果只是单个的 MyBatis 项目的话, 就自己手动new一个properties对象
//  然后key输入自己要覆盖掉的key就可以了
        Properties dbConfigProperties = new Properties();
        dbConfigProperties.setProperty("jdbc.password","GavinYang");

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisInputStream,dbConfigProperties);    
    
```





​	  **标签二 :  settings**



​      这是 MyBatis对 settings 的操作. 		

​      具体的 settings 中每项配置参考官网链接 :  https://mybatis.org/mybatis-3/configuration.html#properties

```java
// 解析 setting ---> 转化为 key /value
Properties settings = settingsAsProperties(root.evalNode("settings"));
// 
loadCustomVfs(settings);
loadCustomLogImpl(settings);
```

   

  settingsAsProperties 方法

​    可以看到, 该方法就是进行加载,转化为key/value键值对类型, 然后对其key检验是否在

​     Configuration 中都有 set 方法.

  Notes :  为了验证下,  <setting name="nnnnn" value="GavinYang"/> 我们加上一个没有的标签, 可以看到下面的异常.  所以我们看到这种异常的时候，是可以去检查下是不是名字什么有问题.

### Cause: org.apache.ibatis.builder.BuilderException: Error parsing SQL Mapper Configuration. Cause: org.apache.ibatis.builder.BuilderException: The setting nnnnn is not known.  Make sure you spelled it correctly (case sensitive).

```java
private Properties settingsAsProperties(XNode context) {
  if (context == null) {
    return new Properties();
  }
 // 对 settings 下的 setting 进行解析 并且 转化为 key / value 操作.   
  Properties props = context.getChildrenAsProperties();
  // Check that all settings are known to the configuration class
 // 对 Configuration 进行校验, 确认上面的 props 中的key 在 Configuration
// 中是都有set 方法的,目测是后面反射需要使用到.    
  MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
  for (Object key : props.keySet()) {
    if (!metaConfig.hasSetter(String.valueOf(key))) {
      throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
    }
  }
  return props;
}
```

​    

 loadCustomVfs(settings)  方法

   该方法,主要就是读取 vfsImpl 对用的value,切割下,然后用 classForName 来获取 class,

   最后赋值到 configuration 中去. 这里算是对 vfs 的一种自定义的扩展,虽然目前还不太清楚vfs具体作用.

```java
private void loadCustomVfs(Properties props) throws ClassNotFoundException {
  // 获取 vfsImpl 的 value.  
  String value = props.getProperty("vfsImpl");
  if (value != null) {
   // 根据 , 进行切割.   
    String[] clazzes = value.split(",");
    for (String clazz : clazzes) {
      if (!clazz.isEmpty()) {
        @SuppressWarnings("unchecked")
        // 反射,获取出 Class , 最后赋值到 configuration 中去.  
        Class<? extends VFS> vfsImpl = (Class<? extends VFS>)Resources.classForName(clazz);
        configuration.setVfsImpl(vfsImpl);
      }
    }
  }
}
```



loadCustomLogImpl(settings) 方法



```java
private void loadCustomLogImpl(Properties props) {
  Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
  // 将 log set 到 configuration 中去.  
  configuration.setLogImpl(logImpl);
}

-----------------------
// resolve 最后如果不是 null 的话,
org.apache.ibatis.type.TypeAliasRegistry#resolveAlias

 // 就会走到这里,这里可以看先是在 typeAliases(HashMap) 中判断下,如果存在就直接获取
// 如果不存在就用 Resources.ClassForName来操作
// 这里的 HashMap就类似于,记录之前是否已经加载了或者预热.
// 如果是用来做cache的话, 那就应该最后会在 return 之前继续把值给放入进去.    
  public <T> Class<T> resolveAlias(String string) {
    try {
      if (string == null) {
        return null;
      }
      // issue #748
      String key = string.toLowerCase(Locale.ENGLISH);
      Class<T> value;
      if (typeAliases.containsKey(key)) {
        value = (Class<T>) typeAliases.get(key);
      } else {
        value = (Class<T>) Resources.classForName(string);
      }
      return value;
    } catch (ClassNotFoundException e) {
      throw new TypeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
    }
  }    


------------
// 如果我们在配置文件中没有定义的话,这里默认是null,也就是说不会set进去.    
  public void setLogImpl(Class<? extends Log> logImpl) {
    if (logImpl != null) {
      this.logImpl = logImpl;
      LogFactory.useCustomLogging(this.logImpl);
    }
  }    
```



 **标签三 :  <typeAliases>**

​     关于别名的配置.

```java
typeAliasesElement(root.evalNode("typeAliases"));
```



```java
private void typeAliasesElement(XNode parent) {
  if (parent != null) {
   // 对 typeAliases 下的子标签进行迭代.
   // 分为是 package 和非 package   
    for (XNode child : parent.getChildren()) {
      if ("package".equals(child.getName())) {
       // 获取你输入的包   
        String typeAliasPackage = child.getStringAttribute("name");
        configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
      } else {
 // <typeAlias type="com.iyang.mybatis.pojo.TbBlog" alias="TbBlog" />
 // 这里就是对这种进行解析的         
        String alias = child.getStringAttribute("alias");
        String type = child.getStringAttribute("type");
        try {
          Class<?> clazz = Resources.classForName(type);
   // 如果没写别名,就只传入 clazz.         
          if (alias == null) {
            typeAliasRegistry.registerAlias(clazz);
          } else {
   // 写了别名,就别名和clazz一起传入进来.           
            typeAliasRegistry.registerAlias(alias, clazz);
          }
        } catch (ClassNotFoundException e) {
          throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
        }
      }
    }
  }
}


----------------
// 这里可以看到是根据 packageName 来 register进来的.    
  public void registerAliases(String packageName, Class<?> superType) {
    // new 一个解析器工具类
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    // 获取包的path,然后获取该包下的文件,如果文件是.class结尾的话
    // 最后在 ResolverUtil 中matchess是有该包下的全名称.
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    // 这里返回的是上一步说的 matches
    Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
    for (Class<?> type : typeSet) {
      // Ignore inner classes and interfaces (including package-info.java)
      // Skip also inner classes. See issue #6
      // 如果不是接口,不是内部类等条件的话,就走  registerAlias 方法
      if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
 // 先获取类名字,判断该类上有没有 @Alias 注解,如果有注解的话,就用注解的值作为缩写的.
 // 最后判断是不是null,是null就会抛出异常来.最后将上面获取出来的缩写名字,转化为大写.
 // 如果此时 typeAliases 是已经有了该值的话,就会抛出异常来.否则就放入到typeAliases来
 // private final Map<String, Class<?>> typeAliases = new HashMap<>();
 // 可以看到 typeAliases 是一个HashMap,并且其存储的Key/Value还是蛮明显的.         
        registerAlias(type);
      }
    }
  }    
    
```

