## 				MyBatis 的 Mapper文件解析



####  题记

​    MyBatis是如何对 Mapper 文件中的sql进行处理呢？ 虽然上篇解析 mybatis-config.xml 是有进行说明的, 但是应该拿出来单独仔细解析下.  因为这个里面涉及到动态sql, 加上mapper文件自身也有很多标签内容,然后MyBatis是怎么读取出这些内容的呢？读取出来后,又是做了怎么样的处理, 然后达到了sql那种执行效果的呢？

​    意思也就是,Mapper + 动态sql , 内容还是有点多的, 并且也很重要, 是非常有必要拿出来单独的仔细讲解下的.



#### Target

​     	在之前对标签的进行解析的时候,是有对  <mappers> 标签进行一个初步的解析.  然后里面其实是很多内容还没填补很详细,所以特意记录下对 <mappers> 详细操作的. 那么，下文就开始操作吧.

​        

​         **org.apache.ibatis.builder.xml.XMLMapperBuilder#parse**

 主要来看这段解析的代码 : 

  

```java
public void parse() {
    
// 利用 org.apache.ibatis.session.Configuration 的 loadedResources
// 来判断是不是已经加载过了的.    
  if (!configuration.isResourceLoaded(resource)) {
    configurationElement(parser.evalNode("/mapper"));
// 这里添加到 loadedResources 中来,也就是用来控制是不是已经解析过了的.      
    configuration.addLoadedResource(resource);
    bindMapperForNamespace();
  }

  parsePendingResultMaps();
  parsePendingCacheRefs();
  parsePendingStatements();
}


// configurationElement 方法,
// 可以看到这个方法中,很多标签(namespace/parameterMa/resultMap/sql)
// 还有下面的select/insert/update/delete
// 这些熟悉的标签
  private void configurationElement(XNode context) {
    try {
      String namespace = context.getStringAttribute("namespace");
      if (namespace == null || namespace.equals("")) {
        throw new BuilderException("Mapper's namespace cannot be empty");
      }
// 将 namespace 赋值进去,也就是当前正在解析的 namespace.        
      builderAssistant.setCurrentNamespace(namespace);
        
// 这里是对缓存标签进行解析.        
      cacheRefElement(context.evalNode("cache-ref"));
      cacheElement(context.evalNode("cache"));
 
// 解析 parameterMap标签        
      parameterMapElement(context.evalNodes("/mapper/parameterMap"));
        
//         
      resultMapElements(context.evalNodes("/mapper/resultMap"));
      sqlElement(context.evalNodes("/mapper/sql"));
      buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
    } catch (Exception e) {
      throw new BuilderException("Error parsing Mapper XML. The XML location is '" + resource + "'. Cause: " + e, e);
    }
  }


```





​	**resultMapElements 方法 :** 

​    

```java

// 这里的 list 是 xml 文件中的所有 <resultMap> 标签文件.
private void resultMapElements(List<XNode> list) {
  for (XNode resultMapNode : list) {
    try {
//  有点好奇,该方法返回的 ResultMap 这边好像并没有参数,有点尴尬.
//  不过是已经存储在 org.apache.ibatis.session.Configuration#resultMaps 中.       
      resultMapElement(resultMapNode);
    } catch (IncompleteElementException e) {
      // ignore, it will be retried
    }
  }
}

----------
// 最后跟进到这个方法中来.
  private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings, Class<?> enclosingType) {
    ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
 
// 获取出 type , 这里我们获取出来的 type 是 TbBlog.    
    String type = resultMapNode.getStringAttribute("type",
        resultMapNode.getStringAttribute("ofType",
            resultMapNode.getStringAttribute("resultType",
                resultMapNode.getStringAttribute("javaType"))));
// 先判断 org.apache.ibatis.type.TypeAliasRegistry#typeAliases 中有没有,
// 如果没有的话,就会自己new一个出来.    
    Class<?> typeClass = resolveClass(type);
    if (typeClass == null) {
// TODO,如果没有话?        
      typeClass = inheritEnclosingType(resultMapNode, enclosingType);
    }
    Discriminator discriminator = null;
    List<ResultMapping> resultMappings = new ArrayList<>(additionalResultMappings);
    
 // 获取该 <resultMap> 下的子标签
// 那么这里也就是获取 <id> 和 <result> 这二个.    
    List<XNode> resultChildren = resultMapNode.getChildren();
    for (XNode resultChild : resultChildren) {
// 分为 constructor / discriminator / 其他 这三类情况        
      if ("constructor".equals(resultChild.getName())) {
        processConstructorElement(resultChild, typeClass, resultMappings);
      } else if ("discriminator".equals(resultChild.getName())) {
        discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
      } else {
 // 非前二者情况.         
        List<ResultFlag> flags = new ArrayList<>();
        if ("id".equals(resultChild.getName())) {
         // 如果标签是id的话,就会给flags添加ResultFlag.ID.
          flags.add(ResultFlag.ID);
        }
  //  将返回回来的 ResultMapping 添加进来.       
        resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
      }
    }
// 这里获取的是 <resultMap> 标签的 id 字段.    
    String id = resultMapNode.getStringAttribute("id",
            resultMapNode.getValueBasedIdentifier());
// 这里还可以使用 extends 属性, 不是看到这里, 都好奇还有这种标签.    
    String extend = resultMapNode.getStringAttribute("extends");
    Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
// 这里 new 了一个 ResultMapResolver 对象.   
    ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
    try {
// 这里最后就是 new 了一个 ResultMap 对象, 该对象的 id 是 namespace + 方法ID 拼接.
// 然后将该对象给添加到  org.apache.ibatis.session.Configuration#resultMaps 中来,
// key 就是其id, 最后就是根据 local / global 来分别进行二种情况检查.        
      return resultMapResolver.resolve();
    } catch (IncompleteElementException  e) {
      configuration.addIncompleteResultMap(resultMapResolver);
      throw e;
    }
  }    


//  buildResultMappingFromContext 方法
// 该方法是对 resultMap 中的字段进行解析.
  private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) {
    String property;
    if (flags.contains(ResultFlag.CONSTRUCTOR)) {
      property = context.getStringAttribute("name");
    } else {
      property = context.getStringAttribute("property");
    }
    String column = context.getStringAttribute("column");
    String javaType = context.getStringAttribute("javaType");
    String jdbcType = context.getStringAttribute("jdbcType");
    String nestedSelect = context.getStringAttribute("select");
    String nestedResultMap = context.getStringAttribute("resultMap", () ->
      processNestedResultMappings(context, Collections.emptyList(), resultType));
    String notNullColumn = context.getStringAttribute("notNullColumn");
    String columnPrefix = context.getStringAttribute("columnPrefix");
    String typeHandler = context.getStringAttribute("typeHandler");
    String resultSet = context.getStringAttribute("resultSet");
    String foreignColumn = context.getStringAttribute("foreignColumn");
    boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
      
// 获取 javaType , typeHandler , jdbcType 等对象.      
    Class<?> javaTypeClass = resolveClass(javaType);
    Class<? extends TypeHandler<?>> typeHandlerClass = resolveClass(typeHandler);
    JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
// org.apache.ibatis.builder.MapperBuilderAssistant#buildResultMapping(java.lang.Class<?>, java.lang.String, java.lang.String, java.lang.Class<?>, org.apache.ibatis.type.JdbcType, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Class<? extends org.apache.ibatis.type.TypeHandler<?>>, java.util.List<org.apache.ibatis.mapping.ResultFlag>, java.lang.String, java.lang.String, boolean)
// 可以看到这里传递进来的参数还是很多的.
// 最后返回 ResultMapping 对象,也就是说这么多参数&buildResultMapping方法中的参数,
//都设置到该对象中来了.     
    return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
  }

```



**SqlElement 方法**

   该方法可以很明显的感受到是对 <sql> 标签进行解析的.

```java
private void sqlElement(List<XNode> list) {
 // configuration 获取出来 dataBaseId是null,跳过此方法  
  if (configuration.getDatabaseId() != null) {
    sqlElement(list, configuration.getDatabaseId());
  }
//    
  sqlElement(list, null);
}
```

