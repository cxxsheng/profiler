package com.cxxsheng.profiler.utils.exceptions;

public class ProfilerRuntimeException extends RuntimeException{
  private static final long serialVersionUID = -4031179042919512718L;

  public ProfilerRuntimeException(String message) {
    super(message);
  }

  public ProfilerRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
