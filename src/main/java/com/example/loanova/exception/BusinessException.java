package com.example.loanova.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final Map<String, String> errors;

  public BusinessException(String message) {
    super(message);
    this.errors = null;
  }

  public BusinessException(String message, Map<String, String> errors) {
    super(message);
    this.errors = errors;
  }
}
