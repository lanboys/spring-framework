package org.springframework.jdbc.tx.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.tx.service.MultiTxService;
import org.springframework.jdbc.tx.service.SingleTxService;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionStatus;

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
    executeSql("multiTxService");
  }

  private void executeSql(String name) {
    String sql = "insert into T_TEST (NAME) values ('" + name + "');";
    jdbcTemplate.execute(sql);
  }

  // REQUIRED	如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
  // 瞎搞保存点的话，就能把 REQUIRED 变成 NESTED
  // 结论就是：我们不要乱使用 保存点
  @Override
  public void savepoint1() {
    // 第一个特例：如果在内部事务中手动新增一个保存点，那么就不会被标记 rollback-only，因为异常而导致的回滚，只会回滚到最近一个保存点位置，
    // 所以事务还是会正常提交。跟 NESTED 的实现原理是一样的
    executeSql("aa");
    try {
      singleTxService.savepoint(true, true);
    } catch (Exception e) {
      System.out.println("multiRequired(): " + e.getLocalizedMessage());
    }
    // 不会被标记 rollback-only，事务正常提交了，结果是 [{NAME=aa}, {NAME=bb}]
    executeSql("bb");
  }

  // REQUIRED	如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
  @Override
  public void savepoint2() {
    // 第二个特例：内部没有保存点，外部事务手动增加保存点，然后内部事务异常，被标记上了 rollback-only，那么是提交不了的，会回滚到外部事务的保存点上，保存点之前的
    // SQL并没有回滚，也没有提交。看最终结果就知道了（ 需要先注释这句代码 con.setAutoCommit(true)，这个设置会导致未提交的事务提交一次 ）

    executeSql("aa");

    // 设置保存点
    System.out.println("required(): 外部事务 手动设置保存点");
    DefaultTransactionStatus status = (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus();
    status.createAndHoldSavepoint();

    try {
      singleTxService.savepoint(true, false);
    } catch (Exception e) {
      System.out.println("multiRequired(): " + e.getLocalizedMessage());
    }
    // 被标记上了 rollback-only，所以需要回滚，但是只会回滚到保存点，事务不会提交，但是实际上查询数据结果却是: [{NAME=aa}]
    // 那就说明事务提交了，其实是因为   恢复自动提交，这个设置会导致未提交的事务提交一次，可注释后再次测试，会发现确实没有数据
    executeSql("bb");
  }

  // https://www.cnblogs.com/duanxz/p/4746892.html
  // https://mp.weixin.qq.com/s/6tRPXwXnWUW4mVfCdBlkog
  // https://mp.weixin.qq.com/s/np_q8mQTvvjBBs5cxrHFHQ

  // REQUIRED	如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
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

    executeSql();
    // 2 try catch，内部事务如果抛了异常，那么整个事务都将回滚，即使 try catch 也没用，因为内部事务标记了 rollback-only，在提交检查时，还是会回滚
    // 最终抛的异常：Transaction rolled back because it has been marked as rollback-only 非常常见的错误

    // 特例见上面的方法
    try {
      singleTxService.required(throwException);
    } catch (Exception e) {
      System.out.println("multiRequired(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  // NESTED	如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；
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

    executeSql();

    // 2 try catch， 内部事务如果抛了异常，只是回滚到了保存点，不会影响外部事务
    try {
      singleTxService.nested(throwException);
    } catch (Exception e) {
      System.out.println("multiNested(): " + e.getLocalizedMessage());
      // 隔离失败，隔离失败的业务而不影响流程继续走
      // 这里业务上可以调用其他分支
    }
    executeSql();
  }

  // REQUIRES_NEW	创建一个新的事务，如果当前存在事务，则把当前事务挂起
  // 可以隔离失败，不能保证同时成功或者失败
  @Override
  public void multiRequiresNew(boolean tryCatch, boolean throwException) {
    // 1 不 try catch，内部事务如果抛了异常，在这里也会继续抛出，导致外部事务回滚(还需要看 rollback-for 配置)
    if (!tryCatch) {
      executeSql();
      singleTxService.requiresNew(throwException);
      executeSql();
      return;
    }

    executeSql();
    // 2 try catch，内部事务如果抛了异常，不影响外部事务，至于到底要不要 try catch 要看具体的业务需求了
    try {
      singleTxService.requiresNew(throwException);
    } catch (Exception e) {
      // 隔离失败
      System.out.println("multiRequiresNew(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  // SUPPORTS	如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
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

    executeSql();
    try {
      singleTxService.supports(throwException);
    } catch (Exception e) {
      System.out.println("supports(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  // MANDATORY	如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
  @Override
  public void multiMandatory(boolean tryCatch, boolean throwException) {
    // 抛异常后结果跟 required 类似，因为都是在同一个事务内
    if (!tryCatch) {
      executeSql();
      singleTxService.mandatory(throwException);
      executeSql();
      return;
    }

    executeSql();
    try {
      singleTxService.mandatory(throwException);
    } catch (Exception e) {
      System.out.println("mandatory(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  // NOT_SUPPORTED	以非事务方式运行，如果当前存在事务，则把当前事务挂起
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

    executeSql();
    try {
      singleTxService.notSupported(throwException);
    } catch (Exception e) {
      System.out.println("notSupported(): " + e.getLocalizedMessage());
    }
    executeSql();
  }

  // NEVER	以非事务方式运行，如果当前存在事务，则抛出异常
  @Override
  public void multiNever(boolean tryCatch, boolean throwException) {
    // 准备创建内部事务的时候就抛异常了 Existing transaction found for transaction marked with propagation 'never'
    if (!tryCatch) {
      executeSql();
      singleTxService.never(throwException);
      executeSql();
      return;
    }

    executeSql();
    try {
      singleTxService.never(throwException);
    } catch (Exception e) {
      System.out.println("never(): " + e.getLocalizedMessage());
    }
    executeSql();
  }
}
