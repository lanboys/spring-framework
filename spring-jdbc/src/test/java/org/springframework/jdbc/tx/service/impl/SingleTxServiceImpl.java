package org.springframework.jdbc.tx.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.tx.service.SingleTxService;

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
