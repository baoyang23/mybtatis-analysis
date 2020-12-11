##                      SqlSession & Execute Sql



#### 题记

​    从 SqlSessionFactory 中获取出 SqlSession 和 SqlSession 获取出我们定义的 mapper 接口，最后执行我们的sql 语句，到这里，包括解析文件, 获取SqlSessionFactory, SqlSession ,事务设置，执行 sql ,所以算上整套流程, MyBatis 大致的流程点还是比较好阅读的. 

​     那么，从这里开始就看 MyBatis 是执行 Sql 的， 在执行的Sql 的时候，如果是查询的话, 那么会不会有cache呢？



####  方法

​     先说获取 SqlSession 的方法,可以直接定位下面类的方法来看, 该方法是获取 SqlSession的.

​      org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource



   **获取 SqlSession**

 看过最初讲解的时候，是有对该方法进行说明的.   这里就再大致的讲述下其流程.

```java
private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
  Transaction tx = null;
  try {
// 获取Environment       
    final Environment environment = configuration.getEnvironment();
      
// 根据  Environment 来获取 事务工场, 既然有事务工厂的话, 那么该工厂肯定要生产事务.     
    final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
// 这里就是根据工厂来生产事务.      
    tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
// 获取执行器.      
    final Executor executor = configuration.newExecutor(tx, execType);
// 利用上面的参数,创建了一个 默认的 SqlSession会话.      
    return new DefaultSqlSession(configuration, executor, autoCommit);
  } catch (Exception e) {
    closeTransaction(tx); // may have fetched a connection so lets call close()
    throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
  } finally {
    ErrorContext.instance().reset();
  }
}
```

​       

该方法理解起来的话,主要是看其获取事务工厂，然后从事务工厂里面获取出事务，接着就是获取执行器，最后讲configuration/执行器/autoCommit是否自动提交事务.

  

  **session.getMapper(BlogMapper.class)**

从session中获取出mapper接口.  可以直接在下面提供的方法打上断点,或者直接看.

 org.apache.ibatis.binding.MapperRegistry#getMapper 



```java
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    
// 这里可以看到 kownMappers中已经存储好了之前加载好了的Mapper接口类信息.
// key : 该class.  value:  MapperProxyFactory, value 的mapperInterface中
//    存了该Mapper的接口.
  final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
// 如果是null的话,那就是没有,抛出异常来.    
  if (mapperProxyFactory == null) {
    throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
  }
  try {
// 这就是用 Proxy.newProxyInstance 来创建该接口的实现类.      
    return mapperProxyFactory.newInstance(sqlSession);
  } catch (Exception e) {
    throw new BindingException("Error getting mapper instance. Cause: " + e, e);
  }
}
```

   

  这里最后可以打印出我们获取的Mapper地址池,可以很明显的看到是通过代理生成的. 所以这里根据 class 从之前已经加载好的地方获取 MapperProxyFactory , 然后调用 Proxy.newProxyInstance 来实体化接口，这里注意，我们都清楚接口是不可以new出来的.



  **blogMapper.selectBlog(1)** 

​    最后来到 查询方法，看下 MyBatis 是怎么操作的.

​    这里建议 debug 来一步一步的跟进代码

   

​    org.apache.ibatis.binding.MapperProxy#invoke

  

```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  try {
    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    } else {
      return cachedInvoker(method).invoke(proxy, method, args, sqlSession);
    }
  } catch (Throwable t) {
    throw ExceptionUtil.unwrapThrowable(t);
  }
}



-------------------
org.apache.ibatis.binding.MapperProxy.PlainMethodInvoker#invoke
// 接下来走到这里来.    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
      return mapperMethod.execute(sqlSession, args);
    }    

//   org.apache.ibatis.binding.MapperMethod#execute
// 紧接着就是跟进到这里了. 下面可以着重分析该方法了
	

```



**MapperMethod#execute**

 可以看到这里是根据执行sql的 INSERT/UPDATE/DELETE/SELECT来分别进行操作的.

```java
public Object execute(SqlSession sqlSession, Object[] args) {
  Object result;
  switch (command.getType()) {
    case INSERT: {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
      break;
    }
    case UPDATE: {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
      break;
    }
    case DELETE: {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
      break;
    }
 
 // 处理 SELECT 分支         
    case SELECT:
      
      if (method.returnsVoid() && method.hasResultHandler()) {
// 该if分支,是满足返回是void并且 resultHandlerIndex(有待更新其具体作用)不是 null.    
// 最后是将result设置为null.          
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
// 返回值是多个.          
          
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
// 返回值是 Map          
          
        result = executeForMap(sqlSession, args);
      } else if (method.returnsCursor()) {
// 返回值是 cursor.          
          
        result = executeForCursor(sqlSession, args);
      } else {
// 不是上面的所有情况. 然后从selectOne中看,这里是只查询一个的情况.
// args 是传入进来的参数.
// org.apache.ibatis.reflection.ParamNameResolver#getNamedParams
// getNamedParams是对传入进来的参数进行判断处理
// 这里三种解析情况 : 1(没有参数)  2(至于一个参数)    3(多个参数)
// 最后这里获取出来的 param 的只就是1          
        Object param = method.convertArgsToSqlCommandParam(args);
// org.apache.ibatis.session.defaults.DefaultSqlSession#selectOne(java.lang.String, java.lang.Object)
// getName 获取出  com.iyang.mybatis.mapper.BlogMapper.selectBlog (namespace+id)
// 有意思的是,其实表面是走的selectOne,最后也是走到selectList中来.
// 获取出来的 MappedStatement,也是之前我们提到的解析 mapper 文件
// org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
// 看 query , 先获取出 sql 语句来, 也就是 BoundSql 对象
// 接着就是 createCacheKey 方法,org.apache.ibatis.executor.BaseExecutor#createCacheKey     // 最后也是跟到该方法中来了. 最后根据 ms等创建出来了一个 Cache 对象
// org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
// 最后看到,query的时候,最后就走入到了 Cache 执行器里面来了.如果能cache执行器里面获取出,就会不走往后走了.
// 如果是第一次的话,那么cache是肯定没有的,就会往后查询.
// org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
// 最后看到这个方法,该方法中还是有去 localCache 中获取,也就是说 BaseExecutor 的 localCache就相当于一个cache一样,从名字上看,这是一个本地的cache.
// 获取配置,创建一个 StatementHandler 对象出来, 注意这里的在创建这个对象之后,是走了interceptorChain.pluginAll(statementHandler);也就是说在真正查询之前,还是走了一次插件的方法.   
// org.apache.ibatis.executor.SimpleExecutor#prepareStatement          
// 获取 Connection ,  再获取  Statement (获取的时候还会获取事务超时时间). 注意在获取 Connection的时候,如果是连接不上数据库,那么在 getConnection 的时候，就会抛出异常来了.
// 最后在 org.apache.ibatis.executor.statement.PreparedStatementHandler#query 中
// 执行 sql 语句. 
// 最后是可以看到,在 org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSets 进行字段以及对象之间的转化操作.
// 最后再返回的时候可以看到,如果最后不是一个的话,是会有异常跑出来的.          
        result = sqlSession.selectOne(command.getName(), param);
        if (method.returnsOptional()
            && (result == null || !method.getReturnType().equals(result.getClass()))) {
  // 这里是对没有查询出值的情况,进行默认null值处理.          
          result = Optional.ofNullable(result);
        }
      }
      break;
    case FLUSH:
      result = sqlSession.flushStatements();
      break;
    default:
      throw new BindingException("Unknown execution method for: " + command.getName());
  }
  if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
    throw new BindingException("Mapper method '" + command.getName()
        + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
  }
  return result;
}
```

  

​    最后返回的 result 就是我们 blogMapper.selectBlog() 方法 的返回值. 

