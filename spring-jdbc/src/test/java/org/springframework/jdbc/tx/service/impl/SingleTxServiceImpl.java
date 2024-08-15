package org.springframework.jdbc.tx.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.tx.service.SingleTxService;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class SingleTxServiceImpl implements SingleTxService {

  JdbcTemplate jdbcTemplate;

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private void executeSql(boolean throwException) {
    String sql = "insert into T_TEST (NAME) values ('singleTxService');";
    jdbcTemplate.execute(sql);

    if (throwException) {
      throw new RuntimeException("内部事务异常");
    }
  }

  @Override
  public void savepoint(boolean throwException, boolean createAndHoldSavepoint) {
    if (createAndHoldSavepoint) {
      // 设置保存点
      System.out.println("required(): 内部事务 手动设置保存点");
      DefaultTransactionStatus status = (DefaultTransactionStatus) TransactionAspectSupport.currentTransactionStatus();
      status.createAndHoldSavepoint();
    }

    executeSql(throwException);
  }

  @Override
  public void required(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void supports(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void mandatory(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void requiresNew(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void notSupported(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void never(boolean throwException) {
    executeSql(throwException);
  }

  @Override
  public void nested(boolean throwException) {
    executeSql(throwException);
  }
}
