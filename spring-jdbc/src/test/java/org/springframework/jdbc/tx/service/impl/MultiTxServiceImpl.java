package org.springframework.jdbc.tx.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.tx.service.MultiTxService;
import org.springframework.jdbc.tx.service.SingleTxService;

public class MultiTxServiceImpl implements MultiTxService {

  JdbcTemplate jdbcTemplate;

  SingleTxService singleTxService;

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void setSingleTxService(SingleTxService singleTxService) {
    this.singleTxService = singleTxService;
  }

  private void executeSql() {
    String sql = "insert into T_TEST (NAME) values ('multiTxService');";
    jdbcTemplate.execute(sql);
  }

  //https://www.cnblogs.com/duanxz/p/4746892.html
  //https://mp.weixin.qq.com/s/6tRPXwXnWUW4mVfCdBlkog
  //https://mp.weixin.qq.com/s/np_q8mQTvvjBBs5cxrHFHQ

  //NESTED	如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；
  //        如果当前没有事务，则该取值等价于 TransactionDefinition.PROPAGATION_REQUIRED
  @Override
  public void multiNested(boolean tryCatch, boolean throwException) {
    // 1 不 try catch，内部事务如果抛了异常，在这里也会继续抛出，不再往下执行，最终整个事务回滚(还需要看 rollback-for 配置)
    if (!tryCatch) {
      executeSql();
      singleTxService.nested(throwException);// 联合成功
      executeSql();
      return;
    }

    // 2 try catch， 内部事务如果抛了异常，只是回滚到了保存点，不会影响外部事务
    try {
      executeSql();
      singleTxService.nested(throwException);
    } catch (Exception e) {
      System.out.println("multiNested(): " + e.getLocalizedMessage());
      // 隔离失败
      // 这里业务上可以调用其他分支
    }
    executeSql();
  }

  //REQUIRED	如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
  // 可以联合成功(失败)，无法隔离失败
  @Override
  public void multiRequired(boolean tryCatch, boolean throwException) {
    // 1 不 try catch，内部事务如果抛了异常，在这里也会继续抛出，不再往下执行，整个事务回滚(还需要看
    // rollback-for 配置)，所以不包起来更好，不用再执行后面逻辑
    // 最终抛的异常：内部事务的异常
    if (!tryCatch) {
      executeSql();
      singleTxService.required(throwException);
      executeSql();
      return;
    }

    // 2 try catch， 内部事务如果抛了异常，那么整个事务都将回滚，即使 try catch 也没用，因为内部事务标记了 rollback-only
    // 最终抛的异常：Transaction rolled back because it has been marked as rollback-only 非常常见的错误
    try {
      executeSql();
      singleTxService.required(throwException);
    } catch (Exception e) {
      System.out.println("multiRequired(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  //REQUIRES_NEW	创建一个新的事务，如果当前存在事务，则把当前事务挂起
  //可以隔离失败，不能保证同时成功或者失败
  @Override
  public void multiRequiresNew(boolean tryCatch, boolean throwException) {
    // 1 不 try catch，内部事务如果抛了异常，在这里也会继续抛出，导致外部事务回滚(还需要看 rollback-for 配置)
    if (!tryCatch) {
      executeSql();
      singleTxService.requiresNew(throwException);
      executeSql();
      return;
    }

    // 2 try catch，内部事务如果抛了异常，不影响外部事务，至于到底要不要 try catch 要看具体的业务需求了
    try {
      executeSql();
      singleTxService.requiresNew(throwException);
    } catch (Exception e) {
      //隔离失败
      System.out.println("multiRequiresNew(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  //SUPPORTS	如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
  // 非事务的方式：是指事务管理器不做一些事务操作，但数据库的默认事务还是有的，每条sql都是一个事务，都是一个新连接
  @Override
  public void multiSupports(boolean tryCatch, boolean throwException) {
    // 抛异常后结果跟 required 类似，因为都是在同一个事务内
    if (!tryCatch) {
      executeSql();
      singleTxService.supports(throwException);
      executeSql();
      return;
    }

    try {
      executeSql();
      singleTxService.supports(throwException);
    } catch (Exception e) {
      System.out.println("supports(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  //MANDATORY	如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
  @Override
  public void multiMandatory(boolean tryCatch, boolean throwException) {
    // 抛异常后结果跟 required 类似，因为都是在同一个事务内
    if (!tryCatch) {
      executeSql();
      singleTxService.mandatory(throwException);
      executeSql();
      return;
    }

    try {
      executeSql();
      singleTxService.mandatory(throwException);
    } catch (Exception e) {
      System.out.println("mandatory(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  //NOT_SUPPORTED	以非事务方式运行，如果当前存在事务，则把当前事务挂起
  // 非事务的方式：是指事务管理器不做一些事务操作，但数据库的默认事务还是有的，每条sql都是一个事务，都是一个新连接
  // 跟 REQUIRES_NEW 类似，只不过是根据sql条数来开启自动提交事务
  @Override
  public void multiNotSupported(boolean tryCatch, boolean throwException) {
    // 抛异常后结果跟 requiresNew 类似，因为都不在同一个事务内
    if (!tryCatch) {
      executeSql();
      singleTxService.notSupported(throwException);
      executeSql();
      return;
    }

    try {
      executeSql();
      singleTxService.notSupported(throwException);
    } catch (Exception e) {
      System.out.println("notSupported(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  //NEVER	以非事务方式运行，如果当前存在事务，则抛出异常
  @Override
  public void multiNever(boolean tryCatch, boolean throwException) {
    // 准备创建内部事务的时候就抛异常了 Existing transaction found for transaction marked with propagation 'never'
    if (!tryCatch) {
      executeSql();
      singleTxService.never(throwException);
      executeSql();
      return;
    }

    try {
      executeSql();
      singleTxService.never(throwException);
    } catch (Exception e) {
      System.out.println("never(): " + e.getLocalizedMessage());
    }
    executeSql();
  }
}
