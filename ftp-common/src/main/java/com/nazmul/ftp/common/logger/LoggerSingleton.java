package com.nazmul.ftp.common.logger;

import org.apache.log4j.Logger;

/**
 * Using Singleton design pattern for Logger
 *
 * LoggerSingleton properties:
 * control concurrent access to a shared resource
 * access to the resource will be requested from multiple, disparate parts of the system
 * there can be only one object
 *
 * Intent
 * Ensure a class has only one instance, and provide a global point of access to it.
 * Encapsulated "just-in-time initialization" or "initialization on first use".
 */
public enum LoggerSingleton {
  INSTANCE;

  private static final Logger LOGGER = Logger.getLogger(LoggerSingleton.class);

  private LoggerSingleton() {
  }

  public static LoggerSingleton getInstance() {
    return INSTANCE;
  }

  public void info(String msg) {
    LOGGER.info(msg);
  }

  public void error(String msg) {
    LOGGER.error(msg);
  }

  public void debug(String msg) {
    LOGGER.debug(msg);
  }

  public void warn(String msg) {
    LOGGER.warn(msg);
  }

}
