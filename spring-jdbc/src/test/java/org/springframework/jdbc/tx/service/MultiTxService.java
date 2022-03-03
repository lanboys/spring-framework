package org.springframework.jdbc.tx.service;

public interface MultiTxService {

  void multiRequired(boolean tryCatch, boolean throwException);

  void multiSupports(boolean tryCatch, boolean throwException);

  void multiMandatory(boolean tryCatch, boolean throwException);

  void multiRequiresNew(boolean tryCatch, boolean throwException);

  void multiNotSupported(boolean tryCatch, boolean throwException);

  void multiNever(boolean tryCatch, boolean throwException);

  void multiNested(boolean tryCatch, boolean throwException);
}
