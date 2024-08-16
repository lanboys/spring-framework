package org.springframework.jdbc.tx.service;

public interface MultiTxService {

  void multiInnerNoTransaction(boolean tryCatch, boolean throwException);

  void multiSavepoint1();

  void multiSavepoint2();

  void multiRequired(boolean tryCatch, boolean throwException);

  void multiSupports(boolean tryCatch, boolean throwException);

  void multiMandatory(boolean tryCatch, boolean throwException);

  void multiRequiresNew(boolean tryCatch, boolean throwException);

  void multiNotSupported(boolean tryCatch, boolean throwException);

  void multiNever(boolean tryCatch, boolean throwException);

  void multiNested(boolean tryCatch, boolean throwException);
}
