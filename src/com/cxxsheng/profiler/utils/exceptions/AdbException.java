package com.cxxsheng.profiler.utils.exceptions;

public class AdbException extends Exception {

  private static final long serialVersionUID = -2125948883209505276L;

  public AdbException(String message) {
    super(message);
  }

  public AdbException(String message, Throwable cause) {
    super(message, cause);
  }
}
