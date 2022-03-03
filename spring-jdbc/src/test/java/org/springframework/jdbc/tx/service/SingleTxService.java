package org.springframework.jdbc.tx.service;

public interface SingleTxService {

  void required(boolean throwException);

  void supports(boolean throwException);

  void mandatory(boolean throwException);

  void requiresNew(boolean throwException);

  void notSupported(boolean throwException);

  void never(boolean throwException);

  void nested(boolean throwException);
}
