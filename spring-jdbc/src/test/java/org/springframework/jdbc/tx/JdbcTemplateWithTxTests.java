/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.tx;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.tx.service.MultiTxService;
import org.springframework.jdbc.tx.service.SingleTxService;

import java.util.List;
import java.util.Map;

public class JdbcTemplateWithTxTests {

  MultiTxService multiTxService;

  SingleTxService singleTxService;

  JdbcTemplate jdbcTemplate;

  @Before
  public void setup() {
    BeanFactory beanFactory = new ClassPathXmlApplicationContext("JdbcTemplateWithTxTests.xml", JdbcTemplateWithTxTests.class);
    this.multiTxService = (MultiTxService) beanFactory.getBean("multiTxService");
    this.singleTxService = (SingleTxService) beanFactory.getBean("singleTxService");
    this.jdbcTemplate = (JdbcTemplate) beanFactory.getBean("jdbcTemplate");
    String sql = "drop table T_TEST if exists;";
    jdbcTemplate.execute(sql);
    sql = "create table T_TEST (NAME varchar(50) not null);";
    jdbcTemplate.execute(sql);
  }

  @After
  public void end() {
    String sql = "select * from T_TEST";
    List<Map<String, Object>> li = jdbcTemplate.queryForList(sql);
    System.out.println("end(): " + li);
  }

  //NESTED	如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；
  //        如果当前没有事务，则该取值等价于 TransactionDefinition.PROPAGATION_REQUIRED
  @Test
  public void nested() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 内部事务异常是否影响外部事务 主要看内部事务异常有没有 try catch
      multiTxService.multiNested(false, true);
    } else {
      singleTxService.nested(false);
    }
  }

  //REQUIRED	如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
  @Test
  public void required() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 主要测试内部事务异常后，会怎么样? 两个事务( 其实是同一个事务 )是 && 的关系，都成功整个才会成功
      // 外部有异常，肯定会回滚
      multiTxService.multiRequired(false, true);
    } else {
      singleTxService.required(false);
    }
  }

  //REQUIRES_NEW	创建一个新的事务，如果当前存在事务，则把当前事务挂起
  @Test
  public void requiresNew() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 内部事务异常是否影响外部事务 主要看内部事务异常有没有 try catch
      multiTxService.multiRequiresNew(true, true);
    } else {
      singleTxService.requiresNew(false);
    }
  }

  //SUPPORTS	如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
  // 非事务的方式：是指事务管理器不做一些事务操作，但数据库的默认事务还是有的，每条sql都是一个事务，都是一个新连接
  @Test
  public void supports() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 异常回滚机制跟required一样，是 && 的关系
      multiTxService.multiSupports(false, true);
    } else {
      singleTxService.supports(false);
    }
  }

  //MANDATORY	如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
  @Test
  public void mandatory() {
    boolean isExistingTransaction = false;
    if (isExistingTransaction) {
      // 异常回滚机制跟required一样，是 && 的关系
      multiTxService.multiMandatory(false, true);
    } else {
      singleTxService.mandatory(false);
    }
  }

  //NOT_SUPPORTED	以非事务方式运行，如果当前存在事务，则把当前事务挂起
  // 非事务的方式：是指事务管理器不做一些事务操作，但数据库的默认事务还是有的，每条sql都是一个事务，都是一个新连接
  // 跟 REQUIRES_NEW 类似，只不过是根据sql条数来开启自动提交事务
  @Test
  public void notSupported() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 跟 requiresNew 类似
      multiTxService.multiNotSupported(false, true);
    } else {
      singleTxService.notSupported(false);
    }
  }

  //NEVER	以非事务方式运行，如果当前存在事务，则抛出异常
  @Test
  public void never() {
    boolean isExistingTransaction = true;
    if (isExistingTransaction) {
      // 内部事务在创建事务的时候就抛异常了，还没有机会执行到手动抛异常的地方
      multiTxService.multiNever(false, false);
    } else {
      singleTxService.never(false);
    }
  }
}
