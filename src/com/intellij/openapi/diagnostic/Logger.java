package com.intellij.openapi.diagnostic;

import org.slf4j.LoggerFactory;

// Get control from intellij logcat system
// Warning! do not use intellij logcat jar package
public class Logger {
  private org.slf4j.Logger LOG;
  public static Logger getInstance(Class c){
    Logger instance = new Logger();
    instance.LOG = LoggerFactory.getLogger(c);
    return instance;
  }


  public void warn(String info){
    LOG.warn(info);
  }


}
